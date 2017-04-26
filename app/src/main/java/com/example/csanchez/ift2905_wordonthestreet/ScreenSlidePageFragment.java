package com.example.csanchez.ift2905_wordonthestreet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScreenSlidePageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScreenSlidePageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScreenSlidePageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ScreenSlidePageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScreenSlidePageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScreenSlidePageFragment newInstance(String param1, String param2) {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ScrollView scrollView = (ScrollView) inflater.inflate(R.layout.fragment_screen_slide_page, container, false);
        ListView newsList = (ListView)scrollView.findViewById(R.id.listView_main2);

        newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){

                TextView t = (TextView) view.findViewById(R.id.hiddenurl);
                String link = t.getText().toString();
                t = (TextView)view.findViewById(R.id.date);
                String newsDate = t.getText().toString();
                t = (TextView)view.findViewById(R.id.hiddenDescription);
                String desc = t.getText().toString();


                Intent intent = new Intent(getContext(), SingleNewsExpand.class);

                intent.putExtra("date", newsDate);
                intent.putExtra("desc", desc);
                intent.putExtra("link", link);
                intent.putExtra("caller", "MainActivity");
                startActivity(intent);
            }
        });

        final NewsFetcher news = new NewsFetcher(newsList);
        news.execute();

        // Inflate the layout for this fragment
        return scrollView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class NewsFetcher extends AsyncTask<Object, Object, News[]> {

        String[] srcArr;
        ListView newsList = null;

        protected NewsFetcher(ListView newsList) {
            this.newsList = newsList;
        }

        @Override
        protected News[] doInBackground(Object... params) {

            News[] news = new News[0];

            try {
                SharedPreferences prefs = getActivity().getSharedPreferences("SavedData", MODE_PRIVATE);
                String sourcesStr = prefs.getString("CustomSources", "Nothing");//"No name defined" is the default value.
                Log.v("TAG", "RETRIEVED: "+sourcesStr);

                sourcesStr = "Nothing";
                if(sourcesStr.equals("Nothing")){
                    Log.v("TAG", "Really got : "+sourcesStr);
                    try{
                        String[] cat ={"general"};
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

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return news;
        }

        @Override
        protected void onPostExecute(final News[] news) {

            newsList.setAdapter(new BaseAdapter() {
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
                    if (convertView == null)
                        convertView = getActivity().getLayoutInflater().inflate(R.layout.single_news, parent, false);

                    TextView title = (TextView) convertView.findViewById(R.id.title);
                    TextView date = (TextView) convertView.findViewById(R.id.date);
                    ImageView image = (ImageView) convertView.findViewById(R.id.image);
                    TextView hidDescription = (TextView) convertView.findViewById(R.id.hiddenDescription);
                    TextView hidUrl = (TextView) convertView.findViewById(R.id.hiddenurl);

                    title.setText(news[position].title);
                    date.setText(news[position].date.toString());
                    hidDescription.setText(news[position].description);
                    hidUrl.setText(news[position].url);
                    Picasso.with(getActivity().getApplicationContext())
                            .load(news[position].image)
                            .into(image);

                    return convertView;
                }
            });

        }
    }

}
