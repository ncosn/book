package cn.njupt.iot.b19060226.book;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface TestDao {

    @Insert(onConflict = REPLACE)
    void insert(TestData testData);

    @Query("SELECT * FROM test WHERE user_id = :user_id")
    List<TestData> getAl(int user_id);

}
