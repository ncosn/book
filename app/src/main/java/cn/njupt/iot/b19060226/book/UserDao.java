package cn.njupt.iot.b19060226.book;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface UserDao {

    @Insert(onConflict = REPLACE)
    void insert(UserData userData);

    @Query("Update USER SET name = :sname, education = :seducation, tel = :tel WHERE ID = :sID")
    void update(int sID, String sname, String seducation, String tel);

    @Query("SELECT * FROM USER")
    List<UserData> getAll();

}
