package com.example.adoptmapet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

private static final String Tag = "RecyclerView";
private Context pConext;
private ArrayList<Posts> postsArrayList;

    private DatabaseReference PostsRef;

    public RecyclerAdapter(Context pConext, ArrayList<Posts> postsArrayList) {
        this.pConext = pConext;
        this.postsArrayList = postsArrayList;
    }


    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//put them into template or layout

        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_post_layout, parent, false);

        return new ViewHolder(view);

    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //ImageView :glide
        Glide.with(pConext).load(postsArrayList.get(position).getPostimage()).into(holder.postimage);
        Glide.with(pConext).load(postsArrayList.get(position).getProfileimage()).into(holder.profileimage);
        //TextView
        holder.fullname.setText(postsArrayList.get(position).getFullname());
        holder.date.setText(postsArrayList.get(position).getDate());
        holder.time.setText(postsArrayList.get(position).getTime());
        holder.description.setText(postsArrayList.get(position).getDescription());


        final String uid =  postsArrayList.get(position).getUid();
        final String strDate =  postsArrayList.get(position).getDate();
        final String strTime =  postsArrayList.get(position).getTime();


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String postKey = uid+strDate+strTime.toString();

                //goes to new activity passing the item name
                Intent intent = new Intent(holder.itemView.getContext(), ClickPostActivity.class);

                //put text into a bundle and add to intent
                intent.putExtra("POSTKEY", postKey);

                //begin activity
                holder.itemView.getContext().startActivity(intent);


            }
        });

    }

    @Override
    public int getItemCount() {
        return postsArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

    //Widgets
    CircleImageView profileimage;
    ImageView postimage;
    TextView time,date, description, fullname;



    public ViewHolder(@NonNull View itemView) {
        super(itemView);


        profileimage = itemView.findViewById(R.id.post_profile_image);
        postimage = itemView.findViewById(R.id.post_image);
        date = itemView.findViewById(R.id.post_date);
        time = itemView.findViewById(R.id.post_time);
        description = itemView.findViewById(R.id.post_description);
        fullname = itemView.findViewById(R.id.post_full_name);



    }
}


}
