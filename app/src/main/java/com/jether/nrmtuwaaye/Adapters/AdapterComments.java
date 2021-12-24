package com.jether.nrmtuwaaye.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jether.nrmtuwaaye.Models.ModelComment;
import com.jether.nrmtuwaaye.R;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder> {

    Context context;
    List<ModelComment> commentList;

    String myUid,postId;

    public AdapterComments(Context context, List<ModelComment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_comments,parent,false);
        return  new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data

        final String uid=commentList.get(position).getUid();
        String name=commentList.get(position).getuName();
        String email=commentList.get(position).getUEmail();
        String image=commentList.get(position).getuDp();
        final String cid=commentList.get(position).getcId();
        String comment=commentList.get(position).getComment();
        String timeStamp=commentList.get(position).getTimestamp();


        //convert the time stamp to dd/MM/yyyy hh:mm am/pm

        Calendar calendar=Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));

        String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();


        //set data
        holder.nameTv111.setText(name);
        holder.commenttv111.setText(comment);
        holder.timeTv111.setText(pTime);

        //set user dp
        try{

            Picasso.get().load(image).placeholder(R.drawable.ic_face_yellow).into(holder.Avatar22);

        }catch(Exception e){

        }


        //comment click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if comment is currently signed in user
                if(myUid.equals(uid)){

                    //my comment
                    //show delete dialog

                    AlertDialog.Builder builder= new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setTitle("Delete..");
                    builder.setMessage("Are you sure you want to delete?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            deleteComment(cid);

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //dismiss
                            dialogInterface.dismiss();

                        }
                    });
                    //show
                    builder.create().show();


                }else{
                    //no my comment
                    Toast.makeText(context, "Can't delete other's comment...", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void deleteComment(String cid) {
        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue();//it wil delete the comment


        //now update the comment count
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments=""+snapshot.child("pComments").getValue();
                int newCommentBal= Integer.parseInt(comments) - 1;
                ref.child("pComments").setValue(""+newCommentBal);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //declare views from orw comments
        TextView nameTv111,commenttv111,timeTv111;
        ImageView Avatar22;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            nameTv111=itemView.findViewById(R.id.nameTv111);
            commenttv111=itemView.findViewById(R.id.commenttv111);
            timeTv111=itemView.findViewById(R.id.timeTv111);
            Avatar22=itemView.findViewById(R.id.Avatar22);

        }
    }
}
