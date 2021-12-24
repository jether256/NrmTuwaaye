package com.jether.nrmtuwaaye;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;

public class CreateGroupActivity extends AppCompatActivity {
    //action bar
    private ActionBar actionBar;

    ProgressDialog pd;

    //private
    private FirebaseAuth firebaseAuth;

    private ImageView groupIconTv;
    private EditText groupTitle;
    private EditText groupDescription;
    private FloatingActionButton done;

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
        setContentView(R.layout.activity_create_group);

        actionBar=getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Create Group");

        groupIconTv=findViewById(R.id.groupIconTv);
        groupDescription=findViewById(R.id.groupDescription);
        groupTitle=findViewById(R.id.groupTitle);
        done=findViewById(R.id.done);


        cameraPermissions= new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth=FirebaseAuth.getInstance();

        checkUser();

        groupIconTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showImagePickDialog();

            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreatingGroup();
            }
        });
    }

    private void startCreatingGroup() {
        pd= new ProgressDialog(this);
        pd.setMessage("Creating Group");

        //input data
        final String  groupTitle1=groupTitle.getText().toString().trim();
        final String  groupDes1=groupDescription.getText().toString().trim();

        if(TextUtils.isEmpty(groupTitle1)){
            Toast.makeText(this, "Please enter group title...", Toast.LENGTH_SHORT).show();
            return;

        }
        pd.show();

        //timestamp for group icon iamge,group id, timecreated
        final String g_timestamp=""+System.currentTimeMillis();

        if(image_rui==null){
            //creating gruop without icon Image
            createGroup(""+g_timestamp,
                    ""+groupTitle1,
                    ""+groupDes1,
                    ""
            );

        }else{
            //creating group with icon image
            //upload image
            //image name nd path
             String fileNameAndPath="Group_Imgs/"+"image"+g_timestamp;

            StorageReference storageReference= FirebaseStorage.getInstance().getReference(fileNameAndPath);
            storageReference.putFile(image_rui)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image is uploaded get iits uri
                            Task<Uri> p_uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while(!p_uriTask.isSuccessful());
                            Uri p_downloadUri=p_uriTask.getResult();
                            if(p_uriTask.isSuccessful()){
                                //creating gruop without icon Image
                                createGroup(""+g_timestamp,
                                        ""+groupTitle1,
                                        ""+groupDes1,
                                        ""+p_downloadUri
                                );

                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void createGroup(final String g_timestamp, String groupTitle1, String groupDes1, String groupIcon) {
        //set info for group

        final HashMap<String,String> hashMap= new HashMap<>();
        hashMap.put("groupId",""+g_timestamp);
        hashMap.put("groupTitle",""+groupTitle1);
        hashMap.put("groupDescription",""+groupDes1);
        hashMap.put("groupIcon",""+groupIcon);
        hashMap.put("timestamp",""+g_timestamp);
        hashMap.put("createdBy",""+firebaseAuth.getUid());


        //create group
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(g_timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //successful

                        //setup member info(add current user in groups participants list)
                        HashMap<String,String> hashMap1= new HashMap<>();
                        hashMap1.put("uid",firebaseAuth.getUid());
                        hashMap1.put("role","creator");
                        hashMap1.put("timestamp",g_timestamp);


                        DatabaseReference ref1=FirebaseDatabase.getInstance().getReference("Groups");
                        ref1.child(g_timestamp).child("Participants").child(firebaseAuth.getUid())
                                .setValue(hashMap1)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //participant added
                                        pd.dismiss();
                                        Toast.makeText(CreateGroupActivity.this, "Group Created....", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        pd.dismiss();
                        Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

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
        contentValues.put(MediaStore.Images.Media.TITLE,"Group Image Icon Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Group Image Icon Description");

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
                //aws picked from gallery
                image_rui=data.getData();
                //set iamge view
                groupIconTv.setImageURI(image_rui);

            }

            else if(requestCode==IMAGE_PICK_CAMERA_CODE){
                //image picked from camera ,get uri of image
                //set iamge view
                groupIconTv.setImageURI(image_rui);


            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void checkUser() {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user !=null){
                actionBar.setSubtitle(user.getEmail());
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}