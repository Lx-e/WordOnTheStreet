package com.example.csanchez.ift2905_wordonthestreet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;

// Instances of this class are fragments representing a single
// object in our collection.
public class PagerCarlosFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    private String[] srcArr;
    ListView list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_news, container, false);
        list = (ListView) rootView.findViewById(R.id.listView_news);

        NewsFetcher news = new NewsFetcher();
        news.execute();

        return rootView;
    }
    public class NewsFetcher extends AsyncTask<Object, Object, News[]> {

        @Override
        protected News[] doInBackground(Object... params) {

            News[] news = new News[0];
            ArrayList<News> filtered = new ArrayList<News>();
            try {
                Bundle args = getArguments();
                ArrayList<String> srcInCat = args.getStringArrayList("Sources");

                SharedPreferences prefs = getActivity().getSharedPreferences("SavedData", MODE_PRIVATE);
                String sourcesStr = prefs.getString("FavoriteSources", "Nothing");//"No name defined" is the default value.
                Log.v("TAG", "RETRIEVED: "+sourcesStr);

                if(sourcesStr.equals("Nothing")){
                    Log.v("TAG", "Really got : "+sourcesStr);
                    try{
                        String[] cat ={args.getString("Category")};
                        Source[] sources = NewsAPI.getSources(cat); //Retrieve all sources(default)

                        srcArr = new String[sources.length];
                        for(int i=0; i<sources.length; i++){
                            srcArr[i] = sources[i].id;
                        }
                        Log.v("TAG", Arrays.toString(srcArr));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }else{
                    srcArr = sourcesStr.split(","); //Retrieve favorite sources
                }
                news = NewsAPI.getNews(srcArr);

                // On filtre les source par la catégorie du fragment
                for(News aNews : news ){
                    if(srcInCat.contains(aNews.source)){
                        filtered.add(aNews);
                    }
                }
//                if(filtered.size()==0){
//                    try{
//                        String[] cat ={args.getString("Category")};
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
//                    return NewsAPI.getNews(srcArr);
//                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch(ParseException e){
                e.printStackTrace();
            }

            return filtered.toArray(new News[filtered.size()]);
        }

        @Override
        protected void onPostExecute(final News[] news) {

            list.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return news.length;
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

                    if(convertView == null)
                        convertView = getLayoutInflater(null).inflate(R.layout.single_news, parent, false);

                    TextView title = (TextView) convertView.findViewById(R.id.title);
                    TextView date = (TextView) convertView.findViewById(R.id.date);
                    TextView source = (TextView) convertView.findViewById(R.id.source);
                    ImageView image = (ImageView) convertView.findViewById(R.id.image);
                    TextView hidDescription = (TextView) convertView.findViewById(R.id.hiddenDescription);
                    TextView hidUrl = (TextView) convertView.findViewById(R.id.hiddenurl);

                    hidDescription.setText(news[position].description);
                    hidUrl.setText(news[position].url);

                    source.setText(WordUtils.capitalizeFully(news[position].source));
                    title.setText(news[position].title);
                    date.setText(news[position].date.toString());

                    Picasso.with(getActivity().getApplicationContext())
                            .load(news[position].image)
                            .into(image);

                    return convertView;
                }
            });

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position, long id){

                    TextView t = (TextView) view.findViewById(R.id.hiddenurl);
                    String link = t.getText().toString();
                    t = (TextView)view.findViewById(R.id.date);
                    String newsDate = t.getText().toString();
                    t = (TextView)view.findViewById(R.id.hiddenDescription);
                    String desc = t.getText().toString();


                    Intent intent = new Intent(getActivity().getApplicationContext(), SingleNewsExpand.class);
                    Log.v("TAG","FROM NEWSFEED: "+newsDate+desc+link);
                    intent.putExtra("date", newsDate);
                    intent.putExtra("desc", desc);
                    intent.putExtra("link", link);
                    intent.putExtra("caller", "PagerCarlosFragment");
                    startActivity(intent);
                }
            });



        }
    }
}