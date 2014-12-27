package eu.laprell.timetable.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by david on 02.12.14.
 */
public class BitmapUtils {

    public static class ScalingConfig implements Parcelable {
        public float scale_x;
        public float scale_y;
        public int new_width;
        public int new_height;
        public float scaled_width;
        public float scaled_height;
        public float crop_x;
        public float crop_y;

        public int res_id;

        public ScalingConfig() {
            // empty
        }

        protected ScalingConfig(Parcel in) {
            scale_x = in.readFloat();
            scale_y = in.readFloat();
            new_width = in.readInt();
            new_height = in.readInt();
            scaled_width = in.readFloat();
            scaled_height = in.readFloat();
            crop_x = in.readFloat();
            crop_y = in.readFloat();
            res_id = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeFloat(scale_x);
            dest.writeFloat(scale_y);
            dest.writeInt(new_width);
            dest.writeInt(new_height);
            dest.writeFloat(scaled_width);
            dest.writeFloat(scaled_height);
            dest.writeFloat(crop_x);
            dest.writeFloat(crop_y);
            dest.writeInt(res_id);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<ScalingConfig> CREATOR = new Parcelable.Creator<ScalingConfig>() {
            @Override
            public ScalingConfig createFromParcel(Parcel in) {
                return new ScalingConfig(in);
            }

            @Override
            public ScalingConfig[] newArray(int size) {
                return new ScalingConfig[size];
            }
        };
    }

    public static Bitmap scaleCenterCrop(Bitmap source, int newWidth, int newHeight) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }

    public static ScalingConfig makeScaleConfigCenterCrop(Bitmap source, int newWidth, int newHeight) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        ScalingConfig c = new ScalingConfig();
        c.scale_x = xScale;
        c.scale_y = yScale;
        c.crop_x = left;
        c.crop_y = top;
        c.new_width = newWidth;
        c.new_height = newHeight;
        c.scaled_width = scaledWidth;
        c.scaled_height = scaledHeight;

        return c;
    }

    public static Bitmap scaleCenterCrop(Bitmap source, ScalingConfig c) {
        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(c.crop_x, c.crop_y,
                c.crop_x + c.scaled_width, c.crop_y + c.scaled_height);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(c.new_width, c.new_height, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }

    public static Bitmap decodeResource(Resources res, int resId, ScalingConfig c) {
        return null;
    }
}
