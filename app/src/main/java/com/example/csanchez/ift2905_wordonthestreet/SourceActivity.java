package com.example.csanchez.ift2905_wordonthestreet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SourceActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    String categoryName = null;
    ListView list = null;

    List<Source> allSources = new ArrayList<Source>();
    List<Source> favoriteSources = new ArrayList<Source>();
    List<Source> initialFavoriteSources = new ArrayList<Source>();
    List<Source> categorySources = new ArrayList<Source>();

    Map<String, Source> namesToSources = new HashMap<String, Source>();
    Map<String, Source> idsToSources = new HashMap<String, Source>();
    Map<View, Source> viewsToSources = new HashMap<View, Source>();
    Map<Source, CheckBox> sourcesToCheckboxes = new HashMap<Source, CheckBox>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sources);

        categoryName = getIntent().getStringExtra("Category");

        list = (ListView) findViewById(R.id.listView_sources);

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

//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
        changeTypeface(navigationView);

        navigationView.getMenu().findItem(R.id.nav_fav).setEnabled(false);

        ((TextView)findViewById(R.id.select_source_instr)).setText("Select " + categoryName + " sources");
        SourceActivity.SourceFetcher sourcesFetcher = new SourceActivity.SourceFetcher();
        sourcesFetcher.execute();
    }

    protected void loadFavoriteSources() {

        String sourcesStr = getSharedPreferences("SavedData", MODE_PRIVATE).getString("FavoriteSources", "Nothing");//"No name defined" is the default value.
        Log.v("TAG", "RETRIEVED FAVORITE SOURCES: "+sourcesStr);

        if(sourcesStr == null || sourcesStr.equals("Nothing")) return;

        String[] sourceIds = sourcesStr.split(",");

        for (String sourceIdStr: sourceIds) {
            sourceIdStr = sourceIdStr.trim();
            Log.v("TAG", "Parsed: " + sourceIdStr);
            if (sourceIdStr.length() > 0 && idsToSources.containsKey(sourceIdStr)) {
                Source source =  idsToSources.get(sourceIdStr);
                if (!favoriteSources.contains(source)) favoriteSources.add(source);
            }
        }
    }

    protected void saveFavoriteSources() {

        StringBuffer sourcesBuffer = new StringBuffer();
        if (favoriteSources.size() == 0)
            sourcesBuffer.append("Nothing");
        else
            for (Source source: favoriteSources) { sourcesBuffer.append(source.id).append(","); }

        SharedPreferences.Editor editor = getSharedPreferences("SavedData", MODE_PRIVATE).edit();
        editor.remove("FavoriteSources");
        editor.putString("FavoriteSources", sourcesBuffer.toString());
        editor.commit();
        Log.v("TAG", "SAVING FAVORITE SOURCES: "+ sourcesBuffer.toString());
    }

    //Inspiré de http://stackoverflow.com/questions/14509552/uncheck-all-checbox-in-listview-in-android
    private void unselectAll(ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View v = vg.getChildAt(i);
            if (v instanceof CheckBox) {
                CheckBox cb = (CheckBox)v;
                if (cb.isChecked()) {
                    cb.setChecked(false);
                }
            } else if (v instanceof ViewGroup) {
                unselectAll((ViewGroup) v);
            }
        }
        for (Source s: categorySources) {
            if (favoriteSources.contains(s)) {
                favoriteSources.remove(s);
            }
        }
    }

    //Inspiré de http://stackoverflow.com/questions/14509552/uncheck-all-checbox-in-listview-in-android
    private void selectAll(ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View v = vg.getChildAt(i);
            if (v instanceof CheckBox) {
                CheckBox cb = (CheckBox)v;
                if (!cb.isChecked()) {
                    cb.setChecked(true);
                }
            } else if (v instanceof ViewGroup) {
                selectAll((ViewGroup) v);
            }
        }
        for (Source s: categorySources) {
            if (!favoriteSources.contains(s)) {
                favoriteSources.add(s);
            }
        }
    }

    private void revertAll(ViewGroup vg) {
        unselectAll(vg);
        for (Source s: initialFavoriteSources) {
            if (!categoryName.equals(s.category)) continue;
            CheckBox cb = sourcesToCheckboxes.get(s);
            cb.setChecked(true);
        }
        favoriteSources.clear();
        favoriteSources.addAll(initialFavoriteSources);
        saveFavoriteSources();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        String sourcesStr = getSharedPreferences("SavedData", MODE_PRIVATE).getString("FavoriteSources", "Nothing");//"No name defined" is the default value.
        int resultCount = 0;
        if(sourcesStr != null && !sourcesStr.equals("Nothing")) {
            String[] sourceNames = sourcesStr.split(",");
            for (String sourceName: sourceNames) {
                Source source =  idsToSources.get(sourceName.trim());
                if (source.category.equals(categoryName)) resultCount++;
            }
        }


        Intent intent = new Intent();
        intent.putExtra("CategoryName", categoryName);
        intent.putExtra("FavoriteCount", resultCount);
        setResult(RESULT_OK, intent);
        finish();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_fav) {
            Toast.makeText(getApplicationContext(), "favorites", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), CategoryActivity.class));
        }
        else if (id == R.id.nav_history) {
            Toast.makeText(getApplicationContext(), "history", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
        }
        else if (id == R.id.nav_book) {
            SharedPreferences prefs = getSharedPreferences("bookmarks", MODE_PRIVATE);
            int size = prefs.getInt("bookmark_size", 0);

            Toast.makeText(getApplicationContext(), "bookmarks"+((Integer)size).toString(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), BookmarkActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_settings) {
            //Toast.makeText(getApplicationContext(), "settings", Toast.LENGTH_SHORT).show();
            SharedPreferences prefs = getSharedPreferences("bookmarks", MODE_PRIVATE);
            Toast.makeText(getApplicationContext(), ((Integer)prefs.getInt("bookmark_size",0)).toString(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        finish();
        return true;
    }


    private void applyFontToItem(MenuItem item, Typeface font) {
        SpannableString mNewTitle = new SpannableString(item.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font, 30), 0 ,
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

    @Override
    public void onClick(View v) {

        Source source = viewsToSources.get(v);
        if (source == null) return;

        if (favoriteSources.contains(source)) {
            favoriteSources.remove(source);
            saveFavoriteSources();
            Log.v("TAG", "Favorite source removed: " + new Gson().toJson(source));
        }
        else {
            favoriteSources.add(source);
            saveFavoriteSources();
            Log.v("TAG", "Favorite source added: " + new Gson().toJson(source));
        }

        CheckBox cb = (v instanceof CheckBox) ? null : (CheckBox)v.findViewById(R.id.checkbox);
        if (cb != null) cb.setChecked(!cb.isChecked());

    }

    public class SourceFetcher extends AsyncTask<Object, Object, Source[]> {

        @Override
        protected Source[] doInBackground(Object... params) {

            Source[] sources = new Source[0];

            try {
                sources = NewsAPI.getSources();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("TAG", "Error in Reading: " + e.getLocalizedMessage());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            for (Source source: sources) {
                allSources.add(source);
                idsToSources.put(source.id, source);
                namesToSources.put(source.name, source);
                if (source.category.equals(categoryName)) categorySources.add(source);
            }

            loadFavoriteSources();
            initialFavoriteSources.addAll(favoriteSources);

            return sources;
        }

        @Override
        protected void onPostExecute(final Source[] sources) {

            ((ImageButton)findViewById(R.id.uncheck_all_button)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unselectAll(list);
                    saveFavoriteSources();
                }
            });

            ((ImageButton)findViewById(R.id.check_all_button)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectAll(list);
                    saveFavoriteSources();
                }
            });

            ((ImageButton)findViewById(R.id.revert_button)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    revertAll(list);
                    saveFavoriteSources();
                }
            });

            list.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return categorySources.size();
                }

                @Override
                public Object getItem(int position) {
                    return null;
                }

                @Override
                public long getItemId(int position) {
                    return 0;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {

                    Source source = categorySources.get(position);

                    if(convertView == null)
                        convertView = getLayoutInflater().inflate(R.layout.single_source, parent, false);

                    TextView nameView = (TextView) convertView.findViewById(R.id.name);
                    TextView countView = (TextView) convertView.findViewById(R.id.count);
                    CheckBox checkBoxView = (CheckBox) convertView.findViewById(R.id.checkbox);

                    countView.setVisibility(View.INVISIBLE);
                    countView.setText("");
                    nameView.setText(source.name);
                    checkBoxView.setChecked(favoriteSources.contains(source));

                    convertView.setOnClickListener(SourceActivity.this);
                    checkBoxView.setOnClickListener(SourceActivity.this);

                    viewsToSources.put(convertView, source);
                    viewsToSources.put(checkBoxView, source);
                    sourcesToCheckboxes.put(source, checkBoxView);

                    return convertView;
                }
            });
        }
    }
}
