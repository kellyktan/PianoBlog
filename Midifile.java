import com.soundcloud.api.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.sound.midi.*;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.http.HttpResponse;

public class Midifile {
    public static enum Note {
        C4(60),
        D4(62),
        E4(64),
        F4(65),
        G4(67),
        A4(69),
        B4(71),
        C5(72),
        OFF(0);

        Note(int note) {
            this.note = note;
        }

        private final int note;

        public int getNote() {
            return note;
        }

        public static Note getNoteForString(String s) {
            switch(s) {
                case "c": return C4;
                case "D": return D4;
                case "E": return E4;
                case "F": return F4;
                case "G": return G4;
                case "A": return A4;
                case "B": return B4;
                case "C": return C5;
                default: return OFF;
            }
        }
    }

    public static void midifile(String recording, String filename) {
        System.out.println("midifile begin ");
        try
        {
            double ticksPerMillis = 0.005;

        //****  Create a new MIDI sequence with 25 ticks per beat  ****
            Sequence s = new Sequence(javax.sound.midi.Sequence.PPQ,25);

        //****  Obtain a MIDI track from the sequence  ****
            Track t = s.createTrack();

        //****  General MIDI sysex -- turn on General MIDI sound set  ****
            byte[] b = {(byte)0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte)0xF7};
            SysexMessage sm = new SysexMessage();
            sm.setMessage(b, 6);
            MidiEvent me = new MidiEvent(sm,(long)0);
            t.add(me);

        //****  set tempo (meta event)  ****
            MetaMessage mt = new MetaMessage();
            byte[] bt = {0x50, (byte)0x00, 0x00};
            mt.setMessage(0x51 ,bt, 3);
            me = new MidiEvent(mt,(long)0);
            t.add(me);

        //****  set track name (meta event)  ****
            mt = new MetaMessage();
            String TrackName = new String("midifile track");
            mt.setMessage(0x03 ,TrackName.getBytes(), TrackName.length());
            me = new MidiEvent(mt,(long)0);
            t.add(me);

        //****  set omni on  ****
            ShortMessage mm = new ShortMessage();
            mm.setMessage(ShortMessage.CONTROL_CHANGE, 0x7D,0x00);
            me = new MidiEvent(mm,(long)0);
            t.add(me);

        //****  set poly on  ****
            mm = new ShortMessage();
            mm.setMessage(ShortMessage.CONTROL_CHANGE, 0x7F,0x00);
            me = new MidiEvent(mm,(long)0);
            t.add(me);

        //****  set instrument to Piano  ****
            mm = new ShortMessage();
            mm.setMessage(ShortMessage.PROGRAM_CHANGE, 56, 0x00);
            me = new MidiEvent(mm,(long)0);
            t.add(me);

            BufferedReader reader = new BufferedReader(new StringReader(recording));
            long tick = 1;
            StringBuilder sequence = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                sequence.append(str);
            }
            String[] notes = sequence.toString().split(":");
            for (String current : notes) {
                if (!current.isEmpty()) {
                    String[] noteInfo = current.split("=");
                    Note note = Note.getNoteForString(noteInfo[0]);

                    if (note != Note.OFF) {
                        // note on
                        mm = new ShortMessage();
                        mm.setMessage(ShortMessage.NOTE_ON, note.getNote(), 0x60);
                        me = new MidiEvent(mm, tick);
                        t.add(me);
                        tick += Math.round(Long.parseLong(noteInfo[1]) * ticksPerMillis);

                        // note off
                        mm = new ShortMessage();
                        mm.setMessage(ShortMessage.NOTE_OFF, note.getNote(), 0x40);
                        me = new MidiEvent(mm, tick);
                        t.add(me);
                    } else {
                        tick += Math.round(Long.parseLong(noteInfo[1]) * ticksPerMillis);
                    }
                }
            }

        //****  set end of track (meta event)  ****
            mt = new MetaMessage();
            byte[] bet = {}; // empty array
            mt.setMessage(0x2F,bet,0);
            me = new MidiEvent(mt, tick);
            t.add(me);

        //****  write the MIDI sequence to a MIDI file  ****
            File f = new File(filename + ".mid");
            MidiSystem.write(s,1,f);
        } //try
            catch(Exception e)
        {
            System.out.println("Exception caught " + e.toString());
        } //catch
        System.out.println("midifile end ");
    }

    public static void convertToMp3(String filename) {
        try {
            Process p = Runtime.getRuntime().exec("timidity " + filename + ".mid -Ow");
            p.waitFor();
            System.out.println("making mp3");
            Process p2 = Runtime.getRuntime().exec("lame " + filename + ".wav " + filename + ".mp3");
            p2.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendToSoundCloud(String filename) {
        try {
            ApiWrapper wrapper = new ApiWrapper("client_id", "client_secret", null, null);
            wrapper.login("username", "password");
            File audioFile = new File(filename + ".mp3");
            audioFile.setReadable(true, false);
            SimpleDateFormat sdf = new SimpleDateFormat("MMMMM dd,yyyy hh:mm a");
            sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            Date resultdate = new Date(Long.parseLong(filename));
            HttpResponse resp = wrapper.post(Request.to(Endpoints.TRACKS)
                    .add(Params.Track.TITLE, sdf.format(resultdate))
                    .add(Params.Track.TAG_LIST, "pianoblog")
                    .withFile(Params.Track.ASSET_DATA, audioFile));
        } catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    public static void sendMidiFile(String filename) {
        try {
            final String userName = "username";
            final String password = "password";
            String host = "smtp.gmail.com";
            String port = "587";

            String toAddress = "email";
            String subject = "New Piano Blog Recording";

            Properties properties = new Properties();
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.user", userName);
            properties.put("mail.password", password);

            // creates a new session with an authenticator
            Authenticator auth = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, password);
                }
            };
            Session session = Session.getInstance(properties, auth);

            // creates a new e-mail message
            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(userName));
            InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
            msg.setRecipients(Message.RecipientType.TO, toAddresses);
            msg.setSubject(subject);
            msg.setSentDate(new Date());

            // creates multi-part
            Multipart multipart = new MimeMultipart();

            String[] attachFiles = new String[1];
            attachFiles[0] = filename + ".mp3";

            // adds attachments
            if (attachFiles != null && attachFiles.length > 0) {
                for (String filePath : attachFiles) {
                    MimeBodyPart attachPart = new MimeBodyPart();

                    try {
                        attachPart.attachFile(filePath);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    multipart.addBodyPart(attachPart);
                }
            }

            // sets the multi-part as e-mail's content
            msg.setContent(multipart);

            // sends the e-mail
            Transport.send(msg);

            System.out.println("successfully sent to email...");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
   }
}
