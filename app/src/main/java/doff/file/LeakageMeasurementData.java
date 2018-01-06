package doff.file;

import android.app.Activity;

import com.doffs.skorsten.MainActivity;
import com.doffs.skorsten.R;

import static com.doffs.skorsten.MainActivity.*;

public class LeakageMeasurementData {

    //Activity activity = MainActivity.self; Går ej för JsonAdpater

    public String uuid = "";
    public String date = "datum";
    public String company = "företag";
    public String author = "förnamn efternamn";

    public String location_gps = "gps";
    public String housePropertyId = "fastighetsbeteckning";

    public String toString(Activity activity) {
        String s = "";
        s += activity.getResources().getString(R.string.Date) + ": " + date + "\n";
        s += activity.getResources().getString(R.string.Company) + ": " + company + "\n";
        s += activity.getResources().getString(R.string.Sweeper) + ": " + author + "\n";
        s += activity.getResources().getString(R.string.LocationGPS) + ": " + location_gps + "\n";
        s += activity.getResources().getString(R.string.HousePropertyId) + ": " + housePropertyId + "\n";
        return s;
    }

}
