package com.jether.nrmtuwaaye.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jether.nrmtuwaaye.ChatActivity;
import com.jether.nrmtuwaaye.Models.ModelUser;
import com.jether.nrmtuwaaye.R;
import com.jether.nrmtuwaaye.ThereProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    Context context;

    List<ModelUser> userList;
    //getting the current user id
    FirebaseAuth firebaseAuth;
    String myUid;

    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;

        firebaseAuth=FirebaseAuth.getInstance();
        myUid=firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_users,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        //get data
        final String hisUID=userList.get(position).getUid();
        String userImage=userList.get(position).getImage();
        String userName=userList.get(position).getName();
        final String userEmail=userList.get(position).getEmail();


        //set data
        holder.mNameTv.setText(userName);
        holder.mEmailTv.setText(userEmail);

        try{
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_face_yellow)
                    .into(holder.mAvatarTv);
        }catch(Exception e){

        }

        holder.blockTv.setImageResource(R.drawable.ic_unblocked_u);
        //check if user is blocked or not
        checkIsBlocked(hisUID,holder,position);


        //handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //show dialog

                AlertDialog.Builder builder= new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if(i==0){
                            //profile clicked
                             /*Click to go to There Profile Activity with uid,this uid is of clicked user
                                which will be used to show user specific data/posts/
                                * */
                            Intent intent= new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uid",hisUID);
                            context.startActivity(intent);

                        }



                        if(i==1){

                            //chat clicked

                            /*Click user from user list to start chatting
                             *start activity by putting uid of the receiver
                             *we will get that UID to the identity the user we are gonna chat
                             * */
                            isBlockedOrNot(hisUID);

                        }

                    }
                });
                builder.create().show();

            }
        });


        holder.blockTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userList.get(position).isBlocked()){
                    unBlockUser(hisUID);

                }else{

                    blockUser(hisUID);

                }

            }
        });


    }


    private void isBlockedOrNot(final String hisUID){
        //first check if sender(current user) is blocked by receiver or not
        //(LOgic) if uid of the sender(current user) exits in BlockedUsers of receiver then sender (current user) is blocked otherwise not
        //if blocked then just display a message.g you are blocked by user,cant send message
        //if not blocked simpliy start the chart activity
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            if(ds.exists()){
                                Toast.makeText(context, "You are blocked by other user cant send message....", Toast.LENGTH_SHORT).show();
                                //blocked dont proceed further

                                return;
                            }
                        }
                        //not blocked start activity
                        Intent intent= new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid",hisUID);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsBlocked(String hisUID, final MyHolder holder, final int position) {
        //check each user if blocked or not
        //if uid of blocked user exists in Blocked users node then the user is blocked otherwise not

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            if(ds.exists()){
                                holder.blockTv.setImageResource(R.drawable.ic_block);
                                userList.get(position).setBlocked(true);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser(String hisUID) {
        //block user by adding  uid to currents users blockUsers node.

        //put values in hashmap to put in db
        HashMap<String,String> hashMap= new HashMap<>();
        hashMap.put("uid",hisUID);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //blocked
                        Toast.makeText(context, "Blocked Successfully....", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed.."+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void unBlockUser(String hisUID) {
        //unblock user by removing  uid from currents users blockUsers node.

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            if(ds.exists()){
                                //remove blocked user from current user Blocked List
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //unblocked
                                                Toast.makeText(context, "Unblocked successfully..", Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to unblock
                                                Toast.makeText(context, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        ImageView mAvatarTv,blockTv;
        TextView mNameTv,mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //iniviews
            mAvatarTv=itemView.findViewById(R.id.avatarTv1);
            mNameTv=itemView.findViewById(R.id.nameTv1);
            mEmailTv=itemView.findViewById(R.id.emailTv1);
            blockTv=itemView.findViewById(R.id.blockTv);
        }
    }
}
