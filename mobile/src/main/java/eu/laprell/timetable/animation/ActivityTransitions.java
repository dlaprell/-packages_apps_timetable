package eu.laprell.timetable.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Application;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import eu.laprell.timetable.background.SpecialBitmapCache;
import eu.laprell.timetable.utils.BitmapUtils;
import eu.laprell.timetable.utils.MetricsUtils;
import eu.laprell.timetable.widgets.LessonCardView;

/**
 * Created by david on 22.11.14.
 */
public class ActivityTransitions {

    private static final float DIMEN_100DP = MetricsUtils.convertDpToPixel(100);
    private static ActivityTransitions sInstance;

    public static ActivityTransitions init(Application a) {
        if(a != null || sInstance == null || sInstance.app() != a) {
            sInstance = new ActivityTransitions(a);
        }
        return sInstance;
    }
    private static ActivityTransitions get() {
        return sInstance;
    }


    private ArrayList<Bitmap> mElements;
    private WeakReference<Application> mApp;

    private ActivityTransitions(Application a) {
        sInstance = this;

        mApp = new WeakReference<>(a);
        mElements = new ArrayList<>();
    }

    public void onTrimMemory(int level) {

    }

    private Application app() {
        return mApp.get();
    }

    private int addElement(Bitmap b) {
        mElements.add(b);

        return mElements.size() - 1;
    }

    private Bitmap getBitmap(int id) {
        return mElements.get(id);
    }

    private boolean isAvailable(int id) {
        return (id >= 0 && id < mElements.size() && mElements.get(id) != null);
    }

    private void clearRef(int id) {
        mElements.set(id, null);
    }

    public static void makeCircularRevealFromView(Intent i, View v) {
        int[] loc_s = new int[2];

        v.getLocationOnScreen(loc_s);

        i.putExtra("view_location_screen_x", loc_s[0]);
        i.putExtra("view_location_screen_y", loc_s[1]);
        i.putExtra("view_width", v.getWidth());
        i.putExtra("view_height", v.getHeight());
    }

    public static CircularRevealAnimationData createFromBundle(Bundle b) {
        CircularRevealAnimationData d = new CircularRevealAnimationData();

        d.locationX = b.getInt("view_location_screen_x", -1);
        d.locationY = b.getInt("view_location_screen_y", -1);
        d.viewWidth = b.getInt("view_width", -1);
        d.viewHeight = b.getInt("view_height", -1);

        if(d.locationX == -1 || d.locationY == -1
                || d.viewHeight == -1 || d.viewWidth == -1)
            return null;

        return d;
    }

    public static void animateFromBundle(Bundle b, final CircularRevealRelativeLayout view,
                                         final Runnable endAction, long time) {
        ActivityTransitions.CircularRevealAnimationData data
                = ActivityTransitions.createFromBundle(b);

        if(data == null)
            return;

        int[] location = new int[2];
        view.getLocationOnScreen(location);

        float mBeginScale, mBeginX, mBeginY;

        mBeginScale = Math.min(data.viewHeight, data.viewWidth) / 2;
        mBeginX = data.locationX - location[0] + mBeginScale;
        mBeginY = data.locationY - location[1] + mBeginScale;

        view.setMaskX(mBeginX);
        view.setMaskY(mBeginY);
        view.setMaskScale(mBeginScale);

        float mEndX, mEndY, mEndScale;

        mEndX = view.getWidth() / 2;
        mEndY = view.getHeight() / 2;
        mEndScale = Math.max(view.getHeight(), view.getWidth()) * 0.6f;

        ObjectAnimator moveCenterX = ObjectAnimator.ofFloat(view, "maskX", mBeginX, mEndX);
        ObjectAnimator moveCenterY = ObjectAnimator.ofFloat(view, "maskY", mBeginY, mEndY);
        ObjectAnimator scaleUp = ObjectAnimator.ofFloat(view, "maskScale", mBeginScale, mEndScale);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "maskAlpha", 0f, 1f);

        moveCenterX.setDuration(time / 2);
        moveCenterY.setDuration(time / 2);
        scaleUp.setDuration(time / 5 * 4);
        fadeIn.setDuration(time / 5);

        moveCenterX.setInterpolator(new AccelerateDecelerateInterpolator());
        moveCenterY.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleUp.setInterpolator(new AccelerateInterpolator());

        AnimatorSet set = new AnimatorSet();
        set.play(moveCenterX).with(moveCenterY).with(scaleUp).after(fadeIn);

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setEnableCircleDraw(false);

