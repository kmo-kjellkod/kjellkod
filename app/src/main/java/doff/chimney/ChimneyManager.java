package doff.chimney;

import android.graphics.Color;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;

import com.doffs.skorsten.MainActivity;
import com.doffs.skorsten.R;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import doff.bt.Bluetooth;
import doff.bt.Protocol;
import doff.file.FileManager;
import doff.file.Settings;

import static doff.chimney.ChimneyManager.State.Idle;

public class ChimneyManager {

    public enum State {
        Idle,
        Leakage,
        BatteryTest,
        Manual,

    }
    public State state = Idle;
    public boolean isStateIdle() { return state == State.Idle; }
    public void setStateIdle() {
        protocol.txQuit();
        state=State.Idle;
    }
    public void setStateLeakage() { state=State.Leakage;}
    public boolean isStateBatteryTest() { return state == State.BatteryTest; }
    public void setStateBatteryTest() {
        protocol.txBatteryTest();
        state=State.BatteryTest;
    }
    public boolean isStateManual() { return state == State.Manual; }
    public void setStateManual() { state=State.Manual;}

    private String space = "   ";

    public ChimneyManager(MainActivity mainActivity) {
        this.activity = mainActivity;
        settings = new Settings();

    }
    public void initAfterReadingSettings() {
        bluetooth = new Bluetooth(activity);
        bluetooth.setCommunicationCallback(activity);
        bluetooth.connectToAddress(settings.Bluetooth_Mac);
        protocol = new Protocol(bluetooth, sensors, actuators);
        chimney = new Chimney(activity, sensors, actuators, settings);
        chimney.file2leak();
        batteryTest = new BatteryTest(activity, protocol);
        manualControl = new ManualControl(activity);
        pressureTest = new PressureTest(activity);
        houseProperty = new HouseProperty(activity,settings);
    }

    private MainActivity activity=null;

    public Chimney chimney = null;
    public Bluetooth bluetooth = null;
    public Protocol protocol = null;

    public Actuators actuators = new Actuators();
    public Sensors sensors = new Sensors();


    public Settings settings = null;
    public void settings2file() {
        String fileName = "settings.json";
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Settings> jsonAdapter = moshi.adapter(Settings.class);
        String json = jsonAdapter.toJson(settings);

        Log.d("doff-file","settings: "+ json);
        try{
            FileManager.WriteReadableExternalStorage(activity,fileName,json);
        } catch (Exception e) {
            Log.d("doff-file","exception=" + e.getMessage());
        }
        Log.d("doff-file","write settingsfile done");

        //file2settings(); //TEST
    }
    public void file2settings() {
        String fileName = "settings.json";
        boolean isExisting = FileManager.isExternalFileExisting(activity, fileName);
        Log.d("doff-file", "file2settings: exists settings.json: " + isExisting);
        if (isExisting) {
            try {
                String json = FileManager.ReadExternalStorage(activity, fileName);
                Log.d("doff-file", "file2settings: json: " + json);
                Moshi moshi = new Moshi.Builder().build();
                JsonAdapter<Settings> jsonAdapter = moshi.adapter(Settings.class);
                this.settings = jsonAdapter.fromJson(json);
                Log.d("doff-file", "file2settings: settings: " + settings.Bluetooth_Mac);
            } catch (Exception e) {
                Log.e("doff-file", e.getMessage());
            }
        }
    }

    public BatteryTest batteryTest = null;
    public ManualControl manualControl = null;
    public PressureTest pressureTest = null;
    public HouseProperty houseProperty = null;

    public SpannableStringBuilder information() {
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        String s0 = space+activity.getString(R.string.info_renifoam)+"\n\n";
        SpannableString ss0 = new SpannableString(s0);
        ss0.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, s0.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss0.setSpan(new RelativeSizeSpan(1.5f), 0, s0.length()-1, flag);
        ss0.setSpan(new ForegroundColorSpan(Color.BLUE), 0, s0.length(), flag);
        ssb.append(ss0);

        String s1 = space+activity.getString(R.string.info_mobile) + activity.getString(R.string.info_mobile_number)+"\n\n";
        SpannableString ss1 = new SpannableString(s1);
        ssb.append(ss1);

        String s2 = space+activity.getString(R.string.info_telephone) + activity.getString(R.string.info_telephone_number)+"\n\n";
        SpannableString ss2 = new SpannableString(s2);
        ssb.append(ss2);

        return ssb;
    }

}
