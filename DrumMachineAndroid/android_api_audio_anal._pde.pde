//The MIT License (MIT)

//Copyright (c) 2013 Mick Grierson, Matthew Yee-King, Marco Gillies

//Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
//The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// Modifications:  June 2013  Martin Bruner - Audio Analysis recovered by Constantino Fernandez Traba

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.net.MalformedURLException;
import java.util.*;
import java.net.URL;
//import android.content.res.Resources;
import android.app.Activity; 
import android.os.Bundle; 
import android.media.*;
import android.media.audiofx.Visualizer;
import android.content.res.AssetFileDescriptor;
import android.hardware.*;


public class Maxim {

  private float sampleRate = 44100;

  public final float[] mtof = {
    0, 8.661957, 9.177024, 9.722718, 10.3, 10.913383, 11.562325, 12.25, 12.978271, 13.75, 14.567617, 15.433853, 16.351599, 17.323914, 18.354048, 19.445436, 20.601723, 21.826765, 23.124651, 24.5, 25.956543, 27.5, 29.135235, 30.867706, 32.703197, 34.647827, 36.708096, 38.890873, 41.203445, 43.65353, 46.249302, 49., 51.913086, 55., 58.27047, 61.735413, 65.406395, 69.295654, 73.416191, 77.781746, 82.406891, 87.30706, 92.498604, 97.998856, 103.826172, 110., 116.540939, 123.470825, 130.81279, 138.591309, 146.832382, 155.563492, 164.813782, 174.61412, 184.997208, 195.997711, 207.652344, 220., 233.081879, 246.94165, 261.62558, 277.182617, 293.664764, 311.126984, 329.627563, 349.228241, 369.994415, 391.995422, 415.304688, 440., 466.163757, 493.883301, 523.25116, 554.365234, 587.329529, 622.253967, 659.255127, 698.456482, 739.988831, 783.990845, 830.609375, 880., 932.327515, 987.766602, 1046.502319, 1108.730469, 1174.659058, 1244.507935, 1318.510254, 1396.912964, 1479.977661, 1567.981689, 1661.21875, 1760., 1864.655029, 1975.533203, 2093.004639, 2217.460938, 2349.318115, 2489.015869, 2637.020508, 2793.825928, 2959.955322, 3135.963379, 3322.4375, 3520., 3729.31, 3951.066406, 4186.009277, 4434.921875, 4698.63623, 4978.031738, 5274.041016, 5587.651855, 5919.910645, 6271.926758, 6644.875, 7040., 7458.620117, 7902.132812, 8372.018555, 8869.84375, 9397.272461, 9956.063477, 10548.082031, 11175.303711, 11839.821289, 12543.853516, 13289.75
  };

  private AndroidAudioThread audioThread;
  //private PApplet processing;
  private void initAudioThread() {
    audioThread = new AndroidAudioThread(sampleRate, 256);
    audioThread.start();
  }  
    
  public Maxim () {
    initAudioThread();
  }

  public Maxim(float sampleRate){
    this.sampleRate = sampleRate;
    initAudioThread();
  }  
  
  public AudioPlayer createEmptyPlayer(){
        AudioPlayer ap = new AudioPlayer(sampleRate);
        AddAudioGenerator(ap);
        ap.resetAudioPlayer(); 
        return ap;
  }
      
  public void AddAudioGenerator(AudioPlayer ap){
      audioThread.addAudioGenerator(ap);
  }
  
  public void reLoadPlayer(AudioPlayer player, short[] audioData){
      player.setAudioData(audioData);
        
  }   
  /** 
   *  load the sent file into an audio player and return it. Use
   *  this if your audio file is not too long want precision control
   *  over looping and play head position
   * @param String filename - the file to load
   * @return AudioPlayer - an audio player which can play the file
   */
  public AudioPlayer loadFile(String filename) {
    // this will load the complete audio file into memory
    AudioPlayer ap = createEmptyPlayer();
    reLoadPlayer(ap, ap.justLoadAudioFile ( filename) );
    return ap;
  }

  /**
   * Create a wavetable player object with a wavetable of the sent
   * size. Small wavetables (<128) make for a 'nastier' sound!
   * 
   */
   
  public WavetableSynth createWavetableSynth(int size) {
    // this will load the complete audio file into memory
    WavetableSynth ap = new WavetableSynth(size, sampleRate);
    audioThread.addAudioGenerator(ap);
    // now we need to tell the audiothread
    // to ask the audioplayer for samples
    return ap;
  }
  /**
   * Create an AudioStreamPlayer which can stream audio from the
   * internet as well as local files.  Does not provide precise
   * control over looping and playhead like AudioPlayer does.  Use this for
   * longer audio files and audio from the internet.
   */
  public AudioStreamPlayer createAudioStreamPlayer(String url) {
    AudioStreamPlayer asp = new AudioStreamPlayer(url);
    return asp;
  }
}

/**
 * Represents an audio source is streamed as opposed to being completely loaded (as WavSource is)
 */
public class AudioStreamPlayer {
  /** a class from the android API*/
  private MediaPlayer mediaPlayer;
  /** a class from the android API*/
  private Visualizer viz; 
  private byte[] waveformBuffer;
  private byte[] fftBuffer;
  private byte[] powerSpectrum;

  /**
   * create a stream source from the sent url 
   */
  public AudioStreamPlayer(String url) {
    try {
      mediaPlayer = new MediaPlayer();
      //mp.setAuxEffectSendLevel(1);
      mediaPlayer.setLooping(true);

      // try to parse the URL... if that fails, we assume it
      // is a local file in the assets folder
      try {
        URL uRL = new URL(url);
        mediaPlayer.setDataSource(url);
      }
      catch (MalformedURLException eek) {
        // couldn't parse the url, assume its a local file
        AssetFileDescriptor afd = getAssets().openFd(url);
        //mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
        mediaPlayer.setDataSource(afd.getFileDescriptor());
        afd.close();
      }

      mediaPlayer.prepare();
      //mediaPlayer.start();
      //println("Created audio with id "+mediaPlayer.getAudioSessionId());
      viz = new Visualizer(mediaPlayer.getAudioSessionId());
      viz.setEnabled(true);
      waveformBuffer = new byte[viz.getCaptureSize()];
      fftBuffer = new byte[viz.getCaptureSize()/2];
      powerSpectrum = new byte[viz.getCaptureSize()/2];
    }
    catch (Exception e) {
      println("StreamSource could not be initialised. Check url... "+url+ " and that you have added the permission INTERNET, RECORD_AUDIO and MODIFY_AUDIO_SETTINGS to the manifest,");
      e.printStackTrace();
    }
  }

  public void play() {
    mediaPlayer.start();
  }

  public int getLengthMs() {
    return mediaPlayer.getDuration();
  }

  public void cue(float timeMs) {
    if (timeMs >= 0 && timeMs < getLengthMs()) {// ignore crazy values
      mediaPlayer.seekTo((int)timeMs);
    }
  }

  /**
   * Returns a recent snapshot of the power spectrum as 8 bit values
   */
  public byte[] getPowerSpectrum() {
    // calculate the spectrum
    viz.getFft(fftBuffer);
    short real, imag;
    for (int i=2;i<fftBuffer.length;i+=2) {
      real = (short) fftBuffer[i];
      imag = (short) fftBuffer[i+1];
      powerSpectrum[i/2] = (byte) ((real * real)  + (imag * imag));
    }
    return powerSpectrum;
  }

  /**
   * Returns a recent snapshot of the waveform being played 
   */
  public byte[] getWaveForm() {
    // retrieve the waveform
    viz.getWaveForm(waveformBuffer);
    return waveformBuffer;
  }
} 


/**
 * This class can play audio files and includes an fx chain 
 */
public class AudioPlayer implements Synth, AudioGenerator {
  private FXChain fxChain;
  private boolean isPlaying;
  private boolean isLooping;
  private boolean analysing;
  private FFT fft;
  private int fftInd;
  private float[] fftFrame;
  private float[] powerSpectrum;