                if(endAction != null)
                    endAction.run();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        set.start();
    }

    public static void makeHeroTransitionFromLessonView(LessonCardView view, int color, BitmapUtils.ScalingConfig config, Intent out) {
        Rect onScreen = view.getImageLocationOnScreen();

        Log.d("Timetable", "make Herotransition: onScreen=" + onScreen.toString());

        out.putExtra("hero_transition", true);

        out.putExtra("hero_scaling_config", config);

        out.putExtra("hero_color", color);
        out.putExtra("hero_rect_from", onScreen);
        out.putExtra("hero_title", view.getTitle());

        if(view.getImage() != null) {
            Drawable drawable = null;

            // If its a transitiondrawable try to find the last drawable
            if(view.getImage() instanceof TransitionDrawable) {
                TransitionDrawable t = (TransitionDrawable) view.getImage();

                if(t.getNumberOfLayers() > 0) {
                    drawable = t.getDrawable(t.getNumberOfLayers() - 1);
                }
            } else { // else just take the one deliviered
                drawable = view.getImage();
            }

            // try to get the bitmap from the drawable
            if (drawable != null && drawable instanceof BitmapDrawable) {
                Bitmap b = ((BitmapDrawable) drawable).getBitmap();

                out.putExtra("hero_bitmap_id", get().addElement(b));
            }
        }
    }

    public static TransitionDrawable getHeroTransitionDrawable(Bundle in, Resources res,
                                                               int width, int height) {
        int heroColor = in.getInt("hero_color", Color.WHITE);
        int resId = in.getInt("hero_image_id", -1);

        BitmapUtils.ScalingConfig config = in.getParcelable("hero_scaling_config");

        Drawable end;
        Drawable start;

        int id = in.getInt("hero_bitmap_id");
        if(id != -1 && get().isAvailable(id)) {
            start = new BitmapDrawable(res, get().getBitmap(id));

            get().clearRef(id);
        } else if(config != null) {
            Bitmap b = BitmapUtils.scaleCenterCrop(
                    BitmapFactory.decodeResource(res, config.res_id), config);

            start = new BitmapDrawable(res, b);
        } else {
            start = new ColorDrawable(heroColor);
        }

        int endResId = config == null ? resId : config.res_id;
        if (endResId == 0 || endResId == -1)
            end = new ColorDrawable(heroColor);
        else
            end = new BitmapDrawable(res, SpecialBitmapCache.getInstance().loadBitmap(
                    endResId, width, height));

        end.setColorFilter(heroColor, PorterDuff.Mode.MULTIPLY);

        Drawable[] layers = new Drawable[] {
                start,
                end
        };

        return new TransitionDrawable(layers);
    }

    public static boolean animateHeroTransition(Bundle in, final ImageView imageTarget, HeroTransitionInterface i,
                                             long timePer100dp, final Runnable endAction,
                                             float width, float height) {
        if(in == null || !in.getBoolean("hero_transition", false)) {
            if(i != null) {
                i.setBackgroundColorAlpha(255);
            }
            if(endAction != null) {
                endAction.run();
            }
            return false;
        }

        TransitionDrawable drawable = getHeroTransitionDrawable(in, imageTarget.getResources(),
                imageTarget.getWidth(), imageTarget.getHeight());

        imageTarget.setImageDrawable(drawable);

        Rect origPosition = in.getParcelable("hero_rect_from");

        int[] locS = new int[2];
        imageTarget.getLocationOnScreen(locS);

        float beginX = origPosition.left - locS[0];
        float beginY = origPosition.top - locS[1];

        /*Log.d("Timetable", "Heroanimation props: origPosition=" + origPosition.toString()
                + " beginX=" + beginX + " beginY=" + beginY + " v_X=" + locS[0] + " v_Y=" + locS[1]);*/

        float beginScaleX = origPosition.width() / width;
        float beginScaleY = origPosition.height() / height;

        float distance = (float)Math.sqrt((beginX * beginX) + (beginY * beginY));

        long time = (long)(timePer100dp * (distance / DIMEN_100DP));
        time = Math.min(Math.max(100, time), 400);

        imageTarget.setPivotX(0);
        imageTarget.setPivotY(0);

        ObjectAnimator moveX = ObjectAnimator.ofFloat(imageTarget, View.TRANSLATION_X, beginX, 0f);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(imageTarget, View.TRANSLATION_Y, beginY, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageTarget, View.SCALE_X, beginScaleX, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageTarget, View.SCALE_Y, beginScaleY, 1f);

        ObjectAnimator fadeInB = ObjectAnimator.ofInt(i, "backgroundColorAlpha", 0, 255);

        AnimatorSet set = new AnimatorSet();
        set.play(moveX).with(moveY).with(scaleX).with(scaleY).with(fadeInB);
        set.setDuration(time);
        set.setInterpolator(new AccelerateDecelerateInterpolator());

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(endAction != null)
                    endAction.run();

                imageTarget.invalidate();
            }
        });

        set.start();

        drawable.startTransition((int)(time / 3));

        return true;
    }

    public interface HeroTransitionInterface {
        public void setBackgroundColorAlpha(int alpha);
    }

    public static class CircularRevealAnimationData {
        public int viewWidth, viewHeight, locationX, locationY;
    }
}
