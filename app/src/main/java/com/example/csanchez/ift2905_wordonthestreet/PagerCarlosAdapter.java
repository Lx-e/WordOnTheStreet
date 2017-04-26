package com.example.csanchez.ift2905_wordonthestreet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class PagerCarlosAdapter extends FragmentStatePagerAdapter {
    String[] categories =  {"business", "entertainment", "gaming", "general", "music", "politics", "science-and-nature", "sport", "technology"};
    Map<String, ArrayList<String>> srcByCat;

    public PagerCarlosAdapter(FragmentManager fm, Map<String, ArrayList<String>> sourcesByCat) {
        super(fm);
        this.srcByCat = sourcesByCat;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new PagerCarlosFragment();
        Bundle args = new Bundle();
        //On va chercher la catégorie concernée ainsi que les sources y appartenant
        args.putString("Category", categories[i]);
        args.putStringArrayList("Sources", srcByCat.get(categories[i]));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return categories.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return categories[position];
    }


}