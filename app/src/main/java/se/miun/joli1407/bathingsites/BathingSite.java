package se.miun.joli1407.bathingsites;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import java.util.Date;

@Entity(indices = {@Index(value={"longitude", "latitude"}, unique = true)}, tableName = "bathing_site")
public class BathingSite {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long mId;

    @ColumnInfo(name ="name")
    private String mName;
    @ColumnInfo(name ="description")
    private String mDescription;
    @ColumnInfo(name ="address")
    private String mAddress;
    @ColumnInfo(name ="longitude")
    private Double mLongitude;
    @ColumnInfo(name ="latitude")
    private Double mLatitude;
    @ColumnInfo(name ="temp")
    private Double mTemp;
    @ColumnInfo(name ="rating")
    private int mRating;

    @TypeConverters({DateConverter.class})
    private Date mDate;

    public BathingSite(String mName, String mDescription, String mAddress, Double mLongitude, Double mLatitude, Double mTemp, int mRating, Date mDate) {
        this.mName = mName;
        this.mDescription = mDescription;
        this.mAddress = mAddress;
        this.mLongitude = mLongitude;
        this.mLatitude = mLatitude;
        this.mTemp = mTemp;
        this.mRating = mRating;
        this.mDate = mDate;
    }

    public long getId(){
        return mId;
    }

    public void setId(long id){
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getAddress() {
        return mAddress;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    public Double getTemp() {
        return mTemp;
    }

    public int getRating() {
        return mRating;
    }

    public Date getDate() {
        return mDate;
    }

    @Override
    public String toString(){
        return mName + "\n" + mDescription + "\n" + mAddress + "\n" + mLongitude + "\n" + mLatitude + "\n" + mRating + "\n" + mTemp + "\n" + mDate;
    }
}
