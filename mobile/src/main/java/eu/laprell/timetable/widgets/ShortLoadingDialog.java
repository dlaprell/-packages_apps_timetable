package eu.laprell.timetable.widgets;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import eu.laprell.timetable.R;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * Created by david on 13.11.14.
 */
public class ShortLoadingDialog {

    private Context mContext;
    private AlertDialog mDialog;

    public ShortLoadingDialog(Context c) {
        mContext = c;
    }

    public void show() {
        if(mDialog != null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        // Get the layout inflater
        LayoutInflater inflater = LayoutInflater.from(mContext);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_short_loading, null));
        AlertDialog a = builder.create();

        mDialog = a;
        a.setCancelable(false);
        a.show();
    }

    public void finish(final Runnable r) {
        ImageView done = (ImageView) mDialog.findViewById(R.id.done);
        CircularProgressBar prog = (CircularProgressBar) mDialog.findViewById(R.id.circular_loading);

        done.setAlpha(0.3f);
        done.setRotationX(90);
        done.setRotationY(90);

        done.setPivotX(done.getWidth());
        done.setPivotY(done.getHeight());
        done.setVisibility(View.VISIBLE);

        ViewPropertyAnimator a = done.animate().alpha(1).rotationX(0).rotationY(0)
                .setDuration(500).setInterpolator(new DecelerateInterpolator());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            a.withEndAction(new Runnable() {
                @Override
                public void run() {
                    continueAnimation(r);
                }
            });
        } else {
            a.setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    continueAnimation(r);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
    }

    private void continueAnimation(final Runnable r) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();
                mDialog = null;

                if(r != null)
                    r.run();
            }
        }, 1000);
    }
}