  private int length;
  private short[] audioData;
  private float startPos;
  private float readHead;
  private float dReadHead;
  private float sampleRate;
  private float masterVolume;
  private short[][] audioClipArray;
  float x1, x2, y1, y2, x3, y3;

  public AudioPlayer(float sampleRate) {
    this.sampleRate = sampleRate;
    fxChain = new FXChain(sampleRate);
  }

  
 
public void resetAudioPlayer(){
  readHead = 0;
  startPos = 0;
  dReadHead = 1;
  isPlaying = false;
  isLooping = true;
  masterVolume = 1;
} 

public void selectAudioClip(int index){
  audioData = audioClipArray[index];
}
public void loadAudioClips(String[] filenames){
    audioClipArray = new short [filenames.length][];
     for(int i = 0; i< filenames.length;i++){
       audioClipArray[i] = justLoadAudioFile(filenames[i]);
     }
}
public short[] justLoadAudioFile (String filename) {

    short [] myAudioData = null;
    int fileSampleRate = 0;
    try {
      // how long is the file in bytes?
      long byteCount = getAssets().openFd(filename).getLength();
      
      // check the format of the audio file first!
      // only accept mono 16 bit wavs
      InputStream is = getAssets().open(filename); 
      BufferedInputStream bis = new BufferedInputStream(is);

      // chop!!

      int bitDepth;
      int channels;
      boolean isPCM;
      
      // allows us to read up to 4 bytes at a time 
      byte[] byteBuff = new byte[4];

      // skip 20 bytes to get file format
      // (1 byte)
      bis.skip(20);
      bis.read(byteBuff, 0, 2); // read 2 so we are at 22 now
      isPCM = ((short)byteBuff[0]) == 1 ? true:false; 
      //System.out.println("File isPCM "+isPCM);

      // skip 22 bytes to get # channels
      // (1 byte)
      bis.read(byteBuff, 0, 2);// read 2 so we are at 24 now
      channels = (short)byteBuff[0];
      //System.out.println("#channels "+channels+" "+byteBuff[0]);
      // skip 24 bytes to get sampleRate
      // (32 bit int)
      bis.read(byteBuff, 0, 4); // read 4 so now we are at 28
      
      fileSampleRate = bytesToInt(byteBuff, 4);
     /* if((float) fileSampleRate != this.sampleRate){
         throw new InputMismatchException("In File: "+filename+" The sample rate of: "+fileSampleRate+ " does not match the default sample rate of: "+this.sampleRate);
      }  */

      // skip 34 bytes to get bits per sample
      // (1 byte)
      bis.skip(6); // we were at 28...
      bis.read(byteBuff, 0, 2);// read 2 so we are at 36 now
      bitDepth = (short)byteBuff[0];
      //System.out.println("bit depth "+bitDepth);
      // convert to word count...
      bitDepth /= 8;
      // now start processing the raw data
      // data starts at byte 36
      int sampleCount = (int) ((byteCount - 36) / (bitDepth * channels));
      myAudioData = new short[sampleCount];
      int skip = (channels -1) * bitDepth;
      int sample = 0;
      // skip a few sample as it sounds like shit
      bis.skip(bitDepth * 4);
      while (bis.available () >= (bitDepth+skip)) {
        bis.read(byteBuff, 0, bitDepth);// read 2 so we are at 36 now
        //int val = bytesToInt(byteBuff, bitDepth);
        // resample to 16 bit by casting to a short
        myAudioData[sample] = (short) bytesToInt(byteBuff, bitDepth);
        bis.skip(skip);
        sample ++;
      }

      float secs = (float)sample / (float)sampleRate;
      //System.out.println("Read "+sample+" samples expected "+sampleCount+" time "+secs+" secs ");      
      bis.close();

      // unchop
      readHead = 0;
      startPos = 0;
      // default to 1 sample shift per tick
      dReadHead = 1;
      isPlaying = false;
      isLooping = true;
      masterVolume = 1;


    } 
    
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    if((float) fileSampleRate != this.sampleRate){
         //throw new InputMismatchException("In File: "+filename+" The sample rate of: "+fileSampleRate+ " does not match the default sample rate of: "+this.sampleRate);
      Resampler resampler = new Resampler();
      System.out.println("Resampling file" +filename+" from "+fileSampleRate+" Hz to "+this.sampleRate+ " Hz"); 
  return resampler.reSample(myAudioData,  (int)fileSampleRate,(int) (this.sampleRate)); 
    } 
  return myAudioData;
  }
  
  
  public void setAnalysing(boolean analysing_) {
    this.analysing = analysing_;
    if (analysing) {// initialise the fft
      fft = new FFT();
      fftInd = 0;
      fftFrame = new float[1024];
      powerSpectrum = new float[fftFrame.length/2];
    }
  }

  public float getAveragePower() {
    if (analysing) {
      // calc the average
      float sum = 0;
      for (int i=0;i<powerSpectrum.length;i++) {
        sum += powerSpectrum[i];
      }
      sum /= powerSpectrum.length;
      return sum;
    }
    else {
      System.out.println("call setAnalysing to enable power analysis");
      return 0;
    }
  }
  public float[] getPowerSpectrum() {
    if (analysing) {
      return powerSpectrum;
    }
    else {
      System.out.println("call setAnalysing to enable power analysis");
      return null;
    }
  }




  /** 
   *convert the sent byte array into an int. Assumes little endian byte ordering. 
   *@param bytes - the byte array containing the data
   *@param wordSizeBytes - the number of bytes to read from bytes array
   *@return int - the byte array as an int
   */
  private int bytesToInt(byte[] bytes, int wordSizeBytes) {
    int val = 0;
    for (int i=wordSizeBytes-1; i>=0; i--) {
      val <<= 8;
      val |= (int)bytes[i] & 0xFF;
    }
    return val;
  }

  /**
   * Test if this audioplayer is playing right now
   * @return true if it is playing, false otherwise
   */
  public boolean isPlaying() {
    return isPlaying;
  }

  /**
   * Set the loop mode for this audio player
   * @param looping 
   */
  public void setLooping(boolean looping) {
    isLooping = looping;
  }

  /**
   * Move the start pointer of the audio player to the sent time in ms
   * @param timeMs - the time in ms
   */
  public void cue(int timeMs) {
    //startPos = ((timeMs / 1000) * sampleRate) % audioData.length;
    //readHead = startPos;
    //println("AudioPlayer Cueing to "+timeMs);
    if (timeMs >= 0) {// ignore crazy values
      readHead = (((float)timeMs / 1000f) * sampleRate) % audioData.length;
      //println("Read head went to "+readHead);
    }
  }

  /**
   *  Set the playback speed,
   * @param speed - playback speed where 1 is normal speed, 2 is double speed
   */
  public void speed(float speed) {
    //println("setting speed to "+speed);
    dReadHead = speed;
  }

  /**
   * Set the master volume of the AudioPlayer
   */

  public void volume(float volume) {
    masterVolume = volume;
  }

  /**
   * Get the length of the audio file in samples
   * @return int - the  length of the audio file in samples
   */
  public int getLength() {
    return audioData.length;
  }
  /**
   * Get the length of the sound in ms, suitable for sending to 'cue'
   */
  public float getLengthMs() {
    return (audioData.length / sampleRate * 1000);
  }

  /**
   * Start playing the sound. 
   */
  public void play() {
    isPlaying = true;
  }

  /**
   * Stop playing the sound
   */
  public void stop() {
    isPlaying = false;
  }

  /**
   * implementation of the AudioGenerator interface
   */
  public short getSample() {
    if (!isPlaying) {
      return 0;
    }
    else {
      short sample;
      readHead += dReadHead;
      if (readHead > (audioData.length - 1)) {// got to the end
        //% (float)audioData.length;
        if (isLooping) {// back to the start for loop mode
          readHead = readHead % (float)audioData.length;
        }
        else {
          readHead = 0;
          isPlaying = false;
        }
      }

      // linear interpolation here
      // declaring these at the top...
      // easy to understand version...
      //      float x1, x2, y1, y2, x3, y3;
      x1 = floor(readHead);
      x2 = x1 + 1;
      y1 = audioData[(int)x1];
      y2 = audioData[(int) (x2 % audioData.length)];
      x3 = readHead;
      // calc 
      y3 =  y1 + ((x3 - x1) * (y2 - y1));
      y3 *= masterVolume;
      sample = fxChain.getSample((short) y3);
      if (analysing) {
        // accumulate samples for the fft
        fftFrame[fftInd] = (float)sample / 32768f;
        fftInd ++;
        if (fftInd == fftFrame.length - 1) {// got a frame
          powerSpectrum = fft.process(fftFrame, true);
          fftInd = 0;
        }
      }

      //return sample;
      return (short)y3;      
    }
  }

