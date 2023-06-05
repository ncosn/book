package cn.njupt.iot.b19060226.book;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class MyDialog extends Dialog {
    int flag = 0;
    public static String TAG = "MyDialog";
    public static String FORMAT = "yyyy-MM-dd HH:mm";
    private static SortActivity context;
    private static int type = 1;
    private static int level = 0;
    private int userId;
    ImageView ivClose;
    static EditText etQuestion, etAnswer, etSubject;
    Spinner spLevel;
    RadioGroup rgType;
    RadioButton rbQuestion, rbAnswer;
    RelativeLayout rlQuestion, rlAnswer;
    ImageView ivGallery, ivCamera;
    TextView tvSave;
    RoomDB database;
    TestData d;

    private onGalleryOnClickListener galleryOnClickListener;
    private onCameraOnClickListener cameraOnClickListener;
    private typeInterface myTypeInterface;
//    private onCloseOnClickListener closeOnClickListener;

    public MyDialog(@NonNull SortActivity Acontext,int userID,int add) {
        super(Acontext);
        context = Acontext;
        userId = userID;
        flag = 0;
    }

    public MyDialog(@NonNull SortActivity Acontext,int userID,TestData d) {
        super(Acontext);
        context = Acontext;
        userId = userID;
        flag = 1;
        this.d = d;
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
        initData();
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

        ivGallery = findViewById(R.id.iv_gallery);
        ivCamera = findViewById(R.id.iv_camera);

        tvSave = findViewById(R.id.tv_save);

    }

    private void initData() {
        type = 1;
        level = 1;
        switch (flag) {
            case 0:
                break;
            case 1:
                etSubject.setText(d.getSubject());
                spLevel.setSelection(d.getLevel());
                initContent();
                break;
            default:
                break;
        }

        database = RoomDB.getInstance(context);
    }

    private void initEvent() {
        rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb_question:
                        rlQuestion.setVisibility(View.VISIBLE);
                        type = 1;
                        myTypeInterface.getType(type);
                        break;
                    case R.id.rb_answer:
                        rlQuestion.setVisibility(View.GONE);
                        type = 2;
                        myTypeInterface.getType(type);
                        break;
                    default:
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

        //下拉框点击事件
        spLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                level = i;
//                Log.e("test",""+i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("".equals(etSubject.getText().toString())) {
                    Toast.makeText(context,"请输入科目",Toast.LENGTH_SHORT).show();
                    return;
                } else if("".equals(etQuestion.getText().toString())) {
                    Toast.makeText(context,"请输入题目",Toast.LENGTH_SHORT).show();
                    return;
                }
                Date date = new Date();
                SimpleDateFormat dateFormat= new SimpleDateFormat(FORMAT);
                String time = dateFormat.format(date);
                TestData data = new TestData();
                data.setTime(time);
                data.setSubject(etSubject.getText().toString().trim());
                data.setLevel(level);
                data.setUser_id(userId);
                data.setQuestion(etQuestion.getText().toString());
                data.setAnswer(etAnswer.getText().toString());
                switch (flag) {
                    case 0:
                        database.testDao().insert(data);
                        Log.d(TAG, "onClick: 新建");
                        break;
                    case 1:
                        data.setID(d.getID());
                        database.testDao().update(data);
                        Log.d(TAG, "onClick: 更新");
                        break;
                    default:
                        break;
                }
                dismiss();
                context.testAdapter.dataList = database.testDao().getAl(userId);
                context.testAdapter.notifyDataSetChanged();
            }
        });

        //编辑框滑动事件
        etQuestion.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //触摸的是EditText并且当前EditText可以滚动则将事件交给EditText处理；否则将事件交由其父类处理
                if ((view.getId() == R.id.et_question)) {
                    //垂直方向上可以滚动
                    if(etQuestion.canScrollVertically(-1) || etQuestion.canScrollVertically(0)) {
                        //请求父控件不拦截滑动事件
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    }
                }
                return false;
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

    public void setTypeInterface(typeInterface myTypeInterface) {
        this.myTypeInterface = myTypeInterface;
    }

