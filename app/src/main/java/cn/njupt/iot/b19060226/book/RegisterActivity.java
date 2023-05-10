package cn.njupt.iot.b19060226.book;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etPassword, etEducation, etTel;
    Button btBack,btRegister;
    RoomDB database;
    List<UserData> dataList = new ArrayList<>();
    private static String WRONG_INFO = "用户名已存在";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    public void init() {
        etName = (EditText) findViewById(R.id.et_name);
        etPassword = (EditText) findViewById(R.id.et_password);
        etEducation = (EditText) findViewById(R.id.et_education);
        etTel = (EditText) findViewById(R.id.et_phone);
        btBack = findViewById(R.id.bt_back);
        btRegister = findViewById(R.id.bt_register);
        database = RoomDB.getInstance(this);
        dataList = database.userDao().getAll();

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserData data = new UserData();
                String name = etName.getText().toString();
                String password = etPassword.getText().toString();
                String education = etEducation.getText().toString();
                String tel = etTel.getText().toString();
                for(UserData d : dataList) {
                    if(name.equals(d.getName())) {
                        Toast.makeText(RegisterActivity.this, WRONG_INFO, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                data.setName(name);
                data.setPassword(password);
                data.setEducation(education);
                data.setTel(tel);
                database.userDao().insert(data);
                finish();
            }
        });
    }
}