  public void setAudioData(short[] audioData) {
    this.audioData = audioData;
  }

  public short[] getAudioData() {
    return audioData;
  }

  public void setDReadHead(float dReadHead) {
    this.dReadHead = dReadHead;
  }

  ///
  //the synth interface
  // 

  public void ramp(float val, float timeMs) {
    fxChain.ramp(val, timeMs);
  } 



  public void setDelayTime(float delayMs) {
    fxChain.setDelayTime( delayMs);
  }

  public void setDelayFeedback(float fb) {
    fxChain.setDelayFeedback(fb);
  }

  public void setFilter(float cutoff, float resonance) {
    fxChain.setFilter( cutoff, resonance);
  }
}

/**
 * This class can play wavetables and includes an fx chain
 */
public class WavetableSynth extends AudioPlayer {

  private short[] sine;
  private short[] saw;
  private short[] wavetable;
  private float sampleRate;

  public WavetableSynth(int size, float sampleRate) {
    super(sampleRate);
    sine = new short[size];
    for (float i = 0; i < sine.length; i++) {
      float phase;
      phase = TWO_PI / size * i;
      sine[(int)i] = (short) (sin(phase) * 32768);
    }
    saw = new short[size];
    for (float i = 0; i<saw.length; i++) {
      saw[(int)i] = (short) (i / (float)saw.length *32768);
    }

    this.sampleRate = sampleRate;
    setAudioData(sine);
    setLooping(true);
  }

  public void setFrequency(float freq) {
    if (freq > 0) {
      //println("freq freq "+freq);
      setDReadHead((float)getAudioData().length / sampleRate * freq);
    }
  }

  public void loadWaveForm(float[] wavetable_) {
    if (wavetable == null || wavetable_.length != wavetable.length) {
      // only reallocate if there is a change in length
      wavetable = new short[wavetable_.length];
    }
    for (int i=0;i<wavetable.length;i++) {
      wavetable[i] = (short) (wavetable_[i] * 32768);
    }
    setAudioData(wavetable);
  }
}

public interface Synth {
  public void volume(float volume);
  public void ramp(float val, float timeMs);  
  public void setDelayTime(float delayMs);  
  public void setDelayFeedback(float fb);  
  public void setFilter(float cutoff, float resonance);
}

public class AndroidAudioThread extends Thread
{
  private int minSize;
  private AudioTrack track;
  private short[] bufferS;
  private float[] bufferF;
  private ArrayList audioGens;
  private boolean running;

  public AndroidAudioThread(float samplingRate, int bufferLength)
  {
    audioGens = new ArrayList();
    minSize =AudioTrack.getMinBufferSize( (int)samplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );        
    //println();
    // note that we set the buffer just to something small
    // not to the minSize
    // setting to minSize seems to cause glitches on the delivery of audio 
    // to the sound card (i.e. ireegular delivery rate)
    bufferS = new short[bufferLength];
    bufferF = new float[bufferLength];

    track = new AudioTrack( AudioManager.STREAM_MUSIC, (int)samplingRate, 
    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, 
    minSize, AudioTrack.MODE_STREAM);

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
        if (audioGens.size() > 0) {
          for (int j=0;j<audioGens.size(); j++) {
            AudioGenerator ag = (AudioGenerator)audioGens.get(j);
            val += ag.getSample();
          }
          val /= audioGens.size();
        }
        bufferS[i] = (short) val;
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
    
    public void refreshAudio(int index, AudioGenerator ag)
    {
      audioGens.set(index, ag);
    } 

    public void clearAudioGenerator() {
      //System.out.println("ag added ");
  audioGens.clear();
    }
}

/**
 * Implement this interface so the AudioThread can request samples from you
 */
public interface AudioGenerator {
  /** AudioThread calls this when it wants a sample */
  short getSample();
}


public class FXChain implements Synth {
  private float currentAmp;
  private float dAmp;
  private float targetAmp;
  private boolean goingUp;
  private Filter filter;

  private float[] dLine;   

  private float sampleRate;

  public FXChain(float sampleRate_) {
    sampleRate = sampleRate_;
    currentAmp = 1;
    dAmp = 0;
    // filter = new MickFilter(sampleRate);
    filter = new RLPF(sampleRate);

    //filter.setFilter(0.1, 0.1);
  }

  public void ramp(float val, float timeMs) {
    // calc the dAmp;
    // - change per ms
    targetAmp = val;
    dAmp = (targetAmp - currentAmp) / (timeMs / 1000 * sampleRate);
    if (targetAmp > currentAmp) {
      goingUp = true;
    }
    else {
      goingUp = false;
    }
  }


  public void setDelayTime(float delayMs) {
  }

  public void setDelayFeedback(float fb) {
  }

  public void volume(float volume) {
  }


  public short getSample(short input) {
    float in;
    in = (float) input / 32768;// -1 to 1

    in =  filter.applyFilter(in);
    if (goingUp && currentAmp < targetAmp) {
      currentAmp += dAmp;
    }
    else if (!goingUp && currentAmp > targetAmp) {
      currentAmp += dAmp;
    }  

    if (currentAmp > 1) {
      currentAmp = 1;
    }
    if (currentAmp < 0) {
      currentAmp = 0;
    }  
    in *= currentAmp;  
    return (short) (in * 32768);
  }

  public void setFilter(float f, float r) {
    filter.setFilter(f, r);
  }
}




/**
 * Use this class to retrieve data about the movement of the device
 */
public class Accelerometer implements SensorEventListener {
  private SensorManager sensorManager;
  private Sensor accelerometer;
  private float[] values;

  public Accelerometer() {
    sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    values = new float[3];
  }


  public float[] getValues() {
    return values;
  }

  public float getX() {
    return values[0];
  }

  public float getY() {
    return values[1];
  }

  public float getZ() {
    return values[2];
  }

  /**
   * SensorEventListener interace
   */
  public void onSensorChanged(SensorEvent event) {
    values = event.values;
    //float[] vals = event.values;
    //for (int i=0; i<vals.length;i++){
    //  println(" sensor! "+vals[i]);
    //}
  }

  /**
   * SensorEventListener interace
   */
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }
}

public interface Filter {
  public void setFilter(float f, float r);
  public float applyFilter(float in);
}

/** https://github.com/supercollider/supercollider/blob/master/server/plugins/FilterUGens.cpp */

public class RLPF implements Filter {
  float a0, b1, b2, y1, y2;
  float freq;
  float reson;
  float sampleRate;
  boolean changed;

  public RLPF(float sampleRate_) {
    this.sampleRate = sampleRate_;
    reset();
    this.setFilter(sampleRate / 4, 0);
  }
  private void reset() {
    a0 = 0.f;
    b1 = 0.f;
    b2 = 0.f;
    y1 = 0.f;
    y2 = 0.f;
    //setFilter(sampleRate/2, 0);
  }
  /** f is in the range 0-sampleRate/2 */
  public void setFilter(float f, float r) {
    // constrain 
    // limit to 0-1 
    f = constrain(f, 0, sampleRate/2);
    r = constrain(r, 0, 1);
    // remap to appropriate ranges
    f = map(f, 0, sampleRate/4, 30, sampleRate / 4);
    r = map(r, 0, 1, 0.005, 2);

    println("rlpf: f "+f+" r "+r);

    this.freq = f * TWO_PI / sampleRate;
    this.reson = r;
    changed = true;
  }

