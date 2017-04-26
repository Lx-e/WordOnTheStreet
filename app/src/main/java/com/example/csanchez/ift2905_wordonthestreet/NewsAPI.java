package com.example.csanchez.ift2905_wordonthestreet;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewsAPI {
    public static String APIkey = "1f962ecdac564b228b83986d0a29f2b4";
    private static OkHttpClient http;
    private static JSONObject json[] = null;

    private static JSONObject[] getJSON(String[] urls) throws IOException, JSONException {
        json = new JSONObject[urls.length];
        for(int i=0; i<urls.length;i++){
            Request request = new Request.Builder().url(urls[i]).build();

            if(http==null){
                http = new OkHttpClient();
            }

            Response response = http.newCall(request).execute();
            json[i] = new JSONObject(response.body().string());
            System.out.println(json[i].toString());
        }

        return json;
    }

    //Returns all sources
    public static Source[] getSources() throws IOException, JSONException{
        String[] allSourcesUrl = {"https://newsapi.org/v1/sources?&language=en"};
        JSONArray json = getJSON(allSourcesUrl)[0].getJSONArray("sources");

        Source[] sources = new Source[json.length()];

        for(int i=0; i<json.length();i++){
            sources[i] = new Source(
                    ((JSONObject)json.get(i)).getString("id"),
                    ((JSONObject)json.get(i)).getString("name"),
                    ((JSONObject)json.get(i)).getString("description"),
                    ((JSONObject)json.get(i)).getString("url"),
                    ((JSONObject)json.get(i)).getString("category"),
                    ((JSONObject)json.get(i)).getString("language"),
                    ((JSONObject)json.get(i)).getString("country")
            );
        }

        return sources;

    }

    //Returns sources by category
    public static Source[] getSources(String[] categories) throws IOException, JSONException{
        String[] urls = new String[categories.length];
        for(int i=0; i<urls.length;i++){
            urls[i] = "https://newsapi.org/v1/sources?category="+categories[i]+"&language=en";
        }

        JSONObject[] sourcesByCat = getJSON(urls);
        int sourceCount = 0;
        for(int i=0;i<sourcesByCat.length; i++){
            sourceCount += sourcesByCat[i].getJSONArray("sources").length();
        }
        Source[] sources = new Source[sourceCount];
        sourceCount = 0;

        for(int i=0; i<sourcesByCat.length;i++){

            JSONArray catArr = sourcesByCat[i].getJSONArray("sources");

            for(int j=0; j<catArr.length();j++) {
                sources[sourceCount] = new Source(
                        ((JSONObject)catArr.get(j)).getString("id"),
                        ((JSONObject)catArr.get(j)).getString("name"),
                        ((JSONObject)catArr.get(j)).getString("description"),
                        ((JSONObject)catArr.get(j)).getString("url"),
                        ((JSONObject)catArr.get(j)).getString("category"),
                        ((JSONObject)catArr.get(j)).getString("language"),
                        ((JSONObject)catArr.get(j)).getString("country")
                );
                sourceCount++;
            }
        }

        return sources;
    }

    public static News[] getNews(String[] sources) throws IOException, JSONException, ParseException {

        String[] urls = new String[sources.length];
        for(int i=0; i<urls.length;i++){
            urls[i] = "https://newsapi.org/v1/articles?source="+sources[i]+"&sortBy=top&apiKey="+APIkey;
        }

        JSONObject[] sourcesArticles = getJSON(urls);
        int newsCount = 0;
        for(int i=0;i<sourcesArticles.length; i++){
            newsCount += sourcesArticles[i].getJSONArray("articles").length();
        }
        News[] news = new News[newsCount];
        newsCount = 0;
        for(int i=0; i<sourcesArticles.length;i++){

            JSONArray fromSource = sourcesArticles[i].getJSONArray("articles");

            for(int j=0; j<fromSource.length();j++) {
                try {
                    news[newsCount] = new News(
                            ((JSONObject) fromSource.get(j)).getString("title"),
                            ((JSONObject) fromSource.get(j)).getString("description"),
                            sources[i],
                            ((JSONObject) fromSource.get(j)).getString("author"),
                            ((JSONObject) fromSource.get(j)).getString("urlToImage"),
                            ((JSONObject) fromSource.get(j)).getString("url"),
                            ((JSONObject) fromSource.get(j)).getString("publishedAt")
                    );
                    System.out.println(news[newsCount].source +" "+ news[newsCount].date+" "+news[newsCount].url);
                }catch(ParseException e){
                    e.printStackTrace();
                }
                newsCount++;
            }
        }


        Arrays.sort(news, new Comparator<News>() {
            @Override
            public int compare(News o1, News o2) {
                int result = -1;
                return result = o2.date.compareTo(o1.date);
            }
        });

        return news;

    }
}
