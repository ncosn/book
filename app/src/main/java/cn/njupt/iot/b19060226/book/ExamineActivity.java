package cn.njupt.iot.b19060226.book;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExamineActivity extends AppCompatActivity {
    Integer userId;
    int position,number;
    List<TestData> dataList = new ArrayList<>();
    RoomDB database;
    TextView tvPosition, tvLast, tvNext, tvQuestion;
    EditText etScore;
    Button btAnswer,btCommit;

    private static final int NUM = 10;
    int[] scores = new int[NUM];
    Pattern p = Pattern.compile("<img src=\".*?\"/>");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examine);
        init();
    }

    public void init() {
        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id",0);
        position = 0;

        tvPosition = findViewById(R.id.tv_position);
        tvLast = findViewById(R.id.tv_last);
        tvNext = findViewById(R.id.tv_next);
        tvQuestion = findViewById(R.id.tv_question);
        etScore = findViewById(R.id.et_score);
        btAnswer = findViewById(R.id.bt_answer);
        btCommit = findViewById(R.id.bt_commit);

        database = RoomDB.getInstance(ExamineActivity.this);
        int total = database.testDao().getCount(userId);
        if (NUM > total) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ExamineActivity.this);
            builder.setMessage("错题数量不到10道哦！");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            builder.setNegativeButton("返回", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            builder.show();
        } else {
            dataList = database.testDao().getPaper(userId);
            number = dataList.size();
            position = 1;
            initContent(1);
        }

        btCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String scoreString = etScore.getText().toString();
                int score = ("".equals(scoreString))? 0 : Integer.parseInt(scoreString);
                if (10 < score) {
                    Toast.makeText(ExamineActivity.this,"分数为0-10",Toast.LENGTH_SHORT).show();
                    return;
                }
                scores[position-1] = score;
                int grade = 0;
                for(int i : scores) {
                    grade += i;
                }
                Toast toast = Toast.makeText(ExamineActivity.this,"本次测试得分为:"+grade,Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String scoreString = etScore.getText().toString();
                int score = ("".equals(scoreString))? 0 : Integer.parseInt(scoreString);
                if (10 < score) {
                    Toast.makeText(ExamineActivity.this,"分数为0-10",Toast.LENGTH_SHORT).show();
                    return;
                }
                scores[position-1] = score;
                if (position == 1) {
                    tvLast.setVisibility(View.VISIBLE);
                }
                position++;
                initContent(position);
                if (position == NUM) {
                    tvNext.setVisibility(View.GONE);
                }
            }
        });

        tvLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String scoreString = etScore.getText().toString();
                int score = ("".equals(scoreString))? 0 : Integer.parseInt(scoreString);
                if (10 < score) {
                    Toast.makeText(ExamineActivity.this,"分数为0-10",Toast.LENGTH_SHORT).show();
                    return;
                }
                scores[position-1] = score;
                if (position == NUM) {
                    tvNext.setVisibility(View.VISIBLE);
                }
                position--;
                initContent(position);
                if (position == 1) {
                    tvLast.setVisibility(View.GONE);
                }
            }
        });

        btAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WindowManager wm = getWindowManager();
                int dialogWidth = wm.getDefaultDisplay().getWidth();
                int dialogHeight = wm.getDefaultDisplay().getHeight();

                Dialog dialog = new Dialog(ExamineActivity.this);
                dialog.setContentView(R.layout.dialog_answer);

                //Show dialog
                dialog.show();
                //Set layout
                dialog.getWindow().setLayout(dialogWidth,dialogHeight);

                TextView tvAnswer = dialog.findViewById(R.id.tv_answer);
                ImageView ivClose = dialog.findViewById(R.id.iv_close);
                ivClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                TestData d = dataList.get(position-1);
                String input = d.getAnswer();
                Matcher m = p.matcher(input);
                //List<String> result = new ArrayList<String>();

                SpannableString spannable = new SpannableString(input);
                while(m.find()){
                    String s = m.group();
                    int start = m.start();
                    int end = m.end();
                    String path = s.replaceAll("<img src=\"|\"/>","").trim();

                    //利用spannableString和ImageSpan来替换掉这些图片
                    int width = ScreenUtils.getScreenWidth(ExamineActivity.this);
                    int height = ScreenUtils.getScreenHeight(ExamineActivity.this);

                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                        bitmap = ImageUtils.zoomImage(bitmap,(width-32)*0.9,bitmap.getHeight()/(bitmap.getWidth()/((width-32)*0.9)));
                        ImageSpan imageSpan = new ImageSpan(ExamineActivity.this, bitmap);
                        spannable.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                tvAnswer.setText(spannable);

            }
        });
    }

    public void initContent(int position) {
        //设置文本
        tvPosition.setText("第"+position+"/"+number+"题");
        etScore.setText(""+scores[position-1]);

        TestData d = dataList.get(position-1);
        String input = d.getQuestion();
        Matcher m = p.matcher(input);
        //List<String> result = new ArrayList<String>();

        SpannableString spannable = new SpannableString(input);
        while(m.find()){
            String s = m.group();
            int start = m.start();
            int end = m.end();
            String path = s.replaceAll("<img src=\"|\"/>","").trim();

            //利用spannableString和ImageSpan来替换掉这些图片
            int width = ScreenUtils.getScreenWidth(ExamineActivity.this);
            int height = ScreenUtils.getScreenHeight(ExamineActivity.this);

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                bitmap = ImageUtils.zoomImage(bitmap,(width-32)*0.9,bitmap.getHeight()/(bitmap.getWidth()/((width-32)*0.9)));
                ImageSpan imageSpan = new ImageSpan(ExamineActivity.this, bitmap);
                spannable.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        tvQuestion.setText(spannable);
    }
}