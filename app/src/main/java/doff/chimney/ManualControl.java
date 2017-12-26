package doff.chimney;

import android.app.Activity;
import android.graphics.Color;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.Range;

import com.doffs.skorsten.R;

public class ManualControl {
    Activity activity = null;
    private String space = "  ";

    // Pressure in Pa
    public Range<Double> rangeFanMotor;
    public double fanMotor;
    public double setFanMotor(int progress) {
        fanMotor = (double) (seekBar2value(rangeFanMotor,progress));
        return fanMotor;
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


    public ManualControl(Activity activity) {

        this.activity = activity;
        rangeFanMotor = new Range<>(new Double(0), new Double(100));
        fanMotor = 0;
    }

    public SpannableStringBuilder BuildTextFanMotor() {
        String s;
        SpannableString ss;
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        SpannableStringBuilder builder = new SpannableStringBuilder();

        s = space + activity.getString(R.string.manual_actuator) +": " + (int) fanMotor + "%";
        ss =new SpannableString(space+s);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ss.length(), flag);
        ss.setSpan(new RelativeSizeSpan(1.25f), 0, ss.length(), flag);
        ss.setSpan(new ForegroundColorSpan(Color.BLUE), 0, ss.length(), flag);
        builder.append(ss);

        return builder;
    }

    public SpannableStringBuilder BuildTextPressure(double pressure) {
        String s;
        SpannableString ss;
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        SpannableStringBuilder builder = new SpannableStringBuilder();

        s = space + activity.getString(R.string.manual_pressure) +": " + String.format("%.1f", pressure) + " Pa";
        ss =new SpannableString(space+s);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ss.length(), flag);
        ss.setSpan(new RelativeSizeSpan(1.25f), 0, ss.length(), flag);
        ss.setSpan(new ForegroundColorSpan(Color.BLUE), 0, ss.length(), flag);
        builder.append(ss);

        return builder;
    }

    public SpannableStringBuilder BuildTextFlow(double flow) {
        String s;
        SpannableString ss;
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        SpannableStringBuilder builder = new SpannableStringBuilder();

        s = space + activity.getString(R.string.manual_flow) +": " + String.format("%.1f", flow) + " dm3/s\n";
        ss =new SpannableString(space+s);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ss.length(), flag);
        ss.setSpan(new RelativeSizeSpan(1.25f), 0, ss.length(), flag);
        ss.setSpan(new ForegroundColorSpan(Color.BLUE), 0, ss.length(), flag);
        ss.setSpan(new SuperscriptSpan(),ss.length()-4, ss.length()-3, flag);
        builder.append(ss);



        return builder;
    }
}
