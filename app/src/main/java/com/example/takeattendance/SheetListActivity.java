package com.example.takeattendance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.takeattendance.databinding.ActivitySheetListBinding;

import java.util.ArrayList;

public class SheetListActivity extends AppCompatActivity {

    ActivitySheetListBinding binding;
    private ArrayAdapter adapter;
    private ArrayList<String> listItems = new ArrayList();
    private long cid;
    Toolbar toolbar;
    private String className;
    private String subjectName;
    private MyCalender calender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySheetListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.save);


        calender = new MyCalender();

        cid = getIntent().getLongExtra("cid", -1);
        Log.i("1234567890", "onCreate: "+cid);
        className = getIntent().getStringExtra("className");
        subjectName = getIntent().getStringExtra("subjectName");
        loadListItem();
        ListView sheetList = findViewById(R.id.sheetList);
        adapter = new ArrayAdapter(this,R.layout.sheet_list,R.id.date_list_item,listItems);
        sheetList.setAdapter(adapter);

        sheetList.setOnItemClickListener(((parent, view, position, id) -> openSheetActivity(position)));


        title.setText(subjectName);
//        subtitle.setText(calender.getDate());
        subtitle.setVisibility(View.GONE);
        save.setVisibility(View.GONE);
    }

    private void openSheetActivity(int position) {
        long[] idArray = getIntent().getLongArrayExtra("idArray");
        int[] rollArray = getIntent().getIntArrayExtra("rollArray");
        String[] nameArray = getIntent().getStringArrayExtra("nameArray");
        Intent intent = new Intent(this,SheetActivity.class);
        intent.putExtra("idArray",idArray);
        intent.putExtra("rollArray",rollArray);
        intent.putExtra("nameArray",nameArray);
        intent.putExtra("month", listItems.get(position));
        intent.putExtra("subjectName", subjectName);

        startActivity(intent);
    }

    private void loadListItem() {
        Cursor cursor = new DbHelper(this).getDistinctMonths(cid);

        Log.i("1234567890", "loadListItems: "+cursor.getCount());
        while (cursor.moveToNext()){
            @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(DbHelper.DATE_KEY));
            listItems.add(date.substring(3));
//            listItems.add(date)
        }
    }
}