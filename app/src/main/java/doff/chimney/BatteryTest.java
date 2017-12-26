package doff.chimney;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SuperscriptSpan;

import com.doffs.skorsten.MainActivity;
import com.doffs.skorsten.R;

import doff.bt.Protocol;

public class BatteryTest {

//    public enum State {
//        Off,
//        On
//    }
//    public State state = State.Off;
//
//    public boolean isOn() { return state == State.On; }
//    public void On() {
//        state = State.On;
//        //this.protocol.
//    }
//    public void Off() { state = State.Off; }

    private MainActivity activity = null;
    private Protocol protocol = null;
    private String space = "   ";

    public BatteryTest(MainActivity activity, Protocol protocol) {
        this.activity = activity;
        this.protocol =protocol;
    }

    public SpannableStringBuilder BuildText() {
        String s;
        SpannableString ss;
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        SpannableStringBuilder builder = new SpannableStringBuilder();

        s = activity.getString(R.string.battery_text);
        ss =new SpannableString(space+s);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ss.length(), flag);
        ss.setSpan(new RelativeSizeSpan(1.25f), 0, ss.length(), flag);
        ss.setSpan(new ForegroundColorSpan(Color.BLUE), 0, ss.length(), flag);
        builder.append(ss);

        return builder;
    }


    public SpannableStringBuilder BuildTextBatteryNotOK() {
        String s;
        SpannableString ss;
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        SpannableStringBuilder builder = new SpannableStringBuilder();

        s = activity.getString(R.string.battery_text_not_ok);
        ss =new SpannableString(space+s);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ss.length(), flag);
        ss.setSpan(new RelativeSizeSpan(1.5f), 0, ss.length(), flag);
        ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(), flag);
        builder.append(ss);

        return builder;
    }


    public SpannableStringBuilder BuildTextBatteryOK() {
        String s;
        SpannableString ss;
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        SpannableStringBuilder builder = new SpannableStringBuilder();

        s = activity.getString(R.string.battery_text_ok);
        ss =new SpannableString(space+s);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ss.length(), flag);
        ss.setSpan(new RelativeSizeSpan(1.5f), 0, ss.length(), flag);
        ss.setSpan(new ForegroundColorSpan(Color.GREEN), 0, ss.length(), flag);
        builder.append(ss);

        return builder;
    }



}
