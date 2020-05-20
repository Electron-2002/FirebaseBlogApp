package com.example.firebaseapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.example.firebaseapp.databinding.ActivityPostBlogBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class PostBlogActivity extends AppCompatActivity {

    private ActivityPostBlogBinding binding;
    private static final int GALLERY_CODE = 1;
    private Uri imageUri;
    private StorageReference storage;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBlogBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        storage = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance().getReference().child("Blog");

        binding.blogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
            }
        });

        binding.submitBlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postBlog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            binding.blogImage.setImageURI(imageUri);
        }
    }

    public void postBlog() {
        final String title = binding.blogTitle.getText().toString().trim();
        final String description = binding.blogDescription.getText().toString().trim();

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && imageUri != null) {
            final StorageReference filePath = storage.child("Blog Images").child(Objects.requireNonNull(imageUri.getLastPathSegment()));

            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            DatabaseReference newBlog = database.push();
                            newBlog.child("title").setValue(title);
                            newBlog.child("description").setValue(description);
                            newBlog.child("imageUri").setValue(uri.toString());
                        }
                    });
                }
            });

            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }
}
