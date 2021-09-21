package cn.graydove.ndovel.spider.model;

import lombok.Data;

/**
 * @author graydove
 */
@Data
public class NovelChapter {

    private String title;

    private String content;

    private String currentUrl;

    private String prevUrl;

    private String nextUrl;
}
