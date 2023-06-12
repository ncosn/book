//package cn.njupt.iot.b19060226.book;
//
//import android.content.Context;
//
//import com.baidu.ocr.sdk.OCR;
//import com.baidu.ocr.sdk.OnResultListener;
//import com.baidu.ocr.sdk.exception.OCRError;
//import com.baidu.ocr.sdk.model.GeneralBasicParams;
//import com.baidu.ocr.sdk.model.GeneralParams;
//import com.baidu.ocr.sdk.model.GeneralResult;
//import com.baidu.ocr.sdk.model.WordSimple;
//
//import java.io.File;
//
///**
// * 这个类是用于将拍摄或者图库中获得的图片进行识别，返回JSON格式的字符串。
// */
//public class RecognizeService {
//
//    public interface ServiceListener {
//        public void onResult(String result);
//    }
//
//    //高精度版
//    public static void recAccurateBasic(Context ctx, String filePath, final ServiceListener listener) {
//        GeneralParams param = new GeneralParams();
//        param.setDetectDirection(true);
//        param.setVertexesLocation(true);
//        param.setLanguageType(GeneralBasicParams.ENGLISH);
//        param.setRecognizeGranularity(GeneralParams.GRANULARITY_SMALL);
//        param.setImageFile(new File(filePath));
//
//        //这里的recognizeAccurateBasic方法为百度OCR识别的核心方法
//        OCR.getInstance(ctx).recognizeAccurateBasic(param, new OnResultListener<GeneralResult>() {
//            @Override
//            public void onResult(GeneralResult result) {
//                StringBuilder sb = new StringBuilder();
//                for (WordSimple wordSimple : result.getWordList()) {
//                    WordSimple word = wordSimple;
//                    sb.append(word.getWords());
//                    sb.append("\n");
//                }
//                listener.onResult(result.getJsonRes());
//            }
//
//            @Override
//            public void onError(OCRError error) {
//                listener.onResult(error.getMessage());
//            }
//        });
//    }
//
//
//}
