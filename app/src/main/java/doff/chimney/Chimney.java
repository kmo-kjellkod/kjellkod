package doff.chimney;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.*;
import android.text.style.*;
import android.util.Log;
import android.util.Range;
import android.widget.Toast;

import com.doffs.skorsten.MainActivity;
import com.doffs.skorsten.R;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import doff.email.GMailSend;
import doff.file.FileManager;
import doff.file.LeakageMeasurementData;
import doff.file.Settings;

public class Chimney {

    private Activity activity=null;
    private Sensors sensors = null;
    private Actuators actuators = null;
    private Settings settings = null;
    private LeakageMeasurementData lmd = null;
    private List<LeakageMeasurementData> listLMD = null;

    public Chimney(MainActivity mainMctivity, Sensors sensors, Actuators actuators, Settings settings) {
        this.activity=mainMctivity;
        this.sensors = sensors;
        this.actuators = actuators;
        this.settings = settings;
        this.lmd = new LeakageMeasurementData();
        this.listLMD = new ArrayList<>();

        rangePressure = new Range<>(new Double(settings.Pressure_Min), new Double(settings.Pressure_Max));
        pressure = Integer.parseInt(settings.Pressure_Default);

        rangeLengthR =  new Range<>(new Double(settings.Length_Min), new Double(settings.Length_Max));
        lengthR = Double.parseDouble(settings.Length_Default);

        rangeSide =  new Range<>(new Double(settings.Side_Min), new Double(settings.Side_Max));
        sideA = Integer.parseInt(settings.Side_Default);
        sideB = Integer.parseInt(settings.Side_Default);

        rangeRadius =  new Range<>(new Double(settings.Radius_Min), new Double(settings.Radius_Max));
        radius = Integer.parseInt(settings.Radius_Default);

        rangeLengthC =  new Range<>(new Double(settings.Length_Min), new Double(settings.Length_Max));
        lengthC = Double.parseDouble(settings.Length_Default);;
    }

    public enum ChimneyChannel {
        Rectangular,
        Circular,
        BothCircularAndRectanguar
    }
    //Type of chimney: rectangle or circular top
    public ChimneyChannel chimneyChannel = ChimneyChannel.Rectangular;

    // Pressure in Pa
    public Range<Double> rangePressure;
    public int pressure;
    public int setPressure(int progress) {
        pressure = (int) (seekBar2value(rangePressure,progress));
        return pressure;
    }

    // Length in m
    public Range<Double> rangeLengthR;
    public double lengthR;
    public double setLengthR(int progress) {
        lengthR = seekBar2value(rangeLengthR,progress);
        return lengthR;
    }

    // Side in mm
    public Range<Double> rangeSide;
    public int sideA;
    public int sideB;
    public int setSideA(int progress) {
        sideA = (int) (seekBar2value(rangeSide,progress));
        return sideA;
    }
    public int setSideB(int progress) {
        sideB = (int) (seekBar2value(rangeSide,progress));
        return sideB;
    }
    // Radius in mm
    public Range<Double> rangeRadius;
    public int radius;
    public int setRadius(int progress) {
        radius = (int) (seekBar2value(rangeSide,progress));
        return radius;
    }

    // Length in m
    public Range<Double> rangeLengthC;
    public double lengthC;
    public double setLengthC(int progress) {
        lengthC = seekBar2value(rangeLengthC,progress);
        return lengthC;
    }

    public double seekBar2value(Range<Double> range, int progress) {
        double lower = range.getLower().doubleValue();
        double upper = range.getUpper().doubleValue();
        double length = upper-lower;
        double k = length/1000.0;

        return lower + k*progress ;
    }
    public int value2seekBar(Range<Double> range, double value) {
        double lower = range.getLower().doubleValue();
        double upper = range.getUpper().doubleValue();
        double length = upper-lower;
        double k = length/1000.0;
        return (int) ((value - lower)/k + 0.5);
    }

    public double Area() {
        switch (chimneyChannel)
        {
            case Circular: return AreaC();
            case Rectangular: return AreaR();
            case BothCircularAndRectanguar: return AreaRC();
        }
        return 0;
    }
    public double AreaR() {
        double a = (sideA/1000.0);
        double b = (sideB/1000.0);
        double l = lengthR;
        double area = 2*( a*l + b*l);
        return area;
    }
    public double AreaC() {
        double area = 2*Math.PI*(radius/1000.0)*lengthC;
        return area;
    }
    public double AreaRC(){
        return AreaC()+AreaR();
    }

