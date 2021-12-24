package com.jether.nrmtuwaaye.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jether.nrmtuwaaye.AddPostActivity;
import com.jether.nrmtuwaaye.Models.ModelPost;
import com.jether.nrmtuwaaye.PostDetailActivity;
import com.jether.nrmtuwaaye.PostLikedByActivity;
import com.jether.nrmtuwaaye.R;
import com.jether.nrmtuwaaye.ThereProfileActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {

    Context context;
    List<ModelPost> postList;

    String myUid;

    private DatabaseReference likesRef;// for likes database node
    private DatabaseReference postsRef;//reference for posts


    boolean mProcessLike=false;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid= FirebaseAuth.getInstance().getCurrentUser().getUid();

        likesRef=FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef=FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_posts,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        // get Data
        final String uid=postList.get(position).getUid();
        String uEmail=postList.get(position).getuEmail();
        String uName=postList.get(position).getuName();
        String uDp=postList.get(position).getuDp();
        final String pId=postList.get(position).getpId();
        final String pTitle=postList.get(position).getpTitle();
        final String pDescription=postList.get(position).getpDescr();
        final String pImage=postList.get(position).getpImage();
        String pTimeStamp=postList.get(position).getpTime();
        String pComment=postList.get(position).getpComments();//contains total number of comments for a post
        String pLikes=postList.get(position).getpLikes();//contains total number of likes for a post



        //convert the time stamp to dd/MM/yyyy hh:mm am/pm

        Calendar calendar=Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));

        String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        //set data
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);
        holder.pCommentsTv.setText(pComment+"Comments");
        holder.pLikesTv.setText(pLikes+"Likes");

        //set likes for each post
        setLikes(holder,pId);


        //set user dp
        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_face_yellow).into(holder.uPicTv);

        }catch(Exception e){

        }

        //set post Image
        //if there is no Image i.epImage equals("noImage") then hide imageView
        if(pImage.equals("noImage")){

            //hide image view
            holder.pImageTv.setVisibility(View.GONE);

        }else{

            //show image view
            holder.pImageTv.setVisibility(View.VISIBLE);

            try{
                Picasso.get().load(pImage).into(holder.pImageTv);

            }catch(Exception e){

            }



        }







        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                showMoreOptions(holder.moreBtn,uid,myUid,pId,pImage);
            }
        });



        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //get the total number of likes for the post,whoose liked button clicked
                //if currently signed in user has not liked before
                //increase the value by 1,otherwise decrease by 1
                final int pLikes= Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike=true;
                //get id of clicked post
                final String postIde=postList.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(mProcessLike){
                            if(snapshot.child(postIde).hasChild(myUid)){
                                //already liked so remove like
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike=false;

                            }else{
                                //not liked, like it
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                likesRef.child(postIde).child(myUid).setValue("Liked");//set any value
                                mProcessLike=false;

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });


        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent=new Intent(context, PostDetailActivity.class);
               intent.putExtra("postId",pId);//will get detail of the post using this id,and its id of the post clicked
                context.startActivity(intent);
            }
        });


        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*some posts contain only text and some conatin iamge and text so we shall handle them both
                * */
                //get image rom iamge view

                BitmapDrawable bitmapDrawable=(BitmapDrawable)holder.pImageTv.getDrawable();
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


        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Click to go to There Profile Activity with uid,this uid is of clicked user
                which will be used to show user specific data/posts/
                * */
                Intent intent= new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
            }
        });

        ///click like count to start PostLikedActivity,and pass postid
        holder.pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(context, PostLikedByActivity.class);
                intent.putExtra("postId",pId);
                context.startActivity(intent);
            }
        });

    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        //concatenate tetle and description to share
        String shareBody=pTitle+"\n"+pDescription;

        //fisrt we will save this image in the cache,get saved image uri
        Uri uri=saveImageToShare(bitmap);

        //share intent
        Intent sintent= new Intent(Intent.ACTION_SEND);
        sintent.putExtra(Intent.EXTRA_STREAM,uri);
        sintent.putExtra(Intent.EXTRA_TEXT,shareBody);
        sintent.putExtra(Intent.EXTRA_SUBJECT,"Subject here");
        sintent.setType("image/png");
        context.startActivity(Intent.createChooser(sintent,"Share via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder= new File(context.getCacheDir(),"images");
        Uri uri=null;

        try{
            imageFolder.mkdirs();//create if it doesnot exist
            File file= new File(imageFolder,"shared_image_png");

            FileOutputStream stream= new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri= FileProvider.getUriForFile(context,"com.jether.nrmtuwaye.fileprovider",file);

        }catch (Exception e){

            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

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
        context.startActivity(Intent.createChooser(sIntent,"Share Via"));//message show in share dialog


    }


    //add key named "pLikes" to each Post and set its value to "0" manually in fire base

    private void setLikes(final MyHolder holder, final String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child(postKey).hasChild(myUid)){
                    //user has liked this post
                    //To indicated that the post has been liked by this(Signed in user)
                    //Change Drawable left icon of the like button;
                    //Change text of the like bUton from "Like" to "Liked"
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked_yellow,0,0,0);
                    holder.likeBtn.setText("Liked");

                }else{
                    //user has not liked the post
                    //To indicated that the post not liked by this(Signed in user)
                    //Change Drawable left icon of the like button;
                    //Change text of the like bUton from "Liked" to "Like"
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    holder.likeBtn.setText("Like");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {
        //creating pop menu currently having delete,we shall add others
        PopupMenu popupMenu= new PopupMenu(context,moreBtn, Gravity.END);

        //show delete of option in only posts of signed in user
        if(uid.equals(myUid)){
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
//            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");

        }

        popupMenu.getMenu().add(Menu.NONE,1,0,"View Detail");



        //item listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id=menuItem.getItemId();
                if(id==0){
                    //deleted is clicked
                    beginDelete(pId,pImage);
                }
//                else if(id==1){
//                    //Edit is clicked
//                    //start AddPostActivity with key "editPost" and the id of the post clicked
//                    Intent intent= new Intent(context, AddPostActivity.class);
//                    intent.putExtra("key","editPost");
//                    intent.putExtra("editPostId",pId);
//                    context.startActivity(intent);
//
//                }

                else if (id==1) {
                    Intent intent=new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId",pId);//will get detail of the post using this id,and its id of the post clicked
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        //post can be with or without image
        if(pImage.equals("noImage")){
            //without image
            deleteWithoutImage(pId);

        }else{
            //post with image
            deleteWithImage(pId,pImage);

        }
    }

    private void deleteWithImage(final String pId, String pImage) {
        //progress bar
        final ProgressDialog pd=new ProgressDialog(context);
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

                        Query fQuery= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds:snapshot.getChildren()){
                                    ds.getRef().removeValue();//remove values from database hwere pid matches
                                }
                                //deleted
                                Toast.makeText(context, "Deleted successfully....", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void deleteWithoutImage(String pId) {
        //progress bar
        final ProgressDialog pd=new ProgressDialog(context);
        pd.setMessage("Deleting...");
        Query fQuery= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    ds.getRef().removeValue();//remove values from database hwere pid matches
                }
                //deleted
                Toast.makeText(context, "Deleted successfully....", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //views from post row
        ImageView pImageTv,uPicTv;

        TextView uNameTv,pTimeTv,pTitleTv,pDescriptionTv,pLikesTv,pCommentsTv;
        ImageButton moreBtn;
        Button likeBtn,commentBtn,shareBtn;

        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);


            pImageTv=itemView.findViewById(R.id.pImageTv);
            uPicTv=itemView.findViewById(R.id.uPicTv);
            uNameTv=itemView.findViewById(R.id.uNameTv);
            pTimeTv=itemView.findViewById(R.id.pTimeTv);
            pTitleTv=itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv=itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv=itemView.findViewById(R.id.pLikesTv);
            moreBtn=itemView.findViewById(R.id.moreBtn);
            likeBtn=itemView.findViewById(R.id.likeBtn);
            commentBtn=itemView.findViewById(R.id.commentBtn);
            shareBtn=itemView.findViewById(R.id.shareBtn);
            profileLayout=itemView.findViewById(R.id.profileLayout);
            pCommentsTv=itemView.findViewById(R.id.pCommentsTv);



        }
    }
}
