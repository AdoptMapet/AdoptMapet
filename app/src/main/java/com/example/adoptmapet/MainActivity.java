package com.example.adoptmapet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private RecyclerView postList;
    private RecyclerAdapter recyclerAdapter;
    private Context pContext;

    private Toolbar mToolbar;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton AddNewPostButton;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, myRef;

    private ArrayList<Posts> postsArrayList;

    String currentUserID;

    String text;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myRef = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        postsArrayList = new ArrayList<>();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        AddNewPostButton = findViewById(R.id.add_new_post_button);


        drawerLayout = findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Navigation header
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        postList = findViewById(R.id.all_users_post_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        postList.setLayoutManager(linearLayoutManager);
        postList.setHasFixedSize(true);




        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = navView.findViewById(R.id.nav_user_full_name);



        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("fullname"))
                    {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        //display to navigation from database
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    //in databae
                    String fullname = snapshot.child("fullname").getValue().toString();
                    String image = snapshot.child("profileimage").getValue().toString();

                    NavProfileUserName.setText(fullname);
                    Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.profile).into(NavProfileImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                UserMenuSelector(item);

                return false;
            }

        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToPostActivity();
            }
        });

        //Clear array list
        ClearAll();

        //Display post
        DisplayAllUsersPost();

    }

    private void DisplayAllUsersPost() {

        Query query = myRef.child("Posts");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ClearAll();

                for (DataSnapshot dataSnapshot: snapshot.getChildren() ){

                        //itong mga post value  ay mai babato sa RecyclerAdapter.java
                         Posts posts = new Posts();
                         posts.setUid(dataSnapshot.child("uid").getValue().toString());
                         posts.setDate(dataSnapshot.child("date").getValue().toString());
                         posts.setTime(dataSnapshot.child("time").getValue().toString());
                         posts.setDescription(dataSnapshot.child("description").getValue().toString());
                         posts.setProfileimage(dataSnapshot.child("profileimage").getValue().toString());
                         posts.setPostimage(dataSnapshot.child("postimage").getValue().toString());
                         posts.setFullname(dataSnapshot.child("fullname").getValue().toString());
                         postsArrayList.add(posts);
}

                recyclerAdapter = new RecyclerAdapter(getApplicationContext(), postsArrayList);
                postList.setAdapter(recyclerAdapter);
                recyclerAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //Ensure that our list is empty
    private void ClearAll(){
        if (postsArrayList != null){
            postsArrayList.clear();

            if (recyclerAdapter != null){
                recyclerAdapter.notifyDataSetChanged();
            }
        }

        postsArrayList = new ArrayList<>();

    }




    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent (getApplicationContext(), PostActivity.class);
        startActivity(addNewPostIntent);

    }


    //once app run this onStart will run
    //Check if login already else back to login
    @Override
    protected void onStart() {

        super.onStart();

        //get current user or it will check if you arleary log-in
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            SendUserToLoginActivity();
        }else{
            CheckUserExistence();
        }


    }

    private void CheckUserExistence() {

        final String current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if record not exist in realtimedatabase
                if(!dataSnapshot.hasChild(current_user_id)){
                    //most important in app
                    Toast.makeText(getApplicationContext(), "Setup Activity", Toast.LENGTH_SHORT).show();
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });






    }

    private void SendUserToSetupActivity() {
        Intent SetupIntent = new Intent(getApplicationContext(), SetupActivity.class);
        SetupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(SetupIntent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent LoginIntent = new Intent(MainActivity.this, LoginActivity.class);
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
        finish();
    }


    //this is the function to perform burger button is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //somebody click 3 lines in toolbar, actionBarDrawer will open
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void UserMenuSelector(MenuItem item) {



        switch (item.getItemId()) {
            case R.id.nav_post:
                SendUserToPostActivity();
                break;
            case R.id.nav_profile:
                Toast.makeText(getApplicationContext(), "Profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                Toast.makeText(getApplicationContext(), "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                Toast.makeText(getApplicationContext(), "Friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_message:
                Toast.makeText(getApplicationContext(), "Message", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_setting:
                SendUserToSettingsActivity();
                break;

            case R.id.nav_logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }

    private void SendUserToSettingsActivity() {
        Intent SettingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(SettingsIntent);
    }

}

