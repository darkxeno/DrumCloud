package com.codefixia.googledrive;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class DummyActivity extends Activity {
	

	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_AUTHORIZATION = 2;	
	private static GoogleAccountCredential credential;
	private String operation;
	
	public static GoogleAccountCredential getCredential() {
		return credential;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title bar
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		//Remove notification bar
		//this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		//this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		//this.setTheme(android.R.style.Theme_Translucent_NoTitleBar);
		
		
	    // Get intent, action and MIME type
	    Intent intent = getIntent();
	    operation=intent.getStringExtra("operation");
	    if(operation==null)
	    	operation=intent.getAction();
	    
	    Log.d("CREATING","DummyActivity for operation:"+operation);
	    //echoIntent(intent);
	     
	    if(operation!=null && operation.equalsIgnoreCase("REQUEST_AUTHORIZATION")){
	    	Intent i=(Intent)intent.getParcelableExtra("intent");
	    	if(i!=null){
	    		Log.i("DUMMY","REQUEST_AUTHORIZATION");
	    		startActivityForResult(i, REQUEST_AUTHORIZATION);
	    	}    	
	    }else{
	    	credential = GoogleAccountCredential.usingOAuth2(this.getApplicationContext(), DriveScopes.DRIVE);
			Intent accountIntent=credential.newChooseAccountIntent();
			startActivityForResult(accountIntent, REQUEST_ACCOUNT_PICKER);
	    }
	}
	
	public void echoIntent(Intent intent){
		Integer requestCode=intent.getIntExtra("requestCode", 0);
		startActivityForResult(intent, requestCode);
	}
	
	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
	    switch (requestCode) {
	    case REQUEST_ACCOUNT_PICKER:
			if(data!=null){			
				Log.d("INTENT","Action"+data.getAction()+" categories:"+data.getCategories()+" data:"+data.getDataString());
				Intent i=new Intent(this,GoogleDriveService.class);
				i.fillIn(data, Intent.FILL_IN_ACTION|Intent.FILL_IN_DATA);
				i.putExtra("requestCode", requestCode);
				i.putExtra("resultCode", resultCode);
				i.putExtra("operation", operation);
				startService(i);
				finish();
			}else{
				Intent i=new Intent(this,GoogleDriveService.class);
				i.putExtra("requestCode", requestCode);
				i.putExtra("resultCode", resultCode);
				//i.putExtra("operation", "loginCanceled");
				startService(i);			
				finish();
			}
	      break;
	    case REQUEST_AUTHORIZATION:
	      if (resultCode != Activity.RESULT_OK) {
	    	  startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	      } else {
	    	  finish();
	      }
	      break;
	    }

	}
	
	@Override
	public void onDestroy() 
	{   
		Log.d("ONDESTROY","Detroying DummyActivity");
	   super.onDestroy();  
	}

}
