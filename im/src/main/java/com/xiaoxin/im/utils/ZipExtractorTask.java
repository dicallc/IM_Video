package com.xiaoxin.im.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.file.zip.ZipEntry;
import com.file.zip.ZipFile;
import com.xiaoxin.im.model.AppStoreModel;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipException;
import org.greenrobot.eventbus.EventBus;

public class ZipExtractorTask extends AsyncTask<Void, Integer, Long> {
    private final String TAG = "ZipExtractorTask";
    private final File mInput;
    private final File mOutput;
    //private final ProgressDialog mDialog;
    private int mProgress = 0;
    private final Context mContext;
    private boolean mReplaceAll;
    public ZipExtractorTask(String in, String out, Context context, boolean replaceAll){
        super();
        mInput = new File(in);
        mOutput = new File(out);
        if(!mOutput.exists()){
            makeFile();
        }else{

            //存在即删除掉,在解压
            File mFile = new File(out );
            deleteDir(mFile);
            mFile.exists();
            makeFile();
        }
        if(context!=null){
            //mDialog = new ProgressDialog(context);
        }
        else{
            //mDialog = null;
        }
        mContext = context;
        mReplaceAll = replaceAll;
    }

    private void makeFile() {
        if(!mOutput.mkdirs()){
            Log.e(TAG, "Failed to make directories:"+mOutput.getAbsolutePath());
        }
    }

    @Override
    protected Long doInBackground(Void... params) {
        // TODO Auto-generated method stub
        return unzip();
    }

    @Override
    protected void onPostExecute(Long result) {
        // TODO Auto-generated method stub
        Toast.makeText(mContext, "解压完成", Toast.LENGTH_SHORT).show();
        EventBus.getDefault().post(new AppStoreModel("110"));
        //super.onPostExecute(result);
        //if(mDialog!=null&&mDialog.isShowing()){
        //    mDialog.dismiss();
        //}
        if (isCancelled()) {
            return;
        }
    }
    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        //super.onPreExecute();
        //if(mDialog!=null){
        //    mDialog.setTitle("Extracting");
        //    mDialog.setMessage(mInput.getName());
        //    mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //    mDialog.setOnCancelListener(new OnCancelListener() {
        //
        //        @Override
        //        public void onCancel(DialogInterface dialog) {
        //            // TODO Auto-generated method stub
        //            cancel(true);
        //        }
        //    });
        //    mDialog.show();
        //}
    }
    @Override
    protected void onProgressUpdate(Integer... values) {
        // TODO Auto-generated method stub
        //super.onProgressUpdate(values);
        //if (mDialog == null) {
        //  return;
        //}
        //if (values.length > 1) {
        //  int max = values[1];
        //  mDialog.setMax(max);
        //} else {
        //  mDialog.setProgress(values[0].intValue());
        //}
    }


    private long getOriginalSize(ZipFile file){
        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) file.getEntries();
        long originalSize = 0l;
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            if(entry.getSize()>=0){
                originalSize+=entry.getSize();
            }
        }
        return originalSize;
    }

    private int copy(InputStream input, OutputStream output){
        byte[] buffer = new byte[1024*8];
        BufferedInputStream in = new BufferedInputStream(input, 1024*8);
        BufferedOutputStream out  = new BufferedOutputStream(output, 1024*8);
        int count =0,n=0;
        try {
            while((n=in.read(buffer, 0, 1024*8))!=-1){
                out.write(buffer, 0, n);
                count+=n;
            }
            out.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            try {
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return count;
    }

    private final class ProgressReportingOutputStream extends FileOutputStream{

        public ProgressReportingOutputStream(File file)
            throws FileNotFoundException {
            super(file);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void write(byte[] buffer, int byteOffset, int byteCount)
            throws IOException {
            // TODO Auto-generated method stub
            super.write(buffer, byteOffset, byteCount);
            mProgress += byteCount;
            publishProgress(mProgress);
        }

    }
    private long unzip(){
        long extractedSize = 0L;
        Enumeration<ZipEntry> entries;
        ZipFile zip = null;
        try {
            zip = new ZipFile(mInput);
            long uncompressedSize = getOriginalSize(zip);
            publishProgress(0, (int) uncompressedSize);

            entries = (Enumeration<ZipEntry>) zip.getEntries();
            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                if(entry.isDirectory()){
                    continue;
                }
                File destination = new File(mOutput, entry.getName());
                if(!destination.getParentFile().exists()){
                    Log.e(TAG, "make="+destination.getParentFile().getAbsolutePath());
                    destination.getParentFile().mkdirs();
                }
                if(destination.exists()&&mContext!=null&&!mReplaceAll){

                }
                ProgressReportingOutputStream outStream = new ProgressReportingOutputStream(destination);
                extractedSize+=copy(zip.getInputStream(entry),outStream);
                outStream.close();
            }
        } catch (ZipException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            try {
                if (null!=zip)
                    zip.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return extractedSize;
    }
    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

}  