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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Objects;

public class AddPostActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDbRef;
    ActionBar actionBar;

    //permission constants
    private static final int CAMERA_REQUEST_CODE=200;
    private static final int STORAGE_REQUEST_CODE=300;

    //permission arrays
    private String [] cameraPermissions;
    private String [] storagePermissions;





    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE=400;
    private static final int IMAGE_PICK_CAMERA_CODE=500;

    EditText pTitle,pDescription;

    ImageView pImage;

    Button pUpload;

    ProgressDialog pd;

    //image picked Uri
    private Uri image_rui= null;

    //user info

    String name,email,uid,dp;

    //info of post to be edited
    String editTitle,editDescription,editImage;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

    actionBar = getSupportActionBar();
   actionBar.setTitle("Add New Post");

   //enable back button
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        cameraPermissions= new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd= new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();
        checkUserStatus();

        pTitle=findViewById(R.id.pTitle);
        pDescription=findViewById(R.id.pDescription);
        pImage=findViewById(R.id.pImage);
        pUpload=findViewById(R.id.pUpload);




        //get data through intent from previous activities adapter
        Intent intent= new Intent();


        final String isUpdateKey=""+intent.getStringExtra("key");
        final String editPostId=""+intent.getStringExtra("editPostId");


        //validate if we came here to update post i.e came from adapter post
        if(isUpdateKey.equals("editPost")){
            //update
            actionBar.setTitle("Update Post");
            pUpload.setText("Update");
            loadPostData(editPostId);

        }else{
            //add
            actionBar.setTitle("Add New Post");
            pUpload.setText("Upload");
        }

        actionBar.setTitle(email);

        //get some info of cuurent user to include in the post

        userDbRef=FirebaseDatabase.getInstance().getReference("Users");

        Query query=userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds:snapshot.getChildren()){

                    name=""+ds.child("name").getValue();
                    email=""+ds.child("email").getValue();
                    dp=""+ds.child("image").getValue();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





        pImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });


        pUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get title,description from edit Texts

                String  title= pTitle.getText().toString().trim();
                String description= pDescription.getText().toString().trim();

                if (TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "Enter Title", Toast.LENGTH_SHORT).show();
                    return;

                }

                if (TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this, "Enter description", Toast.LENGTH_SHORT).show();
                    return;

                }


                    uploadData(title,description);


//                if(isUpdateKey.equals("editPost")){
//                   beginUpdate(title,description,editPostId);
//                }else{
//                    uploadData(title,description);
//                }


            }
        });

    }



