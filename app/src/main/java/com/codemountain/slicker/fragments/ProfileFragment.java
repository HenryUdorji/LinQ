package com.codemountain.slicker.fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codemountain.slicker.R;
import com.codemountain.slicker.activities.AddPostActivity;
import com.codemountain.slicker.activities.EditProfileActivity;
import com.codemountain.slicker.activities.MainActivity;
import com.codemountain.slicker.adapters.PostAdapter;
import com.codemountain.slicker.model.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;

    private StorageReference storageReference;
    String storagePath = "Users_Profile_Cover_Images/";

    private CircleImageView profileImageView;
    private ImageView coverProfileImageView;
    private TextView profileNameTextView, profileEmailTextView, profilePhoneNumberTextView, profileBioTextView,
            profileBirthDateTextView, profileJoinedDateTextView, profileLocationTextView;
    private FloatingActionButton editFab;
    private ProgressBar coverImageProgressBar, profileImageProgressBar;
    private String name, email, phone, image, coverImage, profileBio, location, birthDate, joinedDate;

    private RecyclerView postRecyclerView;
    private List<ModelPost> postList = new ArrayList<>();
    private PostAdapter postAdapter;

    private String uid;

    private ProgressDialog loadingProgress;

    Uri imageUri;

    String profileOrCoverPhoto;

    //Permissions
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    String cameraPermissions[];
    String storagePermissions[];


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.from(getContext()).inflate(R.layout.fragment_profile, container, false);

        //Init Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("Users");
        reference.keepSynced(true);
        storageReference = getInstance().getReference();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //Init Views
        profileImageView = view.findViewById(R.id.profileImageView);
        coverProfileImageView = view.findViewById(R.id.profileCoverImageView);
        profileNameTextView = view.findViewById(R.id.profileName);
        profileEmailTextView = view.findViewById(R.id.profileEmail);
        profilePhoneNumberTextView = view.findViewById(R.id.profilePhoneNo);
        profileBioTextView = view.findViewById(R.id.profileBio);
        profileBirthDateTextView = view.findViewById(R.id.birthDate);
        profileLocationTextView = view.findViewById(R.id.editLocation);
        profileJoinedDateTextView = view.findViewById(R.id.joinedDate);
        coverImageProgressBar = view.findViewById(R.id.coverImageProgressBar);
        profileImageProgressBar = view.findViewById(R.id.profileImageProgressBar);
        editFab = view.findViewById(R.id.editFab);

        loadingProgress = new ProgressDialog(getContext());

        //RecyclerView
        postRecyclerView = view.findViewById(R.id.profilesPostRecyclerView);
        postRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postRecyclerView.setLayoutManager(linearLayoutManager);

        Query query = reference.orderByChild("email").equalTo(currentUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    name =  ds.child("name").getValue().toString();
                    email =  ds.child("email").getValue().toString();
                    phone =  ds.child("phone").getValue().toString();
                    image =  ds.child("image").getValue().toString();
                    coverImage =  ds.child("cover_image").getValue().toString();
                    profileBio =  ds.child("bio").getValue().toString();
                    location = ds.child("location").getValue().toString();
                    birthDate = ds.child("date_of_birth").getValue().toString();
                    joinedDate = ds.child("registration_date").getValue().toString();


                    profileNameTextView.setText(name);
                    profileEmailTextView.setText(email);
                    profilePhoneNumberTextView.setText(phone);
                    profileBioTextView.setText(profileBio);
                    profileLocationTextView.setText(location);
                    profileBirthDateTextView.setText("Born " + birthDate);
                    profileJoinedDateTextView.setText("Joined " + joinedDate);

                   //Setting image to the profile image
                    if (getActivity() == null){
                        return;
                    }
                    Glide.with(getActivity()).load(image)
                            .placeholder(R.drawable.default_image)
                            .addListener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    profileImageProgressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    profileImageProgressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .into(profileImageView);

                   //setting image to the profile cover
                    Glide.with(getActivity()).load(coverImage)
                            .placeholder(R.drawable.cover_default)
                            .addListener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    coverImageProgressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    coverImageProgressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .into(coverProfileImageView);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        editFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showEditProfileDialog();
                Intent callEditProfile = new Intent(getActivity(), EditProfileActivity.class);
                callEditProfile.putExtra("NAME", name);
                callEditProfile.putExtra("LOCATION", location);
                callEditProfile.putExtra("PHONE_NUMBER", phone);
                callEditProfile.putExtra("BIO", profileBio);
                callEditProfile.putExtra("DOB", birthDate);
                startActivity(callEditProfile);
            }
        });
        coverProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createCoverImage();
            }
        });
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createProfileImage();
            }
        });

        checkUserStatus();
        loadMyPost();
        return view;
    }

    private void loadMyPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ModelPost post = ds.getValue(ModelPost.class);
                    postList.add(post);
                }
                postAdapter = new PostAdapter(getActivity(), postList);
                postRecyclerView.setAdapter(postAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPost(final String s){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ModelPost post = ds.getValue(ModelPost.class);

                    if(post.getpDescription().toLowerCase().contains(s.toLowerCase())) {
                        postList.add(post);
                    }
                }
                postAdapter = new PostAdapter(getActivity(), postList);
                postRecyclerView.setAdapter(postAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStatus() {

        currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //User is signed in stay here
            uid = currentUser.getUid();
        }
        else {

            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
    
    private void createCoverImage() {
        loadingProgress.setMessage("Updating cover photo");
        profileOrCoverPhoto = "cover_image";
        showImagePicDialog();
    }

    private void createProfileImage(){
        loadingProgress.setMessage("Updating profile picture");
        profileOrCoverPhoto = "image";
        showImagePicDialog();
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    //Camera permission
    private boolean checkCameraStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        requestPermissions(storagePermissions, CAMERA_REQUEST_CODE);
    }

    private void showImagePicDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.profile_bottom_sheet_dialog,
                (ViewGroup) getView().findViewById(R.id.bottomSheetContainer));
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

                        Toast.makeText(getActivity(), "Please enable permissions for storage & camera", Toast.LENGTH_SHORT).show();
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

                        Toast.makeText(getActivity(), "Please enable permissions for storage", Toast.LENGTH_SHORT).show();
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

                //Compressing the Image for Gallery
                Bitmap bitmap = null;
                try{
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                //Converting to byte array
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
                final byte[] bitmapBytes = byteArrayOutputStream.toByteArray();

                uploadProfileCoverPhoto(bitmapBytes);

            }

            // Camera
            if(requestCode == IMAGE_PICK_CAMERA_CODE){

                /**
                 * Compressing the Image for Gallery
                 */
                Bitmap bitmap = null;
                try{
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                /**
                 * Converting to byte array
                 */
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                final byte[] bitmapBytes = byteArrayOutputStream.toByteArray();

                uploadProfileCoverPhoto(bitmapBytes);

            }


        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    /**
     * This method would upload the image for both cover photo & profile photo
     */
    private void uploadProfileCoverPhoto(byte[] thumbnail) {
        loadingProgress.show();

        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + currentUser.getUid();
        StorageReference storageReference1 = storageReference.child(filePathAndName);
        storageReference1.putBytes(thumbnail)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //Check if image is uploaded and received
                        if(uriTask.isSuccessful()){

                            HashMap<String, Object> profileMap = new HashMap<>();
                            assert downloadUri != null;
                            profileMap.put(profileOrCoverPhoto, downloadUri.toString());

                            reference.child(currentUser.getUid()).updateChildren(profileMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            loadingProgress.dismiss();
                                            Toast.makeText(getActivity(), "Image updated", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            loadingProgress.dismiss();
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                        else {

                            loadingProgress.dismiss();
                            Toast.makeText(getActivity(), "Some error occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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

        imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        //Start intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);


    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    /**
     * Inflating menu-item
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_save).setVisible(false);

        //menu.findItem(R.id.action_search).setVisible(false);
        //SearchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query)){
                    searchPost(query);
                }
                else {
                    loadMyPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //usersAdapter.getFilter().filter(newText);
                if(!TextUtils.isEmpty(newText)){
                    searchPost(newText);
                }
                else {
                    loadMyPost();
                }


                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }


    /**
     * Click listener for menu-item
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_logout:

                mAuth.signOut();
                checkUserStatus();
                break;

            case R.id.action_add_post:
                startActivity(new Intent(getActivity(), AddPostActivity.class));

                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
