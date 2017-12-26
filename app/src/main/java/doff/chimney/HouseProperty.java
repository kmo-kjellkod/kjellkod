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

import com.doffs.skorsten.R;

import doff.file.Settings;

public class HouseProperty {
    Activity activity = null;
    Settings settings = null;
    private String space = "  ";

    public HouseProperty(Activity activity, Settings settings) {
        this.activity = activity;
        this.settings = settings;
    }


    public SpannableStringBuilder BuildTextSweeper() {
        String s;
        SpannableString ss;
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        SpannableStringBuilder builder = new SpannableStringBuilder();

        s = space + activity.getString(R.string.house_property_sweep);
        ss =new SpannableString(space+s);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL), 0, ss.length(), flag);
        ss.setSpan(new RelativeSizeSpan(1.25f), 0, ss.length(), flag);
        ss.setSpan(new ForegroundColorSpan(Color.BLUE), 0, ss.length(), flag);
        builder.append(ss);

        return builder;
    }


    public SpannableStringBuilder BuildTextId() {
        String s;
        SpannableString ss;
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        SpannableStringBuilder builder = new SpannableStringBuilder();

        s = space + activity.getString(R.string.house_property_id);
        ss =new SpannableString(space+s);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL), 0, ss.length(), flag);
        ss.setSpan(new RelativeSizeSpan(1.25f), 0, ss.length(), flag);
        ss.setSpan(new ForegroundColorSpan(Color.BLUE), 0, ss.length(), flag);
        builder.append(ss);

        return builder;
    }

}
