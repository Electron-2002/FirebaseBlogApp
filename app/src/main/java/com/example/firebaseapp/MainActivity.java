package com.example.firebaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.databinding.ActivityMainBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    DatabaseReference mDatabase, mDatabaseUsers, mDatabaseLikes;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    ActivityMainBinding binding;

    boolean mProcess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
        mAuth = FirebaseAuth.getInstance();

        mDatabase.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabaseLikes.keepSynced(true);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                    startActivity(intent);
                }
            }
        };

        binding.blogList.setHasFixedSize(true);
        binding.blogList.setLayoutManager(new LinearLayoutManager(this));

        if (mAuth.getCurrentUser() != null)
            checkUserInDatabase();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthStateListener);

        FirebaseRecyclerAdapter<Blog, BlogViewHolder> adapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class,
                R.layout.blog_item,
                BlogViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder blogViewHolder, Blog blog, int i) {
                final String postID = getRef(i).getKey();
                blogViewHolder.setTitle(blog.getTitle());
                blogViewHolder.setDescription(blog.getDescription());
                blogViewHolder.setImageUri(blog.getImageUri());
                blogViewHolder.setUsername(blog.getUsername());

                blogViewHolder.like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mProcess = true;

                        mDatabaseLikes.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (mProcess) {
                                    if (dataSnapshot.child(postID).hasChild(mAuth.getCurrentUser().getUid())) {
                                        mDatabaseLikes.child(postID).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mProcess = false;
                                    } else {
                                        mDatabaseLikes.child(postID).child(mAuth.getCurrentUser().getUid()).setValue("random");
                                        mProcess = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });
            }
        };

        binding.blogList.setAdapter(adapter);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View view;
        ImageButton like;
        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);

            view = itemView;

            like = view.findViewById(R.id.like_button);
        }

        void setTitle(String title) {
            TextView blogTitle = view.findViewById(R.id.blog_item_title);
            blogTitle.setText(title);
        }

        void setDescription(String description) {
            TextView blogDescription = view.findViewById(R.id.blog_item_description);
            blogDescription.setText(description);
        }

        void setImageUri(String imageUri) {
            ImageView blogImage = view.findViewById(R.id.blog_item_image);
            Picasso.get().load(imageUri).into(blogImage);
        }

        void setUsername(String username) {
            TextView blogUsername = view.findViewById(R.id.blog_item_username);
            blogUsername.setText(username);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_add_blog) {
            Intent intent = new Intent(MainActivity.this, PostBlogActivity.class);
            startActivity(intent);
        }

        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkUserInDatabase() {
        final String currentUser = mAuth.getCurrentUser().getUid();

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(currentUser)) {
                    Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void logout() {
        mAuth.signOut();
    }
}
