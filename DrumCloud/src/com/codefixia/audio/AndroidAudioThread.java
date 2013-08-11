package com.codefixia.audio;

import java.util.ArrayList;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;


public class AndroidAudioThread extends Thread
{
  private int minSize;
  private AudioTrack track;
  private short[] bufferS;
  private float[] bufferF;
  private ArrayList<AudioGenerator> audioGens;
  private boolean running;

  public AndroidAudioThread(float samplingRate, int bufferLength)
  {
    audioGens = new ArrayList<AudioGenerator>();
    minSize =AudioTrack.getMinBufferSize( (int)samplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );        
    //println();
    // note that we set the buffer just to something small
    // not to the minSize
    // setting to minSize seems to cause glitches on the delivery of audio 
    // to the sound card (i.e. ireegular delivery rate)
    bufferS = new short[bufferLength];
    bufferF = new float[bufferLength];

    track = new AudioTrack( AudioManager.STREAM_MUSIC, (int)samplingRate, 
    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 
    minSize, AudioTrack.MODE_STREAM);
    track.setStereoVolume(2.0f, 2.0f);

    track.play();
  }     
  // overidden from Thread
  public void run() {
    running = true;
    while (running) {
      //System.out.println("AudioThread : ags  "+audioGens.size());
      for (int i=0;i<bufferS.length;i++) {
        // we add up using a 32bit int
        // to prevent clipping
        int val = 0;
        short totalPlaying=0;        
        if (audioGens.size() > 0) {	
          for (int j=0;j<audioGens.size(); j++) {
            AudioGenerator ag = (AudioGenerator)audioGens.get(j);
            val += ag.getSample();
            if(ag.isPlaying()){
            	totalPlaying++;
            }
          }
        }
        if(totalPlaying>0)
        	bufferS[i] = (short)(val*4/totalPlaying);
        else
        	bufferS[i] = (short)(val*4);
        /*
        if(totalPlaying>4)
        	bufferS[i] = (short)val;        
        else if(totalPlaying>2)
        	bufferS[i] = (short)(val*2);
        else if(totalPlaying>1)
        	bufferS[i] = (short)(val*2);
        else 
        	bufferS[i] = (short)(val*3);*/
      }
      // send it to the audio device!
      track.write( bufferS, 0, bufferS.length );
    }
  }

  public void addAudioGenerator(AudioGenerator ag) {
    audioGens.add(ag);
  }
  /*public int addAudioGenerator(AudioGenerator ag) {
   //System.out.println("ag added ");
   audioGens.add(ag);
   return audioGens.lastIndexOf(ag);
   }*/

  public boolean removeAudioGenerator(AudioGenerator ag) {
    //System.out.println("ag removed ");
    return audioGens.remove(ag);
  }    

  public void refreshAudio(int index, AudioGenerator ag)
  {
    audioGens.set(index, ag);
  } 

  public void clearAudioGenerator() {
    //System.out.println("ag added ");
    audioGens.clear();
  }
}