package com.codefixia.googledrive;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.codefixia.drumcloud.SelectDialog;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

//usually, subclasses of AsyncTask are declared inside the activity class.
//that way, you can easily modify the UI thread from here
public class DownloadFile extends AsyncTask<String, Integer, String> {

	public SelectDialog delegate;
	public String filename="testfile.wav";
	public String outputFilePath;
	public InputStream input=null;

	@Override
	protected String doInBackground(String... sUrl) {
		try {

			int fileLength=0;
			
			// download the file
			if(input==null){
				URL url = new URL(sUrl[0]);
				Log.i("STARTING DOWNLOAD","Path:"+url.getPath());
				URLConnection connection = url.openConnection();
				connection.connect();
				// this will be useful so that you can show a typical 0-100% progress bar
				fileLength = connection.getContentLength();				
				input = new BufferedInputStream(url.openStream());
			}else{
				fileLength=input.available();
			}
			
			System.err.println("Reading "+fileLength+" bytes");
			
			OutputStream output = new FileOutputStream(outputFilePath);

			byte data[] = new byte[1024];
			long total = 0;
			int count;			
			while ((count = input.read(data)) != -1) {
				total += count;
				// publishing the progress....
				if(fileLength>0)
					publishProgress((int) (total * 100 / fileLength));
				else
					publishProgress(0);
				output.write(data, 0, count);
			}

			output.flush();
			output.close();
			input.close();
		} catch (Exception e) {
			System.err.println("Error saving file:"+e);
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		delegate.mProgressDialog.setProgress(100);
		delegate.mProgressDialog.hide();
		delegate.postDownloadCallback(outputFilePath);
	}
	
	public static String getDownloadPath(){		
		return Environment.getExternalStorageDirectory().getPath()+"/drumcloud/downloads/";
	}
	
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        String outputPath=getDownloadPath();
		File outputFile=new File(outputPath);
		if(!outputFile.exists()){
			Log.i("CREATING FOLDER","Path:"+outputFile.getAbsolutePath());
			outputFile.mkdirs();
		}
		outputFilePath=outputPath+filename;
        delegate.mProgressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        if(progress[0]==0){
        	delegate.mProgressDialog.setIndeterminate(true);
        }else{
        	delegate.mProgressDialog.setProgress(progress[0]);
        }
    }	


}
