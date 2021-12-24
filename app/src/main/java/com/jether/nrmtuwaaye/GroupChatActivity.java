package com.jether.nrmtuwaaye;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.text.TextUtils;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jether.nrmtuwaaye.Adapters.AdapterGroupChat;
import com.jether.nrmtuwaaye.Models.ModelGroupChat;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    private String groupId,myGroupRole="";

    private Toolbar toolbar;
    private ImageButton sendBtn,attachBtn ;
    private EditText messageEt ;
    private ImageView groupIconTv ;
    private TextView groupTitleTv;



    private RecyclerView recyk;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelGroupChat> groupChats;
    private AdapterGroupChat adapterGroupChat;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);



        toolbar=findViewById(R.id.toolBar);
        sendBtn=findViewById(R.id.sendBtn);
        attachBtn=findViewById(R.id.attachBtn);
        messageEt=findViewById(R.id.messageEt);
        groupIconTv=findViewById(R.id.groupIconTv);
        groupTitleTv=findViewById(R.id.groupTitleTv);
        recyk=findViewById(R.id.recyk);

        setSupportActionBar(toolbar);


        Intent intent= getIntent();
        groupId=intent.getStringExtra("groupId");

        cameraPermissions= new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth=FirebaseAuth.getInstance();

        loadGroupInfo();
        loadGroupMessages();
        loadMyGroupRole();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message=messageEt.getText().toString().trim();

                //check if text is empty
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(GroupChatActivity.this,"Cannot send empty Message.....",Toast.LENGTH_SHORT).show();
                }else{
                    sendMessage(message);
                }
            }
        });

        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick image from gallery//camera
                showImageImportDialog();
            }
        });
    }

    private void showImageImportDialog() {
        //options(camera,gallery ) to show

        String [] options={"Camera","Gallery"};

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Pick Image");
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
        contentValues.put(MediaStore.Images.Media.TITLE,"GroupImageTitle");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"GroupImageDescription");

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
        progressDialog.setTitle("Please wait..");
        progressDialog.setMessage("Sending image....");
        progressDialog.setCanceledOnTouchOutside(false);
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

                        String p_downloadUri=task.getResult().toString();

                        if(task.isSuccessful()){
                            //add iamge uri and other info to database

                            DatabaseReference refs=FirebaseDatabase.getInstance().getReference();

                            //set data required
                            HashMap<String,Object> hashMap= new HashMap<>();
                            hashMap.put("sender",""+firebaseAuth.getUid());
                            hashMap.put("message",""+p_downloadUri);
                            hashMap.put("timestamp",""+timestamp);
                            hashMap.put("type",""+"image");

                            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
                            ref.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //message sent
                                            //clear text
                                            messageEt.setText("");
                                            progressDialog.dismiss();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    });


                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                    }
                });

    }


    private void loadMyGroupRole() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants")
                .orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            myGroupRole=""+ds.child("role").getValue();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadGroupMessages() {
        groupChats= new ArrayList<>();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChats.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelGroupChat modelGroupChat=ds.getValue(ModelGroupChat.class);
                    groupChats.add(modelGroupChat);
                }
                adapterGroupChat= new AdapterGroupChat(GroupChatActivity.this,groupChats);
                recyk.setAdapter(adapterGroupChat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {


        String timestamp=""+System.currentTimeMillis();

        HashMap<String,Object> hashMap= new HashMap<>();
        hashMap.put("sender",""+firebaseAuth.getUid());
        hashMap.put("message",""+message);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("type",""+"text");//text/image/file

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //message sent
                        messageEt.setText("");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //message failed
                        Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });



    }

    private void loadGroupInfo() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot ds:snapshot.getChildren()){
                            String grupTitle=""+ds.child("groupTitle").getValue();
                            String grupDe=""+ds.child("groupDescription").getValue();
                            String grupIc=""+ds.child("groupIcon").getValue();
                            String timez=""+ds.child("timestamp").getValue();
                            String created=""+ds.child("createdBy").getValue();


                            groupTitleTv.setText(grupTitle);

                            try{
                                Picasso.get().load(grupIc).placeholder(R.drawable.ic_gulupu).into(groupIconTv);

                            }catch(Exception e){
                                groupIconTv.setImageResource(R.drawable.ic_gulupu);
                                invalidateOptionsMenu();

                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_main,menu);
        //hide search  add post,view as we dont need it
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);

        if(myGroupRole.equals("creator") || myGroupRole.equals("admin")){
            //in is admin/creator,show add person option
            menu.findItem(R.id.action_add_participant).setVisible(true);

        }else{
            menu.findItem(R.id.action_add_participant).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();

        if(id==R.id.action_add_participant){
           Intent intent= new Intent(this,GroupAddParticipantsActivity.class);
           intent.putExtra("groupId",groupId);
           startActivity(intent);
        }
        else if(id==R.id.action_add_goupInfo){
            Intent intent= new Intent(this,GroupInfoActivity.class);
            intent.putExtra("groupId",groupId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}