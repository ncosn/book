package cn.njupt.iot.b19060226.book;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingActivity extends AppCompatActivity {

    EditText etName, etPassword, etTel, etEducation, etEmail;
    Button btBack, btSave;
    RoomDB database;
    public int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        init();

    }

    public void init() {
        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id",0);

        etName = findViewById(R.id.et_name);
        etTel = findViewById(R.id.et_tel);
        etEducation = findViewById(R.id.et_education);
        etPassword = findViewById(R.id.et_password);
        etEmail = findViewById(R.id.et_email);
        btBack = findViewById(R.id.bt_back);
        btSave = findViewById(R.id.bt_save);
        database = RoomDB.getInstance(SettingActivity.this);
        UserData data = new UserData();
        data = database.userDao().getUser(userId);
        etName.setText(data.getName());
        etPassword.setText(data.getPassword());
        etEducation.setText(data.getEducation());
        etTel.setText(data.getTel());
        etEmail.setText(data.getEmail());

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserData data = new UserData();
                data.setID(userId);
                data.setName(etName.getText().toString());
                data.setTel(etTel.getText().toString());
                data.setEducation(etEducation.getText().toString());
                data.setPassword(etPassword.getText().toString());
                data.setEmail(etEmail.getText().toString());
                database.userDao().updateUser(data);
                finish();
            }
        });


    }
}