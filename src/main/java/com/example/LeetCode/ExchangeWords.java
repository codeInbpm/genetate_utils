package com.example.LeetCode;

import java.util.*;

public class ExchangeWords {
    public List<String> removeAnagrams(String[] words) {
        List<String> res = new ArrayList<>();
        for(int i=words.length-1;i>0;i--){
            if(!checkAnagrams(words[i],words[i-1])){
                res.add(words[i]);
            }
        }
        res.add(words[0]);
        Collections.reverse(res);
        return res;
    }

    public boolean checkAnagrams(String a, String b){
        char[] aChar = a.toCharArray();
        char[] bChar = b.toCharArray();
        Arrays.sort(aChar);
        Arrays.sort(bChar);
        return Arrays.equals(aChar, bChar);
    }

        public static void main(String[] args) {

    }
}
