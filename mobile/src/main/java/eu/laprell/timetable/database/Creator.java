package eu.laprell.timetable.database;

import java.util.ArrayList;

/**
 * Created by david on 13.11.14
 */
public abstract class Creator {

    private static final int TYPE_LESSON = 1;
    private static final int TYPE_BREAK = 2;

    public static class Data {
        public long place_id;
        public String place;
        public long lesson_id;
        public String lesson;
        public int color;
        public int day_of_week;
        public long time_id;
        public boolean is_break;
        public String teacher;
    }

    public static void createLessonFromData(TimetableDatabase db, Data data) {
        _make(db, data, TYPE_LESSON);
    }

    public static void createFreeTimeFromData(TimetableDatabase db, Data data) {
        _make(db, data, 2);
    }

    public static void createBreakFromData(TimetableDatabase db, Data data) {
        _make(db, data, TYPE_BREAK);
    }

    private static void _make(TimetableDatabase db, Data data, int type) {
        Day day = db.getDayForDayOfWeek(data.day_of_week);

        if(day == null) {
            day = new Day(-1);
            day.setDayOfWeek(data.day_of_week);
        }

        Lesson lessonToSave = null;

        if(type == TYPE_LESSON) {

            // First try to find a lesson that fits by Title
            lessonToSave = db.getLessonByTitle(data.lesson);

            // Else try to get the lesson by its id
            // TODO: maybe swap with instruction above (id more accurate)
            if (lessonToSave == null && data.lesson_id > 0)
                lessonToSave = (Lesson)db.getDatabaseEntryById(TimetableDatabase.TYPE_LESSON, data.lesson_id);

            // Nothing found, go ahead and create a empty one
            if (lessonToSave == null)
                lessonToSave = new Lesson(-1);

            // Add all infos that are missing
            lessonToSave.setTitle(data.lesson);

            // For color compare ignore the alpha value
            if ((data.color & 0x00FFFFFF) != 0)
                lessonToSave.setColor(data.color);

            if(data.teacher != null)
                lessonToSave.setTeacher(data.teacher);

            // Now save the lesson in the db
            if (lessonToSave.getId() == -1) {
                lessonToSave = (Lesson)db.insertDatabaseEntryForId(lessonToSave);
            } else {
                db.updateDatabaseEntry(lessonToSave);
            }
        } else if (type == TYPE_BREAK) {
            // A lesson with -1 id -> no lesson -> break
            lessonToSave = new Lesson(-1);
        }

        Place placeToSave = (Place)db.getDatabaseEntryById(TimetableDatabase.TYPE_PLACE, data.place_id);

        if(placeToSave == null && data.place != null)
            placeToSave = db.getPlaceByTitle(data.place);

        if(placeToSave == null && data.place != null) {
            placeToSave = new Place(-1);
            placeToSave.setTitle(data.place);
        }

        if(placeToSave != null) {
            if (placeToSave.getId() == -1) {
                placeToSave = (Place) db.insertDatabaseEntryForId(placeToSave);
            } else {
                db.updateDatabaseEntry(placeToSave);
            }
        } else {
            placeToSave = new Place(-1);
        }

        ArrayList<TimeUnit> times = new ArrayList<>();
        ArrayList<Lesson> lessons = new ArrayList<>();
        ArrayList<Place> places = new ArrayList<>();

        TimeUnit time = (TimeUnit) db.getDatabaseEntryById(TimetableDatabase.TYPE_TIMEUNIT, data.time_id);

        // Get current timeunits or make a new array
        long[] tids = day.getTimeUnits();
        if(tids == null)
            tids = new long[1];

        // Get current lessons or create a new array
        long[] lids = day.getLessons();
        if(lids == null)
            lids = new long[1];

        // Get current places or create new array
        long[] pids = day.getPlaces();
        if(pids == null)
            pids = new long[1];

        int pos = -1;

        for(int i = 0;i < tids.length;i++) {

            // Try to get timeunit from db, will return null, if nothing found
            TimeUnit timeAtCurPos = (TimeUnit) db.getDatabaseEntryById(TimetableDatabase.TYPE_TIMEUNIT, tids[i]);

            // If the lesson is not inserted (-> pos == -1)
            // and the "time" from the lesson to insert is before the cur Time
            // go ahead and add the lesson + place + time
            if(pos == -1 && (timeAtCurPos == null || time.isBefore(timeAtCurPos))) {
                times.add(time);
                pos = times.size() - 1;

                lessons.add(lessonToSave);
                places.add(placeToSave);
            }

            // If the current time is != null -> is existing and
            // it is not the same than the lesson go ahead and add
            // it back
            if(timeAtCurPos != null && !time.isMatching(timeAtCurPos)) {

                // First add the time
                times.add(timeAtCurPos);

                // Add Lesson by id (if in array) or create a empty one (id -> -1)
                if (i < lids.length && lids[i] > 0) {
                    lessons.add(new Lesson(lids[i]));
                } else {
                    lessons.add(new Lesson(-1));
                }

                // Same for place
                if(i < pids.length && pids[i] > 0) {
                    places.add(new Place(pids[i]));
                } else {
                    places.add(new Place(-1));
                }

            } else if(timeAtCurPos != null) {
                // The times were matching so go
                // ahead and just add it into the
                // list

                times.add(time);
                lessons.add(lessonToSave);
                places.add(placeToSave);
                pos = times.size() - 1;
            }
        }

        // If the data couldn't be inserted
        // add it to the end
        if(pos == -1) {
            times.add(time);
            lessons.add(lessonToSave);
            places.add(placeToSave);
        }

        tids = new long[times.size()];
        lids = new long[lessons.size()];
        pids = new long[places.size()];

        for(int i = 0;i < tids.length;i++) {
            tids[i] = times.get(i).getId();
            lids[i] = lessons.get(i).getId();
            pids[i] = places.get(i).getId();
        }

        day.setLessons(lids);
        day.setTimeUnits(tids);
        day.setPlaces(pids);

        if(day.getId() != -1)
            db.updateDatabaseEntry(day);
        else
            db.insertDatabaseEntryForId(day);
    }
}
