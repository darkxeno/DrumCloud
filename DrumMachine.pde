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
 
boolean isAndroidDevice=false; 

Midi midi;

Maxim maxim;
AudioPlayer[] playerKick=new AudioPlayer[4];
AudioPlayer[] playerBass=new AudioPlayer[4];
AudioPlayer[] playerSnare=new AudioPlayer[4];
AudioPlayer[] playerHitHat=new AudioPlayer[4];

int lastPlayerUsed=0;
boolean onlyOnePlayerMode=true;

float BPM=120.0;
float beatMS=60000.0/BPM;
float beatsPerTempo=4.0;
float gridsByBeat=16.0;
float tempoMS=beatsPerTempo*beatMS;
float tempoOffset=0;
float beatOffset=0;
float lastPlayTime;
float firstKick=0;
boolean snapToGrid=true;
boolean liveMode=false;

PImage backTopMachine, backBottomMachine;

float filterFrequency=11025.0, filterResonance=0.5, delayTime=0, delayFeedback=0, speed=1.0, speed1=1.0, volumeKick=1.0, volumeBass=1.0, volume=1.0;

color candyPink=color(247, 104, 124);
color redColor=#B90016;
color orangeColor=#C18900;
color blueColor=#5828A3;
color blueDarkColor=#380883;
color blueLightColor=#380883;
color greenColor=#22A300;

PFont lcdFont;

FloatList savedCues= new FloatList(), notPlayedCues=new FloatList();
StringDict soundByCue= new StringDict();

final int LINES=0;
final int BARS=1;
final int CURVES=2;
int spectrumMode=LINES;


AudioPlayThread audioPlayThread= new AudioPlayThread(this, 1, "AudioPlayThread");

float barOriginX, barOriginY, barWidth, barHeight, markerWidth;
color barColor, markerColor, separatorColor;

float pressBarWidth, pressBarHeight, pressBarHeightMargin;
ClickablePad[] kick=new ClickablePad[4], bass=new ClickablePad[4], snare=new ClickablePad[4], hithat=new ClickablePad[4];
Clickable panelModeButton, trackModeButton;

float valueX=0.0, valueY=0.0;
float buttonSize, buttonsOriginX, buttonsOriginY, buttonMarginX, buttonMarginY;
float sliderWidth, sliderHeight, sliderOriginX, sliderMinY, sliderMaxY, sliderMarginX;
VerticalSlider[] sliders=new VerticalSlider[6];
PImage sliderImage;

float maxLedWidth,ledHeight;
float originPowerSpecX,originPowerSpecY;

PanelMode panelMode=PanelMode.FILTER;
final int ALL=0;
final int KICK=1;
final int BASS=2;
final int SNARE=3;
final int HITHAT=4;
int trackMode=ALL;

void setup()
{
  setupGeneral();
  setupTempoBar();
  setupPadButtons();
  setupSliders();
  setupMidi();
  setupPowerSpectrum();
}


void setupPowerSpectrum() {
  for (int i=0;i<playerKick.length;i++) {
    playerKick[i].setAnalysing(true);
    playerBass[i].setAnalysing(true);
    playerSnare[i].setAnalysing(true);
    playerHitHat[i].setAnalysing(true);
  }
  maxLedWidth=buttonSize;
  ledHeight=(height*0.01);      
  originPowerSpecX=0;
  originPowerSpecY=(int)height;  
}

void setupGeneral() {
  if(!isAndroidDevice)
    size(480, 688);
  audioPlayThread.start();
  background(0);
  maxim = new Maxim(this);
  //maxim = new Minim(this);
  for (int i=0;i<playerKick.length;i++) {
    playerKick[i] = maxim.loadFile("kick_"+i+".wav");
    playerKick[i].setLooping(false);
    playerKick[i].volume(volume);
    playerKick[i].setFilter(filterFrequency,filterResonance);
    playerBass[i] = maxim.loadFile("bass_"+i+".wav");
    playerBass[i].setLooping(false);
    playerBass[i].volume(volume);
    playerBass[i].setFilter(filterFrequency,filterResonance);
    playerSnare[i] = maxim.loadFile("snare_"+i+".wav");
    playerSnare[i].setLooping(false);
    playerSnare[i].volume(volume);
    playerSnare[i].setFilter(filterFrequency,filterResonance);
    playerHitHat[i] = maxim.loadFile("hithat_"+i+".wav");
    playerHitHat[i].setLooping(false);
    playerHitHat[i].volume(volume);
    playerHitHat[i].setFilter(filterFrequency,filterResonance);
  }
  //player.volume(1.0);
  backTopMachine=loadImage("MPD26_mod.png");
  //lcdFont = loadFont("lcd.ttf");
  rectMode(CORNER);
}

