package eu.laprell.timetable.animation;

import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by david on 04.12.14.
 */
public abstract class CAnimator {

    public abstract void start();

    public void startOnPreDraw(final View from) {
        from.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                from.getViewTreeObserver().removeOnPreDrawListener(this);

                start();
                return true;
            }
        });
    }
}
