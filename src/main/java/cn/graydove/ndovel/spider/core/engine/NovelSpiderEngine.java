package cn.graydove.ndovel.spider.core.engine;

import cn.graydove.ndovel.spider.core.MyWebDriver;
import cn.graydove.ndovel.spider.core.WebDriverFactory;
import cn.graydove.ndovel.spider.exception.SpiderException;
import cn.graydove.ndovel.spider.model.NovelChapter;
import cn.graydove.ndovel.spider.model.NovelIndex;
import cn.graydove.ndovel.spider.model.SearchResult;
import cn.graydove.ndovel.spider.model.TextLink;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.URLUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author graydove
 */
public class NovelSpiderEngine extends AbstractBaiduSearchSpiderEngine {

    private WebDriver webDriver;

    public NovelSpiderEngine(WebDriver webDriver) {
        this.webDriver = new MyWebDriver(webDriver);
    }

    @Override
    public NovelIndex getNovelIndex(String url) throws SpiderException {
        webDriver.get(url);
        NovelIndex novelIndex = new NovelIndex();
        novelIndex.setCurrentUrl(webDriver.getCurrentUrl());
        WebElement info = webDriver.findElement(By.id("info"));
        if (info == null) {
            throw new SpiderException("can not get info");
        }
        //title
        String title = Optional.ofNullable(info.findElement(By.tagName("h1")))
                .map(WebElement::getText)
                .orElseThrow(() -> new SpiderException("can not get title"));
        novelIndex.setTitle(title);

        //author
        List<WebElement> authorP = info.findElements(By.tagName("p"));
        if (CollectionUtil.isEmpty(authorP)) {
            throw new SpiderException("can not get author");
        }
        String text = authorP.get(0).getText();
        final String splitStr = "：";
        if (text.indexOf(splitStr) > 0) {
            String[] split = text.split(splitStr);
            text = split[split.length - 1];
        }
        novelIndex.setAuthor(text);

        //introduce
        List<WebElement> intro = webDriver.findElements(By.cssSelector("#intro>p"));
        if (CollectionUtil.isEmpty(intro)) {
            throw new SpiderException("can not get introduce");
        }
        String introduce = intro.get(0).getText();
        novelIndex.setIntroduce(introduce);

        //cover
        String img = Optional.ofNullable(webDriver.findElement(By.cssSelector("#fmimg>img")))
                .map(webElement -> webElement.getAttribute("src"))
                .orElseThrow(() -> new SpiderException("can not get cover"));
        novelIndex.setCover(URLUtil.completeUrl(novelIndex.getCurrentUrl(), img));

        //chapterList
        List<TextLink> chapterList = Optional.ofNullable(webDriver.findElement(By.id("list")))
                .map(webElement -> webElement.getAttribute("outerHTML"))
                .map(html -> {
                    Document document = Jsoup.parse(html);
                    Elements elements = document.select("#list>dl>dd>a");
                    return elements.stream().map(element -> {
                        TextLink textLink = new TextLink();
                        textLink.setName(element.text());
                        String href = element.attr("href");
                        textLink.setUrl(URLUtil.completeUrl(novelIndex.getCurrentUrl(), href));
                        return textLink;
                    }).collect(Collectors.toList());
                }).orElseThrow(() -> new SpiderException("can not get chapterList"));
        novelIndex.setChapterList(chapterList);
        return novelIndex;
    }

    @Override
    public NovelChapter getNovelChapter(String url) throws SpiderException {
        webDriver.get(url);
        NovelChapter novelChapter = new NovelChapter();
        novelChapter.setCurrentUrl(webDriver.getCurrentUrl());
        WebElement title = webDriver.findElement(By.cssSelector(".bookname>h1"));
        if (null == title) {
            throw new SpiderException("can not find title");
        }
        novelChapter.setTitle(title.getText());
        WebElement content = webDriver.findElement(By.id("content"));
        if (null == content) {
            throw new SpiderException("can not find content");
        }
        novelChapter.setContent(content.getText());
        String bottom = getHtml(webDriver.findElement(By.className("bottem1")));
        if (null == bottom) {
            throw new SpiderException("can not find bottom");
        }
        for (Element e : Jsoup.parse(bottom).select("a")) {
            if (e.text().startsWith("上一")) {
                String href = e.attr("href");
                novelChapter.setPrevUrl(URLUtil.completeUrl(novelChapter.getCurrentUrl(), href));
            } else if (e.text().startsWith("下一")) {
                String href = e.attr("href");
                novelChapter.setNextUrl(URLUtil.completeUrl(novelChapter.getCurrentUrl(), href));
            }
        }
        return novelChapter;
    }

    @Override
    public void close() {
        webDriver.quit();
    }

    private String getHtml(WebElement webElement) {
        if (null == webElement) {
            return null;
        }
        return webElement.getAttribute("outerHTML");
    }

    @Override
    public WebDriver getWebDriver() {
        return this.webDriver;
    }
}
