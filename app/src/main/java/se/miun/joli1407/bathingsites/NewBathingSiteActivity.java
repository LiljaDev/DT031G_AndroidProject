package se.miun.joli1407.bathingsites;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Activity holding a fragment for user creation of bathing sites.
 */
public class NewBathingSiteActivity extends AppCompatActivity {

    private NewBathingSiteFragment mFragmentNBS;
    BathingSitesFragment mFragmentBS;
    AppDataBase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_bathing_site);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFragmentNBS = (NewBathingSiteFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentNewBathingSite);
        mFragmentBS = (BathingSitesFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentBathingSites);
        mDatabase = AppDataBase.getDatabase(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_bathing_site, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean consumed = false;

        switch(id){
            case R.id.action_clear:
                mFragmentNBS.clearInput();
                consumed = true;
                break;
            case R.id.action_save:
                mFragmentNBS.saveBathingSite();
                consumed = true;
                break;
            case R.id.action_weather:
                mFragmentNBS.showWeather();
                consumed = true;
                break;
        }

        return consumed;
    }
}
