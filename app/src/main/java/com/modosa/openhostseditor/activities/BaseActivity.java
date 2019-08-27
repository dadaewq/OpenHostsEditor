package com.modosa.openhostseditor.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

/**
 * Created by vittorio on 23/07/17.
 */

public abstract class BaseActivity extends AppCompatActivity {
    void openActivityForResult(Class<? extends Activity> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, 1);
    }

    void getPermissionAndExecute(String permission, int code) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {

                haveASnack("I need to explain permission.");

                askPermissionBase(permission, code);

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                askPermissionBase(permission, code);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            askPermissionBase(permission, code);
        }

    }

    private void askPermissionBase(String permission, int code) {
        ActivityCompat.requestPermissions(this,
                new String[]{permission},
                code);
    }

    void haveASnack(String message) {
        System.out.println(message);
        View view = getWindow().getDecorView().getRootView();

        try {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