  public float applyFilter(float in) {
    float y0;
    if (changed) {
      float D = tan(freq * reson * 0.5f);
      float C = ((1.f-D)/(1.f+D));
      float cosf = cos(freq);
      b1 = (1.f + C) * cosf;
      b2 = -C;
      a0 = (1.f + C - b1) * .25f;
      changed = false;
    }
    y0 = a0 * in + b1 * y1 + b2 * y2;
    y2 = y1;
    y1 = y0;
    if (Float.isNaN(y0)) {
      reset();
    }
    return y0;
  }
}

/** https://github.com/micknoise/Maximilian/blob/master/maximilian.cpp */

class MickFilter implements Filter {

  private float f, res;
  private float cutoff, z, c, x, y, out;
  private float sampleRate;

  MickFilter(float sampleRate) {
    this.sampleRate = sampleRate;
  }

  void setFilter(float f, float r) {
    f = constrain(f, 0, 1);
    res = constrain(r, 0, 1);
    f = map(f, 0, 1, 25, sampleRate / 4);
    r = map(r, 0, 1, 1, 25);
    this.f = f;
    this.res = r;    

    //println("mickF: f "+f+" r "+r);
  }
  float applyFilter(float in) {
    return lores(in, f, res);
  }

  float lores(float input, float cutoff1, float resonance) {
    //cutoff=cutoff1*0.5;
    //if (cutoff<10) cutoff=10;
    //if (cutoff>(sampleRate*0.5)) cutoff=(sampleRate*0.5);
    //if (resonance<1.) resonance = 1.;

    //if (resonance>2.4) resonance = 2.4;
    z=cos(TWO_PI*cutoff/sampleRate);
    c=2-2*z;
    float r=(sqrt(2.0)*sqrt(-pow((z-1.0), 3.0))+resonance*(z-1))/(resonance*(z-1));
    x=x+(input-y)*c;
    y=y+x;
    x=x*r;
    out=y;
    return out;
  }
}
/*
 * Copyright (C) 2011 Jacquet Wong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Window functions generator
 *
 * @author Jacquet Wong
 *
 */
public class WindowFunction {

        public  final int RECTANGULAR = 0;
        public  final int BARTLETT = 1;
        public  final int HANNING = 2;
        public  final int HAMMING = 3;
        public  final int BLACKMAN = 4;

        int windowType = 0; // defaults to rectangular window

        public WindowFunction() {
        }

        public void setWindowType(int wt) {
                windowType = wt;
        }

        public void setWindowType(String w) {
                if (w.toUpperCase().equals("RECTANGULAR"))
                        windowType = RECTANGULAR;
                if (w.toUpperCase().equals("BARTLETT"))
                        windowType = BARTLETT;
                if (w.toUpperCase().equals("HANNING"))
                        windowType = HANNING;
                if (w.toUpperCase().equals("HAMMING"))
                        windowType = HAMMING;
                if (w.toUpperCase().equals("BLACKMAN"))
                        windowType = BLACKMAN;
        }

        public int getWindowType() {
                return windowType;
        }

        /**
         * Generate a window
         *
         * @param nSamples      size of the window
         * @return      window in array
         */
        public double[] generate(int nSamples) {
                // generate nSamples window function values
                // for index values 0 .. nSamples - 1
                int m = nSamples / 2;
                double r;
                double pi = Math.PI;
                double[] w = new double[nSamples];
                switch (windowType) {
                case BARTLETT: // Bartlett (triangular) window
                        for (int n = 0; n < nSamples; n++)
                                w[n] = 1.0f - Math.abs(n - m) / m;
                        break;
                case HANNING: // Hanning window
                        r = pi / (m + 1);
                        for (int n = -m; n < m; n++)
                                w[m + n] = 0.5f + 0.5f * Math.cos(n * r);
                        break;
                case HAMMING: // Hamming window
                        r = pi / m;
                        for (int n = -m; n < m; n++)
                                w[m + n] = 0.54f + 0.46f * Math.cos(n * r);
                        break;
                case BLACKMAN: // Blackman window
                        r = pi / m;
                        for (int n = -m; n < m; n++)
                                w[m + n] = 0.42f + 0.5f * Math.cos(n * r) + 0.08f
                                                * Math.cos(2 * n * r);
                        break;
                default: // Rectangular window function
                        for (int n = 0; n < nSamples; n++)
                                w[n] = 1.0f;
                }
                return w;
        }
}


/**
 * Resample signal data (base on bytes)
 *
 * @author jacquet
 *
 */
public class Resampler {

        public Resampler() {
        }

        /**
         * Do resampling. Currently the amplitude is stored by short such that maximum bitsPerSample is 16 (bytePerSample is 2)
         *
         * @param sourceData    The source data in bytes
         * @param bitsPerSample How many bits represents one sample (currently supports max. bitsPerSample=16)
         * @param sourceRate    Sample rate of the source data
         * @param targetRate    Sample rate of the target data
         * @return re-sampled data
         */
        //public byte[] reSample(byte[] sourceData, int bitsPerSample, int sourceRate, int targetRate) 
        public short[] reSample(short[] sourceData,  int sourceRate, int targetRate) 
        {

                // make the bytes to amplitudes first
                /*int bytePerSample = bitsPerSample / 8;
                int numSamples = sourceData.length / bytePerSample;
                short[] amplitudes = new short[numSamples];     // 16 bit, use a short to store
               
                int pointer = 0;
                for (int i = 0; i < numSamples; i++) {
                        short amplitude = 0;
                        for (int byteNumber = 0; byteNumber < bytePerSample; byteNumber++) {
                                // little endian
                                amplitude |= (short) ((sourceData[pointer++] & 0xFF) << (byteNumber * 8));
                        }
                        amplitudes[i] = amplitude;
                }*/
                // end make the amplitudes
               
                // do interpolation
                LinearInterpolation reSample=new LinearInterpolation();
                short[] targetSample = reSample.interpolate(sourceRate, targetRate, sourceData);
                int targetLength = targetSample.length;
                // end do interpolation
               
                // TODO: Remove the high frequency signals with a digital filter, leaving a signal containing only half-sample-rated frequency information, but still sampled at a rate of target sample rate. Usually FIR is used
               
                // end resample the amplitudes

                // convert the amplitude to bytes
               /* short[] output;
                if (bytePerSample==1){
                        output= new byte[targetLength];
                        for (int i=0; i<targetLength; i++){
                                bytes[i]=(byte)targetSample[i];
                        }
                }
                else{
                        // suppose bytePerSample==2
                        bytes= new byte[targetLength*2];
                        for (int i=0; i<targetSample.length; i++){                              
                                // little endian                        
                                bytes[i*2] = (byte)(targetSample[i] & 0xff);
                                bytes[i*2+1] = (byte)((targetSample[i] >> 8) & 0xff);                  
                        }
                }*/
                // end convert the amplitude to bytes
               
                return targetSample;
        }
}


/**
 * Construct new data points within the range of a discrete set of known data points by linear equation
 *
 * @author Jacquet Wong
 */
public class LinearInterpolation {

        public LinearInterpolation(){
               
        }
       
        /**
         * Do interpolation on the samples according to the original and destinated sample rates
         *
         * @param oldSampleRate sample rate of the original samples
         * @param newSampleRate sample rate of the interpolated samples
         * @param samples       original samples
         * @return interpolated samples
         */
        public short[] interpolate(int oldSampleRate, int newSampleRate, short[] samples) {

                if (oldSampleRate==newSampleRate){
                        return samples;
                }
               
                int newLength=(int)Math.round(((float)samples.length/oldSampleRate*newSampleRate));
                float lengthMultiplier=(float)newLength/samples.length;
        short[] interpolatedSamples = new short[newLength];

        // interpolate the value by the linear equation y=mx+c        
        for (int i = 0; i < newLength; i++){
           
                // get the nearest positions for the interpolated point
                float currentPosition = i / lengthMultiplier;
            int nearestLeftPosition = (int)currentPosition;
            int nearestRightPosition = nearestLeftPosition + 1;
            if (nearestRightPosition>=samples.length){
                nearestRightPosition=samples.length-1;
            }
           
            float slope=samples[nearestRightPosition]-samples[nearestLeftPosition];     // delta x is 1
            float positionFromLeft = currentPosition - nearestLeftPosition;
           
            interpolatedSamples[i] = (short)(slope*positionFromLeft+samples[nearestLeftPosition]);      // y=mx+c
        }
       
        return interpolatedSamples;
        }
}

