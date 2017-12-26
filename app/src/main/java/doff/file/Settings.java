package doff.file;

import android.util.Log;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.lang.reflect.Field;

public class Settings {

    public String Sweeper = "Kalle Anka";
    public String HousePropertyId = "Fastighetsbeteckning";
    public String Bluetooth_Mac = "AC:B5:7D:A4:2C:6F";
    //public String bluetoothMacAddress = "DD:B5:7D:A4:2C:6F"; //Ej giltig
    public String Email_Address = "kjellkod.kurs@gmail.com";
    public String Email_Password = "kmo123st";
    public String Email_To = "kjellkod.kurs@gmail.com";

    public String Pressure_Max = "90";
    public String Pressure_Min = "10";
    public String Pressure_Default = "40";
    public String Side_Max = "300";
    public String Side_Min = "50";
    public String Side_Default = "100";
    public String Length_Max = "20";
    public String Length_Min = "1";
    public String Length_Default = "3";
    public String Radius_Max = "300";
    public String Radius_Min = "50";
    public String Radius_Default = "100";

    public String[] key() {
        Field[] fields = (this.getClass()).getFields();
        String[] arr = new String[fields.length-2];

       for (int i=0; i <fields.length-2; i++) {
            arr[i] = fields[i].getName();
       }

        return arr;
    }
    public String[] value() {
        Field[] fields = (this.getClass()).getFields();
        String[] arr = new String[fields.length-2];

        try {
            for (int i=0; i <fields.length-2; i++) {
                arr[i] = (String) fields[i].get(this);
            }
        } catch(Exception e) {
            Log.e("doff-file", e.getMessage());
        }


        return arr;
    }

    public void view2settings(String[] key , String[] value ) {

        try {
            for (int i=0; i< key.length; i++) {
                Field f = (this.getClass()).getField(key[i]);
                f.set(this, value[i]);
                Log.d("doff-file",""+key[i]+": " + f.get(this));
            }
        } catch (Exception e) {
            Log.e("doff-file", e.getMessage());
        }
    }

}
