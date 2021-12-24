package com.jether.nrmtuwaaye.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
import com.jether.nrmtuwaaye.Adapters.AdapterPosts;
import com.jether.nrmtuwaaye.AddPostActivity;
import com.jether.nrmtuwaaye.MainActivity;
import com.jether.nrmtuwaaye.Models.ModelPost;
import com.jether.nrmtuwaaye.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


public class ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    RecyclerView postsRecyclerView;

    ImageView avatarTv,coverTv;
    TextView nameTv,emailTv,phoneTv;
    ProgressDialog pd;

    //permission constants
    private static final int CAMERA_REQUEST_CODE=200;
    private static final int STORAGE_REQUEST_CODE=300;
    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE=400;
    private static final int IMAGE_PICK_CAMERA_CODE=500;

    String profileOrCoverPhoto;



    //path where the images of the user profile and cover will be stored
    String storagePath="Users_Profile_Cover_Imgs/";

    //permission arrays
    private String [] cameraPermissions;
    private String [] storagePermissions;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    //image picked Uri
    private Uri image_uri;

    private FloatingActionButton fab;
    public ProfileFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth =FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        firebaseDatabase= FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Users");
        storageReference= FirebaseStorage.getInstance().getReference();


        //init array permissions

        cameraPermissions= new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        //init views
        avatarTv=view.findViewById(R.id.avatarTv);
        nameTv=view.findViewById(R.id.nameTv);
        emailTv=view.findViewById(R.id.emailTv);
        phoneTv=view.findViewById(R.id.phoneTv);
        fab=view.findViewById(R.id.fab);
        coverTv=view.findViewById(R.id.coverTv);
        postsRecyclerView=view.findViewById(R.id.recycler_viewPosts);


        pd= new ProgressDialog(getActivity());


        /*We have to get the info of the currently signed in user we can get it using the user's email or uid
         * i'm gona retrieve user detail using email*/
        /*By using oderBychild query we will show the detail  from the node whose key named email has a value to currently signed email
         it will search all nodes, where the key matches it will get detail*/

        Query query=databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //check until get the data
                for(DataSnapshot ds:snapshot.getChildren()){
                    //get data
                    String name=""+ds.child("name").getValue();
                    String email=""+ds.child("email").getValue();
                    String phone=""+ds.child("phone").getValue();
                    String image=""+ds.child("image").getValue();
                    String cover=""+ds.child("cover").getValue();

                    //set
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    try{

                        //if image received set
                        Picasso.get().load(image).into(avatarTv);
                    }catch(Exception e){
                        //if there is any exception while getting image then set default
                        Picasso.get().load(R.drawable.ic_face_yellow).into(avatarTv);
                    }

                    try{

                        //if image received set
                        Picasso.get().load(cover).into(coverTv);
                    }catch(Exception e){
                        //if there is any exception while getting image then set default
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });
        postList= new ArrayList<>();

        checkUserStatus();
        loadMyPosts();

        return view;

    }

    private void loadMyPosts() {
        //linear layout for recyclerview
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getActivity());
        //show newest post first,for this load from last
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        //set this layout to recycler view
        postsRecyclerView.setLayoutManager(linearLayoutManager);
        //init posts list
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        //query to load posts
        Query query=ref.orderByChild("uid").equalTo(uid);
        //get all data from ref

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();

                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelPost myPosts=ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(myPosts);

                    //adpter
                    adapterPosts= new AdapterPosts(getActivity(),postList);
                    //set this adapter to recycler
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });
    }

    private void searchMyPosts(final String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getActivity());
        //show newest post first,for this load from last
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        //set this layout to recycler view
        postsRecyclerView.setLayoutManager(linearLayoutManager);
        //init posts list
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        //query to load posts
        Query query=ref.orderByChild("uid").equalTo(uid);
        //get all data from ref

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();

                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelPost myPosts=ds.getValue(ModelPost.class);


                    if(myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
                        //add to list
                        postList.add(myPosts);
                    }


                    //adpter
                    adapterPosts= new AdapterPosts(getActivity(),postList);
                    //set this adapter to recycler
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });
    }

    private void showEditProfileDialog() {
        //options to show
        String[] options={"Edit Profile Picture","Edit Cover Photo","Edit Name","Edit Phone","Change Password"};
        //alert dailog
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Choose Action");
        //set items to builder
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //handle item clicks
                if(i==0){
                    //edit profile clicked
                    pd.setTitle("Updating Profile Picture");
                    profileOrCoverPhoto="image";
                    showImagePicDialog();

                }else if(i==1){
                    //edit cover clicked
                    pd.setTitle("Updating Cover Photo");
                    profileOrCoverPhoto="cover";
                    showImagePicDialog();

                }else if(i==2){
                    //Edit name clicked
                    pd.setTitle("Updating Name");
                    //calling method and pass key "name" as parameter to update its value in database
                    showNamePhoneUpdateDialog("name");

                }else if(i==3){
                    //Edit phone clicked
                    pd.setTitle("Updating Phone");
                    showNamePhoneUpdateDialog("phone");
                }else if(i==4){
                    //Edit phone clicked
                    pd.setTitle("Changing Password");
                    showChangePasswordDialog();
                }

            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void showChangePasswordDialog() {
        //password change dailog with custom layout having currentPassword,new password and update button


        //inflate layout for dialog
        View view=LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_password,null);
        final EditText passwordEt=view.findViewById(R.id.passwordEt);
        final EditText newpasswordEt=view.findViewById(R.id.newpasswordEt);
        final Button upDatepassword=view.findViewById(R.id.upDatepassword);

        final AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        builder.setView(view);//set view to dialog

         final AlertDialog dialog=builder.create();
        dialog.show();
        

        upDatepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //validate data
                String oldPassword=passwordEt.getText().toString().trim();
                String newPassword=newpasswordEt.getText().toString().trim();

                if(TextUtils.isEmpty(oldPassword)){
                    Toast.makeText(getActivity(), "Enter you current password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(newPassword.length()<6){
                    Toast.makeText(getActivity(), "Password length should not be less than 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updatepassword(oldPassword,newPassword);
            }
        });

    }

    private void updatepassword(String oldPassword, final String newPassword) {
        pd.show();

        //get current user
        final FirebaseUser user=firebaseAuth.getCurrentUser();

        //before changing the password re-authenticate the user
        AuthCredential authCredential= EmailAuthProvider.getCredential(user.getEmail(),oldPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //succesfull begin update
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //password updated
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), "Password Updated...", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //password updated
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //authentication  failed
                        pd.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private boolean checkStoragePermissions(){
        //check if storage permission is enabled or not
        //return true if enable
        //return  false if not enabled
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result;

    }

    private void requestStoragePermissions(){
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions(){
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)==
                (PackageManager.PERMISSION_GRANTED);

        boolean result2= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);

        return result && result2;//return true of false

    }

    private void requestCameraPermissions(){
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }

    private void showNamePhoneUpdateDialog(final String key) {
        /*parameter "key" will contain value:
        either "name" which is key user'sdatabase which is used to update user's name
        or "phone" which is key user'sdatabase which is used to update user's phone
        * */

        //custom dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Update"+key);//eg update name or phone
        //set linear
        LinearLayout linearLayout= new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //add edit text
        final EditText editText= new EditText(getActivity());
        editText.setHint("Enter"+key);//eg edit name
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add button to update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //input from edit text
                final String value=editText.getText().toString().trim();
                //validate if user has entered something or not
                if(!TextUtils.isEmpty(value)){
                    pd.show();

                    HashMap<String,Object> result= new HashMap<>();
                    result.put(key,value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(),"Updated..",Toast.LENGTH_SHORT).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(),""+e.getMessage(),Toast.LENGTH_SHORT).show();

                                }
                            });

                    //if user changes name also change it in posts
                    if(key.equals("name")){
                        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                        Query query=ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                for(DataSnapshot ds:snapshot.getChildren()){
                                    String child=ds.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        //update name in current comments
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds:snapshot.getChildren()){
                                    String child=ds.getKey();
                                    if(snapshot.child(child).hasChild("Comments")){
                                        String child1=""+snapshot.child(child).getKey();
                                        Query child2=FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for(DataSnapshot ds:snapshot.getChildren()){
                                                    String child=ds.getKey();
                                                    snapshot.getRef().child(child).child("uName").setValue(value);

                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

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

                }else{
                    Toast.makeText(getActivity(),"Please enter"+key,Toast.LENGTH_SHORT).show();

                }

            }
        });
        //add button to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                pd.dismiss();

            }
        });
        builder.create().show();
    }

    private void showImagePicDialog() {
        //show image pic shows options of camera and gallery to pic image
        //options to show
        String[] options={"Camera","Gallery"};
        //alert dailog
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Pick Image From");
        //set items to builder
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //handle item clicks
                if(i==0){
                    //Camera clicked

                    if(!checkCameraPermissions()){
                        requestCameraPermissions();;

                    }else{
                        pickFromCamera();

                    }


                }else if(i==1){
                    //Gallery clicked
                    if(!checkStoragePermissions()){
                        requestStoragePermissions();

                    }else{
                        pickFromGallery();

                    }

                }

            }
        });
        //create and show dialog
        builder.create().show();

    }

    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this method is called when the user press allow or deny from permission requst dialog
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

                        Toast.makeText(getActivity(), "Camera and storage permissions...required", Toast.LENGTH_SHORT).show();
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

                        Toast.makeText(getActivity(), "Storage permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                //get picked image
                image_uri=data.getData();

                uploadProfileCoverPhoto(image_uri);

            }

            if(requestCode==IMAGE_PICK_CAMERA_CODE){

                uploadProfileCoverPhoto(image_uri);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(final Uri uri) {
        /*Instead of creating function for profile picture and cover photo
         *iam doing work for both functions
         *
         *To add check i will add a sstring variable and assign it value "image" when user clicks
         *"Edit Profile Pic" and assign it value "cover" when the user clicks "Edit Cover Photo"
         * Here: image is the key in each user containing uri of user's profile picture;
         *  cover is the key in each user containing uri of user's cover photo;
         * */

        /*The parameter "image_uri" contains the uri of he image picked either from the camera or gallery.

         *We will use UId of the currently signed in user as name of the image so there will be only one image
         *profile and one image for cover for eachother
         * */

        //path nad image of the  image to be stored

        String filePathAndName=storagePath+""+profileOrCoverPhoto+"_"+user.getUid();

        StorageReference storageReference1=storageReference.child(filePathAndName);
        storageReference1.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is uploaded in the storage, now get its url and store it in the database
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());

                        final Uri downloadUri=uriTask.getResult();

                        //check if the image is uploaded or not and uri is received
                        if(uriTask.isSuccessful()){

                            //image is uploaded

                            //add updated uri in user database
                            HashMap<String,Object> results= new HashMap<>();
                            /*First parameter is the profileOrCoverPhoto that ha s a value of "image " or "cover" which are keys in the users database where uri of
                            image will asved in one of them
                            Second parameter contains the uri of the image stored in tha firebasestorage, this uri will be saved as the value against key "image"
                            or "cover"
                            * */
                            results.put(profileOrCoverPhoto,downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //uri of database of user ias added successfully
                                            pd.dismiss();
                                            Toast.makeText(getActivity(),"Image Updated",Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(),"Error Updating Image",Toast.LENGTH_SHORT).show();

                                        }
                                    });


                                            if(profileOrCoverPhoto.equals("image")){
                                                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                                                Query query=ref.orderByChild("uid").equalTo(uid);
                                                query.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                        for(DataSnapshot ds:snapshot.getChildren()){
                                                            String child=ds.getKey();
                                                            snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());

                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                                //update uer image  in current comments
                                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for(DataSnapshot ds:snapshot.getChildren()){
                                                            String child=ds.getKey();
                                                            if(snapshot.child(child).hasChild("Comments")){
                                                                String child1=""+snapshot.child(child).getKey();
                                                                Query child2=FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                                child2.addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        for(DataSnapshot ds:snapshot.getChildren()){
                                                                            String child=ds.getKey();
                                                                            snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());

                                                                        }

                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

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
                        }else{
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Some error Occured",Toast.LENGTH_SHORT).show();

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });



    }

    private void pickFromGallery(){
        //intent to pick image from gallery
        Intent intent= new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){
        //intent to pick image from camera

        //using mediastore to pick high quality image
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image_Description");

        image_uri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);

    }


    private void checkUserStatus(){
        //get current user

        FirebaseUser user=firebaseAuth.getCurrentUser();

        if(user!=null){
            //user is signed in stay here
            //profileTv.setText(user.getEmail());
            uid=user.getUid();


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
        //hide
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_add_goupInfo).setVisible(false);

        MenuItem item=menu.findItem(R.id.action_search);
        //searchview to specific posts
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when you press the search button

                if(!TextUtils.isEmpty(s)){
                    searchMyPosts(s);
                }else{
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called wen the user presses any letter
                if(!TextUtils.isEmpty(s)){
                    searchMyPosts(s);
                }else{
                    loadMyPosts();
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

        if(id==R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }

}