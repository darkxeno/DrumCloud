package com.codefixia.googledrive;
import android.R;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import processing.core.PApplet;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.drumcloud.FileItem;
import com.codefixia.drumcloud.FileType;
import com.codefixia.drumcloud.SelectDialog;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Children;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleDriveService extends IntentService {

	static final int REQUEST_AUTHORIZATION = 2;
	static final int CAPTURE_IMAGE = 3;
	static final int REQUEST_FOLDER_FILES = 4;

	private static Uri fileUri;
	private static Drive service=null;
	private java.io.File selectedFile=null;
	private String filePath;
	private String folderId;
	private String operation;
	private static Boolean uploadMode=false;
	private String fileId;
	final static public String googleDriveMainFolderId="0B7AaL1Q9Q5fTZGY2eG12WDc5aFU";
	final static public String[] folderIds={"0B7AaL1Q9Q5fTb2RScXBFMGljTm8","0B7AaL1Q9Q5fTZGY2eG12WDc5aFU","0B7AaL1Q9Q5fTLWU1b1VkRXdvV00"};
	final static public String[] folderNames={"/DrumCloud","/DrumCloud/Samples","/DrumCloud/Samples/DrumSets"};
	final static public String[] folderCategoryIds={"0B7AaL1Q9Q5fTX1pjNnViTFMzVG8","0B7AaL1Q9Q5fTRHZHbldlbWFjREk","0B7AaL1Q9Q5fTMTZGaFFKM1JuVWc","0B7AaL1Q9Q5fTdVU1MTNuNkFJZHM","0B7AaL1Q9Q5fTYmJYUmR6MElvUDQ","0B7AaL1Q9Q5fTdVVOWEdQdFNNSEU","0B7AaL1Q9Q5fTbXBzZHIyaENQOFE","0B7AaL1Q9Q5fTYjlzeEg0LUJvMUE"};
	final static public String[] folderCategoryNames={"Others","Kick","Bass","Snare","Hats","Percurssion","Synth","FX"};
	private int categorySelected=-1;
	private final IBinder myBinder = new MyLocalBinder();
	
	public static SelectDialog delegate;
	//private Activity mainActivity=DrumCloud.activity;

	public GoogleDriveService() {
		super("GoogleDriveService");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public class MyLocalBinder extends Binder {
		public GoogleDriveService getService() {
			return GoogleDriveService.this;
		}
	}	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		String newOperation=intent.getStringExtra("operation");
		Log.d("DRIVESERVICE","Last operation:\""+operation+"\" new:"+newOperation);
		if(newOperation!=null && newOperation.length()>0){
			operation=newOperation;
			folderId=intent.getStringExtra("folderId");
			filePath=intent.getStringExtra("filePath");
			Log.d("DRIVESERVICE","Received operation:\""+operation+"\" folderId:"+folderId+" filePath:"+filePath);
			if(service!=null){
				processOperation(operation);
			}else{
				Intent i=new Intent(this,DummyActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				//i.putExtra("operation", operation);
				startActivity(i);				
			}
		}else{//response from DummyActivity
			int resultCode=intent.getIntExtra("resultCode",0);
			int requestCode=intent.getIntExtra("requestCode",0);
			if (resultCode == Activity.RESULT_OK && intent != null && intent.getExtras() != null) {
				String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					DummyActivity.getCredential().setSelectedAccountName(accountName);
					service = getDriveService(DummyActivity.getCredential());
					Log.d("DRIVESERVICE","Account name:"+accountName+" recovering operation:"+operation);
					if(service!=null)
						processOperation(operation);
				}else{
					returnCanceledLogin();
				}
			}else{
				returnCanceledLogin();
			}
		}
	}

	public void processOperation(final String operation){
		if(operation==null){
			showToast("Undefined operation");
		}else if(operation.equalsIgnoreCase("filesInFolder")){
			filesInFolder(folderId);
		}else if(operation.equalsIgnoreCase("startCameraIntent")){
			startCameraIntent();
		}else if(operation.equalsIgnoreCase("downloadFile")){
			downloadFile(filePath);
		}else if(operation.equalsIgnoreCase("uploadFile")){
			uploadFile(filePath);
		}else{
			showToast("Unimplemented operation:"+operation);
		}				
	}
	
	private Drive getDriveService(GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
		.build();
	}

	private void startCameraIntent() {
		String mediaStorageDir = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES).getPath();
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		fileUri = Uri.fromFile(new java.io.File(mediaStorageDir + java.io.File.separator + "IMG_"
				+ timeStamp + ".jpg"));

		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		//startActivityForResult(cameraIntent, CAPTURE_IMAGE);
	}
	
	public String getFolderById(String fileId){
		
		for(int i=0;i<folderIds.length;i++){
			if(folderIds[i].equalsIgnoreCase(fileId))
				return folderNames[i];
		}
		return "/UnknowFolder";
	}

	public void filesInFolder(final String folderId){
		final GoogleDriveService GDS=this;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				List<FileItem> fileItemList=new ArrayList<FileItem>();
				if(service==null){
					Log.e("DRIVE SERVICE","ERROR NOT INITIALIZED");
					returnFiles(fileItemList);
					return;
				}		
				Files.List request;
				try {
					request = service.files().list().setQ("'"+((folderId==null)?googleDriveMainFolderId:folderId)+"' in parents");
					do {
						//try {
							FileList oneFile = request.execute();
							
							for (File child : oneFile.getItems()) {
								FileItem fileItem=getFileItem(child);
								if(fileItem!=null)
									fileItemList.add(fileItem);
								System.out.println("File Id: " + child.toString());
							}
							request.setPageToken(oneFile.getNextPageToken());
						//} catch (IOException e) {
						//	System.out.println("An error occurred: " + e);
						//	request.setPageToken(null);
						//}
					} while (request.getPageToken() != null &&
							request.getPageToken().length() > 0);			
					
				} catch (final UserRecoverableAuthIOException e) {
					Log.e("NOT AUTHORIZED","NOT AUTHORIZED");					
					requestAuth(GDS,e);
				}catch (IOException e1) {
					e1.printStackTrace();
				}
				returnFiles(fileItemList);
			}		
		});
		t.start();		
	}
	
	
	private static void requestAuth(final Context context,final UserRecoverableAuthIOException e){		
		DrumCloud.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				delegate.hide();
				Intent i=new Intent(DrumCloud.activity,DummyActivity.class);
				i.setAction("REQUEST_AUTHORIZATION");
				//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("operation", "REQUEST_AUTHORIZATION");
				i.putExtra("intent", e.getIntent());
				DrumCloud.activity.startActivity(i);
				DrumCloud.activity.overridePendingTransition (R.anim.fade_in,R.anim.fade_out);
			}
		});		
	}	
	
	private void returnFiles(final List<FileItem> childs) {
		DrumCloud.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				delegate.callbackDriveFolderList(childs);
			}
		});		
		//fa.finish();
	}
	
	private static void hideDialog() {
		DrumCloud.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				delegate.hide();
			}
		});		
	}	

	private static FileItem getFileItem(String fileId) {
		try {
			Log.i("FILE INFO","File: " + fileId);
			File file = service.files().get(fileId).execute();
			FileItem item=new FileItem(file.getTitle(),(file.getFileSize()>0)?FileType.File:FileType.Folder,new java.io.File(file.getSelfLink()));
			item.downloadUrl=file.getDownloadUrl();
			item.fileId=fileId;
			Log.i("FILE INFO","Download url: " + file.getDownloadUrl());
			Log.i("FILE INFO","Title: " + file.getTitle());
			Log.i("FILE INFO","MIME type: " + file.getMimeType());
			return item;
		}catch (UserRecoverableAuthIOException e) {
			requestAuth(delegate.getContext(),e);						
		}catch (IOException e) {
			Log.e("FILE INFO ERROR","An error occured: " + e);
		}
		return null;
	}	
	
	private FileItem getFileItem(File file) {
		FileType fileType=file.getMimeType().contains("folder")?FileType.Folder:FileType.File;
		FileItem fileItem=new FileItem(file.getTitle(),fileType, new java.io.File(file.getSelfLink()) );
		fileItem.downloadUrl=file.getDownloadUrl();
		fileItem.parentFolderId=folderId;
		fileItem.fileId=file.getId();
		fileItem.extension=file.getFileExtension();
		fileItem.size=file.getFileSize();
		fileItem.isOnline=true;
		
		if(file.getCreatedDate()!=null)
			fileItem.creationDate=file.getCreatedDate().getValue();
		if(file.getModifiedDate()!=null)
			fileItem.modifiedDate=file.getModifiedDate().getValue();
		if(file.getLastViewedByMeDate()!=null)
			fileItem.lastView=file.getLastViewedByMeDate().getValue();		

		return fileItem;
	}	
	
	/**
	 * Download a file's content.
	 *
	 * @param service Drive API service instance.
	 * @param file Drive File instance.
	 * @return InputStream containing the file's content if successful,
	 *         {@code null} otherwise.
	 */	
	public static void downloadFile(final String fileUrl) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {		
				if (fileUrl != null && fileUrl.length() > 0) {
					try {
						HttpResponse resp =
								service.getRequestFactory().buildGetRequest(new GenericUrl(fileUrl))
								.execute();
						returnInputStream(resp.getContent());
					}catch (UserRecoverableAuthIOException e) {
						requestAuth(delegate.getContext(),e);												
					}catch (IOException e) {
						// An error occurred.
						e.printStackTrace();
						returnInputStream(null);
					}
				}else{
					returnInputStream(null);
				}
			}
		});
		t.start();
	}
	
	public static void downloadFileByID(final String fileId,final String localPath) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {		
				if (fileId != null && fileId.length() > 0) {
					try {
						FileItem fileItem=getFileItem(fileId);
						String fileUrl=fileItem.downloadUrl;
						if(fileUrl!=null && fileUrl.length()>0){
							HttpResponse resp =
								service.getRequestFactory().buildGetRequest(new GenericUrl(fileUrl))
								.execute();
							  DownloadFile downloadFile = new DownloadFile();
							  downloadFile.input=resp.getContent();
							  downloadFile.filename=fileItem.getName();
							  downloadFile.localPath=localPath.replace(downloadFile.filename, "");
							  downloadFile.execute(fileUrl);							
						}else{
							Log.e("DRIVE","Error obtaining downloadUrl of:"+fileId);
						}
					}catch (UserRecoverableAuthIOException e) {
						requestAuth(delegate.getContext(),e);						
					}catch (IOException e) {
						// An error occurred.
						e.printStackTrace();
					}
				}
			}
		});
		t.start();
	}	
	
	private static void returnInputStream(final InputStream is) {
		DrumCloud.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				delegate.preDownloadCallback(is);
			}
		});		
	}
	
	private static void returnCanceledLogin() {
		DrumCloud.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(delegate!=null && delegate.mProgressDialog!=null){
					delegate.mProgressDialog.dismiss();
				}
				Toast.makeText(DrumCloud.activity, "Login on Google Drive canceled", Toast.LENGTH_SHORT).show();
				delegate.dismiss();
			}
		});		
	}	

	private void uploadFile(final String localFilePath) {
		final GoogleDriveService GDS=this;
		AlertDialog.Builder  d = new AlertDialog.Builder(DrumCloud.activity).
				//setMessage("Select the category of the sample.").
				setTitle("Do you want to upload and share your sample on Google Drive?");
		d.setSingleChoiceItems(folderCategoryNames, 0 , new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				          String str = folderCategoryNames[which];
				          categorySelected=which;
				          Toast.makeText(DrumCloud.activity,
				                    "You have selected the \""+str+"\" sample category.",
				                     Toast.LENGTH_LONG).show();
			}
		});
		d.setNegativeButton("Cancel",new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//fa.finish();
			}
		});
		d.setPositiveButton("Save", new OnClickListener() {
			   @Override
			   public void onClick(DialogInterface dialog, int which) {
				   final int selected=categorySelected;
				   Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								String selectedFolderID;
								if(selected>=0 && selected<folderCategoryIds.length){
									selectedFolderID=folderCategoryIds[selected];
									Log.d("CATEGORY",folderCategoryNames[selected]);
								}
								else
									selectedFolderID=googleDriveMainFolderId;								
								// File's binary content					
								if(localFilePath!=null){
									Log.d("FILE TO UPLOAD", "PATH:"+localFilePath);
									selectedFile=new java.io.File(localFilePath);	
								}else{
									showToast("Error uploading sound");
									return;
								}					
								FileContent mediaContent = new FileContent("audio/wav", selectedFile);
								Log.d("FILE TO UPLOAD", selectedFile.getAbsolutePath());

						          // File's metadata.
						          File body = new File();
						          body.setTitle(selectedFile.getName());
						          body.setMimeType("audio/wav");

						          File file = service.files().insert(body, mediaContent).execute();
						          if (file != null) {
						            showToast("Sound uploaded: " + file.getTitle());
						            ChildReference newChild = new ChildReference();
						            newChild.setId(file.getId());
						            try {
						            	newChild=service.children().insert(selectedFolderID, newChild).execute();
						            	Log.i("FILE INSERTED",newChild.getChildLink());
						            } catch (IOException e) {
						            	Log.e("ERROR INSERTING IN FOLDER","An error occurred inserting file in main folder: " + e);
						            }			            		            
						          }else{
						        	  Log.e("UPLOAD ERROR", "Error while uploading:"+selectedFile.getName());
						          }
							} catch (UserRecoverableAuthIOException e) {
								requestAuth(delegate.getContext(),e);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
					t.start();
			   }				
			});		
		d.show();
	}

	public void showToast(final String toast) {
		DrumCloud.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(DrumCloud.activity, toast, Toast.LENGTH_SHORT).show();
				//fa.finish();
			}
		});
	}
	
	@Override
	public void onDestroy() 
	{   
		Log.d("ONDESTROY","Detroying GoogleDriveService");
	   super.onDestroy();  
	}	
	
}
