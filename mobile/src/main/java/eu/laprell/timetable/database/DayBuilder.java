package eu.laprell.timetable.database;

import java.util.ArrayList;

/**
 * Created by david on 11.11.14
 */
public class DayBuilder {
    private ArrayList<Lesson> mLessons;
    private ArrayList<TimeUnit> mTime;
    private Day mDay;

    private DayBuilder() {
        mLessons = new ArrayList<Lesson>();
        mTime = new ArrayList<TimeUnit>();
    }

    public void addLesson(Lesson l, int starth, int endh, int startm , int endm) {
        addLesson(l, starth * 60 + startm, startm * 60 + endm);
    }

    public void addFreeLesson(long tid) {
        mLessons.add(null);
        mTime.add(new TimeUnit(tid));
    }

    public void addLesson(Lesson l, int start, int end) {
        mLessons.add(l);
        TimeUnit t = new TimeUnit(-1);
        t.setStartTime(start);
        t.setEndTime(end);
        mTime.add(t);
    }

    public void addLesson(long id, long tid) {
        mLessons.add(new Lesson(id));
        mTime.add(new TimeUnit(tid));
    }

    public void addLesson(long id, TimeUnit t) {
        mLessons.add(new Lesson(id));
        mTime.add(new TimeUnit(id));
    }

    public void addBreak(int starth, int endh, int startm , int endm) {
        addBreak(starth * 60 + startm, startm * 60 + endm);
    }

    public void addBreak(int start, int end) {
        mLessons.add(null);
        TimeUnit t = new TimeUnit(-1);
        t.setBreak(true);
        t.setStartTime(start);
        t.setEndTime(end);
        mTime.add(t);
    }

    public void addBreak(long id) {
        mLessons.add(null);
        mTime.add(new TimeUnit(id));
    }

    public void setDayOfWeek(int day) {
        mDay.setDayOfWeek(day);
    }

    public void insertIntoDb(TimetableDatabase db) {
        long[] lids = new long[mTime.size()];
        long[] tids = new long[mTime.size()];

        for(int i = 0;i < lids.length;i++) {
            TimeUnit t = mTime.get(i);

            if(t.isBreak() || mLessons.get(i) == null) {
                lids[i] = -1;
            } else {
                if(mLessons.get(i).getId() == -1) {
                    lids[i] = db.insertDatabaseEntryForId(mLessons.get(i)).getId();
                } else {
                    lids[i] = mLessons.get(i).getId();
                }
            }

            if(mTime.get(i).getId() == -1) {
                tids[i] = db.insertDatabaseEntryForId(mTime.get(i)).getId();
            } else {
                tids[i] = mTime.get(i).getId();
            }
        }

        mDay.setLessons(lids);
        mDay.setTimeUnits(tids);

        db.insertDatabaseEntryForId(mDay);
    }

    public static DayBuilder create(int dayOfWeek) {
        DayBuilder b = new DayBuilder();
        b.mDay = new Day(-1);
        b.mDay.setDayOfWeek(dayOfWeek);
        return b;
    }
}
