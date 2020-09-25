package uci.edu.fabflix_mobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class singleMovie extends ActionBarActivity{
    private TextView title;
    private TextView year;
    private TextView genres;
    private TextView director;
    private TextView stars;
    private Button backButton;
    private TextView errText;
    private String url = "https://ec2-3-91-232-225.compute-1.amazonaws.com:8443/fabflix/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singlemovie);
        title = findViewById(R.id.movieTitle);
        year = findViewById(R.id.movieYear);
        genres = findViewById(R.id.movieGenres);
        director = findViewById(R.id.movieDirector);
        stars = findViewById(R.id.movieStars);
        backButton = findViewById(R.id.bck_btn);
        errText = findViewById(R.id.errtext);
        // set text for the above fields
        setTextFields();

        //assign a listener to call a function to handle the user request when clicking a button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToHome();
            }
        });
    }

    public void backToHome(){
        super.onBackPressed();
    }

    public void setTextFields(){
        Bundle b = getIntent().getExtras();
        String movieId = "null";
        if(b != null)
            movieId = b.getString("movieId");

        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        //request type is POST
        final StringRequest movieRequest = new StringRequest(Request.Method.GET, url + "movie?id=" + movieId, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                JSONArray array = null;
                try {
                    array = new JSONArray(response);
                    jsonObject = array.getJSONObject(0);
                    // Log.d("json",jsonObject.toString());
                    title.setText(jsonObject.getString("title"));
                    year.setText(jsonObject.getString("year"));
                    director.setText(jsonObject.getString("director"));
                    genres.setText(jsonObject.getString("genres"));
                    stars.setText(jsonObject.getString("stars"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    errText.setText(e.toString());
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errText.setText(error.toString());
                    }
                });

        // !important: queue.add is where the login request is actually sent
        queue.add(movieRequest);
    }

}
