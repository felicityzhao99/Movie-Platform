package uci.edu.fabflix_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class Login extends ActionBarActivity{
    private static final int MY_SOCKET_TIMEOUT_MS = 10000;
    private EditText username;
    private EditText password;
    private TextView message;
    private Button loginButton;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        message = findViewById(R.id.message);
        loginButton = findViewById(R.id.login);

        url = "https://ec2-3-91-232-225.compute-1.amazonaws.com:8443/fabflix/api/";  //Notice: This is corresponding to the aws https url.
                                                    // Make sure the aws works and make the same url as the website does.
                                                    //Final version: change to 10.0.2.2

        //assign a listener to call a function to handle the user request when clicking a button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    public void login() {
        message.setText("Trying to login...");
        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        //request type is POST
        final StringRequest loginRequest = new StringRequest(Request.Method.POST, url + "mobilelogin?username=" + this.username.getText() + "&password=" +
                this.password.getText(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("login.success", response);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    Log.d("json",jsonObject.toString());
                    if(jsonObject.getString("status").equals("success")){
                        message.setText("");
                        //initialize the activity(page)/destination
                        Intent listPage = new Intent(Login.this, ListViewActivity.class);
                        //without starting the activity/page, nothing would happen
                        startActivity(listPage);
                    }
                    else{
                        message.setText(jsonObject.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("login.error", error.toString());
                        message.setText(error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Post request form data
                final Map<String, String> params = new HashMap<>();
                params.put("username", username.getText().toString());
                params.put("password", password.getText().toString());

                return params;
            }
        };

        loginRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // !important: queue.add is where the login request is actually sent
        queue.add(loginRequest);
    }
}
