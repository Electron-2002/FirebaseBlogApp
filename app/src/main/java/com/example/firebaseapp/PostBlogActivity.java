package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.example.firebaseapp.databinding.ActivityPostBlogBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class PostBlogActivity extends AppCompatActivity {

    private ActivityPostBlogBinding binding;

    private static final int GALLERY_CODE = 1;
    private Uri imageUri;

    private StorageReference mStorage;
    private DatabaseReference mDatabase, mDatabaseUsers;
    private FirebaseAuth mAuth;

    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBlogBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

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
            final StorageReference filePath = mStorage.child("Blog Images").child(Objects.requireNonNull(imageUri.getLastPathSegment()));
            builder = new AlertDialog.Builder(this);
            setDialog(true);

            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {
                            final DatabaseReference newBlog = mDatabase.push();

                            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    newBlog.child("title").setValue(title);
                                    newBlog.child("description").setValue(description);
                                    newBlog.child("imageUri").setValue(uri.toString());
                                    newBlog.child("userUID").setValue(mAuth.getCurrentUser().getUid());
                                    newBlog.child("username").setValue(dataSnapshot.child("name").getValue());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                }
            });
            setDialog(false);
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    private void setDialog(boolean show){
        builder.setView(R.layout.progress_layout);
        Dialog dialog = builder.create();
        if (show)dialog.show();
        else dialog.dismiss();
    }
}
