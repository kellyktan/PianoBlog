#include <SPI.h>
#include <Ethernet.h>

#define NO_NOTE  0
#define NOTE_C4  262
#define NOTE_D4  294
#define NOTE_E4  330
#define NOTE_F4  349
#define NOTE_G4  392
#define NOTE_A4  440
#define NOTE_B4  494
#define NOTE_C5  523

const int SPEAKER = A2; // pin for SPEAKER
const int LED = A1;

const int BUTTON_C1 = 2; // pin for BUTTON
const int BUTTON_D = 3; // pin for BUTTON
const int BUTTON_E = 4; // pin for BUTTON
const int BUTTON_F = 5; // pin for BUTTON
const int BUTTON_G = 6; // pin for BUTTON
const int BUTTON_A = 7; // pin for BUTTON
const int BUTTON_B = 8; // pin for BUTTON
const int BUTTON_C2 = 9; // pin for BUTTON
const int BUTTON_REC = A0; // pin for BUTTON

// MAC address and IP address
byte mac[] = {
  0x90, 0xA2, 0xDA, 0x0F, 0xBE, 0xF3
};

char pianoBlogServer[] = "<insert EC2 instance DNS here>";

int rec = 0;
int prevRec = 0;
int recState = 0;
int prevNote = NO_NOTE;
long start = 0;
String recording;

EthernetClient client;

void setup() {
  // put your setup code here, to run once:
  pinMode(BUTTON_C1, INPUT);
  pinMode(BUTTON_D, INPUT);
  pinMode(BUTTON_E, INPUT);
  pinMode(BUTTON_F, INPUT);
  pinMode(BUTTON_G, INPUT);
  pinMode(BUTTON_A, INPUT);
  pinMode(BUTTON_B, INPUT);
  pinMode(BUTTON_C2, INPUT);
  pinMode(BUTTON_REC, INPUT);
  pinMode(SPEAKER, OUTPUT);
  pinMode(LED, OUTPUT);

  // start the Ethernet connection:
  if (Ethernet.begin(mac) == 0) {
    // Failed to configure Ethernet using DHCP
    tone(SPEAKER, NOTE_C4);
    delay(400);
    noTone(SPEAKER);
  } else {
    tone(SPEAKER, NOTE_C5);
    delay(400);
    noTone(SPEAKER);
  }
}

void loop() {

  // get note
  int note;
  if (digitalRead(BUTTON_C1) == HIGH) {
    note = NOTE_C4;
  } else if (digitalRead(BUTTON_D) == HIGH) {
    note = NOTE_D4;
  } else if (digitalRead(BUTTON_E) == HIGH) {
    note = NOTE_E4;
  } else if (digitalRead(BUTTON_F) == HIGH) {
    note = NOTE_F4;
  } else if (digitalRead(BUTTON_G) == HIGH) {
    note = NOTE_G4;
  } else if (digitalRead(BUTTON_A) == HIGH) {
    note = NOTE_A4;
  } else if (digitalRead(BUTTON_B) == HIGH) {
    note = NOTE_B4;
  } else if (digitalRead(BUTTON_C2) == HIGH) {
    note = NOTE_C5;
  } else {
    note = NO_NOTE;
  }

  // play to speaker
  if (note == NO_NOTE) {
    noTone(SPEAKER);
  } else {
    tone(SPEAKER, note);
  }

  // recored note
  rec = digitalRead(BUTTON_REC);
  if (rec == HIGH && prevRec == LOW) {
      recState = 1 - recState;
      if (recState == 1) {
        prevNote = note;
        start = millis();
        recording = "";
      } else {
        long end = millis();
        if (recording.length() + (getStringForNote(prevNote) + "=" + (end - start) + ":").length() < 170) {
          recording = recording + getStringForNote(prevNote) + "=" + (end - start) + ":";
        }
        sendToPianoBlog(recording);
      }
      delay(10);
  } else if (rec == LOW && prevRec == HIGH) {
    delay(10);
  }
  
  if (recState == 1) {
    digitalWrite(LED, HIGH);
    if (prevNote != note) {
      // note was changed
      long end = millis();
      if (recording.length() + (getStringForNote(prevNote) + "=" + (end - start) + ":").length() < 170) {
        recording = recording + getStringForNote(prevNote) + "=" + (end - start) + ":";
        start = end;
      } else {
        sendToPianoBlog(recording);
        recState = 0;
      }
    }
  } else {
    digitalWrite(LED, LOW);
  }
  prevRec = rec;
  prevNote = note;
}

String getStringForNote(int note) {
  if (note == NOTE_C4) {
    return "C4";
  } else if (note == NOTE_D4) {
    return "D4";
  } else if (note == NOTE_E4) {
    return "E4";
  } else if (note == NOTE_F4) {
    return "F4";
  } else if (note == NOTE_G4) {
    return "G4";
  } else if (note == NOTE_A4) {
    return "A4";
  } else if (note == NOTE_B4) {
    return "B4";
  } else if (note == NOTE_C5) {
    return "C5";
  } else {
    return "X";
  }
}

void sendToPianoBlog(String recording) {
  tone(SPEAKER, NOTE_C5);
  delay(100);
  noTone(SPEAKER);
  if (client.connect(pianoBlogServer, 80)) {
    client.print("POST / HTTP/1.1\n");
    client.print("Host: ");
    client.print(pianoBlogServer);
    client.print("\n");
    client.print("Connection: close\n");
    client.print("Content-Type: text/html\n");
    client.print("Content-Length: ");
    client.print(recording.length());
    client.print("\n\n");
    client.print(recording);
    tone(SPEAKER, NOTE_C4);
    delay(100);
  }
  client.stop();
  // start the Ethernet connection:
  if (Ethernet.begin(mac) == 0) {
    // Failed to configure Ethernet using DHCP
    tone(SPEAKER, NOTE_C4);
    delay(100);
    noTone(SPEAKER);
    for(;;)
      ;
  } else {
    tone(SPEAKER, NOTE_C5);
    delay(100);
    noTone(SPEAKER);
  }
}

