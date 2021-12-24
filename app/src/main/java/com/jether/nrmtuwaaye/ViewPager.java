package com.jether.nrmtuwaaye;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.jether.nrmtuwaaye.Fragments.FragmentA;
import com.jether.nrmtuwaaye.Fragments.FragmentB;
import com.jether.nrmtuwaaye.Fragments.FragmentC;

public class ViewPager extends FragmentPagerAdapter {
    public ViewPager(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:return new FragmentA();
            case 1:return new FragmentB();
            case 2:return new FragmentC();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
