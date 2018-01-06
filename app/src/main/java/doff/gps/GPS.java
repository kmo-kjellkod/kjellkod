package doff.gps;

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

public class GPS {

    public LocationTracker tracker = null;
    public String latitudeLongitude = "";

    private Activity activity = null;

    public GPS(Activity activity) {
        this.activity=activity;
        tracker = new LocationTracker(activity);
    }

    public SpannableStringBuilder BuildTextLongitudeLatitude(boolean isInit, boolean isLongitude, double longitudeLatidtude) {
        String s, s1;
        SpannableString ss;
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if ( isLongitude )
            s1 = activity.getString(R.string.gps_longitude);
        else
            s1 = activity.getString(R.string.gps_latitude);

        if ( isInit )
            s = s1 + " ? ";
        else
            s =s1 + longitudeLatidtude;
        ss =new SpannableString(s);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ss.length(), flag);
        ss.setSpan(new RelativeSizeSpan(1.5f), 0, ss.length(), flag);
        ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(), flag);
        builder.append(ss);

        return builder;
    }

    public SpannableStringBuilder BuildTextPlace(String place) {
        String s;
        SpannableString ss;
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        SpannableStringBuilder builder = new SpannableStringBuilder();

        s = place;
        ss =new SpannableString(s);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ss.length(), flag);
        ss.setSpan(new RelativeSizeSpan(1.5f), 0, ss.length(), flag);
        ss.setSpan(new ForegroundColorSpan(Color.GREEN), 0, ss.length(), flag);
        builder.append(ss);

        return builder;
    }

}
