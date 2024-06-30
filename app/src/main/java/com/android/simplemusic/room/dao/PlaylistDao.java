package com.android.simplemusic.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.simplemusic.room.entity.Playlist;

import java.util.List;

@Dao
public interface PlaylistDao {
    @Query("SELECT * FROM Playlist")
    List<Playlist> getAllPlaylists();

    @Query("SELECT * FROM Playlist WHERE timeCreated=:timeCreated")
    List<Playlist> getPlaylist(long timeCreated);

    @Insert(onConflict= OnConflictStrategy.REPLACE)
    void insertPlaylist(Playlist playlist);

    @Insert
    void insertPlaylists(List<Playlist> playlists);

    @Update(onConflict=OnConflictStrategy.REPLACE)
    int updatePlaylist(Playlist playlist);

    @Delete
    void deletePlaylist(Playlist playlist);

    @Query("DELETE FROM Playlist WHERE 1=1")
    void deleteAllPlaylists();
}