/*
 * Copyright (C) 2011 Jacquet Wong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


//import com.sun.media.sound.FFT;

/**
 * FFT object, transform amplitudes to frequency intensities
 *
 * @author Jacquet Wong
 *
 */


public final class resampleFFT {

    private double[] w;
    private int fftFrameSize;
    private int sign;
    private int[] bitm_array;
    private int fftFrameSize2;

    // Sign = -1 is FFT, 1 is IFFT (inverse FFT)
    // Data = Interlaced double array to be transformed.
    // The order is: real (sin), complex (cos)
    // Framesize must be power of 2
    public resampleFFT(int fftFrameSize, int sign) {
        w = computeTwiddleFactors(fftFrameSize, sign);

        this.fftFrameSize = fftFrameSize;
        this.sign = sign;
        fftFrameSize2 = fftFrameSize << 1;

        // Pre-process Bit-Reversal
        bitm_array = new int[fftFrameSize2];
        for (int i = 2; i < fftFrameSize2; i += 2) {
            int j;
            int bitm;
            for (bitm = 2, j = 0; bitm < fftFrameSize2; bitm <<= 1) {
                if ((i & bitm) != 0)
                    j++;
                j <<= 1;
            }
            bitm_array[i] = j;
        }

    }

    public void transform(double[] data) {
        bitreversal(data);
        calc(fftFrameSize, data, sign, w);
    }

    private final  double[] computeTwiddleFactors(int fftFrameSize, //static
            int sign) {

        int imax = (int) (Math.log(fftFrameSize) / Math.log(2.));

        double[] warray = new double[(fftFrameSize - 1) * 4];
        int w_index = 0;

        for (int i = 0,  nstep = 2; i < imax; i++) {
            int jmax = nstep;
            nstep <<= 1;

            double wr = 1.0;
            double wi = 0.0;

            double arg = Math.PI / (jmax >> 1);
            double wfr = Math.cos(arg);
            double wfi = sign * Math.sin(arg);

            for (int j = 0; j < jmax; j += 2) {
                warray[w_index++] = wr;
                warray[w_index++] = wi;

                double tempr = wr;
                wr = tempr * wfr - wi * wfi;
                wi = tempr * wfi + wi * wfr;
            }
        }

        // PRECOMPUTATION of wwr1, wwi1 for factor 4 Decomposition (3 * complex
        // operators and 8 +/- complex operators)
        {
            w_index = 0;
            int w_index2 = warray.length >> 1;
            for (int i = 0,  nstep = 2; i < (imax - 1); i++) {
                int jmax = nstep;
                nstep *= 2;

                int ii = w_index + jmax;
                for (int j = 0; j < jmax; j += 2) {
                    double wr = warray[w_index++];
                    double wi = warray[w_index++];
                    double wr1 = warray[ii++];
                    double wi1 = warray[ii++];
                    warray[w_index2++] = wr * wr1 - wi * wi1;
                    warray[w_index2++] = wr * wi1 + wi * wr1;
                }
            }

        }

        return warray;
    }

    private final  void calc(int fftFrameSize, double[] data, int sign, //stat
            double[] w) {

        final int fftFrameSize2 = fftFrameSize << 1;

        int nstep = 2;

        if (nstep >= fftFrameSize2)
            return;
        int i = nstep - 2;
        if (sign == -1)
            calcF4F(fftFrameSize, data, i, nstep, w);
        else
            calcF4I(fftFrameSize, data, i, nstep, w);

    }

    private final  void calcF2E(int fftFrameSize, double[] data, int i, //st
            int nstep, double[] w) {
        int jmax = nstep;
        for (int n = 0; n < jmax; n += 2) {
            double wr = w[i++];
            double wi = w[i++];
            int m = n + jmax;
            double datam_r = data[m];
            double datam_i = data[m + 1];
            double datan_r = data[n];
            double datan_i = data[n + 1];
            double tempr = datam_r * wr - datam_i * wi;
            double tempi = datam_r * wi + datam_i * wr;
            data[m] = datan_r - tempr;
            data[m + 1] = datan_i - tempi;
            data[n] = datan_r + tempr;
            data[n + 1] = datan_i + tempi;
        }
        return;

    }

    // Perform Factor-4 Decomposition with 3 * complex operators and 8 +/-
    // complex operators
    private final  void calcF4F(int fftFrameSize, double[] data, int i, //st
            int nstep, double[] w) {
        final int fftFrameSize2 = fftFrameSize << 1; // 2*fftFrameSize;
        // Factor-4 Decomposition

        int w_len = w.length >> 1;
        while (nstep < fftFrameSize2) {

            if (nstep << 2 == fftFrameSize2) {
                // Goto Factor-4 Final Decomposition
                // calcF4E(data, i, nstep, -1, w);
                calcF4FE(fftFrameSize, data, i, nstep, w);
                return;
            }
            int jmax = nstep;
            int nnstep = nstep << 1;
            if (nnstep == fftFrameSize2) {
                // Factor-4 Decomposition not possible
                calcF2E(fftFrameSize, data, i, nstep, w);
                return;
            }
            nstep <<= 2;
            int ii = i + jmax;
            int iii = i + w_len;

            {
                i += 2;
                ii += 2;
                iii += 2;

                for (int n = 0; n < fftFrameSize2; n += nstep) {
                    int m = n + jmax;

                    double datam1_r = data[m];
                    double datam1_i = data[m + 1];
                    double datan1_r = data[n];
                    double datan1_i = data[n + 1];

                    n += nnstep;
                    m += nnstep;
                    double datam2_r = data[m];
                    double datam2_i = data[m + 1];
                    double datan2_r = data[n];
                    double datan2_i = data[n + 1];

                    double tempr = datam1_r;
                    double tempi = datam1_i;

                    datam1_r = datan1_r - tempr;
                    datam1_i = datan1_i - tempi;
                    datan1_r = datan1_r + tempr;
                    datan1_i = datan1_i + tempi;

                    double n2w1r = datan2_r;
                    double n2w1i = datan2_i;
                    double m2ww1r = datam2_r;
                    double m2ww1i = datam2_i;

                    tempr = m2ww1r - n2w1r;
                    tempi = m2ww1i - n2w1i;

                    datam2_r = datam1_r + tempi;
                    datam2_i = datam1_i - tempr;
                    datam1_r = datam1_r - tempi;
                    datam1_i = datam1_i + tempr;

                    tempr = n2w1r + m2ww1r;
                    tempi = n2w1i + m2ww1i;

                    datan2_r = datan1_r - tempr;
                    datan2_i = datan1_i - tempi;
                    datan1_r = datan1_r + tempr;
                    datan1_i = datan1_i + tempi;

                    data[m] = datam2_r;
                    data[m + 1] = datam2_i;
                    data[n] = datan2_r;
                    data[n + 1] = datan2_i;

                    n -= nnstep;
                    m -= nnstep;
                    data[m] = datam1_r;
                    data[m + 1] = datam1_i;
                    data[n] = datan1_r;
                    data[n + 1] = datan1_i;

                }
            }

            for (int j = 2; j < jmax; j += 2) {
                double wr = w[i++];
                double wi = w[i++];
                double wr1 = w[ii++];
                double wi1 = w[ii++];
                double wwr1 = w[iii++];
                double wwi1 = w[iii++];
                // double wwr1 = wr * wr1 - wi * wi1; // these numbers can be
                // precomputed!!!
                // double wwi1 = wr * wi1 + wi * wr1;

                for (int n = j; n < fftFrameSize2; n += nstep) {
                    int m = n + jmax;

                    double datam1_r = data[m];
                    double datam1_i = data[m + 1];
                    double datan1_r = data[n];
                    double datan1_i = data[n + 1];

                    n += nnstep;
                    m += nnstep;
                    double datam2_r = data[m];
                    double datam2_i = data[m + 1];
                    double datan2_r = data[n];
                    double datan2_i = data[n + 1];

                    double tempr = datam1_r * wr - datam1_i * wi;
                    double tempi = datam1_r * wi + datam1_i * wr;

                    datam1_r = datan1_r - tempr;
                    datam1_i = datan1_i - tempi;
                    datan1_r = datan1_r + tempr;
                    datan1_i = datan1_i + tempi;

                    double n2w1r = datan2_r * wr1 - datan2_i * wi1;
                    double n2w1i = datan2_r * wi1 + datan2_i * wr1;
                    double m2ww1r = datam2_r * wwr1 - datam2_i * wwi1;
                    double m2ww1i = datam2_r * wwi1 + datam2_i * wwr1;

                    tempr = m2ww1r - n2w1r;
                    tempi = m2ww1i - n2w1i;

                    datam2_r = datam1_r + tempi;
                    datam2_i = datam1_i - tempr;
                    datam1_r = datam1_r - tempi;
                    datam1_i = datam1_i + tempr;

                    tempr = n2w1r + m2ww1r;
                    tempi = n2w1i + m2ww1i;

                    datan2_r = datan1_r - tempr;
                    datan2_i = datan1_i - tempi;
                    datan1_r = datan1_r + tempr;
                    datan1_i = datan1_i + tempi;

                    data[m] = datam2_r;
                    data[m + 1] = datam2_i;
                    data[n] = datan2_r;
                    data[n + 1] = datan2_i;

                    n -= nnstep;
                    m -= nnstep;
                    data[m] = datam1_r;
                    data[m + 1] = datam1_i;
                    data[n] = datan1_r;
                    data[n + 1] = datan1_i;
                }
            }

            i += jmax << 1;

        }

        calcF2E(fftFrameSize, data, i, nstep, w);

    }

