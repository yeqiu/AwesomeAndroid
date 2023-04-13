package com.yeqiu.awesomeandroid;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.yeqiu.common.UtilsKt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
@RequiresApi(api = Build.VERSION_CODES.Q)
public class TxtUtil{

    private static final String TXT = "/txt/";
    private static volatile TxtUtil instance;
    private static final Uri externalUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
//    private static final String TXT_PATH = Environment.DIRECTORY_DOWNLOADS + TXT;
    private static final String TXT_PATH = Environment.DIRECTORY_DOWNLOADS ;
    private Context context;

    public static TxtUtil getInstance(){
        if(instance == null){
            synchronized(TxtUtil.class){
                if(instance == null){
                    instance = new TxtUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 写txt文件
     */
    public void writeTxt(Context context,String fileName, String content){
        setContext(context);
        Log.e("VERSION", Build.VERSION.SDK_INT + "");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            deleteFile(fileName);
            createSAFTxt(fileName, content);
        }else{
        }
    }

    private void createSAFTxt(String fileName, String content){


        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Downloads.RELATIVE_PATH, TXT_PATH);
        Uri insertResult = getContext().getContentResolver().insert(externalUri, contentValues);
        if(insertResult != null){
            try{
                OutputStream outputStream =  getContext().getContentResolver().openOutputStream(insertResult);
                PrintWriter printWriter = new PrintWriter(outputStream);
                printWriter.print(content);
                printWriter.flush();
                printWriter.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除txt 文件
     */
    public void deleteFile(String fileName){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            deleteSAFFile(fileName);
        }else{
        }
    }

    private void deleteSAFFile(String fileName){
        String selection = MediaStore.Downloads.DISPLAY_NAME + "=? and " + MediaStore.Downloads.RELATIVE_PATH + "=?";
        String[] selectionArgs = new String[]{fileName, TXT_PATH};
        //使用uri参数来确定查询哪张表，projection参数用于确定查询哪些列，selection和selectionArgs参数用于约束查询哪些行；
        // sortorder参数用于对结果进行排序，查询的结果存放在Cursor对象中返回。
        int delete =  getContext().getContentResolver().delete(externalUri, selection, selectionArgs);
        Log.e("delete:___________", delete + "");
    }

    /**
     * 删除txt 文件
     */
    public String readFile(Context context,String fileName){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            return readSAFFile(context,fileName);
        }else{
        }
        return "";
    }

    @SuppressLint("ShowToast")
    private String readSAFFile(Context context,String fileName){
        setContext(context);
        String selection = MediaStore.Downloads.DISPLAY_NAME + "=? and " + MediaStore.Downloads.RELATIVE_PATH + "=?";
        String[] selectionArgs = new String[]{fileName, TXT_PATH};
        Cursor query =  getContext().getContentResolver().query(externalUri, null, selection, selectionArgs, null);
        String uriPath = "";
        if(query != null && query.moveToFirst()){
            uriPath = query.getString(query.getColumnIndexOrThrow(MediaStore.Downloads.DATA));
        }
        if(query != null){
            query.close();
        }
        String content = "";
        if(uriPath != null && uriPath.length() > 0){
            InputStream instream;
            try{
                instream = new FileInputStream(uriPath);
                InputStreamReader inputStreamReader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputStreamReader);
                String line;
                while((line = buffreader.readLine()) != null){
                    content += line;
                }
                buffreader.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return content;
    }


    /**
     重命名txt 文件
     */
    public boolean renameFile(String oldFileName, String newFileName){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            String filePath = getFilePath(getContext(),oldFileName);
            File file = new File(filePath);
            File file2 = new File(filePath.replace(oldFileName, newFileName));
            if(file.exists()){
                return file.renameTo(file2);
            }
        }else{
        }
        return false;
    }
    public String getFilePath(Context context ,String fileName){
        setContext(context);
        String selection = MediaStore.Downloads.DISPLAY_NAME + "=? and " + MediaStore.Downloads.RELATIVE_PATH + "=?";
        String[] selectionArgs = new String[]{fileName, TXT_PATH};

        UtilsKt.log("TXT_PATH = "+TXT_PATH);
        UtilsKt.log("externalUri = "+externalUri);
        UtilsKt.log("fileName = "+fileName);

        Cursor query =  getContext().getContentResolver().query(externalUri, null, selection, selectionArgs, null);
        String filePath = "";
        if(query != null && query.moveToFirst()){
            filePath = query.getString(query.getColumnIndexOrThrow(MediaStore.Downloads.DATA));
        }
        if(query != null){
            query.close();
        }
        return filePath;
    }



    private void setContext(Context context){

        this.context = context;
    }

    public Context getContext(){
        Context context= null;
        return   this.context;
    }

}
