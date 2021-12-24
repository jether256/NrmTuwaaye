package com.jether.nrmtuwaaye;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jether.nrmtuwaaye.Adapters.AdapterSourceList;
import com.jether.nrmtuwaaye.Models.ModelSourceList;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewsActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private EditText searchEt;
    private ImageButton filterBtn;
    private RecyclerView news;

    private ArrayList<ModelSourceList> sourceLists;
    private AdapterSourceList adapterSourceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        progressBar = findViewById(R.id.progressBar);
        searchEt = findViewById(R.id.searchEt);
        filterBtn = findViewById(R.id.filterBtn);
        news = findViewById(R.id.news);

        loadSources();

        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //called as wen the user type/remove letter
                try{
                    adapterSourceList.getFilter().filter(charSequence);
                }catch(Exception e){

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void loadSources() {
        sourceLists = new ArrayList<>();
        sourceLists.clear();

    //request data
    String url = "https://newsapi.org/v2/sources?apiKey=" + Constants.API_KEY;

    StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            //response is to get string

            try {
                //convert string to json object
                JSONObject jsonObject = new JSONObject(response);
                //get sorces array from that object
                JSONArray jsonArray = jsonObject.getJSONArray("sources");

                //get all data from that array using loop
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                    String id = jsonObject1.getString("id");
                    String name = jsonObject1.getString("name");
                    String description = jsonObject1.getString("description");
                    String url = jsonObject1.getString("url");
                    String country = jsonObject1.getString("country");
                    String category = jsonObject1.getString("category");
                    String language = jsonObject1.getString("language");


                    //set data to mode
                    ModelSourceList model = new ModelSourceList(
                            "" + id,
                            "" + name,
                            "" + description,
                            "" + url,
                            "" + category,
                            "" + language,
                            "" + country
                    );
                    //add model to list
                    sourceLists.add(model);

                }
                //set up adapter
                adapterSourceList = new AdapterSourceList(NewsActivity.this, sourceLists);
                news.setAdapter(adapterSourceList);

            } catch (Exception e) {
                //exception while loading the Json data
                Toast.makeText(NewsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }


        }
    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            //error while requesting response
            progressBar.setVisibility(View.GONE);
            Toast.makeText(NewsActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();

        }
    });

    //add request to queue
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }
}