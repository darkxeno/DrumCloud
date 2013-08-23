package com.codefixia.audio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.drumcloud.R;
import com.codefixia.googledrive.DownloadFile;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.event.meta.TimeSignature;

public class MidiFileIO {
	
	public final static String midiFolder=Environment.getExternalStorageDirectory().getPath()+"/drumcloud/midi/";
	
	public final static int basePitch=35;
	
	public static String fileToLoad="";	
	
	public static void showMidiPreLoadDialog(final DrumCloud drumCloud,final float BPM,final boolean[][] samplesPerBeat){
		
   	 	final Dialog dialog = new Dialog(drumCloud);
   	 	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
   
     	dialog.setContentView(R.layout.midipreload);
     	dialog.setCancelable(true);          	

        final Button midiLoadButton = (Button) dialog.findViewById(R.id.midiLoadButton);
        final Button midiCancelButton = (Button) dialog.findViewById(R.id.midiCancelButton);

        Spinner spinner = (Spinner) dialog.findViewById(R.id.Spinner01);
        File midiFolderFile=new File(midiFolder);
        File[] midis=midiFolderFile.listFiles(new FilenameFilter() {			
			@Override
			public boolean accept(File dir, String filename) {
				return filename.toLowerCase().endsWith(".midi");
			}
		});
	    String[] midiFiles = new String[midis.length];
	    for (int i = 0; i < midis.length; i++) midiFiles[i] = midis[i].getName();
	    ArrayAdapter<CharSequence> adapter = new ArrayAdapter(dialog.getContext(), android.R.layout.simple_spinner_item, midiFiles);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    spinner.setOnItemSelectedListener(
		    new OnItemSelectedListener() {
		        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		        	fileToLoad=parent.getSelectedItem().toString();
		        }

		        public void onNothingSelected(AdapterView<?> parent) {}
		    });
	
	    
	    midiLoadButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				if(!(fileToLoad.length()>0)){
					File inputMidiFile=new File(midiFolder+""+fileToLoad);
		    		MidiFileIO.load(inputMidiFile,BPM,samplesPerBeat);
				}
			}
		});		    
	    
	    midiCancelButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});	    	    
         
        dialog.show();		
	
	}
	
    public static void showMidiPostSaveDialog(final DrumCloud drumCloud,final File savedFile){
    	
   	 	final Dialog dialog = new Dialog(drumCloud);
   	 	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
   
     	dialog.setContentView(R.layout.midipostsave);
     	dialog.setCancelable(true);          	

        final Button midiShareButton = (Button) dialog.findViewById(R.id.midiShareFileButton);
        final Button midiOpenButton = (Button) dialog.findViewById(R.id.midiOpenFileButton);
        final Button midiCancelButton = (Button) dialog.findViewById(R.id.midiCancelButton);

        midiShareButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
				i.setType("text/plain");				
				ArrayList<Uri> uris = new ArrayList<Uri>();
				uris.add(Uri.fromFile(savedFile));
				Uri uri;
				File file;
				for(int p=0;p<drumCloud.getPlayerKick().length;p++){
					Log.d("MAIL FILE","PATH:"+drumCloud.getPlayerKick()[p].getFile().getAbsoluteFile());
					file=drumCloud.getPlayerKick()[p].getFile();
					if(!file.exists()){
						file=copyFileFromAssets(drumCloud,file);					
					}
					uri=Uri.fromFile(file);
					Log.d("MAIL FILE","URI:"+uri.toString());
					uris.add(uri);
					
					file=drumCloud.getPlayerBass()[p].getFile();
					if(!file.exists()){
						file=copyFileFromAssets(drumCloud,file);					
					}
					uri=Uri.fromFile(file);
					uris.add(uri);
					
					file=drumCloud.getPlayerSnare()[p].getFile();
					if(!file.exists()){
						file=copyFileFromAssets(drumCloud,file);					
					}						
					uri=Uri.fromFile(file);
					uris.add(uri);
					
					file=drumCloud.getPlayerHitHat()[p].getFile();
					if(!file.exists()){
						file=copyFileFromAssets(drumCloud,file);					
					}				
					uri=Uri.fromFile(file);
					uris.add(uri);
				}
				Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
				Account[] accounts = AccountManager.get(drumCloud).getAccounts();
				ArrayList<String> emails=new ArrayList<String>();
				for (Account account : accounts) {
				    if (emailPattern.matcher(account.name).matches()) {
				    	if(!emails.contains(account.name))
				    		emails.add(account.name);
				    }
				}				
				i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
				//i.putExtra(android.content.Intent.EXTRA_CC, new String[]{emailCC});
				i.putExtra(android.content.Intent.EXTRA_EMAIL, emails.toArray(new String[emails.size()]));
				i.putExtra(android.content.Intent.EXTRA_SUBJECT, drumCloud.getString(R.string.midi_mail_subject));
				String emailBody="Date:"+DateFormat.getDateFormat(drumCloud).format(new Date());
				emailBody+="\nTime:"+DateFormat.getTimeFormat(drumCloud).format(new Date());
				i.putExtra(android.content.Intent.EXTRA_TEXT, emailBody);
				drumCloud.startActivity(Intent.createChooser(i, "E-mail"));
			}
		});
	    
	    midiOpenButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
            	if(savedFile.exists() && savedFile.isFile()){
            		Intent i = new Intent();
                    i.setAction(android.content.Intent.ACTION_VIEW);
                    i.setDataAndType(Uri.fromFile(savedFile), "audio/*");
                    drumCloud.startActivity(i);            		
            	}
			}
		});
	    
	    midiCancelButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
            	dialog.dismiss();
			}
		});	    
 
	    final TextView tv = (TextView) dialog.findViewById(R.id.midiPostSaveText);
	    tv.setText(drumCloud.getString(R.string.file_save_text)+"\n\n"+savedFile.getAbsolutePath());
	       
        dialog.show();
    	
    }	

	public static void save(File output,float bpm,boolean[][] samplesPerBeat) {
		
		// 1. Create some MidiTracks
		MidiTrack tempoTrack = new MidiTrack();
		MidiTrack noteTrack = new MidiTrack();
		
		// 2. Add events to the tracks
		// 2a. Track 0 is typically the tempo map
		TimeSignature ts = new TimeSignature();
		ts.setTimeSignature(4, 8, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);
		
		Tempo t = new Tempo();
		t.setBpm(bpm);
		int gridMS=(int) Math.round(7500.0/bpm);//480;
		
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(t);
		
		// 3. Create a MidiFile with the tracks we created
		ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
		tracks.add(tempoTrack);		
		
		// 2b. Track 1 will have some notes in it
		for(int i = 0; i < samplesPerBeat.length; i++) {
			for(int j = 0; j < samplesPerBeat[i].length; j++) {
				if(samplesPerBeat[i][j]){
				int channel = 9, pitch = basePitch + i, velocity = 100;
				NoteOn on = new NoteOn(j*gridMS, channel, pitch, velocity);
				NoteOff off = new NoteOff(j*gridMS + 120, channel, pitch, 0);

				noteTrack.insertEvent(on);
				noteTrack.insertEvent(off);

					//There is also a utility function for notes that you should use instead of the above.				
					//noteTrack.insertNote(channel, pitch, velocity, j*gridMS, 120);
				}
			}
		}
		
		// It's best not to manually insert EndOfTrack events; MidiTrack will
		// call closeTrack() on itself before writing itself to a file
		

		tracks.add(noteTrack);
		
		MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);
		
		// 4. Write the MIDI data to a file
		//File output = new File("exampleout.mid");
		try {
			File midiFolder=new File(Environment.getExternalStorageDirectory().getPath()+"/drumcloud/midi/");
			if(midiFolder.exists() && midiFolder.isDirectory()){
				midi.writeToFile(output);
			}else{
				if(midiFolder.mkdirs())
					midi.writeToFile(output);
			}
		} catch(IOException e) {
			System.err.println(e);
		}
	}
	
	public static void load(File input,Float bpm,boolean[][] samplesPerBeat) {
		// 1. Open up a MIDI file
        MidiFile mf = null;
        
        try {
                mf = new MidiFile(input);
        } catch(IOException e) {
                System.err.println("Error parsing MIDI file:");
                e.printStackTrace();
                return;
        }
        
        
        MidiTrack tempoTrack = mf.getTracks().get(0);
        Iterator<MidiEvent> it = tempoTrack.getEvents().iterator();
        while(it.hasNext()) {
                MidiEvent E = it.next();                
                if(E.getClass().equals(Tempo.class)) {                        
                        Tempo tempo = (Tempo)E;
                        bpm=tempo.getBpm();
                }
        }        	
    	int gridMS=(int) Math.round(7500.0/bpm);
        		
        // 2. Do some editing to the file
        // 2a. Strip out anything but notes from track 1
        MidiTrack track = mf.getTracks().get(1);
        
     // It's a bad idea to modify a set while iterating, so we'll collect
        // the events first, then remove them afterwards
        it = track.getEvents().iterator();
        
		for(int i = 0; i < samplesPerBeat.length; i++) {
			for(int j = 0; j < samplesPerBeat[i].length; j++) {
				samplesPerBeat[i][j]=false;
			}
		}
        
        while(it.hasNext()) {
            MidiEvent E = it.next();                
            if(E.getClass().equals(NoteOn.class) && !E.getClass().equals(NoteOff.class)) {
            	NoteOn noteOn=(NoteOn)E;
            	int pos=(int) (noteOn.getTick()/gridMS);
            	int trackSel=noteOn.getNoteValue()-basePitch;
            	Log.d("MIDI LOAD","Activating ["+trackSel+"]["+pos+"]");
            	if(pos>=0 && trackSel>=0 && samplesPerBeat.length>trackSel && samplesPerBeat[trackSel].length>pos){
            		samplesPerBeat[trackSel][pos]=true;
            	}
            }
        }        
	}
	
	private static File copyFileFromAssets(Context context,File file) {
		AssetManager am = context.getAssets();
		try {
		    // Create new file to copy into.
		    File fileDst = new File(DownloadFile.getDownloadPath()+ file.getName());
		    File folder = new File(DownloadFile.getDownloadPath());
		    if(!folder.exists())
		    	folder.mkdirs();
		    fileDst.createNewFile();		    
		    //Log.d("COPY FILE","DST:"+fileDst.getAbsolutePath());

		    OutputStream out = new FileOutputStream(fileDst);
		    InputStream in = am.open(file.getName());  
	        copyFile(in, out);
		    
		    return fileDst;

		} catch (IOException e) {
		    e.printStackTrace();
		    return null;
		}
	}	
	
	private static void copyFile(InputStream in, OutputStream out) throws IOException { 
	    byte[] buffer = new byte[1024]; 
	    int read; 
	    while((read = in.read(buffer)) != -1){ 
	      out.write(buffer, 0, read); 
	    } 
	}

}
