package com.codefixia.drumcloud;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

import org.donations.DonationsActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.FloatList;
import processing.data.StringDict;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codefixia.audio.AudioPlayThread;
import com.codefixia.audio.AudioPlayer;
import com.codefixia.audio.Maxim;
import com.codefixia.audio.Midi;
import com.codefixia.audio.MidiFileIO;
import com.codefixia.googledrive.DownloadFile;
import com.codefixia.input.multitouch.DragEvent;
import com.codefixia.input.multitouch.FlickEvent;
import com.codefixia.input.multitouch.PinchEvent;
import com.codefixia.input.multitouch.RotateEvent;
import com.codefixia.input.multitouch.TapEvent;
import com.codefixia.input.multitouch.TouchProcessor;
import com.codefixia.selectlibrary.SelectLibrary;
import com.codefixia.ui.Clickable;
import com.codefixia.ui.ClickablePad;
import com.codefixia.ui.ExpandableButtons;
import com.codefixia.ui.HorizontalSlider;
import com.codefixia.ui.MenuButton;
import com.codefixia.ui.ToggleButton;
import com.codefixia.ui.ToggleButtonsBar;
import com.codefixia.ui.VerticalSlider;
import com.codefixia.utils.AndroidUtil;
import com.codefixia.utils.FontAdjuster;
import com.codefixia.utils.MainMode;
import com.codefixia.utils.PanelMode;
import com.github.espiandev.showcaseview.ShowcaseView;
import com.github.espiandev.showcaseview.ShowcaseView.OnShowcaseEventListener;


public class DrumCloud extends PApplet implements OnShowcaseEventListener {

