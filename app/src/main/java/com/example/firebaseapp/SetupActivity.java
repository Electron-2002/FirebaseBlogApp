package com.example.firebaseapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.example.firebaseapp.databinding.ActivitySetupBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class SetupActivity extends AppCompatActivity {

    private ActivitySetupBinding binding;

    private static final int GALLERY_CODE = 1;
    private Uri imageUri;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mStorage = FirebaseStorage.getInstance().getReference();

        binding.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
            }
        });

        binding.setupAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountSetup();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            binding.userImage.setImageURI(imageUri);
        }
    }

    private void accountSetup() {
        final String name = binding.userName.getText().toString().trim();
        final String userUID = mAuth.getCurrentUser().getUid();

        if (!TextUtils.isEmpty(name) && imageUri != null) {
            final StorageReference filePath = mStorage.child("Blog Images").child(Objects.requireNonNull(imageUri.getLastPathSegment()));

            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            DatabaseReference newUser = mDatabase.child(userUID);
                            newUser.child("name").setValue(name);
                            newUser.child("image").setValue(uri.toString());
                        }
                    });
                }
            });
            startActivity(new Intent(SetupActivity.this, MainActivity.class));
        }
    }
}
