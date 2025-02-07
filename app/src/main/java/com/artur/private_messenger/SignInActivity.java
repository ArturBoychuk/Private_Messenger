package com.artur.private_messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.artur.private_messenger.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    private FirebaseAuth auth;

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;
    private EditText nameEditText;
    private TextView toggleLoginSignUpTextView;
    private Button loginSignUpButton;

    private boolean loginModeActive;

    FirebaseDatabase database;
    DatabaseReference usersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersDatabaseReference = database.getReference().child("users");

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        nameEditText = findViewById(R.id.nameEditText);
        toggleLoginSignUpTextView = findViewById(R.id.toggleLoginSignUpTextView);
        loginSignUpButton = findViewById(R.id.loginSignUpButton);
        repeatPasswordEditText = findViewById(R.id.repeatPasswordEditText);

        loginSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginSignUpUser(emailEditText.getText().toString().trim(),
                        passwordEditText.getText().toString().trim());
            }
        });
            if(auth.getCurrentUser() != null){
                startActivity(new Intent(SignInActivity.this, UserListActivity.class));
            }
    }

    private void loginSignUpUser(String email, String password){

        if (loginModeActive) {
            if(validLoginUserFieldsPass()){
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    setUserNameInChat(nameEditText.getText().toString().trim());
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = auth.getCurrentUser();
                                    Intent intent = new Intent(SignInActivity.this, UserListActivity.class);
                                    startActivity(intent);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(SignInActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }); }
        }
        else {
            if (validCreateUserFieldsPass()) {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = auth.getCurrentUser();
                                    createUser(user);
                                    setUserNameInChat(nameEditText.getText().toString().trim());
                                    Intent intent = new Intent(SignInActivity.this, UserListActivity.class);
                                    startActivity(intent);

                                } else {
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(SignInActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    private void createUser(FirebaseUser firebaseUser) {
        User user = new User();
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setName(nameEditText.getText().toString().trim());


        usersDatabaseReference.push().setValue(user);
    }


    public void toggleLoginMode(View view){
        if (loginModeActive){
            loginModeActive = false;
            loginSignUpButton.setText("Sign Up");
            toggleLoginSignUpTextView.setText("Or, log in");
            repeatPasswordEditText.setVisibility(View.VISIBLE);
            nameEditText.setVisibility(View.VISIBLE);
        }
        else{
            loginModeActive = true;
            loginSignUpButton.setText("Login");
            toggleLoginSignUpTextView.setText("Or, sign up");
            repeatPasswordEditText.setVisibility(View.GONE);
        }
    }

    public boolean validCreateUserFieldsPass(){
        if (!passwordEditText.getText().toString().equals(repeatPasswordEditText.getText().toString())){
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(passwordEditText.getText().toString().length() < 7){
            Toast.makeText(this, "Passwords should be at least 7 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(emailEditText.getText().toString().isEmpty()){
            Toast.makeText(this, "Please input your email", Toast.LENGTH_SHORT).show();
            return false;
        }
        else{
            return true;
        }
    }

    public boolean validLoginUserFieldsPass(){
        if(passwordEditText.getText().toString().length() < 7){
            Toast.makeText(this, "Passwords should be at least 7 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(emailEditText.getText().toString().isEmpty()){
            Toast.makeText(this, "Please input your email", Toast.LENGTH_SHORT).show();
            return false;
        }
        else{
            return true;
        }
    }

   public void setUserNameInChat(String userName){
       Intent intent = new Intent(SignInActivity.this, ChatActivity.class);
       intent.putExtra("userName",  userName);
       startActivity(intent);
   }



}