	public static DrumCloud X;
	private static int soundsLoaded=0;


Midi midi;
private AudioPlayer auxAudioPlayer;
Maxim maxim;
float BPM=120.0f;
float beatMS=60000.0f/BPM;
float beatsPerTempo=4.0f;
float gridsByBeat=8.0f;
float gridMS=60000.0f/BPM/gridsByBeat;
float tempoMS=beatsPerTempo*beatMS;
long pausedMS=-1, totalPaused=0, audioMs=0;
float tempoOffset=0;
float beatOffset=0;
float lastPlayTime;
float firstKick=0;
boolean snapToGrid=true;
MainMode mode=MainMode.LIVE;
boolean paused=true;
boolean pressureEnabled=true;
Automator automator=new Automator(this);
Sequencer sequencer=new Sequencer(this);
final int totalSamples=16;
final int totalGrids=(int)gridsByBeat*(int)beatsPerTempo;
AudioPlayer[] playerKick=new AudioPlayer[4];
AudioPlayer[] playerBass=new AudioPlayer[4];
AudioPlayer[] playerSnare=new AudioPlayer[4];
AudioPlayer[] playerHitHat=new AudioPlayer[4];
boolean[][] samplesPerBeat=new boolean[totalSamples][totalGrids];
short pattern=0;
boolean[] playedGrids=new boolean[totalGrids];
int currentGrid=0, lastGrid=-1;
float lastMarkerOffset=0;
float maxDif=0;
int loadPlayerOfSoundType=-1;

int lastPlayerUsed=0;
boolean onlyOnePlayerMode=true;

boolean render3D=true;

final int normalMode=0;
final int loadMode=1;
final int deleteMode=2;
int mainMode=normalMode;

PImage backTopMachine, backBottomMachine;
public final static float mainFrequency=(AndroidUtil.numCores()>1)?22050:11025.0f;
float filterFrequency=mainFrequency/4.0f, filterResonance=0.5f, delayTime=0, delayFeedback=0, speed=1.0f, speed1=1.0f, volumeKick=1.0f, volumeBass=1.0f, volume=1.0f;

int candyPink=color(247, 104, 124);
public int redColor=0xffB90016;
public int orangeColor=0xffC18900;
public int blueColor=0xff5828A3;
int blueDarkColor=0xff380883;
int blueLightColor=0xff380883;
public int greenColor=0xff22A300;
int darkGreyColor=color(50);
int mediumGreyColor=color(127);
int lightGreyColor=color(200);
public int yellowColor=color(100, 100, 0);

PFont lcdFont;

FloatList savedCues= new FloatList(), notPlayedCues=new FloatList();
StringDict soundByCue= new StringDict();

final int LINES=0;
final int BARS=1;
final int CURVES=2;
int spectrumMode=LINES;


AudioPlayThread audioPlayThread=null;

float barOriginX, barOriginY, barWidth, barHeight, markerWidth;
int barColor, markerColor, separatorColor;

float pressBarWidth, pressBarHeight, pressBarHeightMargin;
ClickablePad[] kick=new ClickablePad[4], bass=new ClickablePad[4], snare=new ClickablePad[4], hithat=new ClickablePad[4];
Clickable panelModeButton, trackModeButton;
ExpandableButtons deleteButtons;
ToggleButton loadButton, playButton;
ToggleButtonsBar menuBar;

float valueX=0.0f, valueY=0.0f;
float buttonSize, buttonsOriginX, buttonsOriginY, buttonMarginX, buttonMarginY, padsContainerOriginY;
float sliderWidth, sliderHeight, sliderOriginX, sliderMinY, sliderMaxY, sliderMarginX;
VerticalSlider[] sliders=new VerticalSlider[6];
HorizontalSlider bpmSlider;
PImage sliderImage;

float maxLedWidth, ledHeight;
float originPowerSpecX, originPowerSpecY;

PanelMode panelMode=PanelMode.FILTER;
final int ALL=0;
final int KICK=1;
final int BASS=2;
final int SNARE=3;
final int HITHAT=4;
int filterTrackMode=ALL;

public final static boolean isAndroidDevice=true;

SelectLibrary files;

public static boolean isAndroidDevice(){
	return isAndroidDevice;
}



public int getCurrentGrid() {
	return currentGrid;
}

public void playSoundFile(File file){
	if(file.exists() && file.isFile()){
		Log.i("FILE PREVIEW","path: "+file.getAbsolutePath());
		if(auxAudioPlayer==null){
			auxAudioPlayer=maxim.loadFile(file.getAbsolutePath());
			auxAudioPlayer.volume(1.0f);
			auxAudioPlayer.setFilter(11025.0f, 0.5f);				
		}else{
			auxAudioPlayer.stop();
			maxim.reloadFile(auxAudioPlayer, file.getAbsolutePath());
		}
		auxAudioPlayer.setLooping(false);	
		auxAudioPlayer.cue(0);
		auxAudioPlayer.play();
	}
}

public void setupAndroid() {
  files = new SelectLibrary(this);
  files.filterExtension=".wav;.json;.aiff;.aif"; 
	DrumCloud.X.runOnUiThread(new Runnable() {
		@Override
		public void run() {
			  SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(X);
			  if(!prefs.getBoolean("firstTime", false)) {
			      // run your one time code
			      SharedPreferences.Editor editor = prefs.edit();
			      editor.putBoolean("firstTime", true);
			      editor.commit();
			      startHelpShowCase();
			  }
		}
	});      
}


public void setup()
{
  setupGeneral();
  setupTempoBar();
  setupSliders(); 
  setupPadButtons();
  setupMidi();
  setupPowerSpectrum();
  setupTopControls();
  if (isAndroidDevice)
    setupAndroid();
  sequencer.setup();
  automator.setup();
}

public AudioPlayer[] getPlayerKick() {
	return playerKick;
}

public AudioPlayer[] getPlayerBass() {
	return playerBass;
}

public AudioPlayer[] getPlayerSnare() {
	return playerSnare;
}

public AudioPlayer[] getPlayerHitHat() {
	return playerHitHat;
}

public void setupPowerSpectrum() {
  if (AndroidUtil.numCores()>1) {
	  for (int i=0;i<playerKick.length;i++) {
		  playerKick[i].setAnalysing(true);
		  playerBass[i].setAnalysing(true);
		  playerSnare[i].setAnalysing(true);
		  playerHitHat[i].setAnalysing(true);
	  }
  }
  maxLedWidth=buttonSize;
  ledHeight=(height*0.01f);      
  originPowerSpecX=0;
  originPowerSpecY=(int)height;
}

public void setupGeneral() {
  //frameRate(30);
  /*if (!isAndroidDevice){
    if(render3D)
      size(480, 688, P3D);
    else
      size(480, 688);
  }
  else
    size(420, 700);*/
 

  //textMode(MODEL);
  //noSmooth();
  //smooth(8);
  hint(DISABLE_DEPTH_TEST);
  FontAdjuster.setWidth(width);
  audioPlayThread=new AudioPlayThread(1, "AudioPlayThread");
  audioPlayThread.start();
  paused=false;
  background(0);
  maxim = new Maxim(this,mainFrequency);
  //maxim = new Minim(this); 
  for (int i=0;i<playerKick.length;i++) {
	if(i<5){
    playerKick[i] = maxim.loadFile("kick_"+i+".wav");
    playerKick[i].setLooping(false);
    playerKick[i].volume(volume);
    playerKick[i].setFilter(filterFrequency, filterResonance);
    playerBass[i] = maxim.loadFile("bass_"+i+".wav");
    playerBass[i].setLooping(false);
    playerBass[i].volume(volume);
    playerBass[i].setFilter(filterFrequency, filterResonance);
    playerSnare[i] = maxim.loadFile("snare_"+i+".wav");
    playerSnare[i].setLooping(false);
    playerSnare[i].volume(volume);
    playerSnare[i].setFilter(filterFrequency, filterResonance);
    playerHitHat[i] = maxim.loadFile("hithat_"+i+".wav");
    playerHitHat[i].setLooping(false);
    playerHitHat[i].volume(volume);
    playerHitHat[i].setFilter(filterFrequency, filterResonance);
	}else{
	    playerKick[i] = playerKick[0];
	    playerBass[i] = playerBass[0];
	    playerSnare[i] = playerSnare[0];
	    playerHitHat[i] = playerHitHat[0];
	}
  }
  //player.volume(1.0);
  //backTopMachine=loadImage("MPD26_mod.png");
  //lcdFont = loadFont("lcd.ttf");
  rectMode(CORNER);
}

public void setupMidi() {
  midi=new Midi(this);
}

public void changeBPM(float newBPM) {
  BPM=newBPM;
  gridMS=60000.0f/BPM/gridsByBeat;
  beatMS=60000.0f/BPM;
  beatsPerTempo=4.0f;
  tempoMS=beatsPerTempo*beatMS;
    
}

public void noteOn(int channel, int pitch, int velocity) {
  int soundPlayer=(pitch-24)%6;
  int soundNumber=(pitch-24)/6;
  int soundType=soundPlayer+soundNumber*4;  
  //println("type:"+soundType+" player:"+soundPlayer+" number:"+soundNumber);
  if (soundType>=0 && soundType<16 && soundNumber>=0 && soundNumber<4 && soundPlayer>=0 && soundPlayer<4) {
    switch(soundPlayer) {
    case 0:
      playerKick[soundNumber].volume(map(velocity, 0, 127, 0, 1.0f));
      break;
    case 1:
      playerBass[soundNumber].volume(map(velocity, 0, 127, 0, 1.0f));
      break;
    case 2:
      playerSnare[soundNumber].volume(map(velocity, 0, 127, 0, 1.0f));
      break;
    case 3:
      playerHitHat[soundNumber].volume(map(velocity, 0, 127, 0, 1.0f));
      break;
    }    
    addSoundTypeToList(soundType);
  }
}

public void noteOff(int channel, int pitch, int velocity) {
  //midi.noteOff(channel, pitch, velocity);
}

public void controllerChange(int channel, int number, int value) {
  //midi.controllerChange(channel, number, value);
  if (channel==15) {
    if (number==0) {
      speed=map(value, 0, 127, 0, 2);
      filterFrequency=map(value, 0, 127, 0, mainFrequency/4.0f);
    }
    if (number==1) {
      filterResonance=map(value, 0, 127, 0, 1);
    } 
    controlPitch(speed, 0);    
    controlFilter(filterFrequency, filterResonance, 0);
  }
}

public void setupTempoBar() {
  barWidth=width*0.82f;
  barHeight=height*0.025f;
  blueDarkColor=lerpColor(blueColor, color(150), .5f);
  blueLightColor=lerpColor(blueColor, color(0), .5f);  
  barColor = color(50);
  markerColor = orangeColor;
  separatorColor = candyPink;
  markerWidth=width*0.005f;
  barOriginX=width*0.091f;
  if (!isAndroidDevice)
    barOriginY=height*0.01f;
  else
    barOriginY=height*0.025f;
}

public void setupPadButtons() {
  buttonSize=width*0.153f;
  buttonsOriginX=width*0.131f;
  buttonMarginX=buttonSize*0.29f;
  buttonMarginY=buttonSize*0.275f;

  if (isAndroidDevice) {
    buttonsOriginY=height*0.425f;
  }
  else {
    buttonsOriginY=height*0.445f;
  }
  
  padsContainerOriginY=buttonsOriginY-buttonSize*0.2f;
}

public void setupSliders() {
  sliderWidth=29.0f*(width*0.14f/44.0f);//width*0.12;
  sliderHeight=29.0f*(height*0.06f/44.0f);//width*0.025;
  sliderMarginX=width*0.142f;
  sliderOriginX=width*0.10f;
  sliderImage=loadImage("slider.png");

  if (isAndroidDevice) {
    sliderMinY=height*0.175f;
    sliderMaxY=height*0.305f;
  }
  else {
    sliderMinY=height*0.162f;
    sliderMaxY=height*0.33f;
  }
	for (int i=0;i<sliders.length;i++) {
		if (sliders[i]==null) {
			sliders[i]=new VerticalSlider(sliderOriginX+(sliderMarginX*i), sliderMinY+(sliderMaxY-sliderMinY)*0.5f, sliderWidth, sliderHeight);
			sliders[i].limitY(sliderMinY, sliderMaxY);      
			if (i>2) {
				sliders[i].setY(sliderMinY);
			}
		}
	}
	sliders[0].setText("FILTER\nFREQ");
	sliders[1].setText("FILTER\nRES");
	sliders[2].setText("PITCH\nSPEED");
	sliders[3].setText("KICK\nVOL");
	sliders[4].setText("BASS\nVOL");
	sliders[5].setText("MAIN\nVOL");  
}

public void toggleAudioPlayThread() {

  if (paused) {
    audioPlayThread= new AudioPlayThread(1, "AudioPlayThread");
    audioPlayThread.start();
  }
  else {
    pausedMS=millis();
    if (audioPlayThread.isRunning())
      audioPlayThread.quit();
  }

  paused=!paused;
}

public long getPlayedMS(){
	return audioMs;
}
public float getTempoMS(){
	return tempoMS;
}
public float getGridMS(){
	return gridMS;
}

//synchronized 
public void processTempoVars() {

  if (pausedMS>=0) {
    totalPaused=(totalPaused+(millis()-pausedMS))%(int)tempoMS;
    pausedMS=-1;
  }  

  audioMs=millis()-totalPaused;

  //println("Comparing times, audioMs:"+audioMs+" pausedOn:"+pausedMS+" totalPaused:"+totalPaused);
  tempoOffset=audioMs%tempoMS;
  beatOffset=audioMs%beatMS;
  //println("tempoOffset:"+tempoOffset+" beatOffset:"+beatOffset+" gridMS:"+gridMS);

  if (currentGrid==0 && lastGrid!=0) {
    for (int i=0;i<totalGrids;i++) {
      playedGrids[i]=false;
    }
  }   

  if ((currentGrid-lastGrid)%32>1)println("ERROR skipped grids:"+(currentGrid-lastGrid-1));
  lastGrid=currentGrid;
  currentGrid=(int)(tempoOffset/gridMS); 
  if(currentGrid<0 || currentGrid>=totalGrids)
	  return;

  if (!playedGrids[currentGrid]) {
    //println("Playing grid:"+currentGrid);
    playedGrids[currentGrid]=true;
    for (int i=0;i<totalSamples;i++) {
      if (samplesPerBeat[i][currentGrid]) {
    	if(i%4==0)
    		playSoundType(i,volume*volumeKick);
    	else if(i%4==1)
    		playSoundType(i,volume*volumeBass);
    	else
    		playSoundType(i,volume);
        float dif=((millis()%tempoMS)-(currentGrid*gridMS));
        if (dif>maxDif)maxDif=dif;
        //println("Estimated play offset:"+(currentGrid*gridMS)+" real play offset:"+(millis()%tempoMS)+" dif:"+dif+" maxDif:"+maxDif);  
        //println("Dif:"+dif+" maxDif:"+maxDif);
      }
    }
    audioPlayThread.setWait((int)gridMS-10);
  }
  else {
    audioPlayThread.setWait(1);
  }
}

public void setupTopControls() {
  bpmSlider=new HorizontalSlider(barOriginX, barOriginY+barHeight*2.0f, sliderWidth, buttonSize*0.5f);
  bpmSlider.limitX(barOriginX, barOriginX+barWidth*0.25f);
  bpmSlider.valuesX(80, 160, 120);
  bpmSlider.setText("BPM");

  playButton=new ToggleButton(barOriginX+barWidth*0.368f, barOriginY+barHeight*2.0f, buttonSize*0.5f, buttonSize*0.5f);
  playButton.setText("II");
  playButton.setActiveText(">");
  playButton.setFillColor(color(150, 150, 0));
  //playButton.blinkWhenOn=true;  

  loadButton=new ToggleButton(barOriginX+barWidth*0.62f-buttonSize*0.6f, barOriginY+barHeight*2.0f, buttonSize*1.2f, buttonSize*0.5f);
  loadButton.setText(getString(R.string.LOAD));
  loadButton.setFillColor(color(150, 150, 0));
  loadButton.setBlinkWhenOn(true);

  deleteButtons=new ExpandableButtons(barOriginX+barWidth-buttonSize*1.2f, barOriginY+barHeight*2.0f, buttonSize*1.2f, buttonSize*0.5f);
  deleteButtons.setText(getString(R.string.DELETE));
  deleteButtons.setFillColor(yellowColor);
  String[] buttons = { 
    "KICK", "BASS", "SNARE", "HITHAT", getString(R.string.ALL)
  };
  deleteButtons.setOtherButtons(buttons);
  int[] colors = { 
    redColor, orangeColor, blueColor, greenColor, mediumGreyColor
  };
  deleteButtons.setOtherButtonColors(colors);
}

public void drawTopControls() {
  bpmSlider.rollover(mouseX, mouseY);
  bpmSlider.dragHorizontally(mouseX);  
  bpmSlider.draw();
  changeBPM(bpmSlider.value());

  playButton.drawState();

  loadButton.drawState();

  deleteButtons.drawState();
}

public void drawMenuBar(){
	
	if(menuBar==null){
		menuBar=new ToggleButtonsBar(0, height*0.95f, width, height*0.05f);
		menuBar.setShowMenuButton(true);		
		String[] buttons={
				getString(R.string.live_menu_item),
				getString(R.string.seq_menu_item),
				getString(R.string.auto_menu_item)
				};
		menuBar.setButtons(buttons);
	}

	if(menuBar!=null)
		menuBar.draw();	

}

public void drawTempoBar() {
  strokeWeight(3);
  stroke(100);
  fill(barColor);  
  //println("x:"+width*0.05+"y:"+height*0.05+" barWidth:"+barWidth+" barHeight:"+barHeight);
  rect(barOriginX, barOriginY-2, barWidth, barHeight+4);

  //println("millis():"+millis()+" offset:"+offset+" tempoMS:"+tempoMS);
  float beatsWidth=barWidth/beatsPerTempo;
  float gridWidth=barWidth/beatsPerTempo/gridsByBeat;

  noStroke();
  for (int j=0;j<32;j++) {
      int count=0;
      for (int i=0;i<totalSamples;i++) {
      if (samplesPerBeat[i][j]) {
        count++;
        fill(getColorSoundType(i));
        float cOffset=gridWidth*j;
        switch(count){
          case 2:
            rect(barOriginX+cOffset, barOriginY+barHeight*0.5f, gridWidth, barHeight*0.5f);
          break;
          case 3:
            rect(barOriginX+cOffset, barOriginY+barHeight*0.33f, gridWidth, barHeight*0.33f);
          break;
          case 4:
            rect(barOriginX+cOffset, barOriginY+barHeight*0.5f, gridWidth, barHeight*0.25f);
          break;          
          default:
             rect(barOriginX+cOffset, barOriginY, gridWidth, barHeight);
          break;
        }
      }
    }
  }   


  for (int i=0;i<beatsPerTempo;i++) {
    if (i!=0) {
      fill(separatorColor);
      rect(beatsWidth*i+barOriginX-markerWidth*0.5f, barOriginY, markerWidth, barHeight);
    }
    fill(255);
    if (i!=beatsPerTempo)
      for (int j=1;j<gridsByBeat;j++) {
        rect((beatsWidth*i)+gridWidth*j+barOriginX, barOriginY, markerWidth/4.0f, barHeight);
      }
  }

  fill(markerColor);
  if (pausedMS<0) {
    float offset=map((millis()-totalPaused)%tempoMS, 0.0f, tempoMS, 0.0f, barWidth);
    lastMarkerOffset=offset;
    rect(barOriginX+offset-markerWidth, barOriginY, markerWidth, barHeight);
  }
  else {
    rect(barOriginX+lastMarkerOffset-markerWidth, barOriginY, markerWidth, barHeight);
  }
}

public void drawPadsContainer() {
  strokeWeight(3);
  stroke(100, 100, 100, 255);
  fill(50, 50, 50, 255);
  switch (mode) {
  case LIVE:
	  rect(barOriginX, padsContainerOriginY, barWidth, buttonSize*5.295f);	
	  break;
  case AUTOMATOR:
	  rect(automator.getContainerOriginX(), padsContainerOriginY, width-2*automator.getContainerOriginX(), buttonSize*5.295f);	
	  break;	
  }

}

public void drawPadButtons(int c, ClickablePad[] padArray, float buttonsOriginX, float buttonsOriginY, float buttonSize, float buttonMargin, String buttonText) {
  strokeWeight(3);
  stroke(100, 100, 100, 255);
  for (int i=0;i<padArray.length;i++) {
    if (padArray[i]==null)
      padArray[i]=new ClickablePad(buttonsOriginX+(buttonSize+buttonMargin)*i, buttonsOriginY, buttonSize, buttonSize);
    padArray[i].setText(buttonText+i);
    padArray[i].setFillColor(c);
    if (mainMode==loadMode) {
      if (loadButton.isBlinkOn()) {
        padArray[i].setStrokeColor(color(255));
      }
      else {
        padArray[i].setStrokeColor(color(100));
      }
    }
    else {
      padArray[i].setStrokeColor(color(100));
    }    
    padArray[i].drawState();
  }
}

public PVector getPadButtonOrigin(int soundType) {
  int row=PApplet.parseInt(soundType/4);
  int col=soundType%4;
  return new PVector(buttonsOriginX+(buttonSize+buttonMarginX)*col, buttonsOriginY+(buttonSize+buttonMarginY)*row);
}

public void drawKickButtons() {
  drawPadButtons(redColor, kick, buttonsOriginX, buttonsOriginY, buttonSize, buttonMarginX, "K");
}

public void drawBassButtons() {
  drawPadButtons(orangeColor, bass, buttonsOriginX, buttonsOriginY+buttonSize+buttonMarginY, buttonSize, buttonMarginX, "B");
}

public void drawSnareButtons() {
  drawPadButtons(blueColor, snare, buttonsOriginX, buttonsOriginY+(buttonSize+buttonMarginY)*2, buttonSize, buttonMarginX, "S");
}

public void drawHitHatButtons() {
  drawPadButtons(greenColor, hithat, buttonsOriginX, buttonsOriginY+(buttonSize+buttonMarginY)*3, buttonSize, buttonMarginX, "H");
}


public void drawSliders() {
  for (int i=0;i<sliders.length;i++) {
    sliders[i].rollover(mouseX, mouseY);
    sliders[i].dragVertically(mouseY);    
    //image(sliderImage, sliders[i].x, sliders[i].y, sliders[i].w, sliders[i].h);
    //sliders[i].debugDisplay();
    sliders[i].draw();
  }
}

public void drawFiltersZone() {

  fill(255);
  rect(width*0.05f, height*0.5f, width*0.9f, height*0.4f); 
  stroke(255);
  textSize(10);

  if (panelModeButton==null)
    panelModeButton=new Clickable(width*0.1f, height*0.51f, width*0.2f, height*0.025f);
  panelModeButton.setFillColor(redColor);   
  panelModeButton.drawState();

  fill(0); 
  text("[PANEL MODE] ["+panelMode+"]", width*0.2f, height*0.525f);

  fill(orangeColor);
  if (trackModeButton==null)
    trackModeButton=new Clickable(width*0.5f, height*0.51f, width*0.2f, height*0.025f);
  panelModeButton.setFillColor(orangeColor);   
  panelModeButton.drawState();    

  fill(0);
  //text("[TRACK MODE] ["+trackMode+"]", width*0.6f, height*0.525f);

  if (mouseY>height*0.5f) {
    stroke(0);
    line(mouseX, height*0.5f, mouseX, height);
    line(0, mouseY, width, mouseY); 
    textSize(10);
    textAlign(CORNER);
    fill(0);
    text("["+valueX+","+valueY+"]", mouseX+width*0.01f, mouseY-height*0.005f);
  }
}

public void drawPowerSpectrum() {
	if (AndroidUtil.numCores()>1) {	
		for (int i=0;i<playerKick.length;i++) {
			drawSpectrumOf(playerKick[i], redColor, i);
			drawSpectrumOf(playerBass[i], orangeColor, i+4);
			drawSpectrumOf(playerSnare[i], blueColor, i+8);
			drawSpectrumOf(playerHitHat[i], greenColor, i+12);
		}
	}
}

public void drawSpectrumOf(AudioPlayer audioPlayer, int c, int soundType) {
  if (audioPlayer.isPlaying()) {
    float[] values=audioPlayer.getPowerSpectrum();
    if (values!=null && values.length>0) {
      float avg=audioPlayer.getAveragePower();
      //println("total values:"+values.length+" value[0]:"+values[0]+" avg:"+avg);
      strokeWeight(1);
      //stroke(red(c),green(c),blue(c));
      stroke(c, 128);

      if (avg>=0) {
        float ledWidth=maxLedWidth*avg;
        float ledX=buttonsOriginX+(buttonSize+buttonMarginX)*(soundType%4);
        float ledY=buttonsOriginY-(buttonSize*0.15f)+(buttonSize+buttonMarginY)*(soundType/4);
        fill(c);        
        rect(ledX+maxLedWidth-ledWidth, ledY, ledWidth, ledHeight);
      }
      float xOffset=0;
      PVector origin=getPadButtonOrigin(soundType);
      originPowerSpecX=origin.x+1;
      originPowerSpecY=origin.y+buttonSize-(spectrumMode==BARS?2:3);      
      origin.y=origin.y+buttonSize-3-values[0]*height*0.1f;
      origin.x+=3;      
      fill(100);
      for (int i=0;i<values.length;i+=values.length/10.0f) {
        if (values[i]<0)values[i]=0;
        xOffset=map(i, 0, values.length, 0, buttonSize-4-(spectrumMode==BARS?6:0));
        switch(spectrumMode) {
        case LINES:
          stroke(50);
          strokeWeight(3);
          if (i>=(int)(values.length/10.0f)) {
            PVector destiny=new PVector(originPowerSpecX+xOffset, originPowerSpecY-values[i]*height*0.1f);
            line(origin.x, origin.y, destiny.x, destiny.y);
            origin=destiny;
          }
          break;
        case BARS:
          noStroke();
          PVector destiny=new PVector(originPowerSpecX+xOffset, originPowerSpecY-values[i]*height*0.1f);
          rect(destiny.x, destiny.y, buttonSize/10, originPowerSpecY-destiny.y);
          origin=destiny;
          break;
        }
      }
    }
    else {
      println("No values received");
    }
  }
}

float animationStart, animationEnd;
boolean animating=false, animatingBack=false;
float halfTime=0;
void animateTransition(float animTime, float startValue, float endValue) {

  int ms=millis();

  if (animating) {
    animating=false;
    animatingBack=false;    
    animationStart=ms;
    halfTime=animationStart+animTime*0.5f;
    animationEnd=animationStart+animTime;
  }

  if (ms<animationEnd) {
    if (ms<halfTime) {
      if (render3D) {
        float rot=map(ms, animationStart, halfTime, 0, PI);
        rotateY(rot);
      }
      else {
        if (mode==MainMode.SEQUENCER) {
          float xPos=map(ms, animationStart, halfTime, 0, width*0.5f);          
          float yPos=map(ms, animationStart, halfTime, 0, height*0.5f);           
          float scale=map(ms, animationStart, halfTime, 1, 0);
          translate(xPos, yPos);
          scale(scale);
        }
        else {          
          float yPos=map(ms, animationStart, halfTime, 0, -height);
          translate(0, yPos);
        }
      }
    }
    else { 
      if (!animatingBack) {
        animatingBack=true;
        animationEnd=ms+animTime*0.5f;
        if (mode!=MainMode.LIVE){
        	mode=MainMode.LIVE;
        }else{
        	mode=MainMode.SEQUENCER;
        }
      } 

      if (render3D) {
        float xPos=map(ms, halfTime, animationEnd, width, 0);  
        float zPos=map(ms, halfTime, animationEnd, 1500, 0);
        float rot=map(ms, halfTime, animationEnd, PI, 0);          
        translate(xPos, 0, -zPos);        
        rotateY(rot);
      }
      else {
        if (mode==MainMode.SEQUENCER) {
          float xPos=map(ms, halfTime, animationEnd, width*0.5f, 0);          
          float yPos=map(ms, halfTime, animationEnd, height*0.5f, 0);          
          float scale=map(ms, halfTime, animationEnd, 0, 1);
          translate(xPos, yPos);
          scale(scale);
        }
        else {
          float yPos=map(ms, halfTime, animationEnd, height, 0);
          translate(0, yPos);
        }
      }
    }
  }
  else {     
    //animating=false;
    animationStart=0;
    animationEnd=0;
  }
}

public void draw()
{  
  touch.analyse();
  touch.sendEvents();	
  clear();
  background(127, 127, 127);

  //animateTransition(1000,0,90);
  
  if (mode!=MainMode.SEQUENCER) {
	  drawTempoBar();
	  drawPadsContainer();
	  if (mode!=MainMode.AUTOMATOR) {
		  drawKickButtons();
		  drawBassButtons();
		  drawSnareButtons();
		  drawHitHatButtons();      
		  drawSliders();
		  drawPowerSpectrum();
		  drawTopControls();
	  }else{
		  automator.draw();
	  }
  }
  else {
	  sequencer.draw();
  }  

  drawMenuBar();
}


public void controlVolume(float volume, int trackMode) {
  if (trackMode==KICK || trackMode==ALL) {
    for (int i=0;i<playerKick.length;i++)   
      playerKick[i].volume(volume);
    	//playerKick[i].ramp(volume,10);	
  }
  if (trackMode==BASS || trackMode==ALL) {
    for (int i=0;i<playerBass.length;i++)
      playerBass[i].volume(volume);
  }
  if (trackMode==SNARE || trackMode==ALL) {
    for (int i=0;i<playerSnare.length;i++)
      playerSnare[i].volume(volume);
  }
  if (trackMode==HITHAT || trackMode==ALL) {
    for (int i=0;i<playerHitHat.length;i++)
      playerHitHat[i].volume(volume);
  }
}


public void controlFilter(float filterFrequency, float filterResonance, int trackMode) {
  if (trackMode==KICK || trackMode==ALL) {
    for (int i=0;i<playerKick.length;i++)   
      playerKick[i].setFilter(filterFrequency, filterResonance);
  }
  if (trackMode==BASS || trackMode==ALL) {
    for (int i=0;i<playerBass.length;i++)
      playerBass[i].setFilter(filterFrequency, filterResonance);
  }
  if (trackMode==SNARE || trackMode==ALL) {
    for (int i=0;i<playerSnare.length;i++)
      playerSnare[i].setFilter(filterFrequency, filterResonance);
  }
  if (trackMode==HITHAT || trackMode==ALL) {
    for (int i=0;i<playerHitHat.length;i++)
      playerHitHat[i].setFilter(filterFrequency, filterResonance);
  }
}

public void controlPitch(float speed, int trackMode) {

  if (trackMode==KICK || trackMode==ALL) {   
    for (int i=0;i<playerKick.length;i++){ 
      playerKick[i].speed(speed);
    }
  }
  if (trackMode==BASS || trackMode==ALL) {
    for (int i=0;i<playerBass.length;i++){
      playerBass[i].speed(speed);
    }
  }
  if (trackMode==SNARE || trackMode==ALL) {
    for (int i=0;i<playerSnare.length;i++){
      playerSnare[i].speed(speed);
    }
  }
  if (trackMode==HITHAT || trackMode==ALL) {
    for (int i=0;i<playerHitHat.length;i++){
      playerHitHat[i].speed(speed);
    }
  }
}

public void proccessPanel() {
  valueX=constrain(map(mouseX, width*0.05f, width*0.9f, 0.0f, 1.0f), 0.0f, 1.0f);
  valueY=constrain(map(mouseY, height*0.5f, height*0.95f, 0.0f, 1.0f), 0.0f, 1.0f);   
  switch(panelMode) {
  case FILTER:
    filterFrequency=map(valueY, 0.0f, 1.0f, 0.0f, mainFrequency/4.0f);
    filterResonance=map(valueX, 0.0f, 1.0f, -1.0f, 1.0f);  
    controlFilter(filterFrequency, filterResonance, filterTrackMode);
    break;
  case ECHO:
    delayTime=map(valueX, 0.0f, 1.0f, 0.0f, 3000);
    delayFeedback=map(valueY, 0.0f, 1.0f, 0.0f, 100.0f); 
    if (filterTrackMode==KICK || filterTrackMode==ALL) {
      for (int i=0;i<playerKick.length;i++) {
        playerKick[i].setDelayTime(delayTime);
        playerKick[i].setDelayFeedback(delayFeedback);
      }
    }
    if (filterTrackMode==BASS || filterTrackMode==ALL) {
      for (int i=0;i<playerBass.length;i++) {
        playerBass[i].setDelayTime(delayTime);
        playerBass[i].setDelayFeedback(delayFeedback);
      }
    }
    if (filterTrackMode==SNARE || filterTrackMode==ALL) {
      for (int i=0;i<playerSnare.length;i++) {
        playerSnare[i].setDelayTime(delayTime);
        playerSnare[i].setDelayFeedback(delayFeedback);
      }
    }
    if (filterTrackMode==HITHAT || filterTrackMode==ALL) {
      for (int i=0;i<playerHitHat.length;i++) {
        playerHitHat[i].setDelayTime(delayTime);
        playerHitHat[i].setDelayFeedback(delayFeedback);
      }
    }       
    break;
  case PITCH:
    speed=map(valueX, 0.0f, 1.0f, 0.0f, 2.0f);
    controlPitch(speed, filterTrackMode);
    break;
  }
}



/*
public void mousePressed(){
	mousePressed(-1,mouseX,mouseY,1.0);
}
*/
public void mousePressed(int id,int mouseX,int mouseY,float pressure)
{
	  switch (mode) {
	  	case LIVE:
	  	    for (int i=0;i<kick.length;i++) {
	  	      if (kick[i].isClicked(mouseX, mouseY)) {
	  	        if (mainMode==normalMode)
	  	          addSoundTypeToList(kick.length*i,pressure);
	  	        else if (mainMode==loadMode)
	  	          loadSoundType(kick.length*i, playerKick[i]);
	  	        else if (mainMode==deleteMode)
	  	          deleteSoundType(kick.length*i);
	  	      }
	  	      else if (bass[i].isClicked(mouseX, mouseY)) {
	  	        if (mainMode==normalMode)
	  	          addSoundTypeToList(1+bass.length*i,pressure);
	  	        else if (mainMode==loadMode)
	  	          loadSoundType(1+bass.length*i, playerBass[i]);
	  	        else if (mainMode==deleteMode)
	  	          deleteSoundType(1+bass.length*i);
	  	      }
	  	      else if (snare[i].isClicked(mouseX, mouseY)) {
	  	        if (mainMode==normalMode)
	  	          addSoundTypeToList(2+snare.length*i,pressure);
	  	        else if (mainMode==loadMode)
	  	          loadSoundType(2+snare.length*i, playerSnare[i]);
	  	        else if (mainMode==deleteMode)
	  	          deleteSoundType(2+snare.length*i);
	  	      }
	  	      else if (hithat[i].isClicked(mouseX, mouseY)) {
	  	        if (mainMode==normalMode)
	  	          addSoundTypeToList(3+hithat.length*i,pressure);
	  	        else if (mainMode==loadMode)
	  	          loadSoundType(3+snare.length*i, playerHitHat[i]);
	  	        else if (mainMode==deleteMode)
	  	          deleteSoundType(3+snare.length*i);
	  	      }
	  	    }
	  	    for (int i=0;i<sliders.length;i++) {
	  	      sliders[i].clicked(mouseX, mouseY);
	  	    }
	  	    bpmSlider.clicked(mouseX, mouseY);

	  	    if (deleteButtons.isClicked(mouseX, mouseY)) {
	  	    }
	  	    if (loadButton.isClicked(mouseX, mouseY)) {
	  	      if (loadButton.isON()) {
	  	        mainMode=loadMode;
	  	      }
	  	      else {
	  	        mainMode=normalMode;
	  	      }
	  	    } 
	  	    if (playButton.isClicked(mouseX, mouseY)) {
	  	      toggleAudioPlayThread();
	  	    }
		break;
	  	case SEQUENCER:
	  		sequencer.mousePressed(mouseX,mouseY);	
		break;
	  	case AUTOMATOR:
	  		automator.mousePressed(id,mouseX,mouseY,pressure);	
		break;		
	  }	
	  
	  if (menuBar.isMenuClicked(mouseX, mouseY)) {
		  this.openOptionsMenu();
	  }
	  switch (menuBar.isButtonClicked(mouseX, mouseY)) {
	  	case 0:
	  		  controlVolume(1.0f, ALL);
			  mode=MainMode.LIVE;		
		break;
	  	case 1:
	  		  controlVolume(1.0f, ALL);
			  sequencer.updateState();	  
			  mode=MainMode.SEQUENCER;			
		break;
	  	case 2:
			  automator.updateState();	  
			  mode=MainMode.AUTOMATOR;
		break;		
	}
}


public void mouseMoved()
{
	switch (mode) {
	case LIVE:
		for (int i=0;i<kick.length;i++) {
			if (kick[i].isOver(mouseX, mouseY)) {
			}
			if (bass[i].isOver(mouseX, mouseY)) {
			}
			if (snare[i].isOver(mouseX, mouseY)) {
			}
			if (hithat[i].isOver(mouseX, mouseY)) {
			}
		}
		if (deleteButtons.isOver(mouseX, mouseY)) {
		}
		if (loadButton.isOver(mouseX, mouseY)) {
		}
		if (playButton.isOver(mouseX, mouseY)) {
		}
		if (menuBar.isOver(mouseX, mouseY)) {
		}   	
		break;
	case SEQUENCER:
		sequencer.mouseMoved();	
		break;	
	case AUTOMATOR:
		automator.mouseMoved();
		break;		
	}	
}

public void mouseDragged(){
	//if(mode==MainMode.SEQUENCER)
		//mousePressed(-1,mouseX,mouseY,1.0f);
}

public void mouseDragged(int id,int mouseX,int mouseY,int pmouseX,int pmouseY,float pressure)
{
	  switch (mode) {
	  	case LIVE:
	  	    if (deleteButtons.isSelected(mouseX, mouseY)) {
	  	    }  

	  	    for (int i=0;i<sliders.length;i++) {
	  	      if (sliders[i].isDragging()) {
	  	        float valY=constrain(map(sliders[i].getY(), sliderMinY, sliderMaxY, 1.0f, 0.0f), 0.0f, 1.0f);    
	  	        switch(i) {
	  	        case 0:
	  	          filterFrequency=map(valY, 0.0f, 1.0f, 0.0f, mainFrequency/4.0f);
	  	          controlFilter(filterFrequency, filterResonance, 0);
	  	          break;
	  	        case 1:
	  	          filterResonance=valY;
	  	          controlFilter(filterFrequency, filterResonance, 0);
	  	          break;
	  	        case 2:
	  	          speed=valY*2;
	  	          controlPitch(speed, 0);
	  	          break;
	  	        case 3:
	  	          volumeKick=valY;
	  	          controlVolume(volumeKick*volume, 1);      
	  	          break;
	  	        case 4:
	  	          volumeBass=valY;
	  	          controlVolume(volumeBass*volume, 2);      
	  	          break;
	  	        case 5:
	  	          volume=valY;
	  	          controlVolume(volume, 0);
	  	          break;
	  	        }
	  	        //println("frequency:"+filterFrequency+" resonance:"+filterResonance+" speed:"+valY*2);
	  	      }
	  	    }  	
		break;
	  	case SEQUENCER:
	  	    sequencer.mouseDragged(pmouseX,pmouseY,mouseX,mouseY);	
		break;
	    case AUTOMATOR:
	        automator.mouseDragged(id,mouseX,mouseY,pressure);
	        break;		
	  }	
}

public void mouseReleased() {
	//mouseReleased(-1,mouseX,mouseY,1.0f);
}

public void mouseReleased(int id,int mouseX,int mouseY,float pressure) {
	  switch (mode) {
	  	case LIVE:
	  	    for (int i=0;i<sliders.length;i++) {
	  	      sliders[i].stopDragging();
	  	    }
	  	    bpmSlider.stopDragging();
	  	    for (int i=0;i<kick.length;i++) {
	  	      kick[i].stopClick();
	  	      bass[i].stopClick();
	  	      snare[i].stopClick();
	  	      hithat[i].stopClick();
	  	    }
	  	    int index=deleteButtons.buttonSelectedAt(mouseX, mouseY);
	  	    if (index!=-1) {
	  	      if (index>=4)
	  	        deleteAllSounds();
	  	      else
	  	    	deleteSoundOfGroup(index);
	  	    }
	  	    loadButton.stopClick();
	  	    playButton.stopClick();
		break;
	  	case SEQUENCER:
	  		sequencer.mouseReleased(mouseX,mouseY);	
		break;
	    case AUTOMATOR:
	        automator.mouseReleased(id,mouseX,mouseY,pressure);
	        break;		
	  }	
	  
      menuBar.stopClick();
}

public void deleteSoundType(int soundType) {
  for (int i=0;i<32;i++) {
    samplesPerBeat[soundType][i]=false;
  }
  //sequencer.updateTracksState();
}


public void deleteAllSounds() {
  for (int i=0;i<totalSamples;i++) {
    getPlayerBySoundType(i).stop();
    for (int j=0;j<(int)(gridsByBeat*beatsPerTempo);j++) {
      samplesPerBeat[i][j]=false;
    }
  }
  sequencer.updateState();
}

public void deleteSoundOfGroup(int soundGroup) {
  //println("Deleting sound group:"+soundGroup);
  for (int i=0;i<totalSamples;i+=4) {
    deleteSoundType(i+soundGroup);
  }
  sequencer.updateState();
}


public void loadSoundType(int soundType, AudioPlayer player) {
  loadPlayerOfSoundType=soundType;
  //if (isAndroidDevice)
  files.selectInput("Select a .wav,.aif file to load:", "fileSelected");

  //selectInput("Select a .wav,.aif file to load:", "fileSelected");
}

public JSONArray loadJsonSoundPack(File file) {
	JSONArray sounds=null;
  if (file.exists() && file.isFile()) {
    if(isAndroidDevice){
      String text = join( loadStrings( file.getAbsolutePath() ), "");
      println("Loaded JSON:"+text);
		try {
			sounds = new JSONArray(text);
			println("Parsed:"+sounds.length()+" sounds"+sounds);			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      return sounds;
    }else{
      //sounds=loadJSONArray(file.getAbsolutePath());
    }
  }    
  return sounds;
}

static boolean toggled=false;

public void toggleAudio(){
	if(!paused){
  		toggled=true;
  		toggleAudioPlayThread();
  		Log.d("TOGGLED AUDIO","TOGGLED AUDIO");
  	}else{
  		//toggled=false;
  	}
}

public void fileSelected(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } 
  else {
	toggleAudio();
    println("User selected " + selection.getAbsolutePath()+" name:"+selection.getName());
    if (selection.getName().endsWith("json")) {
    	JSONArray sounds=loadJsonSoundPack(selection);
    	final ProgressDialog progressDialog= new ProgressDialog(DrumCloud.X);;
		if(isAndroidDevice){
			//progressDialog = new ProgressDialog(DrumCloud.activity);
			progressDialog.setMessage("Reading sound files");
			progressDialog.setIndeterminate(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMax(16);
			progressDialog.show();
		}    	
		soundsLoaded=0;
    	for (int i=0;i<sounds.length();i++) {
    		try{
    			JSONObject sound = sounds.getJSONObject(i);        		
    			if(sound!=null){       		
    				String finalPath=DownloadFile.getDownloadPath()+""+sound.getString("filePath");
    				println("Loading sound:"+finalPath+" on pad:"+sound.getInt("soundType"));
    				final File localFile=new File(finalPath);
    				final int soundType=sound.getInt("soundType");
    				final String fileName=sound.getString("fileName");
    				//else
    				//localFile=new File(this.dataPath("")+sound.getString("filePath"));
    			    Thread t = new Thread() {
    			        public void run() {
    	    				loadSoundOnPlayer(soundType,localFile);
    	    				synchronized (this) {
    	    					soundsLoaded++;
							}
    	    				DrumCloud.X.runOnUiThread(new Runnable() {
    	    					@Override
    	    					public void run() {
    	    	    				if(progressDialog!=null){
    	    	    					progressDialog.setMessage("Reading sound:"+fileName);
    	    	    					progressDialog.setProgress(soundsLoaded);
    	    	    					if(soundsLoaded==16){
    	    	    						progressDialog.dismiss();
    	    	    					    if(toggled){
    	    	    					    	toggleAudioPlayThread();
    	    	    					    	toggled=false;
    	    	    					    }
    	    	    					}
    	    	    				}
    	    					}
    	    				});    	    				
    			        }
    			    };
    			    t.start();
    			}
    		}catch(JSONException e){
    			e.printStackTrace();
    		}
      }	
    }
    else {
      if (loadPlayerOfSoundType!=-1) {
        loadSoundOnPlayer(loadPlayerOfSoundType,selection);
	    if(toggled){
	    	toggleAudioPlayThread();
	    	toggled=false;
	    }
      }
    }
  }
  mainMode=normalMode;
  loadButton.setON(false);
}

public void loadSoundOnPlayer(int soundType,File selection) {
	if(selection.exists() && selection.isFile()){
		println("Loading file " + selection.getAbsolutePath());
		AudioPlayer ap=getPlayerBySoundType(soundType);
		ap.stop();
		maxim.reloadFile(ap, selection.getAbsolutePath()); 
		if (ap!=null) {
			//if (AndroidUtil.numCores()>1) {
			ap.setAnalysing(true);
			//}
			ap.setLooping(false);
		}else {
			println("loaded Player is null");
		}    
	}
}

public AudioPlayer getPlayerBySoundType(int soundType) {
  int soundNumber=soundType/4;
  switch(soundType%4) {
  case 0:
    return playerKick[soundNumber];        
  case 1:
    return playerBass[soundNumber];        
  case 2:
    return playerSnare[soundNumber];        
  case 3:
    return playerHitHat[soundNumber];
  }
  return null;
}

public int getSampleIndexBySoundType(int soundType) {
	return (soundType%4*4)+(soundType/4);
}

public void playSoundType(int soundType,float volume) {
  AudioPlayer ap=getPlayerBySoundType(soundType);
  ap.ramp(ap.getVolume()*volume,2);  
  ap.cue(0);
  ap.play();
  midi.sendNote(1, getSampleIndexBySoundType(soundType), (int)(127*ap.getVolume()*volume),100);
}

public int getColorByMs(float msOcc) {
  if (soundByCue.hasKey(msOcc+"")) {
    int[] types = PApplet.parseInt(split(soundByCue.get(msOcc+""), '#'));
    if (types.length>0) {
      int finalColor=getColorSoundType(types[0]);
      //for (int i=1;i<types.length;i++)
      //  finalColor=lerpColor(finalColor, getColorSoundType(types[i]), 0.5);//1.0/types.length);
      return finalColor;
    }
    else return color(0);
  }
  else
    return color(0);
}

public int getColorSoundType(int sountType) {
  switch(sountType%4) {
  case 0:
    return redColor;        
  case 1:
    return orangeColor;        
  case 2:
    return blueColor;        
  case 3:
    return greenColor;       
  default:
    return color(0);
  }
}

public void addSoundTypeToList(int soundType){
	addSoundTypeToList(soundType,1);
}

public void addSoundTypeToList(int soundType,float pressure) {
  playSoundType(soundType,pressure);
  if (!paused) {
    samplesPerBeat[soundType][currentGrid]=!samplesPerBeat[soundType][currentGrid];
    //println("changed sound:"+soundType+" at position:"+currentGrid);
  }
}

void addSoundTypeRepeated(int soundType,float pressure,int repeatOffset) {
	playSoundType(soundType,pressure);
	if (!paused) {
		int size=samplesPerBeat[soundType].length;
		int times=size/repeatOffset;
		for(int i=0;i<times;i++){
			int index=(currentGrid+i*repeatOffset)%size;
			samplesPerBeat[soundType][index]=true;
			//println("changed sound:"+soundType+" at position:"+currentGrid);
		}
	}
}

void updateSoundTypeRepeated(int soundType,int startPos,int repeatOffset) {
	if (!paused) {
		removeAllSoundsOfType(soundType);
		int size=samplesPerBeat[soundType].length;
		int times=size/repeatOffset;
		for(int i=0;i<times;i++){
			int index;
			if(startPos>=0)
				index=(startPos+i*repeatOffset)%size;
			else
				index=(i*repeatOffset)%size;
			samplesPerBeat[soundType][index]=true;
		}
	}
}

void removeAllSoundsOfType(int soundType){
	int size=samplesPerBeat[soundType].length;
	//getPlayerBySoundType(soundType).ramp(0, 10);
	//getPlayerBySoundType(soundType).stop();	
	for(int i=0;i<size;i++){
		if(samplesPerBeat[soundType][i]){			
			samplesPerBeat[soundType][i]=false;
		}
	}
}

public boolean surfaceKeyDown(int code, KeyEvent event) {
	if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
		onBackPressed();
		return false;
	}
	return super.surfaceKeyDown(code, event);
}

public boolean surfaceKeyUp(int code, KeyEvent event) {
	return super.surfaceKeyDown(code, event);
}


public void keyPressed() {
	
  if (key == CODED) {	  
    //println("Pressed:"+keyCode);	  
	if (keyCode == UP) {
      if (BPM<140)
        changeBPM(BPM+1);
    } 
    else if (keyCode == DOWN) {
      if (BPM>80)
        changeBPM(BPM-1);
    } 
    else if (keyCode == LEFT) {
      if (gridsByBeat>2)gridsByBeat*=0.5f;
    }
    else if (keyCode == RIGHT) {
      if (gridsByBeat<32)gridsByBeat*=2;
    }
    else if (keyCode == BACKSPACE) {
      if (savedCues.size()>0) {
        savedCues.remove(savedCues.size()-1);
        //println("Deleting last cue");
      }
    }
  } 
  else {
    switch(keyCode) {
    case 32://SPACEBAR
      toggleAudioPlayThread();
      break;
    case BACKSPACE:
    case 187:
      if (savedCues.size()>0) {
        savedCues.remove(savedCues.size()-1);
        //println("Deleting last cue");
      }      
      break;
    case 49:
      addSoundTypeToList(0);
      break;      
    case 50:
      addSoundTypeToList(4);
      break;      
    case 51:
      addSoundTypeToList(8);
      break;      
    case 52:
      addSoundTypeToList(12);
      break;
    case 81:
      addSoundTypeToList(1);
      break;      
    case 87:
      addSoundTypeToList(5);
      break;      
    case 69:
      addSoundTypeToList(9);
      break;      
    case 82:
      addSoundTypeToList(13);
      break;
    case 65:
      addSoundTypeToList(2);
      break;      
    case 83:
      addSoundTypeToList(6);
      break;      
    case 68:
      addSoundTypeToList(10);
      break;      
    case 70:
      addSoundTypeToList(14);
      break; 
    case 90:
      addSoundTypeToList(3);
      break;      
    case 88:
      addSoundTypeToList(7);
      break;      
    case 67:
      addSoundTypeToList(11);
      break;      
    case 86:
      addSoundTypeToList(15);
      break;
    }   
    //println("Pressed code:"+keyCode);
  }
}

