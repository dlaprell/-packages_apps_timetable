package eu.laprell.timetable.database;

import android.content.Context;
import android.content.res.TypedArray;

import eu.laprell.timetable.R;

/**
 * Created by david on 23.12.14
 */
public class TimetableDatabase extends AbsTimetableDatabase {

    public TimetableDatabase(Context c) {
        super(c);
    }

    public Lesson getLessonByTitle(String title) {
        String selection = LessonEntry.COLUMN_NAME_TITLE + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { title };

        return (Lesson)queryDatabaseEntry(TYPE_LESSON, selection, selectionArgs);
    }

    public Day getDayForDayOfWeek(int dayOfWeek) {
        // Define 'where' part of query.
        String selection = DayEntry.COLUMN_NAME_DAY_OF_WEEK + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(dayOfWeek)};

        Day d = (Day) queryDatabaseEntry(TYPE_DAY, selection, selectionArgs);

        if(d == null) {
            d = new Day(-1, dayOfWeek);
            d.setLessons(new long[0]);
            d.setPlaces(new long[0]);
            d.setTimeUnits(new long[0]);
            d = (Day) insertDatabaseEntryForId(d);
        }

        return d;
    }

    public TimeUnit[] getTimeUnitsByIds(long[] ids) {
        TimeUnit[] t = new TimeUnit[ids.length];
        for(int i = 0;i < t.length;i++)
            t[i] = (TimeUnit)getDatabaseEntryById(TYPE_TIMEUNIT, ids[i]);
        return t;
    }

    /**
     * Helper function to get an appropriate (Image)Resources Id
     * for the given {@link eu.laprell.timetable.database.Lesson} <br>
     * This method will either use the {@link Lesson#getImageId()} or
     * the default Image for the given Lesson
     * @param l the lesson for which the image id is searched
     * @return a Resources Id if one was found, or -1
     */
    public int getImageIdForLesson(Lesson l) {
        int index = -1;
        int res = -1;

        if(l != null && !isNoId(l.getImageId())) {
            return ImageDb.getImageById(l.getImageId());
        } else if (l == null) {
            return -1;
        }

        String[] subs = getContext().getResources().getStringArray(R.array.array_subjects);

        for(int i = 0;i < subs.length && index == -1;i++) {
            if(subs[i].equalsIgnoreCase(l.getTitle()))
                index = i;
        }

        if(index != -1) {
            TypedArray ids = getContext().getResources().obtainTypedArray(R.array.array_subjects_images);

            // Get resource id by its index
            res = ids.getResourceId(index, -1);

            // be sure to call TypedArray.recycle() when done with the array
            ids.recycle();
        }

        return res;
    }

    public int[] getImageList() {
        TypedArray ids = getContext().getResources().obtainTypedArray(R.array.array_subjects_images_all);

        int[]images = new int[ids.length()];

        for(int i = 0;i < images.length;i++) {
            images[i] = ids.getResourceId(i, -1);
        }

        // be sure to call TypedArray.recycle() when done with the array
        ids.recycle();

        return images;
    }

    public Lesson[] getLessonsByIds(long[] lids) {
        Lesson[] les = new Lesson[lids.length];

        for (int i = 0;i < les.length;i++) {
            les[i] = (Lesson) getDatabaseEntryById(TimetableDatabase.TYPE_LESSON, lids[i]);
        }

        return les;
    }

    public Place[] getPlacesByIds(long[] ids) {
        Place[] t = new Place[ids.length];
        for(int i = 0;i < t.length;i++)
            t[i] = (Place) getDatabaseEntryById(TYPE_PLACE, ids[i]);
        return t;
    }

    public Place getPlaceByTitle(String title) {
        // Define 'where' part of query.
        String selection = PlaceEntry.COLUMN_NAME_TITLE + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { title };

        return (Place) queryDatabaseEntry(TYPE_PLACE, selection, selectionArgs);
    }
}
