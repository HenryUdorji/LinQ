package com.codemountain.slicker.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codemountain.slicker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText regEmail, regPassword, regName;
    private Button registerBtn;
    private ProgressBar loadingProcess;
    private TextView loginTextView;
    private ImageView regImageView;
    private ProgressDialog loadingProgress;

    private Toolbar toolbar;

    //Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        //Init views
        regEmail = findViewById(R.id.regEmail);
        regPassword = findViewById(R.id.regPassword);
        regName = findViewById(R.id.regName);
        registerBtn = findViewById(R.id.registerBtn);
        loadingProcess = findViewById(R.id.regProgressBar);
        loginTextView = findViewById(R.id.loginTextView);
        regImageView = findViewById(R.id.registerImageView);

        //setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        loadingProgress = new ProgressDialog(this);
        loadingProgress.setMessage("Registering user");

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = regEmail.getText().toString().trim();
                String password = regPassword.getText().toString().trim();
                String name = regName.getText().toString().trim();

                //Validate
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

                    regEmail.setError("Invalid Email");
                    regEmail.setFocusable(true);
                }
                else if(password.length() < 6){

                    regPassword.setError("Password length too short");
                    regPassword.setFocusable(true);

                }
                else if(TextUtils.isEmpty(name)){
                    regName.setError("Name cannot be empty");
                    regName.setFocusable(true);
                }
                else {

                    //Register users
                    registerUser(email, password, name);

                }
            }
        });

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    /**
     * Method for registering users
     * @param email
     * @param password
     */
    private void registerUser(String email, String password, final String name) {

        loadingProgress.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            loadingProgress.dismiss();

                            final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                            String lastSeen = String.valueOf(System.currentTimeMillis());
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            if (currentUser != null) {
                                String email = currentUser.getEmail();
                                String uid = currentUser.getUid();

                                HashMap<Object, String> regHashMap = new HashMap<>();
                                regHashMap.put("email", email);
                                regHashMap.put("uid", uid);
                                regHashMap.put("name", name);
                                regHashMap.put("phone", "phone Number");
                                regHashMap.put("image", "");
                                regHashMap.put("online_status", "online");
                                regHashMap.put("typing_to", "noOne");
                                regHashMap.put("cover_image", "");
                                regHashMap.put("bio", "Hi there i am using LinQ");
                                regHashMap.put("registration_date", currentDate);
                                regHashMap.put("date_of_birth", "Date of birth");
                                regHashMap.put("location", "Location");

                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                DatabaseReference reference = firebaseDatabase.getReference("Users");
                                reference.child(uid).setValue(regHashMap);

                                Toast.makeText(RegisterActivity.this, "Registration complete", Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                                finish();
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        loadingProgress.dismiss();
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
