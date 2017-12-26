package doff.file;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.Page;
import android.graphics.pdf.PdfDocument.PageInfo;

public class Pdf {

    public final static float A4_PostscriptHeight = 842;
    public final static float HeightDiv15 = A4_PostscriptHeight / 15f;
    public final static float mm = A4_PostscriptHeight / 270f;
    public final static float h10 = A4_PostscriptHeight / 27f;
    public final static float h10half = h10 / 2;
    public final static float h11 = A4_PostscriptHeight - 2 * h10;
    public final static float h12 = h10;

    public final static float A4_PostscriptWidth = 595;
    public final static float w10 = A4_PostscriptWidth / 20;
    public final static float w11 = A4_PostscriptWidth - 2 * w10;
    public final static float w12 = w10;

    private static float text_size = 12;
    private static float dy_question_space = 6*mm;
    private static int page_no = 0;

    private static float pdf_header(Canvas c, Paint p)
    {
        String s = String.format("%s %s", "förnamn","efternamn");
        p.setTextSize(text_size);
        c.drawText(s, 10, 10*mm, p);
        //Draw.DrawInBox(s, 10, c, p, 10, 0, h10, A4_PostscriptWidth, h10);
        return 2*h10+10*mm;
    }

    private static void pdf_footer(Canvas c, Paint p)
    {
        p.setColor(Color.BLACK);
        //Draw.DrawInBox(""+page_no, (int) text_size, c, p, 10, 0, A4_PostscriptHeight-h10, A4_PostscriptWidth, h10);
    }

    public static void data2pdf(Context context, String fileName)
    {
        try
        {

            float dy = 10 * mm;
            float xx, yy, ww, hh;
            String str_q, str_a, str_ua, str_s, str_t;

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

            dy += pdf_header(c,p);
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
//                    Log.i(TAG,"Pdf.xml2pdf, height="+pdq.height+"mm");
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
            pdf_footer(c,p);
            document.finishPage(page);
            //String path = context.getFilesDir()+ "/" + fileName + date.toString();
            String name = FileManager.fileNameWithDate(fileName);
            String path = context.getExternalFilesDir(null)+ "/" + name;

            FileOutputStream fos = new FileOutputStream(path);
            document.writeTo(fos);
            File file = new File(path);
            file.setReadable(true);

            Log.d("doff-pdf", path);
        } catch (Exception e)
        {
            Log.e("doff-file",  e.getMessage());
            e.printStackTrace();
        } finally
        {
        }
    }


}
