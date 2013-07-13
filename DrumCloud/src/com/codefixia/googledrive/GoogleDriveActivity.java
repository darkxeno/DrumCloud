package com.codefixia.googledrive;
import android.app.Activity;
import android.app.AlertDialog;

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
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
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

public class GoogleDriveActivity extends Activity {
	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_AUTHORIZATION = 2;
	static final int CAPTURE_IMAGE = 3;
	static final int REQUEST_FOLDER_FILES = 4;

	private static Uri fileUri;
	private static Drive service=null;
	private GoogleAccountCredential credential;
	private java.io.File selectedFile=null;
	private String filePath;
	private String folderId;
	private static Boolean uploadMode=false;
	private String fileId;
	final static public String googleDriveMainFolderId="0B7AaL1Q9Q5fTZGY2eG12WDc5aFU";
	final static public String[] folderIds={"0B7AaL1Q9Q5fTb2RScXBFMGljTm8","0B7AaL1Q9Q5fTZGY2eG12WDc5aFU","0B7AaL1Q9Q5fTLWU1b1VkRXdvV00"};
	final static public String[] folderNames={"/DrumCloud","/DrumCloud/Samples","/DrumCloud/Samples/DrumSets"};
	final static public String[] folderCategoryIds={"0B7AaL1Q9Q5fTX1pjNnViTFMzVG8","0B7AaL1Q9Q5fTRHZHbldlbWFjREk","0B7AaL1Q9Q5fTMTZGaFFKM1JuVWc","0B7AaL1Q9Q5fTdVU1MTNuNkFJZHM","0B7AaL1Q9Q5fTYmJYUmR6MElvUDQ","0B7AaL1Q9Q5fTdVVOWEdQdFNNSEU","0B7AaL1Q9Q5fTbXBzZHIyaENQOFE","0B7AaL1Q9Q5fTYjlzeEg0LUJvMUE"};
	final static public String[] folderCategoryNames={"Others","Kick","Bass","Snare","Hats","Percurssion","Synth","FX"};
	private int categorySelected=-1;
	
	public static SelectDialog delegate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setTheme(android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
		
		filePath=getIntent().getStringExtra("filePath");
		folderId=getIntent().getStringExtra("folderId");
		uploadMode=filePath!=null;

		if(service==null){
			credential = GoogleAccountCredential.usingOAuth2(this.getApplicationContext(), DriveScopes.DRIVE);
			Intent accountIntent=credential.newChooseAccountIntent();
			if(!uploadMode){
				accountIntent.putExtra("folderId", folderId);
				startActivityForResult(accountIntent, REQUEST_FOLDER_FILES);				
			}else{
				accountIntent.putExtra("filePath", filePath);
				startActivityForResult(accountIntent, REQUEST_ACCOUNT_PICKER);
			}
		}else{
			if(uploadMode){
				uploadFile(filePath);
			}else{
				filesInFolder(folderId);
			}
		}
	}
	
