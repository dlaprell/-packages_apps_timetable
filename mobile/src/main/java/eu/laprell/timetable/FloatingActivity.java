package eu.laprell.timetable;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import eu.laprell.timetable.animation.ActivityTransitions;
import eu.laprell.timetable.utils.AnimUtils;
import eu.laprell.timetable.widgets.FloatingActionButton;


public class FloatingActivity extends ActionBarActivity {

    private static final int ANIM_TIME = 150;

    private FloatingActionButton mFabClear;
    private ViewGroup mFabMicContainer, mFabNewTaskContainer;
    private TextView mFabMicDes, mFabNewTaskDes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_floating);

        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int id = v.getId();
                if(id == R.id.fab_mic) {
                    int[] loc = new int[2];
                    findViewById(R.id.fab_mic).getLocationOnScreen(loc);

                    Intent i = new Intent(FloatingActivity.this, AddActivity.class);

                    ActivityTransitions.makeCircularRevealFromView(i,
                            findViewById(R.id.fab_mic));

                    FloatingActivity.this.startActivity(i);
                    FloatingActivity.this.overridePendingTransition(0, 0);

                    mFabMicDes.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1000);
                } else if(id == R.id.fab_clear) {
                    animateOut();
                } else if(id == R.id.fab_add_task) {
                    int[] loc = new int[2];
                    findViewById(R.id.fab_add_task).getLocationOnScreen(loc);

                    Intent i = new Intent(FloatingActivity.this, NewTaskActivity.class);

                    ActivityTransitions.makeCircularRevealFromView(i,
                            findViewById(R.id.fab_add_task));

                    FloatingActivity.this.startActivity(i);
                    FloatingActivity.this.overridePendingTransition(0, 0);

                    mFabMicDes.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1000);
                }
            }
        };

        mFabClear = (FloatingActionButton)findViewById(R.id.fab_clear);

        mFabMicContainer = (ViewGroup)findViewById(R.id.fab_mic_container);
        mFabNewTaskContainer = (ViewGroup)findViewById(R.id.fab_add_task_container);

        mFabMicDes = (TextView)findViewById(R.id.fab_mic_des);
        mFabNewTaskDes = (TextView)findViewById(R.id.fab_add_task_des);
        mFabMicDes.setAlpha(0);
        mFabNewTaskDes.setAlpha(0);

        findViewById(R.id.fab_mic).setOnClickListener(click);
        findViewById(R.id.fab_add_task).setOnClickListener(click);
        mFabClear.setOnClickListener(click);

        mFabClear.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mFabClear.getViewTreeObserver().removeOnPreDrawListener(this);

                AnimatorSet set = new AnimatorSet();
                set.setDuration(ANIM_TIME);
                set.setInterpolator(new DecelerateInterpolator());

                final float distanceMic = mFabMicContainer.getWidth() / 2;
                final float distanceTask = mFabNewTaskContainer.getWidth() / 2;

                mFabMicContainer.setTranslationX(distanceMic);
                mFabNewTaskContainer.setTranslationX(distanceTask);

                set.playSequentially(
                        ObjectAnimator.ofFloat(mFabMicContainer, "translationX", distanceMic, 0),
                        ObjectAnimator.ofFloat(mFabNewTaskContainer, "translationX", distanceTask, 0)
                );

                mFabMicDes.animate().alpha(1).setDuration(ANIM_TIME * 2);
                mFabNewTaskDes.animate().alpha(1).setDuration(ANIM_TIME * 2);

                set.start();

                return true;
            }
        });
    }

    private void animateOut() {
        AnimatorSet set = new AnimatorSet();
        set.setDuration(ANIM_TIME);
        set.setInterpolator(new AccelerateInterpolator());

        final float distanceMic = mFabMicContainer.getWidth() / 2;
        final float distanceTask = mFabNewTaskContainer.getWidth() / 2;

        mFabMicDes.animate().alpha(0).setDuration(ANIM_TIME);
        mFabNewTaskDes.animate().alpha(0).setDuration(ANIM_TIME);

        mFabMicContainer.setTranslationX(0);
        mFabNewTaskContainer.setTranslationX(0);

        set.playSequentially(
                ObjectAnimator.ofFloat(mFabNewTaskContainer, "translationX", 0, distanceTask),
                ObjectAnimator.ofFloat(mFabMicContainer, "translationX", 0, distanceMic)
        );

        AnimUtils.withEndAction(set, new Runnable() {
            @Override
            public void run() {
                finish();

                FloatingActivity.this.overridePendingTransition(0, R.anim.fade_out);
            }
        });

        set.start();
    }

    @Override
    public void onBackPressed() {
        animateOut();
    }
}