  //public int sketchWidth() { return 768; }
  //public int sketchHeight() { return 1280; }
  //public String sketchRenderer() { return P3D; }
  
  
  public TouchProcessor touch=new TouchProcessor(this);
  
	public boolean surfaceTouchEvent(MotionEvent event) {
	  
	  // extract the action code & the pointer ID
	  int action = event.getAction();
	  int code   = action & MotionEvent.ACTION_MASK;
	  int index  = action >> MotionEvent.ACTION_POINTER_ID_SHIFT;

	  float x = event.getX(index);
	  float y = event.getY(index);
	  int id  = event.getPointerId(index);
	  float pressure = event.getPressure(index);
	  //println("Pressure:"+pressure);

	  // pass the events to the TouchProcessor
	  if ( code == MotionEvent.ACTION_DOWN || code == MotionEvent.ACTION_POINTER_DOWN) {
		 int numPointers = event.getPointerCount();
		 //println("Pressed "+numPointers+" pointers.");
		 onTouchDown(id, x, y, pressure);		 
		 if(mode==MainMode.SEQUENCER){
			 touch.pointDown(x, y, id, pressure);
		 }
		 /*
		 for (int i=0; i < numPointers; i++) {
		      id = event.getPointerId(i);
		      x = event.getX(i);
		      y = event.getY(i);
		      pressure = event.getPressure(i);		      
		      onTouchDown(x, y, pressure);
		      if(i==0)
		  	    touch.pointDown(x, y, id, pressure);
		 }	*/	  				  
	  }
	  else if (code == MotionEvent.ACTION_UP || code == MotionEvent.ACTION_POINTER_UP) {
		int numPointers = event.getPointerCount();  
		//println("Released "+numPointers+" pointers. Index:"+index);  
		
		if(mode==MainMode.SEQUENCER){ 
			if(numPointers==1)
				onTouchUp(event.getPointerId(index), x, y, pressure);			
			touch.pointUp(event.getPointerId(index));		
		}else{
			onTouchUp(event.getPointerId(index), x, y, pressure);
		}
	  }
	  else if ( code == MotionEvent.ACTION_MOVE) {
	    int numPointers = event.getPointerCount();
	    final int historySize = event.getHistorySize();
	    //println("Moved "+numPointers+" pointers.");
	    for (int i=0; i < numPointers; i++) {
	      id = event.getPointerId(i);
	      x = event.getX(i);
	      y = event.getY(i);
	      float px = x;
	      if(historySize>0)px=event.getHistoricalX(i, 0);
	      float py = y;
	      if(historySize>0)py=event.getHistoricalY(i, 0);
	      pressure = event.getPressure(i);
	      onTouchMove(id, x, y, px, py, pressure);
	      if(mode==MainMode.SEQUENCER){
	    	  touch.pointMoved(x, y, id);
	      }	      
	    }
	  } 
	  
		//DrumCloud.activity.runOnUiThread(new Runnable() {
			//@Override
			//public void run() {
				//touch.analyse();
				//touch.sendEvents();
			//}
		//}); 	  	  
	  
	  return super.surfaceTouchEvent(event);
	}
	
	
	LinkedList<Float> tapPressures=new LinkedList<Float>();
	  
	
  public void onTouchDown(int id, float x,float y,float pressure){
	  if(pressureEnabled){
		  if(tapPressures.size()>=10){
			  int pos=(int)random(0, tapPressures.size());
			  //println("removing:"+pos);
			  tapPressures.remove(pos);
		  }
		  tapPressures.add(pressure);
		  Collections.sort(tapPressures);
	  }else{
		  pressure=1;
	  }
	  //println("original pressure:"+pressure+" min:"+tapPressures.getFirst()+" max:"+tapPressures.getLast());
	  pressure=map(pressure,tapPressures.getFirst(),tapPressures.getLast(),0.25f,1);
	  //println("end pressure:"+pressure);
	  mousePressed(id,(int)x, (int)y, pressure);		
  }
  
