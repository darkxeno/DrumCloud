package com.codefixia.googledrive;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class DummyActivity extends Activity {
	
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
	    
	    Log.d("CREATING","DummyActivity");
	    //echoIntent(intent);
	    
		credential = GoogleAccountCredential.usingOAuth2(this.getApplicationContext(), DriveScopes.DRIVE);
		Intent accountIntent=credential.newChooseAccountIntent();
		startActivityForResult(accountIntent, 0);
	}
	
	public void echoIntent(Intent intent){
		Integer requestCode=intent.getIntExtra("requestCode", 0);
		startActivityForResult(intent, requestCode);
	}
	
	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
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
	}
	
	@Override
	public void onDestroy() 
	{   
		Log.d("ONDESTROY","Detroying DummyActivity");
	   super.onDestroy();  
	}

}
