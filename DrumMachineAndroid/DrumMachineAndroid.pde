

import themidibus.*;

/*
import ddf.minim.spi.*;
 import ddf.minim.signals.*;
 import ddf.minim.*;
 import ddf.minim.analysis.*;
 import ddf.minim.ugens.*;
 import ddf.minim.effects.*;
 
 Minim maxim;
 
 ddf.minim.AudioPlayer player;
 ddf.minim.AudioPlayer playerBass;
 ddf.minim.AudioPlayer playerSnare;
 ddf.minim.AudioPlayer playerHitHat;
 ddf.minim.AudioPlayer[] playersArray=new ddf.minim.AudioPlayer[20];
 */

Midi midi;

Maxim maxim;
float BPM=120.0;
float beatMS=60000.0/BPM;
float beatsPerTempo=4.0;
float gridsByBeat=8.0;
float gridMS=60000.0/BPM/gridsByBeat;
float tempoMS=beatsPerTempo*beatMS;
long pausedMS=-1, totalPaused=0, audioMs=0;
float tempoOffset=0;
float beatOffset=0;
float lastPlayTime;
float firstKick=0;
boolean snapToGrid=true;
boolean liveMode=false;
boolean sequencerMode=false;
Sequencer sequencer=new Sequencer();
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

float filterFrequency=11025.0, filterResonance=0.5, delayTime=0, delayFeedback=0, speed=1.0, speed1=1.0, volumeKick=1.0, volumeBass=1.0, volume=1.0;

color candyPink=color(247, 104, 124);
color redColor=#B90016;
color orangeColor=#C18900;
color blueColor=#5828A3;
color blueDarkColor=#380883;
color blueLightColor=#380883;
color greenColor=#22A300;
color darkGreyColor=color(50);
color mediumGreyColor=color(127);
color lightGreyColor=color(200);
color yellowColor=color(100, 100, 0);

PFont lcdFont;

FloatList savedCues= new FloatList(), notPlayedCues=new FloatList();
StringDict soundByCue= new StringDict();

final int LINES=0;
final int BARS=1;
final int CURVES=2;
int spectrumMode=LINES;


AudioPlayThread audioPlayThread=null;

float barOriginX, barOriginY, barWidth, barHeight, markerWidth;
color barColor, markerColor, separatorColor;

float pressBarWidth, pressBarHeight, pressBarHeightMargin;
ClickablePad[] kick=new ClickablePad[4], bass=new ClickablePad[4], snare=new ClickablePad[4], hithat=new ClickablePad[4];
Clickable panelModeButton, trackModeButton;
ExpandableButtons deleteButtons;
ToggleButton loadButton, playButton, deleteButton;

float valueX=0.0, valueY=0.0;
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

void setupAndroid() {
  files = new SelectLibrary(this);
  files.filterExtension=".wav;.json;.aiff;.aif";
}


void setup()
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

void setupPowerSpectrum() {
  //if (AndroidUtil.numCores()>1) {
  for (int i=0;i<playerKick.length;i++) {
    playerKick[i].setAnalysing(true);
    playerBass[i].setAnalysing(true);
    playerSnare[i].setAnalysing(true);
    playerHitHat[i].setAnalysing(true);
  }
  //}
  maxLedWidth=buttonSize;
  ledHeight=(height*0.01);      
  originPowerSpecX=0;
  originPowerSpecY=(int)height;
}

