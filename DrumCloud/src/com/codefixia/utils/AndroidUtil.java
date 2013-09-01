package com.codefixia.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.codefixia.drumcloud.DrumCloud;

import android.R.bool;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.Toast;

public class AndroidUtil{

  private static int numCores=-1;
  
  public static int numCores(){
    if(numCores!=-1)
      return numCores;
    else{
      numCores=getNumCores();
      return numCores;
    }
  }
  
  public static boolean isLowVersion(){
	 return (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB);
  }
  
  public static float soundFrequency(){
	  switch (numCores()) {
	  case 1:
		  return 11025;
	  case 2:
		  return 22050;		
	  }

	  return 44100;	
  }  
  
  public static String HTTPGetCall(String WebMethodURL) throws IOException, MalformedURLException 
  { 
	  StringBuilder response = new StringBuilder(); 

	  //Prepare the URL and the connection 
	  URL u = new URL(WebMethodURL); 
	  HttpURLConnection conn = (HttpURLConnection) u.openConnection(); 

	  if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) 
	  { 
		  //Get the Stream reader ready 
		  BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()),8192); 

		  //Loop through the return data and copy it over to the response object to be processed 
		  String line = null; 

		  while ((line = input.readLine()) != null) 
		  { 
			  response.append(line); 
		  } 

		  input.close(); 
	  } 

	  return response.toString(); 
  }  
  
  public static JSONObject HTTPGetJSON(String url){
	  String result;
	  JSONObject obj = null; 	  
	  try {
		  result = HTTPGetCall(url);
		  obj = new JSONObject(result);
	  } catch (MalformedURLException e) {
		  e.printStackTrace();
	  } catch (IOException e) {
		  e.printStackTrace();
	  } catch (JSONException e) {
		  e.printStackTrace();
	  }
	  return obj;
  }

  public static boolean isNetworkAvailable(Context context) {
	    ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }
  
/**
 * Gets the number of cores available in this device, across all processors.
 * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
 * @return The number of cores, or 1 if failed to get result
 */
private static int getNumCores() {
  
    //Private Class to display only CPU devices in the directory listing
    class CpuFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            //Check if filename is "cpu", followed by a single digit number
            if(Pattern.matches("cpu[0-9]", pathname.getName())) {
                return true;
            }
            return false;
        }      
    }

    try {
        //Get directory containing CPU info
        File dir = new File("/sys/devices/system/cpu/");
        //Filter to only list the devices we care about
        File[] files = dir.listFiles(new CpuFilter());
        //Return the number of cores (virtual CPU devices)
        return files.length;
    } catch(Exception e) {
        //Default to return 1 core
        return 1;
    }
}

public static void showToast(final String toast) {
	DrumCloud.X.runOnUiThread(new Runnable() {
		@Override
		public void run() {
			Toast.makeText(DrumCloud.X, toast, Toast.LENGTH_SHORT).show();
		}
	});
}

}