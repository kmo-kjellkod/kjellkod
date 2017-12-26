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

public class PressureTest {

    Activity activity = null;
    private String space = "  ";

    public PressureTest(Activity activity) {
        this.activity=activity;
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
}
