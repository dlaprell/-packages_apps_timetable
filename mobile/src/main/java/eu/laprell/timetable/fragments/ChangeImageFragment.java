package eu.laprell.timetable.fragments;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import eu.laprell.timetable.R;
import eu.laprell.timetable.database.DbAccess;

/**
 * Created by david on 18.12.14.
 */
public class ChangeImageFragment extends Fragment {

    private int[] mImages;
    private int mColor;
    private DbAccess mAccess;
    private ViewPager mPager;
    private TextView mText;
    private String mBeginning;
    private Button mButton;
    private int mCurrentSelectedItem;

    private OnImageChangeResult mResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccess = new DbAccess(getActivity());
        mImages = mAccess.get().getImageList();

        mBeginning = getString(R.string.image_hash);

        if(savedInstanceState != null) {
            mCurrentSelectedItem = savedInstanceState.getInt("selected_image", 0);
            mColor = savedInstanceState.getInt("color", 0);
        } else if(getArguments() != null) {
            mCurrentSelectedItem = getArguments().getInt("selected_image", -1);

            if(mCurrentSelectedItem == -1) {
                if(getArguments().getInt("res_id", 0) != 0) {
                    int resid = getArguments().getInt("res_id");

                    for (int i = 0;i < mImages.length;i++) {
                        if(mImages[i] == resid) {
                            mCurrentSelectedItem = i;
                            break;
                        }
                    }
                } else {
                    mCurrentSelectedItem = 0;
                }
            }

            mColor = getArguments().getInt("color", 0);
        }
    }

    public void setOnResultCallback(OnImageChangeResult res) {
        mResult = res;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_image_changing, container, false);

        mText = (TextView)v.findViewById(R.id.text_counter);
        mButton = (Button)v.findViewById(R.id.select_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mResult != null) {
                    mResult.selectedImage(mImages[mPager.getCurrentItem()]);
                }
            }
        });

        mPager = (ViewPager)v.findViewById(R.id.pager);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mText.setText(mBeginning + String.valueOf(position + 1));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Fragment f = new ImageFragment();
                Bundle args = new Bundle();

                args.putInt("image_id", mImages[position]);
                args.putInt("image_color", mColor);

                f.setArguments(args);
                return f;
            }

            @Override
            public int getCount() {
                return mImages.length;
            }

            @Override
            public float getPageWidth(int position) {
                return 0.9f;
            }
        });

        mPager.setCurrentItem(mCurrentSelectedItem);
        mText.setText(mBeginning + String.valueOf(mCurrentSelectedItem + 1));

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mAccess.close();
    }

    public interface OnImageChangeResult {
        public void selectedImage(int resId);
    }

    public static class ImageFragment extends Fragment {
        private int mImageId = -1;
        private int mColor = -1;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if(getArguments() != null) {
                mImageId = getArguments().getInt("image_id", -1);
                mColor = getArguments().getInt("image_color", -1);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            final View v = inflater.inflate(R.layout.fragment_image_simple, container, false);

            ImageView image = (ImageView)v.findViewById(R.id.image);
            if(mImageId != -1) {
                image.setImageResource(mImageId);

                if(mColor != -1) {
                    PorterDuff.Mode mMode = PorterDuff.Mode.MULTIPLY;
                    image.setColorFilter(mColor, mMode);
                }
            }

            return v;
        }
    }
}
