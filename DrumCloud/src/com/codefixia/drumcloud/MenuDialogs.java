package com.codefixia.drumcloud;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class MenuDialogs{

	private static float nextFrequency;
	
    public static float getNextFrequency() {
		return nextFrequency;
	}
    
	public static void setNextFrequency(float nextFrequency) {
		MenuDialogs.nextFrequency = nextFrequency;
	}
	
	public static void showAboutDialog(final DrumCloud drumCloud){
	 final Dialog dialog = new Dialog(drumCloud);
     dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
     dialog.setContentView(R.layout.about_dialog);
     //dialog.setTitle(R.string.about);
     dialog.setCancelable(true);
     //there are a lot of settings, for dialog, check them all out!

     //set up text
     TextView text = (TextView) dialog.findViewById(R.id.TextView01);
     text.setText(R.string.aboutDialogText);

     //set up image view
     ImageView img = (ImageView) dialog.findViewById(R.id.ImageView01);
     img.setImageResource(R.drawable.codefixia_logo);
     img.setOnClickListener(new OnClickListener() {
    	    public void onClick(View v) {
    	    	String url = "http://www.codefixia.com";
    	    	Intent i = new Intent(Intent.ACTION_VIEW);
    	    	i.setData(Uri.parse(url));
    	    	drumCloud.startActivity(i);
    	    }
    	});

     //set up button
     Button button = (Button) dialog.findViewById(R.id.Button01);
     button.setOnClickListener(new OnClickListener() {
     @Override
         public void onClick(View v) {
    	 	dialog.dismiss();
         }
     });
     //now that the dialog is set up, it's time to show it    
     dialog.show(); 
	}
	
	public static void showChangeFrequencyDialog(final DrumCloud drumCloud){
		
   	 	final Dialog dialog = new Dialog(drumCloud);
   	 	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
   
     	dialog.setContentView(R.layout.changefrequency);
     	dialog.setCancelable(true);          	

        final Button restartButton = (Button) dialog.findViewById(R.id.setButton);
        final Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);

        Spinner spinner = (Spinner) dialog.findViewById(R.id.Spinner01);
	    String[] frequencies = {"44100Hz","22050Hz","11025Hz"};
	    ArrayAdapter<CharSequence> adapter = new ArrayAdapter(dialog.getContext(), android.R.layout.simple_spinner_item, frequencies);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    if(DrumCloud.mainFrequency==44100){
	    	spinner.setSelection(0);
	    }else if(DrumCloud.mainFrequency==22050){
	    	spinner.setSelection(1);
	    }else if(DrumCloud.mainFrequency==11025){
	    	spinner.setSelection(2);
	    }	    
	    spinner.setOnItemSelectedListener(
		    new OnItemSelectedListener() {
		        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		        	MenuDialogs.setNextFrequency(Float.parseFloat(parent.getSelectedItem().toString().replace("Hz", "")));
		        }

		        public void onNothingSelected(AdapterView<?> parent) {}
		    });
	
	    
	    restartButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				drumCloud.changeMainFrequency(nextFrequency);				
			}
		});		    
	    
	    cancelButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});	    	    
         
        dialog.show();		
	
	}    
}
