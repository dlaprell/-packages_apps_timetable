package eu.laprell.timetable;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewPropertyAnimator;

import eu.laprell.timetable.animation.ActivityTransitions;
import eu.laprell.timetable.animation.CircleColorDrawable;
import eu.laprell.timetable.animation.WaveAnimator;
import eu.laprell.timetable.background.SpecialBitmapCache;
import eu.laprell.timetable.database.Day;
import eu.laprell.timetable.database.DbAccess;
import eu.laprell.timetable.database.ImageDb;
import eu.laprell.timetable.database.Lesson;
import eu.laprell.timetable.database.Place;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.database.TimetableDatabase;
import eu.laprell.timetable.fragments.ChangeImageFragment;
import eu.laprell.timetable.fragments.LessonImageFragment;
import eu.laprell.timetable.fragments.interfaces.LessonInfoCallback;
import eu.laprell.timetable.fragments.interfaces.LessonViewController;
import eu.laprell.timetable.utils.AnimUtils;
import eu.laprell.timetable.utils.ColorUtils;
import eu.laprell.timetable.utils.Const;
import eu.laprell.timetable.utils.Dialogs;
import eu.laprell.timetable.utils.MetricsUtils;
import eu.laprell.timetable.widgets.FloatingActionButton;


public class LessonViewActivity extends ActionBarActivity implements LessonViewController {

    private static final int ANIM_TIME = 70;

    //private ImageView mMainImage;
    private FrameLayout mContainer, mFragmentOptionsContainer;
    private Toolbar mToolbar;
    private TextView mTitle;
    private FloatingActionButton mFab;

    private Lesson mLesson;
    private TimeUnit mTimeUnit;
    private Place mPlace;
    private int mDayOfWeek;

    private boolean mPlaceDirty;
    private boolean mLessonDirty;

    private boolean mInEditMode;

    private boolean mReloading;

    private CircleColorDrawable mAnimatingCircle;

    private DbAccess mAccess;

    private TransitionDrawable mFabDrawable;
    private int mFabColorEdit;
    private int mFabColorDone;

    private Menu mMenu;

    private LessonInfoCallback mLessonInfoCb;
    private LessonImageFragment mImageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLesson = getIntent().getParcelableExtra("lesson");
        mTimeUnit = getIntent().getParcelableExtra("timeunit");
        mPlace = getIntent().getParcelableExtra("place");
        mDayOfWeek = getIntent().getIntExtra("day_of_week", 0);

        setContentView(R.layout.activity_lesson_view);

