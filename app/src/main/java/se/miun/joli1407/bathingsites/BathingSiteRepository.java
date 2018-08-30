package se.miun.joli1407.bathingsites;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import java.util.List;
import java.util.Observable;

/**
 * Singleton used by other parts of the application for database access.
 * The interface OnDBAccessComplete is used by other classes to register themselves as listeners
 * to receive a callback on the MainThread when operations finishes.
 * Class is observable so that UI elements can be notified of relevant changes.
 */
public class BathingSiteRepository extends Observable {
    private static BathingSiteRepository mInstance = null;
    private static long mNumBathingSites;
    private AppDataBase mDatabase;

    private BathingSiteRepository(Context context){
        mDatabase = AppDataBase.getDatabase(context);
        loadBathingSites();
    }

    public static BathingSiteRepository getInstance(){
        return mInstance;
    }

    //Updates mNumBathingSites with number of entries in the database
    private void loadBathingSites(){
        BathingSiteCounter bsc = new BathingSiteCounter();
        bsc.execute();
    }

    /**
     * Must be called before any other interaction with the repository
     * @param context Used to retrieve database instance
     */
    public static void init(Context context){
        if(mInstance == null)
            mInstance = new BathingSiteRepository(context);
    }

    public long getNumBathingSites(){
        return mNumBathingSites;
    }

    /**
     * Gets bathing site from database based on ID.
     * @param id Bathing site ID for the query
     * @param listener Listener for MainThread callback
     */
    public void getBathingSite(Long id, OnDBAccessCompleted listener){
        BathingSiteGetter bsg = new BathingSiteGetter(listener);
        bsg.execute(id);
    }

    /**
     * Gets all bathing sites from database.
     * @param listener Listener for MainThread callback
     */
    public void getAllBathingSites(OnDBAccessCompleted listener){
        BathingSitesGetter bsg = new BathingSitesGetter(listener);
        bsg.execute();
    }

    /**
     * Persists a bathing site in database.
     * @param bs The site to be persisted
     * @param listener Listener for MainThread callback
     */
    public void addBathingSite(BathingSite bs, OnDBAccessCompleted listener){
        BathingSiteInserter bsi = new BathingSiteInserter(listener);
        bsi.execute(bs);
    }

    /**
     * Persists a list of bathing sites in database.
     * @param bs The sites to be persisted
     * @param listener Listener for MainThread callback
     */
    public void addBathingSites(List<BathingSite> bs, OnDBAccessCompleted listener){
        BathingSiteListInserter bsli = new BathingSiteListInserter(listener);
        bsli.execute(bs);
    }

    //Deletes all bathing sites from the database, notifies observers
    private class BathingSiteDeleter extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            mDatabase.clearAllTables();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mInstance.setChanged();
            mInstance.notifyObservers();
        }
    }

    //Gets the number of bathing sites in the database, updates mNumBathingSites and notifies observers
    private class BathingSiteCounter extends AsyncTask<Void, Void, Long> {

        @Override
        protected Long doInBackground(Void... voids) {
            return mNumBathingSites = mDatabase.bathingSiteDao().countBathingSites();
        }

        @Override
        protected void onPostExecute(Long aLong) {
            mNumBathingSites = aLong;
            setChanged();
            mInstance.notifyObservers();
        }
    }

    //Gets a bathing site from the database
    private class BathingSiteGetter extends AsyncTask<Long, Void, BathingSite> {
        OnDBAccessCompleted mListener;

        public BathingSiteGetter(OnDBAccessCompleted listener){
            mListener = listener;
        }

        @Override
        protected BathingSite doInBackground(Long... longs) {
            BathingSite result;
            try{
                result = mDatabase.bathingSiteDao().getBathingSite(longs[0]);
            }
            catch(SQLiteException e){
                return null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(BathingSite result) {
            mListener.onDBAccessCompleted(result);
        }
    }

    //Gets all bathing sites from the database
    private class BathingSitesGetter extends AsyncTask<Void, Void, List<BathingSite>> {
        OnDBAccessCompleted mListener;

        public BathingSitesGetter(OnDBAccessCompleted listener){
            mListener = listener;
        }

        @Override
        protected List<BathingSite> doInBackground(Void... voids) {
            List<BathingSite> result;
            try{
                result = mDatabase.bathingSiteDao().getAllBathingSites();
            }
            catch(SQLiteException e){
                return null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<BathingSite> result) {
            mListener.onDBAccessCompleted(result);
        }
    }

    //Persists a list of bathing sites to the database, notifies observers
    private class BathingSiteListInserter extends AsyncTask<List<BathingSite>, Void, Long[]> {
        OnDBAccessCompleted mListener;

        public BathingSiteListInserter(OnDBAccessCompleted listener) {
            mListener = listener;
        }

        @Override
        protected Long[] doInBackground(List<BathingSite>... lists) {
            Long[] result;
            try{
                result = mDatabase.bathingSiteDao().addBathingSites(lists[0]);
            }
            catch(SQLiteException e){
                return null;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Long[] result) {
            if(result != null){
                loadBathingSites();
                setChanged();
                mInstance.notifyObservers();
            }
            mListener.onDBAccessCompleted(result);
        }
    }

    //Persists a bathing site to the database, notifies observers
    private class BathingSiteInserter extends AsyncTask<BathingSite, Void, Long> {
        OnDBAccessCompleted mListener;

        public BathingSiteInserter(OnDBAccessCompleted listener){
            mListener = listener;
        }

        @Override
        protected Long doInBackground(BathingSite... params) {
            long result;
            try{
                result = mDatabase.bathingSiteDao().addBathingSite(params[0]);
            }
            catch(SQLiteException e){
                return -1L;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Long result) {
            if(result >= 0){
                ++mNumBathingSites;
                setChanged();
                mInstance.notifyObservers();
            }
            mListener.onDBAccessCompleted(result);
        }
    }
}