package com.jether.nrmtuwaaye.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jether.nrmtuwaaye.Models.ModelGroupChat;
import com.jether.nrmtuwaaye.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat>{

    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;

    Context context;
    private ArrayList<ModelGroupChat> modelGroupChats;

    private FirebaseAuth firebaseAuth;

    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> modelGroupChats) {
        this.context = context;
        this.modelGroupChats = modelGroupChats;

        firebaseAuth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row

        if(viewType==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(context).inflate(R.layout.row_groupchat_right,parent,false);
            return new HolderGroupChat(view);
        }else{
            View view= LayoutInflater.from(context).inflate(R.layout.row_groupchat_lrft,parent,false);
            return new HolderGroupChat(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {
        //get data
        ModelGroupChat model=modelGroupChats.get(position);
        String timestamp=model.getTimestamp();
        String message=model.getMessage();
        String senderUid=model.getSender();
        String messageType=model.getType();

        //convert the time stamp to dd/MM/yyyy hh:mm am/pm

        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));

        String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        //set data
        if(messageType.equals("text")){
            //text message/hide ImageView
            holder.imageTv.setVisibility(View.GONE);
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageTv.setText(message);

        }else{
            //image message,hide text message
            holder.imageTv.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);

            try{
                Picasso.get().load(message).placeholder(R.drawable.ic_blackimage).into(holder.imageTv);
            }catch(Exception e){
                holder.imageTv.setImageResource(R.drawable.ic_blackimage);

            }

        }

        //set data

        holder.timeTv.setText(dateTime);

        setUsername(model,holder);

    }

    private void setUsername(ModelGroupChat model, final HolderGroupChat holder) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()) {
                            String name=""+ds.child("name").getValue();
                            holder.nameTv.setText(name);

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelGroupChats.size();
    }

    @Override
    public int getItemViewType(int position){
        if(modelGroupChats.get(position).getSender().equals(firebaseAuth.getUid())){
            return MSG_TYPE_RIGHT;

        }else{
            return MSG_TYPE_LEFT;

        }
    }


    class HolderGroupChat extends RecyclerView.ViewHolder{

        private TextView nameTv,messageTv,timeTv;
        private ImageView imageTv;
        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);

            nameTv=itemView.findViewById(R.id.nameTv);
            messageTv=itemView.findViewById(R.id.messageTv);
            timeTv=itemView.findViewById(R.id.timeTv);
            imageTv=itemView.findViewById(R.id.imageTv);
        }
    }
}
