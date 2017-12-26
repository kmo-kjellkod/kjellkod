package doff.file;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import java.lang.reflect.Method;

public final class Draw
{
    private static String TAG = "doff-file";

    public enum Shape
    {
        Circle, Square
    }

    private static Rect bounds = new Rect();
    public static void TextInABox(String txt, float sizeIn, Canvas c, Paint p, float delta, float x, float y, float w, float h)
    {
        p.setTypeface(Typeface.MONOSPACE);

        float size = 1;

        if (txt.compareTo("@borrow10") == 0) txt = "10";

        if (txt.compareTo("@keyLine") == 0)
        {
            c.drawLine(x, y + h / 2, x + w, y + h / 2, p);
        } else if (txt.compareTo("@arrowDown") == 0)
        {
            c.drawLine(x + w / 2, y + h, x + w / 2, y, p);
            c.drawLine(x + w / 2, y + h, x + w / 2 + w / 4, y + h - w / 4, p);
            c.drawLine(x + w / 2, y + h, x + w / 2 - w / 4, y + h - w / 4, p);
        } else if (txt.compareTo("@arrowUp") == 0)
        {
            c.drawLine(x + w / 2, y + h, x + w / 2, y, p);
            c.drawLine(x + w / 2, y, x + w / 2 + w / 4, y + w / 4, p);
            c.drawLine(x + w / 2, y, x + w / 2 - w / 4, y + w / 4, p);
        } else if (txt.compareTo("@arrowRight") == 0)
        {
            c.drawLine(x, y + h / 2, x + w, y + h / 2, p);
            c.drawLine(x + w, y + h / 2, x + w - h / 4, y + h / 2 - h / 4, p);
            c.drawLine(x + w, y + h / 2, x + w - h / 4, y + h / 2 + h / 4, p);
        } else if (txt.compareTo("@arrowLeft") == 0)
        {
            c.drawLine(x, y + h / 2, x + w, y + h / 2, p);
            c.drawLine(x, y + h / 2, x + h / 4, y + h / 2 - h / 4, p);
            c.drawLine(x, y + h / 2, x + h / 4, y + h / 2 + h / 4, p);
        } else if (txt.contains("\n"))
        {
            DrawTextRectangle(txt, 0, c, p, x, y, w, h);
        } else
        {
            if (sizeIn > 0)
            {
                size = sizeIn;
            } else
            {
                size = SizeForTextInABox(txt, p, x, y, w, h);
            }

            p.setTextAlign(Align.LEFT);
            p.setTextSize(size);
            p.getTextBounds(txt, 0, txt.length(), bounds);
            float wb = p.measureText(txt);
            if ((txt.compareTo("+") == 0) || (txt.compareTo("*") == 0))
            {
                //bounds.offsetTo((int) (x + w / 2 - wb / 2), (int) (y + h/2-bounds.height()/2 ) );
                bounds = new Rect();
                bounds.left = (int) (x + w / 2 - wb / 2);
                bounds.right = (int) (x + w / 2 + wb / 2);
                bounds.top = (int) (y + 0);
                bounds.bottom = (int) (y + h);
            } else if ((txt.compareTo("-") == 0) || (txt.compareTo("=") == 0))
            {
                bounds.offsetTo((int) (x + w / 2 - wb / 2), (int) (y + h / 2 - bounds.height() / 2 + p.descent()));
            } else
            {
                //bounds.offsetTo((int) (x + w/2 - bounds.width()/2),
                // (int) (y + h / 2 - bounds.height() / 2));
                bounds.offsetTo((int) (x + w / 2 - wb / 2), (int) (y + h / 2 - bounds.height() / 2)); //@change 20150101
            }
            c.drawText(txt, bounds.left, bounds.bottom, p);
            //p.setStyle(Style.STROKE);
            //c.drawRect(bounds,p);
            // p.setStyle(Style.STROKE);
            // c.drawRect(bounds, p);
            // p.setStyle(Style.FILL);

            // c.drawText(txt, x + w / 2 - bounds.width() / 2, y + h / 2 +
            // bounds.height() / 2, p);
        }
    }

    public static float SizeForTextInABox(String txt, Paint p, float x, float y, float w, float h)
    {
        p.setTypeface(Typeface.MONOSPACE);

        float size = 1;

        for (int i = 1; i < 700; i++)
        {
            p.setTextSize(i);
            p.getTextBounds(txt, 0, txt.length(), bounds);
            if ((bounds.width() < (w)) && (bounds.height() < (h)))
            {
                size = i;
            } else break;
        }
        size = size - size * 0.2f;
        if (size < 6) size = 6;


        return size;
    }

    public static void TextInABoxLeftAligned(String txt, Canvas c, Paint p, float delta, float x, float y, float w, float h)
    {
        p.setTypeface(Typeface.MONOSPACE);

        float size = 1;

        for (int i = 1; i < 100; i++)
        {
            p.setTextSize(i);
            p.getTextBounds(txt, 0, txt.length(), bounds);
            if ((bounds.width() < (w - 2 * delta)) && (bounds.height() < (h - 2 * delta)))
            {
                size = i;
            } else break;
        }
        size = size - size * 0.2f;
        if (size < 12) size = 12;

        p.setTextAlign(Align.LEFT);
        p.setTextSize(size);
        p.getTextBounds(txt, 0, txt.length(), bounds);
        c.drawText(txt, x + delta, y + h / 2 + bounds.height() / 2 - delta, p);
    }

