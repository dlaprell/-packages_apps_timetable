package eu.laprell.timetable.fragments;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import eu.laprell.timetable.R;
import eu.laprell.timetable.addon.Addons;
import eu.laprell.timetable.background.MenuNavigation;
import eu.laprell.timetable.utils.MetricsUtils;
import eu.laprell.timetable.widgets.RippleFrameLayout2;
import eu.laprell.timetable.widgets.RippleTouchImageView;

/**
 * Created by david on 06.11.14.
 */
public class DrawerFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private FrameLayout mDrawerTopContainer;
    private ImageView mTopImageView;
    private RippleTouchImageView mExpandView;
    private TextView mTopTitle;

    private DrawerNavigationCallback mCallback;

    private int mSchoolImageId;
    private Bitmap mSchoolImage;
    private String mSchoolName;

    private int mTopImageHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getActivity() instanceof DrawerNavigationCallback) {
            mCallback = (DrawerNavigationCallback)getActivity();
            mCallback.setDrawerNavigationBackend(new DrawerNavigationBackend() {
                @Override
                public void navigateMenu(int menu) {
                    _navigate(mAdapter.mNav.getAbsPosOfMenu(menu));
                }

                @Override
                public void reloadDrawer() {
                    mAdapter.mNav.forceReloading();
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_drawer, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.drawer_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(false);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(new MenuNavigation(v.getContext()) , new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = mRecyclerView.getChildPosition(v);

                if(v instanceof RippleFrameLayout2)
                    ((RippleFrameLayout2)v).cancelAnimation();
                v.invalidate();

                _navigate(itemPosition);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        v.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                v.getViewTreeObserver().removeOnPreDrawListener(this);

                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;

                if(v.getWidth() > width - MetricsUtils.convertDpToPixel(56)) {
                    ViewGroup.LayoutParams p = v.getLayoutParams();
                    p.width = (int)(width - MetricsUtils.convertDpToPixel(56));
                    v.setLayoutParams(p);
                }

                mTopImageHeight = (int) (mRecyclerView.getWidth() * (9f / 16f));

                if(mDrawerTopContainer != null)
                    updateLPDrawerTop();

                return true;
            }
        });

        return v;
    }

    private void updateLPDrawerTop() {
        ViewGroup.LayoutParams p = mDrawerTopContainer.getLayoutParams();
        p.height = mTopImageHeight;
        mDrawerTopContainer.setLayoutParams(p);
    }

    private void loadTopBarInfo() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                SharedPreferences pref = mDrawerTopContainer.getContext().getSharedPreferences("school", 0);

                int schoolId = pref.getInt("school_id", 0);

                if(schoolId != 0) {
                    if(schoolId == Addons.Ids.ID_KREISGYMNASIUM_HEINSBERG) {
                        mSchoolImageId = R.drawable.kgh;
                        mSchoolName = "Kreisgymnasium Heinsberg";
                    }
                } else {
                    mSchoolName = "";
                    mSchoolImageId = R.drawable.room_hightech;
                }

                mSchoolImage = BitmapFactory.decodeResource(getResources(), mSchoolImageId);

                mTopTitle = (TextView)mDrawerTopContainer.findViewById(
                        R.id.top_text_view);
                mTopImageView = (ImageView)mDrawerTopContainer.findViewById(
                        R.id.top_main_background_image);
                mExpandView = (RippleTouchImageView)mDrawerTopContainer.findViewById(
                        R.id.top_image_expand);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mTopTitle.setText(mSchoolName);

                Drawable bg = new BitmapDrawable(mDrawerTopContainer.getResources(), mSchoolImage);
                mTopImageView.setImageDrawable(bg);

                mDrawerTopContainer.setBackgroundColor(0);
            }
        }.execute();
    }

    private void _navigate(int itemPosition) {
        mAdapter.selectPosition(itemPosition);

        int nPos = mAdapter.mNav.getMenuRefAt(itemPosition);

        int menu = mAdapter.mNav.getMenuAtPos(nPos);

        if(mCallback != null)
            mCallback.navigateTo(menu, mAdapter.mNav.getTitleAtPos(nPos));
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        public TextView mCaption;
        public ImageView mImage;
        private FrameLayout mContainer;

        public ViewHolder(View v) {
            super(v);
            mView = v;

            if(v instanceof FrameLayout)
                mContainer = (FrameLayout)v;

            mCaption = (TextView)v.findViewById(R.id.text);
            mImage = (ImageView)v.findViewById(R.id.image);
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<ViewHolder> {
        private static final int TYPE_NORMAL = 1;
        private static final int TYPE_SPACE = 2;
        private static final int TYPE_LINE = 3;
        private static final int TYPE_TOP = 4;

        private MenuNavigation mNav;

        private int mSelected = -1;
        private ColorDrawable mBlueOverlay;

        private View.OnClickListener mListener;

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(MenuNavigation nav, View.OnClickListener lis) {
            mNav = nav;
            mListener = lis;

            mBlueOverlay = new ColorDrawable(0x11000044);
        }

        @Override
        public int getItemViewType(int position) {
            int ref = mNav.getMenuRefAt(position);

            if(ref == MenuNavigation.Menu.MENU_LINE)
                return TYPE_LINE;
            else if(ref == MenuNavigation.Menu.MENU_SPACE)
                return TYPE_SPACE;
            else if(ref == MenuNavigation.Menu.MENU_TOP)
                return TYPE_TOP;
            else
                return TYPE_NORMAL;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolder vh = null;

            if (viewType == TYPE_TOP) {
                mDrawerTopContainer = (FrameLayout)LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragement_drawer_top_item, parent, false);

                if(mTopImageHeight != 0)
                    updateLPDrawerTop();

                loadTopBarInfo();

                vh = new ViewHolder(mDrawerTopContainer);
            } else if(viewType == TYPE_NORMAL) {

                // create a new view
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.drawer_item_menu, parent, false);

                // set the view's size, margins, paddings and layout parameters
                vh = new ViewHolder(v);
                v.setOnClickListener(mListener);

            } else if(viewType == TYPE_LINE) {

                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_line, parent, false);
                vh = new ViewHolder(v);

            } else if(viewType == TYPE_SPACE) {

                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_space, parent, false);
                vh = new ViewHolder(v);

            }
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if(getItemViewType(position) == TYPE_NORMAL) {
                int nPos = mNav.getMenuRefAt(position);

                // - get element from your dataset at this position
                // - replace the contents of the view with that element
                holder.mCaption.setText(mNav.getTitleAtPos(nPos));

                if (mNav.getImageAtPos(nPos) != -1)
                    holder.mImage.setImageResource(mNav.getImageAtPos(nPos));

                if (mSelected == position) {
                    holder.mView.setBackgroundColor(0x55CCCCCC);
                    holder.mCaption.setTypeface(null, Typeface.BOLD);

                    holder.mContainer.setForeground(mBlueOverlay);
                } else {
                    holder.mView.setBackgroundColor(Color.TRANSPARENT);
                    holder.mCaption.setTypeface(null, Typeface.NORMAL);

                    holder.mContainer.setForeground(null);
                }
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mNav.getMenuCountFull();
        }

        public void selectPosition(int item) {
            mAdapter.notifyItemChanged(mSelected);
            mSelected = item;
            mAdapter.notifyItemChanged(item);
        }
    }

    public interface DrawerNavigationCallback {
        public boolean navigateTo(int to, String title);
        public void setDrawerNavigationBackend(DrawerNavigationBackend b);
    }

    public interface DrawerNavigationBackend {
        public void navigateMenu(int menu);
        public void reloadDrawer();
    }
}
