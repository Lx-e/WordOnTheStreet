package com.example.csanchez.ift2905_wordonthestreet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class PagerCarlosAdapter extends FragmentStatePagerAdapter {
//    String[] categories =  {"business", "entertainment", "gaming", "general", "music", "politics", "science-and-nature", "sport", "technology"};
    Map<String, ArrayList<String>> srcByCat = new HashMap<String, ArrayList<String>>();
    List<String> cats = new ArrayList<String>();
    public PagerCarlosAdapter(FragmentManager fm, Map<String, ArrayList<String>> sourcesByCat) {
        super(fm);
        for (Map.Entry<String, ArrayList<String>> me: sourcesByCat.entrySet()) {
            if (me.getValue().size() == 0) continue;
            this.srcByCat.put(me.getKey(), me.getValue());
            this.cats.add(me.getKey());
        }
    }

    public void update(Map<String, ArrayList<String>> sourcesByCat) {
        this.srcByCat.clear();
        this.cats.clear();
        for (Map.Entry<String, ArrayList<String>> me: sourcesByCat.entrySet()) {
            if (me.getValue().size() == 0) continue;
            this.srcByCat.put(me.getKey(), me.getValue());
            this.cats.add(me.getKey());
        }
        int x = 3;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new PagerCarlosFragment();
        Bundle args = new Bundle();
        //On va chercher la catégorie concernée ainsi que les sources y appartenant
        args.putString("Category", cats.get(i));
        args.putStringArrayList("Sources", srcByCat.get(cats.get(i)));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return cats.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return WordUtils.capitalizeFully(cats.get(position));
    }


}