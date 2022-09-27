package com.example.takeattendance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.takeattendance.ClassActivities.ClassAdapter;
import com.example.takeattendance.ClassActivities.ClassItem;
import com.example.takeattendance.databinding.FragmentOneBinding;

import java.util.ArrayList;

public class OneFragment extends Fragment {

    FragmentOneBinding binding;
    ClassAdapter classAdapter;
    ArrayList<ClassItem> arrayList = new ArrayList<>();;
    Toolbar toolbar;
    DbHelper dbHelper;
    private int cid;


    public OneFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_one, container, false);
        binding = FragmentOneBinding.inflate(inflater,container,false);

        dbHelper = new DbHelper(getContext());
        loadData();

        binding.FloatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });



        binding.recyclerview.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        binding.recyclerview.setLayoutManager(layoutManager);
        classAdapter = new ClassAdapter(getContext(), arrayList);
        binding.recyclerview.setAdapter(classAdapter);
        classAdapter.setOnItemClickListener(new ClassAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                gotoItemActivity(position);
            }
        });



//        setToolbar();

        return binding.getRoot();





    }
    private void loadData() {
        Cursor cursor = dbHelper.getClassTable();

        arrayList.clear();
        while (cursor.moveToNext()){
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(DbHelper.C_ID));
            @SuppressLint("Range") String className = cursor.getString(cursor.getColumnIndex(DbHelper.CLASS_NAME_KEY));
            @SuppressLint("Range") String subjectName = cursor.getString(cursor.getColumnIndex(DbHelper.SUBJECT_NAME_KEY));

            arrayList.add(new ClassItem(id,className,subjectName));

        }
    }

//    private void setToolbar() {
//        toolbar = toolbar.findViewById(R.id.toolbar);
//        TextView title = toolbar.findViewById(R.id.title_toolbar);
//        TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
//        ImageButton back = toolbar.findViewById(R.id.back);
//        ImageButton save = toolbar.findViewById(R.id.save);
//
//        title.setText("Attendance App");
//        subtitle.setVisibility(View.GONE);
//        back.setVisibility(View.INVISIBLE);
//        save.setVisibility(View.INVISIBLE);
//    }

    private void gotoItemActivity(int position) {
        Intent intent = new Intent(getContext(), StudentActivity.class);

        intent.putExtra("className", arrayList.get(position).getClassName());
        intent.putExtra("subjectName", arrayList.get(position).getSubjectName());
        intent.putExtra("position", position);
        intent.putExtra("cid",arrayList.get(position).getCid());
        startActivity(intent);
    }

    private void showDialog() {
        MyDialog dialog = new MyDialog();
        dialog.show(getActivity().getFragmentManager(), MyDialog.CLASS_ADD_DIALOG);
        dialog.setListener((className, subjectName) -> addClass(className, subjectName));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addClass(String className, String subjectName) {
        long cid = dbHelper.addClass(className,subjectName);

        ClassItem classItem = new ClassItem(cid,className,subjectName);
        arrayList.add(classItem);
        classAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case 0:
                showUpdateDialog(item.getGroupId());
                break;
            case 1:
                deleteClass(item.getGroupId());
        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(int position) {
        MyDialog dialog = new MyDialog(arrayList.get(position).getClassName(),arrayList.get(position).getSubjectName());
        dialog.show(getActivity().getFragmentManager(),
                MyDialog.CLASS_UPDATE_DIALOG);
        dialog.setListener((className, subjectName)->updateClass(position,className,subjectName));
    }

    private void updateClass(int position, String className, String subjectName) {
        dbHelper.updateClass(arrayList.get(position).getCid(),className,subjectName);
        arrayList.get(position).setClassName(className);
        arrayList.get(position).setSubjectName(subjectName);
        classAdapter.notifyItemChanged(position);
    }

    private void deleteClass(int position) {
        dbHelper.deleteClass(arrayList.get(position).getCid());
        arrayList.remove(position);
        classAdapter.notifyItemRemoved(position);

    }
}