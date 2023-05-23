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

    @Query("SELECT * FROM test WHERE user_id = :user_id AND (subject LIKE '%'||:words||'%' OR question LIKE '%'||:words||'%' OR orc_question LIKE '%'||:words||'%')")
    List<TestData> queryWords(int user_id, String words);

    @Query("SELECT COUNT(*) FROM test WHERE user_id = :user_id")
    int getCount(int user_id);

    @Query("select * from test WHERE user_id = :user_id order by random() limit 0,10")
    List<TestData> getPaper(int user_id);

    @Update
    void update(TestData data);

    @Delete
    void delete(TestData data);
}
