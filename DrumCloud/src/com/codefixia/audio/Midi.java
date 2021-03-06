package com.codefixia.audio;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.drumcloud.R;

import processing.core.PApplet;

import de.humatic.nmj.NMJConfig;
import de.humatic.nmj.NMJSystemListener;
import de.humatic.nmj.NetworkMidiInput;
import de.humatic.nmj.NetworkMidiListener;
import de.humatic.nmj.NetworkMidiOutput;
import de.humatic.nmj.NetworkMidiSystem;
import de.humatic.nmj.NetworkMidiClient;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Basic nmj sample application for Android.
 * This shows how to initialize the system, create input
 * and output ports, read and write MIDI etc.
 * Please refer to the main readMe file to learn about general concepts behind nmj.
 *
 * You will need to make sure that the nmj.jar file is accessible (and gets exported). 
 * In Eclipse see Project / Properties / Java Build Path / Libraries.
 * 
 * With nmj 0.86 the Android API level needs to be set to 12 or greater
 * to build things or you will get compiler errors due to missing USB classes.
 * Deployment is still possible on devices with lower API levels.
 */

public class Midi implements NetworkMidiListener, NMJSystemListener {

	private NetworkMidiInput midiIn;
	private NetworkMidiOutput midiOut;

	private byte[] myNote = new byte[]{(byte)0x90, (byte)0x24, 0};

	private MidiLogger midiLogger;

	private NetworkMidiSystem nmjs;
	private static DrumCloud drumCloud;
	private Spinner spinner;
	private ArrayAdapter<CharSequence> adapter;
	private String[] channelArray;
	
    public Midi(DrumCloud drumCloudRef) {
    	drumCloud=drumCloudRef;
        try{ 
        	nmjs = NetworkMidiSystem.get(drumCloud);
        } catch (Exception e) {
        	/*
        	 * This would happen if no network permissions were given.
        	 * See AndroidManifest.xml
        	 */
        	e.printStackTrace();
        	return;
        } 

        NMJConfig.addSystemListener(this); 
        
        int connectivity = NMJConfig.getConnectivity(drumCloud);
        
        if ((connectivity & NMJConfig.ADB) != 0 && NMJConfig.getNumChannels() < 4) {
        	/* 
        	 * Add a fourth channel to the default 3 if
        	 * USB debugging is enabled. The desktop version
        	 * of nmj can then be used to exchange MIDI over USB.
        	 */
        	NMJConfig.setNumChannels(4);
        	NMJConfig.setMode(3, NMJConfig.ADB);
        }
    } 
    
    public void showMidiTransportDialog(){
    	
   	 	final Dialog dialog = new Dialog(drumCloud);
   	 	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
   
     	dialog.setContentView(R.layout.midisetup);
     	//dialog.setTitle(R.string.about);
     	dialog.setCancelable(true);          	

        final Button midiTestButton = (Button) dialog.findViewById(R.id.midiTestButton);
        final Button midiSetupButton = (Button) dialog.findViewById(R.id.midiSetupButton);

        /* can't use "this" in the below event handler */
        final NetworkMidiListener ml = this;

        spinner = (Spinner) dialog.findViewById(R.id.Spinner01);
	    channelArray = new String[NMJConfig.getNumChannels()];
	    for (int i = 0; i < NMJConfig.getNumChannels(); i++) channelArray[i] = NMJConfig.getName(i);
	    ArrayAdapter<CharSequence> adapter = new ArrayAdapter(dialog.getContext(), android.R.layout.simple_spinner_item, channelArray);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    spinner.setOnItemSelectedListener(
		    new OnItemSelectedListener() {
		        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		        	/* using null instead of a specific client or listener removes all eventually attached clients and closes the port. */
		        	try{ midiIn.close(null); } catch (NullPointerException ne){}
		        	try{ midiOut.close(null); } catch (NullPointerException ne){}
		        	try{
		        		midiIn = nmjs.openInput(position, ml);
		        	} catch (Exception e){
		        		Log.e("MIDI IN","EX:"+e);
		        		/* channel is output only */
		        	}
		        	midiTestButton.setEnabled(true);
			        try{
		        		midiOut = nmjs.openOutput(position, ml);
		        	} catch (Exception e){
		        		Log.e("MIDI OUT","EX:"+e);
		        		/* channel is input only */
		        		midiTestButton.setEnabled(false);
		        	}
		        }

		        public void onNothingSelected(AdapterView<?> parent) {}
		    });

	    midiTestButton.setOnTouchListener(new View.OnTouchListener() {
           public boolean onTouch(View v, MotionEvent me) {
            	try{
               		if (me.getAction() == MotionEvent.ACTION_DOWN) {
               			myNote[2] = (byte)100;
                		midiOut.sendMidi(myNote);
                		midiTestButton.setPressed(true);
                		return true;
                    } else if (me.getAction() == MotionEvent.ACTION_UP) {
                    	myNote[2] = 0;
                		midiOut.sendMidi(myNote);
                		midiTestButton.setPressed(false);
                		return false;
                    }
                } catch (Exception ex){
                	Log.d("ONTOUCH","ex:"+ex.getStackTrace());
                }
            	return true;
           	}
         });	    
	    
