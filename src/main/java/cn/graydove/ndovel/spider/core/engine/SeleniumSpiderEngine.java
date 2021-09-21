package cn.graydove.ndovel.spider.core.engine;

import org.openqa.selenium.WebDriver;

/**
 * @author graydove
 */
public interface SeleniumSpiderEngine extends SpiderEngine {

    WebDriver getWebDriver();

}
