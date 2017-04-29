package com.example.csanchez.ift2905_wordonthestreet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;




public class SingleNewsExpand extends AppCompatActivity implements View.OnClickListener{

    private TextView textviewDate;
    private TextView textviewDesc;
    boolean bookmarked=false;
    Button brow;
    Button share;
    Button toggle;
    String link;
    String date;
    String desc;
    String caller;
    static int i, j;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singlenewsexpand);
        SharedPreferences prefs = getSharedPreferences("bookmarks", MODE_PRIVATE);
        SharedPreferences prefsH = getSharedPreferences("history", MODE_PRIVATE);
        SharedPreferences.Editor e = getSharedPreferences("bookmarks",MODE_PRIVATE).edit();
        SharedPreferences.Editor ee = getSharedPreferences("history",MODE_PRIVATE).edit();
        caller     = getIntent().getStringExtra("caller");
        textviewDate = (TextView)findViewById(R.id.textDate);
        textviewDesc = (TextView)findViewById(R.id.textDesc);

        brow = (Button)findViewById(R.id.button2);
        share =(Button)findViewById(R.id.button4);
        toggle = (Button)findViewById(R.id.button5);

        brow.setOnClickListener(this);
        share.setOnClickListener(this);
        toggle.setOnClickListener(this);

        int size = prefs.getInt("bookmark_size", 0);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        Log.v("TAG",b.get("date").toString()+b.get("desc").toString()+b.get("link").toString());
        if(b!=null){

            TextView title = (TextView)findViewById(R.id.textTitle);
            ImageView image = (ImageView) findViewById(R.id.image);
            String titleStr = (String) b.get("title");
            String imageStr = (String) b.get("image");
            title.setText(titleStr);
            Picasso.with(getApplicationContext())
                    .load(imageStr)
                    .into(image);


            date = (String) b.get("date");
            textviewDate.setText(date);
            desc = (String) b.get("desc");
            textviewDesc.setText(desc);
            link = (String) b.get("link");
            if(!(caller.equals("HistoryActivity")||caller.equals("BookmarkActivity"))){
                Calendar c = Calendar.getInstance();
                System.out.println("Current time => " + c.getTime());
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                String historyDate = df.format(c.getTime());

                j=prefsH.getInt("history_size", 0);
                ee.putInt("history_size",j+1);
                String listHistory = "H_url"+j;
                String titleHistory = "H_title"+j;
                String dateHistory = "H_date"+j;
                ee.putString(listHistory, link);
                ee.putString(titleHistory, desc);
                ee.putString(dateHistory, "Visited on "+historyDate);
                ee.commit();
            }
        }
        String urlbook;
        for(int k=0;k<size+1;k++){
            urlbook = prefs.getString("url"+((Integer)k).toString(), null);
           if(link.equals(urlbook)){
               toggle.setBackgroundResource(R.drawable.ic_book_black_48dp);
               bookmarked = true;
           }
        }

    }
    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.button2:
                Uri uri = Uri.parse(link);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.button4:
                shareIt();
                break;
            case R.id.button5:
                if(!bookmarked) {
                    //mettre bookmark
                    toggle.setBackgroundResource(R.drawable.ic_book_black_48dp);
                    SharedPreferences prefs = getSharedPreferences("bookmarks", MODE_PRIVATE);
                    SharedPreferences.Editor e = getSharedPreferences("bookmarks",MODE_PRIVATE).edit();
                    i=prefs.getInt("bookmark_size", 0);
                    e.putInt("bookmark_size",i+1);

                    String listCount = "url"+i;
                    String titleCount = "title"+i;
                    String dateCount = "date"+i;

                    e.putString(listCount, link);
                    e.putString(titleCount, desc);
                    e.putString(dateCount, date);
                    e.commit();

                    bookmarked = true;
                }
                else{
                    //retirer bookmark
                    String urlbook;
                    int pivot=0;
                    boolean sucessRemove=false;
                    SharedPreferences prefs = getSharedPreferences("bookmarks", MODE_PRIVATE);
                    SharedPreferences.Editor e = getSharedPreferences("bookmarks",MODE_PRIVATE).edit();
                    int size = prefs.getInt("bookmark_size", 0);

                    for(int k=0;k<size+1;k++){
                        urlbook = prefs.getString("url"+((Integer)k).toString(), null);
                        if(link.equals(urlbook)){
                            pivot = k;
                            e.remove("url"+((Integer)k).toString());
                            e.remove("title"+((Integer)k).toString());
                            e.remove("date"+((Integer)k).toString());
                            e.apply();
                            sucessRemove = true;
                        }
                    }
                    if(sucessRemove) {
                        for (int k = pivot; k < size ; k++){
                            String listCount = "url"+k;
                            String titleCount = "title"+k;
                            String dateCount = "date"+k;

                            String newLink = prefs.getString("url"+((Integer)(k+1)).toString(), null);
                            String newDesc = prefs.getString("title"+((Integer)(k+1)).toString(), null);
                            String newDate = prefs.getString("date"+((Integer)(k+1)).toString(), null);

                            e.putString(listCount, newLink);
                            e.putString(titleCount, newDesc);
                            e.putString(dateCount, newDate);
                            e.commit();
                        }
                        e.putInt("bookmark_size", size-1);
                        e.apply();
                    }

                    toggle.setBackgroundResource(R.drawable.ic_bookmark_border_white_48dp);
                    bookmarked = false;
                }

                break;
        }
    }
    private void shareIt() {

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, desc);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, link);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        caller     = getIntent().getStringExtra("caller");
        if(caller.equals("BookmarkActivity"))
            startActivity(new Intent(this, BookmarkActivity.class));
        else if(caller.equals("HistoryActivity"))
            startActivity(new Intent(this, HistoryActivity.class));
        else
            startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
