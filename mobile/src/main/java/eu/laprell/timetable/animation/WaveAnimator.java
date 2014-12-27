package eu.laprell.timetable.animation;

import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;

import java.util.ArrayList;

/**
 * Created by david on 01.12.14.
 */
public class WaveAnimator extends CAnimator {

    private WaveAnimationApplier mApplier;
    private ArrayList<Holder>mTargets;
    private AnimatorSet mSet;

    /* Settings */

    private float mSpeed;
    private int[] mStartPoint;
    private boolean mStartImmediantlyFromFirstView;
    private boolean mComputeMoreExactly;

    private final Object mLock = new Object();

    private class Holder {
        private View target;
        private double distance;
        private Animator anim;
        private Object data;
    }

    public WaveAnimator(WaveAnimationApplier a) {
        mApplier = a;

        mSet = new AnimatorSet();
        mTargets = new ArrayList<Holder>();
    }

    public WaveAnimator addTarget(View v) {
        synchronized (mLock) {
            _addTarget(v, null);
        }
        return this;
    }

    public WaveAnimator addTarget(View v, Object data) {
        synchronized (mLock) {
            _addTarget(v, data);
        }
        return this;
    }

    private void _addTarget(View v, Object data) {
        Holder h = new Holder();
        h.target = v;
        h.data = data;

        mTargets.add(h);
    }

    public WaveAnimator setStartAnchorView(View v, boolean useCenter) {
        int[] l = new int[2];

        if(useCenter) {
            l[0] = l[0] + (v.getWidth() / 2);
            l[1] = l[1] + (v.getHeight() / 2);
        }

        setStartPoint(l);
        return this;
    }

    public WaveAnimator setStartPoint(int[] loc) {
        mStartPoint = loc;
        return this;
    }

    public boolean isStartImmediantlyFromFirstView() {
        return mStartImmediantlyFromFirstView;
    }

    public WaveAnimator setStartImmediantlyFromFirstView(boolean mStartImmediantlyFromFirstView) {
        this.mStartImmediantlyFromFirstView = mStartImmediantlyFromFirstView;
        return this;
    }

    public WaveAnimator addTargetsWithoutData(View...views) {
        synchronized (mLock) {
            for (View v : views) {
                addTarget(v, null);
            }
        }
        return this;
    }

    /**
     * Sets the speed in pixels per 100 ms
     * @param d the pixels per 100 ms
     * @return this
     */
    public WaveAnimator setSpeed(float d) {
        mSpeed = d;
        return this;
    }

    @SuppressWarnings("unchecked")
    public void start() {
        synchronized (mLock) {
            ArrayList<Holder> targets = new ArrayList<Holder>(mTargets.size());

            int pos;

            for(int i = 0;i < mTargets.size();i++) {
                Holder h = mTargets.get(i);

                double distance1 = mComputeMoreExactly ? getDistanceBetweenMoreExaclty(h.target, mStartPoint)
                        : getDistanceBetween(h.target, mStartPoint);

                h.distance = distance1;

                pos = -1;
                for(int x = i - 1;x >= 0; x--) {
                    if(distance1 <= targets.get(x).distance) {
                        pos = x;
                    }
                }

                if(pos == -1) {
                    targets.add(i, h);
                } else {
                    targets.add(pos, h);
                }
            }

            mTargets = targets;

            ArrayList<Animator> animations = new ArrayList<>(mTargets.size());

            long fDelay = 0;

            for (int i = 0;i < mTargets.size();i++) {
                Holder h = mTargets.get(i);
                h.anim = mApplier.makeAnimationForView(h.target, h.data);
                long delay = Math.round((h.distance / mSpeed) * 100);

                if(i == 0)
                    fDelay = delay;

                if(mStartImmediantlyFromFirstView) {
                    delay -= fDelay;
                }

                h.anim.setStartDelay(delay);

                animations.add(h.anim);
            }

            mSet.playTogether(animations);

            mSet.start();
        }
    }

    private int[] getViewLoc(View v) {
        int[] p = new int[2];
        v.getLocationOnScreen(p);
        return p;
    }

    private double getDistanceBetween(View a, int[]b) {
        return getDistanceBetween(getViewLoc(a), b);
    }

    /**
     * This method calculates the Distance between a view and a specific point, by
     * calculating the distance from the 9 points when dividing the view by 3 columns
     * and rows
     * @param a the view
     * @param b the point
     * @return the closest distance between them
     */
    private double getDistanceBetweenMoreExaclty(View a, int[]b) {
        final int[] loc = getViewLoc(a);

        final int w = a.getWidth();
        final int h = a.getHeight();

        double min_r1, min_r2, min_r3;

        /*    l m r
         *    _ _ _
         * 1 |_|_|_|
         * 2 |_|_|_|
         * 3 |_|_|_|
         */

        double xl, xr, xm;

        // ROW 1
        xl = getDistanceBetween(repositionBy(loc,     0,     0), b);
        xm = getDistanceBetween(repositionBy(loc, w / 2,     0), b);
        xr = getDistanceBetween(repositionBy(loc,     w,     0), b);
        min_r1 = Math.min(xr, Math.min(xl, xm));

        // ROW 2
        xl = getDistanceBetween(repositionBy(loc,     0, h / 2), b);
        xm = getDistanceBetween(repositionBy(loc, w / 2, h / 2), b);
        xr = getDistanceBetween(repositionBy(loc,     w, h / 2), b);
        min_r2 = Math.min(xr, Math.min(xl, xm));

        // ROW 3
        xl = getDistanceBetween(repositionBy(loc,     0,     h), b);
        xm = getDistanceBetween(repositionBy(loc, w / 2,     h), b);
        xr = getDistanceBetween(repositionBy(loc,     w,     h), b);
        min_r3 = Math.min(xr, Math.min(xl, xm));

        return Math.min(Math.min(min_r1, min_r2), min_r3);
    }

    private int[] repositionBy(int[] pos, int deltax, int deltay) {
        int[] p = new int[2];

        p[0] = pos[0] + deltax;
        p[1] = pos[1] + deltay;

        return p;
    }

    private double getDistanceBetween(int[]a, int[]b) {
        return Math.sqrt(Math.pow(b[0] - a[0], 2) + Math.pow(b[1] - a[1], 2));
    }

    public boolean isComputeMoreExactly() {
        return mComputeMoreExactly;
    }

    /**
     * When this option is used, the computation of the distances will take
     * more time, but will be more accurate <br>
     * Notice: not exactly, just a bit more
     * @param mComputeMoreExactly true or false
     */
    public void setComputeMoreExactly(boolean mComputeMoreExactly) {
        this.mComputeMoreExactly = mComputeMoreExactly;
    }

    public interface WaveAnimationApplier<T> {
        public Animator makeAnimationForView(View v, T data);
    }
}
