package eu.laprell.timetable.fragments.interfaces;

import eu.laprell.timetable.database.Lesson;
import eu.laprell.timetable.database.Place;
import eu.laprell.timetable.database.Teacher;
import eu.laprell.timetable.database.TimeUnit;

/**
 * Created by david on 22.12.14
 */
public interface LessonViewController {
    public Lesson getLesson();
    public void makeLessonDirty();

    public int getDay();

    public Place getPlace();
    public void setPlace(Place p);
    public void makePlaceDirty();

    public TimeUnit getTimeUnit();

    public Teacher getTeacher();
    public void setTeacher(Teacher t);
    public void makeTeacherDirty();
}