void setupMidi() {
  midi=new Midi(this);
}

void changeBPM(float newBPM){
  BPM=newBPM;
  beatMS=60000.0/BPM;
  beatsPerTempo=4.0;
  gridsByBeat=16.0;
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
  barOriginY=height*0.01;
}

void setupPadButtons() {
  buttonSize=width*0.153;
  buttonsOriginX=width*0.131;
  buttonMarginX=buttonSize*0.29;
  buttonMarginY=buttonSize*0.275;
  
  if(isAndroidDevice){
    buttonsOriginY=height*0.429;
  }else{
    buttonsOriginY=height*0.445;
  }
}

void setupSliders() {
  sliderWidth=29.0*(width*0.14/44.0);//width*0.12;
  sliderHeight=29.0*(height*0.06/44.0);//width*0.025;
  sliderOriginX=width*0.11;
  sliderMarginX=width*0.14;  
  sliderImage=loadImage("slider.png");
  
  if(isAndroidDevice){
    sliderMinY=height*0.17;
    sliderMaxY=height*0.3;    
  }
  else{
    sliderMinY=height*0.162;
    sliderMaxY=height*0.33;  
  }  
}

//synchronized 
void proccessTempoVars() {

  if (liveMode) {
    if (audioPlayThread.running)
      audioPlayThread.quit();
    return;
  }
  else {
    if (!audioPlayThread.running)
      audioPlayThread.start();
  }

  long ms=millis();
  long tms=audioPlayThread.getMillis();
  //println("Comparing times, main:"+ms+" audio thread:"+tms+" dif:"+(tms-ms));

  if (ms%tempoMS<tempoOffset) {
    notPlayedCues=savedCues.copy();
    notPlayedCues.sort();
    //println("restating loop:"+tempoOffset);
  }
  tempoOffset=ms%tempoMS;
  beatOffset=ms%beatMS;

  if (notPlayedCues.size()>0 && tempoOffset>=notPlayedCues.get(0)) {
    if (onlyOnePlayerMode) {
      if (soundByCue.hasKey(notPlayedCues.get(0)+"")) {
        int[] types = int(split(soundByCue.get(notPlayedCues.get(0)+""), '#'));
        for (int i=0;i<types.length;i++)
          playSoundType(types[i]);
      }
    }    
    //println("playing at:"+tempoOffset+" stored as:"+notPlayedCues.get(0)+" failGap:"+(tempoOffset-notPlayedCues.get(0)));
    lastPlayTime=notPlayedCues.get(0);
    notPlayedCues.remove(0);
    if (notPlayedCues.size()>0 && tempoOffset<notPlayedCues.get(0)) {
      int nextWait=(int)(notPlayedCues.get(0)-tempoOffset-2);
      //if(nextWait>0)
      //audioPlayThread.wait=nextWait;
      //else
      audioPlayThread.wait=1;
    }
    else {
      println("tempoOffset:"+tempoOffset+" nextOn:"+notPlayedCues.get(0)+" wait:"+audioPlayThread.wait);
      audioPlayThread.wait=1;
    }
  }
}

void drawBPM(){
  fill(0);
  //textFont(lcdFont, 20);
  textSize(20);
  text("BPM ["+floor(BPM*10.0)/10.0+"] ", barOriginX+barWidth*0.85, barOriginY+barHeight*2.5);
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
  for (int i=0;i<savedCues.size();i++) {
    fill(getColorByMs(savedCues.get(i)));
    if (!snapToGrid) {
      float cOffset=map(savedCues.get(i), 0.0, tempoMS, 0.0, barWidth-markerWidth);
      rect(barOriginX+cOffset-markerWidth*0.5, barOriginY, markerWidth, barHeight);
    }
    else {
      float cOffset=map(savedCues.get(i), 0.0, tempoMS, 0.0, barWidth);
      rect(barOriginX+cOffset, barOriginY, gridWidth, barHeight);
    }
  }   


  for (int i=0;i<beatsPerTempo;i++) {
    if(i!=0){
      fill(separatorColor);
      rect(beatsWidth*i+barOriginX-markerWidth*0.5, barOriginY, markerWidth, barHeight);
    }
    fill(255);
    if(i!=beatsPerTempo)
    for (int j=1;j<gridsByBeat;j++) {
      rect((beatsWidth*i)+gridWidth*j+barOriginX, barOriginY, markerWidth/4.0, barHeight);
    }
  }

  fill(markerColor);
  float offset=map(tempoOffset, 0.0, tempoMS, 0.0, barWidth-markerWidth);
  rect(barOriginX+offset, barOriginY, markerWidth, barHeight);
}

