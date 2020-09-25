package uci.edu.fabflix_mobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.*;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListViewActivity extends Activity{
    private SearchView search;
    private String name;
    private static String url;
    private ArrayList<Movie> movies = new ArrayList<>();
    private Context context = this;
    private Button prev;
    private Button next;
    private int initial_no = 0;

    public static String getURL() {
        return url;
    }

    public void search_movie(String url){
        System.out.println("Trying to search...");
        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        //request type is POST
        final StringRequest searchRequest = new StringRequest(Request.Method.GET, url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("search.success", response);
                // to handle empty result set
                if(response.equals("[]")){
                    if(initial_no >= 20)
                        initial_no -= 20;
                    Toast.makeText(ListViewActivity.this, "You have reached the last page!",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(response);
                    Log.d("json",jsonArray.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject child =jsonArray.getJSONObject(i);
                        String movie_title = (String) child.get("movie_title");
                        String movie_year = (String) child.get("movie_year");
                        String movie_director = (String) child.get("movie_director");
                        String movie_genre = (String) child.get("movie_genre");
                        String movie_actor = (String) child.get("movie_actor");
                        String movie_id = (String) child.get("movie_id");
                        movies.add(new Movie(movie_id, movie_title, movie_year, movie_director, movie_genre, movie_actor));
                    }
                    MovieListViewAdapter adapter = new MovieListViewAdapter(movies, context);
                    ListView listView = findViewById(R.id.list);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Movie movie = movies.get(position);
                            Intent intent = new Intent(ListViewActivity.this, singleMovie.class);
                            Bundle b = new Bundle();
                            b.putString("movieId", movie.getId()); //movieId
                            intent.putExtras(b); //Put your id to your next Intent
                            startActivity(intent);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("search.error", error.toString());

                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Post request form data
                final Map<String, String> params = new HashMap<>();
                return params;
            }
        };
        queue.add(searchRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);

        prev = (Button) findViewById(R.id.prev);
        next = (Button) findViewById(R.id.next);

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // to prevent crash
                if(url==null || url.equals(""))
                    return;
                String url2 = url;
                if (initial_no >= 20) {
                    initial_no -= 20;
                    url += "&offset=";
                    url += Integer.toString(initial_no);
                    System.out.println(url);
                    movies = new ArrayList<>();
                    search_movie(url);
                    url = url2;
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(url==null || url.equals(""))
                    return;
                String url3 = url;
                initial_no += 20;
                url += "&offset=";
                url += Integer.toString(initial_no);
                System.out.println(url);
                movies = new ArrayList<>();
                search_movie(url);
                url = url3;
            }
        });


        search = (SearchView) findViewById(R.id.search_movie);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                initial_no = 0;
                name = query.trim();
                Toast.makeText(ListViewActivity.this, "Result: " + query, Toast.LENGTH_LONG).show();
                url = "https://ec2-3-91-232-225.compute-1.amazonaws.com:8443/fabflix/api/search?title=" + name +
                        "&limit=19";
                System.out.println(url);

                movies = new ArrayList<>();
                search_movie(url);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                return false;
            }
        });

    }



}
