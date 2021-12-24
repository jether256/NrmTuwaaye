package com.jether.nrmtuwaaye.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jether.nrmtuwaaye.ChatActivity;
import com.jether.nrmtuwaaye.Models.ModelUser;
import com.jether.nrmtuwaaye.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder> {

    Context context;
    List<ModelUser> userList;//get user info
    private HashMap<String,String> lastMessageMap;

    public AdapterChatList(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row
        View view= LayoutInflater.from(context).inflate(R.layout.row_chalist,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        final String hisUid =userList.get(position).getUid();
        String userImage=userList.get(position).getImage();
        String userName=userList.get(position).getName();
        String lastMessage=lastMessageMap.get(hisUid);

        //set data
        holder.nameTv.setText(userName);
        if(lastMessage==null || lastMessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);

        }else{
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }


        try{
            Picasso.get().load(userImage).placeholder(R.drawable.ic_face_yellow).into(holder.profileTv);

        }catch(Exception e){
            Picasso.get().load(R.drawable.ic_face_yellow).into(holder.profileTv);

        }


        //set online status of others on chatlist
        if(userList.get(position).getOnlineStatus().equals("online")){
            //online

            holder.onlineStatusTv.setImageResource(R.drawable.circle_online);
        }else{
            //offline

            holder.onlineStatusTv.setImageResource(R.drawable.circle_oofline);

        }

        //handle click of chatlist
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start activity with that user
                Intent intent= new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",hisUid);
                context.startActivity(intent);
            }
        });


    }


    public void setLastMessageMap(String userId,String lastMessage){
        lastMessageMap.put(userId,lastMessage);

    }

    @Override
    public int getItemCount() {
        return userList.size();//size of list
    }

    class MyHolder extends RecyclerView.ViewHolder{


        ImageView profileTv,onlineStatusTv;
        TextView nameTv,lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileTv=itemView.findViewById(R.id.profileTv);
            onlineStatusTv=itemView.findViewById(R.id.onlineStatusTv);
            nameTv=itemView.findViewById(R.id.nameTv);
            lastMessageTv=itemView.findViewById(R.id.lastMessageTv);
        }




    }
}
