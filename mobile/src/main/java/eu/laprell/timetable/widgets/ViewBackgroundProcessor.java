package eu.laprell.timetable.widgets;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import eu.laprell.timetable.R;

/**
 * Created by david on 13.11.14.
 */
public class ViewBackgroundProcessor extends BaseAdapter {

    private int[] mColors;
    private Context mContext;

    public ViewBackgroundProcessor(Context context, int[] colors) {
        mColors = colors;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mColors.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return mColors[position];
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null)
            view = LayoutInflater.from(mContext).inflate(R.layout.single_view_list_item, parent, false);

        ((FrameLayout) view).setForeground(new ColorDrawable(mColors[position]));

        return view;
    }
}