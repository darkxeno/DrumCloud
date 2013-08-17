package com.codefixia.audio;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.codefixia.drumcloud.DrumCloud;


/**
 * This class can play audio files and includes an fx chain 
 */
public class AudioPlayer implements Synth, AudioGenerator {
 
  private File file;	
  private FXChain fxChain;
  boolean isPlaying;
  private boolean isLooping;
  private boolean analysing;
  private FFT fft;
  private int fftInd;
  private float[] fftFrame;
  private float[] powerSpectrum;
  private static final boolean STEREO_TO_MONO_AVERAGE=false;

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

  public File getFile() {
	  return file;
  }
  
  public float getVolume(){
	  return masterVolume;
  }

  public float getDurationMS(){
	  return (float)audioData.length / (float)sampleRate * 0.001f;
  }

  public void resetAudioPlayer() {
    readHead = 0;
    startPos = 0;
    dReadHead = 1;
    isPlaying = false;
    isLooping = true;
    masterVolume = 1;
  } 

  public void selectAudioClip(int index) {
    audioData = audioClipArray[index];
  }
  public void loadAudioClips(String[] filenames) {
    audioClipArray = new short [filenames.length][];
    for (int i = 0; i< filenames.length;i++) {
      audioClipArray[i] = justLoadAudioFile(filenames[i]);
    }
  }

