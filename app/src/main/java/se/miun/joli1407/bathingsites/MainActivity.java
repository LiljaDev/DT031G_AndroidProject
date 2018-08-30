package se.miun.joli1407.bathingsites;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Entry point of the app, mostly just a gateway to other activities
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Save default preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences,false);

        //Repository needs init with app context before any db use
        BathingSiteRepository.init(getApplicationContext());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(view.getContext(), NewBathingSiteActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handles action bar clicks, starts appropriate activities without extras
     * @param item Menu item that was clicked
     * @return True if event was handled otherwise false
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        boolean consumed = false;

        switch(id){
            case R.id.action_settings:
                intent= new Intent(this, MySettingsActivity.class);
                startActivity(intent);
                consumed = true;
                break;

            case R.id.action_download:
                intent= new Intent(this, DownloadBathingSitesActivity.class);
                startActivity(intent);
                consumed = true;
                break;

            case R.id.action_map:
                intent= new Intent(this, BathingSitesMapActivity.class);
                startActivity(intent);
                consumed = true;
                break;
        }

        return consumed;
    }
}
