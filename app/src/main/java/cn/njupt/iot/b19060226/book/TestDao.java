package cn.njupt.iot.b19060226.book;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface TestDao {

    @Insert(onConflict = REPLACE)
    void insert(TestData testData);

    @Query("SELECT * FROM test WHERE user_id = :user_id ORDER BY time DESC,ID DESC")
    List<TestData> getAl(int user_id);

    @Update
    void update(TestData data);

    @Delete
    void delete(TestData data);
}
