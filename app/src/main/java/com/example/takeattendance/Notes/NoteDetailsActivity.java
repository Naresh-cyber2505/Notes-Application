package com.example.takeattendance.Notes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.example.takeattendance.databinding.ActivityCreateNoteBinding;
import com.example.takeattendance.databinding.ActivityNoteDetailsBinding;
import com.squareup.picasso.Picasso;

public class NoteDetailsActivity extends AppCompatActivity {

    ActivityNoteDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String textTitle = getIntent().getStringExtra("textTitle");
        String textSubTitle = getIntent().getStringExtra("textSubTitle");
        String textDateTime = getIntent().getStringExtra("textDateTime");
        String textInput = getIntent().getStringExtra("textInput");
        String noteId = getIntent().getStringExtra("noteId");
        String textUrl = getIntent().getStringExtra("textUrl");
        String imageNote = getIntent().getStringExtra("imageNote");

        binding.inputNoteTitle.setText(textTitle);
        binding.inputNoteTitle.setEnabled(false);
        binding.inputNoteSubTitle.setText(textSubTitle);
        binding.inputNoteSubTitle.setEnabled(false);
        binding.inputNote.setText(textInput);
        binding.inputNote.setEnabled(false);
        binding.textDateTime.setText(textDateTime);

        binding.iamgeNote.setVisibility(View.VISIBLE);
        Picasso.get()
                .load(imageNote)
                .into(binding.iamgeNote);

        binding.textWebURL.setText(textUrl);

        binding.iamgeNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.iamgeNote.invalidate();
                Drawable dr = binding.iamgeNote.getDrawable();
                Common.IMAGE_BITMAP = ((BitmapDrawable) dr.getCurrent()).getBitmap();
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(NoteDetailsActivity.this, binding.iamgeNote, "image");
                Intent intent = new Intent(getApplicationContext(), NoteImageViewerActivity.class);
                intent.putExtra("textTitle", textTitle);
                intent.putExtra("textUrl", textUrl);
                intent.putExtra("textDateTime", textDateTime);
                startActivity(intent, activityOptionsCompat.toBundle());
            }
        });

        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}