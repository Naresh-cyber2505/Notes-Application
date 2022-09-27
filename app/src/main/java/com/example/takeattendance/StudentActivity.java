package com.example.takeattendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.takeattendance.StudentActivities.StudentAdapter;
import com.example.takeattendance.StudentActivities.StudentItem;
import com.example.takeattendance.databinding.ActivityStudentBinding;

import java.util.ArrayList;

public class StudentActivity extends AppCompatActivity {

    ActivityStudentBinding binding;
    Toolbar toolbar;
    private String className;
    private String subjectName;
    int position;
    private long cid;
    private StudentAdapter studentAdapter;
    private ArrayList<StudentItem> studentItems = new ArrayList<>();
    private DbHelper dbHelper;
    private MyCalender calender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        calender = new MyCalender();
        dbHelper = new DbHelper(this);

        Intent intent = getIntent();
        className = intent.getStringExtra("className");
        subjectName = intent.getStringExtra("subjectName");
        position = intent.getIntExtra("position", -1);
        cid = intent.getLongExtra("cid",-1);

        setToolbar();
        loadData();

        binding.studentRecycle.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.studentRecycle.setLayoutManager(layoutManager);
        studentAdapter = new StudentAdapter(this,studentItems);
        binding.studentRecycle.setAdapter(studentAdapter);
        studentAdapter.setOnItemClickListener(position -> changeStatus(position));
        loadStatusData();
    }

    private void loadData() {
        Cursor cursor = dbHelper.getStudent(cid);
        Log.i("1234567890", "loadData: "+cid);
        studentItems.clear();
        while (cursor.moveToNext()){
            @SuppressLint("Range") long sid = cursor.getLong(cursor.getColumnIndex(DbHelper.S_ID));
            @SuppressLint("Range") int roll = cursor.getInt(cursor.getColumnIndex(DbHelper.STUDENT_ROLL_KEY));
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DbHelper.STUDENT_NAME_KEY));
            studentItems.add(new StudentItem(sid,roll,name));
        }
        cursor.close();
    }

    private void changeStatus(int position) {
        String status = studentItems.get(position).getStatus();

        if (status.equals("P")) status = "A";
        else status = "P";

        studentItems.get(position).setStatus(status);
        studentAdapter.notifyItemChanged(position);
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveStatus();
            }
        });

        title.setText(className);
        subtitle.setText(subjectName+" | " +calender.getDate());

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        toolbar.inflateMenu(R.menu.student_menu);
        toolbar.setOnMenuItemClickListener(menuItem->onMenuItemClick(menuItem));

    }

    private void saveStatus() {
        for (StudentItem studentItem : studentItems){
            String status = studentItem.getStatus();
            if (!status.equals("P")) status = "A";
            long value = dbHelper.addStatus(studentItem.getSid(),cid,calender.getDate(),status);

            if (value==-1)dbHelper.updateStatus(studentItem.getSid(),calender.getDate(),status);
        }
    }

    private void loadStatusData(){
        for (StudentItem studentItem : studentItems){
            String status = dbHelper.getStatus(studentItem.getSid(),calender.getDate());
            if (status!=null) studentItem.setStatus(status);
            else studentItem.setStatus("");
        }
        studentAdapter.notifyDataSetChanged();
    }

    private boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.add_student){
            showAddStudentDialog();
        }
        else if (menuItem.getItemId() == R.id.show_calender){
            showCalender();
        }
        else if (menuItem.getItemId() == R.id.show_attendance_sheet){
            openSheetList();
        }
        return true;
    }

    private void openSheetList() {
        long[] idArray = new long[studentItems.size()];
        for (int i=0;i<idArray.length;i++)
            idArray[i] = studentItems.get(i).getSid();
        int[] rollArray = new int[studentItems.size()];
        for (int i=0;i<rollArray.length;i++)
            rollArray[i] = studentItems.get(i).getRoll();

        String[] nameArray = new String[studentItems.size()];
        for (int i=0;i<nameArray.length;i++)
            nameArray[i] = studentItems.get(i).getName();

        Intent intent = new Intent(this,SheetListActivity.class);
        intent.putExtra("cid", cid);
        intent.putExtra("idArray",idArray);
        intent.putExtra("rollArray",rollArray);
        intent.putExtra("nameArray",nameArray);
        intent.putExtra("className",className);
        intent.putExtra("subjectName",subjectName);

//        className = intent.getStringExtra("className");
//        subjectName = intent.getStringExtra("subjectName");

        startActivity(intent);

    }

    private void showCalender() {

        calender.show(getSupportFragmentManager(),"");
        calender.setOnCalenderOkClickListener(this::onCalenderOkClicked);
    }

    private void onCalenderOkClicked(int year, int month, int day) {
        calender.setDate(year,month,day);
        binding.toolbarStudent.subtitleToolbar.setText(subjectName+" | "+calender.getDate());
        loadStatusData();
    }

    private void showAddStudentDialog() {
        MyDialog dialog = new MyDialog();
        dialog.show(getFragmentManager(),MyDialog.STUDENT_ADD_DIALOG);
        dialog.setListener((roll,name)->addStudent(roll,name));
    }

    private void addStudent(String roll_string, String name) {
        int roll = Integer.parseInt(roll_string);
        long sid = dbHelper.addStudent(cid,roll,name);
        StudentItem studentItem = new StudentItem(sid,roll,name);
        studentItems.add(studentItem);
        studentAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case 0:
                showUpdateStudentDialog(item.getGroupId());
                break;
            case 1:
                deleteStudent(item.getGroupId());
        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateStudentDialog(int position) {
        MyDialog dialog = new MyDialog(studentItems.get(position).getRoll(),studentItems.get(position).getName());
        dialog.show(getFragmentManager(),MyDialog.STUDENT_UPDATE_DIALOG);
        dialog.setListener((roll_string,name)->updateStudent(position,name));
    }

    private void updateStudent(int position, String name) {
        dbHelper.updateStudent(studentItems.get(position).getSid(),name);
        studentItems.get(position).setName(name);
        studentAdapter.notifyItemChanged(position);
    }

    private void deleteStudent(int position) {
        dbHelper.deleteStudent(studentItems.get(position).getSid());
        studentItems.remove(position);
        studentAdapter.notifyItemRemoved(position);
    }


}