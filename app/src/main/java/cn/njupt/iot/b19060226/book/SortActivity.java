package cn.njupt.iot.b19060226.book;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import com.baidu.ocr.sdk.OCR;
//import com.baidu.ocr.sdk.OnResultListener;
//import com.baidu.ocr.sdk.exception.OCRError;
//import com.baidu.ocr.sdk.model.AccessToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SortActivity extends AppCompatActivity {
    private static final int TAKE_PHOTO = 0;  //拍照
    private static final int REQUEST_CROP = 1; //裁剪
    private static final int OPEN_GALLERY = 2; //相册
    private static final int REQUEST_PERMISSION = 100;
    public boolean hasPermission = false;
    public boolean hasGotToken = false;
    private File imgFile;// 拍照保存的图片文件
    private Uri imgUri; // 拍照时返回的uri
    private Uri mCutUri;// 图片裁剪时返回的uri
    private Uri mClearUri;// 清除手写时返回的uri
//    private String result;//返回的字符串数据
    private String imageBase64;
    private Handler handler;
//    Dialog dialog;
    MyDialog myDialog;
    EditText etQuestion, etAnswer;
    Integer userId;
    public int type = 1;

    RoomDB database;
    RecyclerView recyclerView;
    public List<TestData> dataList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    public TestAdapter testAdapter;

    Button btAdd;
    EditText etQuery;
    TextView tvQuery;
    private static String TAG = "SortActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort);
        init();

        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg){
                //得到的result为base64字符串，转化为bitmap
                Bitmap bm = ImageUtils.base64ToBitmap(imageBase64);
//                //保存到本地
//                String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
//                String fileName = "photo_" + time;
//                File mClearFile = new File(Environment.getExternalStorageDirectory() + "/take_photo/", fileName + ".jpeg");
//                if (!mClearFile.getParentFile().exists()) {
//                    mClearFile.getParentFile().mkdirs();
//                }
                File mClearFile = FileUtils.getSaveFile();
                mClearUri = Uri.fromFile(mClearFile);
                Log.d(TAG, "handleMessage: mClearUri"+mClearUri);
                try {

                    FileOutputStream saveImgOut = new FileOutputStream(mClearFile);
                    // compress - 压缩的意思
                    bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                    //存储完成后需要清除相关的进程
                    saveImgOut.flush();
                    saveImgOut.close();
                    //更新相册
                    Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intentBc.setData(mClearUri);
                    sendBroadcast(intentBc);
                    Log.d(TAG, "The picture is save to your phone!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //清除手写图片的path
                myDialog.insertImg(mClearUri.getPath());
            }
        };
    }

    public void init() {
//        initAccessTokenLicenseFile();//证书
        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id",0);

        recyclerView = findViewById(R.id.recyclerview);
        btAdd = findViewById(R.id.bt_add);
        etQuery = findViewById(R.id.et_query);
        tvQuery = findViewById(R.id.tv_query);

        database = RoomDB.getInstance(this);
        dataList = database.testDao().getAl(userId);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        testAdapter = new TestAdapter(SortActivity.this,dataList);
        recyclerView.setAdapter(testAdapter);

        WindowManager wm = this.getWindowManager();
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
//        //dialog
//        dialog = new Dialog(SortActivity.this);
//        dialog.setContentView(R.layout.detail_test);
//        etQuestion = dialog.findViewById(R.id.et_question);
//        etAnswer = dialog.findViewById(R.id.et_answer);

        //添加按钮
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog = new MyDialog(SortActivity.this, userId, -1);

//                int width = ScreenUtils.getScreenWidth(SortActivity.this);
                //Initialize height
//                int height = ScreenUtils.getScreenHeight(SortActivity.this);

                //Show dialog
                myDialog.show();

                //Set layout
                myDialog.getWindow().setLayout(width,height);

                myDialog.setGalleryOnClickListener(new MyDialog.onGalleryOnClickListener() {
                    @Override
                    public void onGalleryClick() {
                        checkPermissions();
                        if (hasPermission) {
                            openGallery();
                        }
                    }
                });
                myDialog.setCameraOnClickListener(new MyDialog.onCameraOnClickListener() {
                    @Override
                    public void onCameraClick() {
                        checkPermissions();
                        if (hasPermission) {
                            takePhoto();
                        }
                    }
                });
                myDialog.setTypeInterface(new MyDialog.typeInterface() {
                    @Override
                    public void getType(int myType) {
                        type = myType;
                    }
                });

            }
        });

        tvQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String words = etQuery.getText().toString().trim();
                if(!("".equals(words))) {
                    dataList.clear();
                    dataList.addAll(database.testDao().queryWords(userId, words));
                    testAdapter.notifyDataSetChanged();
                } else {
                    dataList.clear();
                    dataList.addAll(database.testDao().getAl(userId));
                    testAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, OPEN_GALLERY);
    }

    public void takePhoto(){
        // 要保存的文件名
        String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
        String fileName = "photo_" + time;
        // 创建一个文件夹
        String path = Environment.getExternalStorageDirectory() + "/take_photo";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        // 要保存的图片文件
        imgFile = new File(file, fileName + ".jpeg");
        // 将file转换成uri
//        // 注意7.0及以上与之前获取的uri不一样了，返回的是provider路径
//        imgUri = getUriForFile(this, imgFile);

//        //创建file对象储存拍摄到的照片,将图片命名为output_image.jpg，将他存储在sd卡的关联目录下，调用getExternalCacheDir()
//        //方法可以获得这个目录
//        File outputImg=new File(getExternalCacheDir(),"output_image.jpg");
//        try {
//            if (outputImg.exists()){
//                outputImg.delete();
//            }
//            outputImg.createNewFile();
//        }catch (IOException e){
//            e.printStackTrace();
//        }

        //判断系统版本，低于7.0会将file对象转换为uir对象否则调用getUriForFile将file对象转化为一个封装过的uri对象
        //因为7.0开始直接使用本地真实路径会被认为是不安全的会抛出FileUriExposedException异常，FileProvider是一个
        //内容提供器会将封装的uri提供给外部
        if (Build.VERSION.SDK_INT>=24){
            imgUri= FileProvider.getUriForFile(SortActivity.this,"cn.njupt.iot.b19060226.book.fileprovider",imgFile);
            String adb=imgUri.toString();
            Log.d(TAG,"s输出为："+adb);
        }else {
            imgUri=Uri.fromFile(imgFile);
        }
        Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");

          /*  当向intent传入 MediaStore.EXTRA_OUTPUT参数后，表明这是一个存储动作。
            相机拍摄到的图片会直接存储到相应路径，不会缓存在内存中。*/

        //intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri);指定图片输出地址
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri);
        startActivityForResult(intent,TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bm = null;
        // 外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口
        ContentResolver resolver = getContentResolver();

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case TAKE_PHOTO:
                    Log.e(TAG, "onActivityResult: imgUri:TAKE_PHOTO:" + imgUri.toString());
//                /*  其实可以理解为系统帮你预设好了的标识符，
//                有
//                RESULT_OK
//                RESULT_CANCELED
//                RESULT_FIRST_USER
//                在调用系统activity时返回时RESULT_CANCELED如字面意思代表取消，RESULT_OK代表成功。*/
//                /* 意思是当Activity的启动模式是singleTask时,这个Activity不会运行在该task任务栈中.
//                并且会马上收到一个cancel result的信号.这就是原因了.
//                比如Activity A 使用startActivityForResult()跳转到Activity B中,
//                同时A的启动模式是SingleTask, 这时一调用startActivityForResult()去跳转B,
//                A中的onActivityResult()方法会马上收到一个RESULT_CANCEL(值为0)的resultCode.这样RESULT_OK是无法被响应的.*/
//                    try {
//                        //将拍摄的照片显示出来
//                    /*   BitmapFactory.decodeByteArray(byte[] data, int offset, int length)
//                         从指定字节数组的offset位置开始，将长度为length的字节数据解析成Bitmap对象
//                         BitmapFactory.decodeFile(String path)
//                    　 　该方法将指定路径的图片转成Bitmap，
//                         BitmapFactory.decodeFile(String path, Options options)
//                    　　 该方法使用options的变量信息，将指定路径的图片转成Bitmap
//                        decodeResource()
//                        可以将/res/drawable/内预先存入的图片转换成Bitmap对象
//                        decodeStream()
//                        方法可以将InputStream对象转换成Bitmap对象。*/
//
//                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imgUri));
////                        picture.setImageBitmap(bitmap);
//
//
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
                    cropPhoto(imgUri, true);
                    break;

                // 裁剪后设置图片
                case REQUEST_CROP:
                    try{
                        Log.d(TAG,"onActivityResult:REQUEST_CROP:mCutUri:"+mCutUri);
                        String path = mCutUri.getPath();
                        Log.d(TAG, "onActivityResult:REQUEST_CROP:path:"+path);

                        AlertDialog.Builder builder = new AlertDialog.Builder(SortActivity.this);
                        builder.setMessage("是否图片去除手写？");
                        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //确定删除
                                try {
                                    clearHandwriting(mCutUri);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //取消
                                myDialog.insertImg(path);
                            }
                        });
                        builder.show();
