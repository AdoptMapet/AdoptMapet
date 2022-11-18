package com.example.adoptmapet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;


    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;

    private static final int Galley_Pick = 1;
    private Uri ImageUri;
    String Description;

    private StorageReference PostImagesReference;
    private DatabaseReference UserRef, Postref;
    private FirebaseAuth mAuth;

    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;

    private String imageLink = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        PostImagesReference = FirebaseStorage.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        Postref = FirebaseDatabase.getInstance().getReference().child("Posts");

        SelectPostImage = findViewById(R.id.select_post_image);
        UpdatePostButton = findViewById(R.id.update_post_button);
        PostDescription = findViewById(R.id.post_description);



        mToolbar = findViewById(R.id.post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Post");


        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidatePostInfo();
            }
        });



    }

    private void ValidatePostInfo() {

        Description = PostDescription.getText().toString();
        if (ImageUri==null){
            Toast.makeText(this, "Please select post image", Toast.LENGTH_SHORT).show();
        }

        else if (Description.isEmpty()){
            Toast.makeText(this, "Please say something about post image", Toast.LENGTH_SHORT).show();
        }
        else{


            StoringImageToFirebaseStorage();
        }


    }

    private void StoringImageToFirebaseStorage() {

        //date
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        //time
        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        postRandomName = saveCurrentDate+saveCurrentTime;

        //firebase store path
        StorageReference filePath = PostImagesReference.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");




        filePath.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful());
                Uri downloadUrl = urlTask.getResult();
                imageLink = downloadUrl.toString().trim();

                Toast.makeText(PostActivity.this, "Image uploaded successfully to storage ", Toast.LENGTH_SHORT).show();
                SavingPostInformationToDatabase();
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                   Toast.makeText(PostActivity.this, "Failed upload ", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void SavingPostInformationToDatabase() {

        UserRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    //geting data from firebase
                    String userFullName = snapshot.child("fullname").getValue().toString();

                    String userProfileImage = snapshot.child("profileimage").getValue().toString();



                    HashMap postMap = new HashMap();
                    postMap.put("uid", current_user_id);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("description", Description);
                    postMap.put("postimage", imageLink);
                    postMap.put("profileimage", userProfileImage);
                    postMap.put("fullname", userFullName);

                    Postref.child(current_user_id + postRandomName).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                SendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "New Post is updated successfully", Toast.LENGTH_SHORT).show();

                            }else{
                                Toast.makeText(PostActivity.this, "Error Occured while updating your post ", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void OpenGallery() {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(galleryIntent, Galley_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==Galley_Pick && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainIntent);
    }


}