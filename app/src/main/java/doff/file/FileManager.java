package doff.file;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by kmo on 11/16/2017.
 */

public class FileManager {

    static final int READ_BLOCK_SIZE = 100;

    public static void WriteReadableExternalStorage(Activity activity, String fileName, String txt) {
        try {
            String path = activity.getExternalFilesDir(null)+ "/" + fileName;
            Log.d("doff-file", "WriteReadableExternalStorage: path: "+path);
            FileOutputStream fos = new FileOutputStream(path);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fos);
            outputWriter.write(txt);
            outputWriter.close();

            File file = new File(fileName);
            file.setReadable(true);
            Toast.makeText(activity.getBaseContext(), "File write readable: "+path, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("doff-file", "exception: "+e.getMessage());
            e.printStackTrace();
        }
    }
    public static String ReadExternalStorage(Activity activity, String fileName) {
        String txt="";
        try {
            String path = activity.getExternalFilesDir(null)+ "/" + fileName;
            FileInputStream fileIn=new FileInputStream(path);
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[READ_BLOCK_SIZE];
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                txt +=readstring;
            }
            InputRead.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return txt;
    }


    public static void WriteReadable(Activity activity, String fileName, String txt) {
        try {
            /**
             * String path = context.getExternalFilesDir(null)+ "/" + name;

             FileOutputStream fos = new FileOutputStream(path);
             document.writeTo(fos);
             File file = new File(path);
             file.setReadable(true);

             Log.d("doff-pdf", path);
             */
            Log.d("doff-file", activity.getFilesDir().toString());
            FileOutputStream fileout=activity.openFileOutput(fileName, MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write(txt);
            outputWriter.close();

            File file = new File(fileName);
            file.setReadable(true);
            Toast.makeText(activity.getBaseContext(), "File write readable: "+activity.getFilesDir().toString(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("doff-file", "exception: "+e.getMessage());
            e.printStackTrace();
        }
    }
    public static void WriteNotReadable(Activity activity, String fileName, String txt) {
        try {
            FileOutputStream fileout=activity.openFileOutput(fileName, MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write(txt);
            outputWriter.close();
            Log.d("doff-file", activity.getFilesDir().toString());
            Toast.makeText(activity.getBaseContext(), "File write not readable: "+activity.getFilesDir().toString(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String Read(Activity activity, String fileName) {
        String txt="";
        try {
            FileInputStream fileIn=activity.openFileInput(fileName);
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[READ_BLOCK_SIZE];
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                txt +=readstring;
            }
            InputRead.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return txt;
    }

    public static File[] listAllFilesInExternalDirectory(Activity activity) {
        String path = activity.getExternalFilesDir(null).toString();
        //String path = Environment.getExternalStorageDirectory().toString();
        Log.d("doff-file", "getExternalStorageDirectory: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Log.d("doff-file", "FileName:" + files[i].getName());
        }

        return files;
    }
    public static File[] listAllFilesInInternalDirectory(Activity activity) {
        String path = activity.getFilesDir().toString();
        Log.d("doff-file", "getInternalStorageDirectory: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Log.d("doff-file", "FileName:" + files[i].getName());
        }

        return files;
    }

    public static boolean isExternalFileExisting(Activity activity, String fileName) {
        String path = activity.getExternalFilesDir(null)+ "/" + fileName;
        File file = new File(path);
        return file.exists();
    }
    public static String fileNameWithDate(String fileName )
    {
        String newFileName = "";
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd_HHmmSS");

        String[] a = fileName.split("\\.");
        newFileName += a[0];
        newFileName += simpleDateFormat.format(date);
        newFileName += "." + a[1];
        return newFileName;
    }
}
