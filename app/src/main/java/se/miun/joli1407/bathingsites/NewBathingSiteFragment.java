package se.miun.joli1407.bathingsites;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRatingBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment containing views and logic for the creation and persisting of bathing sites.
 */
public class NewBathingSiteFragment extends Fragment implements OnDBAccessCompleted {
    Map<Integer, TextInputLayout> mEditTexts;
    AppCompatRatingBar mRatingBar;
    ProgressBar mProgressBar;
    AppDataBase mDatabase;

    public NewBathingSiteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_bathing_site, container, false);
        loadViews(view);
        mDatabase = AppDataBase.getDatabase(view.getContext().getApplicationContext());
        return view;
    }

    /**
     * Clears all user input and applies rating bar default value
     */
    public void clearInput(){
        for(TextInputLayout i : mEditTexts.values()){
            i.getEditText().setText("");
            mRatingBar.setRating(getResources().getInteger(R.integer.ratingbar_default));
        }
    }

    /**
     * Validates input and if valid constructs a bathing site which is passed on to be persisted.
     * @return True if input validated successfully
     */
    public boolean saveBathingSite(){
        clearErrors();
        Map<Integer, String> input = getInput();
        boolean inputValid = validateInput(input);
        if(inputValid){
            Date date = DateConverter.fromString(input.get(R.id.til_newbathingsite_date));
            Double lat, lon, temp;

            if("".equals(input.get(R.id.til_newbathingsite_longitude)) || "".equals(input.get(R.id.til_newbathingsite_latitude))){
                lat = lon = null;
            }
            else{
                lon = Double.parseDouble(input.get(R.id.til_newbathingsite_longitude));
                lat = Double.parseDouble(input.get(R.id.til_newbathingsite_latitude));
            }

            if("".equals(input.get(R.id.til_newbathingsite_watertemp)))
                temp = null;
            else
                temp = Double.parseDouble(input.get(R.id.til_newbathingsite_watertemp));

            BathingSite newBS = new BathingSite(input.get(R.id.til_newbathingsite_name),
                    input.get(R.id.til_newbathingsite_description),
                    input.get(R.id.til_newbathingsite_address),
                    lon,
                    lat,
                    temp,
                    (int)(((AppCompatRatingBar)getActivity().findViewById(R.id.ratingbar_newbathingsite)).getRating()),
                    date);

            BathingSiteRepository.getInstance().addBathingSite(newBS, this);
            return true;
        }
        else
            return false;
    }

    //Returns a map with the user input
    private Map<Integer, String> getInput(){
        Map<Integer, String> input = new HashMap<>();
        input.put(R.id.til_newbathingsite_name, mEditTexts.get(R.id.til_newbathingsite_name).getEditText().getText().toString().trim());
        input.put(R.id.til_newbathingsite_description, mEditTexts.get(R.id.til_newbathingsite_description).getEditText().getText().toString().trim());
        input.put(R.id.til_newbathingsite_address, mEditTexts.get(R.id.til_newbathingsite_address).getEditText().getText().toString().trim());
        input.put(R.id.til_newbathingsite_longitude, mEditTexts.get(R.id.til_newbathingsite_longitude).getEditText().getText().toString().trim());
        input.put(R.id.til_newbathingsite_latitude, mEditTexts.get(R.id.til_newbathingsite_latitude).getEditText().getText().toString().trim());
        input.put(R.id.til_newbathingsite_watertemp, mEditTexts.get(R.id.til_newbathingsite_watertemp).getEditText().getText().toString().trim());
        input.put(R.id.til_newbathingsite_date, mEditTexts.get(R.id.til_newbathingsite_date).getEditText().getText().toString().trim());
        return input;
    }

    //Displays weather data based on user latlng input
    public void showWeather(){
        String location;
        String lon = mEditTexts.get(R.id.til_newbathingsite_longitude).getEditText().getText().toString().trim();
        String lat = mEditTexts.get(R.id.til_newbathingsite_latitude).getEditText().getText().toString().trim();

        //Use latlng if available else address (else empty argument)
        if(lon.isEmpty() || lat.isEmpty())
            location = mEditTexts.get(R.id.til_newbathingsite_address).getEditText().getText().toString().trim();
        else
            location = lon + "|" + lat;

        //Start fetching weather data in bg thread
        WeatherFetcher wf = new WeatherFetcher();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String weatherURL = sharedPreferences.getString("weatherURL", "");
        String[] params = {weatherURL, location};
        wf.execute(params);
    }

    //Reset input view errors
    private void clearErrors(){
        for(TextInputLayout i : mEditTexts.values()){
            i.setError(null);
        }
    }

    /**
     * Validates user input, displays relevant messages upon errors.
     * @param input All user input.
     * @return True if obligatory information is entered.
     */
    private boolean validateInput(Map<Integer, String> input){
        boolean valid = true;

        TextInputLayout nameTil = mEditTexts.get(R.id.til_newbathingsite_name);
        if("".equals(input.get(R.id.til_newbathingsite_name))){
            valid = false;
            nameTil.setError(getString(R.string.newbathingsite_nameerror));
        }

        TextInputLayout addressTil = mEditTexts.get(R.id.til_newbathingsite_address);
        if("".equals(input.get(R.id.til_newbathingsite_address))){
            if("".equals(input.get(R.id.til_newbathingsite_longitude)) || "".equals(input.get(R.id.til_newbathingsite_latitude))){
                valid = false;
                if("".equals(input.get(R.id.til_newbathingsite_longitude)))
                    mEditTexts.get(R.id.til_newbathingsite_longitude).setError(getString(R.string.newbathingsite_longitudeerror));

                if("".equals(input.get(R.id.til_newbathingsite_latitude)))
                    mEditTexts.get(R.id.til_newbathingsite_latitude).setError(getString(R.string.newbathingsite_latitudeerror));

                mEditTexts.get(R.id.til_newbathingsite_address).setError(getString(R.string.newbathingsite_addresserror));
            }
        }
        return valid;
    }

    //Store references to all the relevant views
    private void loadViews(View view){
        mEditTexts = new HashMap<>();
        mEditTexts.put(R.id.til_newbathingsite_name, (TextInputLayout) view.findViewById(R.id.til_newbathingsite_name));
        mEditTexts.put(R.id.til_newbathingsite_description, (TextInputLayout)view.findViewById(R.id.til_newbathingsite_description));
        mEditTexts.put(R.id.til_newbathingsite_address, (TextInputLayout)view.findViewById(R.id.til_newbathingsite_address));
        mEditTexts.put(R.id.til_newbathingsite_longitude, (TextInputLayout)view.findViewById(R.id.til_newbathingsite_longitude));
        mEditTexts.put(R.id.til_newbathingsite_latitude, (TextInputLayout)view.findViewById(R.id.til_newbathingsite_latitude));
        mEditTexts.put(R.id.til_newbathingsite_watertemp, (TextInputLayout)view.findViewById(R.id.til_newbathingsite_watertemp));
        mEditTexts.put(R.id.til_newbathingsite_date, (TextInputLayout)view.findViewById(R.id.til_newbathingsite_date));

        mRatingBar = view.findViewById(R.id.ratingbar_newbathingsite);
        mProgressBar = view.findViewById(R.id.pb_newbathingsite_weather);
    }

    //Bathing site persisted callback
    @Override
    public void onDBAccessCompleted(Long result) {
        Toast toast = null;
        if(result >= 0){
            toast = Toast.makeText(getContext(), R.string.all_bathingsite_persisted, Toast.LENGTH_LONG);
            clearInput();
            getActivity().finish();
        }
        else
            toast = Toast.makeText(getContext(), getString(R.string.all_bathingsite_persist_error) + String.valueOf(result), Toast.LENGTH_LONG);

        toast.show();
    }

    @Override
    public void onDBAccessCompleted(Long[] result) {

    }

    @Override
    public void onDBAccessCompleted(BathingSite bathingSite) {

    }

    @Override
    public void onDBAccessCompleted(List<BathingSite> bathingSites) {

    }

    //Requests weather data from webservice at the URL set in app preferences
    private class WeatherFetcher extends AsyncTask<String, String, List<String>> {

        Drawable icon;
        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        //Method simply reads response line by line expecting certain data, if the data is not valid
        //a general exception is caught and prompts user to look over their input and settings.
        //TODO: Cover all bases, could there be an icon supplied while there is still data missing?
        @Override
        protected List<String> doInBackground(String... strings) {
            String line = "";
            List<String> result = new ArrayList<>();

            try {
                URL url = new URL(strings[0] + "?location=" + strings[1]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while((line = br.readLine()) != null) {
                    result.add(line);
                }

                URL iconUrl = new URL(result.get(8).substring(6, result.get(8).length()-4));
                InputStream is = (InputStream)iconUrl.getContent();
                icon = Drawable.createFromStream(is, "img");

            } catch(Exception e){
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getContext(), R.string.newbathingsite_weather_error, Toast.LENGTH_LONG);
                        toast.show();
                    }
                });

                return null;
            }

            return result;
        }

        //TODO: Cover all bases, could there be an icon supplied while there is still data missing?
        @Override
        protected void onPostExecute(List<String> s) {
            mProgressBar.setVisibility(View.INVISIBLE);

            if(s == null)
                return;

            String location = s.get(0).substring(8, s.get(0).length()-4);
            String temp = s.get(4).substring(7, s.get(4).length()-4);
            String condition = s.get(3).substring(10, s.get(3).length()-4);

            AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
            ad.setTitle(R.string.newbathingsite_weather_dialog_title);
            ad.setMessage(location + "\n" + condition + " " + temp + "\u00B0");
            ad.setIcon(icon);
            ad.show();
        }
    }
}