//    private void beginUpdate(String title, String description, String editPostId) {
//        pd.setMessage("Updating post.....");
//        pd.show();
//
//        if(!editImage.equals("noImage")){
//            //without image
//            updateWasWithImage(title,description,editPostId);
//
//        }else if(pImage.getDrawable() !=null){
//            //with image
//            updateWithNowImage(title,description,editPostId);
//
//        }else {
//            //without Image and still no image
//            updateWithoutImage(title,description,editPostId);
//        }
//    }
//
//    private void updateWithoutImage(String title, String description, String editPostId) {
//        HashMap<String,Object> hashMap= new HashMap<>();
//        //put post info
//        hashMap.put("uid",uid);
//        hashMap.put("uName",name);
//        hashMap.put("uEmail",email);
//        hashMap.put("uDp",dp);
//        hashMap.put("pTitle",title);
//        hashMap.put("pDescr",description);
//        hashMap.put("pImage","noImage");
//
//
//        DatabaseReference zex=FirebaseDatabase.getInstance().getReference("Posts");
//        zex.child(editPostId)
//                .updateChildren(hashMap)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        pd.dismiss();
//                        Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        pd.dismiss();
//                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//
//    }
//
//    private void updateWithNowImage(final String title, final String description, final String editPostId) {
//        //image deleted upload new Image
//        //for post-image name,post-id,publish time
//        String timestamp= String.valueOf(System.currentTimeMillis());
//        String filePathName="Posts/"+"post_"+timestamp;
//
//
//        //get image from imageview
//        Bitmap bitmap=((BitmapDrawable)pImage.getDrawable()).getBitmap();
//        ByteArrayOutputStream baos= new ByteArrayOutputStream();
//
//        //image compress
//        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
//        byte[] data=baos.toByteArray();
//
//        StorageReference sRef=FirebaseStorage.getInstance().getReference().child(filePathName);
//        sRef.putBytes(data)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        //image uploaded get its url
//                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
//                        while(!uriTask.isSuccessful());
//
//                        String downloadUri= uriTask.getResult().toString();
//                        if(uriTask.isSuccessful()){
//
//                            //uri is received,upload to firebase database
//
//                            HashMap<String,Object> hashMap= new HashMap<>();
//                            //put post info
//                            hashMap.put("uid",uid);
//                            hashMap.put("uName",name);
//                            hashMap.put("uEmail",email);
//                            hashMap.put("uDp",dp);
//                            hashMap.put("pTitle",title);
//                            hashMap.put("pDescr",description);
//                            hashMap.put("pImage",downloadUri);
//
//
//                            DatabaseReference zex=FirebaseDatabase.getInstance().getReference("Posts");
//                            zex.child(editPostId)
//                                    .updateChildren(hashMap)
//                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//                                            pd.dismiss();
//                                            Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
//                                        }
//                                    })
//                                    .addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            pd.dismiss();
//                                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//
//                                        }
//                                    });
//                        }
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        //image not uploaded
//                        pd.dismiss();
//                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//
//
//    }
//
//    private void updateWasWithImage(final String title, final String description, final String editPostId) {
//        //post is with image,delete image first
//        StorageReference mPic=FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
//        mPic.delete()
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        //image deleted upload new Image
//                        //for post-image name,post-id,publish time
//                        String timestamp= String.valueOf(System.currentTimeMillis());
//                        String filePathName="Posts/"+"post_"+timestamp;
//
//
//                        //get image from imageview
//                        Bitmap bitmap=((BitmapDrawable)pImage.getDrawable()).getBitmap();
//                        ByteArrayOutputStream baos= new ByteArrayOutputStream();
//
//                        //image compress
//                        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
//                        byte[] data=baos.toByteArray();
//
//                        StorageReference sRef=FirebaseStorage.getInstance().getReference().child(filePathName);
//                        sRef.putBytes(data)
//                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                    @Override
//                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                        //image uploaded get its url
//                                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
//                                        while(!uriTask.isSuccessful());
//
//                                        String downloadUri= uriTask.getResult().toString();
//                                        if(uriTask.isSuccessful()){
//
//                                            //uri is received,upload to firebase database
//
//                                            HashMap<String,Object> hashMap= new HashMap<>();
//                                            //put post info
//                                            hashMap.put("uid",uid);
//                                            hashMap.put("uName",name);
//                                            hashMap.put("uEmail",email);
//                                            hashMap.put("uDp",dp);
//                                            hashMap.put("pTitle",title);
//                                            hashMap.put("pDescr",description);
//                                            hashMap.put("pImage",downloadUri);
//
//
//                                            DatabaseReference zex=FirebaseDatabase.getInstance().getReference("Posts");
//                                            zex.child(editPostId)
//                                                    .updateChildren(hashMap)
//                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                        @Override
//                                                        public void onSuccess(Void aVoid) {
//                                                            pd.dismiss();
//                                                            Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
//                                                        }
//                                                    })
//                                                    .addOnFailureListener(new OnFailureListener() {
//                                                        @Override
//                                                        public void onFailure(@NonNull Exception e) {
//                                                            pd.dismiss();
//                                                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//
//                                                        }
//                                                    });
//                                        }
//
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        //image not uploaded
//                                        pd.dismiss();
//                                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//
//                                    }
//                                });
//
//
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                    }
//                });
//
//    }

    private void loadPostData(String editPostId)  {

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        //get detail of post using id
        Query fQuery=ref.orderByChild("pId").equalTo(editPostId);
        fQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    //get data
                    editTitle=""+ds.child("pTitle").getValue();
                    editDescription=""+ds.child("pDescr").getValue();
                    editImage=""+ds.child("pImage").getValue();

                    //set data to views
                    pTitle.setText(editTitle);
                    pDescription.setText(editDescription);


                    //set Image
                    if(!editImage.equals("noImage")){

                        try{
                            Picasso.get().load(editImage).into(pImage);

                        }catch(Exception e){

                        }

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadData(final String title, final String description)  {
        pd.setMessage("Publishing Post...");
        pd.show();

        //for post-image name,post-id,post-publish-time;
        final String timestamp=String.valueOf(System.currentTimeMillis());

        String filePathName="Posts/" +"post_"+timestamp;

        if(pImage.getDrawable() != null){
            //get image from imageview
            Bitmap bitmap=((BitmapDrawable)pImage.getDrawable()).getBitmap();
            ByteArrayOutputStream baos= new ByteArrayOutputStream();

            //image compress
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
            byte[] data=baos.toByteArray();


            //post with iamge

            StorageReference ref= FirebaseStorage.getInstance().getReference().child(filePathName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //iamge uploaded in firebase storage, now get ist uri
                            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful());

                            String downLoadUri=uriTask.getResult().toString();

                            if(uriTask.isSuccessful()){
                                //uri is received upload post to firebase database
                                HashMap<Object,String> hashMap= new HashMap<>();
                                //put post Info
                                hashMap.put("uid",uid);
                                hashMap.put("uName",name);
                                hashMap.put("uEmail",email);
                                hashMap.put("uDp",dp);
                                hashMap.put("pId",timestamp);
                                hashMap.put("pTitle",title);
                                hashMap.put("pDescr",description);
                                hashMap.put("pImage",downLoadUri);
                                hashMap.put("pTime",timestamp);
                                hashMap.put("pComments","0");
                                hashMap.put("pLikes","0");



                                // path to store post data
                                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                                //put data in ref
                                ref.child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //added to db
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();
                                                //reset views
                                                pTitle.setText("");
                                                pDescription.setText("");
                                                pImage.setImageURI(null);
                                                image_rui=null;

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed adding post in database
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed uploading image
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }else{
            //post with out image

            HashMap<Object,String> hashMap= new HashMap<>();
            //put post Info
            hashMap.put("uid",uid);
            hashMap.put("uName",name);
            hashMap.put("uEmail",email);
            hashMap.put("uDp",dp);
            hashMap.put("pId",timestamp);
            hashMap.put("pTitle",title);
            hashMap.put("pDescr",description);
            hashMap.put("pImage","noImage");
            hashMap.put("pTime",timestamp);
            hashMap.put("pComments","0");
            hashMap.put("pLikes","0");


            // path to store post data
            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
            //put data in ref
            ref.child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //added to db
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();
                            //reset views
                            pTitle.setText("");
                            pDescription.setText("");
                            pImage.setImageURI(null);
                            image_rui=null;


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed adding post in database
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }


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

                pImage.setImageURI(image_rui);

            }

            else if(requestCode==IMAGE_PICK_CAMERA_CODE){
                //image picked from camera ,get uri of image
                pImage.setImageURI(image_rui);

            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus(){
        //get current user

        FirebaseUser user=firebaseAuth.getCurrentUser();

        if(user!=null){
            //user is signed in stay here
            //profileTv.setText(user.getEmail());
            email=user.getEmail();
            uid=user.getUid();


        }else{
            //user is not signed in go to main activity
            startActivity(new Intent(this,MainActivity.class));
            finish();

        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//back to previous
        return super.onSupportNavigateUp();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_main,menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_goupInfo).setVisible(false);

        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}