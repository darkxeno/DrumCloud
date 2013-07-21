package com.codefixia.drumcloud;//package com.codefixia.drumcloud;
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
import java.io.FileFilter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Data model, which encapsulates all selection logic.
 * Specifies, which files should be shown, defines, which actions 
 * should be taken due to specific UI clicks.
 * 
 * @author ostap.andrusiv
 *
 */
public abstract class SelectMode implements FileFilter {

  public static final int SELECT_FILE = 1;
  public static final int SELECT_FOLDER = 2;
  public static final int SAVE_FILE = 4;
  
  
  /**
   * Initializes custom UI elements for the selector.
   */
  abstract void updateUI();

  /**
   * Checks, if the proposed file can be selected.
   * @param pathname file to check.
   * @return ACCEPTABLE, if file is ok.
   * DONT_NOTIFY if no action should be performed,
   * or any other SelectConstants.fs_* resource, in case of problems.
   */
  abstract String isOk(File pathname);

  /**
   * this method get's called when ListItem from the file list is clicked. 
   * @param pathname
   */
  abstract void onItemClickedImpl(File pathname);

  SelectDialog ui;

  /**
   * Create an instance of {@link SelectMode} for the specific activity.
   * @param type
   * @param activity
   * @return selectMode
   */
  static SelectMode createSelectMode(int type, SelectDialog activity) {
    switch (type) {
    case SELECT_FILE:
      return new OPEN_FILE(activity);
    case SELECT_FOLDER:
      return new OPEN_FOLDER(activity);
    case SAVE_FILE:
      return new SAVE_FILE(activity);
    default:
      throw new IllegalArgumentException("Only OPEN_FILE, OPEN_FOLDER, SAVE_FILE allowed");
    }
  }

  private static final String ACCEPTABLE = "acpt";
  private static final String DONT_NOTIFY = "dont";

  /**
   * This method is called from the bound activity, when result should be selected.
   * @param f
   */
  public void selectResult(File f) {
    String isOkMessage = isOk(f);
    if (DONT_NOTIFY.equals(isOkMessage)) {
      // do nothing
    } else if (ACCEPTABLE.equals(isOkMessage)) {
      sendResult(f);
    } else {
      sayToUser(SelectConstants.fs_warning, isOkMessage, f.getName());
    }
  }

  public void onItemClicked(File pathname) {
	if(pathname.getAbsolutePath().contains("http")){
	  onItemClickedImpl(pathname);	
	}
	else if (!pathname.canRead()) {
      sayToUser(SelectConstants.fs_warning, SelectConstants.fs_cant_read, pathname.getName());
    } else {
      onItemClickedImpl(pathname);
    }
  }

  void sendResult(File f) {
    Intent result = new Intent();
    result.putExtra(SelectDialog.EX_CALLBACK, ui.getIntent().getExtras().getString(SelectDialog.EX_CALLBACK));

    result.putExtra(SelectDialog.EX_PATH_RESULT, f.getAbsolutePath());
//    activity.setResult(Activity.RESULT_OK, result);
    //Toast.makeText(activity, "Selected: " + f.getAbsolutePath(), Toast.LENGTH_LONG).show();
    ui.onFileSelected(f, result);
    ui.dismiss();
  }