        mLessonInfoCb = (LessonInfoCallback)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_lesson_info);
        mImageFragment = (LessonImageFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_lesson_image);

        mAccess = new DbAccess(this);
        mAnimatingCircle = new CircleColorDrawable();

        mContainer = (FrameLayout)findViewById(R.id.lesson_container);
        mFragmentOptionsContainer = (FrameLayout)findViewById(R.id.fragment_options_container);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mFab = (FloatingActionButton)findViewById(R.id.fab_edit);
        mTitle = (TextView)findViewById(R.id.title);

        mAnimatingCircle.setAlphaF(0f);
        mAnimatingCircle.setColor(getResources().getColor(R.color.material_green));
        mContainer.setForeground(mAnimatingCircle);

        mFab.setAlpha(0f);
        mTitle.setAlpha(0f);
        mToolbar.setAlpha(0f);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_48dp);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTitle.setText(mLesson.getTitle());

        mReloading = savedInstanceState != null && savedInstanceState.getBoolean("reloading", false);
        AnimUtils.afterPreDraw(mFab, mEnterAnimation);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mInEditMode) {
                    exitEditMode();

                    save();

                    setResult(Activity.RESULT_OK);
                } else {
                    goInEditMode();
                }
                mInEditMode = !mInEditMode;
            }
        });

        mFabDrawable = new TransitionDrawable(new Drawable[] {
                getResources().getDrawable(R.drawable.ic_create_white_24dp),
                getResources().getDrawable(R.drawable.ic_done_white_24dp)
        });
        mFab.setImageDrawable(mFabDrawable);

        mFabColorEdit = getResources().getColor(R.color.material_teal);
        mFabColorDone = getResources().getColor(R.color.material_green);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mAccess.close();
    }

    private Runnable mEnterAnimation = new Runnable() {
        @Override
        public void run() {
            findViewById(R.id.image_view_container).setMinimumHeight(mContainer.getHeight());

            /*
              At this point we have to think about some stages:

               * we are recreating an instance of the activity (-> coming from recents)
                 -> all variants (phone + tablet) no animation, but show the rest
                 -> on phones: calculate the new height: 16:9 pictures
               * we are coming for the first time
                 -> on tablets: no animation, but show the rest
                 -> on phones: calculate the new height: 16:9 pictures + animation

             */
            boolean fwHeroTrans = Const.FW_SUPPORTS_HERO_TRANSITION;

            if(mReloading || fwHeroTrans) {
                if(BuildConfig.DEBUG)
                    Toast.makeText(LessonViewActivity.this, "Reloading ...", Toast.LENGTH_SHORT).show();

                int height;
                if (!mImageFragment.isBackground()) {
                    height = mImageFragment.makeImageViewHeight();

                    RelativeLayout.LayoutParams pa = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
                    pa.topMargin = height - (mFab.getHeight() / 2);
                    mFab.setLayoutParams(pa);
                } else height = 0;

                mShowOtherViewsRunnable.run();

                if(!fwHeroTrans)
                    mImageFragment.loadImageFromDb();
                else {
                    ImageView iv = mImageFragment.getImageView();
                    Drawable b = ActivityTransitions.getHeroTransitionDrawable(getIntent().getExtras(),
                            getResources(), iv.getWidth(), height);
                    iv.setImageDrawable(b);
                }
            } else {
                boolean hasCustomTrans = false;

                if (!mImageFragment.isBackground()) {
                    int height = mImageFragment.makeImageViewHeight();

                    RelativeLayout.LayoutParams pa = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
                    pa.topMargin = height - (mFab.getHeight() / 2);
                    mFab.setLayoutParams(pa);

                    hasCustomTrans = ActivityTransitions.animateHeroTransition(
                            getIntent().getExtras(), mImageFragment.getImageView(),
                            new ActivityTransitions.HeroTransitionInterface() {
                                @Override
                                public void setBackgroundColorAlpha(int alpha) {
                                    mContainer.setBackgroundColor(Color.argb(alpha, 0xFF, 0xFF, 0xFF));
                                }
                            }, ANIM_TIME, mShowOtherViewsRunnable, mImageFragment.getImageView().getWidth(), height);
                } else {
                    mShowOtherViewsRunnable.run();
                }

                if (!hasCustomTrans) {
                    mImageFragment.loadImageFromDb();
                }
            }
        }
    };

    private void updateColor(boolean withStatusBar) {
        mImageFragment.updateColorFilter();

        if(withStatusBar) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ColorUtils.convertMaterial500To700(mLesson.getColor()));
            }
        }
    }

    private void exitEditMode() {
        int[] cLoc = new int[2];
        mContainer.getLocationOnScreen(cLoc);

        int[] fLoc = new int[2];
        mLessonInfoCb.getMainView().getLocationOnScreen(fLoc);

        int[] mcLoc= new int[2];
        int top;
        if(mImageFragment.isBackground()) {
            findViewById(R.id.image_view_container).getLocationOnScreen(mcLoc);
            top = mcLoc[1] - cLoc[1];
        } else {
            mFab.getLocationOnScreen(mcLoc);
            top = (mcLoc[1] + (mFab.getHeight() / 2)) - cLoc[1];
        }

        RectF drawRegion = new RectF();
        drawRegion.left = fLoc[0];
        drawRegion.right = fLoc[0] + mLessonInfoCb.getMainView().getWidth();
        drawRegion.bottom = mContainer.getHeight();
        drawRegion.top = Math.max(0f, top);
        mAnimatingCircle.setDrawingRect(drawRegion);

        float finalRadius = Math.max(drawRegion.height(), drawRegion.width()) * 1.7f;

        mFab.getLocationOnScreen(fLoc);
        mAnimatingCircle.setCircleX(fLoc[0] - cLoc[0] + mFab.getWidth() / 2);
        float yStart = fLoc[1] - cLoc[1] + mFab.getHeight() / 2;
        mAnimatingCircle.setCircleY(yStart);

        float yEnd = yStart + (drawRegion.height() / 2);

        mAnimatingCircle.setAlphaF(1f);

        ObjectAnimator radius = ObjectAnimator.ofFloat(mAnimatingCircle, "radius", 0f, finalRadius);
        radius.setDuration(250);
        radius.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator moveDown = ObjectAnimator.ofFloat(mAnimatingCircle, "circleY", yStart, yEnd);
        moveDown.setDuration(250);
        moveDown.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(mAnimatingCircle, "alphaF", 1f, 0f);
        fadeOut.setDuration(400);
        fadeOut.setInterpolator(new AccelerateInterpolator());

        radius.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                mLessonInfoCb.exitEditMode();
            }
        });

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                animateFab(true);
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.play(radius).with(moveDown).before(fadeOut);
        set.start();
    }

    private void animateFab(boolean reverse) {
        int cFrom = reverse ? mFabColorDone : mFabColorEdit;
        int cTo = reverse ? mFabColorEdit : mFabColorDone;

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), cFrom, cTo);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mFab.setBackgroundColor((Integer)animator.getAnimatedValue());
            }
        });
        colorAnimation.setDuration(200);
        colorAnimation.start();

        mFabDrawable.setCrossFadeEnabled(true);
        mFabDrawable.resetTransition();

        if(reverse) mFabDrawable.reverseTransition(200);
        else mFabDrawable.startTransition(200);
    }

    private Runnable mShowOtherViewsRunnable = new Runnable() {
        @Override
        public void run() {
            mContainer.setBackgroundColor(0xFFFFFFFF);

            final float distance = MetricsUtils.convertDpToPixel(120);
            final Interpolator interpolator = new DecelerateInterpolator(2.0f);
            WaveAnimator anim = new WaveAnimator(new WaveAnimator.WaveAnimationApplier() {
                @Override
                public Animator makeAnimationForView(View v, Object data) {
                    ObjectAnimator fade = ObjectAnimator.ofFloat(v, "alpha", 1f);

                    if(data == null) { // All "info boxes" have an object
                        fade.setDuration(500);
                        AnimUtils.withLayer(v, fade);
                        return fade;
                    }

                    ObjectAnimator moveUp = ObjectAnimator.ofFloat(v, "translationY", distance, 0);
                    moveUp.setInterpolator(interpolator);

                    moveUp.setDuration(400);
                    fade.setDuration(300);

                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(fade, moveUp);
                    AnimUtils.withLayer(v, set);

                    return set;
                }
            });
            anim.setStartAnchorView(mImageFragment.getImageView(), true);
            anim.addTargetsWithoutData(mTitle, mToolbar, mFab);
            anim.setComputeMoreExactly(true);

            mLessonInfoCb.prepareEnterAnimationAlpha(anim);

            anim.setSpeed(MetricsUtils.convertDpToPixel(100));
            anim.setStartImmediantlyFromFirstView(true);

            anim.startOnPreDraw(mFab);

            mImageFragment.checkImageTransit();

            updateColor(true);
        }
    };

    private void goInEditMode() {
        WaveAnimator w = mLessonInfoCb.getGoInEditModeAnimation();
        w.setStartAnchorView(mFab, true);
        w.start();
        animateFab(false);
    }

    private void save() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                TimetableDatabase db = mAccess.get();

                if(mPlaceDirty) {
                    if(mPlace.getId() == -1)
                        mPlace = (Place)db.insertDatabaseEntryForId(mPlace);
                    else
                        db.updateDatabaseEntry(mPlace);

                    Day d = db.getDayForDayOfWeek(mDayOfWeek);

                    long[] pids = d.getPlaces();
                    for (int i = 0;i < pids.length;i++) {
                        if(mTimeUnit.getId() == d.getTimeUnits()[i]) {
                            pids[i] = mPlace.getId();

                            break;
                        }
                    }
                    d.setPlaces(pids);

                    db.updateDatabaseEntry(d);

                    mPlaceDirty = false;
                }

                if(mLessonDirty) {
                    db.updateDatabaseEntry(mLesson);

                    mLessonDirty = false;
                }

                setResult(Activity.RESULT_OK);

                return null;
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lesson_view_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_change_image) {
            changeImage();

            item.setEnabled(false);
            mMenu.findItem(R.id.action_change_color).setEnabled(false);
            return true;
        } else if(id == R.id.action_change_color) {
            changeColor();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("reloading", true);
    }

    private void changeColor() {
        Dialogs.showColorList(this, new Dialogs.ColorSelectedCallback() {
            @Override
            public void selectedColor(int color) {
                mLesson.setColor(color);

                updateColor(true);

                saveInDb();
            }
        });
    }

    private void saveLessonImage(int resid) {
        int imageId = ImageDb.getIdByImage(resid);

        if(mLesson.getImageId() != imageId) {
            Bitmap b = SpecialBitmapCache.getInstance().loadBitmap(resid,
                    mImageFragment.getImageView().getWidth(), mImageFragment.getImageView().getHeight());
            Drawable newImage = new BitmapDrawable(getResources(), b);

            //newImage.setColorFilter(mLesson.getColor(), PorterDuff.Mode.MULTIPLY);

            Drawable start = mImageFragment.getImageView().getDrawable();

            if(start instanceof TransitionDrawable) {
                TransitionDrawable t = (TransitionDrawable)start;
                start = t.getDrawable(t.getNumberOfLayers() - 1);
            }

            TransitionDrawable draw = new TransitionDrawable(new Drawable[] {
                    start,
                    newImage
            });

            mImageFragment.getImageView().setImageDrawable(draw);
            //draw.setCrossFadeEnabled(true);
            draw.startTransition(500);

            mLesson.setImageId(imageId);

            saveInDb();
        }
    }

    private void saveInDb() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DbAccess access = new DbAccess(LessonViewActivity.this);
                access.get().updateDatabaseEntry(mLesson);
                access.close();
                return null;
            }
        }.execute();

        setResult(Activity.RESULT_OK);
    }

    private Fragment getOptionsFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_options_container);
    }

    private void changeImage() {
        if(getOptionsFragment() == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            final ChangeImageFragment f = new ChangeImageFragment();
            f.setOnResultCallback(new ChangeImageFragment.OnImageChangeResult() {
                @Override
                public void selectedImage(int resId) {
                    animateImageChanger(true, null);

                    if(mMenu != null) {
                        mMenu.findItem(R.id.action_change_image).setEnabled(true);
                        mMenu.findItem(R.id.action_change_color).setEnabled(true);
                    }

                    saveLessonImage(resId);
                }
            });
            Bundle args = new Bundle();

            args.putInt("res_id", mAccess.get().getImageIdForLesson(mLesson));
            args.putInt("color", mLesson.getColor());
            f.setArguments(args);

            transaction.replace(R.id.fragment_options_container, f);
            transaction.commit();
        }

        animateImageChanger(false, null);
    }

    private void animateImageChanger(final boolean rev, final Runnable endAction) {
        ViewPropertyAnimator.animate(mFab).alpha(rev ? 1f : 0f).setDuration(100);

        if(rev) {
            mLessonInfoCb.getMainView().setVisibility(View.VISIBLE);
        } else {
            mFragmentOptionsContainer.setAlpha(0f);
            mFragmentOptionsContainer.setVisibility(View.VISIBLE);
        }

        AnimUtils.afterPreDraw(mFragmentOptionsContainer, new Runnable() {
            @Override
            public void run() {
                final int height = mFragmentOptionsContainer.getHeight();

                final float alpha1 = rev ? 1f : 0f;
                final float alpha2 = rev ? 0f : 1f;

                final float transYFromCon = !rev ? 0 : (height / 4);
                final float transYToCon = !rev ? (height / 4) : 0;

                final float transYFromFra = !rev ? -(height / 4) : 0;
                final float transYToFra = !rev ? 0 : -(height / 4);

                ObjectAnimator moveDown = ObjectAnimator.ofFloat(mLessonInfoCb.getMainView(), "translationY", transYFromCon, transYToCon)
                        .setDuration(rev ? 500 : 200);
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(mLessonInfoCb.getMainView(), "alpha", alpha2, alpha1)
                        .setDuration(100);

                ObjectAnimator moveDown2 = ObjectAnimator.ofFloat(mFragmentOptionsContainer, "translationY", transYFromFra, transYToFra)
                        .setDuration(rev ? 200 : 500);
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mFragmentOptionsContainer, "alpha", alpha1, alpha2)
                        .setDuration(rev ? 200 : 100);

                moveDown.setInterpolator(rev ? new OvershootInterpolator() : new AccelerateInterpolator());
                moveDown2.setInterpolator(rev ? new AccelerateInterpolator() : new OvershootInterpolator());

                moveDown.addListener(new AnimUtils.LayerAdapter(mLessonInfoCb.getMainView()));
                moveDown2.addListener(new AnimUtils.LayerAdapter(mFragmentOptionsContainer));

                AnimatorSet set = new AnimatorSet();
                if (rev) {
                    set.play(fadeIn).with(moveDown2);
                    set.play(moveDown2).before(fadeOut);
                    set.play(fadeOut).with(moveDown);
                } else {
                    set.play(fadeOut).with(moveDown);
                    set.play(moveDown).before(fadeIn);
                    set.play(fadeIn).with(moveDown2);
                }

                if (endAction != null) {
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            endAction.run();
                        }
                    });
                }


                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (rev) {
                            mFragmentOptionsContainer.setVisibility(View.GONE);
                        } else {
                            mLessonInfoCb.getMainView().setVisibility(View.GONE);
                        }
                    }
                });

                set.start();
            }
        });
    }

    @Override
    public Lesson getLesson() {
        return mLesson;
    }

    @Override
    public void makeLessonDirty() {
        mLessonDirty = true;
    }

    @Override
    public int getDay() {
        return mDayOfWeek;
    }

    @Override
    public Place getPlace() {
        return mPlace;
    }

    @Override
    public void setPlace(Place p) {
        mPlace = p;
    }

    @Override
    public void makePlaceDirty() {
        mPlaceDirty = true;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return mTimeUnit;
    }
}