  public void onTouchUp(int id, float x,float y,float pressure){
	  mouseReleased(id, (int)x, (int)y, pressure);
  }	
  
  public void onTouchMove(int id, float x,float y,float px,float py,float pressure){
	  mouseDragged(id, (int)x, (int)y,(int)px, (int)py, pressure);	
  }	  
  
  public void onTap(TapEvent e){
	  //println("Detected tap on:("+e.getX()+","+e.getY()+")");
	  //sequencer.mousePressed(e.getX(),e.getY());
	  //sequencer.mouseReleased(e.getX(),e.getY());
  }
  
  public void onFlick(FlickEvent e){
	  println("Detected flick velocity:("+e.getVelocity().x+","+e.getVelocity().y+")");
  }  
  
  public void onDrag(DragEvent e){
	  //println("Detected drag dis:("+e.getDx()+","+e.getDy()+")");
  }
  
  public void onPinch(PinchEvent e){
	  //println("Detected pinch amount:"+e.getAmount());
	  switch (mode) {
	  case SEQUENCER:
		  sequencer.changeZoom((int) e.getCenterX(),(int)e.getCenterY(),e.getAmount());
		  break;
	  }

  }
  
  public void onRotate(RotateEvent e){
	  //println("Detected rotation angle:"+e.getAngle());
  }
    