    // Perform Factor-4 Decomposition with 3 * complex operators and 8 +/-
    // complex operators
    private final  void calcF4I(int fftFrameSize, double[] data, int i, //st
            int nstep, double[] w) {
        final int fftFrameSize2 = fftFrameSize << 1; // 2*fftFrameSize;
        // Factor-4 Decomposition

        int w_len = w.length >> 1;
        while (nstep < fftFrameSize2) {

            if (nstep << 2 == fftFrameSize2) {
                // Goto Factor-4 Final Decomposition
                // calcF4E(data, i, nstep, 1, w);
                calcF4IE(fftFrameSize, data, i, nstep, w);
                return;
            }
            int jmax = nstep;
            int nnstep = nstep << 1;
            if (nnstep == fftFrameSize2) {
                // Factor-4 Decomposition not possible
                calcF2E(fftFrameSize, data, i, nstep, w);
                return;
            }
            nstep <<= 2;
            int ii = i + jmax;
            int iii = i + w_len;
            {
                i += 2;
                ii += 2;
                iii += 2;

                for (int n = 0; n < fftFrameSize2; n += nstep) {
                    int m = n + jmax;

                    double datam1_r = data[m];
                    double datam1_i = data[m + 1];
                    double datan1_r = data[n];
                    double datan1_i = data[n + 1];

                    n += nnstep;
                    m += nnstep;
                    double datam2_r = data[m];
                    double datam2_i = data[m + 1];
                    double datan2_r = data[n];
                    double datan2_i = data[n + 1];

                    double tempr = datam1_r;
                    double tempi = datam1_i;

                    datam1_r = datan1_r - tempr;
                    datam1_i = datan1_i - tempi;
                    datan1_r = datan1_r + tempr;
                    datan1_i = datan1_i + tempi;

                    double n2w1r = datan2_r;
                    double n2w1i = datan2_i;
                    double m2ww1r = datam2_r;
                    double m2ww1i = datam2_i;

                    tempr = n2w1r - m2ww1r;
                    tempi = n2w1i - m2ww1i;

                    datam2_r = datam1_r + tempi;
                    datam2_i = datam1_i - tempr;
                    datam1_r = datam1_r - tempi;
                    datam1_i = datam1_i + tempr;

                    tempr = n2w1r + m2ww1r;
                    tempi = n2w1i + m2ww1i;

                    datan2_r = datan1_r - tempr;
                    datan2_i = datan1_i - tempi;
                    datan1_r = datan1_r + tempr;
                    datan1_i = datan1_i + tempi;

                    data[m] = datam2_r;
                    data[m + 1] = datam2_i;
                    data[n] = datan2_r;
                    data[n + 1] = datan2_i;

                    n -= nnstep;
                    m -= nnstep;
                    data[m] = datam1_r;
                    data[m + 1] = datam1_i;
                    data[n] = datan1_r;
                    data[n + 1] = datan1_i;

                }

            }
            for (int j = 2; j < jmax; j += 2) {
                double wr = w[i++];
                double wi = w[i++];
                double wr1 = w[ii++];
                double wi1 = w[ii++];
                double wwr1 = w[iii++];
                double wwi1 = w[iii++];
                // double wwr1 = wr * wr1 - wi * wi1; // these numbers can be
                // precomputed!!!
                // double wwi1 = wr * wi1 + wi * wr1;

                for (int n = j; n < fftFrameSize2; n += nstep) {
                    int m = n + jmax;

                    double datam1_r = data[m];
                    double datam1_i = data[m + 1];
                    double datan1_r = data[n];
                    double datan1_i = data[n + 1];

                    n += nnstep;
                    m += nnstep;
                    double datam2_r = data[m];
                    double datam2_i = data[m + 1];
                    double datan2_r = data[n];
                    double datan2_i = data[n + 1];

                    double tempr = datam1_r * wr - datam1_i * wi;
                    double tempi = datam1_r * wi + datam1_i * wr;

                    datam1_r = datan1_r - tempr;
                    datam1_i = datan1_i - tempi;
                    datan1_r = datan1_r + tempr;
                    datan1_i = datan1_i + tempi;

                    double n2w1r = datan2_r * wr1 - datan2_i * wi1;
                    double n2w1i = datan2_r * wi1 + datan2_i * wr1;
                    double m2ww1r = datam2_r * wwr1 - datam2_i * wwi1;
                    double m2ww1i = datam2_r * wwi1 + datam2_i * wwr1;

                    tempr = n2w1r - m2ww1r;
                    tempi = n2w1i - m2ww1i;

                    datam2_r = datam1_r + tempi;
                    datam2_i = datam1_i - tempr;
                    datam1_r = datam1_r - tempi;
                    datam1_i = datam1_i + tempr;

                    tempr = n2w1r + m2ww1r;
                    tempi = n2w1i + m2ww1i;

                    datan2_r = datan1_r - tempr;
                    datan2_i = datan1_i - tempi;
                    datan1_r = datan1_r + tempr;
                    datan1_i = datan1_i + tempi;

                    data[m] = datam2_r;
                    data[m + 1] = datam2_i;
                    data[n] = datan2_r;
                    data[n + 1] = datan2_i;

                    n -= nnstep;
                    m -= nnstep;
                    data[m] = datam1_r;
                    data[m + 1] = datam1_i;
                    data[n] = datan1_r;
                    data[n + 1] = datan1_i;

                }
            }

            i += jmax << 1;

        }

        calcF2E(fftFrameSize, data, i, nstep, w);

    }

