package se.miun.joli1407.bathingsites;

import android.arch.persistence.room.TypeConverter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Type converter for the ROOM API
 */
public class DateConverter {
    public static SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @TypeConverter
    public static Date fromString(String dateString){
        if(dateString != null){
            try {
                return mDateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }

    @TypeConverter
    public static String fromDate(Date date){
        if(date != null){
            return mDateFormat.format(date);
        }

        return null;
    }
}
