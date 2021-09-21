package cn.graydove.ndovel.spider.controller;

import cn.graydove.ndovel.spider.core.EngineManager;
import cn.graydove.ndovel.spider.core.engine.NovelSpiderEngine;
import cn.graydove.ndovel.spider.core.engine.SpiderEngine;
import cn.graydove.ndovel.spider.exception.SpiderException;
import cn.graydove.ndovel.spider.model.NovelChapter;
import cn.graydove.ndovel.spider.model.NovelIndex;
import cn.graydove.ndovel.spider.model.Response;
import cn.graydove.ndovel.spider.model.SearchResult;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author graydove
 */
@Slf4j
@RestController
@AllArgsConstructor
public class SpiderController {

    private EngineManager engineManager;

    @GetMapping("search")
    public Response<SearchResult> searchNovel(String name, String url) {
        SpiderEngine spiderEngine = engineManager.getEngine();
        try {
            return Response.ok(StrUtil.isBlank(url) ? spiderEngine.search(name) : spiderEngine.getSearchResult(url));
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }

    @GetMapping("index")
    public Response<NovelIndex> novelIndex(String url) {
        SpiderEngine spiderEngine = engineManager.getEngine();
        try {
            return Response.ok(spiderEngine.getNovelIndex(url));
        } catch (SpiderException e) {
            return Response.error(e.getMessage());
        }
    }

    @GetMapping("chapter")
    public Response<NovelChapter> novelChapter(String url) {
        SpiderEngine spiderEngine = engineManager.getEngine();
        try {
            return Response.ok(spiderEngine.getNovelChapter(url));
        } catch (SpiderException e) {
            return Response.error(e.getMessage());
        }
    }
}
