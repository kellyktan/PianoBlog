# PianoBlog
======

## The Project:

I really enjoy piano and music, and so I thought it would be fun to try and make a mini-piano for my ESE 190 final project.  It's also fun sharing music, so I wanted to have the capability to easily record a short song and automatically publish it to SoundCloud!  You can view the latest recordings here: https://soundcloud.com/pianoblog

The recording is done by first keeping track of a short sequence on the Arduino, and then sending a POST request to a Java servlet I deployed on an Amazon Web Services EC2 instance.  Upon receiving the request, the servlet converts the sequence of notes into a MIDI file.  It then uses Timidity++ to convert the MIDI file into a WAV file, and finally uses Lame to convert it into an MP3 file.  Then it utilizes the SoundCloud API to publish the MP3 file directly to the PianoBlog SoundCloud account.  The servlet also emails the MP3 file to me as a backup.

## References:

Java MIDI tutorial:  http://www.automatic-pilot.com/midifile.html

Convert MIDI to MP3:  http://stackoverflow.com/questions/8108672/midi-to-mp3-automated-conversion-with-randomly-generated-synths

SoundCloud API tutorial: http://stackoverflow.com/questions/19588444/how-to-upload-audio-in-soundcloud-via-my-app-and-i-want-to-send-that-id-also-to

Java Email tutorial: http://www.tutorialspoint.com/javamail_api/javamail_api_send_email_with_attachment.htm

Timidity++: http://timidity.sourceforge.net/

Lame: http://lame.sourceforge.net/
