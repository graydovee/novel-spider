package cn.graydove.ndovel.spider.core;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author graydove
 */
@Slf4j
public class MyWebDriver implements WebDriver {

    private WebDriver webDriver;

    public MyWebDriver(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    @Override
    public void get(String s) {
        log.info("get url: {}", s);
        webDriver.get(s);
    }

    @Override
    public String getCurrentUrl() {
        return webDriver.getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return webDriver.getTitle();
    }

    @Override
    public List<WebElement> findElements(By by) {
        try {
            return webDriver.findElements(by);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public WebElement findElement(By by) {
        try {
            return webDriver.findElement(by);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getPageSource() {
        return webDriver.getPageSource();
    }

    @Override
    public void close() {
        webDriver.close();
    }

    @Override
    public void quit() {
        webDriver.quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return webDriver.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return webDriver.getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        return webDriver.switchTo();
    }

    @Override
    public Navigation navigate() {
        return webDriver.navigate();
    }

    @Override
    public Options manage() {
        return webDriver.manage();
    }
}
