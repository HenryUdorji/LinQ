package com.codemountain.slicker.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.codemountain.slicker.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private String myUid;
    private DatabaseReference reference;


    private Toolbar toolbar;
    private EditText nameEditText, bioEditText, locationEditText, phoneNumberEditText, dobEditText;
    private String name, bio, location, phoneNumber, dob;
    private String intentName, intentPhone, intentProfileBio, intentLocation, intentBirthDate;
    private ProgressDialog progressDialog;
    private Calendar calendar;
    private CountryCodePicker countryCodePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Intent intent = getIntent();
        intentName = intent.getStringExtra("NAME");
        intentPhone = intent.getStringExtra("PHONE_NUMBER");
        intentLocation = intent.getStringExtra("LOCATION");
        intentBirthDate = intent.getStringExtra("DOB");
        intentProfileBio = intent.getStringExtra("BIO");

        //Init Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit profile");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameEditText = findViewById(R.id.editName);
        bioEditText = findViewById(R.id.editBio);
        locationEditText = findViewById(R.id.editLocation);
        phoneNumberEditText = findViewById(R.id.editPhoneNumber);
        dobEditText = findViewById(R.id.editDob);
        countryCodePicker = findViewById(R.id.ccp);

        //calling a datePickerDialog for setting the date of birth
        calendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateEditText();
            }
        };
        dobEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(EditProfileActivity.this, date, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        nameEditText.setText(intentName);
        bioEditText.setText(intentProfileBio);
        locationEditText.setText(intentLocation);
        phoneNumberEditText.setText(intentPhone);
        dobEditText.setText(intentBirthDate);
        countryCodePicker.registerCarrierNumberEditText(phoneNumberEditText);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Updating your profile");

    }

    private void updateEditText() {
        String format = "yyyy/MM/dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.US);
        dobEditText.setText(simpleDateFormat.format(calendar.getTime()));
    }

    private void updatingProfile() {
        name = nameEditText.getText().toString();
        bio = bioEditText.getText().toString();
        location = locationEditText.getText().toString();
        phoneNumber = phoneNumberEditText.getText().toString();
        dob = dobEditText.getText().toString();

        if (TextUtils.isEmpty(name)){
            Toast.makeText(this, "Name field cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog.show();
            HashMap<String, Object> updateHashMap = new HashMap();
            updateHashMap.put("name", name);
            updateHashMap.put("phone", phoneNumber);
            updateHashMap.put("date_of_birth", dob);
            updateHashMap.put("location", location);
            updateHashMap.put("bio", bio);

            reference.child(currentUser.getUid()).updateChildren(updateHashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }

    }

    private void checkUserStatus() {
        currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //User is signed in stay here

            myUid = currentUser.getUid();
        }
        else {

            startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {

        checkUserStatus();
        //checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Save changes");
            builder.setMessage("Do you want to save changes");
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    updatingProfile();
                }
            }).setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent goBack = new Intent(EditProfileActivity.this, DashboardActivity.class);
                    startActivity(goBack);
                    finish();
                }
            }).show();

        }
        if (item.getItemId() == R.id.action_save){
            updatingProfile();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save changes");
        builder.setMessage("Do you want to save changes");
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updatingProfile();
            }
        }).setNegativeButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent goBack = new Intent(EditProfileActivity.this, DashboardActivity.class);
                startActivity(goBack);
                finish();
            }
        }).show();
    }
}
