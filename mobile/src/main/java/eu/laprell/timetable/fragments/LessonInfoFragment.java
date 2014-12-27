package eu.laprell.timetable.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;

import eu.laprell.timetable.R;
import eu.laprell.timetable.animation.WaveAnimator;
import eu.laprell.timetable.database.Place;
import eu.laprell.timetable.fragments.interfaces.LessonInfoCallback;
import eu.laprell.timetable.fragments.interfaces.LessonViewController;
import eu.laprell.timetable.utils.Dialogs;
import eu.laprell.timetable.utils.MetricsUtils;

/**
 * Created by david on 22.12.14
 */
public class LessonInfoFragment extends Fragment implements LessonInfoCallback {

    private static final int TYPE_SIMPLE_BOX = 1;
    private static final int TYPE_NO_CLICK = 2;

    private LinearLayout mFieldContainer;

    private ArrayList<VHolder> mList;

    private LessonViewController mCon;

    public class VHolder {
        View view;
        View underline;
        ImageView image;
        TextView tv;
        int image_id;

        int type;

        private VHolder(View v) {
            view = v;
            underline = v.findViewById(R.id.view_underline);

            image = (ImageView)v.findViewById(R.id.image);
            tv = (TextView)v.findViewById(R.id.text);
        }
    }

    public LessonInfoFragment() {
        super();

        mList = new ArrayList<>();
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

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_lesson_info, container, false);

        mFieldContainer = (LinearLayout)v.findViewById(R.id.field_container);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void prepareEnterAnimationAlpha(WaveAnimator anim) {
        View v = makeDayView();

        v.setAlpha(0f);
        anim.addTarget(v);

        v = makePriTimeView();
        v.setAlpha(0f);
        anim.addTarget(v);

        v = makePlaceView();
        v.setAlpha(0f);
        anim.addTarget(v);

        v = makeTeacherView();
        v.setAlpha(0f);
        anim.addTarget(v);
    }

    @Override
    public WaveAnimator getGoInEditModeAnimation() {
        WaveAnimator w = new WaveAnimator(new WaveAnimator.WaveAnimationApplier<VHolder>() {
            @Override
            public Animator makeAnimationForView(View v, VHolder data) {
                if(data.type == TYPE_NO_CLICK) {
                    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(data.view, "alpha", 1f, 0.3f);
                    fadeOut.setInterpolator(new DecelerateInterpolator());
                    fadeOut.setDuration(200);

                    data.view.setAlpha(1f);

                    return fadeOut;
                } else {
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(data.underline, "scaleX", 0f, 1f);
                    scaleX.setInterpolator(new DecelerateInterpolator());
                    scaleX.setDuration(200);

                    data.underline.setScaleX(0f);
                    data.underline.setAlpha(1f);

                    return scaleX;
                }
            }
        }).setSpeed(MetricsUtils.convertDpToPixel(100))
                .setStartImmediantlyFromFirstView(true);

        for(int i = 0;i < mList.size();i++) {
            w.addTarget(mList.get(i).view, mList.get(i));
        }

        for (VHolder h : mList) {
            if(h.type == TYPE_NO_CLICK) {
                h.view.setClickable(false);
            } else {
                h.view.setTag(h);
                h.view.setOnClickListener(mListener);

                h.view.setClickable(true);
            }
        }

        return w;
    }

    @Override
    public View getMainView() {
        return mFieldContainer;
    }

    @Override
    public void exitEditMode() {
        for (VHolder h : mList) {
            h.underline.setAlpha(0f);
            h.view.setAlpha(1f);

            h.view.setClickable(false);
        }
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final VHolder holder = ((VHolder) v.getTag());

            switch (holder.image_id) {
                case R.drawable.ic_person_grey600_48dp:
                    Dialogs.showTeachersList(getActivity(),
                            new Dialogs.TeacherSelectedCallback() {
                                @Override
                                public void selectedTeacher(String name) {
                                    mCon.getLesson().setTeacher(name);

                                    holder.tv.setText(name);
                                    mCon.makeLessonDirty();
                                }
                            });
                    break;
                case R.drawable.ic_room_grey600_36dp:
                    Dialogs.showRoomList(getActivity(),
                            new Dialogs.RoomDialogCallback() {
                                @Override
                                public void selectedRoom(Place p, String text) {
                                    mCon.setPlace(p);
                                    holder.tv.setText(text);

                                    mCon.makePlaceDirty();
                                }

                                @Override
                                public void selectedCustomRoom(String text) {
                                    Place p = new Place(-1);
                                    p.setTitle(text);

                                    mCon.setPlace(p);

                                    holder.tv.setText(text);

                                    mCon.makePlaceDirty();
                                }
                            });
                    break;
                case R.drawable.ic_schedule_grey600_36dp:

                    break;
                case R.drawable.ic_event_grey600_48dp:

                    break;
            }
        }
    };

    private View makeTeacherView() {
        String teacher = "";

        if(mCon.getLesson().getTeacher() != null)
            teacher = mCon.getLesson().getTeacher();

        return makeFieldAndAdd(R.drawable.ic_person_grey600_48dp, teacher, TYPE_SIMPLE_BOX);
    }

    private View makePlaceView() {
        String title = "";

        if(mCon.getPlace() != null && mCon.getPlace().getTitle() != null)
            title = mCon.getPlace().getTitle();

        return makeFieldAndAdd(R.drawable.ic_room_grey600_36dp, title, TYPE_SIMPLE_BOX);
    }

    private View makePriTimeView() {
        return makeFieldAndAdd(R.drawable.ic_schedule_grey600_36dp,
                mCon.getTimeUnit().makeTimeString("s - e"), TYPE_NO_CLICK);
    }

    private View makeDayView() {
        String[] days = getResources().getStringArray(R.array.array_days);
        return makeFieldAndAdd(R.drawable.ic_event_grey600_48dp, days[mCon.getDay() - 1], TYPE_NO_CLICK);
    }

    private View makeFieldAndAdd(int imageId, String text, int type) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View v = inflater.inflate(R.layout.list_item_image_with_text,
                mFieldContainer, false);

        VHolder h = new VHolder(v);
        h.type = TYPE_SIMPLE_BOX;
        h.image_id = imageId;
        v.setTag(h);

        h.image.setImageResource(imageId);
        h.tv.setText(text);
        h.type = type;

        mList.add(h);
        mFieldContainer.addView(v);

        return v;
    }
}
