package com.example.adoptmapet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, CountryName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;
    StorageReference UserProfileImageRef;

    String currentUserID;
    Uri ImageUri;
    final static int Galley_Pick = 1;
    private String imageLink = null;

    private boolean imageRequired = false;
    private ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        UserName = findViewById(R.id.setup_username);
        FullName = findViewById(R.id.setup_fullname);
        CountryName = findViewById(R.id.setup_country_name);
        SaveInformationButton = findViewById(R.id.setup_information_button);
        ProfileImage = findViewById(R.id.setup_profile_image);

        final Handler handler = new Handler();
        progressBar = findViewById(R.id.progressBar);

        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageRequired==true) {
                    progressBar.setVisibility(View.VISIBLE);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 5s = 5000ms
                            progressBar.setVisibility(View.INVISIBLE);
                            SaveAccountSetupInformation();
                        }
                    }, 8000);



                }else {
                    Toast.makeText(SetupActivity.this, "Upload image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //user pick gallery
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(galleryIntent, Galley_Pick);
            }
        });

    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Galley_Pick && resultCode==RESULT_OK && data!=null){


            //image uri value is user picked image
            ImageUri = data.getData();

            Picasso.with(this).load(ImageUri).into(ProfileImage);

            uploadFile();
            imageRequired = true;



        }

    }

    //just get the file extension
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {

        if(ImageUri != null){

            //dito sya nag lalagay
            //palitan mo ang millli second
            //pinangalanan at nag lagay ng proper extension
            StorageReference fileReference = UserProfileImageRef.child(currentUserID
                    + "." + getFileExtension(ImageUri));

            fileReference.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(SetupActivity.this, "Upload success", Toast.LENGTH_SHORT).show();



                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();

                    imageLink = downloadUrl.toString().trim();




                    //SaveAccountSetupInformation();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SetupActivity.this, "Fail to upload", Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }

    }

    private void SaveAccountSetupInformation() {

        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(getApplicationContext(), "Please write username", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(fullname)){
            Toast.makeText(getApplicationContext(), "Please write fullname", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(country)){
            Toast.makeText(getApplicationContext(), "Please write country", Toast.LENGTH_SHORT).show();
        }else {

            HashMap usermap = new HashMap();
            usermap.put("username", username);
            usermap.put("fullname", fullname);
            usermap.put("country", country);


            usermap.put("profileimage", imageLink);

            usermap.put("status", "Hey there, i am using Poster Social network , Gio Coser");
            usermap.put("gender", "none");
            usermap.put("dob","none" );
            usermap.put("relationshipstatus", "none");

            UserRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        SendUserToMainActivity();
                        Toast.makeText(getApplicationContext(), "Your account is created successfully", Toast.LENGTH_LONG).show();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(getApplicationContext(), "Error occured"   + message, Toast.LENGTH_LONG).show();
                    }
                }

            });


        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }
}