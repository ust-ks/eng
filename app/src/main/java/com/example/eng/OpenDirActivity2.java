package com.example.eng;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OpenDirActivity2 extends AppCompatActivity {

    public int dir_id;

    private TextView tv_dir_name;
    private ListView lv_word;
    private Button b_add_this_dir;

    private String[] array_word;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_dir2);

        dir_id = getIntent().getIntExtra("dir_id", -1);

        init();

        tv_dir_name.setText(getName());
        array_word = getWords().str;
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, array_word);
        lv_word.setAdapter(adapter);


        b_add_this_dir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDir();
            }
        });

    }

    private void init(){
        tv_dir_name = findViewById(R.id.tv_dir_name);
        lv_word = findViewById(R.id.lv_word);
        b_add_this_dir = findViewById(R.id.b_add_this_dir);
    }

    private String getName(){
        String name_dir = null;
        SQLiteDatabase database = MainActivity.dbHelper.getWritableDatabase();
        String selection = DBHelper.KEY_ID_dirs + " = " + dir_id;
        Cursor cursor = database.query(
                DBHelper.TABLE_DIRS,
                null,
                selection,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()){
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DBHelper.KEY_NAME_dirs));
            name_dir = name;
        }

        cursor.close();
        return name_dir;
    }

    private ListWords getWords(){

        List<String> List_words = new ArrayList<>();
        List<String> List_trans = new ArrayList<>();
        List<Integer> List_id = new ArrayList<>();
        String selection = DBHelper.KEY_DIR_ID_words + " = " + dir_id;

        SQLiteDatabase database = MainActivity.dbHelper.getWritableDatabase();
        Cursor cursor = database.query(
                DBHelper.TABLE_WORDS,
                null,
                selection,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()){
            @SuppressLint("Range") String word = cursor.getString(cursor.getColumnIndex(DBHelper.KEY_WORD_words));
            @SuppressLint("Range") String trans = cursor.getString(cursor.getColumnIndex(DBHelper.KEY_TRANSLATION_words));
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_ID_words));

            List_words.add(word);
            List_trans.add(trans);
            List_id.add(id);
        }
        cursor.close();

        ListWords listWords = new ListWords(List_words, List_trans, List_id);
        return listWords;
    }

    private void addDir(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_STATUS_dirs, 1);
        contentValues.put(DBHelper.KEY_NUM_dirs, -1);

        // dt будет хранить количество МИНУТ прошедших с 01.01.2022 00:00
        Date date = new Date();
        long sec = date.getTime()/(1000*60); // тут минуты прошедшие с 01.01.1970 00:00
        long dt = (sec - 52*365*24*60); // тут минуты прошедшие с 01.01.2022 00:00
        contentValues.put(DBHelper.KEY_LAST_REPETITION_dirs, dt);
        contentValues.put(DBHelper.KEY_NEXT_REPETITION_dirs, dt+60);
        contentValues.put(DBHelper.KEY_NUM_dirs, -1);

        String where = DBHelper.KEY_ID_dirs + " = " + dir_id;
        MainActivity.database.update(DBHelper.TABLE_DIRS, contentValues, where, null);

        Intent j = new Intent(OpenDirActivity2.this, CatalogActivity.class);
        startActivity(j);
        finish();
    }

    public void onBackPressed() {
        Intent i = new Intent(OpenDirActivity2.this, CatalogActivity.class);
        startActivity(i);
        finish();
    }
}