//    private interface onCloseOnClickListener {
//        public void onCloseClick();
//    }

    public interface onGalleryOnClickListener {
        public void onGalleryClick();
    }

    public interface onCameraOnClickListener {
        public void onCameraClick();
    }

    public interface typeInterface {
        public void getType(int type);
    }

    public void insertImg(String path){
//        Log.e(TAG, "insertImg:" + path);
        String tagPath = "<img src=\""+path+"\"/>";//为图片路径加上<img>标签
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if(bitmap != null){
            SpannableString ss = getBitmapMime(path, tagPath);
            insertPhotoToEditText(ss);
            etQuestion.append("\n");
//            Log.e(TAG, etQuestion.getText().toString());

        }else{
            //Log.d("YYPT_Insert", "tagPath: "+tagPath);
            Toast.makeText(context,"插入失败，无读写存储权限，请到权限中心开启",Toast.LENGTH_LONG).show();
        }
    }

    public static SpannableString getBitmapMime(String path, String tagPath) {
        SpannableString ss = new SpannableString(tagPath);//这里使用加了<img>标签的图片路径
//        Log.e("AddActivity","ss1:"+ss.toString());

        int width = ScreenUtils.getScreenWidth(context);
        int height = ScreenUtils.getScreenHeight(context);

//        Log.d("YYPT_IMG_SCREEN", "高度:"+height+",宽度:"+width);

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
//        Log.d("YYPT_IMG_IMG", "高度:"+bitmap.getHeight()+",宽度:"+bitmap.getWidth());
        bitmap = ImageUtils.zoomImage(bitmap,(width-32)*0.9,bitmap.getHeight()/(bitmap.getWidth()/((width-32)*0.9)));

        /*
        //高:754，宽1008
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        */
        Log.d("YYPT_IMG_COMPRESS", "高度："+bitmap.getHeight()+",宽度:"+bitmap.getWidth());

        ImageSpan imageSpan = new ImageSpan(context, bitmap);
        ss.setSpan(imageSpan, 0, tagPath.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        Log.e(TAG,"ss2:"+ss.toString());
        return ss;
    }

    //将图片插入到EditText中
    public static void insertPhotoToEditText(SpannableString ss){
        Editable et;
        int start;
        switch (type) {
            case 1:
                et = etQuestion.getText();
                start = etQuestion.getSelectionStart();
                et.insert(start,ss);
                etQuestion.setText(et);
                etQuestion.setSelection(start+ss.length());
                etQuestion.setFocusableInTouchMode(true);
                etQuestion.setFocusable(true);
                break;
            case 2:
                et = etAnswer.getText();
                start = etAnswer.getSelectionStart();
                et.insert(start,ss);
                etAnswer.setText(et);
                etAnswer.setSelection(start+ss.length());
                etAnswer.setFocusableInTouchMode(true);
                etAnswer.setFocusable(true);
                break;
            default:
                break;
        }
    }

    private void initContent(){
        String input = d.getQuestion();
        //String regex = "<img src=\\".*?\\"\\/>";
        Pattern p = Pattern.compile("\\<img src=\".*?\"\\/>");
        Matcher m = p.matcher(input);
        //List<String> result = new ArrayList<String>();


        SpannableString spannable = new SpannableString(input);
        while(m.find()){
            //Log.d("YYPT_RGX", m.group());
            //这里s保存的是整个式子，即<img src="xxx"/>，start和end保存的是下标
            String s = m.group();
            int start = m.start();
            int end = m.end();
            //path是去掉<img src=""/>的中间的图片路径
            String path = s.replaceAll("<img src=\"|\"/>","").trim();
            //Log.d("YYPT_AFTER", path);

            //利用spannableString和ImageSpan来替换掉这些图片
            int width = ScreenUtils.getScreenWidth(context);
            int height = ScreenUtils.getScreenHeight(context);

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                bitmap = ImageUtils.zoomImage(bitmap,(width-32)*0.9,bitmap.getHeight()/(bitmap.getWidth()/((width-32)*0.9)));
                ImageSpan imageSpan = new ImageSpan(context, bitmap);
                spannable.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        etQuestion.setText(spannable);
        //content.append("\n");
        //Log.d("YYPT_RGX_SUCCESS",content.getText().toString());

        input = d.getAnswer();
        //String regex = "<img src=\\".*?\\"\\/>";
//        Pattern p = Pattern.compile("\\<img src=\".*?\"\\/>");
        m = p.matcher(input);
        //List<String> result = new ArrayList<String>();


        spannable = new SpannableString(input);
        while(m.find()){
            //Log.d("YYPT_RGX", m.group());
            //这里s保存的是整个式子，即<img src="xxx"/>，start和end保存的是下标
            String s = m.group();
            int start = m.start();
            int end = m.end();
            //path是去掉<img src=""/>的中间的图片路径
            String path = s.replaceAll("<img src=\"|\"/>","").trim();
            //Log.d("YYPT_AFTER", path);

            //利用spannableString和ImageSpan来替换掉这些图片
            int width = ScreenUtils.getScreenWidth(context);
            int height = ScreenUtils.getScreenHeight(context);

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                bitmap = ImageUtils.zoomImage(bitmap,(width-32)*0.9,bitmap.getHeight()/(bitmap.getWidth()/((width-32)*0.9)));
                ImageSpan imageSpan = new ImageSpan(context, bitmap);
                spannable.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        etAnswer.setText(spannable);
    }
    //endregion


}
