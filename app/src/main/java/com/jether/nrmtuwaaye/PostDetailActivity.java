package com.jether.nrmtuwaaye;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.jether.nrmtuwaaye.Adapters.AdapterComments;
import com.jether.nrmtuwaaye.Models.ModelComment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    //to get detail of the user and post
    String myUid,myEmail,myName,myDp,postId,pLikes,hisDp,hisName,hisUid,pImage;

    boolean mProcessComment=false;
    boolean mProcessLike=false;

    ProgressDialog pd;

    //views from post row
    ImageView pImageTv,uPicTv;

    TextView uNameTv,pTimeTv,pTitleTv,pDescriptionTv,pLikesTv,pCommentTV;
    ImageButton moreBtn;
    Button likeBtn,shareBtn;

    LinearLayout profileLayout;

    //views of comments
    ImageButton sendBtn;
    ImageView cAvatar;
    EditText commentEt;

    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        ActionBar actionBar= getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //get id of post using intent
        Intent intent=getIntent();
        postId=intent.getStringExtra("postId");


        pImageTv=findViewById(R.id.pImageTv);
        uPicTv=findViewById(R.id.uPicTv);
        uNameTv=findViewById(R.id.uNameTv);
        pTimeTv=findViewById(R.id.pTimeTv);
        pTitleTv=findViewById(R.id.pTitleTv);
        pDescriptionTv=findViewById(R.id.pDescriptionTv);
        pLikesTv=findViewById(R.id.pLikesTv);
        moreBtn=findViewById(R.id.moreBtn);
        likeBtn=findViewById(R.id.likeBtn);
        shareBtn=findViewById(R.id.shareBtn);
        pCommentTV=findViewById(R.id.pCommentsTv);
        profileLayout=findViewById(R.id.profileLayout);

        recyclerView= findViewById(R.id.recyclll);

        sendBtn=findViewById(R.id.sendBtn);
        cAvatar=findViewById(R.id.cAvatar);
        commentEt=findViewById(R.id.commentEt);

        loadPostInfo();
        checkUserStatus();

        loadUserInfo();

        setLikes();

        //set subtitle of action bar
        actionBar.setSubtitle("SignedIn as:"+myEmail);

        loadComments();

        //send comment button
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postComment();
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               likePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions();

            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pTitle=pTitleTv.getText().toString().trim();
                String pDescription=pDescriptionTv.getText().toString().trim();

                BitmapDrawable bitmapDrawable=(BitmapDrawable)pImageTv.getDrawable();
                if (bitmapDrawable==null){
                    //post without image

                    shareTextOnly(pTitle,pDescription);

                }else{
                    //post with image

                    //convert iamge to bitmap
                    Bitmap bitmap=bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle,pDescription,bitmap);

                }
            }
        });

        ///click like count to start PostLikedActivity,and pass postid
        pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(PostDetailActivity.this, PostLikedByActivity.class);
                intent.putExtra("postId",postId);
                startActivity(intent);
            }
        });
    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        //concatenate tetle and description to share
        String shareBody=pTitle+"\n"+pDescription;

        //fisrt we will save this image in the cache,get saved image uri
        Uri uri=saveImageToShare(bitmap);

        //share intent
        Intent intent= new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.putExtra(Intent.EXTRA_TEXT,shareBody);
        intent.putExtra(Intent.EXTRA_SUBJECT,"Subject here");
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent,"Share via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder= new File(getCacheDir(),"images");
        Uri uri=null;

        try{
            imageFolder.mkdirs();//create if it doesnot exist
            File file= new File(imageFolder,"shared_image_png");

            FileOutputStream stream= new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri= FileProvider.getUriForFile(this,"com.jether.nrmtuwaye.fileprovider",file);

        }catch (Exception e){

            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        return uri;
    }

    private void shareTextOnly(String pTitle, String pDescription) {
        //concatenate tetle and description to share
        String shareBody=pTitle+"\n"+pDescription;

        //share intent
        Intent sIntent= new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject here");//in case you share via email
        sIntent.putExtra(Intent.EXTRA_TEXT,shareBody);//text share
     startActivity(Intent.createChooser(sIntent,"Share Via"));//message show in share dialog


    }



    private void loadComments() {
        //layout for recycler
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getApplicationContext());
        //set Layout to recycler
        recyclerView.setLayoutManager(linearLayoutManager);


        //init comment list
        commentList = new ArrayList<>();

        //path of the post,to get comments
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelComment modelComment= ds.getValue(ModelComment.class);


                    commentList.add(modelComment);

                    //pass myUid and postId  as parameter of the constructor of comment adapter

                    //setup adapter
                    adapterComments=new AdapterComments(getApplicationContext(),commentList,myUid,postId);
                    //set up adapter
                    recyclerView.setAdapter(adapterComments);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setLikes() {
        // when details post is loading,also check if current user has liked or not
        final DatabaseReference likesRef=FirebaseDatabase.getInstance().getReference().child("Likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child(postId).hasChild(myUid)){
                    //user has liked this post
                    //To indicated that the post has been liked by this(Signed in user)
                    //Change Drawable left icon of the like button;
                    //Change text of the like bUton from "Like" to "Liked"
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked_yellow,0,0,0);
                    likeBtn.setText("Liked");

                }else{
                    //user has not liked the post
                    //To indicated that the post not liked by this(Signed in user)
                    //Change Drawable left icon of the like button;
                    //Change text of the like bUton from "Liked" to "Like"
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    likeBtn.setText("Like");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void likePost() {
        //get the total number of likes for the post,whoose liked button clicked
        //if currently signed in user has not liked before
        //increase the value by 1,otherwise decrease by 1
        mProcessLike=true;
        //get id of clicked post
        final DatabaseReference likesRef=FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessLike){
                    if(snapshot.child(postId).hasChild(myUid)){
                        //already liked so remove like
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike=false;


                    }else{
                        //not liked, like it
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likesRef.child(postId).child(myUid).setValue("Liked");//set any value
                        mProcessLike=false;



                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showMoreOptions() {
        //creating pop menu currently having delete,we shall add others
        PopupMenu popupMenu= new PopupMenu(this,moreBtn, Gravity.END);

        //show delete of option in only posts of signed in user
        if(hisUid.equals(myUid)){
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");

        }

        //item listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id=menuItem.getItemId();
                if(id==0){
                    //deleted is clicked
                    beginDelete();
                }
                else if(id==1){
                    //Edit is clicked
                    //start AddPostActivity with key "editPost" and the id of the post clicked
                    Intent intent= new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",postId);
                    startActivity(intent);

                }

                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete() {
        //post can be with or without image
        if(pImage.equals("noImage")){
            //without image
            deleteWithoutImage();

        }else{
            //post with image
            deleteWithImage();

        }
    }

    private void deleteWithImage() {
        //progress bar
        final ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Deleting...");

        /*delete iamge using url
        delete from data base using post id
         */

        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, now delete in database

                        Query fQuery= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds:snapshot.getChildren()){
                                    ds.getRef().removeValue();//remove values from database hwere pid matches
                                }
                                //deleted
                                Toast.makeText(PostDetailActivity.this, "Deleted successfully....", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed cant go further
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void deleteWithoutImage() {
        //progress bar
        final ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Deleting...");
        Query fQuery= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    ds.getRef().removeValue();//remove values from database hwere pid matches
                }
                //deleted
                Toast.makeText(PostDetailActivity.this, "Deleted successfully....", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postComment() {
        pd= new ProgressDialog(this);
        pd.setMessage("Adding Comment....");


        //get data from editText
        String comment =commentEt.getText().toString().trim();
        //validate
        if(TextUtils.isEmpty(comment)){
            // no value is entered
            Toast.makeText(this, "Comment is empty.....", Toast.LENGTH_SHORT).show();

        }

        String timeStamp=String.valueOf(System.currentTimeMillis());

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<Object,String> hashMap= new HashMap<>();
        //put info in hashmap
        hashMap.put("cId",timeStamp);
        hashMap.put("comment",comment);
        hashMap.put("timestamp",timeStamp);
        hashMap.put("uid",myUid);
        hashMap.put("uEmail",myEmail);
        hashMap.put("uDp",myDp);
        hashMap.put("uName",myName);

        //put this data in db

        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, "Comment Added....", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        updateCommentCount();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }



    private void updateCommentCount() {
        //whenener user adds a comment increase the comment comment count
        mProcessComment=true;
        final DatabaseReference ex= FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ex.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessComment){
                    String comments=""+snapshot.child("pComments").getValue();
                    int newCommentBal= Integer.parseInt(comments) + 1;
                    ex.child("pComments").setValue(""+newCommentBal);
                    mProcessComment=false;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadUserInfo() {
        //get user Info
        Query query= FirebaseDatabase.getInstance().getReference("Users");
        query.orderByChild("uid").equalTo(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds:snapshot.getChildren()){
                    myName=""+ds.child("name").getValue();
                    myDp=""+ds.child("image").getValue();


                    //set data
                    try{
                        //if iamge received then set
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_face_yellow).into(cAvatar);

                    }catch(Exception e){
                        Picasso.get().load(R.drawable.ic_face_yellow).into(cAvatar);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        Query query=ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //keep checking the  posts till you get the required post
                for(DataSnapshot ds:snapshot.getChildren()){
                    //get Data
                    String pTitle=""+ds.child("pTitle").getValue();
                    String pDescr=""+ds.child("pDescr").getValue();
                    pLikes =""+ds.child("pLikes").getValue();
                    String pTimestamp=""+ds.child("pTime").getValue();
                     pImage=""+ds.child("pImage").getValue();
                    hisDp =""+ds.child("uDp").getValue();
                    hisUid=""+ds.child("uid").getValue();
                    String uEmail=""+ds.child("uEmail").getValue();
                    hisName =""+ds.child("uName").getValue();

                    String commentCount=""+ds.child("pComments").getValue();


                    //convert timestamp to correct format

                    //convert the time stamp to dd/MM/yyyy hh:mm am/pm

                    Calendar calendar=Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimestamp));

                    String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

                    //set data
                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText(pLikes+"Likes");
                    pTimeTv.setText(pTime);
                    pCommentTV.setText(commentCount+"Comments");

                    uNameTv.setText(hisName);

                    //set image of the user
                    //if there is no image i.e. pImage.equals("noImage") then hide image view
                    if(pImage.equals("noImage")){
                        //hide image view
                        pImageTv.setVisibility(View.GONE);

                    }else{
                        //show Imageview
                        pImageTv.setVisibility(View.VISIBLE);

                        try{

                            Picasso.get().load(pImage).into(pImageTv);

                        }catch(Exception e){

                        }


                        //set user image in comment section
                        try{
                            Picasso.get().load(hisDp).placeholder(R.drawable.ic_face_yellow).into(uPicTv);

                        }catch(Exception e){

                            Picasso.get().load(R.drawable.ic_face_yellow).into(uPicTv);
                        }

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            //user is signed in
            myEmail=user.getEmail();
            myUid=user.getUid();

        }else{
            //user not signed in
            startActivity(new Intent(this,MainActivity.class));

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflating menu
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_main,menu);
        //hide some menu items

        menu.findItem(R.id.action_add_post).setVisible(false);//
        menu.findItem(R.id.action_search).setVisible(false);//
        menu.findItem(R.id.action_add_goupInfo).setVisible(false);

        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get id
        int id=item.getItemId();
        if(id==R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();

        }
        return super.onOptionsItemSelected(item);
    }
}