package com.shivora.puwifimanager.model.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    LiveData<List<UserEntry>> loadAllUsers();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertUser(UserEntry user);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateUser(UserEntry user);

    @Delete
    void deleteUser(UserEntry user);

    @Query("SELECT * FROM users WHERE user_id = :userId")
    LiveData<UserEntry> loadUserById(String userId);
}