	private Drive getDriveService(GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
		.build();
	}	
	
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
		case REQUEST_FOLDER_FILES:
			if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
				String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					credential.setSelectedAccountName(accountName);
					service = getDriveService(credential);
					Log.d("GOT SERVICE","FolderId:"+folderId);
					if(folderId!=null)
						filesInFolder(folderId);
					finish();
				}else{
					
				}
			}
			break;		
		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
				String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					credential.setSelectedAccountName(accountName);
					service = getDriveService(credential);
					uploadFile(filePath);
				}
			}
			break;
		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
				//saveFileToDrive();
			} else {
				startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
			}
			break;						
		case CAPTURE_IMAGE:
			if (resultCode == Activity.RESULT_OK) { 
				uploadFile(filePath);
			}
		}
	}

	private void startCameraIntent() {
		String mediaStorageDir = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES).getPath();
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		fileUri = Uri.fromFile(new java.io.File(mediaStorageDir + java.io.File.separator + "IMG_"
				+ timeStamp + ".jpg"));

		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		startActivityForResult(cameraIntent, CAPTURE_IMAGE);
	}
	
	public String getFolderById(String fileId){
		
		for(int i=0;i<folderIds.length;i++){
			if(folderIds[i].equalsIgnoreCase(fileId))
				return folderNames[i];
		}
		return "/UnknowFolder";
	}

	public void filesInFolder(final String folderId){
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				List<FileItem> childs=new ArrayList<FileItem>();
				if(service==null){
					Log.e("DRIVE SERVICE","ERROR NOT INITIALIZED");
					returnFiles(childs);
					return;
				}		
				//Children.List request;
				Files.List request;
				try {
					//request = service.children().list(folderId);
					request = service.files().list().setQ("'"+folderId+"' in parents");
					do {
						try {
							//ChildList children = request.execute();
							FileList children = request.execute();
							
							for (File child : children.getItems()) {
								FileType ft=child.getMimeType().contains("folder")?FileType.Folder:FileType.File;
								String url = null;
								//if(child.getParents().size()>0)
									//url=getFolderById(child.getParents().get(0).getId());
								if(url==null){
									url=child.getSelfLink();
									//(ft==FileType.File)?child.getDownloadUrl():child.getSelfLink();
								}
								fileId=child.getId();
								FileItem file=new FileItem(child.getTitle(),ft, new java.io.File(url) );
								file.downloadUrl=child.getDownloadUrl();
								file.parentFolderId=folderId;//getFolderById(child.getParents().get(0).getId());
								file.fileId=fileId;
	
								//FileItem file=getFileItem(fileId);
								if(file!=null)
									childs.add(file);
								System.out.println("File Id: " + child.toString());
							}
							request.setPageToken(children.getNextPageToken());
						} catch (IOException e) {
							System.out.println("An error occurred: " + e);
							request.setPageToken(null);
						}
					} while (request.getPageToken() != null &&
							request.getPageToken().length() > 0);			
					
				} catch (UserRecoverableAuthIOException e) {
					startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
				}catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				returnFiles(childs);

			}		
		});
		t.start();		

	}
	
	private void returnFiles(final List<FileItem> childs) {
		DrumCloud.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				delegate.callbackDriveFolderList(childs);
			}
		});		
		finish();
	}

	private FileItem getFileItem(String fileId) {
		try {
			Log.i("FILE INFO","File: " + fileId);
			File file = service.files().get(fileId).execute();
			FileItem item=new FileItem(file.getTitle(),(file.getFileSize()>0)?FileType.File:FileType.Folder,new java.io.File(file.getSelfLink()));
			Log.i("FILE INFO","Title: " + file.getTitle());
			Log.i("FILE INFO","Description: " + file.getDescription());
			Log.i("FILE INFO","MIME type: " + file.getMimeType());
			return item;
		}catch (UserRecoverableAuthIOException e) {
			if(e.getIntent()!=null)
				startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);			
		}catch (IOException e) {
			Log.e("FILE INFO ERROR","An error occured: " + e);
		}
		return null;
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
					//}catch (UserRecoverableAuthIOException e) {
			 	      //startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);						
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
	
	private static void returnInputStream(final InputStream is) {
		DrumCloud.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				delegate.preDownloadCallback(is);
			}
		});		
	}

	private void uploadFile(final String localFilePath) {
		AlertDialog.Builder  d = new AlertDialog.Builder(this).
				//setMessage("Select the category of the sample.").
				setTitle("Do you want to upload and share your sample on Google Drive?");
		d.setSingleChoiceItems(folderCategoryNames, 0 , new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				          String str = folderCategoryNames[which];
				          categorySelected=which;
				          Toast.makeText(GoogleDriveActivity.this,
				                    "You have selected the \""+str+"\" sample category.",
				                     Toast.LENGTH_LONG).show();
			}
		});
		d.setNegativeButton("Cancel",new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				finish();
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
								startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
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
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
				finish();
			}
		});
	}
}