void drawPadsContainer(){
  strokeWeight(3);
  stroke(100,100,100,255);
  fill(50,50,50,255);
  rect(buttonsOriginX-buttonSize*0.2,buttonsOriginY-buttonSize*0.2,buttonSize*5.295,buttonSize*5.295);    
}

void drawPadButtons(color c, ClickablePad[] padArray, float buttonsOriginX, float buttonsOriginY, float buttonSize, float buttonMargin,String buttonText) {
  strokeWeight(3);
  stroke(100,100,100,255);
  for (int i=0;i<padArray.length;i++) {
    if (padArray[i]==null)
      padArray[i]=new ClickablePad(buttonsOriginX+(buttonSize+buttonMargin)*i, buttonsOriginY, buttonSize, buttonSize);
    padArray[i].text=buttonText+i;
    padArray[i].fillColor=c;    
    padArray[i].drawState();
  }
}

PVector getPadButtonOrigin(int soundType){
  int row=int(soundType/4);
  int col=soundType%4;
  return new PVector(buttonsOriginX+(buttonSize+buttonMarginX)*col,buttonsOriginY+(buttonSize+buttonMarginY)*row); 
}

void drawKickButtons() {
  drawPadButtons(redColor, kick, buttonsOriginX, buttonsOriginY, buttonSize, buttonMarginX,"K");
}

void drawBassButtons() {
  drawPadButtons(orangeColor, bass, buttonsOriginX, buttonsOriginY+buttonSize+buttonMarginY, buttonSize, buttonMarginX,"B");
}

void drawSnareButtons() {
  drawPadButtons(blueColor, snare, buttonsOriginX, buttonsOriginY+(buttonSize+buttonMarginY)*2, buttonSize, buttonMarginX,"S");
}

void drawHitHatButtons() {
  drawPadButtons(greenColor, hithat, buttonsOriginX, buttonsOriginY+(buttonSize+buttonMarginY)*3, buttonSize, buttonMarginX,"H");
}

