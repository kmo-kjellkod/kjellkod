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
import android.util.Range;

import com.doffs.skorsten.R;

public class PressureTest {

    Activity activity = null;
    private String space = "  ";

    // Length
    public Range<Double> rangeLength;
    public double length;
    public double setLength(int progress) {
        length = (double) (seekBar2value(rangeLength,progress));
        return length;
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


    public PressureTest(Activity activity) {

        this.activity=activity;
        rangeLength = new Range<>(new Double(1), new Double(30));
        length = 0;
    }

    public SpannableStringBuilder BuildTextLength() {
        String s;
        SpannableString ss;
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        SpannableStringBuilder builder = new SpannableStringBuilder();

        s = activity.getString(R.string.pressure_actuator) +": " + String.format("%.1f", length) + " m";
        ss =new SpannableString(space+s);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ss.length(), flag);
        ss.setSpan(new RelativeSizeSpan(1.25f), 0, ss.length(), flag);
        ss.setSpan(new ForegroundColorSpan(Color.BLUE), 0, ss.length(), flag);
        builder.append(ss);

        return builder;
    }

    public SpannableStringBuilder BuildTextFanMotor(int percent) {
        String s;
        SpannableString ss;
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        SpannableStringBuilder builder = new SpannableStringBuilder();

        s = activity.getString(R.string.pressure_actuator) +": " + String.format("%d", percent) + " %";
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

        s = space + activity.getString(R.string.pressure_pressure) +": " + String.format("%.1f", pressure) + " Pa";
        ss =new SpannableString(space+s);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ss.length(), flag);
        ss.setSpan(new RelativeSizeSpan(1.25f), 0, ss.length(), flag);
        ss.setSpan(new ForegroundColorSpan(Color.BLUE), 0, ss.length(), flag);
        builder.append(ss);

        return builder;
    }

}
