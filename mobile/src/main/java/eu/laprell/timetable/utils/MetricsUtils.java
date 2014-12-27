package eu.laprell.timetable.utils;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by david on 06.11.14.
 */
public class MetricsUtils {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private static final Resources sRes = Resources.getSystem();

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp){
        Resources resources = sRes;
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px){
        Resources resources = sRes;
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    public static float convertSpToPixel(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, sRes.getDisplayMetrics());
    }

    /**
     * Generate a value suitable for use in {@link #setId(int)}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    /**
     * Utility to return a view's standard measurement. Uses the
     * supplied size when constraints are given. Attempts to
     * hold to the desired size unless it conflicts with provided
     * constraints.
     *
     * @param measureSpec Constraints imposed by the parent
     * @param contentSize Desired size for the view
     * @return The size the view should be.
     */
    public static int getMeasurement(int measureSpec, int contentSize) {
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);
        int resultSize = 0;
        switch (specMode) {
            case View.MeasureSpec.UNSPECIFIED:
                //Big as we want to be
                resultSize = contentSize;
                break;
            case View.MeasureSpec.AT_MOST:
                //Big as we want to be, up to the spec
                resultSize = Math.min(contentSize, specSize);
                break;
            case View.MeasureSpec.EXACTLY:
                //Must be the spec size
                resultSize = specSize;
                break;
        }

        return resultSize;
    }

    /**
     *
     * @param radius the radius of the circle
     * @param degres in radians!
     * @param centerx the center of the circle on the x axis
     * @return the x position being calculated
     */
    public static float getXLocationOnCircle(float radius, float degres, float centerx) {
        return (float)(radius * Math.cos(degres) + centerx);
    }

    /**
     *
     * @param radius the radius of the circle
     * @param degres in radians!
     * @param centery the center of the circle on the y axis
     * @return the y position being calculated
     */
    public static float getYLocationOnCircle(float radius, float degres, float centery) {
        return (float)(radius * Math.sin(degres) + centery);
    }
}