    private String space="  ";
    public SpannableString SSChimneyChannel() {
        switch (chimneyChannel)
        {
            case Circular: return SSChimneyChannelCircle();
            case Rectangular: return SSChimneyChannelRectangle();
            case BothCircularAndRectanguar: return SSChimneyChannelBothCircleAndRectangle();
        }
        return null;
    }
    public SpannableString SSChimneyChannelRectangle() {
        String s0 = space+"Fyrkantig rökkanal\n";
        String s1 = space+"Längd: " + String.format("%.1s",lengthR) + " m\n";
        String s2 = space+"Sida A: " + sideA + " mm\n";
        String s3 = space+"Sida B: " + sideB + " mm\n";
        String s = s0+s1+s2+s3;
        SpannableString ss = new SpannableString(s);
        return ss;
    }
    public SpannableString SSChimneyChannelCircle() {
        String s0 = space+"Rund rökkanal\n";
        String s1 = space+"Radie: " + radius + " mm\n";
        String s2 = space+"Längd: " + String.format("%.1f", lengthC) + " m\n";
        String s = s0+s1+s2;
        SpannableString ss = new SpannableString(s);
        return ss;
    }
    public SpannableString SSChimneyChannelBothCircleAndRectangle() {
        String s0 = space+"Både rund och fyrkantig rökkanal" +"\n";
        String s1 = "Längd fyrkant: " + lengthR + " m\n";
        String s2 = "Längd rund: " + lengthC + " m\n";
        String s = s0+s1+s2;
        SpannableString ss = new SpannableString(s);
        return ss;
    }
    public SpannableString SSTime() {
        Date currentTime = java.util.Calendar.getInstance().getTime();
        String s0 = space+"" +currentTime.toString()+"\n";
        String s = s0;
        SpannableString ss = new SpannableString(s);
        return ss;
    }
    public SpannableStringBuilder BuildLeakageMeasureOn(boolean isFinished) {
        String s;
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        SpannableStringBuilder builder = new SpannableStringBuilder();

        SpannableString s0 = new SpannableString(activity.getString(R.string.leaks_measure_measure_data)+"\n");
        s0.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, s0.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s0.setSpan(new RelativeSizeSpan(1.25f), 0, s0.length()-1, flag);
        s0.setSpan(new ForegroundColorSpan(Color.BLUE), 0, s0.length(), flag);
        builder.append(s0);

        SpannableString s10 = new SpannableString(space+"Area: " + String.format("%.1f",Area()) + "m");
        s10.setSpan(new StyleSpan(Typeface.BOLD), 0, s10.length(), flag);
        builder.append(s10);
        
        SpannableString s11 = new SpannableString("2\n");
        s11.setSpan(new SuperscriptSpan(), 0, s11.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(s11);
        
        SpannableString s2 = SSChimneyChannel();
        s2.setSpan(new StyleSpan(Typeface.ITALIC), 0, s2.length(), flag);
        builder.append(s2);
        if ( ! isFinished )
            s = activity.getString(R.string.leaks_measure_measure_underway)+"\n";
        else
            s = activity.getString(R.string.leaks_measure_measure_finished)+"\n";
        SpannableString s3 = new SpannableString(s);
        s3.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, s3.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s3.setSpan(new RelativeSizeSpan(1.25f), 0, s3.length()-1, flag);
        s3.setSpan(new ForegroundColorSpan(Color.BLUE), 0, s3.length(), flag);
        builder.append(s3);

        //Tryck och flöde
        s  = space+activity.getString(R.string.leaks_measure_measure_pressure) + sensors.pressure + " Pa\n";
        s += space+activity.getString(R.string.leaks_measure_measure_flow) + sensors.flow + " l/s\n";
        s0 = new SpannableString(s);
        s0.setSpan(new StyleSpan(Typeface.BOLD), 0, s0.length(), flag);
        builder.append(s0);
        
        builder.append(SSTime());

        return builder;
    }

    public boolean SaveMeasurementResult() {
        if ( leakLmdSave() ) {
            leak2file();
            return true;
        }
        return false;
    }
    public void EmailMeasurementResult() {
        String  fromEmail = settings.Email_Address;
        String password = settings.Email_Password;
        String toEmail = settings.Email_To;
        String subject = "Ämne";
        String body = this.lmd.toString();

        GMailSend.sendWithNoAttachment(fromEmail, password, toEmail, subject, body);


    }

    public void leakLmdResetUUID() { this.lmd.uuid="";}
    public void leakLmdInit() {
        this.lmd.uuid = UUID.randomUUID().toString();
    }
    public void leakLmdAdd() {

    }
    public boolean leakLmdSave() {
        for (int i = 0; i < listLMD.size(); i++) {
            if (listLMD.get(i).uuid.compareTo(lmd.uuid) == 0) {
                return false;
            }
        }

        if ( this.lmd.uuid != "" ) {
            listLMD.add(lmd);
        }
        return true;
    }
    public void leak2file() {
        String fileName = "leak.json";
        Moshi moshi = new Moshi.Builder().build();
        Type type = Types.newParameterizedType(List.class, LeakageMeasurementData.class);
        JsonAdapter<List<LeakageMeasurementData>> jsonAdapter = moshi.adapter(type);
        String json = jsonAdapter.toJson(this.listLMD);

        Log.d("doff-file","leak2file: "+ json);
        try{
            FileManager.WriteReadableExternalStorage(activity,fileName,json);
        } catch (Exception e) {
            Log.d("doff-file","exception=" + e.getMessage());
        }
        Log.d("doff-file","write leak2file done");
    }
    public void file2leak() {
        String fileName = "leak.json";
        boolean isExisting = FileManager.isExternalFileExisting(activity, fileName);
        Log.d("doff-file", "file2settings: exists leaks.json: " + isExisting);
        if (isExisting) {
            try {
                Moshi moshi = new Moshi.Builder().build();
                String json = FileManager.ReadExternalStorage(activity, fileName);
                Log.d("doff-file", "file2leak: json: " + json);
                Type type = Types.newParameterizedType(List.class, LeakageMeasurementData.class);
                JsonAdapter<List<LeakageMeasurementData>> jsonAdapter = moshi.adapter(type);
                this.listLMD = jsonAdapter.fromJson(json);
                Log.d("doff-file", "file2leak: settings: " + settings.Bluetooth_Mac);
           } catch (Exception e) {
                Log.e("doff-file", e.getMessage());
           }
        }
    }

}
