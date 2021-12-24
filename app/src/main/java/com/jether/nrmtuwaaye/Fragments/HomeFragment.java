package com.jether.nrmtuwaaye.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jether.nrmtuwaaye.Adapters.AdapterPosts;
import com.jether.nrmtuwaaye.AddPostActivity;
import com.jether.nrmtuwaaye.MainActivity;
import com.jether.nrmtuwaaye.Models.ModelPost;
import com.jether.nrmtuwaaye.R;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private FirebaseAuth firebaseAuth;

    RecyclerView postRecycler;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;

    public HomeFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);
        firebaseAuth = FirebaseAuth.getInstance();


        postRecycler=view.findViewById(R.id.postRecycler);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getActivity());

        //show newest post first,for load load from last
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        //set layout to recycler
        postRecycler.setLayoutManager(linearLayoutManager);

        //init post list
        postList= new ArrayList<>();


        loadPosts();

        return view;
    }

    private void loadPosts() {
        //path of all posts
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        //get all data from ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();

                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);

                    postList.add(modelPost);

                    //adapter
                    adapterPosts= new AdapterPosts(getActivity(),postList);
                    //set adapter to recyclerview

                    postRecycler.setAdapter(adapterPosts);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });
    }

    private void searchPosts(final String searchQuery){
        //path of all posts
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        //get all data from ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();

                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);



                    if(modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(modelPost);
                    }

                    //adapter
                    adapterPosts= new AdapterPosts(getActivity(),postList);
                    //set adapter to recyclerview

                    postRecycler.setAdapter(adapterPosts);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


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
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();

        }
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

        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_add_goupInfo).setVisible(false);

        MenuItem menuItem=menu.findItem(R.id.action_search);
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(menuItem);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called wen user presses search button
                if(!TextUtils.isEmpty(s)){
                    searchPosts(s);
                }else{
                    loadPosts();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // called as and when a user presses any letter

                if(!TextUtils.isEmpty(s)){
                    searchPosts(s);
                }else{
                    loadPosts();
                }

                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        //get id
        int id=item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();

        }

        if(id==R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }

}