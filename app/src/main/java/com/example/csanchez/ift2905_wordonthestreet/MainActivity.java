package com.example.csanchez.ift2905_wordonthestreet;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener{

    String[] hardCategories =  {"business", "entertainment", "gaming", "general", "music", "politics", "science-and-nature", "sport", "technology"};
    Map<String, ArrayList<String>> sourcesByCat = new HashMap<String, ArrayList<String>>();

    PagerCarlosAdapter mDemoCollectionPagerAdapter;
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
            //Get toutes les sources
            try {
                sources = NewsAPI.getSources();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("TAG", "Error in Reading: " + e.getLocalizedMessage());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return sources;
        }

        @Override
        protected void onPostExecute(final Source[] sources) {

            // Initialise un hashmap par catégorie
            for(String hc: hardCategories){
                sourcesByCat.put(hc, new ArrayList<String>());
            }

            //Distribue les sources aux hashmap correspondant à sa catégorie
            for (Source source: sources) {
                sourcesByCat.get(source.category).add(source.id);
            }

            //On lance l'adaptateur avec les sources triées
            mDemoCollectionPagerAdapter =
                    new PagerCarlosAdapter(getSupportFragmentManager(), sourcesByCat);
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mDemoCollectionPagerAdapter);
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
}




//package com.example.csanchez.ift2905_wordonthestreet;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.graphics.drawable.Drawable;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.support.design.widget.NavigationView;
//import android.support.v4.view.GravityCompat;
//import android.support.v4.widget.DrawerLayout;
//import android.support.v7.app.ActionBarDrawerToggle;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.BaseAdapter;
//import android.widget.Button;
//import android.graphics.Typeface;
//import android.text.Spannable;
//import android.text.SpannableString;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.squareup.picasso.Picasso;
//
//import org.json.JSONException;
//
//import java.io.IOException;
//import java.text.ParseException;
//import java.util.ArrayList;
//import java.util.Arrays;
//
//import java.util.HashMap;
//
//public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
//
//    ListView list;
//    private String[] srcArr;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.getMenu().clear();
//        toolbar.setBackground(new ColorDrawable(0x000000FF));
//        toolbar.setTitle("");
//        toolbar.setSubtitle("");
//        toolbar.setBackgroundDrawable(new ColorDrawable(0x000000FF));
//        toolbar.setLogo(getDrawable(R.drawable.wots2));
//
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();
//
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
//
//        list = (ListView)findViewById(R.id.listView_main);
//
//        final NewsFetcher news = new NewsFetcher();
//        news.execute();
//
//        changeTypeface(navigationView);
//
//        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
//
//                TextView t = (TextView) view.findViewById(R.id.hiddenurl);
//                String link = t.getText().toString();
//                t = (TextView)view.findViewById(R.id.date);
//                String newsDate = t.getText().toString();
//                t = (TextView)view.findViewById(R.id.hiddenDescription);
//                String desc = t.getText().toString();
//
//
//                Intent intent = new Intent(getApplicationContext(), SingleNewsExpand.class);
//
//                intent.putExtra("date", newsDate);
//                intent.putExtra("desc", desc);
//                intent.putExtra("link", link);
//                intent.putExtra("caller", "MainActivity");
//                startActivity(intent);
//            }
//        });
//    }
//
//    public class NewsFetcher extends AsyncTask<Object, Object, News[]> {
//
//        @Override
//        protected News[] doInBackground(Object... params) {
//
//            News[] news = new News[0];
//
//            try {
//                SharedPreferences prefs = getSharedPreferences("SavedData", MODE_PRIVATE);
//                String sourcesStr = prefs.getString("FavoriteSources", "Nothing");//"No name defined" is the default value.
//                Log.v("TAG", "RETRIEVED: "+sourcesStr);
//
//                if(sourcesStr.equals("Nothing")){
//                    Log.v("TAG", "Really got : "+sourcesStr);
//                    try{
//                        String[] cat ={"general"};
//                        Source[] sources = NewsAPI.getSources(cat); //Retrieve all sources(default)
//
//                        srcArr = new String[sources.length];
//                        for(int i=0; i<sources.length; i++){
//                            srcArr[i] = sources[i].id;
//                        }
//                        Log.v("TAG", Arrays.toString(srcArr));
//                    }catch(Exception e){
//                        e.printStackTrace();
//                    }
//                }else{
//                    srcArr = sourcesStr.split(","); //Retrieve favorite sources
//                }
//                news = NewsAPI.getNews(srcArr);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//
//            return news;
//        }
//
//        @Override
//        protected void onPostExecute(final News[] news) {
//
//            list.setAdapter(new BaseAdapter() {
//                @Override
//                public int getCount() {
//                    return news.length;
//                }
//
//                @Override
//                public Object getItem(int position) {
//                    return null;
//                }
//
//                @Override
//                public long getItemId(int position) {
//                    return 0;
//                }
//
//                @Override
//                public View getView(int position, View convertView, ViewGroup parent) {
//                    if (convertView == null)
//                        convertView = getLayoutInflater().inflate(R.layout.single_news, parent, false);
//
//                    TextView title = (TextView) convertView.findViewById(R.id.title);
//                    TextView date = (TextView) convertView.findViewById(R.id.date);
//                    ImageView image = (ImageView) convertView.findViewById(R.id.image);
//                    TextView hidDescription = (TextView) convertView.findViewById(R.id.hiddenDescription);
//                    TextView hidUrl = (TextView) convertView.findViewById(R.id.hiddenurl);
//
//                    title.setText(news[position].title);
//                    date.setText(news[position].date.toString());
//
//                    Picasso.with(getApplicationContext())
//                            .load(news[position].image)
//                            .into(image);
//
//                    return convertView;
//                }
//            });
//
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        /*if (id == R.id.action_settings) {
//            return true;
//        }*/
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//
//        if (id == R.id.nav_fav) {
//            Toast.makeText(getApplicationContext(), "favorites", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(getApplicationContext(), CategoryActivity.class));
//        }
//        else if (id == R.id.nav_history) {
//            Toast.makeText(getApplicationContext(), "history", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
//        }
//        else if (id == R.id.nav_book) {
//            SharedPreferences prefs = getSharedPreferences("bookmarks", MODE_PRIVATE);
//            int size = prefs.getInt("bookmark_size", 0);
//
//            Toast.makeText(getApplicationContext(), "bookmarks"+((Integer)size).toString(), Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(getApplicationContext(), BookmarkActivity.class);
//            startActivity(intent);
//        }
//        else if (id == R.id.nav_settings) {
//            //Toast.makeText(getApplicationContext(), "settings", Toast.LENGTH_SHORT).show();
//            SharedPreferences prefs = getSharedPreferences("bookmarks", MODE_PRIVATE);
//            Toast.makeText(getApplicationContext(), ((Integer)prefs.getInt("bookmark_size",0)).toString(), Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(getApplicationContext(), PagerCarlos.class);
//            startActivity(intent);
//        }
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }
//
//    private void applyFontToItem(MenuItem item, Typeface font) {
//        SpannableString mNewTitle = new SpannableString(item.getTitle());
//        mNewTitle.setSpan(new CustomTypefaceSpan("", font, 30), 0 ,
//                mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//        item.setTitle(mNewTitle);
//    }
//
//    private void changeTypeface(NavigationView navigationView){
//        FontTypeface fontTypeface = new FontTypeface(this);
//        Typeface typeface = fontTypeface.getTypefaceAndroid();
//
//        MenuItem item;
//
//        item = navigationView.getMenu().findItem(R.id.nav_book);
//        item.setTitle("Bookmarks");
//        applyFontToItem(item, typeface);
//
//        item = navigationView.getMenu().findItem(R.id.nav_fav);
//        applyFontToItem(item, typeface);
//
//        item = navigationView.getMenu().findItem(R.id.nav_history);
//        applyFontToItem(item, typeface);
//
//        item = navigationView.getMenu().findItem(R.id.nav_settings);
//        applyFontToItem(item, typeface);
//    }
//
//
//
//}