void setupGeneral() {
  //frameRate(30);
  /*if (!isAndroidDevice){
    if(render3D)
      size(480, 688, P3D);
    else
      size(480, 688);
  }
  else
    size(420, 700);*/
  size(768,1280, P3D);

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

void setupMidi() {
  midi=new Midi(this);
}

void changeBPM(float newBPM) {
  BPM=newBPM;
  beatMS=60000.0/BPM;
  beatsPerTempo=4.0;
  tempoMS=beatsPerTempo*beatMS;
}

void noteOn(int channel, int pitch, int velocity) {
  midi.noteOff(channel, pitch, velocity);
  int soundPlayer=(pitch-24)%6;
  int soundNumber=(pitch-24)/6;
  int soundType=soundPlayer+soundNumber*4;  
  println("type:"+soundType+" player:"+soundPlayer+" number:"+soundNumber);
  if (soundType>=0 && soundType<16 && soundNumber>=0 && soundNumber<4 && soundPlayer>=0 && soundPlayer<4) {
    switch(soundPlayer) {
    case 0:
      playerKick[soundNumber].volume(map(velocity, 0, 127, 0, 1.0));
      break;
    case 1:
      playerBass[soundNumber].volume(map(velocity, 0, 127, 0, 1.0));
      break;
    case 2:
      playerSnare[soundNumber].volume(map(velocity, 0, 127, 0, 1.0));
      break;
    case 3:
      playerHitHat[soundNumber].volume(map(velocity, 0, 127, 0, 1.0));
      break;
    }    
    addSoundTypeToList(soundType);
  }
}

void noteOff(int channel, int pitch, int velocity) {
  midi.noteOff(channel, pitch, velocity);
}

void controllerChange(int channel, int number, int value) {
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

void setupTempoBar() {
  barWidth=width*0.82;
  barHeight=height*0.025;
  blueDarkColor=lerpColor(blueColor, color(150), .5);
  blueLightColor=lerpColor(blueColor, color(0), .5);  
  barColor = color(50);
  markerColor = orangeColor;
  separatorColor = candyPink;
  markerWidth=width*0.005;
  barOriginX=width*0.091;
  if (!isAndroidDevice)
    barOriginY=height*0.01;
  else
    barOriginY=height*0.05;
}

void setupPadButtons() {
  buttonSize=width*0.153;
  buttonsOriginX=width*0.131;
  buttonMarginX=buttonSize*0.29;
  buttonMarginY=buttonSize*0.275;

  if (isAndroidDevice) {
    buttonsOriginY=height*0.45;
  }
  else {
    buttonsOriginY=height*0.445;
  }
}

void setupSliders() {
  sliderWidth=29.0*(width*0.14/44.0);//width*0.12;
  sliderHeight=29.0*(height*0.06/44.0);//width*0.025;
  sliderMarginX=width*0.142;
  sliderOriginX=width*0.10;
  sliderImage=loadImage("slider.png");

  if (isAndroidDevice) {
    sliderMinY=height*0.20;
    sliderMaxY=height*0.33;
  }
  else {
    sliderMinY=height*0.162;
    sliderMaxY=height*0.33;
  }
}

void toggleAudioPlayThread() {

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
void proccessTempoVars() {

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
        playSoundType(i);
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

void setupTopControls() {
  bpmSlider=new HorizontalSlider(barOriginX, barOriginY+barHeight*2.0, sliderWidth, buttonSize*0.5);
  bpmSlider.limitX(barOriginX, barOriginX+barWidth*0.25);
  bpmSlider.valuesX(80, 160, 120);
  bpmSlider.text="BPM";

  playButton=new ToggleButton(barOriginX+barWidth*0.368, barOriginY+barHeight*2.0, buttonSize*0.5, buttonSize*0.5);
  playButton.text="II";
  playButton.activatedText=">";
  playButton.fillColor=color(150, 150, 0);
  //playButton.blinkWhenOn=true;  

  loadButton=new ToggleButton(barOriginX+barWidth*0.62-buttonSize*0.6, barOriginY+barHeight*2.0, buttonSize*1.2, buttonSize*0.5);
  loadButton.text="LOAD";
  loadButton.fillColor=color(150, 150, 0);
  loadButton.blinkWhenOn=true;

  deleteButtons=new ExpandableButtons(barOriginX+barWidth-buttonSize*1.2, barOriginY+barHeight*2.0, buttonSize*1.2, buttonSize*0.5);
  deleteButtons.text="DELETE";
  deleteButtons.fillColor=yellowColor;
  String[] buttons = { 
    "KICK", "BASS", "SNARE", "HITHAT", "ALL"
  };
  deleteButtons.setOtherButtons(buttons);
  color[] colors = { 
    redColor, orangeColor, blueColor, greenColor, mediumGreyColor
  };
  deleteButtons.setOtherButtonColors(colors);
}

void drawTopControls() {
  bpmSlider.rollover(mouseX, mouseY);
  bpmSlider.dragHorizontally(mouseX);  
  bpmSlider.draw();
  changeBPM(bpmSlider.value());

  playButton.drawState();

  loadButton.drawState();

  deleteButtons.drawState();
}

void drawTempoBar() {
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
      rect(beatsWidth*i+barOriginX-markerWidth*0.5, barOriginY, markerWidth, barHeight);
    }
    fill(255);
    if (i!=beatsPerTempo)
      for (int j=1;j<gridsByBeat;j++) {
        rect((beatsWidth*i)+gridWidth*j+barOriginX, barOriginY, markerWidth/4.0, barHeight);
      }
  }

  fill(markerColor);
  if (pausedMS<0) {
    float offset=map((millis()-totalPaused)%tempoMS, 0.0, tempoMS, 0.0, barWidth);
    lastMarkerOffset=offset;
    rect(barOriginX+offset-markerWidth, barOriginY, markerWidth, barHeight);
  }
  else {
    rect(barOriginX+lastMarkerOffset-markerWidth, barOriginY, markerWidth, barHeight);
  }
}

void drawPadsContainer() {
  strokeWeight(3);
  stroke(100, 100, 100, 255);
  fill(50, 50, 50, 255);
  rect(barOriginX, buttonsOriginY-buttonSize*0.2, barWidth, buttonSize*5.295);
}

void drawPadButtons(color c, ClickablePad[] padArray, float buttonsOriginX, float buttonsOriginY, float buttonSize, float buttonMargin, String buttonText) {
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

PVector getPadButtonOrigin(int soundType) {
  int row=int(soundType/4);
  int col=soundType%4;
  return new PVector(buttonsOriginX+(buttonSize+buttonMarginX)*col, buttonsOriginY+(buttonSize+buttonMarginY)*row);
}

void drawKickButtons() {
  drawPadButtons(redColor, kick, buttonsOriginX, buttonsOriginY, buttonSize, buttonMarginX, "K");
}

void drawBassButtons() {
  drawPadButtons(orangeColor, bass, buttonsOriginX, buttonsOriginY+buttonSize+buttonMarginY, buttonSize, buttonMarginX, "B");
}

void drawSnareButtons() {
  drawPadButtons(blueColor, snare, buttonsOriginX, buttonsOriginY+(buttonSize+buttonMarginY)*2, buttonSize, buttonMarginX, "S");
}

void drawHitHatButtons() {
  drawPadButtons(greenColor, hithat, buttonsOriginX, buttonsOriginY+(buttonSize+buttonMarginY)*3, buttonSize, buttonMarginX, "H");
}

void drawSliders() {
  for (int i=0;i<sliders.length;i++) {
    if (sliders[i]==null) {
      sliders[i]=new VerticalSlider(sliderOriginX+(sliderMarginX*i), sliderMinY+(sliderMaxY-sliderMinY)*0.5, sliderWidth, sliderHeight);
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

void drawFiltersZone() {

  fill(255);
  rect(width*0.05, height*0.5, width*0.9, height*0.4); 
  stroke(255);
  textSize(10);

  if (panelModeButton==null)
    panelModeButton=new Clickable(width*0.1, height*0.51, width*0.2, height*0.025);
  panelModeButton.fillColor=redColor;   
  panelModeButton.drawState();

  fill(0); 
  text("[PANEL MODE] ["+panelMode+"]", width*0.2, height*0.525);

  fill(orangeColor);
  if (trackModeButton==null)
    trackModeButton=new Clickable(width*0.5, height*0.51, width*0.2, height*0.025);
  panelModeButton.fillColor=orangeColor;   
  panelModeButton.drawState();    

  fill(0);
  text("[TRACK MODE] ["+trackMode+"]", width*0.6, height*0.525);

  if (mouseY>height*0.5) {
    stroke(0);
    line(mouseX, height*0.5, mouseX, height);
    line(0, mouseY, width, mouseY); 
    textSize(10);
    textAlign(CORNER);
    fill(0);
    text("["+valueX+","+valueY+"]", mouseX+width*0.01, mouseY-height*0.005);
  }
}

void drawPowerSpectrum() {
  for (int i=0;i<playerKick.length;i++) {
    drawSpectrumOf(playerKick[i], redColor, i);
    drawSpectrumOf(playerBass[i], orangeColor, i+4);
    drawSpectrumOf(playerSnare[i], blueColor, i+8);
    drawSpectrumOf(playerHitHat[i], greenColor, i+12);
  }
}

void drawSpectrumOf(AudioPlayer audioPlayer, color c, int soundType) {
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
        float ledY=buttonsOriginY-(buttonSize*0.15)+(buttonSize+buttonMarginY)*(soundType/4);
        fill(c);        
        rect(ledX+maxLedWidth-ledWidth, ledY, ledWidth, ledHeight);
      }
      float xOffset=0;
      PVector origin=getPadButtonOrigin(soundType);
      originPowerSpecX=origin.x+1;
      originPowerSpecY=origin.y+buttonSize-(spectrumMode==BARS?2:3);      
      origin.y=origin.y+buttonSize-3-values[0]*height*0.1;
      origin.x+=3;      
      fill(100);
      for (int i=0;i<values.length;i+=values.length/10.0) {
        if (values[i]<0)values[i]=0;
        xOffset=map(i, 0, values.length, 0, buttonSize-4-(spectrumMode==BARS?6:0));
        switch(spectrumMode) {
        case LINES:
          stroke(50);
          strokeWeight(3);
          if (i>=(int)(values.length/10.0)) {
            PVector destiny=new PVector(originPowerSpecX+xOffset, originPowerSpecY-values[i]*height*0.1);
            line(origin.x, origin.y, destiny.x, destiny.y);
            origin=destiny;
          }
          break;
        case BARS:
          noStroke();
          PVector destiny=new PVector(originPowerSpecX+xOffset, originPowerSpecY-values[i]*height*0.1);
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
    halfTime=animationStart+animTime*0.5;
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
          float xPos=map(ms, animationStart, halfTime, 0, width*0.5);          
          float yPos=map(ms, animationStart, halfTime, 0, height*0.5);           
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
        animationEnd=ms+animTime*0.5;
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
          float xPos=map(ms, halfTime, animationEnd, width*0.5, 0);          
          float yPos=map(ms, halfTime, animationEnd, height*0.5, 0);          
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

void draw()
{  
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


void controlVolume(float volume, int trackMode) {
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


void controlFilter(float filterFrequency, float filterResonance, int trackMode) {
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

void controlPitch(float speed, int trackmode) {

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

void proccessPanel() {
  valueX=constrain(map(mouseX, width*0.05, width*0.9, 0.0, 1.0), 0.0, 1.0);
  valueY=constrain(map(mouseY, height*0.5, height*0.95, 0.0, 1.0), 0.0, 1.0);   
  switch(panelMode) {
  case FILTER:
    filterFrequency=map(valueY, 0.0, 1.0, 0.0, 5000);
    filterResonance=map(valueX, 0.0, 1.0, -1.0, 1.0);  
    controlFilter(filterFrequency, filterResonance, trackMode);
    break;
  case ECHO:
    delayTime=map(valueX, 0.0, 1.0, 0.0, 3000);
    delayFeedback=map(valueY, 0.0, 1.0, 0.0, 100.0); 
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
    speed=map(valueX, 0.0, 1.0, 0.0, 2.0);
    controlPitch(speed, trackMode);
    break;
  }
}


void mouseMoved()
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

void mouseDragged()
{

  if (!sequencerMode) {
    if (deleteButtons.isSelected(mouseX, mouseY)) {
    }  

    for (int i=0;i<sliders.length;i++) {
      if (sliders[i].dragging) {
        float valY=constrain(map(sliders[i].y, sliderMinY, sliderMaxY, 1.0, 0.0), 0.0, 1.0);    
        switch(i) {
        case 0:
          filterFrequency=map(valY, 0.0, 1.0, 0.0, 10000);
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

void mouseReleased() {

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

void deleteSoundType(int soundType) {
  for (int i=0;i<32;i++) {
    samplesPerBeat[soundType][i]=false;
  }
}


void deleteAllSounds() {
  for (int i=0;i<totalSamples;i++) {
    getPlayerBySoundType(i).stop();
    for (int j=0;j<(int)(gridsByBeat*beatsPerTempo);j++) {
      samplesPerBeat[i][j]=false;
    }
  }
}

void deleteSoundOfGroup(int soundGroup) {
  println("Deleting sound group:"+soundGroup);
  for (int i=0;i<totalSamples;i+=4) {
    deleteSoundType(i+soundGroup);
  }
}


void loadSoundType(int soundType, AudioPlayer player) {
  loadPlayerOfSoundType=soundType;
  //if (isAndroidDevice)
  files.selectInput("Select a .wav,.aif file to load:", "fileSelected");

  //selectInput("Select a .wav,.aif file to load:", "fileSelected");
}

JSONArray loadJsonSoundPack(File file) {
  JSONArray sounds=null;
  if (file.exists() && file.isFile()) {
    //sounds=loadJSONArray(file.getAbsolutePath());
  }    
  return sounds;
}

void fileSelected(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } 
  else {
    println("User selected " + selection.getAbsolutePath());
    if (selection.getName().endsWith(".json")) {
      JSONArray sounds=loadJsonSoundPack(selection);
      for (int i=0;i<sounds.size();i++) {
        JSONObject sound = sounds.getJSONObject(i);
        File localFile;
        //if(isAndroidDevice)
        //localFile=new File(DownloadFile.getDownloadPath()+""+sound.getString("filePath"));
        //else
        localFile=new File(this.dataPath("")+sound.getString("filePath"));
        println("Loading sound:"+sound.getString("filePath")+" on pad:"+sound.getInt("soundType"));
        loadSoundOnPlayer(sound.getInt("soundType"), localFile);
      }
    }
    else {
      if (loadPlayerOfSoundType!=-1) {
        loadSoundOnPlayer(loadPlayerOfSoundType, selection);
      }
    }
  }
  mainMode=normalMode;
  loadButton.ON=false;
}

void loadSoundOnPlayer(int soundType, File selection) {
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

void mousePressed()
{
  if (!sequencerMode) {
    for (int i=0;i<kick.length;i++) {
      if (kick[i].isClicked(mouseX, mouseY)) {
        if (mainMode==normalMode)
          addSoundTypeToList(kick.length*i);
        else if (mainMode==loadMode)
          loadSoundType(kick.length*i, playerKick[i]);
        else if (mainMode==deleteMode)
          deleteSoundType(kick.length*i);
      }
      else if (bass[i].isClicked(mouseX, mouseY)) {
        if (mainMode==normalMode)
          addSoundTypeToList(1+bass.length*i);
        else if (mainMode==loadMode)
          loadSoundType(1+bass.length*i, playerBass[i]);
        else if (mainMode==deleteMode)
          deleteSoundType(1+bass.length*i);
      }
      else if (snare[i].isClicked(mouseX, mouseY)) {
        if (mainMode==normalMode)
          addSoundTypeToList(2+snare.length*i);
        else if (mainMode==loadMode)
          loadSoundType(2+snare.length*i, playerSnare[i]);
        else if (mainMode==deleteMode)
          deleteSoundType(2+snare.length*i);
      }
      else if (hithat[i].isClicked(mouseX, mouseY)) {
        if (mainMode==normalMode)
          addSoundTypeToList(3+hithat.length*i);
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

AudioPlayer getPlayerBySoundType(int soundType) {
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

void playSoundType(int soundType) {
  AudioPlayer ap=getPlayerBySoundType(soundType);
  ap.cue(0);
  ap.play();
}

color getColorByMs(float msOcc) {
  if (soundByCue.hasKey(msOcc+"")) {
    int[] types = int(split(soundByCue.get(msOcc+""), '#'));
    if (types.length>0) {
      color finalColor=getColorSoundType(types[0]);
      //for (int i=1;i<types.length;i++)
      //  finalColor=lerpColor(finalColor, getColorSoundType(types[i]), 0.5);//1.0/types.length);
      return finalColor;
    }
    else return color(0);
  }
  else
    return color(0);
}

color getColorSoundType(int sountType) {
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

void addSoundTypeToList(int soundType) {
  playSoundType(soundType);
  if (!liveMode) {
    samplesPerBeat[soundType][currentGrid]=!samplesPerBeat[soundType][currentGrid];
    println("changed sound:"+soundType+" at position:"+currentGrid);
  }
}

void keyPressed() {
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
      if (gridsByBeat>2)gridsByBeat*=0.5;
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

