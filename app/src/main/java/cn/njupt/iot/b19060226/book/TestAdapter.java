package cn.njupt.iot.b19060226.book;

import android.content.DialogInterface;
import android.content.Intent;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
    String mAPiKey = "K80943135688957";
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
                context.myDialog = new MyDialog(context, d.getUser_id(), d);
                //Show dialog
                context.myDialog.show();

                //Set layout
                context.myDialog.getWindow().setLayout(width, height);

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
                        } catch (Exception e) {
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
                orcText = "";
                String input = data.getQuestion();
                Matcher m = p.matcher(input);
                //List<String> result = new ArrayList<String>();
                boolean hasPic = false;

                SpannableString spannable = new SpannableString(input);
                while (m.find()) {
                    hasPic = true;

                    String s = m.group();
                    String path = s.replaceAll("<img src=\"|\"/>", "").trim();
                    int width = ScreenUtils.getScreenWidth(context);
                    int height = ScreenUtils.getScreenHeight(context);
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                        bitmap = ImageUtils.zoomImage(bitmap, (width - 32) * 0.9, bitmap.getHeight() / (bitmap.getWidth() / ((width - 32) * 0.9)));
                        base64 = ImageUtils.bitmapToBase64(bitmap);
                        OCRAsyncTask task = new OCRAsyncTask(context, mAPiKey, isOverlayRequired, mLanguage)
                                .setCallback(new OCRAsyncTask.OCRCallback() {
                                    @Override
                                    public void onOCRCallbackResults(JSONObject response) {
                                        try {
                                            orcText += handleJSONResponse(response);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        task.execute(base64);
                        // 等待 AsyncTask 执行完毕
                        task.get();
                    } catch (Exception e) {
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
                            Log.d(TAG, "查询同类题：onClick: " + orcText);
                            data.setOrcQuestion(orcText);
                            database.testDao().update(data);
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    //code to do the HTTP request
                                    Log.d(TAG, "run: ");
                                    String url = getSearch(orcText.trim());
                                    Intent intent = new Intent(context,SearchActivity.class);
                                    intent.putExtra("url",url);
                                    intent.putExtra("ocr",orcText);
                                    context.startActivity(intent);
                                }
                            });
                            thread.start();

                        } catch (Exception e) {
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
        TextView tvTime, tvSearch, tvContent;

        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvSearch = itemView.findViewById(R.id.tv_search);
        }
    }

    private SpannableString initContent(String input) {
//        //String regex = "<img src=\\".*?\\"\\/>";
//        Pattern p = Pattern.compile("\\<img src=\".*?\"\\/>");
        Matcher m = p.matcher(input);
        //List<String> result = new ArrayList<String>();


        SpannableString spannable = new SpannableString(input);
        while (m.find()) {
            //Log.d("YYPT_RGX", m.group());
            //这里s保存的是整个式子，即<img src="xxx"/>，start和end保存的是下标
            String s = m.group();
            int start = m.start();
            int end = m.end();
            //path是去掉<img src=""/>的中间的图片路径
            String path = s.replaceAll("<img src=\"|\"/>", "").trim();
            //Log.d("YYPT_AFTER", path);

            //利用spannableString和ImageSpan来替换掉这些图片
            int width = ScreenUtils.getScreenWidth(context);
            int height = ScreenUtils.getScreenHeight(context);

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                bitmap = ImageUtils.zoomImage(bitmap, (width - 32) * 0.9, bitmap.getHeight() / (bitmap.getWidth() / ((width - 32) * 0.9)));
                ImageSpan imageSpan = new ImageSpan(context, bitmap);
                spannable.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return spannable;
    }

    private String handleJSONResponse(JSONObject response) throws JSONException {
        String text = "";
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
            Log.d(TAG, "ErrorMessage" + error);
            text = error;
        }
        return text;
    }

    public String getSearch(String ocrText) {
        String url = "https://www.asklib.com";

        String result = ocrText.replaceAll("[\t\n\r]","");
        result = result.replaceAll("\\[","");
        result = result.replaceAll("\\]","");
        result = result.replaceAll(" ","");

        //定义网页地址和xpath地址
        if (result.length()>20) {
            result = result.substring(0,20);
        }























        String webUrl = "https://www.asklib.com/s/" + result;
        Log.d(TAG, "getSearch: webUrl:"+webUrl);
//        String xpathAddress = "/html/body/div[5]/div[2]/div[1]/div[1]/div[2]/div/a";
        String cssQuery = "div:nth-child(5) > div:nth-child(2) > div:nth-child(1) > div:nth-child(1) > div:nth-child(2) > div > a";

        try {
            Document doc = Jsoup.connect(webUrl).get();
            Element link = doc.select(cssQuery).first();
            Log.d(TAG, "getSearch: href:" + link.attr("href"));
            if (link != null) {
                System.out.println(link.attr("href"));
            } else {
                System.out.println("Link not found");
            }

            url = "https://www.asklib.com" + link.attr("href");


        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getSearch: url" + url);
        return url;
//        Document doc = null;
//        try {
//            //获取网页内容
//            doc = Jsoup.connect(webUrl).get();
//
//            //根据xpath地址获取链接
//            Elements links = doc.select(xpathAddress);
//
//            //输出链接
//            Log.d(TAG, "getSearch: href" + links.attr("href"));
//            System.out.println(links.attr("href"));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


//    public void getSearch(String ocrText) {
//
//        // 定义发送请求的地址
//        String url = "https://www.asklib.com/s/" + ocrText;
//
//        // 建立连接，并获得数据
//        HttpURLConnection conn = null;
//        try {
//            URL obj = new URL(url);
//            conn = (HttpURLConnection) obj.openConnection();
//            conn.setRequestMethod("GET");
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            String inputLine;
//            StringBuilder response = new StringBuilder();
//
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            in.close();
//
//            // 将爬取得到的页面信息存储到变量
//            String pageContent = response.toString();
//            Log.e(TAG, "getSearch: pageContent"+pageContent);
//
//            // 做其他的相关处理或操作
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (conn != null) {
//                conn.disconnect();
//            }
//        }

}
