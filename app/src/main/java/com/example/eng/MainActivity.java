package com.example.eng;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button b_add_dir;
    private ImageButton b_settings;

    private TextView tv_notice;

    private ListView lv_dir;
    private String[] array_dir;
    private String[] array_dir_to_repetition;
    private int[] array_dir_id;
    private ArrayAdapter<String> adapter;

    protected static DBHelper dbHelper;
    protected static SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();

        try {
            if(DBHelper.DATABASE_VERSION == 2){dbHelper.loading_dictionaries(this, database);}
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        startService(new Intent(this, ServiceNotice.class));

        init();
        list_dir_block();
        notice_block();

        b_add_dir.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog();
            }
        });

        b_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(array_dir.length == 0){
                    Toast toast = Toast.makeText(MainActivity.this, "Нет словарей, нет и расписания!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0,0);
                    toast.show();
                }
                else{
                    Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(i);
                    finish();
                }

            }
        });

        lv_dir.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                int dir_id = array_dir_id[position];

                Intent j = new Intent(MainActivity.this, OpenDirActivity.class);
                j.putExtra("dir_id", dir_id);
                startActivity(j);

                finish();
            }
        });
    }

    private void init(){
        b_add_dir = findViewById(R.id.b_add_dir);
        b_settings = findViewById(R.id.b_settings);
        tv_notice = findViewById(R.id.tv_notice);
        lv_dir = findViewById(R.id.lv_dir);
    }

    private void notice_block(){
        if(array_dir.length == 0){
            tv_notice.setText("У вас еще нет словарей. Создайте свой первый словарь или добавьте готовый из каталога!");
        }
        else{
            array_dir_to_repetition = get_dirs_to_repetition();

            if(array_dir_to_repetition.length == 0){
                tv_notice.setText("Вы молодец :)");
            }
            else{
                String str = "ВАМ СЛЕДУЕТ ПОВТОРИТЬ: \n";
                for(int i=0; i<array_dir_to_repetition.length; i++){
                    str += array_dir_to_repetition[i] + " \n";
                }
                tv_notice.setText(str);
            }
        }
    }

    private String[] get_dirs_to_repetition(){
        List<String> list = new ArrayList<>();

        Date date = new Date();
        long sec = date.getTime()/(1000*60); // тут минуты прошедшие с 01.01.1970 00:00
        long dt = (sec - 52*365*24*60); // тут минуты прошедшие с 01.01.2022 00:00

        for(int i=0; i<get_dirs().id_dirs.length; i++){
            if(get_dirs().NextRepetitions_dirs[i] <= dt){
                list.add(get_dirs().names_dirs[i]);
            }
        }

        String[] array = new String[list.size()];
        for(int i=0; i<list.size(); i++){
            array[i] = list.get(i);
        }

        return array;
    }

    private void list_dir_block(){
        array_dir = get_dirs().names_dirs;
        array_dir_id = get_dirs().id_dirs;
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, array_dir);
        lv_dir.setAdapter(adapter);
    }

    private void dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ConstraintLayout cl = (ConstraintLayout) getLayoutInflater().inflate(R.layout.dialog_add_dir, null);
        builder.setView(cl);
        AlertDialog dialog = builder.show();

        Button b_cancel = dialog.findViewById(R.id.b_edit);
        Button b_ok = dialog.findViewById(R.id.b_ok);
        Button b_find_dir = dialog.findViewById(R.id.b_find_dir);
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
                EditText et_name_new_dir = dialog.findViewById(R.id.et_name_new_dir);
                String str = et_name_new_dir.getText().toString();
                if(str.length() == 0){
                    tv_msg.setText("Введите название словаря");
                }
                else{
                    add_dir(str);
                    dialog.dismiss();
                }
            }
        });

        b_find_dir.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent i = new Intent(MainActivity.this, CatalogActivity.class);
                startActivity(i);
                dialog.dismiss();
                finish();
            }
        });
    }

    private void add_dir(String name){

        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.KEY_NAME_dirs, name);
        contentValues.put(DBHelper.KEY_STATUS_dirs, 1);
        contentValues.put(DBHelper.KEY_CUSTOM_dirs, 1);
        contentValues.put(DBHelper.KEY_NUM_dirs, -1);

        // dt будет хранить количество МИНУТ прошедших с 01.01.2022 00:00
        Date date = new Date();
        long sec = date.getTime()/(1000*60); // тут минуты прошедшие с 01.01.1970 00:00
        long dt = (sec - 52*365*24*60); // тут минуты прошедшие с 01.01.2022 00:00
        contentValues.put(DBHelper.KEY_LAST_REPETITION_dirs, dt);
        contentValues.put(DBHelper.KEY_NEXT_REPETITION_dirs, dt+60);

        database.insert(DBHelper.TABLE_DIRS, null, contentValues);
        recreate();
    }

    public static ListDir get_dirs(){
        List<String> List_dir = new ArrayList<>();
        List<Integer> List_dir_id = new ArrayList<>();
        List<Integer> List_dir_NR = new ArrayList<>();

        String selection = DBHelper.KEY_STATUS_dirs + " = 1";

        SQLiteDatabase database = dbHelper.getWritableDatabase();
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
            @SuppressLint("Range") int next_rep = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_NEXT_REPETITION_dirs));

            List_dir.add(dir);
            List_dir_id.add(dir_id);
            List_dir_NR.add( next_rep);
        }
        cursor.close();

        ListDir listDir = new ListDir(List_dir, List_dir_id, List_dir_NR);

        return listDir;
    }
}