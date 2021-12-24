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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jether.nrmtuwaaye.Adapters.AdapterUsers;
import com.jether.nrmtuwaaye.AddPostActivity;
import com.jether.nrmtuwaaye.CreateGroupActivity;
import com.jether.nrmtuwaaye.MainActivity;
import com.jether.nrmtuwaaye.Models.ModelUser;
import com.jether.nrmtuwaaye.R;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUser> userList;

    private FirebaseAuth firebaseAuth;

    public UsersFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_users, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        //init recycler
        recyclerView=view.findViewById(R.id.users_recyclerview);

        //set its properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        //init user list
        userList= new ArrayList<>();


        //get all user
        getAllUsers();


        return view;


    }

    private void getAllUsers() {

        final FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        //get the path of data base named "users containing user info
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for(DataSnapshot ds: snapshot.getChildren()){

                    ModelUser modelUser= ds.getValue(ModelUser.class);

                    //get all users except currentily signed in user
                     if(!firebaseAuth.getUid().equals(modelUser.getUid())){
                        userList.add(modelUser);

                    }


                }


                //adapter
                adapterUsers=new AdapterUsers(getActivity(),userList);
                //set adapter to recycler
                recyclerView.setAdapter(adapterUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchUsers(final String query) {
        final FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        //get the path of data base named "users containing user info
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for(DataSnapshot ds: snapshot.getChildren()){

                    ModelUser modelUser= ds.getValue(ModelUser.class);

                    //conditions to fulfill search
                    //1.User not current user
                    //2.The user name or email entered contains text in search view (case insesitive)

                    //get all  searched users except currentily signed in user
                    if(!firebaseAuth.getUid().equals(modelUser.getUid())){

                        if(modelUser.getName() != null && modelUser.getName().contains(query.toLowerCase()) ||
                                modelUser.getEmail() != null && modelUser.getEmail().contains(query.toLowerCase())) {
                            userList.add(modelUser);
                        }


                    }


                }

                //adapter
                adapterUsers=new AdapterUsers(getActivity(),userList);
                //refresh adapter
                adapterUsers.notifyDataSetChanged();
                //set adapter to recycler
                recyclerView.setAdapter(adapterUsers);
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

        //hide add post icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_add_goupInfo).setVisible(false);

        MenuItem menuItem=menu.findItem(R.id.action_search);

        SearchView searchView=(SearchView) MenuItemCompat.getActionView(menuItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when the user presses  search button from keyboard
                //if search query is not empty is not empty search

                if(!TextUtils.isEmpty(query.trim())){
                    //search text contains text,search it
                    searchUsers(query);

                }else{
                    //search text empty,get all users
                    getAllUsers();

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                //called wenever user prssess a single letter
                if(!TextUtils.isEmpty(query.trim())){
                    //search text contains text,search it
                    searchUsers(query);

                }else{
                    //search text empty,get all users
                    getAllUsers();

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

        }else if(id==R.id.action_create_group){
            startActivity(new Intent(getActivity(), CreateGroupActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }


}