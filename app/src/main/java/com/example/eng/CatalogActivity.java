package com.example.eng;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CatalogActivity extends AppCompatActivity {

    private ListView lv_dirs;

    private String[] array_dir;
    private int[] array_dir_id;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        lv_dirs = findViewById(R.id.lv_dirs);
        list_dir_block();

        lv_dirs.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                int dir_id = array_dir_id[position];

                Intent j = new Intent(CatalogActivity.this, OpenDirActivity2.class);
                j.putExtra("dir_id", dir_id);
                startActivity(j);
                finish();
            }
        });

    }

    private void list_dir_block(){
        array_dir = get_dirs().names_dirs;
        array_dir_id = get_dirs().id_dirs;
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, array_dir);
        lv_dirs.setAdapter(adapter);
    }

    private ListDir get_dirs(){
        List<String> List_dir = new ArrayList<>();
        List<Integer> List_dir_id = new ArrayList<>();
        List<Integer> List_dir_nr = new ArrayList<>();
        String selection = DBHelper.KEY_STATUS_dirs + " = 0 AND " + DBHelper.KEY_CUSTOM_dirs + " = 0";

        SQLiteDatabase database = MainActivity.dbHelper.getWritableDatabase();
        Cursor cursor = database.query(
                DBHelper.TABLE_DIRS,
                null,
                selection,
                null,
                null,
                null,
                null);

        while (cursor.moveToNext()){
            @SuppressLint("Range") String dir = cursor.getString(cursor.getColumnIndex(DBHelper.KEY_NAME_dirs));
            @SuppressLint("Range") int dir_id = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_ID_dirs));
            @SuppressLint("Range") int dir_nr = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_NEXT_REPETITION_dirs));
            List_dir.add(dir);
            List_dir_id.add(dir_id);
            List_dir_nr.add(dir_nr);
        }
        cursor.close();

        ListDir listDir = new ListDir(List_dir, List_dir_id, List_dir_nr);

        return listDir;
    }

    public void onBackPressed() {
        Intent j = new Intent(CatalogActivity.this, MainActivity.class);
        startActivity(j);
        finish();
    }
}