    // Perform Factor-4 Decomposition with 3 * complex operators and 8 +/-
    // complex operators
    private final  void calcF4FE(int fftFrameSize, double[] data, int i, //st
            int nstep, double[] w) {
        final int fftFrameSize2 = fftFrameSize << 1; // 2*fftFrameSize;
        // Factor-4 Decomposition

        int w_len = w.length >> 1;
        while (nstep < fftFrameSize2) {

            int jmax = nstep;
            int nnstep = nstep << 1;
            if (nnstep == fftFrameSize2) {
                // Factor-4 Decomposition not possible
                calcF2E(fftFrameSize, data, i, nstep, w);
                return;
            }
            nstep <<= 2;
            int ii = i + jmax;
            int iii = i + w_len;
            for (int n = 0; n < jmax; n += 2) {
                double wr = w[i++];
                double wi = w[i++];
                double wr1 = w[ii++];
                double wi1 = w[ii++];
                double wwr1 = w[iii++];
                double wwi1 = w[iii++];
                // double wwr1 = wr * wr1 - wi * wi1; // these numbers can be
                // precomputed!!!
                // double wwi1 = wr * wi1 + wi * wr1;

                int m = n + jmax;

                double datam1_r = data[m];
                double datam1_i = data[m + 1];
                double datan1_r = data[n];
                double datan1_i = data[n + 1];

                n += nnstep;
                m += nnstep;
                double datam2_r = data[m];
                double datam2_i = data[m + 1];
                double datan2_r = data[n];
                double datan2_i = data[n + 1];

                double tempr = datam1_r * wr - datam1_i * wi;
                double tempi = datam1_r * wi + datam1_i * wr;

                datam1_r = datan1_r - tempr;
                datam1_i = datan1_i - tempi;
                datan1_r = datan1_r + tempr;
                datan1_i = datan1_i + tempi;

                double n2w1r = datan2_r * wr1 - datan2_i * wi1;
                double n2w1i = datan2_r * wi1 + datan2_i * wr1;
                double m2ww1r = datam2_r * wwr1 - datam2_i * wwi1;
                double m2ww1i = datam2_r * wwi1 + datam2_i * wwr1;

                tempr = m2ww1r - n2w1r;
                tempi = m2ww1i - n2w1i;

                datam2_r = datam1_r + tempi;
                datam2_i = datam1_i - tempr;
                datam1_r = datam1_r - tempi;
                datam1_i = datam1_i + tempr;

                tempr = n2w1r + m2ww1r;
                tempi = n2w1i + m2ww1i;

                datan2_r = datan1_r - tempr;
                datan2_i = datan1_i - tempi;
                datan1_r = datan1_r + tempr;
                datan1_i = datan1_i + tempi;

                data[m] = datam2_r;
                data[m + 1] = datam2_i;
                data[n] = datan2_r;
                data[n + 1] = datan2_i;

                n -= nnstep;
                m -= nnstep;
                data[m] = datam1_r;
                data[m + 1] = datam1_i;
                data[n] = datan1_r;
                data[n + 1] = datan1_i;

            }

            i += jmax << 1;

        }

    }

    // Perform Factor-4 Decomposition with 3 * complex operators and 8 +/-
    // complex operators
    private final  void calcF4IE(int fftFrameSize, double[] data, int i, //st
            int nstep, double[] w) {
        final int fftFrameSize2 = fftFrameSize << 1; // 2*fftFrameSize;
        // Factor-4 Decomposition

        int w_len = w.length >> 1;
        while (nstep < fftFrameSize2) {

            int jmax = nstep;
            int nnstep = nstep << 1;
            if (nnstep == fftFrameSize2) {
                // Factor-4 Decomposition not possible
                calcF2E(fftFrameSize, data, i, nstep, w);
                return;
            }
            nstep <<= 2;
            int ii = i + jmax;
            int iii = i + w_len;
            for (int n = 0; n < jmax; n += 2) {
                double wr = w[i++];
                double wi = w[i++];
                double wr1 = w[ii++];
                double wi1 = w[ii++];
                double wwr1 = w[iii++];
                double wwi1 = w[iii++];
                // double wwr1 = wr * wr1 - wi * wi1; // these numbers can be
                // precomputed!!!
                // double wwi1 = wr * wi1 + wi * wr1;

                int m = n + jmax;

                double datam1_r = data[m];
                double datam1_i = data[m + 1];
                double datan1_r = data[n];
                double datan1_i = data[n + 1];

                n += nnstep;
                m += nnstep;
                double datam2_r = data[m];
                double datam2_i = data[m + 1];
                double datan2_r = data[n];
                double datan2_i = data[n + 1];

                double tempr = datam1_r * wr - datam1_i * wi;
                double tempi = datam1_r * wi + datam1_i * wr;

                datam1_r = datan1_r - tempr;
                datam1_i = datan1_i - tempi;
                datan1_r = datan1_r + tempr;
                datan1_i = datan1_i + tempi;

                double n2w1r = datan2_r * wr1 - datan2_i * wi1;
                double n2w1i = datan2_r * wi1 + datan2_i * wr1;
                double m2ww1r = datam2_r * wwr1 - datam2_i * wwi1;
                double m2ww1i = datam2_r * wwi1 + datam2_i * wwr1;

                tempr = n2w1r - m2ww1r;
                tempi = n2w1i - m2ww1i;

                datam2_r = datam1_r + tempi;
                datam2_i = datam1_i - tempr;
                datam1_r = datam1_r - tempi;
                datam1_i = datam1_i + tempr;

                tempr = n2w1r + m2ww1r;
                tempi = n2w1i + m2ww1i;

                datan2_r = datan1_r - tempr;
                datan2_i = datan1_i - tempi;
                datan1_r = datan1_r + tempr;
                datan1_i = datan1_i + tempi;

                data[m] = datam2_r;
                data[m + 1] = datam2_i;
                data[n] = datan2_r;
                data[n + 1] = datan2_i;

                n -= nnstep;
                m -= nnstep;
                data[m] = datam1_r;
                data[m + 1] = datam1_i;
                data[n] = datan1_r;
                data[n + 1] = datan1_i;

            }

            i += jmax << 1;

        }

    }

    private final void bitreversal(double[] data) {
        if (fftFrameSize < 4)
            return;

        int inverse = fftFrameSize2 - 2;
        for (int i = 0; i < fftFrameSize; i += 4) {
            int j = bitm_array[i];

            // Performing Bit-Reversal, even v.s. even, O(2N)
            if (i < j) {

                int n = i;
                int m = j;

                // COMPLEX: SWAP(data[n], data[m])
                // Real Part
                double tempr = data[n];
                data[n] = data[m];
                data[m] = tempr;
                // Imagery Part
                n++;
                m++;
                double tempi = data[n];
                data[n] = data[m];
                data[m] = tempi;

                n = inverse - i;
                m = inverse - j;

                // COMPLEX: SWAP(data[n], data[m])
                // Real Part
                tempr = data[n];
                data[n] = data[m];
                data[m] = tempr;
                // Imagery Part
                n++;
                m++;
                tempi = data[n];
                data[n] = data[m];
                data[m] = tempi;
            }

            // Performing Bit-Reversal, odd v.s. even, O(N)

            int m = j + fftFrameSize; // bitm_array[i+2];
            // COMPLEX: SWAP(data[n], data[m])
            // Real Part
            int n = i + 2;
            double tempr = data[n];
            data[n] = data[m];
            data[m] = tempr;
            // Imagery Part
            n++;
            m++;
            double tempi = data[n];
            data[n] = data[m];
            data[m] = tempi;
        }

    }
}
public class FastFourierTransform {

        /**
         * Get the frequency intensities
         *
         * @param amplitudes    amplitudes of the signal
         * @return      intensities of each frequency unit: mag[frequency_unit]=intensity
         */
        public double[] getMagnitudes(double[] amplitudes) {

                int sampleSize = amplitudes.length;
               
                // call the fft and transform the complex numbers
                resampleFFT fft = new resampleFFT(sampleSize/2,-1);
                fft.transform(amplitudes);
                // end call the fft and transform the complex numbers
               
                double[] complexNumbers=amplitudes;

                // even indexes (0,2,4,6,...) are real parts
                // odd indexes (1,3,5,7,...) are img parts
                int indexSize=sampleSize/2;
               
                // FFT produces a transformed pair of arrays where the first half of the values represent positive frequency components and the second half represents negative frequency components.
                // we omit the negative ones
                int positiveSize=indexSize/2;
               
                double[] mag = new double[positiveSize];
                for (int i = 0; i < indexSize; i+=2){
                        mag[i/2] = Math.sqrt(complexNumbers[i] * complexNumbers[i]+ complexNumbers[i+1] * complexNumbers[i+1]);
                }

                return mag;
        }
       
}/**
 * FFT performs a Fast Fourier Transform and forwards the complex data to any listeners. 
 * The complex data is a float of the form float[2][frameSize], with real and imaginary 
 * parts stored respectively.
 * 
 * @beads.category analysis
 */
