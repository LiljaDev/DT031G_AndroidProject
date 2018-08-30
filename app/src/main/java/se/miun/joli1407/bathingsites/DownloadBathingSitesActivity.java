package se.miun.joli1407.bathingsites;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.Uri;
import android.widget.Toast;
import java.util.LinkedList;
import java.util.List;

public class DownloadBathingSitesActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, OnDBAccessCompleted{

    private ContentLoadingProgressBar mProgressBar;
    private WebView mWebView;
    private DownloadManager mDownloadManager;
    private long mCurrentDownloadId = -1L;
    private final int EXTERNAL_STORAGE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_bathing_sites);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDownloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        mProgressBar = findViewById(R.id.downloadProgressBar);
        mWebView = findViewById(R.id.webView);
        mWebView.setWebViewClient(new MyWebViewClient());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String downloadURL = sharedPreferences.getString("downloadURL", "");

        mWebView.loadUrl(downloadURL);
        registerReceiver(onDownloaded,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloaded);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == EXTERNAL_STORAGE_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }
            else{
                Toast toast = Toast.makeText(this, R.string.downloadbathingsites_permission_externalstorage_denied, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    //From android.developer.com
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    BroadcastReceiver onDownloaded =new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            //Parse data, persist bathing sites
            SiteDownloader sd = new SiteDownloader((OnDBAccessCompleted) context);
            sd.execute();
        }
    };

    //Bathing site persist callback
    @Override
    public void onDBAccessCompleted(Long result) {
        /*Toast toast = null;
        if(result >= 0){
            toast = Toast.makeText(this, R.string.all_bathingsites_persisted, Toast.LENGTH_LONG);
        }
        else
            toast = Toast.makeText(this, R.string.all_bathingsites_persist_error, Toast.LENGTH_LONG);

        toast.show();*/
    }

    //Bathing site list persist callback
    @Override
    public void onDBAccessCompleted(Long[] result) {
        Toast toast = null;
        if(result != null){
            toast = Toast.makeText(this, R.string.all_bathingsites_persisted, Toast.LENGTH_LONG);
            mProgressBar.hide();
        }
        else
            toast = Toast.makeText(this, R.string.all_bathingsites_persist_error, Toast.LENGTH_LONG);

        toast.show();
    }

    @Override
    public void onDBAccessCompleted(BathingSite bathingSite) {

    }

    @Override
    public void onDBAccessCompleted(List<BathingSite> bathingSites) {

    }

    //Handle user link interaction
    private class MyWebViewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //If external storage unavailable
                if(!isExternalStorageReadable()){
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.all_externalstorage_unavailable, Toast.LENGTH_LONG);
                    toast.show();
                    return true;
                }

                Uri target = Uri.parse(url);
                //If a .csv file is identified and no current download is in progress
                //Start download
                if (MimeTypeMap.getFileExtensionFromUrl(target.toString()).equals("csv") && mCurrentDownloadId == -1L){
                    mProgressBar.show();
                    mCurrentDownloadId = mDownloadManager.enqueue(new DownloadManager.Request(target).setTitle("Sites")
                            .setDescription("bathing sites csv")
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "bathingsites.csv"));
                }
                return true;
            }
            else{
                ActivityCompat.requestPermissions((Activity)view.getContext(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_PERMISSION);
            }
            return true;
        }
    }

    //Does parsing and persisting of downloaded file
    private class SiteDownloader extends AsyncTask<Void, Void, String> {
        OnDBAccessCompleted listener;

        public SiteDownloader(OnDBAccessCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                ParcelFileDescriptor fd = mDownloadManager.openDownloadedFile(mCurrentDownloadId);
                List<BathingSite> newBathingSites = new LinkedList<>();
                BathingSiteParser.parseBathingSites(fd, newBathingSites, getApplicationContext());
                BathingSiteRepository.getInstance().addBathingSites(newBathingSites, listener);
                mDownloadManager.remove(mCurrentDownloadId);
                mCurrentDownloadId = -1L;
            } catch (Exception e){
                return getString(R.string.downloadbathingsites_download_error);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //Shows message if something went wrong during parsing or db inserts
            if(result != null){
                Toast toast = Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
}
