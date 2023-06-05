package cn.njupt.iot.b19060226.book;
import java.util.HashMap;

import java.util.Map;

import java.util.Set;



/**

 * 字符串相似性匹配算法

 */

public class CosDemo {
//数据结构解析:<单词,二维数组>,其中单词表示公共词，
    //  二维数组一维度表示句子一的向量,另一维度表示句子二的向量
    Map<Character, int[]> vectorMap = new HashMap<Character, int[]>();
    int[] tempArray = null;
    public CosDemo(String string1, String string2) {
        for (Character character1 : string1.toCharArray()) {
            if (vectorMap.containsKey(character1)) {
                vectorMap.get(character1)[0]++;
            } else {
                tempArray = new int[2];
                tempArray[0] = 1;
                tempArray[1] = 0;
                vectorMap.put(character1, tempArray);
            }
        }
        for (Character character2 : string2.toCharArray()) {
            if (vectorMap.containsKey(character2)) {
                vectorMap.get(character2)[1]++;
            } else {
                tempArray = new int[2];
                tempArray[0] = 0;
                tempArray[1] = 1;
                vectorMap.put(character2, tempArray);
            }
        }
        for (Map.Entry<Character, int[]> entry : vectorMap.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue()[0] +","+entry.getValue()[1]);
        }
    }
    // 求余弦相似度
    public double sim() {
        double result = 0;
        result = pointMulti(vectorMap) / sqrtMulti(vectorMap);
        return result;
    }
    private double sqrtMulti(Map<Character, int[]> paramMap) {
        double result = 0;
        result = squares(paramMap);
        result = Math.sqrt(result);
        return result;
    }
    // 求平方和
    private double squares(Map<Character, int[]> paramMap) {
        double result1 = 0;
        double result2 = 0;
        Set<Character> keySet = paramMap.keySet();
        for (Character character : keySet) {
            int temp[] = paramMap.get(character);
            result1 += (temp[0] * temp[0]);
            result2 += (temp[1] * temp[1]);
        }
        return result1 * result2;
    }
    // 点乘法
    private double pointMulti(Map<Character, int[]> paramMap) {
        double result = 0;
        Set<Character> keySet = paramMap.keySet();
        for (Character character : keySet) {
            int temp[] = paramMap.get(character);
            result += (temp[0] * temp[1]);
        }
        return result;
    }


//
//    public static void main(String[] args) {
//
//
//
//
//
//
//        String s1 = "我爱北京甜安门";
//
//        String s2 = "我喜欢吃北京烤鸭";
//
//        //第一步，预处理主要是进行中文分词和去停用词，分词。
//
//
//
//        //第二步，列出所有的词。
//
//        //公共词 ：我爱北京甜安门喜欢吃烤鸭
//
//
//        //第三步，计算词频，写出词频向量。
//
//        //向量1：<1,1,1,1,1,1,1,0,0,0,0,0>
//
//        //向量2：<1,0,1,1,0,0,0,1,1,1,1,1>
//
//        // 3/7 > cos =3/根号56 > 3/8即结果在3/7和3/8之间
//
//        CosDemo similarity = new CosDemo(s1, s2);
//
//        System.out.println(similarity.sim());
//
//    }



}