void drawSliders() {
  for (int i=0;i<sliders.length;i++) {
    if (sliders[i]==null) {
      sliders[i]=new VerticalSlider(sliderOriginX+(sliderMarginX*i), sliderMinY+(sliderMaxY-sliderMinY)*0.5, sliderWidth, sliderHeight);
      sliders[i].limitY(sliderMinY, sliderMaxY);      
      if(i>2){
        sliders[i].y=sliderMinY;
      }
    }
    sliders[i].rollover(mouseX, mouseY);
    sliders[i].dragVertically(mouseY);    
    //image(sliderImage, sliders[i].x, sliders[i].y, sliders[i].w, sliders[i].h);
    //sliders[i].debugDisplay();
    sliders[i].draw();
  }
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
      println("total values:"+values.length+" value[0]:"+values[0]+" avg:"+avg);
      strokeWeight(1);
      //stroke(red(c),green(c),blue(c));
      stroke(c, 128);

      if(avg>=0){
        float fixY=10;
        if(isAndroidDevice)fixY=20;
        float ledWidth=maxLedWidth*avg;
        float ledX=buttonsOriginX+(buttonSize+buttonMarginX)*(soundType%4);
        float ledY=buttonsOriginY-fixY+(buttonSize+buttonMarginY)*(soundType/4);
        fill(c);        
        rect(ledX+maxLedWidth-ledWidth,ledY, ledWidth, ledHeight);
      }
      float xOffset=0;
      PVector origin=getPadButtonOrigin(soundType);
      originPowerSpecX=origin.x+1;
      originPowerSpecY=origin.y+buttonSize-(spectrumMode==BARS?2:3);      
      origin.y=origin.y+buttonSize-3-values[0]*height*0.1;
      origin.x+=3;      
      fill(100);
      for (int i=0;i<values.length;i+=values.length/10.0) {
        if(values[i]<0)values[i]=0;
        xOffset=map(i, 0, values.length, 0, buttonSize-4-(spectrumMode==BARS?6:0));
        switch(spectrumMode){
          case LINES:
            stroke(50);
            strokeWeight(3);
            if(i>=(int)(values.length/10.0)){
              PVector destiny=new PVector(originPowerSpecX+xOffset, originPowerSpecY-values[i]*height*0.1);
              line(origin.x,origin.y,destiny.x,destiny.y);
              origin=destiny;
            }
          break;
          case BARS:
            noStroke();
            PVector destiny=new PVector(originPowerSpecX+xOffset, originPowerSpecY-values[i]*height*0.1);
            rect(destiny.x,destiny.y,buttonSize/10,originPowerSpecY-destiny.y);
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
void draw()
{  
  clear();
  background(127,127,127);  
  //image(backTopMachine, 0, 0, width, 629.0*(width/440.0));
  //proccessTempoVars();
  drawTempoBar();
  drawPadsContainer();
  drawKickButtons();
  drawBassButtons();
  drawSnareButtons();
  drawHitHatButtons();
  drawBPM();
  //drawFiltersZone();
  drawSliders();
  drawPowerSpectrum();
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
  for(int i=0;i<kick.length;i++){
    if(kick[i].isOver(mouseX,mouseY)){
      
    }
    if(bass[i].isOver(mouseX,mouseY)){
      
    }
    if(snare[i].isOver(mouseX,mouseY)){
      
    }
    if(hithat[i].isOver(mouseX,mouseY)){
      
    }    
  }
  
}

void mouseDragged()
{
  for (int i=0;i<sliders.length;i++) {
    if(sliders[i].dragging){
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

void mouseReleased() {
  for (int i=0;i<sliders.length;i++) {
    sliders[i].stopDragging();
  }
  for (int i=0;i<kick.length;i++) {
    kick[i].stopClick();
    bass[i].stopClick();
    snare[i].stopClick();
    hithat[i].stopClick();
  }  
}

void mousePressed()
{
  for (int i=0;i<kick.length;i++) {
    if (kick[i].isClicked(mouseX, mouseY)) {
      addSoundTypeToList(kick.length*i);
    }
    else if (bass[i].isClicked(mouseX, mouseY)) {
      addSoundTypeToList(1+bass.length*i);
    }
    else if (snare[i].isClicked(mouseX, mouseY)) {
      addSoundTypeToList(2+snare.length*i);
    }
    else if (hithat[i].isClicked(mouseX, mouseY)) {
      addSoundTypeToList(3+snare.length*i);
    }
  }
  for (int i=0;i<sliders.length;i++) {
    sliders[i].clicked(mouseX, mouseY);
  }
  /*if(panelModeButton.isClicked(mouseX, mouseY)) {
   panelMode=PanelMode.fromInt((panelMode.value()+1)%4);
   }
   
   if(trackModeButton.isClicked(mouseX, mouseY)) {
   trackMode=(trackMode+1)%5;
   } */
}


void playSoundType(int sountType) {
  int soundNumber=sountType/4;
  switch(sountType%4) {
  case 0:
    playerKick[soundNumber].cue(0);
    playerKick[soundNumber].play();        
    break;
  case 1:
    playerBass[soundNumber].cue(0);
    playerBass[soundNumber].play();        
    break;
  case 2:
    playerSnare[soundNumber].cue(0);
    playerSnare[soundNumber].play();        
    break;
  case 3:
    playerHitHat[soundNumber].cue(0);
    playerHitHat[soundNumber].play();        
    break;
  }
}

color getColorByMs(float msOcc) {
  if (soundByCue.hasKey(msOcc+"")) {
    int[] types = int(split(soundByCue.get(msOcc+""), '#'));
    if (types.length>0) {
      color finalColor=getColorSoundType(types[0]);
      for (int i=1;i<types.length;i++)
        finalColor=lerpColor(finalColor, getColorSoundType(types[i]), 0.5);//1.0/types.length);
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
    float clickOffset=tempoOffset;
    if (snapToGrid) {
      float gridMS=beatMS/gridsByBeat;
      clickOffset=round(tempoOffset/gridMS)*gridMS;
    }
    if (savedCues.size()==0)
      switch(soundType) {
      case 0:
        firstKick=clickOffset;
        break;
      }
    //println("clicked on:"+millis()+"ms tempoOffset:"+clickOffset+"ms of tempoMS:"+tempoMS);
    savedCues.append(clickOffset);

    println("saved cues:"+savedCues);
    if (soundByCue.hasKey(clickOffset+""))
      soundByCue.set(clickOffset+"", soundByCue.hasKey(clickOffset+"")+"#"+soundType);
    else
      soundByCue.set(clickOffset+"", soundType+"");
  }
}

void keyPressed() {
  if (key == CODED) {
    //println("Pressed:"+keyCode);
    if (keyCode == UP) {
      if(BPM<140)
      changeBPM(BPM+1);
    } 
    else if (keyCode == DOWN) {
      if(BPM>80)
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
    switch(keyCode){
      case 32://SPACEBAR
        liveMode=!liveMode;
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

