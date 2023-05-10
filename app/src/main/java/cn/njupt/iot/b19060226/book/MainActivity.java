package cn.njupt.iot.b19060226.book;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;

public class MainActivity extends AppCompatActivity {

    private EditText etName, etPassword;
    private Button btLogin, btRegister;
    private CheckBox cbPassword, cbAutoLogin;
    private static String WRONG_INFO = "用户名或密码不正确，请重新输入";
    List<UserData> dataList = new ArrayList<>();
    RoomDB database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SQLiteStudioService.instance().start(this);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Initialize database
        database = RoomDB.getInstance(this);
        //Store database value in data list
        dataList = database.userDao().getAll();
    }

    private void init() {
        etName = (EditText) findViewById(R.id.name);
        etPassword = (EditText) findViewById(R.id.password);
        //复选框的监听事件
        cbPassword = (CheckBox) findViewById(R.id.cb_pwd);//记住密码
        cbAutoLogin = (CheckBox) findViewById(R.id.cb_auto_login);//自动登录
        SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        String rName = sp.getString("users", "");
        String rPassword = sp.getString("passwords", "");
        Integer id = sp.getInt("id",0);
        boolean choseRemember = sp.getBoolean("remember", false);
        boolean choseAutoLogin = sp.getBoolean("autologin", false);

        //Initialize database
        database = RoomDB.getInstance(this);
        //Store database value in data list
        dataList = database.userDao().getAll();

        //如果上次选了记住密码，那进入登录页面也自动勾选记住密码，并填上用户名和密码
        if (choseRemember) {
            etName.setText(rName);
            etPassword.setText(rPassword);
            cbPassword.setChecked(true);
        }
        //如果上次登录选了自动登录，那进入登录页面也自动勾选自动登录
        if (choseAutoLogin) {
            cbAutoLogin.setChecked(true);
        }

        //是否自动登录
        if (cbAutoLogin.isChecked()) {
            Intent intent1 = new Intent();
            intent1.setClass(this, ContentActivity.class);
            intent1.putExtra("user_id", id);
            startActivity(intent1);
        }

        //注册按钮的事件监听
        btRegister = (Button) findViewById(R.id.bt_register);
        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, cn.njupt.iot.b19060226.book.RegisterActivity.class);
                startActivity(intent);
            }
        });

        //登录按钮的事件监听
        btLogin = (Button) findViewById(R.id.bt_login);
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String struser = etName.getText().toString();
                String strpwd = etPassword.getText().toString();
                boolean login_succ = false;
                String username,password;
                Integer id;

                for(UserData d : dataList) {
                    id = d.getID();
                    username = d.getName();
                    password = d.getPassword();
                    if(struser.equals(username) && strpwd.equals(password)) {
                        login_succ = true;
                        //将用户名存储到sharedpreferences中
                        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        //是否记住密码
                        if (cbPassword.isChecked()) {
                            editor.putInt("id",id);
                            editor.putString("users", username);
                            editor.putString("passwords", password);
                            editor.putBoolean("remember", true);
                        } else {
                            editor.putBoolean("remember", false);
                        }
                        //是否自动登录
                        if (cbAutoLogin.isChecked()) {
                            editor.putBoolean("autologin", true);
                        } else {
                            editor.putBoolean("autologin", false);
                        }
                        editor.apply();
                        Intent intent = new Intent(MainActivity.this, ContentActivity.class);
                        intent.putExtra("user_id",id);
                        startActivity(intent);
                        break;
                    }
                }
                if(!login_succ){
                    Toast.makeText(MainActivity.this, WRONG_INFO, Toast.LENGTH_SHORT).show();
                }
            }

        });
    }


}