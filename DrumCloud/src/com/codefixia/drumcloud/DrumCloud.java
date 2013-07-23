package com.codefixia.drumcloud;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import themidibus.*; 
import java.io.FileFilter; 
import java.util.regex.Pattern; 
import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileNotFoundException; 
import java.io.IOException; 
import java.io.BufferedInputStream; 
import java.net.MalformedURLException; 
import java.util.*; 
import java.net.URL; 
import android.app.Activity; 
import android.os.Bundle; 
import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;
import android.media.*; 
import android.media.audiofx.Visualizer; 
import android.content.Intent;
import android.content.res.AssetFileDescriptor; 
import android.hardware.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.util.LinkedList;
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

import org.donations.DonationsActivity;
import org.json.*; 
import org.json.JSONArray;
import org.json.JSONObject;

import com.codefixia.googledrive.DownloadFile;
import com.codefixia.multitouch.DragEvent;
import com.codefixia.multitouch.FlickEvent;
import com.codefixia.multitouch.MultitouchHandler;
import com.codefixia.multitouch.PinchEvent;
import com.codefixia.multitouch.RotateEvent;
import com.codefixia.multitouch.TapEvent;
import com.codefixia.multitouch.TouchProcessor;


public class DrumCloud extends PApplet {

	public static Activity activity;
	private static int soundsLoaded=0;


Midi midi;

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
boolean liveMode=false;
boolean sequencerMode=false;
boolean pressureEnabled=true;
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

float filterFrequency=11025.0f, filterResonance=0.5f, delayTime=0, delayFeedback=0, speed=1.0f, speed1=1.0f, volumeKick=1.0f, volumeBass=1.0f, volume=1.0f;

int candyPink=color(247, 104, 124);
int redColor=0xffB90016;
int orangeColor=0xffC18900;
int blueColor=0xff5828A3;
int blueDarkColor=0xff380883;
int blueLightColor=0xff380883;
int greenColor=0xff22A300;
int darkGreyColor=color(50);
int mediumGreyColor=color(127);
int lightGreyColor=color(200);
int yellowColor=color(100, 100, 0);

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
ToggleButton loadButton, playButton, deleteButton;

float valueX=0.0f, valueY=0.0f;
float buttonSize, buttonsOriginX, buttonsOriginY, buttonMarginX, buttonMarginY;
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
int trackMode=ALL;

final static boolean isAndroidDevice=true;

SelectLibrary files;

public void setupAndroid() {
  files = new SelectLibrary(this);
  files.filterExtension=".wav;.json;.aiff;.aif";
  activity = (Activity)this;
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
}

public void setupPowerSpectrum() {
  //if (AndroidUtil.numCores()>1) {
  for (int i=0;i<playerKick.length;i++) {
    playerKick[i].setAnalysing(true);
    playerBass[i].setAnalysing(true);
    playerSnare[i].setAnalysing(true);
    playerHitHat[i].setAnalysing(true);
  }
  //}
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
  FontAdjuster.width=width;
  if (AndroidUtil.numCores()>1) {
    audioPlayThread=new AudioPlayThread(this, 1, "AudioPlayThread");
    audioPlayThread.start();
  }
  else {
    //frameRate(10);    
    liveMode=true;
  }
  background(0);
  maxim = new Maxim();
  //maxim = new Minim(this);
  for (int i=0;i<playerKick.length;i++) {
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
  }
  //player.volume(1.0);
  backTopMachine=loadImage("MPD26_mod.png");
  //lcdFont = loadFont("lcd.ttf");
  rectMode(CORNER);
}

public void setupMidi() {
  midi=new Midi(this);
}

public void changeBPM(float newBPM) {
  BPM=newBPM;
  beatMS=60000.0f/BPM;
  beatsPerTempo=4.0f;
  tempoMS=beatsPerTempo*beatMS;
}

public void noteOn(int channel, int pitch, int velocity) {
  midi.noteOff(channel, pitch, velocity);
  int soundPlayer=(pitch-24)%6;
  int soundNumber=(pitch-24)/6;
  int soundType=soundPlayer+soundNumber*4;  
  println("type:"+soundType+" player:"+soundPlayer+" number:"+soundNumber);
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
  midi.noteOff(channel, pitch, velocity);
}

public void controllerChange(int channel, int number, int value) {
  midi.controllerChange(channel, number, value);
  if (channel==15) {
    if (number==0) {
      speed=map(value, 0, 127, 0, 2);
      filterFrequency=map(value, 0, 127, 0, 10000);
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
    barOriginY=height*0.05f;
}

public void setupPadButtons() {
  buttonSize=width*0.153f;
  buttonsOriginX=width*0.131f;
  buttonMarginX=buttonSize*0.29f;
  buttonMarginY=buttonSize*0.275f;

  if (isAndroidDevice) {
    buttonsOriginY=height*0.45f;
  }
  else {
    buttonsOriginY=height*0.445f;
  }
}

public void setupSliders() {
  sliderWidth=29.0f*(width*0.14f/44.0f);//width*0.12;
  sliderHeight=29.0f*(height*0.06f/44.0f);//width*0.025;
  sliderMarginX=width*0.142f;
  sliderOriginX=width*0.10f;
  sliderImage=loadImage("slider.png");

  if (isAndroidDevice) {
    sliderMinY=height*0.20f;
    sliderMaxY=height*0.33f;
  }
  else {
    sliderMinY=height*0.162f;
    sliderMaxY=height*0.33f;
  }
}

public void toggleAudioPlayThread() {

  if (liveMode) {
    audioPlayThread= new AudioPlayThread(this, 1, "AudioPlayThread");
    audioPlayThread.start();
  }
  else {
    pausedMS=millis();
    if (audioPlayThread.running)
      audioPlayThread.quit();
  }

  liveMode=!liveMode;
}

//synchronized 
public void proccessTempoVars() {

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

  if (!playedGrids[currentGrid]) {
    //println("Playing grid:"+currentGrid);
    playedGrids[currentGrid]=true;
    for (int i=0;i<totalSamples;i++) {
      if (samplesPerBeat[i][currentGrid]) {
        playSoundType(i,1);
        float dif=((millis()%tempoMS)-(currentGrid*gridMS));
        if (dif>maxDif)maxDif=dif;
        //println("Estimated play offset:"+(currentGrid*gridMS)+" real play offset:"+(millis()%tempoMS)+" dif:"+dif+" maxDif:"+maxDif);  
        println("Dif:"+dif+" maxDif:"+maxDif);
      }
    }
    audioPlayThread.wait=(int)gridMS-10;
  }
  else {
    audioPlayThread.wait=1;
  }
}

public void setupTopControls() {
  bpmSlider=new HorizontalSlider(barOriginX, barOriginY+barHeight*2.0f, sliderWidth, buttonSize*0.5f);
  bpmSlider.limitX(barOriginX, barOriginX+barWidth*0.25f);
  bpmSlider.valuesX(80, 160, 120);
  bpmSlider.text="BPM";

  playButton=new ToggleButton(barOriginX+barWidth*0.368f, barOriginY+barHeight*2.0f, buttonSize*0.5f, buttonSize*0.5f);
  playButton.text="II";
  playButton.activatedText=">";
  playButton.fillColor=color(150, 150, 0);
  //playButton.blinkWhenOn=true;  

  loadButton=new ToggleButton(barOriginX+barWidth*0.62f-buttonSize*0.6f, barOriginY+barHeight*2.0f, buttonSize*1.2f, buttonSize*0.5f);
  loadButton.text="LOAD";
  loadButton.fillColor=color(150, 150, 0);
  loadButton.blinkWhenOn=true;

  deleteButtons=new ExpandableButtons(barOriginX+barWidth-buttonSize*1.2f, barOriginY+barHeight*2.0f, buttonSize*1.2f, buttonSize*0.5f);
  deleteButtons.text="DELETE";
  deleteButtons.fillColor=yellowColor;
  String[] buttons = { 
    "KICK", "BASS", "SNARE", "HITHAT", "ALL"
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
  for (int i=0;i<totalSamples;i++) {
    for (int j=0;j<32;j++) {
      if (samplesPerBeat[i][j]) {
        fill(getColorSoundType(i));
        float cOffset=gridWidth*j;
        rect(barOriginX+cOffset, barOriginY, gridWidth, barHeight);
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
  rect(barOriginX, buttonsOriginY-buttonSize*0.2f, barWidth, buttonSize*5.295f);
}

public void drawPadButtons(int c, ClickablePad[] padArray, float buttonsOriginX, float buttonsOriginY, float buttonSize, float buttonMargin, String buttonText) {
  strokeWeight(3);
  stroke(100, 100, 100, 255);
  for (int i=0;i<padArray.length;i++) {
    if (padArray[i]==null)
      padArray[i]=new ClickablePad(buttonsOriginX+(buttonSize+buttonMargin)*i, buttonsOriginY, buttonSize, buttonSize);
    padArray[i].text=buttonText+i;
    padArray[i].fillColor=c;
    if (mainMode==loadMode) {
      if (loadButton.blinkOn) {
        padArray[i].strokeColor=color(255);
      }
      else {
        padArray[i].strokeColor=color(100);
      }
    }
    else {
      padArray[i].strokeColor=color(100);
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
    if (sliders[i]==null) {
      sliders[i]=new VerticalSlider(sliderOriginX+(sliderMarginX*i), sliderMinY+(sliderMaxY-sliderMinY)*0.5f, sliderWidth, sliderHeight);
      sliders[i].limitY(sliderMinY, sliderMaxY);      
      if (i>2) {
        sliders[i].y=sliderMinY;
      }
    }
    sliders[i].rollover(mouseX, mouseY);
    sliders[i].dragVertically(mouseY);    
    //image(sliderImage, sliders[i].x, sliders[i].y, sliders[i].w, sliders[i].h);
    //sliders[i].debugDisplay();
    sliders[i].draw();
  }
  sliders[0].text="FILTER\nFREQ";
  sliders[1].text="FILTER\nRES";
  sliders[2].text="PITCH\nSPEED";
  sliders[3].text="KICK\nVOL";
  sliders[4].text="BASS\nVOL";
  sliders[5].text="MAIN\nVOL";
}

public void drawFiltersZone() {

  fill(255);
  rect(width*0.05f, height*0.5f, width*0.9f, height*0.4f); 
  stroke(255);
  textSize(10);

  if (panelModeButton==null)
    panelModeButton=new Clickable(width*0.1f, height*0.51f, width*0.2f, height*0.025f);
  panelModeButton.fillColor=redColor;   
  panelModeButton.drawState();

  fill(0); 
  text("[PANEL MODE] ["+panelMode+"]", width*0.2f, height*0.525f);

  fill(orangeColor);
  if (trackModeButton==null)
    trackModeButton=new Clickable(width*0.5f, height*0.51f, width*0.2f, height*0.025f);
  panelModeButton.fillColor=orangeColor;   
  panelModeButton.drawState();    

  fill(0);
  text("[TRACK MODE] ["+trackMode+"]", width*0.6f, height*0.525f);

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
  for (int i=0;i<playerKick.length;i++) {
    drawSpectrumOf(playerKick[i], redColor, i);
    drawSpectrumOf(playerBass[i], orangeColor, i+4);
    drawSpectrumOf(playerSnare[i], blueColor, i+8);
    drawSpectrumOf(playerHitHat[i], greenColor, i+12);
  }
}

public void drawSpectrumOf(AudioPlayer audioPlayer, int c, int soundType) {
  if (audioPlayer.isPlaying) {
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
        if (sequencerMode) {
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
        sequencerMode=!sequencerMode;
      } 

      if (render3D) {
        float xPos=map(ms, halfTime, animationEnd, width, 0);  
        float zPos=map(ms, halfTime, animationEnd, 1500, 0);
        float rot=map(ms, halfTime, animationEnd, PI, 0);          
        translate(xPos, 0, -zPos);        
        rotateY(rot);
      }
      else {
        if (sequencerMode) {
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

  animateTransition(1000,0,90);

  if (!sequencerMode) {
    drawTempoBar();
    drawPadsContainer();
    drawKickButtons();
    drawBassButtons();
    drawSnareButtons();
    drawHitHatButtons();
    drawSliders();
    drawPowerSpectrum();
    drawTopControls();
  }
  else {
    sequencer.draw();
  }
}


public void controlVolume(float volume, int trackMode) {
  if (trackMode==KICK || trackMode==ALL) {
    for (int i=0;i<playerKick.length;i++)   
      playerKick[i].volume(volume);
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

public void controlPitch(float speed, int trackmode) {

  if (trackMode==KICK || trackMode==ALL) {   
    for (int i=0;i<playerKick.length;i++) 
      playerKick[i].speed(speed);
  }
  if (trackMode==BASS || trackMode==ALL) {
    for (int i=0;i<playerBass.length;i++)
      playerBass[i].speed(speed);
  }
  if (trackMode==SNARE || trackMode==ALL) {
    for (int i=0;i<playerSnare.length;i++)
      playerSnare[i].speed(speed);
  }
  if (trackMode==HITHAT || trackMode==ALL) {
    for (int i=0;i<playerHitHat.length;i++)
      playerHitHat[i].speed(speed);
  }
}

public void proccessPanel() {
  valueX=constrain(map(mouseX, width*0.05f, width*0.9f, 0.0f, 1.0f), 0.0f, 1.0f);
  valueY=constrain(map(mouseY, height*0.5f, height*0.95f, 0.0f, 1.0f), 0.0f, 1.0f);   
  switch(panelMode) {
  case FILTER:
    filterFrequency=map(valueY, 0.0f, 1.0f, 0.0f, 5000);
    filterResonance=map(valueX, 0.0f, 1.0f, -1.0f, 1.0f);  
    controlFilter(filterFrequency, filterResonance, trackMode);
    break;
  case ECHO:
    delayTime=map(valueX, 0.0f, 1.0f, 0.0f, 3000);
    delayFeedback=map(valueY, 0.0f, 1.0f, 0.0f, 100.0f); 
    if (trackMode==KICK || trackMode==ALL) {
      for (int i=0;i<playerKick.length;i++) {
        playerKick[i].setDelayTime(delayTime);
        playerKick[i].setDelayFeedback(delayFeedback);
      }
    }
    if (trackMode==BASS || trackMode==ALL) {
      for (int i=0;i<playerBass.length;i++) {
        playerBass[i].setDelayTime(delayTime);
        playerBass[i].setDelayFeedback(delayFeedback);
      }
    }
    if (trackMode==SNARE || trackMode==ALL) {
      for (int i=0;i<playerSnare.length;i++) {
        playerSnare[i].setDelayTime(delayTime);
        playerSnare[i].setDelayFeedback(delayFeedback);
      }
    }
    if (trackMode==HITHAT || trackMode==ALL) {
      for (int i=0;i<playerHitHat.length;i++) {
        playerHitHat[i].setDelayTime(delayTime);
        playerHitHat[i].setDelayFeedback(delayFeedback);
      }
    }       
    break;
  case PITCH:
    speed=map(valueX, 0.0f, 1.0f, 0.0f, 2.0f);
    controlPitch(speed, trackMode);
    break;
  }
}


public void mouseMoved()
{
  if (!sequencerMode) {

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
  }
  else {
    sequencer.mouseMoved();
  }
}

public void mouseDragged()
{

  if (!sequencerMode) {
    if (deleteButtons.isSelected(mouseX, mouseY)) {
    }  

    for (int i=0;i<sliders.length;i++) {
      if (sliders[i].dragging) {
        float valY=constrain(map(sliders[i].y, sliderMinY, sliderMaxY, 1.0f, 0.0f), 0.0f, 1.0f);    
        switch(i) {
        case 0:
          filterFrequency=map(valY, 0.0f, 1.0f, 0.0f, 10000);
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
          controlVolume(volumeKick, 1);      
          break;
        case 4:
          volumeBass=valY;
          controlVolume(volumeBass, 2);      
          break;
        case 5:
          volume=valY;
          controlVolume(volume, 0);
          break;
        }
        //println("frequency:"+filterFrequency+" resonance:"+filterResonance+" speed:"+valY*2);
      }
    }
  }
  else {
    sequencer.mouseDragged();
  }
}

public void mouseReleased() {

  if (!sequencerMode) {

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
        animating=true;
        //sequencerMode=!sequencerMode;
      //deleteSoundOfGroup(index);
    }
    loadButton.stopClick();
    playButton.stopClick();
  }
  else {
    sequencer.mouseReleased();
  }
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
  sequencer.updateTracksState();
}

public void deleteSoundOfGroup(int soundGroup) {
  println("Deleting sound group:"+soundGroup);
  for (int i=0;i<totalSamples;i+=4) {
    deleteSoundType(i+soundGroup);
  }
  sequencer.updateTracksState();
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

public void fileSelected(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } 
  else {
    println("User selected " + selection.getAbsolutePath()+" name:"+selection.getName());
    if (selection.getName().endsWith("json")) {
    	JSONArray sounds=loadJsonSoundPack(selection);
    	final boolean toggled;
    	if(!liveMode){
    		toggled=true;
    		toggleAudioPlayThread();
    	}else{
    		toggled=false;
    	}
    	final ProgressDialog progressDialog= new ProgressDialog(DrumCloud.activity);;
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
    	    				DrumCloud.activity.runOnUiThread(new Runnable() {
    	    					@Override
    	    					public void run() {
    	    	    				if(progressDialog!=null){
    	    	    					progressDialog.setMessage("Reading sound:"+fileName);
    	    	    					progressDialog.setProgress(soundsLoaded);
    	    	    					if(soundsLoaded==16){
    	    	    						progressDialog.dismiss();
    	    	    					    if(toggled)
    	    	    					    	toggleAudioPlayThread();    	    	    						
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
      }
    }
  }
  mainMode=normalMode;
  loadButton.ON=false;
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
			/*
         //loadPlayer.volume(1.0);
     //loadPlayer.cue(0);
     //playerKick[0].play();
     playerKick[0] = null;   
     playerKick[0] = loadPlayer;*/
		}
		else {
			println("loaded Player is null");
		}    
	}
}

/*
public void mousePressed(){
	mousePressed(mouseX,mouseY,1.0);
}
*/
public void mousePressed(int mouseX,int mouseY,float pressure)
{
  if (!sequencerMode) {
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
      if (loadButton.ON) {
        mainMode=loadMode;
      }
      else {
        mainMode=normalMode;
      }
    } 
    if (playButton.isClicked(mouseX, mouseY)) {
      toggleAudioPlayThread();
    }
  }
  else {
    sequencer.mousePressed();
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

public void playSoundType(int soundType,float pressure) {
  AudioPlayer ap=getPlayerBySoundType(soundType);
  println("Using press:"+pressure);
  ap.volume(pressure);  
  ap.cue(0);
  ap.play();
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
  if (!liveMode) {
    samplesPerBeat[soundType][currentGrid]=!samplesPerBeat[soundType][currentGrid];
    println("changed sound:"+soundType+" at position:"+currentGrid);
  }
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




static class AndroidUtil{

  private static int numCores=-1;
  
  public static int numCores(){
    if(numCores!=-1)
      return numCores;
    else{
      numCores=getNumCores();
      return numCores;
    }
  }
  
/**
 * Gets the number of cores available in this device, across all processors.
 * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
 * @return The number of cores, or 1 if failed to get result
 */
private static int getNumCores() {
  
    //Private Class to display only CPU devices in the directory listing
    class CpuFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            //Check if filename is "cpu", followed by a single digit number
            if(Pattern.matches("cpu[0-9]", pathname.getName())) {
                return true;
            }
            return false;
        }      
    }

    try {
        //Get directory containing CPU info
        File dir = new File("/sys/devices/system/cpu/");
        //Filter to only list the devices we care about
        File[] files = dir.listFiles(new CpuFilter());
        //Return the number of cores (virtual CPU devices)
        return files.length;
    } catch(Exception e) {
        //Default to return 1 core
        return 1;
    }
}

}
class AudioPlayThread extends Thread {

  private DrumCloud drumMachine;  
  
  boolean running;           // Is the thread running?  Yes or no?
  int wait;                  // How many milliseconds should we wait in between executions?
  String id;                 // Thread name
  long millis;
  long count;                // counter
 
  public long getCount() {
    return count;
  }
 
  public long getMillis() {
    return millis;
  }
 
  // Constructor, create the thread
  // It is not running by default
  AudioPlayThread (DrumCloud d,int w, String s) {
    drumMachine=d;
    wait = w;
    running = false;
    id = s;
    count = 0;
  }

// Overriding "start()"
  public void start () {    
    
    // Set running equal to true
    running = true;
    // Print messages
    println("Starting thread (will execute every " + wait + " milliseconds.)"); 
    // Do whatever start does in Thread, don't forget this!
    super.start();
  }
 
 
   // We must implement run, this gets triggered by start()
  public void run () {
    while (running) {
      millis=millis();
      try{
      drumMachine.proccessTempoVars();
      }catch(Exception ex){
        println("Exception on proccessTempoVars:"+ex.toString());
      }
      count++;
      // Ok, let's wait for however long we should wait
      if(wait>0){
        try {
          sleep((long)(wait));
        } catch (Exception e) {
        }
      }
    }
    System.out.println(id + " thread is done!");  // The thread is done when we get to the end of run()
  }
  
  // Our method that quits the thread
  public void quit() {
    System.out.println("Quitting."); 
    running = false;  // Setting running to false ends the loop in run()
    // IUn case the thread is waiting. . .
    interrupt();
  }  

}
public class Clickable {
  boolean clicked = false; // Is the object being clicked?
  boolean overed = false;
  
  int strokeWeight=3;
 
  float x,y,w,h;          // Location and size
  float offsetX, offsetY; // Mouseclick offset
  int fillColor=color(127),strokeColor=color(100);

  public Clickable(float tempX, float tempY, float tempW, float tempH) {
    x = tempX;
    y = tempY;
    w = tempW;
    h = tempH;
    offsetX = 0;
    offsetY = 0;
  }

  public void drawState(){
    if(clicked){
      drawClicked();
    }else if(overed){
      drawOvered();
    }else{
      drawNormal();
    }
  }
  
  public void draw(){
    this.drawNormal();
  }
  
  public void draw(int fillColor){
    this.fillColor=fillColor;
    this.drawNormal();
  }
  
  public void draw(int fillColor,int strokeColor){
    this.fillColor=fillColor;
    this.strokeColor=strokeColor;
    this.drawNormal();
  }
  
  public void drawNormal(){
    stroke(strokeColor);
    fill(red(fillColor),green(fillColor),blue(fillColor),200);  
    rect(x,y,w,h);
    drawCommonContent();
  }

  public void drawCommonContent(){
  }   
  
  public void drawOvered(){
    stroke(150);
    fill(red(fillColor)*0.7f,green(fillColor)*0.7f,blue(fillColor)*0.7f,alpha(fillColor));
    rect(x,y,w,h);
    drawCommonContent();    
  }
  
  public void drawClicked(){
    //stroke(red(strokeColor)*0.8,green(strokeColor)*0.8,blue(strokeColor)*0.8,alpha(strokeColor)*0.8);
    stroke(200);
    fill(red(fillColor)*0.5f,green(fillColor)*0.5f,blue(fillColor)*0.5f,alpha(fillColor));
    rect(x,y,w,h);
    drawCommonContent();    
  }


  // Is a point inside the rectangle (for click)?
  public boolean isClicked(int mx, int my) {
    if (isOver(mx,my)) {
      clicked = true;
      // If so, keep track of relative location of click to corner of rectangle
      offsetX = x-mx;
      offsetY = y-my;
    }else{
      clicked =false;
    }
    
    return clicked;
  }
  
  public boolean isOver(int mx, int my) {
    if (mx > x && mx < x + w && my > y && my < y + h) {
      overed=true;
    }else{
      overed=false;
    }
    return overed;
  }

  public void stopClick() {
    clicked=false;
  }  
 

}
public class ClickablePad extends Clickable{
  
  String text="";
  
  public ClickablePad(float tempX, float tempY, float tempW, float tempH) {
    super(tempX,tempY,tempW,tempH);
  }
  
  public void draw(){
    fill(red(fillColor),green(fillColor),blue(fillColor),100);   
    rect(x, y, w, h);     
  }
  
  public void drawCommonContent(){
    textSize(FontAdjuster.getSize(10));
    textAlign(CENTER);
    fill(200);
    text(text, x+(w*0.15f), y-(h*0.03f));   
  }

}
// Click and Drag an object
// Daniel Shiffman 

// A class for a draggable thing

class Draggable {
  boolean dragging = false; // Is the object being dragged?
  boolean rollover = false; // Is the mouse over the ellipse?
  
  float x,y,w,h;          // Location and size
  float offsetX, offsetY; // Mouseclick offset
  float minX,minY,maxX,maxY;
  boolean limitedX=false,limitedY=false;
  float expandedFactor=1.0f;
  boolean slideOnClick=true;
  float minXZone,minYZone,maxXZone,maxYZone;

  Draggable(float tempX, float tempY, float tempW, float tempH) {
    x = tempX;
    y = tempY;
    w = tempW;
    h = tempH;
    offsetX = 0;
    offsetY = 0;
    calculateClickZone();
  }
  
  public void limitY(float min,float max){
    limitedY=true;
    minY=min;
    maxY=max;
    calculateClickZone();
  }
  
  public void limitX(float min,float max){
    limitedX=true;
    minX=min;
    maxX=max;
    calculateClickZone();    
  }  

  public void calculateClickZone(){
    float xExp=(expandedFactor-1.0f)*w;
    float yExp=(expandedFactor-1.0f)*h;
    
    if(limitedX){
      minXZone=minX-(xExp*0.5f);
      maxXZone=maxX+(xExp*0.5f)+w;
    }else{
      minXZone=x-(xExp*0.5f);
      maxXZone=x+w+(xExp*0.5f);    
    }
    if(limitedY){
      minYZone=minY-(yExp*0.5f);
      maxYZone=h+maxY+(yExp*0.5f);    
    }else{
      minYZone=y-(yExp*0.5f);
      maxYZone=h+y+(yExp*0.5f);
    }  
  }

  // Method to display
  public void debugDisplay() {
    stroke(255);
    //if(expandedFactor!=1.0){
      fill(255,0,0);
      rect(minXZone,minYZone,maxXZone-minXZone,maxYZone-minYZone);    
    //}
    
    if (dragging) fill (50);
    else if (rollover) fill(100);
    else fill(175,200);
    rect(x,y,w,h);
  }
  
  public void draw() {
    strokeWeight(3);
    fill(0,0,0,0.5f);
    rect(minXZone,minYZone,maxXZone-minXZone,maxYZone-minYZone);    
    stroke(200);
    if (dragging) fill (50);
    else if (rollover) fill(100);
    else fill(175,200);
    rect(x,y,w,h);
  }  

  // Is a point inside the rectangle (for click)?
  public void clicked(int mx, int my) {
    calculateClickZone();
    //println("zone (X:"+minXZone+" to "+maxXZone+" Y:"+minYZone+" to "+maxYZone+",) mouse:("+mx+","+my+")");
    if (mx > minXZone && mx < maxXZone && my > minYZone && my < maxYZone) {
    //if (mx > x && mx < x + w && my > y && my < y + h) {
      dragging = true;
      // If so, keep track of relative location of click to corner of rectangle
      offsetX = x-mx;
      offsetY = y-my;
      if(slideOnClick){
        if(limitedY)y=my-h*0.5f;
        if(limitedX)x=mx-w*0.5f;
        intoLimits();
        println("moved to x:"+x+" y:"+y);
      }    
    }
  }
  
  public void intoLimits(){
    if(limitedY){
      if(y<minY)
        y=minY;
      else if(y>maxY)
         y=maxY;   
    }
    if(limitedX){
      if(y<minX)
        x=minX;
      else if(y>maxX)
         x=maxX;   
    }    
  }
  
  // Is a point inside the rectangle (for rollover)
  public void rollover(int mx, int my) {
    if (mx > minXZone && mx < maxXZone && my > minYZone && my < maxYZone) {
    //if (mx > x && mx < x + w && my > y && my < y + h) {
      rollover = true;
    } else {
      rollover = false;
    }
  }

  // Stop dragging
  public void stopDragging() {
    dragging = false;
  }
  
  // Drag the rectangle
  public void drag(int mx, int my) {
    if (dragging) {
      if(!limitedX || (mx + offsetX>=minX && mx + offsetX<=maxX))
        x = mx + offsetX;
      if(!limitedY || (my + offsetY>=minY && my + offsetY<=maxY))
        y = my + offsetY;
    }
  }
  
  public void dragVertically(int my) {
    if (dragging) {
      if(!limitedY)
        y = my + offsetY;
      else if(my + offsetY<minY)
        y = minY;
      else if(my + offsetY>maxY)
        y = maxY;
      else y = my + offsetY;
    }
  }
  
  public void dragHorizontally(int mx) {
    if (dragging) {
      if(!limitedX)
        x = mx + offsetX;
      else if(mx + offsetX<minX)
        x = minX;
      else if(mx + offsetX>maxX)
        x = maxX;
      else x = mx + offsetX;
    }
  }  

}
static class DrumMachine {

  static boolean isAndroidDevice=true;

}
public class ExpandableButtons extends Clickable {

  final short UP=1;
  final short DOWN=2;
  final short LEFT=3;
  final short RIGHT=3;

  private float showTime;
  private float hideTime;

  float animationTime=0.25f;

  private String otherButtonsTexts[];
  private ExpandedButton otherButtonsPads[];
  private boolean showOtherButtons=false;

  short direction=DOWN;  

  String text="";

  public ExpandableButtons(float tempX, float tempY, float tempW, float tempH) {
    super(tempX, tempY, tempW, tempH);
  }

  public void draw() {
    println("YES");
    if(showOtherButtons)
      drawOtherButtons();
    fill(red(fillColor), green(fillColor), blue(fillColor), 100);   
    rect(x, y, w, h);
  }

  public void drawCommonContent() {
    //println("x:"+x+" y:"+y+" w:"+w+" h:"+h);
    textSize(FontAdjuster.getSize(20));
    textAlign(CENTER);
    fill(200);
    text(text, x+(w*0.5f), y+(h*0.7f));
    if(showOtherButtons)
      drawOtherButtons();    
  }

  public void setOtherButtons(String[] texts) {
    if (texts.length>0) {
      otherButtonsTexts=texts;
      otherButtonsPads=new ExpandedButton[texts.length];
      for (int i=0;i<otherButtonsTexts.length;i++) {
        if (direction==UP || direction==DOWN) {
          otherButtonsPads[i]=new ExpandedButton(x, y+(i+1)*((direction==DOWN?h:-h)+strokeWeight), w, h);
        }else{
          otherButtonsPads[i]=new ExpandedButton(x+(i+1)*((direction==RIGHT?w:-w)+strokeWeight), y, w, h);
        }
        otherButtonsPads[i].fillColor=fillColor;
        otherButtonsPads[i].text=texts[i];
      }
    }
  }
  
  public void setOtherButtonColors(int[] colors) {
    if (otherButtonsPads.length>0) {
      for (int i=0;i<otherButtonsPads.length;i++) {
        if(otherButtonsPads[i]!=null && colors.length>i){
          otherButtonsPads[i].fillColor=colors[i]; 
        }else{
          otherButtonsPads[i].fillColor=fillColor;
        }
      }
    }
  }  

  public void drawOtherButtons(){
      for (int i=0;i<otherButtonsTexts.length;i++) {
        otherButtonsPads[i].drawState();
      }
  }

  public boolean isClicked(int mx, int my) {
    if (isOver(mx, my)) {
      clicked = true;
      offsetX = x-mx;
      offsetY = y-my;
      showTime=millis();
      showOtherButtons();
    }
    else {
      clicked =false;
    }

    return clicked;
  } 

  public void showOtherButtons() {
    if (otherButtonsTexts!=null && otherButtonsTexts.length>0) {
      showOtherButtons=true;
      for (int i=0;i<otherButtonsTexts.length;i++) {
      }
    }
  }

  public void hideOtherButtons() {
    showOtherButtons=false;
  }

  public boolean isSelected(int mx, int my) {
    if (mx > x && mx < x + w && my > y && my < y + h) {
      overed=true;
    }else{
      for (int i=0;i<otherButtonsPads.length;i++) {
          otherButtonsPads[i].isOver(mx,my);
      }      
      overed=false;
    }
    return overed;
  }  

  public int buttonSelectedAt(int mx, int my) {
    int index=-1;
    if (clicked) {
      println("Released expandable button at mx:"+mx+" my:"+my);
      for (int i=0;i<otherButtonsPads.length;i++) {
        if(otherButtonsPads[i].isClicked(mx,my)){
          otherButtonsPads[i].stopClick();
          println("Clicked expanded button at:"+i);
          index=i;
        }
      }
    }
    hideOtherButtons();
    clicked = false;
    return index;
  }
}

public class ExpandedButton extends Clickable{
  
  String text="";
  
  public ExpandedButton(float tempX, float tempY, float tempW, float tempH) {
    super(tempX,tempY,tempW,tempH);
  }
  
  public void draw(){
    fill(red(fillColor),green(fillColor),blue(fillColor),100);   
    rect(x, y, w, h);     
  }
  
  public void drawCommonContent() {
    textSize(FontAdjuster.getSize(20));
    textAlign(CENTER);
    fill(200);
    text(text, x+(w*0.5f), y+(h*0.7f));    
  }

}
public static class FontAdjuster {

  static int width=480;

  public static int getSize(int defaultSize){
    
    if(width==480){
      return defaultSize;
    }else{
      float ratio=width/480.0f;
      defaultSize=(int)round(defaultSize*ratio);
      return defaultSize;
    }
  
  }

}
class HorizontalSlider extends Draggable{

  float divisions=10;
  float divWidth=0.0f;
  int outStroke=3;
  String text="";
  float minValue=0.0f,maxValue=1.0f,defaultValue=0.5f;
  
  HorizontalSlider(float x, float y, float w, float h) {
    super(x,y,w,h);
    //expandedFactor=1.2;
  }
  
  public void limitX(float min,float max){
    limitedX=true;
    minX=min;
    maxX=max;
    calculateClickZone();
    divWidth=(maxXZone-minXZone)/divisions;
    println("totalW:"+(maxXZone-minXZone)+" divWidth:"+divWidth);    
  }
 
  public void valuesX(float min,float max,float def){
    minValue=min;
    maxValue=max;
    defaultValue=def;
    x=map(def,minValue,maxValue,minX,maxX);   
  }
 
  public float normalizedValue(){
    return map(x, minX, maxX, 0, 1);  
  }
  
  public float value(){
    return map(x, minX, maxX, minValue, maxValue);  
  }  
  
  public void draw() {
    
    strokeWeight(outStroke);
    stroke(100,100,100,255);
    fill(map(normalizedValue(),0,1,50,127),255);
    rect(minXZone,minYZone,maxX-minX+w,maxYZone-minYZone);
    strokeWeight(2);
    stroke(255,255,255,255);
    for(int i=1;i<divisions;i++){
      float xPos=minXZone+i*divWidth;
      if(i%2==0){
        line(xPos,minYZone+outStroke,xPos,minYZone+h*0.4f);
        line(xPos,maxYZone-h*0.4f,xPos,maxYZone-outStroke);     
      }
      else{
        line(xPos,minYZone+outStroke,xPos,minYZone+h*0.3f);
        line(xPos,maxYZone-h*0.3f,xPos,maxYZone-outStroke);     
      }
    }  
    strokeWeight(outStroke);  
    stroke(200);
    if (dragging) fill (50,180);
    else if (rollover) fill(140,180);
    else fill(100,180);
    rect(x,y+outStroke,w,h-2*outStroke);
    fill(200);
    //textSize(20);
    //text(round(normalizedValue()*100)+"%",x+w*.5,maxYZone+h*.7);
    textSize(FontAdjuster.getSize(20));
    float posY;
    if(DrumMachine.isAndroidDevice)
      posY=y+h*.7f;
    else
      posY=y+h*.75f;    
    text(round(value()),x+w*.5f,posY);
    textSize(FontAdjuster.getSize(12));
    textLeading(FontAdjuster.getSize(12));
    text(text,x+w*.5f,maxYZone+h*.5f);    
  } 

}
class Midi{

  MidiBus myBus=null;
  PApplet processing; 

Midi(PApplet processing){
  this.processing=processing;
  //MidiBus.list();
  //myBus = new MidiBus(processing, 0, 0); // Create a new MidiBus with no input device and the default Java Sound Synthesizer as the output device.
}


public void draw() {
  int channel = 0;
  int pitch = 64;
  int velocity = 127;
  
  //myBus.sendNoteOn(channel, pitch, velocity); // Send a Midi noteOn
  //delay(200);
  //myBus.sendNoteOff(channel, pitch, velocity); // Send a Midi nodeOff
  
  int number = 0;
  int value = 90;
  
  //myBus.sendControllerChange(channel, number, value); // Send a controllerChange
  //delay(2000);
}


public void noteOn(int channel, int pitch, int velocity) {
  // Receive a noteOn
  println();
  println("Note On:");
  println("--------");
  println("Channel:"+channel);
  println("Pitch:"+pitch);
  println("Velocity:"+velocity);
  if(myBus!=null)
    myBus.sendNoteOn(channel, pitch, velocity);
}

public void noteOff(int channel, int pitch, int velocity) {
  // Receive a noteOff
  println();
  println("Note Off:");
  println("--------");
  println("Channel:"+channel);
  println("Pitch:"+pitch);
  println("Velocity:"+velocity);
  if(myBus!=null)
    myBus.sendNoteOff(channel, pitch, velocity);
}

public void controllerChange(int channel, int number, int value) {
  // Receive a controllerChange
  println();
  println("Controller Change:");
  println("--------");
  println("Channel:"+channel);
  println("Number:"+number);
  println("Value:"+value);
}

}
public class ToggleButton extends Clickable{
  
  String text="";
  String activatedText;
  
  boolean ON=false;
  boolean blinkWhenOn=false;
  float blinkIntervalMS=500;
  boolean blinkOn=false;
  boolean released=false;
  boolean dragging=false;
  
  public ToggleButton(float tempX, float tempY, float tempW, float tempH) {
    super(tempX,tempY,tempW,tempH);
  }
  
  public void drawState(){
    if(ON){
      drawActivated();
    }else if(overed){
      drawOvered();
    }else{
      drawNormal();
    }
  }
  
  public void drawActivated(){
    if(blinkWhenOn){
      if(millis()%(blinkIntervalMS*2)<blinkIntervalMS){
        blinkOn=true;  
        stroke(255);
      }
      else{
        blinkOn=false;
        stroke(strokeColor);
      }     
    }    
    fill(red(fillColor),green(fillColor),blue(fillColor),255);
    rect(x,y,w,h);
    drawCommonContent();  
  }
  
  public void drawNormal(){
    stroke(strokeColor);
    fill(red(fillColor)-100,green(fillColor)-100,blue(fillColor)-100,200);  
    rect(x,y,w,h);
    drawCommonContent();
  }  
  
  public void drawCommonContent() {
    textSize(FontAdjuster.getSize(20));
    textAlign(CENTER);
    if(ON){
      fill(255);
      if(activatedText!=null)
        text(activatedText, x+(w*0.5f), y+(h*0.7f));
      else
        text(text, x+(w*0.5f), y+(h*0.7f));
    }
    else{
      fill(200);
      text(text, x+(w*0.5f), y+(h*0.7f));
    }    
  }
  
  public boolean isClicked(int mx, int my) {
    if (isOver(mx, my)) {
      clicked = true;
      offsetX = x-mx;
      offsetY = y-my;
      ON=!ON;
    }
    else {
      clicked =false;
    }

    return clicked;
  }
  
  boolean isReleased(int mx, int my) {
	  if (!dragging && isOver(mx, my)) {
		  released = true;
		  offsetX = x-mx;
		  offsetY = y-my;
		  ON=!ON;
	  }
	  else {
		  released =false;
	  }
	  overed=false;	  
	  dragging=false;
	  clicked =false;

	  return released;
  }
  
  boolean isDragging(int mx, int my) {
	  if (clicked && isOver(mx, my)) {
		  overed=false;
		  dragging = true;
	  }

	  return dragging;
  }   
	  
  boolean cancelClick(int mx, int my) {
		  dragging=false;
		  clicked =false;
		  overed=false;
		  return true;
  }   

}
class VerticalSlider extends Draggable{

  float divisions=10;
  float divHeight=0.0f;
  int outStroke=3;
  String text="";
  
  VerticalSlider(float x, float y, float w, float h) {
    super(x,y,w,h);
    expandedFactor=1.2f;
  }
  
  public void limitY(float min,float max){
    limitedY=true;
    minY=min;
    maxY=max;
    calculateClickZone();
    divHeight=(maxYZone-minYZone)/divisions;
    println("totalH:"+(maxYZone-minYZone)+" divHeight:"+divHeight);    
  } 
 
  public float normalizedValue(){
    return map(y, minY, maxY, 1.0f, 0.0f);  
  }
  
  public void draw() {
    
    strokeWeight(outStroke);
    stroke(100,100,100,255);
    fill(map(normalizedValue(),0,1,50,127),255);
    rect(minXZone,minYZone,maxXZone-minXZone,maxYZone-minYZone);
    strokeWeight(2);
    stroke(255,255,255,255);
    for(int i=1;i<divisions;i++){
      float yPos=minYZone+i*divHeight;
      if(i%2==0){
        line(minXZone+outStroke,yPos,minXZone+w*.5f,yPos);
        line(maxXZone-w*.5f,yPos,maxXZone-outStroke,yPos);      
      }
      else{
        line(minXZone+outStroke,yPos,minXZone+w*.4f,yPos);
        line(maxXZone-w*.4f,yPos,maxXZone-outStroke,yPos);      
      }
    }  
    strokeWeight(outStroke);  
    stroke(200);
    if (dragging) fill (50,180);
    else if (rollover) fill(140,180);
    else fill(100,180);
    rect(x,y,w,h);
    fill(200);
    //textSize(20);
    //text(round(normalizedValue()*100)+"%",x+w*.5,maxYZone+h*.7);
    textSize(FontAdjuster.getSize(20));
    float posY;
    if(DrumMachine.isAndroidDevice)
      posY=y+h*.73f;
    else
      posY=y+h*.785f;
    text(round(normalizedValue()*100),x+w*.5f,posY);
    textSize(FontAdjuster.getSize(12));
    textLeading(FontAdjuster.getSize(12));
    text(text,x+w*.5f,maxYZone+h*.5f);    
  } 

}
//The MIT License (MIT)

//Copyright (c) 2013 Mick Grierson, Matthew Yee-King, Marco Gillies

//Permission is hereby granted, free of charge, to any person obtaining a copy\u2028of this software and associated documentation files (the "Software"), to deal\u2028in the Software without restriction, including without limitation the rights\u2028to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\u2028copies of the Software, and to permit persons to whom the Software is\u2028furnished to do so, subject to the following conditions:
//The above copyright notice and this permission notice shall be included in\u2028all copies or substantial portions of the Software.

//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\u2028IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\u2028FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\u2028AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\u2028LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\u2028OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN\u2028THE SOFTWARE.
// Modifications:  June 2013  Martin Bruner - Audio Analysis recovered by Constantino Fernandez Traba









//import android.content.res.Resources;
 
 









public class Maxim {

  private float sampleRate = 44100;

  public final float[] mtof = {
    0, 8.661957f, 9.177024f, 9.722718f, 10.3f, 10.913383f, 11.562325f, 12.25f, 12.978271f, 13.75f, 14.567617f, 15.433853f, 16.351599f, 17.323914f, 18.354048f, 19.445436f, 20.601723f, 21.826765f, 23.124651f, 24.5f, 25.956543f, 27.5f, 29.135235f, 30.867706f, 32.703197f, 34.647827f, 36.708096f, 38.890873f, 41.203445f, 43.65353f, 46.249302f, 49.f, 51.913086f, 55.f, 58.27047f, 61.735413f, 65.406395f, 69.295654f, 73.416191f, 77.781746f, 82.406891f, 87.30706f, 92.498604f, 97.998856f, 103.826172f, 110.f, 116.540939f, 123.470825f, 130.81279f, 138.591309f, 146.832382f, 155.563492f, 164.813782f, 174.61412f, 184.997208f, 195.997711f, 207.652344f, 220.f, 233.081879f, 246.94165f, 261.62558f, 277.182617f, 293.664764f, 311.126984f, 329.627563f, 349.228241f, 369.994415f, 391.995422f, 415.304688f, 440.f, 466.163757f, 493.883301f, 523.25116f, 554.365234f, 587.329529f, 622.253967f, 659.255127f, 698.456482f, 739.988831f, 783.990845f, 830.609375f, 880.f, 932.327515f, 987.766602f, 1046.502319f, 1108.730469f, 1174.659058f, 1244.507935f, 1318.510254f, 1396.912964f, 1479.977661f, 1567.981689f, 1661.21875f, 1760.f, 1864.655029f, 1975.533203f, 2093.004639f, 2217.460938f, 2349.318115f, 2489.015869f, 2637.020508f, 2793.825928f, 2959.955322f, 3135.963379f, 3322.4375f, 3520.f, 3729.31f, 3951.066406f, 4186.009277f, 4434.921875f, 4698.63623f, 4978.031738f, 5274.041016f, 5587.651855f, 5919.910645f, 6271.926758f, 6644.875f, 7040.f, 7458.620117f, 7902.132812f, 8372.018555f, 8869.84375f, 9397.272461f, 9956.063477f, 10548.082031f, 11175.303711f, 11839.821289f, 12543.853516f, 13289.75f
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

  public Maxim(float sampleRate) {
    this.sampleRate = sampleRate;
    initAudioThread();
  }  

  public AudioPlayer createEmptyPlayer() {
    AudioPlayer ap = new AudioPlayer(sampleRate);
    AddAudioGenerator(ap);
    ap.resetAudioPlayer(); 
    return ap;
  }

  public void AddAudioGenerator(AudioPlayer ap) {
    audioThread.addAudioGenerator(ap);
  }

  public void RemoveAudioGenerator(AudioPlayer ap) {
    audioThread.removeAudioGenerator(ap);
  }  

  public void reLoadPlayer(AudioPlayer player, short[] audioData) {
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

  public void reloadFile(AudioPlayer ap, String filename) {
    RemoveAudioGenerator(ap);
    reLoadPlayer(ap, ap.justLoadAudioFile ( filename) );
    AddAudioGenerator(ap);
    //ap.resetAudioPlayer();
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

  public short[] loadWavFile(File f) {

    short [] myAudioData = null;
    int fileSampleRate = 0;
    String filename=f.getName();    
    try {
      
      // how long is the file in bytes?
      long byteCount = 0;
      BufferedInputStream bis=null;

      try {
        byteCount = getAssets().openFd(filename).getLength();
        // check the format of the audio file first!
        // only accept mono 16 bit wavs
        InputStream is = getAssets().open(filename); 
        bis = new BufferedInputStream(is);
      }
      catch(FileNotFoundException e) {
        println("getAssets not working");
        e.printStackTrace();
        
      FileInputStream fIn = new FileInputStream(f);
        if (fIn!=null) {
          byteCount=fIn.available();
          bis = new BufferedInputStream(fIn);        
        }
        else {
          println("FileInputStream not working");
        }
      }
      println("Opening file:"+filename);
      
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
         //if((float) fileSampleRate != this.sampleRate){
         //throw new InputMismatchException("In File: "+filename+" The sample rate of: "+fileSampleRate+ " does not match the default sample rate of: "+this.sampleRate);
         //}  

        // skip 34 bytes to get bits per sample
        // (1 byte)
        bis.skip(6); // we were at 28...
        bis.read(byteBuff, 0, 2);// read 2 so we are at 36 now
        bitDepth = (short)byteBuff[0];
        System.out.println("bit depth "+bitDepth);
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
    } 

    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    if ((float) fileSampleRate != this.sampleRate) {
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
      println("Aiff File framesize:"+bitDepth);
      AudioFormatJava af=audioFileFormat.getFormat();
      int fileSampleRate=(int)af.getSampleRate();
      int channels=af.getChannels();
      boolean isBigEndian=af.isBigEndian();
      println("Aiff File frameRate:"+af.getFrameRate()+" sampleRate:"+fileSampleRate);
      println("Aiff File is bigEndian?:"+isBigEndian+" sample bitsize:"+af.getSampleSizeInBits()+" channels:"+channels);
      int numBytesRead = 0;
      int skip = 0;//(channels -1) * bitDepth;

      AudioInputStream ais=aiffFileReader.getAudioInputStream(f);
      myAudioData=new short[(int)ais.getFrameLength()];
      while ( (numBytesRead = ais.read (byteBuff)) != -1) {
        //println("Readed:"+numBytesRead+" saved:"+byteBuff.length);
        //println("orig. value:"+bytesToIntBigEndian(byteBuff, numBytesRead)+" final value:"+(short)bytesToIntBigEndian(byteBuff, numBytesRead));
        
        if(!isBigEndian)
          myAudioData[sample] = (short) bytesToInt(byteBuff, numBytesRead);
        else
          myAudioData[sample] = (short) bytesToIntBigEndian(byteBuff, numBytesRead);
          
        if (skip>0 && ais.available()>=bitDepth)
          ais.skip(skip);
        sample ++;
      }

      if (fileSampleRate != this.sampleRate) {
        System.out.println("Resampling file" +fileName+" from "+fileSampleRate+" Hz to "+this.sampleRate+ " Hz");
        return convertSampleRate(myAudioData, (int) (this.sampleRate), fileSampleRate);
      }
    }
    catch(UnsupportedAudioFileException e) {
      e.printStackTrace();
      println("UnsupportedAudioFileException reading:"+fileName+"\n"+e);
    }
    catch(IOException e) {
      e.printStackTrace();
      println("IOException reading:"+fileName+"\n"+e);
    }    

    return myAudioData;
  }

  public short[] justLoadAudioFile (String filename) {

    File f = new File(filename);
    
    boolean isAiff=false;
    AiffFileReader aiffFileReader=new AiffFileReader();

    try {
      AudioFileFormat audioFileFormat=aiffFileReader.getAudioFileFormat(f);

      if (audioFileFormat.getType()==AudioFileFormat.Type.AIFC || audioFileFormat.getType()==AudioFileFormat.Type.AIFF) {
        println("Aiff File detected type:"+audioFileFormat.getType());
        isAiff=true;
      }
    }
    catch(UnsupportedAudioFileException e) {
      e.printStackTrace();
      println("UnsupportedAudioFileException:"+e);
    }
    catch(IOException e) {
      e.printStackTrace();
      println("IOException:"+e);
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
  private int bytesToInt(byte[] bytes, int wordSizeBytes) {
    int val = 0;
    //LIMIT TO 16BITS
    if(wordSizeBytes>2)wordSizeBytes=2;
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
    if(wordSizeBytes>2)start=wordSizeBytes-2;
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
          //val /= audioGens.size();
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

/**
 * Implement this interface so the AudioThread can request samples from you
 */
public interface AudioGenerator {
  /** AudioThread calls this when it wants a sample */
  public short getSample();
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
    r = map(r, 0, 1, 0.005f, 2);

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

  public void setFilter(float f, float r) {
    f = constrain(f, 0, 1);
    res = constrain(r, 0, 1);
    f = map(f, 0, 1, 25, sampleRate / 4);
    r = map(r, 0, 1, 1, 25);
    this.f = f;
    this.res = r;    

    //println("mickF: f "+f+" r "+r);
  }
  public float applyFilter(float in) {
    return lores(in, f, res);
  }

  public float lores(float input, float cutoff1, float resonance) {
    //cutoff=cutoff1*0.5;
    //if (cutoff<10) cutoff=10;
    //if (cutoff>(sampleRate*0.5)) cutoff=(sampleRate*0.5);
    //if (resonance<1.) resonance = 1.;

    //if (resonance>2.4) resonance = 2.4;
    z=cos(TWO_PI*cutoff/sampleRate);
    c=2-2*z;
    float r=(sqrt(2.0f)*sqrt(-pow((z-1.0f), 3.0f))+resonance*(z-1))/(resonance*(z-1));
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
  public short[] reSample(short[] sourceData, int sourceRate, int targetRate) 
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

  public LinearInterpolation() {
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

    if (oldSampleRate==newSampleRate) {
      return samples;
    }

    int newLength=(int)Math.round(((float)samples.length/oldSampleRate*newSampleRate));
    float lengthMultiplier=(float)newLength/samples.length;
    short[] interpolatedSamples = new short[newLength];

    // interpolate the value by the linear equation y=mx+c        
    for (int i = 0; i < newLength; i++) {

      // get the nearest positions for the interpolated point
      float currentPosition = i / lengthMultiplier;
      int nearestLeftPosition = (int)currentPosition;
      int nearestRightPosition = nearestLeftPosition + 1;
      if (nearestRightPosition>=samples.length) {
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

    int imax = (int) (Math.log(fftFrameSize) / Math.log(2.f));

    double[] warray = new double[(fftFrameSize - 1) * 4];
    int w_index = 0;

    for (int i = 0,  nstep = 2; i < imax; i++) {
      int jmax = nstep;
      nstep <<= 1;

      double wr = 1.0f;
      double wi = 0.0f;

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
    resampleFFT fft = new resampleFFT(sampleSize/2, -1);
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
    for (int i = 0; i < indexSize; i+=2) {
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
    double theta = 3.141592653589793f/(n>>1);
    if (isign) {
      c2 = -.5f;
      four1(data, n>>1, true);
    } 
    else {
      c2 = .5f;
      theta = -theta;
    }
    wtemp = Math.sin(.5f*theta);
    wpr = -2.f*wtemp*wtemp;
    wpi = Math.sin(theta);
    wr = 1.f + wpr;
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
      theta = 6.28318530717959f / mmax;
      if (!isign)
        theta = -theta;
      wtemp = Math.sin(0.5f * theta);
      wpr = -2.0f * wtemp * wtemp;
      wpi = Math.sin(theta);
      wr = 1.0f;
      wi = 0.0f;
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
		 println("Detected "+numPointers+" pointers.");
		 onTouchDown(x, y, pressure);
		 touch.pointDown(x, y, id, pressure);
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
	    touch.pointUp(event.getPointerId(index));
	  }
	  else if ( code == MotionEvent.ACTION_MOVE) {
	    int numPointers = event.getPointerCount();
	    //println("Detected "+numPointers+" pointers.");
	    for (int i=0; i < numPointers; i++) {
	      id = event.getPointerId(i);
	      x = event.getX(i);
	      y = event.getY(i);
	      touch.pointMoved(x, y, id);
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
	  
	
  public void onTouchDown(float x,float y,float pressure){
	  if(!sequencerMode){
		  if(pressureEnabled){
			  if(tapPressures.size()>=10){
				  int pos=(int)random(0, tapPressures.size());
				  println("removing:"+pos);
				  tapPressures.remove(pos);
			  }
			  tapPressures.add(pressure);
			  Collections.sort(tapPressures);
		  }else{
			  pressure=1;
		  }
		  println("original pressure:"+pressure+" min:"+tapPressures.getFirst()+" max:"+tapPressures.getLast());
		  pressure=map(pressure,tapPressures.getFirst(),tapPressures.getLast(),0.25f,1);
		  println("end pressure:"+pressure);
		  mousePressed((int)x, (int)y, pressure);
	  }
	  else{
		//sequencer.mouseReleased(x,y);
	  }
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
	  if(sequencerMode)
		  sequencer.changeZoom((int) e.getCenterX(),(int)e.getCenterY(),e.getAmount());
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
  
   @Override public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
            case R.id.toggleMode:
            	   if(!sequencerMode){
            		   item.setTitle("Live Mode");
            		   sequencer.updateTracksState();
            	   }else{
            		   item.setTitle("Sequencer Mode");
            		   //sequencer.updateSamplePerBeatState();
            	   }
                   sequencerMode=!sequencerMode;
                   break;
            case R.id.deleteAll:
                	deleteAllSounds();
                break;
            case R.id.donate:
                startActivity(new Intent(this, DonationsActivity.class));
                break;
            case R.id.about:
                
                break;                
            }
            return true;
   }  
  
}
