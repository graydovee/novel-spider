package cn.graydove.ndovel.spider.model;

import lombok.Data;

import java.util.List;

/**
 * @author graydove
 */
@Data
public class NovelIndex {

    private String title;

    private String author;

    private String introduce;

    private String cover;

    private String currentUrl;

    private List<TextLink> chapterList;
}
