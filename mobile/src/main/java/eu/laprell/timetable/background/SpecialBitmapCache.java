package eu.laprell.timetable.background;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.widget.Toast;

import eu.laprell.timetable.BuildConfig;

/**
 * Created by david on 18.12.14.
 */
public class SpecialBitmapCache {

    private static final String TAG = "SpecialBitmapCache";

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

        int factor;
        if(maxMemory >= 256 * 1024) {
            factor = 3;
        } else if(maxMemory >= 172 * 1024) {
            factor = 4;
        } else if(maxMemory >= 128 * 1024) {
            factor = 5;
        } else if(maxMemory >= 96 * 1024) {
            factor = 6;
        } else {
            factor = 8;
        }

        // Use 1/'factor'th of the available memory for this memory cache.
        final int cacheSize = maxMemory / factor;

        mBitmaps = new CustomLruCache(cacheSize);

        if(BuildConfig.DEBUG) {
            Toast.makeText(mApp, "Cache size is: " + cacheSize, Toast.LENGTH_SHORT).show();
        }
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

    public boolean isInCache(int resId) {
        return mBitmaps.get(resId) != null;
    }

    public Bitmap loadBitmap(int resId, int minSizeW, int minSizeH) {
        Bitmap b = mBitmaps.get(resId);

        if(b == null
                || ((b.getWidth() * 1.2f) < minSizeW)
                || ((b.getHeight() * 1.2f) < minSizeH)) {

            BitmapFactory.Options opt = loadBitmapOptions(resId);

            int scale = getMinScaleFactor(opt.outWidth, opt.outHeight, minSizeW, minSizeH);
            opt.inJustDecodeBounds = false;
            opt.inSampleSize = scale;

            b = BitmapFactory.decodeResource(mApp.getResources(), resId, opt);

            mBitmaps.put(resId, b);

            //Logger.log(TAG, "Created a new Bitmap in cache: " + resId + " cache is now: " + mBitmaps.size() + "kB");
        }

        return b;
    }

    private int getMinScaleFactor(int bW, int bH, int tW, int tH) {
        if(tW <= 0 || tH <= 0)
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

    public void onTrimMemory(int t) {
        if(t == ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            mBitmaps.evictAll();
        } else if (t == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            mBitmaps.trimToSize(mBitmaps.maxSize() / 4);
        }
    }
}