    public static void TextInABox2(String txt, Canvas c, Paint p, float delta, float x, float y, float w, float h)
    {
        p.setTypeface(Typeface.MONOSPACE);

        float size = 1;

        for (int i = 1; i < 100; i++)
        {
            p.setTextSize(i);
            p.getTextBounds(txt, 0, txt.length(), bounds);
            if ((bounds.width() < (w - 2 * delta)) && (bounds.height() < (h - 2 * delta)))
            {
                size = i;
            } else break;
        }

        p.setTextSize(size);
        p.getTextBounds(txt, 0, txt.length(), bounds);
        c.drawText(txt, x + w / 2 - bounds.width() / 2, y + h / 2 + bounds.height() / 2, p);
    }

    public static void PictureInAbox(Canvas c, Paint paint, Bitmap bitmap, float offset)
    {
        float w = c.getWidth() - 2 * offset;
        float h = c.getHeight() - 2 * offset;
        float ratio1 = w / bitmap.getWidth();
        float ratio2 = h / bitmap.getHeight();
        float scale = ratio1 > ratio2 ? ratio2 : ratio1;

        w = scale * bitmap.getWidth();
        h = scale * bitmap.getHeight();
        float ox = (c.getWidth() - w) / 2;
        float oy = (c.getHeight() - h) / 2;
        bounds.set((int) ox, (int) oy, (int) (ox + w), (int) (oy + h));
        c.drawBitmap(bitmap, null, bounds, paint);
    }

    public static void PictureInAbox(Canvas c, Paint paint, Bitmap bitmap, Rect dst)
    {
        float w = dst.width();
        float h = dst.height();
        float ratio1 = w / bitmap.getWidth();
        float ratio2 = h / bitmap.getHeight();
        float scale = ratio1 > ratio2 ? ratio2 : ratio1;

        w = scale * bitmap.getWidth();
        h = scale * bitmap.getHeight();
        float ox = (dst.width() - w) / 2;
        float oy = (dst.height() - h) / 2;
        bounds.set((int) (dst.left + ox), (int) (dst.top + oy), (int) (dst.left + ox + w), (int) (dst.top + oy + h));
        //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        c.drawBitmap(bitmap, null, bounds, paint);
    }


    private static int text_matrix(int n, float w, float h)
    {
        float size = 9;

        while ((h / size) * (w / size) > n) size += 1;

        return (int) size;
    }

    private static TextPaint tp = new TextPaint();

    public static void DrawTextRectangle2(String txt, int rowsInRectangle, Canvas c, Paint p, float x0, float y0, float w0, float h0)
    {

        tp.set(p);
        float size = text_matrix(txt.length() * 2, w0, h0);
        tp.setTextSize(size);
        txt = txt.replace('\n', ' ').trim();
        StaticLayout sl = new StaticLayout(txt, tp, (int) w0 - 10, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

        //Log.v("TEST","Size="+tp.getTextSize());
        int dy = (int) y0 + (int) ((h0 - sl.getHeight()) / 2);
        int dx = (int) x0 + 5;
        c.translate(dx, dy);
        sl.draw(c);
        c.translate(-dx, -dy);
    }

    /**
     * Text formatted by the user of the method. Rows are delimited by the new line char '\n'.
     *
     * @param txt             Text body of rows, each row separated by a '\n'
     * @param rowsInRectangle If this parameter is zero, the number of rows is determined by the
     *                        number of rows in txt.
     */
    public static void DrawTextRectangle(String txt, int rowsInRectangle, Canvas c, Paint p, float x0, float y0, float w0, float h0)
    {
        String[] text = txt.split("\\n");

        int rows = 0;
        rows = rowsInRectangle == 0 ? text.length : rowsInRectangle;

        int iMax = 0;
        for (int i = 1; i < text.length; i++)
        {
            if (text[iMax].length() < text[i].length()) iMax = i;
        }

        float h1 = h0 / rows;

        float textSize = Draw.SizeForTextInABox(text[iMax], p, x0, y0, w0 - 10, h1 * 0.8f);
        p.setTextSize(textSize);

        float h2 = h1;
        if ((int) (h0 / (1.5f * textSize)) > rows)
        {
            h2 = 1.5f * textSize;
        }
        for (int i = 0; i < text.length; i++)
        {
            c.drawText(text[i], x0 + 5, y0 + h1 + i * h2 - 10, p);
        }
    }

    public static void DrawPlusCharacter(Canvas c, Paint p, float x0, float y0, float w0, float h0)
    {
        float strokeWidth = 1;

        strokeWidth = w0 * 0.05f > strokeWidth ? w0 * 0.05f : 1;
        strokeWidth = h0 * 0.05f < w0 * 0.05f ? h0 * 0.05f : strokeWidth;
    }
}
