package eu.laprell.timetable.utils;

import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by david on 19.12.14.
 */
public class RemoteViewUtils {

    /*
     *
     * @hide
     * Equivalent to calling a combination of {@link Drawable#setAlpha(int)},
     * {@link Drawable#setColorFilter(int, android.graphics.PorterDuff.Mode)},
     * and/or {@link Drawable#setLevel(int)} on the {@link Drawable} of a given
     * view.
     * <p>
     * You can omit specific calls by marking their values with null or -1.
     *
     * @param viewId The id of the view that contains the target
     *            {@link Drawable}
     * @param targetBackground If true, apply these parameters to the
     *            {@link Drawable} returned by
     *            {@link android.view.View#getBackground()}. Otherwise, assume
     *            the target view is an {@link ImageView} and apply them to
     *            {@link ImageView#getDrawable()}.
     * @param alpha Specify an alpha value for the drawable, or -1 to leave
     *            unchanged.
     * @param colorFilter Specify a color for a
     *            {@link android.graphics.ColorFilter} for this drawable. This will be ignored if
     *            {@code mode} is {@code null}.
     * @param mode Specify a PorterDuff mode for this drawable, or null to leave
     *            unchanged.
     * @param level Specify the level for the drawable, or -1 to leave
     *            unchanged.
     *
    public void setDrawableParameters(int viewId, boolean targetBackground, int alpha,
                                      int colorFilter, PorterDuff.Mode mode, int level) {
        addAction(new SetDrawableParameters(viewId, targetBackground, alpha,
                colorFilter, mode, level));
    }
     */
    public static boolean setDrawableParameters(@NonNull RemoteViews target, int viewId,
                                                boolean targetBackground, int alpha,
                                                int colorFilter, PorterDuff.Mode mode, int level) {
        Class<RemoteViews> clazz = RemoteViews.class;

        Method[] ms = clazz.getDeclaredMethods();

        for(Method m : ms) {
            if(m.getName().equals("setDrawableParameters")) {
                Class[] classes = m.getParameterTypes();

                String print = "setDrawableParameters(";
                boolean first = true;
                for (Class c : classes) {
                    if(first) first = false;
                    else print += ", ";

                    print += c.getSimpleName();
                }

                print += ")";

                Log.d("Timetable", "Found: " + print);
            }
        }

        Method method;
        try {
            method = clazz.getDeclaredMethod("setDrawableParameters", int.class, boolean.class, int.class, int.class, PorterDuff.Mode.class, int.class);
        } catch (NoSuchMethodException e) {
            Log.e("Timetable", "Failed to use @hide api", e);
            return false;
        }

        try {
            method.invoke(target, viewId, targetBackground, alpha, colorFilter, mode, level);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Log.e("Timetable", "Failed to use @hide api", e);
            return false;
        }
        return true;
    }
}
