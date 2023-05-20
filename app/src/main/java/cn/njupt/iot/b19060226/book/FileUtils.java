package cn.njupt.iot.b19060226.book;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtils {
    public static File getSaveFile() {
        //保存到本地
        String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
        String fileName = "photo_" + time;
        File mFile = new File(Environment.getExternalStorageDirectory() + "/take_photo/", fileName + ".jpeg");
        if (!mFile.getParentFile().exists()) {
            mFile.getParentFile().mkdirs();
        }
        return mFile;
    }
}
