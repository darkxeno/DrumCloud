package com.codefixia.googledrive;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.selectlibrary.SelectDialog;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

//usually, subclasses of AsyncTask are declared inside the activity class.
//that way, you can easily modify the UI thread from here
public class DownloadFile extends AsyncTask<String, Integer, String> {

	public SelectDialog delegate=null;
	public static SelectDialog lastDelegate=null;
	public static String jsonOutputFilePath=null;
	public String filename="testfile.wav";
	public String outputFilePath;
	public String localPath=null;
	public InputStream input=null;
	public static int filesDownloaded=0;
	public static int filesTotal=0;
	public static boolean packMode=false;

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
				System.out.println("Reading "+fileLength+" bytes");
			}else{
				fileLength=input.available();
			}
			
			Log.i("WRITE FILE","Writing file:"+outputFilePath);
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
			
			if(filename.endsWith("json")){
				loadSoundPack(new File(outputFilePath));
			}
			
		} catch (Exception e) {
			System.err.println("Error saving file:"+e);
		}
		return null;
	}
	
	public void loadSoundPack(File jsonFile){
		JSONArray sounds=DownloadFile.loadJsonSoundPack(jsonFile);
		if(sounds!=null)
		for(int i=0;i<sounds.length();i++){
			String id=null,localPath=null;
			try {
				JSONObject jo=sounds.getJSONObject(i);
				id = jo.optString("googleDriveId");
				localPath = jo.optString("filePath");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if(id!=null){
				GoogleDriveService.downloadFileByID(id,localPath);
			}
		} 		
	}
	
	public static JSONArray loadJsonSoundPack(File file) {
		JSONArray sounds=null;
		if (file.exists() && file.isFile()) {
			try {
				//Read text from file
				StringBuilder text = new StringBuilder();
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;

				while ((line = br.readLine()) != null) {
					text.append(line);
					text.append('\n');
				}

				sounds = new JSONArray(text.toString());
				filesTotal=sounds.length();
				System.out.println("Parsed:"+sounds.length()+" sounds"+sounds);
			}
			catch (IOException e) {
				e.printStackTrace();
			}				
			catch (JSONException e) {
				e.printStackTrace();
			}
		}    
		return sounds;
	}	
	
	public static void incDownloaded(){
		DrumCloud.X.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				synchronized ("sync") {
					filesDownloaded++;
				}
				if(lastDelegate!=null)
					lastDelegate.mProgressDialog.setProgress(filesDownloaded);
				
				if(filesDownloaded==filesTotal){
					packMode=false;
					if(lastDelegate!=null){
						lastDelegate.mProgressDialog.setProgress(1);
						lastDelegate.mProgressDialog.dismiss();
						lastDelegate.postDownloadCallback(jsonOutputFilePath);
						lastDelegate=null;
					}
				}
			
				
			}
		});			
		
	}

	@Override
	protected void onPostExecute(String result) {
		if(filename.endsWith("json")){
			if(delegate!=null)
				delegate.mProgressDialog.setIndeterminate(false);
		}else{
			incDownloaded();
			if(filesDownloaded==filesTotal){
				packMode=false;
				if(lastDelegate!=null){
					delegate=lastDelegate;
					outputFilePath=jsonOutputFilePath;
					lastDelegate=null;
				}
			}			
			if(delegate!=null){
				delegate.mProgressDialog.setProgress(1);
				delegate.mProgressDialog.dismiss();
				delegate.postDownloadCallback(outputFilePath);
			}
		}
	}
	
	public static String getDownloadPath(){		
		return Environment.getExternalStorageDirectory().getPath()+"/drumcloud/downloads/";
	}
	
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
          
        String outputPath=getDownloadPath();
        if(localPath!=null){
        	outputPath+=localPath;
        }
		File outputFile=new File(outputPath);
		if(!outputFile.exists()){
			Log.i("CREATING FOLDER","Path:"+outputFile.getAbsolutePath());
			outputFile.mkdirs();
		}
		outputFilePath=outputPath+filename;
		if(filename.endsWith("json")){
			jsonOutputFilePath=outputFilePath;
			filesDownloaded=0;
			packMode=true;
		}		
		if(delegate!=null){
			lastDelegate=delegate;
			delegate.mProgressDialog.show();
		}
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        if(delegate!=null){
        	if(progress[0]==0){
        		delegate.mProgressDialog.setIndeterminate(true);
        	}else{
        		delegate.mProgressDialog.setProgress(progress[0]);
        	}
        }
    }	


}
