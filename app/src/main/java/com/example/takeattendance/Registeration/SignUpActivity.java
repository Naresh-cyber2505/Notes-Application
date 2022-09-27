package com.example.takeattendance.Registeration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.takeattendance.Models.User;
import com.example.takeattendance.OneFragment;
import com.example.takeattendance.R;
import com.example.takeattendance.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.function.BiConsumer;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth auth;
    private String name;
    private String email;
    private String password;
    private String conPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = binding.fullName.getText().toString();
                email = binding.etEmail.getText().toString();
                password = binding.etPass.getText().toString();
                conPassword = binding.edConPass.getText().toString();

                if(name.isEmpty() && email.isEmpty())
                {
                    binding.errorMessage.setVisibility(View.VISIBLE);
                    binding.errorMessage.setText("full name and email address can't be empty");
//                    Toast.makeText(SignUpActivity.this, "full name and email address can't be empty", Toast.LENGTH_SHORT).show();
                }
                else if (password.isEmpty() && conPassword.isEmpty())
                {
                    binding.errorMessage.setVisibility(View.VISIBLE);
                    binding.errorMessage.setText("password and conform pass can't be empty");
//                    Toast.makeText(SignUpActivity.this, "password and conform pass can't be empty", Toast.LENGTH_SHORT).show();
                }
                else if (!password.equals(conPassword))
                {
                    binding.errorMessage.setVisibility(View.VISIBLE);
                    binding.errorMessage.setText("password has not matched");
//                    Toast.makeText(SignUpActivity.this, "password has not matched", Toast.LENGTH_SHORT).show();
                }
                else {
                    binding.errorMessage.setVisibility(View.GONE);
                    binding.progressBar.setVisibility(View.VISIBLE);

                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            String uid = task.getResult().getUser().getUid();

                            User user = new User(name,email,uid);

                            if (task.isSuccessful())
                            {
                                firebaseFirestore.collection("Users")
                                                .document(uid)
                                                .set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                {
                                                    binding.progressBar.setVisibility(View.GONE);
                                                    sendEmailVerificationToUser();
                                                }
                                            }
                                        });


//                                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
//                                startActivity(intent);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(SignUpActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        binding.signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
            }
        });



    }

    private void sendEmailVerificationToUser() {

        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser != null)
        {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {

                        Toast.makeText(SignUpActivity.this, "Verification email is sent, verify and login,", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        finish();
                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }
        else
        {
            Toast.makeText(SignUpActivity.this, " Failed to send Verification email.", Toast.LENGTH_SHORT).show();

        }

    }
}