  void sayToUser(String title, String message, Object... params) {
    AlertDialog dialog = new AlertDialog.Builder(ui.getContext())
      .setTitle(title)
      .setMessage(String.format(message, params))
      .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
        }
      }).create();
    dialog.show();
  }

  private static class OPEN_FILE extends SelectMode {
    public OPEN_FILE(SelectDialog activity) {
      this.ui = activity;
    }

    @Override
    public String isOk(File file) {
      return (file.canRead() && file.isFile() || file.getAbsolutePath().contains("http")) ? ACCEPTABLE : SelectConstants.fs_unacceptable;
    }

    @Override
    public boolean accept(File pathname) {
      // show all files
      return true;
    }

    @Override
    void onItemClickedImpl(File f) {
    	if(SelectDialog.showMainOptions){
    		if(f!=null && (f.getAbsolutePath().contains("drive.google.com")||
    				f.getAbsolutePath().contains("www.googleapis.com"))){
    			SelectDialog.localMode=false;
    		}else{
    			SelectDialog.localMode=true;
    		}
    		SelectDialog.showMainOptions=false;
    		ui.updateCurrentList(f);
    	}else    	
    		if (f.isDirectory()) {
    			ui.updateCurrentList(f);
    		} else {
    			selectResult(f);
    		}
    }

    @Override
    void updateUI() {
    }
  }

  private static class OPEN_FOLDER extends SelectMode {
    public OPEN_FOLDER(SelectDialog activity) {
      this.ui = activity;
    }

    @Override
    public String isOk(File file) {
      return file.isDirectory() ? ACCEPTABLE : SelectConstants.fs_unacceptable;
    }

    @Override
    public boolean accept(File pathname) {
      // accept folders only
      return pathname.isDirectory();
    }

    @Override
    void onItemClickedImpl(File f) {
  	  if(SelectDialog.showMainOptions){
		  /*if(f!=null && (f.getAbsolutePath().contains("drive.google.com")||
				  f.getAbsolutePath().contains("www.googleapis.com"))){
			  SelectDialog.localMode=false;
		  }else{
			  SelectDialog.localMode=true;
		  }*/
		  SelectDialog.showMainOptions=false;
		  ui.updateCurrentList(f);
	  }else{    	
		  ui.updateCurrentList(f);
	  }
      // result is selected with the help of "Select Current Folder" button
    }

    @Override
    void updateUI() {
      Button selectFolder = (Button) ui.findViewById(SelectConstants.RID_FOLDER_BTN);
      selectFolder.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          OPEN_FOLDER.this.selectResult(new File(ui.getCurrentPath()));
        }
      });

      View controls = ui.findViewById(SelectConstants.RID_CONTROLS_LL);
      controls.setVisibility(View.VISIBLE);
      View additionalControls = ui.findViewById(SelectConstants.RID_FOLDER_BTN);
      additionalControls.setVisibility(View.VISIBLE);
    }
  }

  private static class SAVE_FILE extends SelectMode {
    public SAVE_FILE(SelectDialog activity) {
      this.ui = activity;
    }

    @Override
    public boolean accept(File pathname) {
      // accept files and folders... everything
      return true;
    }

    @Override
    public String isOk(final File file) {
      if (!file.getParentFile().canWrite()) {
        return SelectConstants.fs_cant_write_parent_dir;
      }
      if (!file.exists()) {
        return ACCEPTABLE;
      } else {
        DialogInterface.OnClickListener yesNoListener = new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
              sendResult(file);
              break;
            case DialogInterface.BUTTON_NEGATIVE:
              break;
            }
          }
        };
        AlertDialog dialog = new AlertDialog.Builder(ui.getContext()).setTitle(SelectConstants.fs_warning)
            .setMessage(String.format(SelectConstants.fs_save_file_overwrite, file.getName()))
            .setPositiveButton(android.R.string.yes, yesNoListener)
            .setNegativeButton(android.R.string.no, yesNoListener).create();
        dialog.show();
        return DONT_NOTIFY;
      }
    }

    @Override
    void onItemClickedImpl(File f) {
      if (f.isDirectory()) {
        ui.updateCurrentList(f);
      } else {
        EditText editText = (EditText) ui.findViewById(SelectConstants.RID_NAME_ET);
        editText.setText(f.getName());
      }
      // result is returned with the help of "Save file" button
    }

    @Override
    void updateUI() {
      final EditText fileName = (EditText) ui.findViewById(SelectConstants.RID_NAME_ET);
      Button createFile = (Button) ui.findViewById(SelectConstants.RID_SAVE_BTN);
      createFile.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          File path = new File(ui.getCurrentPath());
          File newFile = new File(path, fileName.getText().toString());
          SAVE_FILE.this.selectResult(newFile);
        }
      });

      View controls = ui.findViewById(SelectConstants.RID_CONTROLS_LL);
      controls.setVisibility(View.VISIBLE);
      View additionalControls = ui.findViewById(SelectConstants.RID_SAVE_CTLS_LL);
      additionalControls.setVisibility(View.VISIBLE);
    }
  }
}
