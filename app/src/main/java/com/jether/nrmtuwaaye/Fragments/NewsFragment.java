package com.jether.nrmtuwaaye.Fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.jether.nrmtuwaaye.Adapters.AdapterSourceList;
import com.jether.nrmtuwaaye.Constants;
import com.jether.nrmtuwaaye.Models.ModelSourceList;
import com.jether.nrmtuwaaye.NewsActivity;
import com.jether.nrmtuwaaye.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewsFragment extends Fragment {

    private ProgressBar progressBar;
    private EditText searchEt;
    private ImageButton filterBtn;
    private RecyclerView news;

    private ArrayList<ModelSourceList> sourceLists;
    private AdapterSourceList adapterSourceList;
    public NewsFragment() {
        // Required empty public constructor
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_news, container, false);

        progressBar =view.findViewById(R.id.progressBar);
        searchEt = view.findViewById(R.id.searchEt);
        filterBtn =view. findViewById(R.id.filterBtn);
        news = view.findViewById(R.id.news);


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


        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterBottomSheet();
            }
        });
        return view;
    }

    //initially selected items

    private String selectedCountry="All",selectedCategory="All",selectedLanguage="All";
    private int selectedCountryPosition=0,selectedCategoryPosition=0,selectedlanguagePosition=0;

    private void filterBottomSheet() {

        View view=LayoutInflater.from(getActivity()).inflate(R.layout.filter_layout,null);

        Spinner countrySpinner=view.findViewById(R.id.countrySpinner);
        Spinner categorySpinner=view.findViewById(R.id.categorySpinner);
        Spinner languageSpinner=view.findViewById(R.id.languageSpinner);
        Button apply= view.findViewById(R.id.apply);

        //create Arrayadapter using the string array and a default spinner layout
        ArrayAdapter<String> adapterCountries=new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item,Constants.COUNTRIES);
        ArrayAdapter<String> adapterCategories=new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item,Constants.CATEGORIES);
        ArrayAdapter<String> adapterlanguage=new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item,Constants.LANGUAGES);

        //specify the layout to use when the list of choices appears
        adapterCountries.setDropDownViewResource(android.R.layout.simple_spinner_item);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_item);
        adapterlanguage.setDropDownViewResource(android.R.layout.simple_spinner_item);

        //apply aadapter to our spinner
        countrySpinner.setAdapter(adapterCountries);
        categorySpinner.setAdapter(adapterCategories);
        languageSpinner.setAdapter(adapterlanguage);

        //set last selected value
        countrySpinner.setSelection(selectedCountryPosition);
        categorySpinner.setSelection(selectedCategoryPosition);
        languageSpinner.setSelection(selectedlanguagePosition);

        //spinner item selected listeners
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCountry=Constants.COUNTRIES[i];
                selectedCountryPosition=i;

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCategory=Constants.CATEGORIES[i];
                selectedCategoryPosition=i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedLanguage=Constants.LANGUAGES[i];
                selectedlanguagePosition=i;

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //set up bottom sheet dialog
        BottomSheetDialog bottomSheetDialog= new BottomSheetDialog(getActivity());
//add layoutview to bottomsheet
        bottomSheetDialog.setContentView(view);
        //show bottomsheet
        bottomSheetDialog.show();

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();

                loadSources();
            }
        });
    }


    private void loadSources() {
        Log.d("FILTER_TAG","Country"+selectedCountry);
        Log.d("FILTER_TAG","Category"+selectedCategory);
        Log.d("FILTER_TAG","Language"+selectedLanguage);


        //well as our initial value is "All" thats y ist not filtering data lets replace it with "";
        if(selectedCountry.equals("All")){
            selectedCountry="";
        }
        if(selectedCategory.equals("All")){
selectedCategory="";
        }

        if(selectedLanguage.equals("All")){
selectedLanguage="";
        }


        sourceLists = new ArrayList<>();
        sourceLists.clear();

        //request data
        String url = "https://newsapi.org/v2/sources?apiKey=" + Constants.API_KEY+"&country="+selectedCountry+"&category"+selectedCategory+"&language="+selectedLanguage;

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
                    adapterSourceList = new AdapterSourceList(getActivity(), sourceLists);
                    news.setAdapter(adapterSourceList);

                } catch (Exception e) {
                    //exception while loading the Json data
                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //error while requesting response
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        //add request to queue
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//to show menu option in fragment
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        menu.clear();
        inflater.inflate(R.menu.menu_main,menu);

        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_add_goupInfo).setVisible(false);


    }

}