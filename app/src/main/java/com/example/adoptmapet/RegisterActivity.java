package com.example.adoptmapet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText UserEmail, UserPassword, UserConfirmationPassword;
    private Button CreateAccountButton;

    private FirebaseAuth mAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mAuth = FirebaseAuth.getInstance();

        UserEmail =  findViewById(R.id.register_email);
        UserPassword =  findViewById(R.id.register_password);
        UserConfirmationPassword =  findViewById(R.id.confirm_password);
        CreateAccountButton =  findViewById(R.id.registerBtn);

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }





    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }


    private void CreateNewAccount() {

        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmPassword = UserConfirmationPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(getApplicationContext(), "Please write your email...", Toast.LENGTH_SHORT).show();

        }else if(password.isEmpty()){
            Toast.makeText(getApplicationContext(), "Please write your password...", Toast.LENGTH_SHORT).show();
        }else if(confirmPassword.isEmpty()){
            Toast.makeText(getApplicationContext(), "Please confirm your password...", Toast.LENGTH_SHORT).show();
        }else if(!password.equals(confirmPassword)){
            Toast.makeText(getApplicationContext(), "Not match...", Toast.LENGTH_SHORT).show();
        }else{

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                SendUserToSetupActivity();
                                Toast.makeText(getApplicationContext(), "You are authenticated successfully", Toast.LENGTH_SHORT).show();
                            }else{
                                String message = task.getException().getMessage();
                                Toast.makeText(getApplicationContext(), "Error occur: "+ message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(getApplicationContext(), SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

}