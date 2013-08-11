package com.codefixia.audio;

import java.net.MalformedURLException;
import java.net.URL;

import com.codefixia.drumcloud.DrumCloud;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;

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
        AssetFileDescriptor afd = DrumCloud.X.getAssets().openFd(url);
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
      DrumCloud.println("StreamSource could not be initialised. Check url... "+url+ " and that you have added the permission INTERNET, RECORD_AUDIO and MODIFY_AUDIO_SETTINGS to the manifest,");
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