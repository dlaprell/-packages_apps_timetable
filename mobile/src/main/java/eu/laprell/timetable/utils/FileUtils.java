package eu.laprell.timetable.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by david on 25.01.15.
 */
public class FileUtils {

    public static String readFromfile(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append('\n');
        }
        reader.close();

        return out.toString();
    }

    public static String readFromfile(File file, int numLines) throws IOException {
        InputStream in = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in), 1024);

        String[] lines = new String[numLines];

        int pos = 0;

        String line;
        while ((line = reader.readLine()) != null) {
            lines[pos] = line;

            pos = ((pos + 1) % numLines);
        }
        reader.close();

        // Now build the final string

        StringBuilder out = new StringBuilder();
        int end = pos;

         do {
             pos++;
             if(pos >= numLines)
                 pos = 0;

            line = lines[pos];

            if(line != null) {
                out.append(line);
                out.append('\n');
            }

            lines[pos] = null;
        } while (pos != end);

        return out.toString();
    }
}
