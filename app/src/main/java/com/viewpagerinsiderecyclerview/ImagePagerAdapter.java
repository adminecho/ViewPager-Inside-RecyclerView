package com.viewpagerinsiderecyclerview;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;




import static com.viewpagerinsiderecyclerview.ImageData.IMAGE_DRAWABLES;


public class ImagePagerAdapter extends FragmentStatePagerAdapter {
    public ImagePagerAdapter(Fragment fragment) {

        super(fragment.getChildFragmentManager());
    }

    @Override
    public int getCount() {
        return IMAGE_DRAWABLES.length;
    }

    @Override
    public Fragment getItem(int position) {
        return ImageFragment.newInstance(IMAGE_DRAWABLES[position]);
    }
}
