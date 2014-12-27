package eu.laprell.timetable.background;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;

/**
 * Created by david on 18.12.14.
 */
public class SpecialBitmapCache {

    public static void init(Application a) {
        if(sCache == null && a != null) {
            new SpecialBitmapCache(a);
        }
    }
    public static SpecialBitmapCache getInstance() {
        return sCache;
    }
    private static SpecialBitmapCache sCache;

    private LruCache<Integer, Bitmap> mBitmaps;
    private Application mApp;

    private SpecialBitmapCache(Application a) {
        mApp = a;
        sCache = this;

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mBitmaps = new CustomLruCache(cacheSize);
    }

    private class CustomLruCache extends LruCache<Integer, Bitmap>{
        /**
         * @param maxSize for caches that do not override {@link #sizeOf}, this is
         *                the maximum number of entries in the cache. For all other caches,
         *                this is the maximum sum of the sizes of the entries in this cache.
         */
        public CustomLruCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(Integer key, Bitmap value) {
            return value.getByteCount() / 1024; // kByte
        }
    }

    public Bitmap loadBitmap(int resId, int minSizeW, int minSizeH) {
        Bitmap b = mBitmaps.get(resId);

        if(b == null) {
            BitmapFactory.Options opt = loadBitmapOptions(resId);

            int scale = getMinScaleFactor(opt.outWidth, opt.outHeight, minSizeW, minSizeH);
            opt.inJustDecodeBounds = false;
            opt.inSampleSize = scale;

            b = BitmapFactory.decodeResource(mApp.getResources(), resId, opt);

            mBitmaps.put(resId, b);

            Log.d("Timetable", "Created a new Bitmap in cache: " + resId + " cache is now: " + mBitmaps.size());
        }

        return b;
    }

    private int getMinScaleFactor(int bW, int bH, int tW, int tH) {
        if(tW == 0 || tH == 0)
            return 1;

        int scale = Math.min(bW / tW, bH / tH);

        if (scale <= 1)
            return 1;

        while(scale != 1 && scale != 2 && scale != 4 && scale != 8 && scale != 16) {
            scale--;
        }

        return scale;
    }

    private BitmapFactory.Options loadBitmapOptions(int resId) {
        BitmapFactory.Options opt  = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(mApp.getResources(), resId, opt);

        return opt;
    }
}
