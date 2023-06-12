package cn.njupt.iot.b19060226.book;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity:";
    String ocr,url,text="";
//    WebView webView;
    TextView tvSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        ocr = intent.getStringExtra("ocr");

        tvSearch = findViewById(R.id.tv_search);
//        webView = findViewById(R.id.webView);
//        webView.setWebViewClient(new WebViewClient());
//        webView.loadUrl(url);

        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //code to do the HTTP request
                    Log.d(TAG, "run: ");
                    init();
                }
            });
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intentBrowser = new Intent(SearchActivity.this,Browser.class);
        intentBrowser.putExtra("url",url);
        startActivity(intentBrowser);
    }

    public void init() {
//        String xpathAddress = "/html/body/div[5]/div[2]/div[1]/div[1]/div[2]/div/a";
//        String cssQuery = "div:nth-child(5) > div:nth-child(2) > div:nth-child(1) > div:nth-child(1) > div:nth-child(2) > div > a";

//        "/html/body/div[5]/div[2]/div[1]/div[1]/div[1]/h1";
        String cssQuery = "div:nth-child(5) > div:nth-child(2) > div:nth-child(1) > div:nth-child(1) > div:nth-child(1) > h1";

        try {
            Document doc = Jsoup.connect(url).get();
            System.out.println(doc);
            Element element = doc.select(cssQuery).first();
            if (element != null) {
                text = element.text();
                Log.d(TAG, "init: text:"+text);
                // 在 UI 线程中更新 TextView 内容
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String result = "相似度："+getSimilarity()+"\n";
                        result += "原题文本：\n"+ ocr + "\n";
                        result += "同类题文本：\n" + text;
                        tvSearch.setText(result);
                    }
                });
                System.out.println(text);
            } else {
                System.out.println("Element not found");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getSimilarity() {
        String s1 = ocr;

        String s2 = text;
//        String s1 = "我爱北京甜安门";
//
//        String s2 = "我喜欢吃北京烤鸭";
//

        //第一步，预处理主要是进行中文分词和去停用词，分词。

        //第二步，列出所有的词。

        //公共词 ：我爱北京甜安门喜欢吃烤鸭



        //第三步，计算词频，写出词频向量。

        //向量1：<1,1,1,1,1,1,1,0,0,0,0,0>

        //向量2：<1,0,1,1,0,0,0,1,1,1,1,1>

        // 3/7 > cos =3/根号56 > 3/8即结果在3/7和3/8之间

        CosDemo similarity = new CosDemo(s1, s2);

        System.out.println("Similarity: " + similarity.sim());
        return similarity.sim();
    }

//    static List<String> extractWords(String s1, String s2) {
//        List<String> words = new ArrayList<>();
//        for (String word : s1.split("\\s+")) {
//            if (!words.contains(word)) {
//                words.add(word);
//            }
//        }
//        for (String word : s2.split("\\s+")) {
//            if (!words.contains(word)) {
//                words.add(word);
//            }
//        }
//        return words;
//    }
//
//    static RealVector createVector(String text, List<String> words) {
//        double[] vector = new double[words.size()];
//        String[] tokens = text.split("\\s+");
//        for (String token : tokens) {
//            int index = words.indexOf(token);
//            if (index >= 0) {
//                vector[index]++;
//            }
//        }
//        return new ArrayRealVector(vector);
//    }

}
