package com.android.simplemusic.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.simplemusic.bean.Music;
import com.android.simplemusic.definition.Definition;

import java.util.ArrayList;
import java.util.Arrays;

public class PlaylistDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "playlist.db"; //数据库的名称
    private static final ArrayList<String> TABLE_NAMES = new ArrayList<String>(
            Arrays.asList(Definition.PLAYLIST_MY_FAVORITE, Definition.PLAYLIST_RECENT_PLAY)); //数据库各表的名称列表
    private static PlaylistDBHelper mHelper = null; //数据库帮助器的实例
    private SQLiteDatabase mDB = null; //数据库的实例

    public PlaylistDBHelper(Context context, int version) {
        super(context, DB_NAME, null, version);
    }

    // 利用单例模式获取数据库帮助器的唯一实例
    public static PlaylistDBHelper getInstance(Context context, int version) {
        if (version > 0 && mHelper == null) {
            mHelper = new PlaylistDBHelper(context, version);
        }
        return mHelper;
    }

    // 打开数据库的读连接
    public SQLiteDatabase openReadLink() {
        if (mDB == null || !mDB.isOpen()) {
            mDB = mHelper.getReadableDatabase();
        }
        return mDB;
    }

    // 打开数据库的写连接
    public SQLiteDatabase openWriteLink() {
        if (mDB == null || !mDB.isOpen()) {
            mDB = mHelper.getWritableDatabase();
        }
        return mDB;
    }

    // 关闭数据库的连接
    public void closeLink() {
        if (mDB != null && mDB.isOpen()) {
            mDB.close();
            mDB = null;
        }
    }

    // 创建数据库，执行建表语句
    @Override
    public void onCreate(SQLiteDatabase db) {
        for (int i = 0; i < TABLE_NAMES.size(); i++) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAMES.get(i) + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "name text," +
                    "singer text," +
                    "path text," +
                    "size integer," +
                    "duration integer)");
        }
    }

    // 升级数据库，执行表结构变更语句
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // 删除记录
    public int delete(String tableName, String condition) {
        if (TABLE_NAMES.contains(tableName)) {
            return mDB.delete(tableName, condition, null);
        } else {
            return -1;
        }
    }

    // 插入记录
    public long insert(String tableName, ArrayList<Music> musicList) {
        long result = -1;
        if (TABLE_NAMES.contains(tableName)) {
            for (int i = 0; i < musicList.size(); i++) {
                Music musicItem = musicList.get(i);
                ContentValues cv = new ContentValues();
                cv.put("name", musicItem.getTitle());
                cv.put("singer", musicItem.getArtist());
                cv.put("path", musicItem.getPath());
                cv.put("size", musicItem.getSize());
                cv.put("duration", musicItem.getDuration());
                result = mDB.insert(tableName, "", cv);
                if (result == -1) {
                    return result;
                }
            }
        }
        return result;
    }

    // 更新记录
    public int update(Music musicItem, String tableName, String condition) {
        if (TABLE_NAMES.contains(tableName)) {
            ContentValues cv = new ContentValues();
            cv.put("name", musicItem.getTitle());
            cv.put("singer", musicItem.getArtist());
            cv.put("path", musicItem.getPath());
            cv.put("size", musicItem.getSize());
            cv.put("duration", musicItem.getDuration());
            return mDB.update(tableName, cv, condition, null);
        } else {
            return -1;
        }
    }

    // 查找记录
    public ArrayList<Music> query(String tableName, String condition) {
        if (TABLE_NAMES.contains(tableName)) {
            ArrayList<Music> queryResult = new ArrayList<Music>();
            String sql = String.format("select name,singer,path,size,duration from %s where %s", tableName, condition);
            Cursor cursor = mDB.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String singer = cursor.getString(1);
                String path = cursor.getString(2);
                int size = cursor.getInt(3);
                long duration = cursor.getLong(4);
                Music music = new Music(name, singer, path, size, duration);
                queryResult.add(music);
            }
            cursor.close();
            return queryResult;
        } else {
            return null;
        }
    }
}
