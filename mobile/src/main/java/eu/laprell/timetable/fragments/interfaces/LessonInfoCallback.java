package eu.laprell.timetable.fragments.interfaces;

import android.view.View;

import eu.laprell.timetable.animation.WaveAnimator;

/**
 * Created by david on 22.12.14.
 */
public interface LessonInfoCallback {
    public void prepareEnterAnimationAlpha(WaveAnimator animator);
    public WaveAnimator getGoInEditModeAnimation();
    public View getMainView();
    public void exitEditMode();
}
