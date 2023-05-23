package cn.njupt.iot.b19060226.book;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etPassword, etEducation, etTel, etEmail;
    Button btBack,btRegister;
    RoomDB database;
    List<UserData> dataList = new ArrayList<>();
    private static String WRONG_INFO = "用户名已存在";
    private static String WRONG_INFO2 = "邮箱格式错误";

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
        etEmail =  findViewById(R.id.et_email);
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
                String email = etEmail.getText().toString().trim();

                Pattern pattern = Pattern.compile("^[A-Za-z0-9][\\w\\._]*[a-zA-Z0-9]+@[A-Za-z0-9-_]+\\.([A-Za-z]{2,4})");
                Matcher matcher = pattern.matcher(email);
                if(!matcher.matches()){
                    Toast.makeText(RegisterActivity.this, WRONG_INFO2, Toast.LENGTH_LONG).show();
                    return;
                }

                data.setName(name);
                data.setPassword(password);
                data.setEducation(education);
                data.setTel(tel);
                data.setEmail(email);
                database.userDao().insert(data);
                finish();
            }
        });
    }
}