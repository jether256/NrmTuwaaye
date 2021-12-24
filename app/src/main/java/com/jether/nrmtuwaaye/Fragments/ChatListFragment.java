package com.jether.nrmtuwaaye.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jether.nrmtuwaaye.Adapters.AdapterChatList;
import com.jether.nrmtuwaaye.AddPostActivity;
import com.jether.nrmtuwaaye.CreateGroupActivity;
import com.jether.nrmtuwaaye.MainActivity;
import com.jether.nrmtuwaaye.Models.ModelChat;
import com.jether.nrmtuwaaye.Models.ModelChatList;
import com.jether.nrmtuwaaye.Models.ModelUser;
import com.jether.nrmtuwaaye.R;

import java.util.ArrayList;
import java.util.List;


public class ChatListFragment extends Fragment {

    private FirebaseAuth firebaseAuth;

    RecyclerView recykle;
    List<ModelChatList> chatlistLists;
    List<ModelUser> userList;
    DatabaseReference databaseReference;
    DatabaseReference reference;
    FirebaseUser currentUser;

    AdapterChatList adapterChatList;

    public ChatListFragment() {
        // Required empty public constructor
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_chat_list, container, false);
        firebaseAuth = FirebaseAuth.getInstance();

        currentUser=FirebaseAuth.getInstance().getCurrentUser();

        recykle=view.findViewById(R.id.recykle);
         chatlistLists= new ArrayList<>();



         databaseReference= FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
         databaseReference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 chatlistLists.clear();
                 for(DataSnapshot ds:snapshot.getChildren()){

                     ModelChatList chatList=ds.getValue(ModelChatList.class);
                     chatlistLists.add(chatList);

                 }

                 loadChats();
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });




        return view;
    }

    private void loadChats() {
        userList= new ArrayList<>();

        reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelUser user =ds.getValue(ModelUser.class);
                    for(ModelChatList chatList:chatlistLists){
                        if(user.getUid() !=null && user.getUid().equals(chatList.getId())){
                            userList.add(user);
                            break;
                        }

                    }
                    //adapter

                    adapterChatList= new AdapterChatList(getContext(),userList);
                    //setadpater
                    recykle.setAdapter(adapterChatList);
                    //set last message
                    for(int i=0; i<userList.size(); i++ ){
                        lastMessage(userList.get(i).getUid());

                    }


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void lastMessage(final String userId) {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String theLastMessage="default";

                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelChat chat=ds.getValue(ModelChat.class);
                   if(chat==null){
                        continue;
                   }

                   String sender=chat.getSender();
                   String receiver=chat.getReceiver();

                   if(sender==null || receiver==null){
                        continue;
                   }

                    if(chat.getReceiver().equals(currentUser.getUid()) &&
                        chat.getSender().equals(userId) ||
                    chat.getReceiver().equals(userId) &&
                    chat.getSender().equals(currentUser.getUid())){
                        // instead of showing url in message show photo
                        if (chat.getType().equals("image")){
                            theLastMessage="Sent a photo";

                        }else{
                            theLastMessage=chat.getMessage();
                        }


                    }

                }
                adapterChatList.setLastMessageMap(userId,theLastMessage);
                adapterChatList.notifyDataSetChanged();


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

        else if(id==R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));

        }
        else if(id==R.id.action_create_group){
            startActivity(new Intent(getActivity(), CreateGroupActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }
}