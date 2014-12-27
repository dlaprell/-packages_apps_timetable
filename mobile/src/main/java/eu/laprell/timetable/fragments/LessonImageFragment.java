package eu.laprell.timetable.fragments;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import eu.laprell.timetable.R;
import eu.laprell.timetable.background.SpecialBitmapCache;
import eu.laprell.timetable.database.DbAccess;
import eu.laprell.timetable.fragments.interfaces.LessonViewController;
import eu.laprell.timetable.utils.AnimUtils;

/**
 * Created by david on 22.12.14
 */
public class LessonImageFragment extends Fragment {

    private ImageView mMainImage;

    private LessonViewController mCon;

    private boolean mIsBackground;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_lesson_image, container, false);

        mMainImage = (ImageView)v.findViewById(R.id.lesson_image);

        AnimUtils.afterPreDraw(mMainImage, new Runnable() {
            @Override
            public void run() {
                mIsBackground = mMainImage.getParent().getParent() instanceof FrameLayout;
            }
        });

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        if(activity instanceof LessonViewController) {
            mCon = (LessonViewController)activity;
        } else {
            throw new ClassCastException("Attached activity is not a LessonViewController");
        }
        super.onAttach(activity);
    }

    public int makeImageViewHeight() {
        if(isBackground())
            return -1;

        float h = mMainImage.getWidth() * (9f / 16f); // All images are in 16:9 format!
        int height = (int)h;

        ViewGroup.LayoutParams p = mMainImage.getLayoutParams();
        p.height = height;
        mMainImage.setLayoutParams(p);

        return height;
    }

    public ImageView getImageView() {
        return mMainImage;
    }

    public void loadImageFromDb() {
        mMainImage.setImageDrawable(new ColorDrawable(mCon.getLesson().getColor()));
        new AsyncTask<Void, Void, TransitionDrawable>() {
            @Override
            protected TransitionDrawable doInBackground(Void... params) {
                DbAccess access = new DbAccess(getActivity());

                int resId = access.get().getImageIdForLesson(mCon.getLesson());

                if(resId != -1) {
                    Drawable image = new BitmapDrawable(getResources(),
                            SpecialBitmapCache.getInstance().loadBitmap(resId,
                                    mMainImage.getWidth(), mMainImage.getHeight()));

                    Drawable[] drawables = new Drawable[] {
                            new ColorDrawable(mCon.getLesson().getColor()),
                            image
                    };

                    return new TransitionDrawable(drawables);
                } else return null;
            }

            @Override
            protected void onPostExecute(TransitionDrawable transitionDrawable) {
                super.onPostExecute(transitionDrawable);

                if(transitionDrawable != null) {
                    mMainImage.setImageDrawable(transitionDrawable);
                    transitionDrawable.startTransition(500);
                }

                updateColorFilter();
            }
        }.execute();
    }

    public void updateColorFilter() {
        mMainImage.setColorFilter(mCon.getLesson().getColor(), PorterDuff.Mode.MULTIPLY);
    }

    public void checkImageTransit() {
        if(mMainImage.getDrawable() instanceof TransitionDrawable) {
            TransitionDrawable tra = (TransitionDrawable)mMainImage.getDrawable();

            mMainImage.setImageDrawable(tra.getDrawable(tra.getNumberOfLayers() - 1));
        }
    }

    public boolean isBackground() {
        return mIsBackground;
    }
}
