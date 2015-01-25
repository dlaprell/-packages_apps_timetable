package eu.laprell.timetable.fragments.addons;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.laprell.timetable.R;
import eu.laprell.timetable.fragments.BaseFragment;

/**
 * Created by david on 23.01.15
 */
@SuppressWarnings("FieldCanBeLocal")
public class KghRepresentationPlanFragment extends BaseFragment {

    private RecyclerView mRecyclerView;
    private VerDataAdapter mAdapter;
    private String mForDay;
    private final ArrayList<KghMasterRepPlanFragment.Data> mList = new ArrayList<>();
    private boolean mFakeZero = true;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_representation_plan_1, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));

        mAdapter = new VerDataAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    public String getForDay() {
        return mForDay;
    }

    public void setForDay(String mForDay) {
        this.mForDay = mForDay;
    }

    public void setFakeZero(boolean fake) {
        mFakeZero = fake;
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public List<KghMasterRepPlanFragment.Data> getList() {
        return mList;
    }

    public class VHolder extends RecyclerView.ViewHolder {
        private TextView mHeaderText;

        private TextView mNumText;
        private TextView mTypeText;
        private TextView mPlaceText;
        private TextView mOrigTeacherText;
        private TextView mNewTeacherText;
        private TextView mCourseText;

        public VHolder(View itemView) {
            super(itemView);

            mHeaderText = (TextView) itemView.findViewById(R.id.text);

            mPlaceText = (TextView) itemView.findViewById(R.id.place);
            mNumText = (TextView) itemView.findViewById(R.id.num);
            mNewTeacherText = (TextView) itemView.findViewById(R.id.teacher_new);
            mOrigTeacherText = (TextView) itemView.findViewById(R.id.teacher_orig);
            mCourseText = (TextView) itemView.findViewById(R.id.course_number_text);
            mTypeText = (TextView) itemView.findViewById(R.id.text_type);

            if (mOrigTeacherText != null) {
                mOrigTeacherText.setPaintFlags(
                        mOrigTeacherText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }
    }

    public class VerDataAdapter extends RecyclerView.Adapter<VHolder> {

        @Override
        public int getItemViewType(int position) {
            return mList.get(position).newLevel ? 1 : 2;
        }

        @Override
        public VHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layout = viewType == 1
                    ? R.layout.list_item_simple_header : R.layout.list_item_representation_plan;
            View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return new VHolder(v);
        }

        @Override
        public void onBindViewHolder(VHolder holder, int position) {
            KghMasterRepPlanFragment.Data d = mList.get(position);

            if (getItemViewType(position) == 1) {
                holder.mHeaderText.setText(d.level_name);
            } else {
                holder.mNumText.setText(d.lesson);
                holder.mTypeText.setText(d.type);
                holder.mPlaceText.setText(d.place);
                holder.mOrigTeacherText.setText(d.orig_teacher);
                holder.mNewTeacherText.setText(d.new_teacher);
                holder.mCourseText.setText(d.course_number);
            }
        }

        @Override
        public int getItemCount() {
            return mFakeZero ? 0 : mList.size();
        }
    }
}
