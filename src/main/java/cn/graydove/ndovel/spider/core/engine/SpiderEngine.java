package cn.graydove.ndovel.spider.core.engine;

import cn.graydove.ndovel.spider.exception.SpiderException;
import cn.graydove.ndovel.spider.model.NovelChapter;
import cn.graydove.ndovel.spider.model.NovelIndex;
import cn.graydove.ndovel.spider.model.SearchResult;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author graydove
 */
public interface SpiderEngine extends Closeable {

    /**
     * 搜索
     * @param keyword 关键词
     * @return 搜索结果
     */
    SearchResult search(String keyword);

    /**
     * 直接通过url页面获取搜索结果
     * @param url rl
     * @return 搜索结果
     */
    SearchResult getSearchResult(String url);

    /**
     * 获取小说基本信息
     * @param url 搜索到的主页Url
     * @return 小说基本信息
     * @throws SpiderException 爬虫异常
     */
    NovelIndex getNovelIndex(String url) throws SpiderException;

    /**
     * 获取章节详细内容
     * @param url 小说章节Url
     * @return 小说基本信息
     * @throws SpiderException 爬虫异常
     */
    NovelChapter getNovelChapter(String url) throws SpiderException;


    @Override
    void close();
}
