package doff.file;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import com.doffs.skorsten.MainActivity;
import com.doffs.skorsten.R;

public class Pdf {

    public final static float A4_PostscriptHeight = 842;
    public final static float HeightDiv15 = A4_PostscriptHeight / 15f;
    public final static float h10 = A4_PostscriptHeight / 27f;
    public final static float h10half = h10 / 2;
    public final static float h11 = A4_PostscriptHeight - 2 * h10;
    public final static float h12 = h10;

    public final static float A4_PostscriptWidth = 595;
    public final static float w10 = A4_PostscriptWidth / 20;
    public final static float w11 = A4_PostscriptWidth - 2 * w10;
    public final static float w12 = w10;


    public final static float mmv = A4_PostscriptHeight / 270f;
    public final static float mmh = A4_PostscriptWidth / 200f;

    private static float text_size = 12;
    private static int page_no = 0;

    private static float pdf_header(Canvas c, Paint p, LeakageMeasurementData lmd)
    {
        String s = lmd.company;
        p.setTextSize(text_size);
        //p.setFontFeatureSettings("");
        p.setTypeface(Typeface.create("Arial", Typeface.ITALIC));
        c.drawText(s, 10, 10* mmv, p);
        //Draw.DrawInBox(s, 10, c, p, 10, 0, h10, A4_PostscriptWidth, h10);
        return 2*h10+10* mmv;
    }

    private static void pdf_footer(Context context, Canvas c, Paint p, LeakageMeasurementData lmd)
    {
        p.setColor(Color.BLACK);
        //Draw.DrawInBox(""+page_no, (int) text_size, c, p, 10, 0, A4_PostscriptHeight-h10, A4_PostscriptWidth, h10);
    }

    private static float pdf_rectangle(Context context, Canvas c, Paint p, String txt1, String txt2, float x, float y, float dx1, float dx2, float dy) {
        MainActivity activity = (MainActivity) context;

        p.setStyle(Paint.Style.STROKE);

        p.setColor(activity.getColor(R.color.greySlate3));
        c.drawRect(x,y,x+dx1,y+dy,p);

        p.setColor(activity.getColor(R.color.greySlate2));
        c.drawText(txt1,x,y+dy-1*mmh,p);

        p.setColor(activity.getColor(R.color.greySlate3));
        c.drawRect(x+dx1,y,x+dx1+dx2,y+dy,p);

        p.setColor(Color.BLACK);
        c.drawText(txt2,x+dx1,y+dy-1*mmh,p);

        return dy;
    }

    public static void lmd2pdf(Context context, String fileName, LeakageMeasurementData lmd)
    {
        MainActivity activity = (MainActivity) context;

        try
        {
            float dy = 10 * mmv;
            float dx0 = 10* mmh;
            float dx1 = 80* mmh;
            float dx2 = 80* mmh;
            float y = dy;
            float xx, yy, ww, hh;
            String s;

            PdfDocument document = null;
            PdfDocument.PageInfo pageInfo = null;
            PdfDocument.Page page = null;

            Canvas c = null;
            Paint p = new Paint();

            document = new PdfDocument();

            //First Page
            page_no = 1;
            pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            page = document.startPage(pageInfo);
            c = page.getCanvas();

            p.setTypeface(Typeface.create("Arial", Typeface.ITALIC));
            y += pdf_header(c, p, lmd);

            Log.d("doff-file", "Pdf.lmd2pdf: header" );

            p.setTypeface(Typeface.create("Arial", Typeface.NORMAL));
            String txt1 = activity.getResources().getString(R.string.Company);
            String txt2 = lmd.company;
            y += Pdf.pdf_rectangle(context,c,p,txt1,txt2,dx0,y,dx1,dx2,dy);
            Log.d("doff-file", "Pdf.lmd2pdf: rectangle company" );

            txt1 = activity.getResources().getString(R.string.Sweeper);
            txt2 = lmd.author;
            y += Pdf.pdf_rectangle(context,c,p,txt1,txt2,dx0,y,dx1,dx2,dy);

            txt1 = activity.getResources().getString(R.string.LocationGPS);
            txt2 = lmd.location_gps;
            y += Pdf.pdf_rectangle(context,c,p,txt1,txt2,dx0,y,dx1,dx2,dy);

            txt1 = activity.getResources().getString(R.string.HousePropertyId);
            txt2 = lmd.housePropertyId;
            y += Pdf.pdf_rectangle(context,c,p,txt1,txt2,dx0,y,dx1,dx2,dy);

            p.setTypeface(Typeface.create("Arial", Typeface.NORMAL));

//
//            Draw.DrawInBox("@text "+ L.W.get(L.Word.PercenOfRightAnswers)+xql.percentRight() + "%", 0, c, p, 0, w10, dy, A4_PostscriptWidth-2*w10, h10);
//            dy += h10+dy_question_space;
//
//            xx = w10;
//            hh = h10;
//            ww = w11;
//
//            int i=0;
//            for (Question q : xql.getQuestions())
//            {
//                if ( (dy+200) > A4_PostscriptHeight)
//                {
//                    pdf_footer(c,p);
//                    page_no++;
//                    document.finishPage(page);
//                    page = document.startPage(pageInfo);
//                    c = page.getCanvas();
//                    dy=0;
//                    dy += pdf_header(c,p,xql,fileName);
//                }
//
//                i++;
//
//                PdfQuestionData pdq = null;
//                String xml = q.getStrings().get(Question.StringsIndex.Pdf.getValue());
//                if ( xml.startsWith("<"))
//                {
//                    pdq = PdfQuestionData.read(xml);
//                    Log.i(TAG,"Pdf.xml2pdf, height="+pdq.height+"mmv");
//                }
//
//                switch( pdq.typeOfPrint)
//                {
//                    case OneRow_Q_A_UA:
//                    case Default:
//                    default:
//                        dy = pdf_q_a_ua(q, c, p, i, dy, xx, ww, pdq.height);
//                        break;
//                }
//            }
            pdf_footer(activity, c,p, lmd);
            Log.d("doff-file", "Pdf.lmd2pdf: footer" );

            document.finishPage(page);
            Log.d("doff-file", "Pdf.lmd2pdf: page" );

            //String path = context.getFilesDir()+ "/" + fileName + date.toString();
            String name = FileManager.fileNameWithDate(fileName);
            Log.d("doff-file", "Pdf.lmd2pdf name: " + name);
            String path = context.getExternalFilesDir(null)+ "/" + name;
            Log.d("doff-file", "Pdf.lmd2pdf path: " + path);

            FileOutputStream fos = new FileOutputStream(path);
            document.writeTo(fos);
            File file = new File(path);
            file.setReadable(true);

            Log.d("doff-pdf", path);
        } catch (Exception e)
        {
            Log.e("doff-file", "Pdf.lmd2pdf: " + e.getMessage());
            e.printStackTrace();
        } finally
        {
        }
    }

    public static SpannableStringBuilder SpannableStringFileName(String fileName) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        String s0 = fileName;
        SpannableString ss = new SpannableString(s0);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ss.length(), flag);
        ss.setSpan(new RelativeSizeSpan(0.75f), 0, ss.length(), flag);
        ss.setSpan(new ForegroundColorSpan(Color.BLUE), 0, ss.length(), flag);

        return builder.append(ss);
    }

}
