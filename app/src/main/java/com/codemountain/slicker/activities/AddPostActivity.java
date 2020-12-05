package com.codemountain.slicker.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.codemountain.slicker.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class AddPostActivity extends AppCompatActivity {

    private EditText postDescription;
    private Button sendPostButton;
    private ImageView postImage;
    private Toolbar toolbar;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private StorageReference storageReference;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;

    private String storagePath = "Users_Post_Images/";

    private ProgressDialog loadingProgress;

    private Uri imageUri = null;
    private String name, email, dp, uid;

    //Permissions
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    String cameraPermissions[];
    String storagePermissions[];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add new post");
        getSupportActionBar().setSubtitle(currentUser.getEmail());
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Init views
        postDescription = findViewById(R.id.postDescription);
        postImage = findViewById(R.id.postImage);
        sendPostButton = findViewById(R.id.sendPostButton);

        //Init Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageReference = getInstance().getReference();
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("Users");
        reference.keepSynced(true);

        Query query = reference.orderByChild("email").equalTo(currentUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    name = ds.child("name").getValue().toString();
                    email = ds.child("email").getValue().toString();
                    dp = ds.child("image").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddPostActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        loadingProgress = new ProgressDialog(this);
        loadingProgress.setMessage("Publishing post...");

        sendPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = postDescription.getText().toString().trim();
                if(TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this, "Add a description", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (imageUri == null) {
                    uploadData(description, "noImage");
                }
                else {
                    uploadData(description, String.valueOf(imageUri));
                }
            }
        });

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });
    }

    //Method for uploading the images gotten from both CAMERA & GALLERY to firebase
    private void uploadData(final String description, String uri){

        loadingProgress.show();
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;

        //Post with image
        if (!uri.equals("noImage")){
            if(postImage.getDrawable() != null){

                /**
                 * Compressing the Image gotten from either CAMERA OR GALLERY
                 * The compressed image would be uploaded to Firebase storage
                 * so that the recyclerView would load post with images faster.
                 */
                Bitmap bitmap = null;
                try{
                    bitmap = ((BitmapDrawable)postImage.getDrawable()).getBitmap();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                //Converting the bitmap to byte  array
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                assert bitmap != null;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
                final byte[] bitmapBytes = byteArrayOutputStream.toByteArray();

                final StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                ref.putBytes(bitmapBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Image uploaded to firebase storage
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();

                        if(uriTask.isSuccessful()){
                            HashMap<Object, String> postHashMap = new HashMap<>();
                            postHashMap.put("uid", uid);
                            postHashMap.put("uName", name);
                            postHashMap.put("uEmail", email);
                            postHashMap.put("uDp", dp);
                            postHashMap.put("pId", timeStamp);
                            postHashMap.put("pDescription", description);
                            postHashMap.put("pImage", downloadUri);
                            postHashMap.put("pTime", timeStamp);
                            postHashMap.put("pLikes", "0");
                            postHashMap.put("pComments", "0");

                            //Path to store post data
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                            reference.child(timeStamp).setValue(postHashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    loadingProgress.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();

                                    //Reset views
                                    postDescription.setText("");
                                    postImage.setImageURI(null);
                                    imageUri = null;
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loadingProgress.dismiss();
                                    Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingProgress.dismiss();
                    }
                });
            }
        }
        else {

            //Post without an image
            HashMap<Object, String> postHashMap = new HashMap<>();
            postHashMap.put("uid", uid);
            postHashMap.put("uName", name);
            postHashMap.put("uEmail", email);
            postHashMap.put("uDp", dp);
            postHashMap.put("pId", timeStamp);
            postHashMap.put("pDescription", description);
            postHashMap.put("pImage", "noImage");
            postHashMap.put("pTime", timeStamp);
            postHashMap.put("pLikes", "0");
            postHashMap.put("pComments", "0");

            //Path to store data
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
            reference.child(timeStamp).setValue(postHashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    loadingProgress.dismiss();
                    Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();

                    //Reset views
                    postDescription.setText("");
                    postImage.setImageURI(null);
                    imageUri = null;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingProgress.dismiss();
                    Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean checkStoragePermission(){

        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){

        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    //Camera permission
    private boolean checkCameraStoragePermission(){

        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){

        ActivityCompat.requestPermissions(this, storagePermissions, CAMERA_REQUEST_CODE);
    }

    private void showImagePicDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(AddPostActivity.this).inflate(R.layout.profile_bottom_sheet_dialog,
                (ConstraintLayout)findViewById(R.id.bottomSheetContainer));
        bottomSheetView.findViewById(R.id.galleryFab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!checkStoragePermission()){

                    requestStoragePermission();
                }
                else {

                    pickFromGallery();
                    bottomSheetDialog.dismiss();
                }

            }
        });
        bottomSheetView.findViewById(R.id.cameraFab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!checkCameraStoragePermission()) {

                    requestCameraPermission();
                } else {

                    pickFromCamera();
                    bottomSheetDialog.dismiss();
                }
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){

            case CAMERA_REQUEST_CODE:

                if(grantResults.length > 0){

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && writeStorageAccepted){

                        pickFromCamera();
                    }
                    else {

                        Toast.makeText(this, "Please enable permissions for storage & camera", Toast.LENGTH_SHORT).show();
                    }

                }
                break;

            case STORAGE_REQUEST_CODE:

                if(grantResults.length > 0){

                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(writeStorageAccepted){

                        pickFromGallery();
                    }
                    else {

                        Toast.makeText(this, "Please enable permissions for storage", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK){

            //Gallery
            if(requestCode == IMAGE_PICK_GALLERY_CODE) {

                imageUri = data.getData();

                postImage.setImageURI(imageUri);
            }

            // Camera
            if(requestCode == IMAGE_PICK_CAMERA_CODE){

                postImage.setImageURI(imageUri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    private void pickFromGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        imageUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        //Start intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_save).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            Intent intent = new Intent(AddPostActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {

        if(currentUser != null){
            //User is signed in stay here
            email = currentUser.getEmail();
            uid = currentUser.getUid();
        }
        else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }


}
