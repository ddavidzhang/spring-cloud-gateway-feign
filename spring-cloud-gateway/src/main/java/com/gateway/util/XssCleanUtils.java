package com.gateway.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * XSS处理工具类，过滤请求中的非法参数
 */
public class XssCleanUtils {


    private static final Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);

    /**
     * 利用Jsoup处理XSS攻击
     *
     * @param rawString 原始参数
     * @return 过滤过后的参数
     */
    public static String cleanXss(String rawString) {
        return Jsoup.clean(rawString, "", Safelist.basic(), outputSettings);
    }
}
