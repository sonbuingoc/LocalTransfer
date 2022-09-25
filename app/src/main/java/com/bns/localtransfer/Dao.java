package com.bns.localtransfer;

import androidx.room.Delete;

@androidx.room.Dao
public interface Dao {
    @Delete
    void Delete();
}
