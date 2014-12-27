package eu.laprell.timetable.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import eu.laprell.timetable.animation.RippleDrawable;

/**
 * Created by david on 09.11.14.
 */
public class RippleTouchImageView extends ImageView {

    private RippleDrawable mRipple;

    public RippleTouchImageView(Context context) {
        super(context);

        init();
    }

    public RippleTouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public RippleTouchImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && false) {

        } else {
            mRipple = new RippleDrawable();

            mRipple.setCallback(new RippleDrawable.RippleViewCallback(this));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if(mRipple != null)
            mRipple.setBounds(0, 0, w, h);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        // send the touch event to animator
        if(mRipple != null)
            mRipple.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // let animator show the animation by applying changes to view's canvas
        super.onDraw(canvas);

        if(mRipple != null)
            mRipple.draw(canvas);
    }
}
