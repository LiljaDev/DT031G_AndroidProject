package se.miun.joli1407.bathingsites;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Observable;
import java.util.Observer;

/**
 * Custom view displays image and current number of bathing sites.
 * Observer pattern: When this view is attached it registers itself as an observer with the repository
 * which then notifies of any changes regarding the bathing sites.
 */
public class BathingSitesView extends LinearLayout implements Observer {
    TextView mTextNumOfSites;

    public BathingSitesView(Context context) {
        super(context);
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.view_bathing_sites, this, false);
        this.addView(view);
        loadViews();
    }

    public BathingSitesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.view_bathing_sites, this, false);
        this.addView(view);
        loadViews();
    }

    private void loadViews(){
        mTextNumOfSites = findViewById(R.id.textViewNumberBathingSites);
    }

    //Observer callback
    @Override
    public void update(Observable o, Object arg) {
        updateMessage(BathingSiteRepository.getInstance().getNumBathingSites());
    }

    private void updateMessage(long num){
        mTextNumOfSites.setText(getResources().getString(R.string.bathingsitesview_numberofbathingsites, BathingSiteRepository.getInstance().getNumBathingSites()));
    }

    //Register as an observer and make a first bathing site count update
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        BathingSiteRepository.getInstance().addObserver(this);
        updateMessage(BathingSiteRepository.getInstance().getNumBathingSites());
    }

    //Unregister as an observer
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        BathingSiteRepository.getInstance().deleteObserver(this);
    }
}
