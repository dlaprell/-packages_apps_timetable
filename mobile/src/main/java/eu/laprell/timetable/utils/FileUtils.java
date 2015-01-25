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
}
