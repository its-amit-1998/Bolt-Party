package com.example.boltparty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class TabSetUpAdapter extends FragmentPagerAdapter {

    private List<Fragment> tab_Fragment;
    private String[] tab_titles;

    public TabSetUpAdapter(FragmentManager fragmentManager, List<Fragment> tab_Fragment, String[] tab_titles) {
        super(fragmentManager);
        this.tab_Fragment = tab_Fragment;
        this.tab_titles = tab_titles;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return tab_Fragment.get(position);
    }

    @Override
    public int getCount() {
        return tab_Fragment.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (tab_titles.length == 0) {
            return null;
        }

        return tab_titles[position];
    }
}

