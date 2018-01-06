package doff.file;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.ImageView;

import com.doffs.skorsten.R;

import java.io.File;

public class PdfRender {

    private Activity activity=null;
    private ImageView imageView = null;
    private int currentPage = 0;
    //private Button next, previous;

    public PdfRender(Activity activity, ImageView imageView) {
        this.activity=activity;
        this.imageView = imageView;
    }

    public void render(String fileName) {
        try {

            int REQ_WIDTH = 1;
            int REQ_HEIGHT = 1;
            REQ_WIDTH = imageView.getWidth();
            REQ_HEIGHT = imageView.getHeight();

            Bitmap bitmap = Bitmap.createBitmap(REQ_WIDTH, REQ_HEIGHT, Bitmap.Config.ARGB_4444);
            //File file = new File("/sdcard/Download/test.pdf");
            String path = activity.getExternalFilesDir(null)+ "/" + fileName;
            File file = new File(path);
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));

            if (currentPage < 0) {
                currentPage = 0;
            } else if (currentPage > renderer.getPageCount()) {
                currentPage = renderer.getPageCount() - 1;
            }

            Matrix m = imageView.getImageMatrix();
            Rect rect = new Rect(0, 0, REQ_WIDTH, REQ_HEIGHT);
            //Rect rect = new Rect(0, 0, 595, 842);
            //renderer.openPage(currentPage).render(bitmap, rect, m, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            renderer.openPage(currentPage)
                    .render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            imageView.setImageMatrix(m);
            imageView.setImageBitmap(bitmap);
            imageView.invalidate();

        } catch (Exception e) {
            Log.e("doff-pdf", e.getMessage());
            e.printStackTrace();
        }
    }
}
