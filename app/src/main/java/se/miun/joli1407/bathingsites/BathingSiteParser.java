package se.miun.joli1407.bathingsites;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Stateless parser for .csv to bathing site objects
 */
public class BathingSiteParser {
    /**
     * Parses a .csv file to bathing site objects
     * @param fd .csv file descriptor
     * @param result A list containing the resulting bathing sites
     * @param context Application context to retrieve resource values
     * @throws IOException File not found etc
     * @throws NumberFormatException Parsing errors
     */
    public static void parseBathingSites(ParcelFileDescriptor fd, List<BathingSite> result, Context context) throws IOException, NumberFormatException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fd.getFileDescriptor()), "Cp1252"));
        String line;
        //Line by line parsing, the number of commas gives away the content
        while((line = br.readLine()) !=null){
            String values[] = line.split(",");
            double longitude = Double.parseDouble(values[0].substring(1));
            double latitude = Double.parseDouble(values[1]);
            String name, address = null;
            if(values.length >= 4){
                name = values[2].substring(2);
                StringBuilder addressBuilder = new StringBuilder("");
                for(int i = 3; i < values.length; ++i){
                    if(i == values.length - 1){
                        addressBuilder.append(values[i].substring(0, values[i].length()-3).trim());
                    }
                    else{
                        addressBuilder.append(values[3].trim());
                        addressBuilder.append(", ");
                    }
                }
                address = addressBuilder.toString();
            }
            else
                name = values[2].substring(2, values[2].length()-3);

            result.add(new BathingSite(name, null, address, longitude, latitude, null, context.getResources().getInteger(R.integer.ratingbar_default), null));
        }
    }
}