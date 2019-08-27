package com.modosa.openhostseditor.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.modosa.openhostseditor.model.HostsManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Objects;

import me.vittorio_io.openhostseditor.R;

public class PreviewActivity extends BaseActivity {
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        Bundle b = getIntent().getExtras();

        File file = (File) Objects.requireNonNull(b).get("file");

        TextView textPreview = findViewById(R.id.textPreview);

        try {
            if (file != null) {
                this.file = file;

                setTitle(file.getName());

                //Read text from file
                StringBuilder text = new StringBuilder();


                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();

                textPreview.setText(text.toString());
            } else {
                throw new RuntimeException("No file specified.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            textPreview.setText(String.format("Failed to load preview. %s", e.getLocalizedMessage()));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_restore: {
                HostsManager.writeFromFile(file);
                finish();
                return true;
            }
            case R.id.action_delete: {
                if (file.delete()) {
                    haveASnack("Backup deleted successfully.");
                    finish();
                } else {
                    haveASnack("Failed to delete backup.");
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        return true;
    }
}
