package eu.laprell.timetable.database;

import eu.laprell.timetable.R;

/**
 * Created by david on 19.12.14.
 */
public class ImageDb {
    
    private static final int[] sImages = new int[] {
            R.drawable.book_grey,
            R.drawable.writing_grey,
            R.drawable.biology_grey,
            R.drawable.optics_grey,
            R.drawable.info_grey,
            R.drawable.running_grey,
            R.drawable.chemistry_grey,
            R.drawable.music_grey,
            R.drawable.chor_grey,
            R.drawable.history_grey,
            R.drawable.family_grey,
            R.drawable.map_compass_grey,
            R.drawable.cross_grey,
            R.drawable.thinking_grey,
            R.drawable.politics_grey,
            R.drawable.city_grey,
            R.drawable.latin_grey,
            R.drawable.paris_grey,
            R.drawable.art_grey,
            R.drawable.math_grey,
            R.drawable.bundestag_grey,
            R.drawable.capitol_grey,
            R.drawable.choice_grey,
            R.drawable.colloseum_grey,
            R.drawable.dance_grey,
            R.drawable.elephant_grey,
            R.drawable.geese_grey,
            R.drawable.justice_grey,
            R.drawable.microscope_grey,
            R.drawable.paintings_grey,
            R.drawable.palma_grey,
            R.drawable.plasma_grey,
            R.drawable.plasma_lamp_grey,
            R.drawable.stage_grey,
            R.drawable.thinker_grey,
            R.drawable.weights_grey,      
    };

    private static final int[] sAvailibleIds;

    static {
        sAvailibleIds = new int[sImages.length];
        for(int i = 0;i < sImages.length;i++)
            sAvailibleIds[i] = i + 1;
    }

    public static int getImageById(int id) {
        return sImages[id - 1];
    }

    public static int[] getAllImageIds() {
        return sAvailibleIds;
    }

    public static boolean isValidId(int id) {
        return (id > 0 && id <= sImages.length);
    }

    public static int getIdByImage(int resid) {
        for (int i = 0;i < sImages.length;i++) {
            if(sImages[i] == resid)
                return sAvailibleIds[i];
        }

        return -1;
    }
 }
