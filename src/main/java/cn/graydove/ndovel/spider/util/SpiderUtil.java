package cn.graydove.ndovel.spider.util;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.URLUtil;

/**
 * @author graydove
 */
public class SpiderUtil {
    public static String formatContent(String content) {
        content = content
                .replace("　", " ")
                .replaceAll("\\t", "    ")
                .replaceAll(" {2,}", "  ")
                .replaceAll("\\r", "\n")
                .replaceAll(" +\\n", "\n")
                .replaceAll("\\n+", "\n\n");
        int start = -1;
        int end = content.length();
        for (int i = 0; i < content.length(); ++i) {
            if (content.charAt(i) == '\n') {
                start = i;
            } else {
                break;
            }
        }
        for (int i = content.length() - 1; i >= start; --i) {
            if (content.charAt(i) == '\n' || content.charAt(i)==' ') {
                end = i;
            } else {
                break;
            }
        }
        if (start < end && (start >= 0 || end < content.length())) {
            content = content.substring(start + 1, end);
        }
        return content;
    }


}
