package com.codefixia.drumcloud;

/**
 * SelectFile
 * Android library which provides Dialogs for selectInput(), selectFolder() and selectOutput() methods.
 * https://github.com/pif/android-select-file/tree/dlg
 *
 * Copyright (C) 2013 Ostap Andrusiv http://andrusiv.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author      Ostap Andrusiv http://andrusiv.com
 * @modified    02/20/2013
 * @version     0.0.1 (1)
 */
//package selectsrc.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.codefixia.googledrive.GoogleDriveService;
import com.codefixia.googledrive.DownloadFile;
import com.codefixia.googledrive.GoogleDriveService.MyLocalBinder;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.ChildReference;

import processing.core.PApplet;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Dialog, which provides
 *  <ul>
 *    <li>select file,</li>
 *    <li>select folder,</li>
 *    <li>save file</li>
 * functionality.
 * <br>
 * <br>
 * Usage:
 * {@see FileLibrary}
 *  
 * @author ostap.andrusiv
 *
 */
public class SelectDialog extends Dialog {

  private static final String CURRENT_PATH = "currentPath";

  public static final String EX_PATH = "extraPath";
  public static final String EX_STYLE = "selectStyle";
  public static final String EX_PATH_RESULT = "pathResult";
  public static final String EX_CALLBACK = "selectCallback";
  public static final String EX_TITLE = "selectTitle";
  public static final String EX_FILTER_EXTENSION = "filterExtension";

  public ProgressDialog mProgressDialog;  

  private String currentPath = "";
  private ArrayAdapter<FileItem> simpleAdapter = null;

  private SelectMode selectMode = null;
  private final Intent intent;
  private PApplet parent;

  private ListView listView = null;
  private String filterExtension = null;
  
  private FileItem lastSelectedFileItem=null;
  public static boolean localMode=false;
  public static boolean showMainOptions=true;
  private String currentFolderId=GoogleDriveService.googleDriveMainFolderId;
  //private String parentFolderId=GoogleDriveActivity.googleDriveMainFolderId;
  private String callbackMethod=null;
  GoogleDriveService driveService;
  boolean isBound = false;

  private ServiceConnection myConnection = new ServiceConnection() {

	  public void onServiceConnected(ComponentName className,
			  IBinder service) {
		  MyLocalBinder binder = (MyLocalBinder) service;
		  driveService = binder.getService();
		  isBound = true;
	  }

	  public void onServiceDisconnected(ComponentName arg0) {
		  isBound = false;
	  }

  };
  
