package cn.njupt.iot.b19060226.book;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class MyDialog extends Dialog {
    private boolean hasPermission = false;
    private int type = 1;
    ImageView ivClose;
    EditText etQuestion, etAnswer, etSubject;
    Spinner spLevel;
    RadioGroup rgType;
    RadioButton rbQuestion, rbAnswer;
    RelativeLayout rlQuestion, rlAnswer;
    ImageView ivGallery, ivCamera;
    TextView tvSave;
    Context context;

    private onGalleryOnClickListener galleryOnClickListener;
    private onCameraOnClickListener cameraOnClickListener;
//    private onCloseOnClickListener closeOnClickListener;

    public MyDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_test);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);
        //初始化界面控件
        initView();
//        //初始化界面数据
//        initData();
        //初始化界面控件的事件
        initEvent();
    }

    private void initView() {
        etQuestion = findViewById(R.id.et_question);
        etAnswer = findViewById(R.id.et_answer);
        etSubject = findViewById(R.id.et_subject);
        spLevel = findViewById(R.id.sp_level);
        rgType = findViewById(R.id.rg_type);
        rbQuestion = findViewById(R.id.rb_question);
        rbAnswer = findViewById(R.id.rb_answer);

        rlQuestion = findViewById(R.id.rl_question);
        rlAnswer = findViewById(R.id.rl_answer);
        ivClose = findViewById(R.id.im_close);
    }

    private void initEvent() {
        rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb_question:
                        rlQuestion.setVisibility(View.VISIBLE);
                        type = 1;
                        break;
                    case R.id.rb_answer:
                        rlQuestion.setVisibility(View.GONE);
                        type = 2;
                        break;
                }
            }
        });

        ivGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (galleryOnClickListener != null) {
                    galleryOnClickListener.onGalleryClick();
                }
            }
        });
        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cameraOnClickListener != null) {
                    cameraOnClickListener.onCameraClick();
                }
            }
        });
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
//                if (closeOnClickListener != null) {
//                    closeOnClickListener.onCloseClick();
//                }
            }
        });
    }

    /**
     * 设置图库按钮的监听
     *
     * @param onGalleryOnClickListener
     */
    public void setGalleryOnClickListener(onGalleryOnClickListener onGalleryOnClickListener) {
        this.galleryOnClickListener = onGalleryOnClickListener;
    }

    /**
     * 设置拍照按钮的监听
     *
     * @param onCameraOnClickListener
     */
    public void setCameraOnClickListener(onCameraOnClickListener onCameraOnClickListener) {
        this.cameraOnClickListener = onCameraOnClickListener;
    }

//    private interface onCloseOnClickListener {
//        public void onCloseClick();
//    }

    private interface onGalleryOnClickListener {
        public void onGalleryClick();
    }

    private interface onCameraOnClickListener {
        public void onCameraClick();
    }


}
