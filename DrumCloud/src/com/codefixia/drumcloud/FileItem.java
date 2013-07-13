package com.codefixia.drumcloud;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;

import android.text.format.DateFormat;

import com.codefixia.googledrive.GoogleDriveActivity;
import com.google.api.client.util.DateTime;

public class FileItem {
    private final String name;
    private final FileType type;
    private final File file;
    public String downloadUrl;
    public String fileId;
    public Long creationDate;
    public Long modifiedDate;
    public Long lastView;
    public Long size;
    public boolean isOnline=false;
    public String extension;
    public String parentFolderId=GoogleDriveActivity.googleDriveMainFolderId;

    public FileItem(String name, FileType type, File file) {
      this.name = name;
      this.type = type;
      this.file = file;
    }

	public String getName() {
      return name;
    }
	
	public String dateTimeToString(Long timeStamp,String title){
		if(timeStamp==null)
			return "";
		else{		
			Date jDate=new Date(timeStamp);
			java.text.DateFormat df=DateFormat.getDateFormat(DrumCloud.activity);
			String date=df.format(jDate);
			df=DateFormat.getTimeFormat(DrumCloud.activity);
			String time=df.format(jDate);
			return title+": "+date+" - "+time;
		}
	}
	
	public String getFormattedCreationDate() {
	    return dateTimeToString(creationDate,"Created");
	}
	
	public String getFormattedLastViewDate() {
	    return dateTimeToString(lastView,"Visited");
	}
	
	public String getFormattedLastModifiedDate() {
	    return dateTimeToString(modifiedDate,"Last Mod.");
	}	
	
	public String getFormattedSize() {
	    if(size==null || size==0)
	    	return "";
	    else
	    	return "Size: "+NumberFormat.getNumberInstance().format(Math.round(size/1024.0))+" KB";
	}	

    public FileType getType() {
      return type;
    }

    public File getFile() {
      return file;
    }

    public String getFullPath() {
      return file.getAbsolutePath();
    }

    @Override
    public String toString() {
      return getName();
    }
  }