  public SelectDialog(PApplet context, Intent intent) {
    super(context);
    this.parent = context;
    this.intent = intent;
    callbackMethod = intent.getStringExtra(SelectDialog.EX_CALLBACK);
    Intent i=new Intent(getContext(),GoogleDriveService.class);
	this.getContext().bindService(i, myConnection, Context.BIND_AUTO_CREATE);
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(SelectConstants.generateMainActivityViews(getContext()));
    
    SelectDialog.showMainOptions=true;
	GoogleDriveService.delegate=this;
    listView = (ListView) findViewById(android.R.id.list);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView parent, View v, int position, long id) {
        onListItemClick((ListView) parent, v, position, id);
      }      
    });

    filterExtension=getIntent().getStringExtra(EX_FILTER_EXTENSION);
    //System.err.println("FILTER:"+filterExtension);
    
    setTitle(getIntent().getStringExtra(EX_TITLE));
    currentPath = getIntent().getStringExtra(EX_PATH);
    if (currentPath == null) {
      currentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    }
    if (savedInstanceState != null) {
      String savedPath = savedInstanceState.getString(CURRENT_PATH);
      if (savedPath != null) {
        currentPath = savedPath;
      }
    }

    selectMode = SelectMode.createSelectMode(getIntent().getIntExtra(EX_STYLE, SelectMode.SELECT_FILE), this);
    selectMode.updateUI();

    simpleAdapter = new ArrayAdapter<FileItem>(getContext(), android.R.layout.simple_list_item_2, android.R.id.text1) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        
        FileItem fItem = this.getItem(position);
        //view.setLayoutParams(new LayoutParams(android.widget.AbsListView.LayoutParams.FILL_PARENT, android.widget.AbsListView.LayoutParams.WRAP_CONTENT));
        view.setBackgroundColor(fItem.getType().getColor());
        TextView tv1 = (TextView) view.findViewById(android.R.id.text1);
        tv1.setTextColor(Color.WHITE);
        tv1.setTextSize(15);
        tv1.setShadowLayer((float) 0.01, 1, 1,Color.BLACK); 
        
    	TextView tv2 = (TextView) view.findViewById(android.R.id.text2);
    	tv2.setTextColor(Color.WHITE);
    	tv2.setTextSize(10);
    	tv2.setShadowLayer((float) 0.01, 1, 1,Color.BLACK);
        
        if(fItem.getName().equalsIgnoreCase("Up..")){
        	if(fItem.isOnline)
        		tv2.setText("Press to go back");
        	else
        		tv2.setText("Press to go back to\n"+fItem.getFullPath());
        }else if(fItem.getType()==FileType.Folder){
        	if(fItem.isOnline)
        		tv2.setText(fItem.getFormattedCreationDate()+"\n"+fItem.getFormattedLastViewDate());
        	else        	
        		tv2.setText("Path:\n"+fItem.getFullPath());
        }else if(fItem.getType()==FileType.File){
        	tv2.setText(fItem.getFormattedLastModifiedDate()+"\n"+fItem.getFormattedSize());
        }        
        
        return view;
      }
    };
    
	File f = new File(currentPath);
	updateCurrentList(f);    
	
    setListAdapter(simpleAdapter);
  }

  private void setListAdapter(ArrayAdapter<FileItem> simpleAdapter) {
    listView.setAdapter(simpleAdapter);
  }
  
  void getMainOptions() {
	    simpleAdapter.clear();
        File local = new File(currentPath);
	    FileItem localFiles=new FileItem("Local Files", FileType.Folder, local);
	    simpleAdapter.add(localFiles);
        File drive = new File("https://drive.google.com/");
	    FileItem driveFiles=new FileItem("Google Drive", FileType.Folder, drive);	    
	    simpleAdapter.add(driveFiles);
	    simpleAdapter.notifyDataSetChanged();
	  }  

  void updateCurrentList(File f) {
	  if(showMainOptions){
		  getMainOptions();
	  }
	  else{
		  if(localMode){
			  getLocalFolderList(f);
		  }
		  else{    	    	
			  requestDriveFolderList(currentFolderId);
		  }
	  }	  
  }
  
  void getLocalFolderList(File f){
	  List<FileItem> newData = getData(f);
	  currentPath = f.getAbsolutePath();
	  simpleAdapter.clear();
	  for (FileItem item : newData) {
		  simpleAdapter.add(item);
	  }
	  simpleAdapter.notifyDataSetChanged();	  
  }
  
  void requestDriveFolderList(String folderId) {
	  mProgressDialog = new ProgressDialog(DrumCloud.activity);
	  mProgressDialog.setMessage("Loading Drive folder:  "+(folderId==null?"MAIN":folderId));
	  mProgressDialog.setIndeterminate(true);
	  mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);	  
	  mProgressDialog.show();
	  
	  Intent i=new Intent(getContext(),GoogleDriveService.class);
	  i.putExtra("operation", "filesInFolder");
	  i.putExtra("folderId", folderId);
	  getContext().startService(i);	  
  }
  
  public void callbackDriveFolderList(List<FileItem> newData) {
	  simpleAdapter.clear();
	  Log.d("CALLBACK","Gathered "+newData.size()+" files");
	  sortFileItems(newData);
	  if(currentFolderId!=null && !currentFolderId.equalsIgnoreCase(GoogleDriveService.googleDriveMainFolderId)){
		  FileItem upFileItem=new FileItem(SelectConstants.fs_up_item, FileType.Up, lastSelectedFileItem.getFile());
		  upFileItem.fileId=lastSelectedFileItem.parentFolderId;
		  simpleAdapter.add(upFileItem);
	  }
	  for (FileItem item : newData) {
		  simpleAdapter.add(item);
	  }
	  simpleAdapter.notifyDataSetChanged();
	  selectMode.updateUI();
	  
	  mProgressDialog.hide();	  
	  
  	if(!this.isShowing()){
		this.show();
	}	  
  }  

  /**
   * 1. directories first
   * 2. dirs/files are sorted ignoring case
   */
  private static final Comparator<File> sorter = new Comparator<File>() {
    @Override
    public int compare(File lhs, File rhs) {
      // file or folder
      int lhsType = lhs.isDirectory() ? 0 : 1;
      int rhsType = rhs.isDirectory() ? 0 : 1;
      if (lhsType != rhsType) {
        return lhsType - rhsType;
      }
      return lhs.getName().compareToIgnoreCase(rhs.getName());
    }
  };
  private static final Comparator<FileItem> sorter2 = new Comparator<FileItem>() {
	    @Override
	    public int compare(FileItem lhs, FileItem rhs) {
	      // file or folder
	      int lhsType = lhs.getType()==FileType.Folder ? 0 : 1;
	      int rhsType = rhs.getType()==FileType.Folder ? 0 : 1;
	      if (lhsType != rhsType) {
	        return lhsType - rhsType;
	      }
	      return lhs.getName().compareToIgnoreCase(rhs.getName());
	    }
	  };  


  private void sortData(File[] files) {
    Arrays.sort(files, sorter);
  }
  
  private void sortFileItems(List<FileItem> newData) {
	Collections.sort(newData, sorter2);
  }

  private List<FileItem> getData(File folder) {
    if (!folder.isDirectory()) {
      return Collections.emptyList();
    }

    // selectMode specifies file-filtering rules
    File[] listFiles = folder.listFiles(selectMode);
    sortData(listFiles);

    List<FileItem> result = new ArrayList<FileItem>();

    // add "Up one level" item
    File parentFolder = folder.getParentFile();
    if (parentFolder != null) {
      result.add(new FileItem(SelectConstants.fs_up_item, FileType.Up, parentFolder));
    }

    for (int i = 0; i < listFiles.length; i++) {
      File f = listFiles[i];
      FileItem item = new FileItem(
        f.getName(),
        f.isDirectory() ? FileType.Folder : FileType.File,
        f);
      item.isOnline=false;
      if(!f.isDirectory()){
    	  if(item.getFile().exists()){
    		  item.size=item.getFile().length();
    		  item.modifiedDate=item.getFile().lastModified();
    	  }
        if(filterExtension!=null){
        	String[] extensions=filterExtension.split(";");
          for(int i1=0;i1<extensions.length;i1++){
        	  if(f.getName().toLowerCase().endsWith(extensions[i1].toLowerCase())){
        	  result.add(item);
          	}
          }
        }else{
          result.add(item);
        }
      }
      else
        result.add(item);
    }
    return result;
  }

  protected void onListItemClick(ListView l, View v, int position, long id) {
    FileItem item = simpleAdapter.getItem(position);
    lastSelectedFileItem=item;
    selectMode.onItemClicked(item.getFile());
  }

  /**
   * TODO: Probably, should be moved inside the {@link SelectLibrary#selectImpl(String, String, File, int)} method.
   * @param file
   */
  protected void onFileSelected(File file, Intent intent) {
	  Log.d("onFileSelected","Path:"+file.getAbsolutePath()+" b:"+file.getAbsolutePath().startsWith("https://drive.google.com"));
	  if(localMode){  
		  if (file != null) {
			  //String callbackMethod = intent.getStringExtra(SelectDialog.EX_CALLBACK);
			  selectCallback(file, callbackMethod, parent);
			  if(!file.getAbsolutePath().endsWith("json")){
				Intent i=new Intent(getContext(),GoogleDriveService.class);
			  	i.putExtra("filePath", file.getAbsolutePath());
			  	i.putExtra("operation", "uploadFile");
			  	getContext().startService(i);
			  }
		  }
	  }else{
		  if (file != null) {

			  // instantiate it within the onCreate method
			  mProgressDialog = new ProgressDialog(DrumCloud.activity);
			  mProgressDialog.setMessage("Downloading:  "+lastSelectedFileItem.getName());
			  mProgressDialog.setIndeterminate(false);
			  mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			  
			  if(lastSelectedFileItem.getType()==FileType.File){
				  if(!lastSelectedFileItem.getName().endsWith("json")){
					  mProgressDialog.setMax(1);
				  }else{
					  mProgressDialog.setMax(16);
				  }
				  GoogleDriveService.downloadFile(lastSelectedFileItem.downloadUrl);
			  }
			  else
			  {
				  currentFolderId=lastSelectedFileItem.fileId;
				  requestDriveFolderList(currentFolderId);
			  }
			  //InputStream is=GoogleDriveActivity.downloadFile(file);
			  /*if(is!=null){

				  }*/
		  }
	  }
  }
  
  public void preDownloadCallback(InputStream is){
	  DownloadFile downloadFile = new DownloadFile();
	  downloadFile.input=is;
	  downloadFile.delegate=this;
	  downloadFile.filename=lastSelectedFileItem.getName();
	  downloadFile.execute(lastSelectedFileItem.downloadUrl);	  
  }
  
  
  public void postDownloadCallback(String outputFilePath){
	  Log.i("LOADING LOCAL FILE",outputFilePath);
	  File output=new File(outputFilePath);
	  if(output.exists()){
		  if(!output.getName().endsWith("json")){
			selectCallback(output, callbackMethod, parent);
		  	Toast.makeText(DrumCloud.activity, "Sound downloaded: " + output.getName(), Toast.LENGTH_SHORT).show();
		  }else{ 
			selectCallback(output, callbackMethod, parent);  
			Toast.makeText(DrumCloud.activity, "Downloading SoundPack: " + output.getName(), Toast.LENGTH_SHORT).show();  
		  }
	  }else{
		  Toast.makeText(DrumCloud.activity, "Error downlading file:"+outputFilePath, Toast.LENGTH_SHORT).show();
	  }
  }

  static private void selectCallback(File selectedFile, String callbackMethod, Object callbackObject) {
    try {
      Class<?> callbackClass = callbackObject.getClass();
//      System.err.println(callbackClass + " clazz");
      Method selectMethod = callbackClass.getMethod(callbackMethod, new Class[] { File.class });
//      System.err.println(selectMethod + " method");
      selectMethod.invoke(callbackObject, new Object[] { selectedFile });

    } catch (IllegalAccessException iae) {
      System.err.println(callbackMethod + "() must be public");

    } catch (InvocationTargetException ite) {
      ite.printStackTrace();

    } catch (NoSuchMethodException nsme) {
      System.err.println(callbackMethod + "() could not be found");
    }
  }

//  @Override
//  public boolean onKeyDown(int keyCode, KeyEvent event) {
//    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
//      File parentFile = new File(currentPath).getParentFile();
//      if (parentFile == null) {
//        // finita la comedia: returning to the calling activity
//        return super.onKeyDown(keyCode, event);
//      } else {
//        updateCurrentList(parentFile);
//      }
//      return true;
//
//    } else {
//      return super.onKeyDown(keyCode, event);
//    }
//  }

  public String getCurrentPath() {
    return currentPath;
  }

  @Override
  public Bundle onSaveInstanceState() {
    Bundle outState = super.onSaveInstanceState();
    outState.putString(CURRENT_PATH, currentPath);
    return outState;
  }
  
  public Intent getIntent() {
    return intent;
  }
  
	@Override
	public void onAttachedToWindow() 
	{   
		Log.d("ONATTACHED","onAttachedToWindow SelectDialog");
	   super.onAttachedToWindow();  
	}  
  
	@Override
	public void onDetachedFromWindow() 
	{   
		Log.d("ONDETACHED","onDetachedFromWindow SelectDialog");
	   super.onDetachedFromWindow();  
	}	
	  
}
