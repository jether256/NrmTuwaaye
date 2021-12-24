package com.jether.nrmtuwaaye.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
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
import com.jether.nrmtuwaaye.Adapters.AdapterGroupChatList;
import com.jether.nrmtuwaaye.AddPostActivity;
import com.jether.nrmtuwaaye.CreateGroupActivity;
import com.jether.nrmtuwaaye.MainActivity;
import com.jether.nrmtuwaaye.Models.ModelChatList;
import com.jether.nrmtuwaaye.Models.ModelGroupChatList;
import com.jether.nrmtuwaaye.R;

import java.util.ArrayList;

public class GroupChatFragment extends Fragment {

    private RecyclerView groupRv;
    private FirebaseAuth firebaseAuth;


    private ArrayList<ModelGroupChatList>  groupChatLists;
    private AdapterGroupChatList adapterGroupChatList;



    public GroupChatFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         View view=inflater.inflate(R.layout.fragment_group_chat, container, false);


         groupRv=view.findViewById(R.id.groupRecy);

         firebaseAuth=FirebaseAuth.getInstance();

         loadGroupChatList();

        return view;
    }

    private void loadGroupChatList() {

        groupChatLists= new ArrayList<>();


        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(ds.child("Participants").child(firebaseAuth.getUid()).exists()) {
                        ModelGroupChatList model = ds.getValue(ModelGroupChatList.class);
                        groupChatLists.add(model);

                    }
                }
                adapterGroupChatList= new AdapterGroupChatList(getActivity(),groupChatLists);
                groupRv.setAdapter(adapterGroupChatList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void SearchGroupChatList(final String query) {

        groupChatLists= new ArrayList<>();


        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.size();
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        //search by group title
                        if(ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())) {
                            ModelGroupChatList model = ds.getValue(ModelGroupChatList.class);
                            groupChatLists.add(model);
                        }

                    }

                }
                adapterGroupChatList= new AdapterGroupChatList(getActivity(),groupChatLists);
                groupRv.setAdapter(adapterGroupChatList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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


        menu.findItem(R.id.action_add_post).setVisible(false);
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
                    SearchGroupChatList(s);
                }else{
                    loadGroupChatList();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // called as and when a user presses any letter

                if(!TextUtils.isEmpty(s)){
                    SearchGroupChatList(s);
                }else{
                    loadGroupChatList();
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

        if(id==R.id.action_create_group){
            startActivity(new Intent(getActivity(), CreateGroupActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
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
}