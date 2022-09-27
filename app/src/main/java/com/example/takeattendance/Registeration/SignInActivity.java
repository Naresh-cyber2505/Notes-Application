package com.example.takeattendance.Registeration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.takeattendance.Fragments.TwoFragment;
import com.example.takeattendance.MainActivity;
import com.example.takeattendance.OneFragment;
import com.example.takeattendance.R;
import com.example.takeattendance.databinding.ActivitySignInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        if (firebaseUser != null)
        {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        binding.forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForgetPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = binding.etEmail.getText().toString();
                String password = binding.etPass.getText().toString();

                if(password.isEmpty() && email.isEmpty())
                {
                    binding.errorMessage.setVisibility(View.VISIBLE);
                    binding.errorMessage.setText("email and password can't be empty");
//                    Toast.makeText(SignUpActivity.this, "full name and email address can't be empty", Toast.LENGTH_SHORT).show();
                }
                else {

                    binding.errorMessage.setVisibility(View.GONE);
                    binding.progressBar.setVisibility(View.VISIBLE);

                    auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful())
                            {
                                checkEmailVerifiedOrNot();
                            }
                            else
                            {
                                Toast.makeText(SignInActivity.this, "Account does not exist.", Toast.LENGTH_SHORT).show();

                            }


                        }
                    });

                }
            }
        });



        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });


    }

    private void checkEmailVerifiedOrNot() {

        FirebaseUser fireUser = auth.getCurrentUser();

        if (fireUser.isEmailVerified() == true)
        {

            Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        else
        {
            Toast.makeText(this, "Verify your email first", Toast.LENGTH_SHORT).show();
            auth.signOut();
            binding.progressBar.setVisibility(View.GONE);
        }


    }
}