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
import android.text.format.DateFormat;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class GroupEditActivity extends AppCompatActivity {


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

    private ActionBar actionBar;

    ProgressDialog pd;

    //private
    private FirebaseAuth firebaseAuth;

    private ImageView groupIconTv;
    private EditText groupTitle1;
    private EditText groupDescription1;
    private FloatingActionButton update;



    String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        actionBar=getSupportActionBar();
        actionBar.setTitle("Edit Group");
        actionBar.setDisplayHomeAsUpEnabled(true);

        groupIconTv=findViewById(R.id.groupIconTv);
        groupDescription1=findViewById(R.id.groupDescription);
        groupTitle1=findViewById(R.id.groupTitle);
        update=findViewById(R.id.update);

        groupId=getIntent().getStringExtra("groupId");

        pd= new ProgressDialog(this);
        pd.setMessage("Please wait....");
        pd.setCanceledOnTouchOutside(false);


        cameraPermissions= new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth=FirebaseAuth.getInstance();
        checkUser();
        loadGroupInfo();

        groupIconTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showImagePickDialog();

            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUpdatingGroup();
            }
        });



    }

    private void startUpdatingGroup() {
        //input data
        final String grupTitle=groupTitle1.getText().toString().trim();
        final String grupDesc=groupDescription1.getText().toString().trim();

        if(TextUtils.isEmpty(grupTitle)){
            Toast.makeText(this, "Group title is required..", Toast.LENGTH_SHORT).show();
            return;

        }

        pd.setMessage("Updating Group info.....");
        pd.show();
        if(image_rui==null){
            //update group without icon


            HashMap<String,Object> hashMap= new HashMap<>();
            hashMap.put("groupTitle",grupTitle);
            hashMap.put("groupDescription",grupDesc);

            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
            ref.child(groupId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            Toast.makeText(GroupEditActivity.this, "Group info updated....", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(GroupEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }else{
            //update with icon
            //creating group with icon image
            //upload image
            //image name nd path
            final String g_timestamp=""+System.currentTimeMillis();
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

                                HashMap<String,Object> hashMap= new HashMap<>();
                                hashMap.put("groupTitle",grupTitle);
                                hashMap.put("groupDescription",grupDesc);
                                hashMap.put("groupIcon",""+p_downloadUri);

                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
                                ref.child(groupId).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pd.dismiss();
                                                Toast.makeText(GroupEditActivity.this, "Group info updated....", Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pd.dismiss();
                                                Toast.makeText(GroupEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(GroupEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


        }
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

                    //set groupInfo
                    groupTitle1.setText(groupTitle);
                    groupDescription1.setText(groupDescription);

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