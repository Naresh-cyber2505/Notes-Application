package com.example.takeattendance;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.example.takeattendance.Fragments.TwoFragment;
import com.example.takeattendance.Fragments.ViewPagerAdapter;
import com.example.takeattendance.Models.User;
import com.example.takeattendance.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseStorage storage;

    FirebaseFirestore firebaseFirestore;
    static final float END_SCALE = 0.7f;

    CircleImageView circleImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        setupViewPager(binding.viewPager);
        binding.tableLayout.setupWithViewPager(binding.viewPager);

        binding.navigationView.bringToFront();
        binding.navigationView.setNavigationItemSelectedListener(this);
        binding.navigationView.setCheckedItem(R.id.classes);

//        TextView navUsername = binding.navigationView.findViewById(R.id.user_name);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        View headerview = navigationView.getHeaderView(0);
        TextView navUsername = headerview.findViewById(R.id.user_name);
        TextView navEmail = headerview.findViewById(R.id.email);
        circleImageView = headerview.findViewById(R.id.profile_image);
        ImageView signOutBtn = headerview.findViewById(R.id.signOut);

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                finishAffinity();
            }
        });


        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                intent.setType("image/*");
//                String mimeTypes = ("image/jpeg");
//                intent.putExtra(Intent.EXTRA_MIME_TYPES, (Parcelable) mimeTypes);
//                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                startActivityForResult(intent, 23);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 23);
            }
        });


        firebaseFirestore.collection("Users")
                .document(FirebaseAuth.getInstance().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);

                        navUsername.setText(user.getUsername());
                        navEmail.setText(user.getEmail());

                        Picasso.get().load(user.getProfileImage())
                                .into(circleImageView);

                    }
                });


        animateNavigationDrawer();
    }

    private void setupViewPager(ViewPager viewPager){

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new OneFragment(),"CLASSROOMS");
        viewPagerAdapter.addFragment(new TwoFragment(),"NOTES");
//        viewPagerAdapter.addFragment(new ThreeFragment(),"REMINDERS");
        viewPager.setAdapter(viewPagerAdapter);

    }

//    private fun launchImageCrop(uri: Uri){
//
//        var destination:String = StringBuilder(UUID.randomUUID().toString()).toString()
//        var options: UCrop.Options = UCrop.Options()
//
//        UCrop.of(Uri.parse(uri.toString()), Uri.fromFile(File(cacheDir,destination)))
//                .withOptions(options)
//                .withAspectRatio(2F,1F)
//                .useSourceImageAspectRatio()
//                .withMaxResultSize(2000,2000)
//                .start(this)
//    }

    private void animateNavigationDrawer() {

        binding.drawerLayout.setScrimColor(getResources().getColor(R.color.home_background));
        binding.drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

                final float diffScaleoffset = slideOffset * (1 - END_SCALE);
                final float offerScale = 1 - diffScaleoffset;
                binding.contentView.setScaleX(offerScale);
                binding.contentView.setScaleY(offerScale);

                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = binding.contentView.getWidth() * diffScaleoffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                binding.contentView.setTranslationX(xTranslation);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String userId;
        userId = auth.getCurrentUser().getUid();

        if (requestCode == 23){
            if(data.getData() != null){
                Uri uri = data.getData();

                circleImageView.setImageURI(uri);

                StorageReference reference = storage.getReference().child("profile_photo")
                        .child(userId);
                reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){
                                    String uri = task.getResult().toString();
                                    DocumentReference documentReference = firebaseFirestore.collection("Users")
                                                    .document(userId);

                                    documentReference.update("profileImage", uri);

                                    Toast.makeText(MainActivity.this, "Your Profile photo has been Changed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

//                binding.conBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                    }
//                });



            }
        }
    }

    @Override
    public void onBackPressed() {
        if(binding.drawerLayout.isDrawerVisible(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//        switch (id){
//            case R.id.profile_image:
//                Snackbar.make(item.getActionView(),"New Tryied", Snackbar.LENGTH_SHORT)
//                        .setAction("Action", null).show();
//                break;
//            case R.id.classes:
//                Intent intent = new Intent(getApplicationContext(), OneFragment.class);
//                startActivity(intent);
//                break;
//        }
        return true;
    }
}