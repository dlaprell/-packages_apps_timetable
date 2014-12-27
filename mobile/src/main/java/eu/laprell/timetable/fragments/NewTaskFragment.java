package eu.laprell.timetable.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import eu.laprell.timetable.R;

/**
 * Created by david on 24.12.14
 */
public class NewTaskFragment extends Fragment {

    private FrameLayout mImageSelect;
    private TextView mLessonText;
    private ImageView mLessonImage;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_task_new, container, false);

        mImageSelect = (FrameLayout) v.findViewById(R.id.lesson_select);
        mLessonText = (TextView)mImageSelect.findViewById(R.id.text);
        mLessonImage = (ImageView)mImageSelect.findViewById(R.id.image);

        return v;
    }
}