  @Override public boolean onCreateOptionsMenu(Menu menu) {	  
	  
      super.onCreateOptionsMenu(menu);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.menu, menu);
      
      return true;
   }
  
   @TargetApi(Build.VERSION_CODES.HONEYCOMB)
   private void setupActionBar() {
	    // Make sure we're running on Honeycomb or higher to use ActionBar APIs
	   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {	 
		   int screenType=getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		   if (screenType == Configuration.SCREENLAYOUT_SIZE_NORMAL || screenType == Configuration.SCREENLAYOUT_SIZE_LARGE || screenType == Configuration.SCREENLAYOUT_SIZE_XLARGE){
			   ActionBar actionBar = getActionBar();
			   if(actionBar!=null){
				   actionBar.setTitle("DrumCloud");
				   //actionBar.setDisplayShowTitleEnabled(true);
				   //ViewGroup v = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.header, null);
				   actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME,
				            ActionBar.DISPLAY_SHOW_TITLE);
				   //actionBar.setCustomView(v,
					//	   new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
				      //              ActionBar.LayoutParams.WRAP_CONTENT,
				        //            Gravity.CENTER_VERTICAL | Gravity.RIGHT));
				   actionBar.show();
			   }
			   else
				   Log.e("ACTION BAR", "null Action Bar");
		   }
	   }
	}
   
   private static boolean showMenuBar=false;

   @TargetApi(Build.VERSION_CODES.HONEYCOMB)
   private void setupDefaultTheme() {
	    // Make sure we're running on Honeycomb or higher to use ActionBar APIs
	   if(true){
		   Log.i("DEVICE BUILD","SDK_INT:"+Build.VERSION.SDK_INT);
		   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {	 
			   int screenType=getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
			   Log.i("DEVICE SCREEN","screenType:"+screenType);
			   if (screenType == Configuration.SCREENLAYOUT_SIZE_NORMAL || screenType == Configuration.SCREENLAYOUT_SIZE_LARGE || screenType == Configuration.SCREENLAYOUT_SIZE_XLARGE){
				   setTheme(android.R.style.Theme_Holo);				   
				   showMenuBar=true;
				   setTitle("DrumCloud");
				   //recreate();
				
			   }
		   }
	   }
	}
   
   

   
   @Override
   protected void onResume() {
	// TODO Auto-generated method stub
	super.onResume();
   }

  	public boolean onPrepareOptionsMenu (Menu menu){
  		//MenuInflater inflater = getMenuInflater();
    	if(paused){
    		menu.getItem(0).setTitle(R.string.play);
    		menu.getItem(0).setIcon(android.R.drawable.ic_media_play);
    	}
    	else{
    		menu.getItem(0).setTitle(R.string.pause);
    		menu.getItem(0).setIcon(android.R.drawable.ic_media_pause);
    	}  		

  		return true;        
	}  
  
  public void onClick(View view) {


  }  
  
  ShowcaseView sv1,sv2,sv3,sv4,sv5,sv6,sv7,sv8;
  ShowcaseView.ConfigOptions co;
  boolean noShowMore=false;
  
  public void startHelpShowCase(){
	  
      co = new ShowcaseView.ConfigOptions();
      //co.hideOnClickOutside = true;
      sv1 = ShowcaseView.insertShowcaseView(barOriginX+barWidth*0.5f,barOriginY+barHeight*0.5f,
    		  this, R.string.helpDialogTitle1, R.string.helpDialogText1, co);
      sv1.setShowcaseIndicatorScale(0.3f);
      sv1.setOnShowcaseEventListener(this);
      sv1.overrideButtonClick(showcaseCustomClick(sv1));
  }
    
  public OnClickListener showcaseCustomClick(final ShowcaseView showcaseView){
	  return new OnClickListener() {		
			@Override
			public void onClick(View arg0) {
				noShowMore=true;
				showcaseView.setCustomButtomText(getString(R.string.close));
				showcaseView.hide();			
			}
	 };  
  }
  
  public void shareApp(){
	//create the send intent
	  Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);

	  //set the type
	  shareIntent.setType("text/plain");

	  //add a subject
	  shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.shareAppSubject));

	  String market="";

	  try {
		  ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(),PackageManager.GET_META_DATA);
		  Bundle bundle = ai.metaData;
		  market = bundle.getString("market");
	  } catch (Exception e) {
		  e.printStackTrace();
	  }

	  //build the body of the message to be shared		
	  String shareMessage="";
	  Resources res=getResources();
	  if(market.equalsIgnoreCase("amazon")){
		  shareMessage = String.format(res.getString(R.string.shareAppMessage), res.getString(R.string.amazonMarketWebLink));
	  }else if(market.equalsIgnoreCase("slideme")){
		  shareMessage = String.format(res.getString(R.string.shareAppMessage), res.getString(R.string.slidemeMarketWebLink));
	  }else{
		  shareMessage = String.format(res.getString(R.string.shareAppMessage), res.getString(R.string.playMarketWebLink));
	  }
	  //add the message
	  shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);

	  //start the chooser for sharing
	  startActivity(Intent.createChooser(shareIntent, getString(R.string.shareAppDialogTitle)));

  }
  
  
   @Override 
   public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
            /*case R.id.loadSamples:
            	if(mainMode!=loadMode)
            		mainMode=loadMode;
            	else
            		mainMode=normalMode;
            break;*/
            case R.id.help:
            	startHelpShowCase();
            break; 
            case R.id.shareApp:
            	shareApp();
            break;             
            case R.id.playPause:
            	toggleAudioPlayThread();
            break;    
            case R.id.midiSetup:
            	midi.showMidiTransportDialog();
            	break;
            case R.id.exportToMidi:
            	String fileName="drumcloud_"+DateFormat.getDateFormat(this).format(new Date());
            	fileName+="_"+DateFormat.getTimeFormat(this).format(new Date())+".midi";
            	fileName=fileName.replaceAll("\\\\", "-").replaceAll("/", "-");
            	File midiFile=new File(Environment.getExternalStorageDirectory().getPath()+"/drumcloud/midi/"+fileName);
            	MidiFileIO.save(midiFile,BPM,samplesPerBeat);
            	if(midiFile.exists() && midiFile.isFile()){
            		MidiFileIO.showMidiPostSaveDialog(this, midiFile);          		
            	}
            	break;
            case R.id.importFromMidi:
            	MidiFileIO.showMidiPreLoadDialog(this,BPM, samplesPerBeat);
            	break;            	
            case R.id.deleteAll:
                deleteAllSounds();
                break;
            case R.id.donate:
                startActivity(new Intent(this, DonationsActivity.class));
                break;
            case R.id.about:
            	 final Dialog dialog = new Dialog(this);
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
                	    	startActivity(i);
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
                break;                
            }
            return true;
   }