//
//                        switch (type) {
//                            //题目去手写
//                            case 1:
//                                clearHandwriting(mCutUri);
//                                break;
//                            case 2:
//                                myDialog.insertImg(path);
//                                break;
//                            default:
//                                break;
//                        }

//                        myDialog.insertImg(path);
//                        clearHandwriting(mCutUri);
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(SortActivity.this,"图片插入失败",Toast.LENGTH_SHORT).show();
                    }
                    break;

                case OPEN_GALLERY:
//                Log.d(TAG,"输出为："+requestCode);
//
//                if (resultCode == Activity.RESULT_OK){
//                    //判断系统版本，4.4以上系统用这个方法处理图片
//                    if (Build.VERSION.SDK_INT>=19){
//                        handleImageOnKiKat(data);
//                    }else {
//                        handleImageBeforeKiKat(data);
//                    }
//                }
//                break;
                    Log.e(TAG, "onActivityResult: OPEN_GALLERY:" + data.getData().toString());
                    cropPhoto(data.getData(), false);
                    break;
                default:
                    break;
            }
        }
    }

//    private void insertImg(String path){
//        Log.e(TAG, "insertImg:" + path);
//        String tagPath = "<img src=\""+path+"\"/>";//为图片路径加上<img>标签
//        Bitmap bitmap = BitmapFactory.decodeFile(path);
//        if(bitmap != null){
//            SpannableString ss = getBitmapMime(path, tagPath);
//            insertPhotoToEditText(ss);
//            etQuestion.append("\n");
//            Log.e(TAG, etQuestion.getText().toString());
//
//        }else{
//            //Log.d("YYPT_Insert", "tagPath: "+tagPath);
//            Toast.makeText(SortActivity.this,"插入失败，无读写存储权限，请到权限中心开启",Toast.LENGTH_LONG).show();
//        }
//    }
//    //将图片插入到EditText中
//    private void insertPhotoToEditText(SpannableString ss){
//        Editable et;
//        int start;
//        switch (type) {
//            case 1:
//                et = etQuestion.getText();
//                start = etQuestion.getSelectionStart();
//                et.insert(start,ss);
//                etQuestion.setText(et);
//                etQuestion.setSelection(start+ss.length());
//                etQuestion.setFocusableInTouchMode(true);
//                etQuestion.setFocusable(true);
//                break;
//            case 2:
//                et = etAnswer.getText();
//                start = etAnswer.getSelectionStart();
//                et.insert(start,ss);
//                etAnswer.setText(et);
//                etAnswer.setSelection(start+ss.length());
//                etAnswer.setFocusableInTouchMode(true);
//                etAnswer.setFocusable(true);
//                break;
//        }
//    }
//
//    private SpannableString getBitmapMime(String path,String tagPath) {
//        SpannableString ss = new SpannableString(tagPath);//这里使用加了<img>标签的图片路径
////        Log.e("AddActivity","ss1:"+ss.toString());
//
//        int width = ScreenUtils.getScreenWidth(SortActivity.this);
//        int height = ScreenUtils.getScreenHeight(SortActivity.this);
//
////        Log.d("YYPT_IMG_SCREEN", "高度:"+height+",宽度:"+width);
//
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
////        Log.d("YYPT_IMG_IMG", "高度:"+bitmap.getHeight()+",宽度:"+bitmap.getWidth());
//        bitmap = ImageUtils.zoomImage(bitmap,(width-32)*0.9,bitmap.getHeight()/(bitmap.getWidth()/((width-32)*0.9)));
//
//        /*
//        //高:754，宽1008
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 4;
//        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
//        */
//        Log.d("YYPT_IMG_COMPRESS", "高度："+bitmap.getHeight()+",宽度:"+bitmap.getWidth());
//
//        ImageSpan imageSpan = new ImageSpan(this, bitmap);
//        ss.setSpan(imageSpan, 0, tagPath.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        Log.e(TAG,"ss2:"+ss.toString());
//        return ss;
//    }

    private void clearHandwriting(Uri mCutUri) {
        //Body of your click handler
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                //code to do the HTTP request
                Log.d(TAG, "run: ");
                getClear();
            }
        });
        thread.start();
    }

    public void getClear() {
        Log.d(TAG, "getClear: ");
        BufferedReader in = null;
        DataOutputStream out = null;
        String path="",result="";
        try {
            path = mCutUri.getPath();
            Log.d(TAG, "getClear: mCutUri的path:"+path);
            byte[] imgData = ImageUtils.readfile(path); // image
            Log.d(TAG, "getClear: byte[] imgData:"+imgData);
            URL realUrl = new URL(ResourceDao.TextInUrl);
            HttpURLConnection conn = (HttpURLConnection)realUrl.openConnection();
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("x-ti-app-id", ResourceDao.appId);
            conn.setRequestProperty("x-ti-secret-code", ResourceDao.secretCode);
            conn.setConnectTimeout(10000);//超时
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST"); // 设置请求方式
            out = new DataOutputStream(conn.getOutputStream());
            out.write(imgData);
            out.flush();
            out.close();
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        Log.d(TAG, "getClear: result:"+result);
        try {
            JSONObject resultJSON = new JSONObject(result);
            Log.d(TAG, "getClear: resultJSON:"+resultJSON);
            JSONObject dataJSON = resultJSON.getJSONObject("result");
//            Log.d(TAG, "getClear: dataJSON:"+dataJSON);
//            JSONObject resultJSON = new JSONObject(result);
//            JSONObject dataJSON = resultJSON.getJSONObject("data");
//            forecastJSONArray= dataJSON.getJSONArray("forecast");
            imageBase64 = dataJSON.optString("image");
            Log.d(TAG, "getClear: imageBase64"+imageBase64);
        }catch(JSONException e){
            e.printStackTrace();
        }
        Message msg=handler.obtainMessage();
        handler.sendMessage(msg);
    }

    // 图片裁剪
    private void cropPhoto(Uri uri, boolean fromCapture) {
        Intent intent = new Intent("com.android.camera.action.CROP"); //打开系统自带的裁剪图片的intent
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");

        // 注意一定要添加该项权限，否则会提示无法裁剪
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        intent.putExtra("scale", true);

        // 设置裁剪区域的宽高比例
//        if(android.os.Build.MODEL.contains("HUAWEI"))
//        {//华为特殊处理 不然会显示圆
//            intent.putExtra("aspectX", 9998);
//            intent.putExtra("aspectY", 9999);
//        } else {
//            intent.putExtra("aspectX", 1);
//            intent.putExtra("aspectY", 1);
//        }
//        intent.putExtra("aspectX", 9998);
//        intent.putExtra("aspectY", 9999);


//        // 设置裁剪区域的宽度和高度
//        intent.putExtra("outputX", 200);
//        intent.putExtra("outputY", 200);

        // 取消人脸识别
        intent.putExtra("noFaceDetection", true);
        // 图片输出格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        // 若为false则表示不返回数据
        intent.putExtra("return-data", false);

        // 指定裁剪完成以后的图片所保存的位置,pic info显示有延时
        if (fromCapture) {
            // 如果是使用拍照，那么原先的uri和最终目标的uri一致,注意这里的uri必须是Uri.fromFile生成的
            mCutUri = Uri.fromFile(imgFile);
        } else { // 从相册中选择，那么裁剪的图片保存在take_photo中
            String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
            String fileName = "photo_" + time;
            File mCutFile = new File(Environment.getExternalStorageDirectory() + "/take_photo/", fileName + ".jpeg");
            if (!mCutFile.getParentFile().exists()) {
                mCutFile.getParentFile().mkdirs();
            }
            mCutUri = Uri.fromFile(mCutFile);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCutUri);
        Toast.makeText(this, "剪裁图片", Toast.LENGTH_SHORT).show();
        // 以广播方式刷新系统相册，以便能够在相册中找到刚刚所拍摄和裁剪的照片
        Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentBc.setData(uri);
        this.sendBroadcast(intentBc);

        startActivityForResult(intent, REQUEST_CROP); //设置裁剪参数显示图片至ImageView
    }

    public void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查是否有存储和拍照权限
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            ) {
                hasPermission = true;
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasPermission = true;
            } else {
                Toast.makeText(this, "权限授予失败！", Toast.LENGTH_SHORT).show();
                hasPermission = false;
            }
        }
    }

//    /**
//     * 自定义license的文件路径和文件名称，以license文件方式初始化
//     */
//    private void initAccessTokenLicenseFile() {
//        OCR.getInstance(getApplicationContext()).initAccessToken(new OnResultListener<AccessToken>() {
//            @Override
//            public void onResult(AccessToken accessToken) {
//                String token = accessToken.getAccessToken();
//                Log.d(TAG,token);
//                hasGotToken = true;
//            }
//
//            @Override
//            public void onError(OCRError error) {
//                error.printStackTrace();
////                alertText("自定义文件路径licence方式获取token失败", error.getMessage());
//            }
//        }, "aip.license", getApplicationContext());
//    }

}