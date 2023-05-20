package cn.njupt.iot.b19060226.book;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.BaseViewHolder> {

    private SortActivity context;
    public List<TestData> dataList;
    private RoomDB database;
    boolean hasPermission = false;
    private int type = 1;
    private static final int TAKE_PHOTO = 0;  //拍照
    private static final int REQUEST_CROP = 1; //裁剪
    private static final int OPEN_GALLERY = 2; //相册
    private static final int REQUEST_PERMISSION = 100;
    private static final String TAG = "TestAdapter";
    //String regex = "<img src=\\".*?\\"\\/>";
    Pattern p = Pattern.compile("<img src=\".*?\"/>");

    String base64;
    String orcText;

    //Add your own Registered API key
    String mAPiKey = "K81936755388957";
    boolean isOverlayRequired = true;
    String mLanguage = "chs";
    OCRAsyncTask.OCRCallback mIOCRCallBack;

    public TestAdapter(SortActivity context, List<TestData> dataList) {
        this.context = context;
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TestAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        BaseViewHolder viewHolder;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_main, parent, false);
        viewHolder = new BaseViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TestAdapter.BaseViewHolder holder, int position) {
        BaseViewHolder baseViewHolder = holder;
        TestData data = dataList.get(position);
        database = RoomDB.getInstance(context);
        baseViewHolder.tvTime.setText(data.getTime());
//        baseViewHolder.tvContent.setText(data.getQuestion());
        baseViewHolder.tvContent.setText(initContent(data.getQuestion()));
        baseViewHolder.tvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WindowManager wm = context.getWindowManager();
                int width = wm.getDefaultDisplay().getWidth();
                int height = wm.getDefaultDisplay().getHeight();

                TestData d = dataList.get(position);
                context.myDialog = new MyDialog(context, d.getUser_id(),d);
                //Show dialog
                context.myDialog.show();

                //Set layout
                context.myDialog.getWindow().setLayout(width,height);

                context.myDialog.setGalleryOnClickListener(new MyDialog.onGalleryOnClickListener() {
                    @Override
                    public void onGalleryClick() {
                        context.checkPermissions();
                        if (context.hasPermission) {
                            context.openGallery();
                        }
                    }
                });
                context.myDialog.setCameraOnClickListener(new MyDialog.onCameraOnClickListener() {
                    @Override
                    public void onCameraClick() {
                        context.checkPermissions();
                        if (context.hasPermission) {
                            context.takePhoto();
                        }
                    }
                });
                context.myDialog.setTypeInterface(new MyDialog.typeInterface() {
                    @Override
                    public void getType(int myType) {
                        context.type = myType;
                    }
                });

            }
        });

        baseViewHolder.tvContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("是否删除该条题？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //确定删除
                        try {
                            database.testDao().delete(dataList.get(position));
                            dataList = database.testDao().getAl(context.userId);
                            notifyDataSetChanged();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //取消
                    }
                });
                builder.show();
                return true;
            }
        });

        baseViewHolder.tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orcText="";
                String input = data.getQuestion();
                Matcher m = p.matcher(input);
                //List<String> result = new ArrayList<String>();
                boolean hasPic = false;

                SpannableString spannable = new SpannableString(input);
                while(m.find()){
                    hasPic = true;
                    //Log.d("YYPT_RGX", m.group());
                    //这里s保存的是整个式子，即<img src="xxx"/>，start和end保存的是下标
                    String s = m.group();

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

                        base64 = ImageUtils.bitmapToBase64(bitmap);

                        new OCRAsyncTask(context, mAPiKey, isOverlayRequired, mLanguage)
                                .setCallback(new OCRAsyncTask.OCRCallback() {
                                    @Override
                                    public void onOCRCallbackResults(JSONObject response) {
                                        try {
                                            orcText += handleJSONResponse(response);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).execute(base64);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if (!hasPic) {
                    orcText = input;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("查询该题？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //确定
                        try {
                            Log.d(TAG, "查询同类题：onClick: "+orcText);
                            data.setOrcQuestion(orcText);
                            database.testDao().update(data);

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //取消
                    }
                });
                builder.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class BaseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime,tvSearch, tvContent;

        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvSearch = itemView.findViewById(R.id.tv_search);
        }
    }

    private SpannableString initContent(String input){
//        //String regex = "<img src=\\".*?\\"\\/>";
//        Pattern p = Pattern.compile("\\<img src=\".*?\"\\/>");
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
        return spannable;
    }

    private String handleJSONResponse(JSONObject response) throws JSONException {
        String text="";
        if (response.has("ParsedText")) {
            JSONArray overlay = response.getJSONObject("TextOverlay")
                    .getJSONArray("Lines");
            Log.d(TAG, "TextOverlay returned " + overlay.length() + " Lines");
            for (int i = 0; i < overlay.length(); i++) {
                JSONArray words = overlay.getJSONObject(i).getJSONArray("Words");
                Log.d(TAG, "Line " + i + " returned " + words.length() + " Words");
                for (int x = 0; x < words.length(); x++) {
                    Log.d(TAG, "Line " + i + ", Word " + x + ": " +
                            words.getJSONObject(x).getString("WordText"));
                }
            }
            text = response.getString("ParsedText");

            Log.d(TAG, text);
//            mTxtResult.setText(text);
        } else if (response.has("ErrorMessage")) {
            String error = response.getString("ErrorMessage");
            Log.d(TAG, "ErrorMessage"+error);
            text = error;
        }
        return text;
    }

//    private void checkPermissions() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            // 检查是否有存储和拍照权限
//            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//                    && context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
//            ) {
//                hasPermission = true;
//            } else {
//                context.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_PERMISSION);
//            }
//        }
//    }
//    private void openGallery() {
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
//        context.startActivityForResult(intent, OPEN_GALLERY);
//    }
//    private void takePhoto(){
//        // 要保存的文件名
//        String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
//        String fileName = "photo_" + time;
//        // 创建一个文件夹
//        String path = Environment.getExternalStorageDirectory() + "/take_photo";
//        File file = new File(path);
//        if (!file.exists()) {
//            file.mkdirs();
//        }
//        // 要保存的图片文件
//        imgFile = new File(file, fileName + ".jpeg");
//        // 将file转换成uri
////        // 注意7.0及以上与之前获取的uri不一样了，返回的是provider路径
////        imgUri = getUriForFile(this, imgFile);
//
////        //创建file对象储存拍摄到的照片,将图片命名为output_image.jpg，将他存储在sd卡的关联目录下，调用getExternalCacheDir()
////        //方法可以获得这个目录
////        File outputImg=new File(getExternalCacheDir(),"output_image.jpg");
////        try {
////            if (outputImg.exists()){
////                outputImg.delete();
////            }
////            outputImg.createNewFile();
////        }catch (IOException e){
////            e.printStackTrace();
////        }
//
//        //判断系统版本，低于7.0会将file对象转换为uir对象否则调用getUriForFile将file对象转化为一个封装过的uri对象
//        //因为7.0开始直接使用本地真实路径会被认为是不安全的会抛出FileUriExposedException异常，FileProvider是一个
//        //内容提供器会将封装的uri提供给外部
//        if (Build.VERSION.SDK_INT>=24){
//            imgUri= FileProvider.getUriForFile(context,"cn.njupt.iot.b19060226.book.fileprovider",imgFile);
//            String adb=imgUri.toString();
////            Log.d(TAG,"s输出为："+adb);
//        }else {
//            imgUri= Uri.fromFile(imgFile);
//        }
//        Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
//
//          /*  当向intent传入 MediaStore.EXTRA_OUTPUT参数后，表明这是一个存储动作。
//            相机拍摄到的图片会直接存储到相应路径，不会缓存在内存中。*/
//
//        //intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri);指定图片输出地址
//        intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri);
//        context.startActivityForResult(intent,TAKE_PHOTO);
//    }
}