@Override
public void onShowcaseViewHide(ShowcaseView showcaseView) {
	
	if(noShowMore){
		noShowMore=false;
		return;
	}
	
	if(showcaseView==sv1){
		sv2 = ShowcaseView.insertShowcaseView(bpmSlider.getX()+bpmSlider.getW()*0.5f,bpmSlider.getY()+bpmSlider.getH()*0.5f, this,
				R.string.helpDialogTitle2, R.string.helpDialogText2, co);
		sv2.setShowcaseIndicatorScale(0.3f);
    	sv2.animateGesture(-bpmSlider.getW(), 0, bpmSlider.getW(), 0);
    	sv2.setOnShowcaseEventListener(this);
    	sv2.overrideButtonClick(showcaseCustomClick(sv2));
	}else if(showcaseView==sv2){
		sv3 = ShowcaseView.insertShowcaseView(loadButton.getX()+loadButton.getW()*0.5f,loadButton.getY()+loadButton.getH()*0.5f, this, 
				R.string.helpDialogTitle3, R.string.helpDialogText3, co); 
		sv3.setShowcaseIndicatorScale(0.3f);
	    sv3.animateGesture(0, width*0.05f, 0, 0);
	    sv3.setOnShowcaseEventListener(this);
	    sv3.overrideButtonClick(showcaseCustomClick(sv3));
	}else if(showcaseView==sv3){
		sv4 = ShowcaseView.insertShowcaseView(loadButton.getX()+loadButton.getW()*0.5f,loadButton.getY()+loadButton.getH()*0.5f, this,
				R.string.helpDialogTitle4, R.string.helpDialogText4, co);  
		sv4.setShowcaseIndicatorScale(0.3f);
	    sv4.setOnShowcaseEventListener(this);
	    sv4.overrideButtonClick(showcaseCustomClick(sv4));
	}else if(showcaseView==sv4){
		sv5 = ShowcaseView.insertShowcaseView(deleteButtons.getX()+deleteButtons.getW()*0.5f,deleteButtons.getY()+deleteButtons.getH()*0.5f, this, 
				R.string.helpDialogTitle5, R.string.helpDialogText5, co);  
		sv5.setShowcaseIndicatorScale(0.3f);
		sv5.animateGesture(0, width*0.05f, 0, 0);
	    sv5.setOnShowcaseEventListener(this);
	    sv5.overrideButtonClick(showcaseCustomClick(sv5));
	}else if(showcaseView==sv5){
		sv6 = ShowcaseView.insertShowcaseView(width*0.5f,height*0.27f, this,
				R.string.helpDialogTitle6, R.string.helpDialogText6, co);  
		sv6.setShowcaseIndicatorScale(1.0f);
		sv6.animateGesture(width*0.05f, -height*0.05f, width*0.05f, height*0.09f);
	    sv6.setOnShowcaseEventListener(this);
	    sv6.overrideButtonClick(showcaseCustomClick(sv6));
	}else if(showcaseView==sv6){
		sv7 = ShowcaseView.insertShowcaseView(width*0.5f,height*0.71f, this,
				R.string.helpDialogTitle7, R.string.helpDialogText7, co);  
		sv7.setShowcaseIndicatorScale(1.7f);
		sv7.animateGesture(width*0.05f, height*0.05f, width*0.05f, height*0.15f);
	    sv7.setOnShowcaseEventListener(this);
	    sv7.overrideButtonClick(showcaseCustomClick(sv7));
	}
}

@Override
public void onCreate(Bundle savedInstanceState) {
	setupDefaultTheme();
	X = this;
    super.onCreate(savedInstanceState);
    //setContentView(R.layout.main);
    println("ON CREATE DRUMCLOUD");
 
    //setupActionBar();
  	
}

@Override
public void onBackPressed() {
	Log.d("onBackPressed","onBackPressed");
    new AlertDialog.Builder(this)
        .setTitle(R.string.sureToExitTitle)
        .setMessage(R.string.sureToExitMessage)
        .setNegativeButton(android.R.string.no, null)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int which) {
        		if(audioPlayThread.isAlive()){
        			audioPlayThread.quit();
        		} 
                DrumCloud.super.onBackPressed();
            }
        }).create().show();
}

@Override
public void onShowcaseViewShow(ShowcaseView showcaseView) {
	// TODO Auto-generated method stub
	
}  
  
}
