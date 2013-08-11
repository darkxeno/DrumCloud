package com.codefixia.audio;

import com.codefixia.drumcloud.DrumCloud;

import processing.core.PApplet;

public class Midi{

  //MidiBus myBus=null;
  PApplet processing; 

public Midi(PApplet processing){
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
  DrumCloud.println();
  DrumCloud.println("Note On:");
  DrumCloud.println("--------");
  DrumCloud.println("Channel:"+channel);
  DrumCloud.println("Pitch:"+pitch);
  DrumCloud.println("Velocity:"+velocity);
  //if(myBus!=null)
    //myBus.sendNoteOn(channel, pitch, velocity);
}

public void noteOff(int channel, int pitch, int velocity) {
  // Receive a noteOff
  DrumCloud.println();
  DrumCloud.println("Note Off:");
  DrumCloud.println("--------");
  DrumCloud.println("Channel:"+channel);
  DrumCloud.println("Pitch:"+pitch);
  DrumCloud.println("Velocity:"+velocity);
  //if(myBus!=null)
    //myBus.sendNoteOff(channel, pitch, velocity);
}

public void controllerChange(int channel, int number, int value) {
  // Receive a controllerChange
  DrumCloud.println();
  DrumCloud.println("Controller Change:");
  DrumCloud.println("--------");
  DrumCloud.println("Channel:"+channel);
  DrumCloud.println("Number:"+number);
  DrumCloud.println("Value:"+value);
}

}