public class FFT {

  /** The real part. */
  protected float[] fftReal;

  /** The imaginary part. */
  protected float[] fftImag;

  private float[] dataCopy = null;
  private float[][] features;
  private float[] powers;
  private int numFeatures;

  /**
   * Instantiates a new FFT.
   */
  public FFT() {
    features = new float[2][];
  }

  /* (non-Javadoc)
   * @see com.olliebown.beads.core.UGen#calculateBuffer()
   */
  public float[] process(float[] data, boolean direction) {
    if (powers == null) powers = new float[data.length/2];
    if (dataCopy==null || dataCopy.length!=data.length)
      dataCopy = new float[data.length];
    System.arraycopy(data, 0, dataCopy, 0, data.length);

    fft(dataCopy, dataCopy.length, direction);
    numFeatures = dataCopy.length;
    fftReal = calculateReal(dataCopy, dataCopy.length);
    fftImag = calculateImaginary(dataCopy, dataCopy.length);
    features[0] = fftReal;
    features[1] = fftImag;
    // now calc the powers
    return specToPowers(fftReal, fftImag, powers);
  }

  public float[] specToPowers(float[] real, float[] imag, float[] powers) {
    float re, im;
    double pow;
    for (int i=0;i<powers.length;i++) {
      //real = spectrum[i][j].re();
      //imag = spectrum[i][j].im();
      re = real[i];
      im = imag[i];
      powers[i] = (re*re + im * im);
      powers[i] = (float) Math.sqrt(powers[i]) / 10;
      // convert to dB
      pow = (double) powers[i];
      powers[i] = (float)(10 *  Math.log10(pow * pow)); // (-100 - 100)
      powers[i] = (powers[i] + 100) * 0.005f; // 0-1
    }
    return powers;
  }

  /**
   * The frequency corresponding to a specific bin 
   * 
   * @param samplingFrequency The Sampling Frequency of the AudioContext
   * @param blockSize The size of the block analysed
   * @param binNumber 
   */
  public  float binFrequency(float samplingFrequency, int blockSize, float binNumber)
  {    
    return binNumber*samplingFrequency/blockSize;
  }

  /**
   * Returns the average bin number corresponding to a particular frequency.
   * Note: This function returns a float. Take the Math.round() of the returned value to get an integral bin number. 
   * 
   * @param samplingFrequency The Sampling Frequency of the AudioContext
   * @param blockSize The size of the fft block
   * @param freq  The frequency
   */

  public  float binNumber(float samplingFrequency, int blockSize, float freq)
  {
    return blockSize*freq/samplingFrequency;
  }

  /** The nyquist frequency for this samplingFrequency 
   * 
   * @params samplingFrequency the sample
   */
  public  float nyquist(float samplingFrequency)
  {
    return samplingFrequency/2;
  }

  /*
     * All of the code below this line is taken from Holger Crysandt's MPEG7AudioEnc project.
   * See http://mpeg7audioenc.sourceforge.net/copyright.html for license and copyright.
   */

  /**
   * Gets the real part from the complex spectrum.
   * 
   * @param spectrum
   *            complex spectrum.
   * @param length 
   *       length of data to use.
   * 
   * @return real part of given length of complex spectrum.
   */
  protected  float[] calculateReal(float[] spectrum, int length) {
    float[] real = new float[length];
    real[0] = spectrum[0];
    real[real.length/2] = spectrum[1];
    for (int i=1, j=real.length-1; i<j; ++i, --j)
      real[j] = real[i] = spectrum[2*i];
    return real;
  }

  /**
   * Gets the imaginary part from the complex spectrum.
   * 
   * @param spectrum
   *            complex spectrum.
   * @param length 
   *       length of data to use.
   * 
   * @return imaginary part of given length of complex spectrum.
   */
  protected  float[] calculateImaginary(float[] spectrum, int length) {
    float[] imag = new float[length];
    for (int i=1, j=imag.length-1; i<j; ++i, --j)
      imag[i] = -(imag[j] = spectrum[2*i+1]);
    return imag;
  }

  /**
   * Perform FFT on data with given length, regular or inverse.
   * 
   * @param data the data
   * @param n the length
   * @param isign true for regular, false for inverse.
   */
  protected  void fft(float[] data, int n, boolean isign) {
    float c1 = 0.5f; 
    float c2, h1r, h1i, h2r, h2i;
    double wr, wi, wpr, wpi, wtemp;
    double theta = 3.141592653589793/(n>>1);
    if (isign) {
      c2 = -.5f;
      four1(data, n>>1, true);
    } 
    else {
      c2 = .5f;
      theta = -theta;
    }
    wtemp = Math.sin(.5*theta);
    wpr = -2.*wtemp*wtemp;
    wpi = Math.sin(theta);
    wr = 1. + wpr;
    wi = wpi;
    int np3 = n + 3;
    for (int i=2,imax = n >> 2, i1, i2, i3, i4; i <= imax; ++i) {
      /** @TODO this can be optimized */
      i4 = 1 + (i3 = np3 - (i2 = 1 + (i1 = i + i - 1)));
      --i4; 
      --i2; 
      --i3; 
      --i1; 
      h1i =  c1*(data[i2] - data[i4]);
      h2r = -c2*(data[i2] + data[i4]);
      h1r =  c1*(data[i1] + data[i3]);
      h2i =  c2*(data[i1] - data[i3]);
      data[i1] = (float) ( h1r + wr*h2r - wi*h2i);
      data[i2] = (float) ( h1i + wr*h2i + wi*h2r);
      data[i3] = (float) ( h1r - wr*h2r + wi*h2i);
      data[i4] = (float) (-h1i + wr*h2i + wi*h2r);
      wr = (wtemp=wr)*wpr - wi*wpi + wr;
      wi = wi*wpr + wtemp*wpi + wi;
    }
    if (isign) {
      float tmp = data[0]; 
      data[0] += data[1];
      data[1] = tmp - data[1];
    } 
    else {
      float tmp = data[0];
      data[0] = c1 * (tmp + data[1]);
      data[1] = c1 * (tmp - data[1]);
      four1(data, n>>1, false);
    }
  }

  /**
   * four1 algorithm.
   * 
   * @param data
   *            the data.
   * @param nn
   *            the nn.
   * @param isign
   *            regular or inverse.
   */
  private  void four1(float data[], int nn, boolean isign) {
    int n, mmax, istep;
    double wtemp, wr, wpr, wpi, wi, theta;
    float tempr, tempi;

    n = nn << 1;        
    for (int i = 1, j = 1; i < n; i += 2) {
      if (j > i) {
        // SWAP(data[j], data[i]);
        float swap = data[j-1];
        data[j-1] = data[i-1];
        data[i-1] = swap;
        // SWAP(data[j+1], data[i+1]);
        swap = data[j];
        data[j] = data[i]; 
        data[i] = swap;
      }      
      int m = n >> 1;
      while (m >= 2 && j > m) {
        j -= m;
        m >>= 1;
      }
      j += m;
    }
    mmax = 2;
    while (n > mmax) {
      istep = mmax << 1;
      theta = 6.28318530717959 / mmax;
      if (!isign)
        theta = -theta;
      wtemp = Math.sin(0.5 * theta);
      wpr = -2.0 * wtemp * wtemp;
      wpi = Math.sin(theta);
      wr = 1.0;
      wi = 0.0;
      for (int m = 1; m < mmax; m += 2) {
        for (int i = m; i <= n; i += istep) {
          int j = i + mmax;
          tempr = (float) (wr * data[j-1] - wi * data[j]);  
          tempi = (float) (wr * data[j]   + wi * data[j-1]);  
          data[j-1] = data[i-1] - tempr;
          data[j]   = data[i] - tempi;
          data[i-1] += tempr;
          data[i]   += tempi;
        }
        wr = (wtemp = wr) * wpr - wi * wpi + wr;
        wi = wi * wpr + wtemp * wpi + wi;
      }
      mmax = istep;
    }
  }
}



