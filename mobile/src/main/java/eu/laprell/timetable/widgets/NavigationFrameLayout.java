package eu.laprell.timetable.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by david on 17.11.14.
 */
public class NavigationFrameLayout extends FrameLayout {

    private boolean mEnableTouchEventsForChildren = true;
    private boolean mEnableTouchInGeneral = true;

    public NavigationFrameLayout(Context context) {
        super(context);

        init();
    }

    public NavigationFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public NavigationFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {

    }

    public boolean isEnableTouchEventsForChildren() {
        return mEnableTouchEventsForChildren;
    }

    public void setEnableTouchEventsForChildren(boolean mEnableTouchEventsForChilds) {
        this.mEnableTouchEventsForChildren = mEnableTouchEventsForChilds;
    }

    public boolean isEnableTouchInGeneral() {
        return mEnableTouchInGeneral;
    }

    public void setEnableTouchInGeneral(boolean mEnableTouchInGeneral) {
        this.mEnableTouchInGeneral = mEnableTouchInGeneral;
    }
}
