package eu.laprell.timetable.utils;

import android.content.Context;
import android.content.Intent;

import eu.laprell.timetable.R;

/**
 * Created by david on 11.01.15.
 */
public class IntentUtils {

    public static void shareText(Context con, String subject, String text, String title) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);

        con.startActivity(Intent.createChooser(sharingIntent, title));
    }
}
