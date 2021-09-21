package cn.graydove.ndovel.spider.config;

import cn.graydove.ndovel.spider.core.EngineManager;
import cn.graydove.ndovel.spider.core.WebDriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import java.util.Map;

/**
 * @author graydove
 */
@Slf4j
@Configuration
public class SpiderConfiguration {

    @Value("${spider.driver.chrome}")
    private String chromeDriverPath;

    @Value("${spider.driver.gecko}")
    private String geckoDriverPath;

    @Value("${spider.engine}")
    private String engine;

    private EngineManager engineManager;

    @Autowired
    public void setEngineManager(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    @Bean
    public WebDriverFactory webDriverFactory() {
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        System.setProperty("webdriver.gecko.driver", geckoDriverPath);
        return new WebDriverFactory(engine);
    }

    @EventListener
    public void shutdown(ContextClosedEvent closedEvent) {
        log.info("destroy driver..");
        if (closedEvent != null && engineManager != null) {
            engineManager.destroy();
        }
    }
}
