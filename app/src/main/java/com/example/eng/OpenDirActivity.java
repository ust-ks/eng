package com.example.eng;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class OpenDirActivity extends AppCompatActivity {

    public int dir_id;
    public int custom;

    public Button b_add_word;
    public ImageButton b_edit;
    public Button b_repetition;
    public TextView b_delete_not_custom_dir;

    public TextView tv_dir_name;
    private TextView tv_msg;
    private ListView lv_word;

    private String[] array_word;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_dir);

        dir_id = getIntent().getIntExtra("dir_id", -1);

        init();
        tv_dir_name.setText(getName());
        list_words_block();

        if (array_word.length != 0){
            tv_msg.setVisibility(View.INVISIBLE);
        }

        if (custom == 0){
            b_add_word.setVisibility(View.INVISIBLE);
            b_edit.setVisibility(View.INVISIBLE);
            b_delete_not_custom_dir.setVisibility(View.VISIBLE);
        }

        b_add_word.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { dialog_add_word(); }
        });

        b_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { dialog_edit();}
        });

        b_repetition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (array_word.length == 0){
                    tv_msg.setTextColor(getResources().getColor(R.color.red));
                }
                else{
                    Intent j = new Intent(OpenDirActivity.this, RepetitionActivity.class);
                    j.putExtra("name", getName());
                    j.putExtra("list_words", getWords().words);
                    j.putExtra("list_translations", getWords().translations);
                    j.putExtra("dir_id", dir_id);
                    startActivity(j);
                }
            }
        });

        b_delete_not_custom_dir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_delete_dir();
            }
        });

        lv_word.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(custom == 1){
                    dialog_delete_word(getWords().id_words[i]);
                }
                return true;
            }
        });

    }

    private void init(){
        b_add_word = findViewById(R.id.b_add_word);
        b_edit = findViewById(R.id.b_edit);
        b_repetition = findViewById(R.id.b_repetition);
        tv_dir_name = findViewById(R.id.tv_dir_name);
        tv_msg = findViewById(R.id.tv);
        lv_word = findViewById(R.id.lv_word);
        b_delete_not_custom_dir = findViewById(R.id.b_delete_not_custom_dir);
    }

    private void list_words_block(){
        array_word = getWords().str;
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, array_word);
        lv_word.setAdapter(adapter);
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
            @SuppressLint("Range") int cust = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_CUSTOM_dirs));
            name_dir = name;
            custom = cust;
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

    private void dialog_add_word(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ConstraintLayout cl = (ConstraintLayout) getLayoutInflater().inflate(R.layout.dialog_add_word, null);
        builder.setView(cl);
        AlertDialog dialog = builder.show();

        Button b_cancel = dialog.findViewById(R.id.b_edit);
        Button b_ok = dialog.findViewById(R.id.b_ok);
        TextView tv_msg = dialog.findViewById(R.id.tv_msg);

        b_cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        b_ok.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_word = dialog.findViewById(R.id.et_new_word);
                EditText et_translation = dialog.findViewById(R.id.et_trans);
                String str1 = et_word.getText().toString();
                String str2 = et_translation.getText().toString();

                if((str1.length() == 0) || (str2.length() == 0)){
                    tv_msg.setText("Заполните все поля!");
                }
                else{
                    add_word(str1, str2);
                    dialog.dismiss();
                }
            }
        });
    }

    private void add_word(String word, String translation){
        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.KEY_WORD_words, word);
        contentValues.put(DBHelper.KEY_TRANSLATION_words, translation);
        contentValues.put(DBHelper.KEY_DIR_ID_words, dir_id);

        MainActivity.database.insert(DBHelper.TABLE_WORDS, null, contentValues);
        recreate();
    }

    private void dialog_edit(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ConstraintLayout cl = (ConstraintLayout) getLayoutInflater().inflate(R.layout.dialog_edit_dir, null);
        builder.setView(cl);
        AlertDialog dialog = builder.show();

        Button b_cancel = dialog.findViewById(R.id.b_cancel);
        Button b_ok = dialog.findViewById(R.id.b_ok);
        TextView b_delete_dir = dialog.findViewById(R.id.b_delete_dir);
        TextView tv_msg = dialog.findViewById(R.id.tv_msg);

        b_cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        b_ok.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_word = dialog.findViewById(R.id.et_new_name);
                String str = et_word.getText().toString();

                if(str.length() == 0){
                    tv_msg.setText("Заполните поле!");
                }
                else{
                    rename_dir(str);
                    dialog.dismiss();
                }
            }
        });

        b_delete_dir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_delete_dir();
                dialog.dismiss();
            }
        });
    }

    private void rename_dir(String new_name){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_NAME_dirs, new_name);
        String where = DBHelper.KEY_ID_dirs + " = " + dir_id;
        MainActivity.database.update(DBHelper.TABLE_DIRS, contentValues, where, null);
        recreate();
    }

    private void dialog_delete_dir(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ConstraintLayout cl = (ConstraintLayout) getLayoutInflater().inflate(R.layout.dialog_delete_dir, null);
        builder.setView(cl);
        AlertDialog dialog = builder.show();

        Button b_no = dialog.findViewById(R.id.b_no);
        Button b_yes = dialog.findViewById(R.id.b_yes);

        b_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        b_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_dir();
            }
        });
    }

    private void delete_dir(){

        if(custom == 1){
            String where = DBHelper.KEY_ID_dirs + " = ?";
            MainActivity.database.delete(DBHelper.TABLE_DIRS, where, new String[]{String.valueOf(dir_id)});
        }
        else{
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.KEY_STATUS_dirs, 0);
            contentValues.put(DBHelper.KEY_LAST_REPETITION_dirs, 0);
            contentValues.put(DBHelper.KEY_NEXT_REPETITION_dirs, 0);
            String where = DBHelper.KEY_ID_dirs + " = " + dir_id;
            MainActivity.database.update(DBHelper.TABLE_DIRS, contentValues, where, null);
        }
        Intent i = new Intent(OpenDirActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void dialog_delete_word(int word_id){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ConstraintLayout cl = (ConstraintLayout) getLayoutInflater().inflate(R.layout.dialog_delete_word, null);
        builder.setView(cl);
        AlertDialog dialog = builder.show();

        Button b_no = dialog.findViewById(R.id.b_no);
        Button b_yes = dialog.findViewById(R.id.b_yes);

        b_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        b_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_word(word_id);
                recreate();
            }
        });
    }

    private void delete_word(int word_id){
        String where = DBHelper.KEY_ID_words + " = ?";
        MainActivity.database.delete(DBHelper.TABLE_WORDS, where, new String[]{String.valueOf(word_id)});
    }

    public void onBackPressed() {
        Intent i = new Intent(OpenDirActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}