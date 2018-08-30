package se.miun.joli1407.bathingsites;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Settings fragment presenting the applications preferences.
 */
public class MySettingsFragment extends PreferenceFragmentCompat implements OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        //Add listeners to allow validation before applying preference changes
        findPreference("mapDistance").setOnPreferenceChangeListener(this);
        findPreference("weatherURL").setOnPreferenceChangeListener(this);
        findPreference("downloadURL").setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        findPreference("mapDistance").setSummary(sp.getString("mapDistance", "50"));
        findPreference("weatherURL").setSummary(sp.getString("weatherURL", "http://dt031g.programvaruteknik.nu/badplatser/weather.php"));
        findPreference("downloadURL").setSummary(sp.getString("downloadURL", "http://dt031g.programvaruteknik.nu/badplatser/koordinater/"));
        super.onResume();
    }

    //Only commit changes if string only contains numbers
    private boolean validateMapDistancePreference(String value, Preference preference){
        //Check that string only contains numbers
        if(value.matches("^\\d+$")) {
            //Update the summary
            preference.setSummary(value);
            return true;
        }
        else{
            Toast toast = Toast.makeText(getContext(), R.string.preference_mapdistance_nonvalid, Toast.LENGTH_LONG);
            toast.show();
            return false;
        }
    }

    //Pass the users changes to the appropriate validator method
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        switch(key){
            case "mapDistance":
                return validateMapDistancePreference((String)newValue, preference);
            case "weatherURL":
                return validateURLPreference((String)newValue, preference);
            case "downloadURL":
                return validateURLPreference((String)newValue, preference);
        }

        return true;
    }

    //Only commit changes if string is a valid URL (just a format check)
    private boolean validateURLPreference(String newValue, Preference preference) {
        try {
            URL testURL = new URL(newValue);
        } catch (MalformedURLException e) {
            Log.w("URL FAIL", "MALFORMED!");
            Toast toast = Toast.makeText(getContext(), R.string.preferences_url_nonvalid, Toast.LENGTH_LONG);
            toast.show();
            return false;
        }

        preference.setSummary(newValue);
        return true;
    }
}
