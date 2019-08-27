package com.modosa.openhostseditor.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.modosa.openhostseditor.fragments.hostrule.Fragment;
import com.modosa.openhostseditor.fragments.hostrule.HidingScrollListener;
import com.modosa.openhostseditor.fragments.hostrule.RecyclerViewAdapter;
import com.modosa.openhostseditor.model.ExecuteAsRootBase;
import com.modosa.openhostseditor.model.HostRule;
import com.modosa.openhostseditor.model.HostsManager;

import java.io.IOException;
import java.util.List;

import me.vittorio_io.openhostseditor.R;

public class MainActivity extends BaseActivity {

    private static final int WRITE_BACKUP_ACTION = 0;
    public static List<HostRule> rules;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewRuleActivity();
            }
        });

        if (!ExecuteAsRootBase.isRootAvailable()) {
            new AlertDialog.Builder(this)
                    .setTitle("Root not detected")
                    .setMessage("Root was not detected, the application might not run as expected.\nDo you wish to continue anyway?")
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton(android.R.string.yes, null)
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).show();
        }

        refreshList();

    }

    private void refreshList() {
        try {
            if (rules == null) {
                rules = HostsManager.readFromFile();
            } else if (!rules.equals(HostsManager.readFromFile())) {
                rules = HostsManager.readFromFile();
            }
        } catch (IOException e) {
            // TODO error message
            haveASnack("Failed to read original hosts file.");
            e.printStackTrace();
        }

        RecyclerViewAdapter listViewAdapter = new RecyclerViewAdapter(rules, new Fragment.OnListFragmentInteractionListener() {
            @Override
            public void onListFragmentInteraction(HostRule item) {
                openEditRuleActivity(rules.indexOf(item));
            }
        });

        RecyclerView listView = findViewById(R.id.list);

        listView.swapAdapter(listViewAdapter, true);

        // source: https://stackoverflow.com/a/39145431
        listView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                fab.animate().translationY(fab.getHeight() + lp.bottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
            }

            @Override
            public void onShow() {
                fab.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            }
        });
    }

    private void openEditRuleActivity(int index) {
        Intent intent = new Intent(MainActivity.this, EditRuleActivity.class);
        Bundle b = new Bundle();
        b.putInt("key", index); //Your id
        intent.putExtras(b); //Put your id to your next Intent
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivityForResult(intent, 0);
        overridePendingTransition(R.anim.push_right_out, R.anim.push_right_in);
    }

    private void openNewRuleActivity() {
        openEditRuleActivity(-1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings: {
                return true;
            }
            case R.id.action_backup: {
                // There is no way of launching the callback
                // without asking for the permission at the moment,
                // so it's pointless to check if the permission has been granted.
                // int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                getPermissionAndExecute(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_BACKUP_ACTION);
                return true;
            }
            case R.id.action_edit: {
                openActivityForResult(ManualEditActivity.class);
                return true;
            }
            case R.id.action_restore: {
                openActivityForResult(RestoreActivity.class);
                return true;
            }
            case R.id.action_about: {
                openActivityForResult(AboutActivity.class);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refreshList();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == WRITE_BACKUP_ACTION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                try {
                    HostsManager.saveBackup();
                    haveASnack("Backup saved!");
                } catch (IOException e) {
                    haveASnack("Failed to save a backup");
                    e.printStackTrace();
                }

            } else {
                haveASnack("Permission denied. No changes made.");
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
