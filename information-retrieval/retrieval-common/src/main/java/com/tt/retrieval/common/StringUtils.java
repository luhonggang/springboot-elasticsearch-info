package com.tt.retrieval.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tt.retrieval.common.vo.TimeLineBasicInfoVo;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LuHongGang
 * @version 1.0
 */
public class StringUtils {

    /**
     * 生成UUID
     * @return 替换字符后的UUID
     */
    public static String getUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
     * 替换html 等标签
     * @param html 原始的html
     * @return     String
     */
    public static String replaceHtml(String html){
        //如果有双引号将其先转成单引号
        String htmlStr = html.replaceAll("\"", "'");
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
        String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式

        Pattern p_script = Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); // 过滤script标签

        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); // 过滤style标签

        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); // 过滤html标签
        return htmlStr;
    }


    public static void main(String[] args) {
        System.out.println("产生UUID值 ： UUID=" +StringUtils.getUUID());

        System.out.println(" 格式化 ： " + String.format("%s:%s",Thread.currentThread().getId(),"create"));
    }
}
