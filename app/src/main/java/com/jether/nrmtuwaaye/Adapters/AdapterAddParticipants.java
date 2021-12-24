package com.jether.nrmtuwaaye.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jether.nrmtuwaaye.Models.ModelUser;
import com.jether.nrmtuwaaye.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterAddParticipants extends RecyclerView.Adapter<AdapterAddParticipants.MyHolder> {


    private Context context;
    private ArrayList<ModelUser> userList;
    private String groupId,myGroupRole;//creator /admin

    public AdapterAddParticipants(Context context, ArrayList<ModelUser> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_add_participants,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        final ModelUser modelUser=userList.get(position);
        //gete data
        String name=modelUser.getName();
        String email=modelUser.getEmail();
        String image=modelUser.getImage();
        final String uid=modelUser.getUid();

        //set data
        holder.nameTv1.setText(name);
        holder.emailTv1.setText(email);

        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_face_yellow).into(holder.avatarTv1);

        }catch(Exception e){
            holder.avatarTv1.setImageResource(R.drawable.ic_face_yellow);

        }

        checkIfAlreadyExists(modelUser,holder);

        //handle click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Check if the user is already added or not
                if added show remove participant/make admin/remove-admin option(admin will not be able to change the role of the creator
                if not added show add participant option

                * */

                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    //user exists/participant
                                    String hisPreviousRole=""+snapshot.child("role").getValue();

                                    //options to displayin dialog
                                    String[] options;

                                    AlertDialog.Builder builder= new AlertDialog.Builder(context);
                                    builder.setTitle("Choose Options");
                                    if(myGroupRole.equals("creator")){
                                        if(hisPreviousRole.equals("admin")){
                                            //in creator,he is admin
                                            options= new String[] {"Remove Admin","Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //handle item clicks
                                                    if(i==0){
                                                        //remove admin clicked
                                                        removeAdmin(modelUser);

                                                    }else{
                                                        //remove user clicked
                                                        removeParticipant(modelUser);

                                                    }

                                                }
                                            }).show();

                                        }
                                        else if(hisPreviousRole.equals("participant")){
                                            //if creator is participant
                                            options= new String[] {"Make Admin","Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //handle item clicks
                                                    if(i==0){
                                                        //remove admin clicked
                                                        makeAdmin(modelUser);

                                                    }else{
                                                        //remove user clicked
                                                        removeParticipant(modelUser);

                                                    }

                                                }
                                            }).show();


                                        }


                                    }
                                    else if(myGroupRole.equals("admin")){
                                        if (hisPreviousRole.equals("creator")){
                                            //in admin,he is creator
                                            Toast.makeText(context, "Creator of group....", Toast.LENGTH_SHORT).show();
                                        }
                                        else if(hisPreviousRole.equals("admin")){
                                            //in admin, he is admin too
                                            options= new String[] {"Remove Admin","Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //handle item clicks
                                                    if(i==0){
                                                        //remove admin clicked
                                                        removeAdmin(modelUser);

                                                    }else{
                                                        //remove user clicked
                                                        removeParticipant(modelUser);

                                                    }

                                                }
                                            }).show();
                                        }
                                        else if(hisPreviousRole.equals("participant")){
                                            //in admin he si participant
                                            options=new String[]{"Make Admin","Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //handle item clicks
                                                    if(i==0){
                                                        //remove admin clicked
                                                        removeAdmin(modelUser);

                                                    }else{
                                                        //remove user clicked
                                                        removeParticipant(modelUser);

                                                    }

                                                }
                                            }).show();
                                        }


                                    }




                                }else{
                                    //user doesnt exist not participant:add
                                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participant")
                                            .setMessage("Add this user in this group!..")
                                            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //add user
                                                    addParticipant(modelUser);

                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();

                                                }
                                            }).show();


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

    }

    private void addParticipant(ModelUser modelUser) {
        //set user data
        String timestamp=""+System.currentTimeMillis();
        HashMap<String,String> hashMap= new HashMap<>();
        hashMap.put("uid",modelUser.getUid());
        hashMap.put("role","participant");
        hashMap.put("timestamp",""+timestamp);

        //add that user in the Grouping groupId Participants

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                      //added succesfully
                        Toast.makeText(context, "Added Successfully..", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to be added

                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }

    private void makeAdmin(ModelUser modelUser) {
        //set up data


        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("role","admin");//roles are participant/admin/creator

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //made admin
                        Toast.makeText(context, "User is now admin", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void removeParticipant(ModelUser modelUser) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //removeed successfully

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed

                    }
                });
    }

    private void removeAdmin(ModelUser modelUser) {
        //set up data


        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("role","participant");//just change role

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //made admin
                        Toast.makeText(context, "User is no longer admin now....", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void checkIfAlreadyExists(ModelUser modelUser, final MyHolder holder) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            //already exists

                            String hisRole=""+snapshot.child("role").getValue();
                            holder.statusTv.setText(hisRole);

                        }else{
                            //doesnt exist
                            holder.statusTv.setText("");
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

    class MyHolder extends RecyclerView.ViewHolder{

        private ImageView avatarTv1;
        private TextView nameTv1,emailTv1,statusTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatarTv1=itemView.findViewById(R.id.avatarTv1);
            nameTv1=itemView.findViewById(R.id.nameTv1);
            emailTv1=itemView.findViewById(R.id.emailTv1);
            statusTv=itemView.findViewById(R.id.statusTv);

        }
    }
}
