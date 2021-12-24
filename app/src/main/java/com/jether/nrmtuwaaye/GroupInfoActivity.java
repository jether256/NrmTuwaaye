package com.jether.nrmtuwaaye;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jether.nrmtuwaaye.Adapters.AdapterAddParticipants;
import com.jether.nrmtuwaaye.Models.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {

    private String groupId;
    private String myGroupRole="";
    private ActionBar actionBar;

    private ImageView groupIconTv;
    private TextView decs,createdByTv,editGroup,addPart,leaveGroup,participants;
    private RecyclerView addRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelUser> userList;

    private AdapterAddParticipants addParticipants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        actionBar=getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        groupIconTv=findViewById(R.id.groupIconTv);
        decs=findViewById(R.id.decs);
        createdByTv=findViewById(R.id.createdByTv);
        editGroup=findViewById(R.id.editGroup);
        addPart=findViewById(R.id.addPart);
        leaveGroup=findViewById(R.id.leaveGroup);
        participants=findViewById(R.id.participants);
        addRv=findViewById(R.id.addRv);



        groupId=getIntent().getStringExtra("groupId");
        firebaseAuth= FirebaseAuth.getInstance();

        loadGroupInfo();
        loadMyGroupRole();

        addPart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(GroupInfoActivity.this,GroupAddParticipantsActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });

        leaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if user is admin/user leave group
                //if user is creator of the group delete group
                String dialogTitle="";
                String dialogDescription="";
                String positiveButtonTitle="";

                if (myGroupRole.equals("creator")){
                    dialogTitle="Delete Group";
                    dialogDescription="Are you sure you want to delete Group permanently?..";
                    positiveButtonTitle="DELETE";

                }else{
                    dialogTitle="Leave Group";
                    dialogDescription="Are you sure you want to leave Group permanently?..";
                    positiveButtonTitle="LEAVE";
                }

                AlertDialog.Builder builder= new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(myGroupRole.equals("creator")){
                                    //in creator of group delete group
                                    deleteGroup();

                                }else{
                                    //in admnin leave group
                                    leavegroup();

                                }

                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();

                            }
                        }).show();
            }
        });

        editGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(GroupInfoActivity.this,GroupEditActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });
    }

    private void leavegroup() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupInfoActivity.this, "Group left successfully....", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this,DashBoardActivity.class));
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void deleteGroup() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupInfoActivity.this, "Group deleted.....", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this,DashBoardActivity.class));
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loadMyGroupRole() {

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid")
                .equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            myGroupRole=""+ds.child("role").getValue();
                            actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail()+"{"+myGroupRole+"}");

                            if(myGroupRole.equals("participant")){
                                editGroup.setVisibility(View.GONE);
                                addPart.setVisibility(View.GONE);
                                leaveGroup.setText("leave Group");

                            }
                            else if(myGroupRole.equals("admin")){
                                editGroup.setVisibility(View.GONE);
                                addPart.setVisibility(View.VISIBLE);
                                leaveGroup.setText("leave Group");


                            }
                            else if(myGroupRole.equals("creator")){
                                editGroup.setVisibility(View.VISIBLE);
                                addPart.setVisibility(View.VISIBLE);
                                leaveGroup.setText("Delete Group");

                            }

                        }

                        loadParticipants();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadParticipants() {
        userList= new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
               for(DataSnapshot ds:snapshot.getChildren()){
                   //get uid from Group>Participants
                   String uid=""+ds.child("uid").getValue();

                   //get info of the user using uid we got above
                   DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
                   ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot snapshot) {
                           for(DataSnapshot ds:snapshot.getChildren()){
                               ModelUser modelUser=ds.getValue(ModelUser.class);
                               userList.add(modelUser);
                           }
                           //adapter
                           addParticipants= new AdapterAddParticipants(GroupInfoActivity.this,userList,groupId,myGroupRole);
                           addRv.setAdapter(addParticipants);
                           participants.setText("Participants{"+userList.size()+"}");


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

    private void loadGroupInfo() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    String groupId=""+ds.child("groupId").getValue();
                    String groupTitle=""+ds.child("groupTitle").getValue();
                    String groupDescription=""+ds.child("groupDescription").getValue();
                    String groupIcon=""+ds.child("groupIcon").getValue();
                    String createdBy=""+ds.child("createdBy").getValue();
                    String timestamp=""+ds.child("timestamp").getValue();


                    //convert the time stamp to dd/MM/yyyy hh:mm am/pm

                    Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(timestamp));

                    String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

                    loadCreatorInfo(dateTime,createdBy);

                    //set groupInfo
                    actionBar.setTitle(groupTitle);
                    decs.setText(groupDescription);

                    try{
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_gulupu).into(groupIconTv);

                    }catch(Exception e){
                        groupIconTv.setImageResource(R.drawable.ic_gulupu);

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        
    }

    private void loadCreatorInfo(final String dateTime, final String createdBy) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){

                    String name=""+ds.child("name").getValue();
                    createdByTv.setText("Created by"+name+"on "+dateTime);
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
}