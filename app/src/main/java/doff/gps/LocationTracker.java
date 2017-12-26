package doff.gps;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;


import java.util.List;
import java.util.Locale;

/**
 * Created by androidruler on 24/02/16.
 */
public class LocationTracker extends Service implements LocationListener {

    private final Context con; //declaring Context variable
    private boolean isGPSOn=false;  //flag for gps
    private boolean isNetWorkEnabled=false; //flag for network location
    private boolean isLocationEnabled=false; //flag to getlocation

    private static final long MIN_DISTANCE_TO_REQUEST_LOCATION=1; // in meters
    private static final long MIN_TIME_FOR_UPDATES=1000*1; // 1 sec

    private Location location;
    private double latitude,longitude;
    private LocationManager locationManager;

    public LocationTracker(Context context)
    {
        this.con=context;
        checkIfLocationAvailable();
    }

    public Location checkIfLocationAvailable()
    {
        try
        {
            locationManager=(LocationManager)con.getSystemService(LOCATION_SERVICE);
            isGPSOn=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetWorkEnabled=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.d("doff-gps","locationManager != null: " + (locationManager != null));
            Log.d("doff-gps", "isGPSOn: " + isGPSOn);
            Log.d("doff-gps", "isNetWorkEnabled: " + isNetWorkEnabled);
            if(!isGPSOn && !isNetWorkEnabled)
            {
                isLocationEnabled=false;
                // no location provider is available show toast to user
                Toast.makeText(con,"No Location Provider is Available",Toast.LENGTH_SHORT).show();
            }
            else {
                isLocationEnabled=true;

                // if network location is available request location update
                if(isNetWorkEnabled)
                {
                    //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0, 0, this);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_FOR_UPDATES,MIN_DISTANCE_TO_REQUEST_LOCATION,this);
                    if(locationManager!=null)
                    {
                        location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if(location!=null)
                        {
                            latitude=location.getLatitude();
                            longitude=location.getLongitude();
                        }
                    }
                }
                Log.d("doff-gps", "isLocationEnabled: " + isLocationEnabled);
                if(isGPSOn)
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_FOR_UPDATES,MIN_DISTANCE_TO_REQUEST_LOCATION,this);

                    if(locationManager!=null)
                    {
                        location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(location!=null)
                        {
                            latitude=location.getLatitude();
                            longitude=location.getLongitude();
                        }
                    }
                }
            }

        }
        catch (SecurityException se)
        {
            se.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.d("doff-gps","location != null: " + (location != null));
        return location;
    }

    // call this to stop using location
    public void stopUsingLocation()
    {
        if(locationManager!=null)
        {
            locationManager.removeUpdates(LocationTracker.this);
        }
    }
    // call this to getLatitude
    public double getLatitude()
    {
        if(location!=null)
        {
            latitude=location.getLatitude();
        }
        return latitude;
    }
    //call this to getLongitude
    public double getLongitude()
    {
        if(location!=null)
        {
            longitude=location.getLongitude();
        }
        return longitude;
    }

    public boolean isLocationEnabled() {
        return this.isLocationEnabled;
    }

    //call to open settings and ask to enable Location
    public void askToOnLocation()
    {
        Log.d("doff-gps", "askToOnLocation");
        AlertDialog.Builder dialog=new AlertDialog.Builder(con);

        //set title
        dialog.setTitle("Settings");
        //set Message
        dialog.setMessage("Location is not Enabled.Do you want to go to settings to enable it?");
        // on pressing this will be called
        dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                con.startActivity(intent);
            }
        });

        //on Pressing cancel
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // show Dialog box
        dialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("doff-gps", "onLocationChanged");
        this.location=location;
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("doff-gps", "onProviderDisabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("doff-gps", "onProviderDisabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("doff-gps", "onStatusChanged");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 5);
//            for (int i=0; i<addresses.size(); i++ ) {
//                Log.d("doff-gps", ""+addresses.get(i).toString());
//            }

            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("doff-gps", strReturnedAddress.toString());
            } else {
                Log.w("doff-gps", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("doff-gps", "Canont get Address!");
        }
        return strAdd;
    }

//    public LatLng getLocationFromAddress(Context context,String strAddress) {
//
//        Geocoder coder = new Geocoder(context);
//        List<Address> address;
//        LatLng p1 = null;
//
//        try {
//            // May throw an IOException
//            address = coder.getFromLocationName(strAddress, 5);
//            if (address == null) {
//                return null;
//            }
//            Address location = address.get(0);
//            location.getLatitude();
//            location.getLongitude();
//
//            p1 = new LatLng(location.getLatitude(), location.getLongitude() );
//
//        } catch (IOException ex) {
//
//            ex.printStackTrace();
//        }
//
//        return p1;
//    }

}
