package com.example.firebaseapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.databinding.ActivityMainBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    StorageReference storageReference;
    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");

        binding.blogList.setHasFixedSize(true);
        binding.blogList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Blog, BlogViewHolder> adapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class,
                R.layout.blog_item,
                BlogViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder blogViewHolder, Blog blog, int i) {
                blogViewHolder.setTitle(blog.getTitle());
                blogViewHolder.setDescription(blog.getDescription());
                blogViewHolder.setImageUri(blog.getImageUri());
            }
        };

        binding.blogList.setAdapter(adapter);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View view;
        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);

            view = itemView;
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.add_blog_sign) {
            startActivity(new Intent(MainActivity.this, PostBlogActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
