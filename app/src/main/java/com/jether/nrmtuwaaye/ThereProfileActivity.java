package com.jether.nrmtuwaaye;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jether.nrmtuwaaye.Adapters.AdapterPosts;
import com.jether.nrmtuwaaye.Models.ModelPost;
import com.jether.nrmtuwaaye.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {
    ImageView avatarTv,coverTv;
    TextView nameTv,emailTv,phoneTv;
    FirebaseAuth firebaseAuth;
    RecyclerView postsRecyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        postsRecyclerView=findViewById(R.id.recycler_viewPosts);
        avatarTv=findViewById(R.id.avatarTv);
        nameTv=findViewById(R.id.nameTv);
        emailTv=findViewById(R.id.emailTv);
        phoneTv=findViewById(R.id.phoneTv);
        coverTv=findViewById(R.id.coverTv);
        firebaseAuth =FirebaseAuth.getInstance();

        //get uis od clicked user to retrieve his posts
        Intent intent=getIntent();
        uid=intent.getStringExtra("uid");

        Query query=FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //check until get the data
                for(DataSnapshot ds:snapshot.getChildren()){
                    //get data
                    String name=""+ds.child("name").getValue();
                    String email=""+ds.child("email").getValue();
                    String phone=""+ds.child("phone").getValue();
                    String image=""+ds.child("image").getValue();
                    String cover=""+ds.child("cover").getValue();

                    //set
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    try{

                        //if image received set
                        Picasso.get().load(image).into(avatarTv);
                    }catch(Exception e){
                        //if there is any exception while getting image then set default
                        Picasso.get().load(R.drawable.ic_face_yellow).into(avatarTv);
                    }

                    try{

                        //if image received set
                        Picasso.get().load(cover).into(coverTv);
                    }catch(Exception e){
                        //if there is any exception while getting image then set default
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        postList= new ArrayList<>();

        checkUserStatus();
        loadHistPosts();
    }

    private void loadHistPosts() {

        //linear layout for recyclerview
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this);
        //show newest post first,for this load from last
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        //set this layout to recycler view
        postsRecyclerView.setLayoutManager(linearLayoutManager);
        //init posts list
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        //query to load posts
        Query query=ref.orderByChild("uid").equalTo(uid);
        //get all data from ref

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();

                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelPost myPosts=ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(myPosts);

                    //adpter
                    adapterPosts= new AdapterPosts(ThereProfileActivity.this,postList);
                    //set this adapter to recycler
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+error.getMessage() , Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void searchHistPosts(final String searchQuery){
        //linear layout for recyclerview
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(ThereProfileActivity.this);
        //show newest post first,for this load from last
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        //set this layout to recycler view
        postsRecyclerView.setLayoutManager(linearLayoutManager);
        //init posts list
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        //query to load posts
        Query query=ref.orderByChild("uid").equalTo(uid);
        //get all data from ref

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();

                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelPost myPosts=ds.getValue(ModelPost.class);


                    if(myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
                        //add to list
                        postList.add(myPosts);
                    }


                    //adpter
                    adapterPosts= new AdapterPosts(ThereProfileActivity.this,postList);
                    //set this adapter to recycler
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void checkUserStatus(){
        //get current user

        FirebaseUser user=firebaseAuth.getCurrentUser();

        if(user!=null){
            //user is signed in stay here
            //profileTv.setText(user.getEmail());



        }else{
            //user is not signed in go to main activity
            startActivity(new Intent(this,MainActivity.class));
           finish();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflating menu
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);

        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_add_goupInfo).setVisible(false);

        MenuItem item=menu.findItem(R.id.action_search);
        //searchview to specific posts
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when you press the search button

                if(!TextUtils.isEmpty(s)){
                    searchHistPosts(s);
                }else{
                    loadHistPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called wen the user presses any letter
                if(!TextUtils.isEmpty(s)){
                    searchHistPosts(s);
                }else{
                    loadHistPosts();
                }
                return false;
            }
        });
       return  super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        //get id
        int id=item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();

        }




        return super.onOptionsItemSelected(item);
    }
}