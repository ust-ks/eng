package com.example.eng;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper{

    public static int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "appDB";

    public static final String TABLE_WORDS = "words";
    public static final String TABLE_DIRS = "dirs";

    public static final String KEY_ID_words = "_idWord";
    public static final String KEY_WORD_words = "word";
    public static final String KEY_TRANSLATION_words = "translation";
    public static final String KEY_DIR_ID_words = "dirId";

    public static final String KEY_ID_dirs = "_idDir";
    public static final String KEY_NAME_dirs = "name";
    public static final String KEY_STATUS_dirs = "stat";
    public static final String KEY_CUSTOM_dirs = "custom";
    public static final String KEY_LAST_REPETITION_dirs = "lastRepetition";
    public static final String KEY_NEXT_REPETITION_dirs = "nextRepetition";
    public static final String KEY_NUM_dirs = "num";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("pragma foreign_keys = on;");

        db.execSQL("create table if not exists " + TABLE_DIRS + "("
                + KEY_ID_dirs + " integer primary key autoincrement, "
                + KEY_NAME_dirs + " text, "
                + KEY_STATUS_dirs + " integer, "
                + KEY_CUSTOM_dirs + " integer, "
                + KEY_LAST_REPETITION_dirs + " integer, "
                + KEY_NEXT_REPETITION_dirs + " integer, "
                + KEY_NUM_dirs + " integer);");

        db.execSQL("create table if not exists " + TABLE_WORDS + "("
                + KEY_ID_words + " integer primary key autoincrement, "
                + KEY_DIR_ID_words + " integer, "
                + KEY_WORD_words + " text, "
                + KEY_TRANSLATION_words + " text, "
                + "FOREIGN KEY (" + KEY_DIR_ID_words + ") REFERENCES " + TABLE_DIRS + "(" + KEY_ID_dirs + ") ON DELETE CASCADE);");

        DATABASE_VERSION = 2;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void loading_dictionaries(Context context, SQLiteDatabase db) throws IOException, ParseException {
        JSONParser parser = new JSONParser();

        String jsonText = readText(context, R.raw.data);

        JSONObject root = null;
        root = (JSONObject) parser.parse(jsonText);
        org.json.simple.JSONArray dirs = (org.json.simple.JSONArray) root.get("dictionaries");
        org.json.simple.JSONArray words = (org.json.simple.JSONArray) root.get("words");

        for (Object dir : dirs){
            JSONObject dirJSONObject = (JSONObject) dir;
            ContentValues contentValues = new ContentValues();
            String name = (String) dirJSONObject.get("name");

            contentValues.put(DBHelper.KEY_NAME_dirs, name);
            contentValues.put(DBHelper.KEY_STATUS_dirs, 0);
            contentValues.put(DBHelper.KEY_CUSTOM_dirs, 0);
            contentValues.put(DBHelper.KEY_LAST_REPETITION_dirs, 0);
            contentValues.put(DBHelper.KEY_NEXT_REPETITION_dirs, 0);
            contentValues.put(DBHelper.KEY_NUM_dirs, 0);

            db.insert(DBHelper.TABLE_DIRS, null, contentValues);
        }

        for (Object word : words) {
            JSONObject wordJSONObject = (JSONObject) word;
            ContentValues contentValues = new ContentValues();
            String str_dir_id = (String) wordJSONObject.get("dirid");
            String w = (String) wordJSONObject.get("word");
            String translation = (String) wordJSONObject.get("translation");

            int dir_id = Integer.parseInt (str_dir_id);

            contentValues.put(DBHelper.KEY_DIR_ID_words, (int) dir_id);
            contentValues.put(DBHelper.KEY_WORD_words, w);
            contentValues.put(DBHelper.KEY_TRANSLATION_words, translation);

            db.insert(DBHelper.TABLE_WORDS, null, contentValues);
        }

        DATABASE_VERSION = 3;
    }

    private static String readText(Context context, int resId) throws IOException {
        InputStream is = context.getResources().openRawResource(resId);
        BufferedReader br= new BufferedReader(new InputStreamReader(is));
        StringBuilder sb= new StringBuilder();
        String s= null;
        while((  s = br.readLine())!=null) {
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString();
    }
}
