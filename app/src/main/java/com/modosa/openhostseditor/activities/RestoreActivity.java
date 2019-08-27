package com.modosa.openhostseditor.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.modosa.openhostseditor.fragments.backup.Fragment;
import com.modosa.openhostseditor.fragments.backup.RecyclerViewAdapter;
import com.modosa.openhostseditor.model.HostsManager;

import java.io.File;

import me.vittorio_io.openhostseditor.R;

public class RestoreActivity extends BaseActivity {

    private static final int ACTION_LOAD_BACKUPS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);

        getPermissionAndExecute(Manifest.permission.WRITE_EXTERNAL_STORAGE, ACTION_LOAD_BACKUPS);
    }

    private RecyclerViewAdapter iCanHazAdapter() {
        return new RecyclerViewAdapter(HostsManager.getBackupList(), new Fragment.OnListFragmentInteractionListener() {
            @Override
            public void onListFragmentInteraction(File item, int position) {
                switch (position) {
                    case 0: // row pressed
                        Intent intent = new Intent(RestoreActivity.this, PreviewActivity.class);
                        Bundle b = new Bundle();
                        b.putSerializable("file", item); //Your id
                        intent.putExtras(b); //Put your id to your next Intent
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivityForResult(intent, 0);
                        return;
                    case ACTION_LOAD_BACKUPS: // restore pressed
                        if (HostsManager.writeFromFile(item)) {
                            finish();
                        } else {
                            haveASnack("Backup not restored.");
                        }
                        return;
                    case 2: // delete pressed
                        if (item.delete()) {
                            haveASnack("Backup file deleted.");
                            RecyclerView viewById = findViewById(R.id.backup_list);
                            viewById.setAdapter(iCanHazAdapter());
                        } else {
                            haveASnack("Failed to remove file.");
                        }
                        return;
                    default:
                        Log.e("err", "Unhandled code returned.");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        getPermissionAndExecute(Manifest.permission.WRITE_EXTERNAL_STORAGE, ACTION_LOAD_BACKUPS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == ACTION_LOAD_BACKUPS) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                RecyclerView viewById = findViewById(R.id.backup_list);

                try {
                    viewById.setAdapter(iCanHazAdapter());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                haveASnack("Permission denied. Nothing to do here.");
                finish();
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
