package cn.njupt.iot.b19060226.book;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ContentActivity extends AppCompatActivity {
    Integer id;
    TextView tvSort, tvExamine, tvSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        init();
    }

    public void init() {
        Intent intent = getIntent();
        id = intent.getIntExtra("user_id",0);
        Log.e("test",""+id);

        tvSort = findViewById(R.id.tv_sort);
        tvExamine = findViewById(R.id.tv_examine);
        tvSetting = findViewById(R.id.tv_setting);

        tvSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(ContentActivity.this,SortActivity.class);
                intent1.putExtra("user_id",id);
                startActivity(intent1);
            }
        });

        tvExamine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(ContentActivity.this,ExamineActivity.class);
                intent2.putExtra("user_id",id);
                startActivity(intent2);
            }
        });

        tvSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent3 = new Intent(ContentActivity.this,SettingActivity.class);
                intent3.putExtra("user_id",id);
                startActivity(intent3);
            }
        });
    }
}