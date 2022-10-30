package com.example.adoptmapet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {


    private ImageView PostImage;
    private TextView PostDescription;
    private Button DeletePostButton, EditPostButton;
    private String PostKey;
    private DatabaseReference ClickPostRef;

    private FirebaseAuth mAuth;
    private String currentUserID;
    //infos to add
    private String databaseUserId, image, description;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        //Getting the post key in recycler onclick
        PostKey = getIntent().getExtras().get("POSTKEY").toString();
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        PostImage = findViewById(R.id.click_post_image);
        PostDescription = findViewById(R.id.click_post_description);
        EditPostButton = findViewById(R.id.edit_post_button);
        DeletePostButton = findViewById(R.id.delete_post_button);

        EditPostButton.setVisibility(View.INVISIBLE);
        DeletePostButton.setVisibility(View.INVISIBLE);



        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                image = snapshot.child("postimage").getValue().toString();
                description = snapshot.child("description").getValue().toString();
                databaseUserId = snapshot.child("uid").getValue().toString();

                PostDescription.setText(description);
                Picasso.with(ClickPostActivity.this).load(image).into(PostImage);

                //MOst important part, if you are the owner of post , you can manipulate it
                if(currentUserID.equals(databaseUserId)){
                    EditPostButton.setVisibility(View.VISIBLE);
                    DeletePostButton.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}