  @SuppressWarnings("unused")
public short[] loadWavFile(File f) {

	  short [] myAudioData = null;
	  int fileSampleRate = 0;
	  String filename=f.getName();    
	  try {

		  // how long is the file in bytes?
		  long byteCount = 0;
		  BufferedInputStream bis=null;

		  try {
			  byteCount = DrumCloud.X.getAssets().openFd(filename).getLength();
			  // check the format of the audio file first!
			  // only accept mono 16 bit wavs
			  InputStream is = DrumCloud.X.getAssets().open(filename); 
			  bis = new BufferedInputStream(is);
		  }
		  catch(FileNotFoundException e) {
			  DrumCloud.println("getAssets not working");
			  e.printStackTrace();

			  FileInputStream fIn = new FileInputStream(f);
			  if (fIn!=null) {
				  byteCount=fIn.available();
				  bis = new BufferedInputStream(fIn);        
			  }
			  else {
				  DrumCloud.println("FileInputStream not working");
			  }
		  }
		  DrumCloud.println("Opening file:"+filename);

		  if (bis!=null) {
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
			  System.out.println("#channels "+channels+" "+byteBuff[0]);
			  // skip 24 bytes to get sampleRate
			  // (32 bit int)
			  bis.read(byteBuff, 0, 4); // read 4 so now we are at 28
			  fileSampleRate = bytesToInt(byteBuff, 4);
			  System.out.println("Sample rate "+fileSampleRate);
			  // skip 34 bytes to get bits per sample
			  // (1 byte)
			  //bis.skip(6); // we were at 28...
			  bis.skip(4);
			  bis.read(byteBuff, 0, 2);// read 2 so we are at 34 now
			  short blockAlign = (short)byteBuff[0];    
			  System.out.println("block align "+blockAlign);  

		      bis.read(byteBuff, 0, 2);// read 2 so we are at 36 now
		      bitDepth = (short)byteBuff[0];
		      System.out.println("bit depth "+bitDepth);
		      // convert to word count...
		      bitDepth /= 8;
		      if (blockAlign/channels>bitDepth)bitDepth=blockAlign/channels;
		      // now start processing the raw data
		      //bis.skip(4); //skip Subchunk2ID now at 40
		      String subchunkId="";      
		      int dataByteOffset=40;
		      while (!subchunkId.equalsIgnoreCase("data") && bis.available()>=1) {
		        bis.read(byteBuff, 0, 1);
		        dataByteOffset++;
		        subchunkId="";
		        if((char)byteBuff[0]=='d' || (char)byteBuff[0]=='D'){
		          subchunkId+=(char)byteBuff[0];
		          if(bis.available()>=1){
		            bis.read(byteBuff, 0, 1);
		            dataByteOffset++;
		            if((char)byteBuff[0]=='a' || (char)byteBuff[0]=='A'){
		              subchunkId+=(char)byteBuff[0];
		              if(bis.available()>=1){
		                bis.read(byteBuff, 0, 1);
		                if((char)byteBuff[0]=='t' || (char)byteBuff[0]=='T'){
		                  subchunkId+=(char)byteBuff[0];
		                  if(bis.available()>=1){
		                    bis.read(byteBuff, 0, 1);
		                    if((char)byteBuff[0]=='a' || (char)byteBuff[0]=='A'){
		                      subchunkId+=(char)byteBuff[0];
		                    }    
		                  }  
		                }
		              }
		            }
		            //println("Search: "+subchunkId);            
		          }
		        }
		      }
		      if (!subchunkId.equalsIgnoreCase("data")) {
		        DrumCloud.println("Incompatible .wav file: "+filename);
		        return new short[1];
		      }
		      int sampleCount=0;      
		      bis.read(byteBuff, 0, 4); //reads Subchunk2Size now at 44
		      DrumCloud.println("Located \"data\" at byte:"+dataByteOffset);
		      sampleCount = bytesToInt(byteBuff, 4)/ (bitDepth * channels);      
		      //println("0:"+byteBuff[0]+" 1:"+byteBuff[1]+" 2:"+byteBuff[2]+" 3:"+byteBuff[3]);
		      System.out.println("total samples "+sampleCount+" resting bytes:"+(int) ((byteCount - 44) / (bitDepth * channels)));
		      myAudioData = new short[sampleCount];      
		      int skip = (channels -1) * bitDepth;
		      int sample = 0;
			  while (bis.available () >= (bitDepth+skip) && sample<sampleCount) {
				  if(skip>0 && STEREO_TO_MONO_AVERAGE){
					  bis.read(byteBuff, 0, bitDepth);
					  short left=(short) bytesToIntLimited(byteBuff, bitDepth);
					  bis.read(byteBuff, 0, bitDepth);
					  short right=(short) bytesToIntLimited(byteBuff, bitDepth);
					  myAudioData[sample] = (short)((left+right)/2.0f);
				  }else{
					  bis.skip(skip);
					  bis.read(byteBuff, 0, bitDepth);
					  myAudioData[sample] = (short) bytesToIntLimited(byteBuff, bitDepth);
				  }
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
	  } 

	  catch (FileNotFoundException e) {
		  e.printStackTrace();
	  }
	  catch (IOException e) {
		  e.printStackTrace();
	  }

	  if (fileSampleRate>0 && (float) fileSampleRate != this.sampleRate) {
		  //throw new InputMismatchException("In File: "+filename+" The sample rate of: "+fileSampleRate+ " does not match the default sample rate of: "+this.sampleRate);
		  Resampler resampler = new Resampler();
		  System.out.println("Resampling file" +filename+" from "+fileSampleRate+" Hz to "+this.sampleRate+ " Hz"); 
		  return resampler.reSample(myAudioData, (int)fileSampleRate, (int) (this.sampleRate));
	  } 
	  return myAudioData;
  }  


  public short[] convertSampleRate(short[] originalAudio, int targetRate, int originalRate) {
    if (targetRate==originalRate) {
      //throw new InputMismatchException("In File: "+filename+" The sample rate of: "+fileSampleRate+ " does not match the default sample rate of: "+this.sampleRate);
      return originalAudio;
    }
    else {        
      Resampler resampler = new Resampler();
      return resampler.reSample(originalAudio, originalRate, targetRate);
    }
  }

  public short[] loadAiffFile(File f) {

    short [] myAudioData = null;
    AiffFileReader aiffFileReader=new AiffFileReader();    
    int sample = 0;      
    String fileName="";

    try {
      fileName=f.getName();
      AudioFileFormat audioFileFormat=aiffFileReader.getAudioFileFormat(f);

      int bitDepth=audioFileFormat.getFormat().getFrameSize();
      byte[] byteBuff=new byte[bitDepth];
      DrumCloud.println("Aiff File framesize:"+bitDepth);
      AudioFormatJava af=audioFileFormat.getFormat();
      int fileSampleRate=(int)af.getSampleRate();
      int channels=af.getChannels();
      boolean isBigEndian=af.isBigEndian();
      DrumCloud.println("Aiff File frameRate:"+af.getFrameRate()+" sampleRate:"+fileSampleRate);
      DrumCloud.println("Aiff File is bigEndian?:"+isBigEndian+" sample bitsize:"+af.getSampleSizeInBits()+" channels:"+channels);
      int numBytesRead = 0;
      int skip = 0;//(channels -1) * bitDepth;

      AudioInputStream ais=aiffFileReader.getAudioInputStream(f);
      myAudioData=new short[(int)ais.getFrameLength()];
      while ( (numBytesRead = ais.read (byteBuff)) != -1) {

        if(!isBigEndian)
          myAudioData[sample] = (short) bytesToIntLimited(byteBuff, numBytesRead);
        else
          myAudioData[sample] = (short) bytesToIntBigEndian(byteBuff, numBytesRead);
          
        if (skip>0 && ais.available()>=bitDepth)
          ais.skip(skip);
        sample ++;
      }

      if (fileSampleRate>0 && fileSampleRate != this.sampleRate) {
        System.out.println("Resampling file" +fileName+" from "+fileSampleRate+" Hz to "+this.sampleRate+ " Hz");
        return convertSampleRate(myAudioData, (int) (this.sampleRate), fileSampleRate);
      }
    }
    catch(UnsupportedAudioFileException e) {
      e.printStackTrace();
      DrumCloud.println("UnsupportedAudioFileException reading:"+fileName+"\n"+e);
    }
    catch(IOException e) {
      e.printStackTrace();
      DrumCloud.println("IOException reading:"+fileName+"\n"+e);
    }    

    return myAudioData;
  }
  
  public void reloadAudioFile (String filename) {
	  this.setAudioData(this.justLoadAudioFile(filename));
	  this.resetAudioPlayer();
  }

  public short[] justLoadAudioFile (String filename) {

    File f = new File(filename);
    this.file=f;
    
    boolean isAiff=false;
    AiffFileReader aiffFileReader=new AiffFileReader();

    try {
      AudioFileFormat audioFileFormat=aiffFileReader.getAudioFileFormat(f);

      if (audioFileFormat.getType()==AudioFileFormat.Type.AIFC || audioFileFormat.getType()==AudioFileFormat.Type.AIFF) {
        DrumCloud.println("Aiff File detected type:"+audioFileFormat.getType());
        isAiff=true;
      }
    }
    catch(UnsupportedAudioFileException e) {
      e.printStackTrace();
      DrumCloud.println("UnsupportedAudioFileException:"+e);
    }
    catch(IOException e) {
      e.printStackTrace();
      DrumCloud.println("IOException:"+e);
    }

    if (isAiff)
      return loadAiffFile(f);
    else
      return loadWavFile(f);
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
  private int bytesToIntLimited(byte[] bytes, int wordSizeBytes) {
	    int val = 0;
	    //LIMIT TO 16BITS
	    if (wordSizeBytes>2)wordSizeBytes=2;
	    for (int i=wordSizeBytes-1; i>=0; i--) {
	      val <<= 8;
	      val |= (int)bytes[i] & 0xFF;
	    }
	    return val;
	  }  

  private int bytesToInt(byte[] bytes, int wordSizeBytes) {
	  int val = 0;
	  for (int i=wordSizeBytes-1; i>=0; i--) {
		  val <<= 8;
		  val |= (int)bytes[i] & 0xFF;
	  }
	  return val;
  }
  
  private int bytesToIntBigEndian(byte[] bytes, int wordSizeBytes) {
    int val = 0;
    //LIMIT TO 16BITS
    int start=0;
    //if(wordSizeBytes>2)start=wordSizeBytes-2;
    if(wordSizeBytes>2)wordSizeBytes=2;
    for (int i=start;i<wordSizeBytes; i++) {
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
  
  public float getSpeed() {
	  return dReadHead;
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
      //FIX FOR STARTING AND ENDING WRONG SAMPLE VALUES !=0
      if (readHead==0 || readHead==audioData.length-1) {
    	  return 0;
      }      

      // linear interpolation here
      // declaring these at the top...
      // easy to understand version...
      //      float x1, x2, y1, y2, x3, y3;
      x1 = DrumCloud.floor(readHead);
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

      return sample;
      //return (short)y3;
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