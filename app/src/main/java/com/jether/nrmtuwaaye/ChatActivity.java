package com.jether.nrmtuwaaye;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jether.nrmtuwaaye.Adapters.AdapterChat;
import com.jether.nrmtuwaaye.Models.ModelChat;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    ImageButton sendBtn,attachBtn;
    EditText messageEEt;
    TextView nameTV,statusTV;
    ImageView profileTv,blockTv;
    Toolbar toolbar;
    RecyclerView recyclerView;
    private FirebaseAuth firebaseAuth;

    String hisUid;
    String myUid;
    String hisImage;

    boolean isBlocked=false;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference ref;

    //check if user has seen the message or not
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    //permission constants
    private static final int CAMERA_REQUEST_CODE=200;
    private static final int STORAGE_REQUEST_CODE=300;

    //permission arrays
    private String [] cameraPermissions;
    private String [] storagePermissions;


    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE=400;
    private static final int IMAGE_PICK_CAMERA_CODE=500;

    //image picked Uri
    private Uri image_rui= null;

    private boolean notify=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar=findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        recyclerView=findViewById(R.id.chat_recycler);
        profileTv=findViewById(R.id.profileTv);
        nameTV =findViewById(R.id.userNameTv);
        statusTV=findViewById(R.id.status);
        messageEEt=findViewById(R.id.messageEt);
        sendBtn=findViewById(R.id.sendBtn);
        attachBtn=findViewById(R.id.attachBtn);
        blockTv=findViewById(R.id.blockTv);


        cameraPermissions= new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        //layout for recycler
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this);
        linearLayoutManager.getStackFromEnd();

        //reccycler properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);



        /*On clicking  user from users list we have passed  that user,s UId using intent;

         *So get the uid here to get Profile Picture, name and start caht with that person


         * */

        Intent intent= getIntent();
        hisUid=intent.getStringExtra("hisUid");

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase=FirebaseDatabase.getInstance();
        ref=firebaseDatabase.getReference("Users");

        //serach user to get users info
        Query userQuery=  ref.orderByChild("uid").equalTo(hisUid);
        //get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //check until required info is received
                for(DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    String name =""+ds.child("name").getValue();
                    hisImage =""+ds.child("image").getValue();
                    String typingStatus =""+ds.child("typingTo").getValue();

                    if(typingStatus.equals(myUid)){

                        statusTV.setText("typing....");
                    }else{

                        String onlineStatus =""+ds.child("onlineStatus").getValue();

                        if(onlineStatus.equals("online")){
                            statusTV.setText(onlineStatus);

                        }else{
                            Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));

                            String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
                            statusTV.setText("Last seen at:"+dateTime);

                        }
                    }

                    //set data
                    nameTV.setText(name);

                    try{
                        //image received set it to image view in toolbar
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_face_white).into(profileTv);

                    }catch(Exception e){
                        //exeception getting picture set defalut
                        Picasso.get().load(R.drawable.ic_face_white).into(profileTv);

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //click button to import
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();

            }
        });


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String message=messageEEt.getText().toString().trim();

                //check if text is empty
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(ChatActivity.this,"Cannot send empty Message.....",Toast.LENGTH_SHORT).show();
                }else{
                    sendMessage(message);
                }


            }
        });

        messageEEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0){
                    checkTypingStatus("noOne");
                }else{
                    checkTypingStatus(hisUid);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        blockTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBlocked){
                    unBlockUser();

                }else{

                    blockUser();

                }
            }
        });


        readMessages();

        checkIsBlocked();

        seenMessages();
    }



    private void checkIsBlocked() {
        //check each user if blocked or not
        //if uid of blocked user exists in Blocked users node then the user is blocked otherwise not

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            if(ds.exists()){
                               blockTv.setImageResource(R.drawable.ic_block);
                                isBlocked=true;
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser() {
        //block user by adding  uid to currents users blockUsers node.

        //put values in hashmap to put in db
        HashMap<String,String> hashMap= new HashMap<>();
        hashMap.put("uid",hisUid);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //blocked
                        Toast.makeText(ChatActivity.this, "Blocked Successfully....", Toast.LENGTH_SHORT).show();
                        blockTv.setImageResource(R.drawable.ic_block);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, "Failed.."+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void unBlockUser() {
        //unblock user by removing  uid from currents users blockUsers node.

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
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
                                                Toast.makeText(ChatActivity.this, "Unblocked successfully..", Toast.LENGTH_SHORT).show();
                                                blockTv.setImageResource(R.drawable.ic_unblocked_u);

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to unblock
                                                Toast.makeText(ChatActivity.this, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();

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


    private void showImagePickDialog(){
        //options(camera,gallery ) to show

        String [] options={"Camera","Gallery"};

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Choose Image From");
        //set options to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(i==0){
                    //camera clicked

                    if(!checkCameraPermissions()){
                        requestCameraPermissions();
                    }else{
                        pickFromCamera();
                    }
                }

                if(i==1){
                    //gallery clicked
                    if(!checkStoragePermissions()){
                        requestStoragePermissions();
                    }else{
                        pickFromGallery();
                    }

                }

            }
        });
        builder.create().show();
    }

    private void pickFromGallery() {
        //intent to pick image from gallery
        Intent intent= new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //intent to pick image from camera

        //using mediastore to pick high quality image
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Pik");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Descr");

        image_rui=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_rui);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermissions(){
        //check if storage permission is enabled or not
        //return true if enable
        //return  false if not enabled
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result;

    }

    private void requestStoragePermissions(){
        //request runtime storage permission
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions(){
        // check if camera permission is enabled
        //return true if enabled
        //return false if not
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==
                (PackageManager.PERMISSION_GRANTED);

        boolean result2= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);

        return result && result2;//return true of false

    }

    private void requestCameraPermissions(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }

    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this method is called when the user press allow or deny from permission reqeust dialog
        //here we will handle permission cases(allowed&denied)
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                //picking camera firts check if the camera and storage permissions are allowed or not
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //permission allowed
                        pickFromCamera();
                    } else {

                        //permission denied

                        Toast.makeText(this, "Camera and storage permissions...required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                //picking camera firts check if the camera and storage permissions are allowed or not
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //permission allowed
                        pickFromGallery();
                    } else {

                        //permission denied

                        Toast.makeText(this, "Storage permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                //image is picked from the gallery get the image rui
                image_rui=data.getData();
                //use this image uri to upload to database
                try {
                    sendImageMessage(image_rui);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            else if(requestCode==IMAGE_PICK_CAMERA_CODE){
                //image picked from camera ,get uri of image
                try {
                    sendImageMessage(image_rui);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageMessage(Uri image_rui) throws IOException {


        final ProgressDialog progressDialog= new ProgressDialog(this);
        progressDialog.setMessage("Sending image....");
        progressDialog.show();

        final String timestamp=""+System.currentTimeMillis();

        String fileNameAndPath="ChatImages/"+"post_"+timestamp;

        /*chats node will be created that will contain all images sent via chat

         */
        //get bitmap from image uri

       Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),image_rui);

        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data=baos.toByteArray();//convert image to bytes

        StorageReference ref= FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded
                        progressDialog.dismiss();

                        Task<Uri> task= taskSnapshot.getStorage().getDownloadUrl();
                        while(!task.isSuccessful());

                        String downloadUri=task.getResult().toString();

                        if(task.isSuccessful()){
                            //add iamge uri and other info to database

                            DatabaseReference refs=FirebaseDatabase.getInstance().getReference();

                            //set data required
                            HashMap<String,Object> hashMap= new HashMap<>();
                            hashMap.put("sender",myUid);
                            hashMap.put("receiver",hisUid);
                            hashMap.put("message",downloadUri);
                            hashMap.put("timestamp",timestamp);
                            hashMap.put("type","image");
                            hashMap.put("isSeen",false);

                            //put to firebase
                            refs.child("Chats").push().setValue(hashMap);

                            //create chatList node/child in firebaseDatabase

                            final DatabaseReference Chatref1=FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(myUid)
                                    .child(hisUid);
                            Chatref1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(!snapshot.exists()){
                                        Chatref1.child("id").setValue(hisUid);

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            final DatabaseReference Chatref2=FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(hisUid)
                                    .child(myUid);
                            Chatref2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(!snapshot.exists()){
                                        Chatref2.child("id").setValue(myUid);

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        progressDialog.dismiss();

                    }
                });

    }


    private void seenMessages() {
        userRefForSeen=FirebaseDatabase.getInstance().getReference("Chats");
        seenListener=userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelChat chat=ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)){
                        HashMap<String,Object> hasSeenHashMap= new HashMap<>();
                        hasSeenHashMap.put("isSeen",true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        chatList= new ArrayList<>();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelChat chat= ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }

                    adapterChat= new AdapterChat(ChatActivity.this,chatList,hisImage);
                    adapterChat.notifyDataSetChanged();

                    recyclerView.setAdapter(adapterChat);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void sendMessage(final String message) {
        /*Chats node will be created that contains all chats
         *Whenever user sends message it will create a new child in chats node and it will contain
         *sender:UID of sender
         *receiver:UID of receiver
         * message:actual message*/

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();

        String timestamp= String.valueOf(System.currentTimeMillis());

        HashMap<String,Object> hashMap= new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isSeen",false);
        hashMap.put("type","text");

        databaseReference.child("Chats").push().setValue(hashMap);


        //reset Edit text after sending
        messageEEt.setText("");

        //create chatList node/child in firebaseDatabase

        final DatabaseReference Chatref1=FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(myUid)
                .child(hisUid);
        Chatref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    Chatref1.child("id").setValue(hisUid);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference Chatref2=FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisUid)
                .child(myUid);
        Chatref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    Chatref2.child("id").setValue(myUid);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void checkOnlineStatus(String status){
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap= new HashMap<>();
        hashMap.put("onlineStatus",status);

        ref.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap= new HashMap<>();
        hashMap.put("typingTo",typing);

        ref.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        //set online
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //get timestamp
        String timestamp=String.valueOf(System.currentTimeMillis());
        //set offline with last seen timestamp
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);
    }


    @Override
    protected void onResume() {
        //set online
        checkOnlineStatus("online");
        super.onResume();
    }

    private void checkUserStatus(){
        //get current user

        FirebaseUser user=firebaseAuth.getCurrentUser();

        if(user!=null){
            //user is signed in stay here
            //profileTv.setText(user.getEmail());

            myUid=user.getUid();//currently signed in user's uid

        }else{
            //user is not signed in go to main activity
            startActivity(new Intent(this,MainActivity.class));
            finish();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();

        getMenuInflater().inflate(R.menu.menu_main,menu);
        //hide search  add post,view as we dont need it
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);

        menu.findItem(R.id.action_add_goupInfo).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get id
        int id=item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();

        }
        return super.onOptionsItemSelected(item);
    }
}