import javax.servlet.http.*;
import java.io.*;

public class PianoBlogServlet extends HttpServlet {

    public static String lastPost = "";

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String current;
            while ((current = reader.readLine()) != null) {
                sb.append(current);
            }

            String filename = String.valueOf(System.currentTimeMillis());
            lastPost = sb.toString();
            Midifile.midifile(sb.toString(), filename);
            Midifile.convertToMp3(filename);
            Midifile.sendToSoundCloud(filename);
            Midifile.sendMidiFile(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            String query = request.getQueryString();
            if (query != null && !query.isEmpty()) {
                String filename = String.valueOf(System.currentTimeMillis());
                lastPost = query;
                Midifile.midifile(query, filename);
                System.out.println("converting to wav");
                Midifile.convertToMp3(filename);
                System.out.println("finished converting to mp3");
                Midifile.sendToSoundCloud(filename);
                System.out.println("sent to soundcloud");
                Midifile.sendMidiFile(filename);
            }

            response.setContentType("text/html");
            PrintWriter writer = response.getWriter();
            writer.print("<html><h1>Welcome to the PianoBlog Servlet!</h1>");
            writer.print("<p>Here's the lastest tune we recieved:</p><p>");
            if (lastPost != null) {
                writer.print(lastPost);
            }
            writer.print("</p></html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
