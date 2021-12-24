package com.jether.nrmtuwaaye;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jether.nrmtuwaaye.Adapters.AdapterAddParticipants;
import com.jether.nrmtuwaaye.Models.ModelUser;

import java.util.ArrayList;

public class GroupAddParticipantsActivity extends AppCompatActivity {


    private RecyclerView rcv;

    private ActionBar actionBar;

    private FirebaseAuth firebaseAuth;

    private String groupId;
    private String myGroupRole;

    private ArrayList<ModelUser> userList;
    private AdapterAddParticipants adapterAddParticipants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add_participants);


        actionBar=getSupportActionBar();
        actionBar.setTitle("Add Participants");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        firebaseAuth=FirebaseAuth.getInstance();

        rcv=findViewById(R.id.rcv);

        groupId=getIntent().getStringExtra("groupId");
        loadGroupInfo();


    }

    private void getAllUsers() {
        userList= new ArrayList<>();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelUser modelUser=ds.getValue(ModelUser.class);


                    //get all users accept currently signed in
                    if(!firebaseAuth.getUid().equals(modelUser.getUid())){
                        //not my uid

                    userList.add(modelUser);
                    }

                }
                //setup adapter
                adapterAddParticipants= new AdapterAddParticipants(GroupAddParticipantsActivity.this,userList,""+groupId,""+myGroupRole);
                rcv.setAdapter(adapterAddParticipants);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void searchAllUsers(final String query) {
        userList= new ArrayList<>();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelUser modelUser=ds.getValue(ModelUser.class);


                    //get all users accept currently signed in
                    if(!firebaseAuth.getUid().equals(modelUser.getUid())){

                        //not my uid
                        if(modelUser.getName() != null && modelUser.getName().contains(query.toLowerCase()) ||
                                modelUser.getEmail() != null && modelUser.getEmail().contains(query.toLowerCase()))  {
                            userList.add(modelUser);
                        }


                    }

                }
                //setup adapter
                adapterAddParticipants= new AdapterAddParticipants(GroupAddParticipantsActivity.this,userList,""+groupId,""+myGroupRole);
                rcv.setAdapter(adapterAddParticipants);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadGroupInfo() {

        final DatabaseReference ref1= FirebaseDatabase.getInstance().getReference("Groups");


        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    String groupId=""+ds.child("groupId").getValue();
                    final String groupTitle=""+ds.child("groupTitle").getValue();
                    String groupDescription=""+ds.child("groupDescription").getValue();
                    String groupIcon=""+ds.child("groupIcon").getValue();
                    String createdBy=""+ds.child("createdBy").getValue();
                    String timestamp=""+ds.child("timestamp").getValue();


                    ref1.child(groupId).child("Participants").child(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        myGroupRole=""+snapshot.child("role").getValue();
                                        actionBar.setTitle(groupTitle+"{"+ myGroupRole+"}");

                                        getAllUsers();

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


    private void checkUserStatus(){
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            //user is signed in


        }else{
            //user not signed in
            startActivity(new Intent(this,MainActivity.class));

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
            public boolean onQueryTextSubmit(String query) {
                //called when the user presses  search button from keyboard
                //if search query is not empty is not empty search

                if(!TextUtils.isEmpty(query.trim())){
                    //search text contains text,search it
                    searchAllUsers(query);

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
                    searchAllUsers(query);

                }else{
                    //search text empty,get all users
                    getAllUsers();

                }
                return false;
            }


        });
        return  super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get id
        int id=item.getItemId();
        if(id==R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();

        }
        return super.onOptionsItemSelected(item);
    }
   

}