	    midiSetupButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
            	final Intent si = new Intent(dialog.getContext(), de.humatic.nmj.NMJConfigDialog.class);
            	drumCloud.startActivity(si);
			}
		});	    	    
 
	    final TextView tv = (TextView) dialog.findViewById(R.id.TextView01);

        midiLogger = new MidiLogger(tv);
        
        dialog.show();
    	
    }
     
	@Override
	public void midiReceived(int channel, int ssrc, byte[] data, long timestamp) {

		/*
		 * As MIDI does not arrive on the GUI thread, it needs to be offloaded in
		 * order to be displayed. Android's Handler class is one way to do this.
		 */
		Message msg = Message.obtain();
    	Bundle b = new Bundle();
    	b.putByteArray("MIDI", data);
    	b.putInt("CH", channel);
    	msg.setData(b);

    	midiLogger.sendMessage(msg);

	}
	
	public void sendNote(final int channel, final int pitch, final int velocity,int durationMS) {
		
		if(Looper.myLooper() == Looper.getMainLooper()){
			new Timer().schedule(new TimerTask() {          
			    @Override
			    public void run() {
			    	sendNoteOn(channel,pitch,velocity);       
			    }
			}, 0);		
		}else{
	    	sendNoteOn(channel,pitch,velocity);
		}		
		
		new Timer().schedule(new TimerTask() {          
		    @Override
		    public void run() {
		    	sendNoteOff(channel,pitch,velocity);       
		    }
		}, durationMS);
	}
	
	
	public void sendNoteOn(int channel, int pitch, int velocity) {
		myNote[0] = (byte)144;
		myNote[1] = (byte)(36+(pitch*2));
		myNote[2] = (byte)(velocity);//(byte)(100);
		//PApplet.println("Sending midi note:"+(36+pitch*2)+" velocity:"+velocity+" midiOut:"+(midiOut!=null)+" mainthread:"+(Looper.myLooper() == Looper.getMainLooper()));
		try {
			if(midiOut!=null)
				midiOut.sendMidi(myNote);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendNoteOff(int channel, int pitch, int velocity) {
		myNote[0] = (byte)128;
		myNote[1] = (byte)(36+(pitch*2));		
		myNote[2] = 0;
		try {
			if(midiOut!=null)
				midiOut.sendMidi(myNote);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	private static class MidiLogger extends android.os.Handler {

    	private StringBuffer sb = new StringBuffer();
    	private TextView tv;

    	private MidiLogger(TextView tv) {
    		super();
    		this.tv = tv;
    	}

    	public void handleMessage(android.os.Message msg) { 
  
    		Bundle b = msg.getData();
    		sb.setLength(0); 
    		byte[] data = b.getByteArray("MIDI");
    		sb.append("MIDI received: ");
    		StringBuffer lastMessage=new StringBuffer();
    		for (int i = 0; i < data.length; i++){
    			lastMessage.append((data[i] & 0xFF)+" ");
    		}
    		sb.append(lastMessage);
    		sb.append("\n");
    		tv.setText(sb.toString());
    		String[] midiParts=lastMessage.toString().split("\\s+");
    		//Log.d("midiParts","total:"+midiParts.length+":"+midiParts[0]);
    		if(midiParts.length==3){
    			int messageType=Integer.parseInt(midiParts[0]);
    			int pitch=Integer.parseInt(midiParts[1]);
    			int velocity=Integer.parseInt(midiParts[2]);
    			if(messageType>=128 && messageType<=143){
    				receiveNoteOff(messageType-127,pitch,velocity);
    			}else if(messageType>=144 && messageType<=159){
    				receiveNoteOn(messageType-143,pitch,velocity);
    			}else{
    				receiveControllerChange(messageType-175,pitch,velocity);
    			}
    		}
    	}
		    	
		
		public void receiveNoteOn(int channel, int pitch, int velocity) {
		  // Receive a noteOn
		  /*DrumCloud.println();
		  DrumCloud.println("Note On:");
		  DrumCloud.println("--------");
		  DrumCloud.println("Channel:"+channel);
		  DrumCloud.println("Pitch:"+pitch);
		  DrumCloud.println("Velocity:"+velocity);*/
		  drumCloud.noteOn(channel, pitch, velocity);
		}		
		
		public void receiveNoteOff(int channel, int pitch, int velocity) {
		  // Receive a noteOff
		  /*DrumCloud.println();
		  DrumCloud.println("Note Off:");
		  DrumCloud.println("--------");
		  DrumCloud.println("Channel:"+channel);
		  DrumCloud.println("Pitch:"+pitch);
		  DrumCloud.println("Velocity:"+velocity);*/
		  drumCloud.noteOff(channel, pitch, velocity);
		}
		
		public void receiveControllerChange(int channel, int number, int value) {
		  // Receive a controllerChange
		  DrumCloud.println();
		  DrumCloud.println("Controller Change:");
		  DrumCloud.println("--------");
		  DrumCloud.println("Channel:"+channel);
		  DrumCloud.println("Number:"+number);
		  DrumCloud.println("Value:"+value);
		  drumCloud.controllerChange(channel, number, value);
		}    	
    }
  
	@Override
	public void systemChanged(int channel, int property, int value) {

		System.out.println(" System changed "+channel+" "+property+" "+value);

		if (property == NMJConfig.RTPA_EVENT && value == NMJConfig.RTPA_CH_DISCOVERED) {
			/*
			 * Given multicast works on your device then DNS might 
			 * uncover more RTP channels and call this for notification. 
			 * Newly found USB host channels will also be announced here.
			 * Time to update the spinner, which is not done in this sample. 
			 * New channels will be available on the next launch.
			 */
		    channelArray = new String[NMJConfig.getNumChannels()];
		    for (int i = 0; i < NMJConfig.getNumChannels(); i++){
		    	channelArray[i] = NMJConfig.getName(i);
		    	Log.d("NEW MIDI CHANNEL","->"+channelArray[i]);
		    }
		    adapter.notifyDataSetChanged();
		    
		}			

	}

	@Override
	public void systemError(int channel, int err, String description) {
		Log.e("MIDI SYSTEM ERROR","Desc:"+description+" on channel:"+channel);
	}
}
