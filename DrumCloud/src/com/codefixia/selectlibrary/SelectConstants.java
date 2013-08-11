package com.codefixia.selectlibrary;/**
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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

/**
 * Removed all resource files. Moved all valuable onstants into this file.
 * This was done due to the way processing handles android builds.
 * 
 * @author ostap.andrusiv
 *
 */
public final class SelectConstants {

  /**
   * values copied from strings.xml
   */
  public static final String fs_file_type = "Folder or file";
  public static final String fs_cant_read = "[%1$s] can\'t be read.";
  public static final String fs_cant_write_parent_dir = "[%1$s] can\'t be written into selected folder.";
  public static final String fs_do_nothing = "";
  public static final String fs_unacceptable = "[%1$s] can\'t be selected.";
  public static final String fs_warning = "Oopsâ";
  public static final String fs_title_activity_test = "DrumCloud";
  public static final String fs_select_current_folder = "Select Current Folder";
  public static final String fs_enter_file_name = "Enter file name hereâ";
  public static final String fs_save_file = "Create";
  public static final String fs_save_file_overwrite = "Overwrite existing file [%1$s]?";  
  public static final String fs_up_item = "Up..";
  
  public static final int RID_WRAPPER_RL = 10;
  public static final int RID_CONTROLS_LL = 20;
  public static final int RID_FOLDER_BTN = 30;
  public static final int RID_SAVE_CTLS_LL = 40;
  public static final int RID_NAME_ET = 50;
  public static final int RID_SAVE_BTN = 60;
  public static final int RID_ITEMS_LV = android.R.id.list;

    
  static final int COLOR_FILE = 0xff22A300; 
  static final int COLOR_FOLDER = 0xffC18900;
  static final int COLOR_UP = 0xffB90016;
  static final int COLOR_FILE_PACK = 0xff380883;
  
  /**
   * <pre>
   * 10: RelativeLayout
   *    20: LinearLayout
   *       30: Button
   *       40: LinearLayout
   *          50: EditText
   *          60: Button
   *  android.R.id.list: ListView
   *  </pre>
   */
  public static View generateMainActivityViews(Context context) {
    RelativeLayout rl = new RelativeLayout(context);
    rl.setId(RID_WRAPPER_RL);
    rl.setLayoutParams(new LayoutParams(
        LayoutParams.MATCH_PARENT, 
        LayoutParams.MATCH_PARENT));
    
      LinearLayout ll = new LinearLayout(context);
      ll.setId(RID_CONTROLS_LL);
      RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(
          LayoutParams.MATCH_PARENT, 
          LayoutParams.WRAP_CONTENT);
      rllp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
      ll.setLayoutParams(rllp);
      ll.setOrientation(LinearLayout.VERTICAL);
      ll.setVisibility(View.GONE);
      
        Button scf = new Button(context);
        scf.setId(RID_FOLDER_BTN);
        scf.setLayoutParams(new LayoutParams(
            LayoutParams.MATCH_PARENT, 
            LayoutParams.WRAP_CONTENT));
        scf.setText(SelectConstants.fs_select_current_folder);
        scf.setVisibility(View.GONE);
        
        LinearLayout llsave = new LinearLayout(context);
        llsave.setId(RID_SAVE_CTLS_LL);
        RelativeLayout.LayoutParams rllsavep = new RelativeLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, 
            LayoutParams.WRAP_CONTENT);
        llsave.setLayoutParams(rllsavep);
        llsave.setVisibility(View.GONE);
        
          EditText et = new EditText(context);
          et.setId(RID_NAME_ET);
          et.setLayoutParams(new LinearLayout.LayoutParams(
              0,
              LayoutParams.WRAP_CONTENT,
              1f));
          et.setHint(SelectConstants.fs_enter_file_name);
          et.setSingleLine(true);
          
          Button bsf = new Button(context);
          bsf.setId(RID_SAVE_BTN);
          bsf.setLayoutParams(new LayoutParams(
              LayoutParams.WRAP_CONTENT,
              LayoutParams.WRAP_CONTENT));
          bsf.setText(SelectConstants.fs_save_file);
          
        llsave.addView(et);
        llsave.addView(bsf);
        
      ll.addView(scf);
      ll.addView(llsave);
      
    ListView lv = new ListView(context);
    lv.setId(RID_ITEMS_LV);
    RelativeLayout.LayoutParams rlvl = new RelativeLayout.LayoutParams(
        LayoutParams.MATCH_PARENT, 
        LayoutParams.MATCH_PARENT);
    rlvl.addRule(RelativeLayout.ABOVE, RID_CONTROLS_LL);
    lv.setLayoutParams(rlvl);
    
    rl.addView(ll);
    rl.addView(lv);
    
    return rl;
  }
}
