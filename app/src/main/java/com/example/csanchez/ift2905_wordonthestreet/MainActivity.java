package com.example.csanchez.ift2905_wordonthestreet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener{

    String[] hardCategories =  {"business", "entertainment", "gaming", "general", "music", "politics", "science-and-nature", "sport", "technology"};
    Map<String, ArrayList<String>> sourcesByCat = new HashMap<String, ArrayList<String>>();

    NewsPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;
    TabLayout tabs;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.getMenu().clear();
        toolbar.setBackground(new ColorDrawable(0x000000FF));
        toolbar.setTitle("");
        toolbar.setSubtitle("");
        toolbar.setBackgroundDrawable(new ColorDrawable(0x000000FF));
        toolbar.setLogo(getDrawable(R.drawable.wots2));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
        changeTypeface(navigationView);

        tabs = (TabLayout) findViewById(R.id.tabLayout);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);

        SourceFetcher srcFetcher = new SourceFetcher();
        srcFetcher.execute();

    }

    public class SourceFetcher extends AsyncTask<Object, Object, Source[]> {

        @Override
        protected Source[] doInBackground(Object... params) {

            Source[] sources = new Source[0];
            List<Source> activeSources = new ArrayList();
            Map<String, Source> sourcesById = new HashMap<String, Source>();

            try {
                sources = NewsAPI.getSources();
                for (Source s: sources) { sourcesById.put(s.id, s);};
                SharedPreferences prefs = getSharedPreferences("SavedData", MODE_PRIVATE);
                String sourcesStr = prefs.getString("FavoriteSources", "Nothing");//"No name defined" is the default value.
                if (!sourcesStr.equals("Nothing")) {
                    String[] srcArr = sourcesStr.split(",");
                    for (String sn: srcArr) {
                        activeSources.add(sourcesById.get(sn));
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("TAG", "Error in Reading: " + e.getLocalizedMessage());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            sources = new Source[activeSources.size()];
            int i = 0;
            for (Source s: activeSources) {sources[i++] = s;};
            return sources;
        }

        @Override
        protected void onPostExecute(final Source[] sources) {

            sourcesByCat.clear();

             //Distribue les sources aux hashmap correspondant à sa catégorie
            for (Source source: sources) {
                if (sourcesByCat.containsKey(source.category)) {
                    sourcesByCat.get(source.category).add(source.id);
                }
                else {
                    ArrayList<String> sourceLst = new ArrayList<String>();
                    sourceLst.add(source.id);
                    sourcesByCat.put(source.category, sourceLst);
                }
            }

            //On lance l'adaptateur avec les sources triées
            if (mDemoCollectionPagerAdapter != null) {
                mDemoCollectionPagerAdapter.update(sourcesByCat);
                mDemoCollectionPagerAdapter.notifyDataSetChanged();
                ((TabLayout)findViewById(R.id.tabLayout)).setupWithViewPager(mViewPager);
            }
            else {
                mDemoCollectionPagerAdapter = new NewsPagerAdapter(getSupportFragmentManager(), sourcesByCat);
                mViewPager = (ViewPager) findViewById(R.id.pager);
                mViewPager.setAdapter(mDemoCollectionPagerAdapter);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        SourceFetcher srcFetcher = new SourceFetcher();
        srcFetcher.execute();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_fav) {
            Toast.makeText(getApplicationContext(), "Favorites", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(getApplicationContext(), CategoryActivity.class), 0);

        }
        else if (id == R.id.nav_history) {
            Toast.makeText(getApplicationContext(), "History", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(getApplicationContext(), HistoryActivity.class), 0);
        }
        else if (id == R.id.nav_book) {
            SharedPreferences prefs = getSharedPreferences("bookmarks", MODE_PRIVATE);
            int size = prefs.getInt("bookmark_size", 0);
            Toast.makeText(getApplicationContext(), "Bookmarks ("+((Integer)size).toString() + ")", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), BookmarkActivity.class);
            startActivityForResult(intent, 0);
        }
        else if (id == R.id.nav_settings) {
            //Toast.makeText(getApplicationContext(), "settings", Toast.LENGTH_SHORT).show();
            SharedPreferences prefs = getSharedPreferences("bookmarks", MODE_PRIVATE);
            Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivityForResult(intent, 0);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void applyFontToItem(MenuItem item, Typeface font) {
        SpannableString mNewTitle = new SpannableString(item.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font, 22), 0 ,
                mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        item.setTitle(mNewTitle);
    }

    private void changeTypeface(NavigationView navigationView){
        FontTypeface fontTypeface = new FontTypeface(this);
        Typeface typeface = fontTypeface.getTypefaceAndroid();

        MenuItem item;

        item = navigationView.getMenu().findItem(R.id.nav_book);
        item.setTitle("Bookmarks");
        applyFontToItem(item, typeface);

        item = navigationView.getMenu().findItem(R.id.nav_fav);
        applyFontToItem(item, typeface);

        item = navigationView.getMenu().findItem(R.id.nav_history);
        applyFontToItem(item, typeface);

        item = navigationView.getMenu().findItem(R.id.nav_settings);
        applyFontToItem(item, typeface);
    }

}
