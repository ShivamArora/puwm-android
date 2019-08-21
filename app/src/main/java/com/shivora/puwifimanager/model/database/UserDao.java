package com.shivora.puwifimanager.model.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

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
