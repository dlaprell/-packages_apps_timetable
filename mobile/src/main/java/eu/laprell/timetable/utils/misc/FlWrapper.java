package eu.laprell.timetable.utils.misc;

/**
 * Created by david on 18.01.15.
 */
public class FlWrapper {
    public float mValue;

    public FlWrapper() {

    }

    public FlWrapper(float f) {
        mValue = f;
    }

    public static FlWrapper of(float f) {
        return new FlWrapper(f);
    }
}
