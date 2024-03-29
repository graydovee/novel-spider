package cn.graydove.ndovel.spider.core.engine;

import cn.graydove.ndovel.spider.model.SearchResult;
import cn.graydove.ndovel.spider.model.TextLink;
import cn.hutool.core.net.url.UrlBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author graydove
 */
public abstract class AbstractBaiduSearchSpiderEngine implements SeleniumSpiderEngine {

    private final static String KEYWORD_SUFFIX = " 笔趣阁";

    @Override
    public SearchResult search(String keyword) {
        return getSearchResult(new BaiduSearchBuilder().keyWord(keyword).build());
    }

    @Override
    public SearchResult getSearchResult(String url) {
        WebDriver webDriver = getWebDriver();
        webDriver.get(url);
        SearchResult searchResult = new SearchResult();
        List<TextLink> result = webDriver.findElements(By.tagName("h3")).stream()
                .map(webElement -> {
                    TextLink textLink = new TextLink();
                    textLink.setName(webElement.getText());
                    String href = Optional.ofNullable(webElement.findElement(By.tagName("a")))
                            .map(element -> element.getAttribute("href"))
                            .orElse(null);
                    textLink.setUrl(href);
                    return textLink;
                }).collect(Collectors.toList());
        searchResult.setResult(result);

        String nextPage = Optional.ofNullable(
                webDriver.findElements(By.id("page")))
                .flatMap(webElements -> webElements.stream()
                       .map(webElement -> webElement.findElement(By.className("n"))
                               .getAttribute("href"))
                       .findAny()
                ).orElse("");
        searchResult.setNextPageUrl(nextPage);
        return searchResult;
    }

    private static class BaiduSearchBuilder {

        private UrlBuilder urlBuilder = UrlBuilder.of("https://www.baidu.com/s", StandardCharsets.UTF_8);

        BaiduSearchBuilder keyWord(String k) {
            urlBuilder.addQuery("wd", k + KEYWORD_SUFFIX);
            return this;
        }

        String build() {
            return urlBuilder.build();
        }
    }
}
