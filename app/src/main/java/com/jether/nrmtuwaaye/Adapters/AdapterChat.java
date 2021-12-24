package com.jether.nrmtuwaaye.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jether.nrmtuwaaye.Models.ModelChat;
import com.jether.nrmtuwaaye.R;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;

    Context context;

    List<ModelChat> chatList;

    String imageUrl;

    FirebaseUser fUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layouts:row_chat_left:xml for receiver,row chat right xml for sender
        if(viewType==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
            return new MyHolder(view);

        }else{
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return new MyHolder(view);

        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {

        //get data
        String message=chatList.get(position).getMessage();
        String timeStamp =chatList.get(position).getTimestamp();
        String type =chatList.get(position).getType();

        //convert the time stamp to dd/MM/yyyy hh:mm am/pm

        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));

        String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        if(type.equals("text")){
            //text message
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.GONE);


            holder.messageTv.setText(message);


        }else{

            //image message
            holder.messageTv.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);

            Picasso.get().load(message).placeholder(R.drawable.ic_blackimage).into(holder.messageImage);

        }

        //set holder
        holder.messageTv.setText(message);
        holder.timeTv.setText(dateTime);
        try{

            Picasso.get().load(imageUrl).into(holder.profileTv);
        }catch(Exception e){

        }
        //click to show delete dialog
        holder.messageLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show delete message to confirm dialog

                AlertDialog.Builder builder= new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete this message?");

                //delete button
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                });
                //cancel delete
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dialog dismiss
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        //set seen /delivered status of the message
        if(position==chatList.size()-1){
            if(chatList.get(position).isSeen()){
                holder.seenTv.setText("Seen");
            }else{
                holder.seenTv.setText("Delivered");
            }

        }else{
            holder.seenTv.setVisibility(View.GONE);
        }


    }

    private void deleteMessage(int position) {
        final String myUID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        /*LOgic
         *Get timestamp of clicked message
         *Compare timestamp of clicked message to all message in the chats
         *Where both values match delete that message
         *This will allow sender to delete his and receivers message
         * */

        String msgTimeStamp=chatList.get(position).getTimestamp();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Chats");
        Query query=ref.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                /*if you want to allow the sender to delete only his message then compare senders value with his current uid
                if they match its the message of  sender that is trying to delete
                */

                    if(ds.child("sender").getValue().equals(myUID)){

                        /* We can do one of the following
                         *1 remove message from the chats
                         *2 set the valu of message "THis message is deleted"
                         * Do wat you want
                         */
                        //delete message from chats
                        //ds.getRef().removeValue();
                        //set the value of message"This message was deleted...."
                        HashMap<String,Object> hashMap= new HashMap<>();
                        hashMap.put("message"," message deleted....");
                        ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context, "Message deleted....", Toast.LENGTH_SHORT).show();

                    }else{

                        Toast.makeText(context, "You can delete only your messages....", Toast.LENGTH_SHORT).show();

                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //currently signed in user
        fUser= FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }

    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView profileTv,messageImage;
        TextView timeTv,seenTv,messageTv;
        LinearLayout messageLay;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileTv=itemView.findViewById(R.id.profileTv);
            timeTv=itemView.findViewById(R.id.timeTv);
            seenTv=itemView.findViewById(R.id.seenTv);
            messageTv=itemView.findViewById(R.id.messageTv);
            messageLay=itemView.findViewById(R.id.messageLayout);
            messageImage=itemView.findViewById(R.id.messageImage);
        }
    }
}
