class Midi{

  MidiBus myBus=null;
  PApplet processing; 

Midi(PApplet processing){
  this.processing=processing;
  //MidiBus.list();
  //myBus = new MidiBus(processing, 0, 0); // Create a new MidiBus with no input device and the default Java Sound Synthesizer as the output device.
}


void draw() {
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


void noteOn(int channel, int pitch, int velocity) {
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

void noteOff(int channel, int pitch, int velocity) {
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

void controllerChange(int channel, int number, int value) {
  // Receive a controllerChange
  println();
  println("Controller Change:");
  println("--------");
  println("Channel:"+channel);
  println("Number:"+number);
  println("Value:"+value);
}

}
