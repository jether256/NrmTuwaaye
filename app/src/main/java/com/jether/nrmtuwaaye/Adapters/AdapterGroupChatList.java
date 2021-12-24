package com.jether.nrmtuwaaye.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jether.nrmtuwaaye.GroupChatActivity;
import com.jether.nrmtuwaaye.Models.ModelGroupChatList;
import com.jether.nrmtuwaaye.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.HolderGroupChatList> {

    private Context context;
    private ArrayList<ModelGroupChatList> groupChatLists;

    public AdapterGroupChatList(Context context, ArrayList<ModelGroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row
        View view= LayoutInflater.from(context).inflate(R.layout.row_groupchats,parent,false);
        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChatList holder, int position) {

        //get data
        ModelGroupChatList modelGroupChatList=groupChatLists.get(position);
        final String gropId=modelGroupChatList.getGroupId();
        String gropIcon=modelGroupChatList.getGroupIcon();
        String gropTitle=modelGroupChatList.getGroupTitle();


        holder.nameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");

        //load last message and message-time
        loadLastMessage(modelGroupChatList,holder);


        //set data
        holder.groupTitle.setText(gropTitle);

        try{
            Picasso.get().load(gropIcon).placeholder(R.drawable.ic_gulupu).into(holder.groupIconTv);

        }catch (Exception e){
            holder.groupIconTv.setImageResource(R.drawable.ic_gulupu);

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent= new Intent(context, GroupChatActivity.class);
            intent.putExtra("groupId",gropId);
            context.startActivity(intent);
            }
        });
    }

    private void loadLastMessage(ModelGroupChatList modelGroupChatList, final HolderGroupChatList holder) {
        //get last message from Group
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(modelGroupChatList.getGroupId()).child("Messages").limitToLast(1)//get last item(message) from that child
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){

                            //get data
                            String message=""+ds.child("message").getValue();
                            String timestamp=""+ds.child("timestamp").getValue();
                            String sender=""+ds.child("sender").getValue();
                            String messageType=""+ds.child("type").getValue();

                            Calendar cal=Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

                            if(messageType.equals("image")){
                                holder.messageTv.setText("Sent a photo");
                            }else{
                                holder.messageTv.setText(message);

                            }
                            holder.timeTv.setText(dateTime);


                            //get last message

                            DatabaseReference ref1=FirebaseDatabase.getInstance().getReference("Users");
                            ref1.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for(DataSnapshot ds:snapshot.getChildren()){
                                                String name=""+ds.child("name").getValue();
                                                holder.nameTv.setText(name);

                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    class HolderGroupChatList extends RecyclerView.ViewHolder{

        private ImageView groupIconTv;
        private TextView groupTitle,nameTv,messageTv,timeTv;

        public HolderGroupChatList(@NonNull View itemView) {
            super(itemView);

            groupIconTv=itemView.findViewById(R.id.groupIconTv);
            groupTitle=itemView.findViewById(R.id.groupTitle);
            nameTv=itemView.findViewById(R.id.nameTv);
            messageTv=itemView.findViewById(R.id.messageTv);
            timeTv=itemView.findViewById(R.id.timeTv);

                  }
    }
}
