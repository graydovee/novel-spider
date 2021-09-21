package cn.graydove.ndovel.spider.core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/**
 * @author graydove
 */
public class WebDriverFactory {

    private final static Map<String, Supplier<WebDriver>> ENGINE_SUPPLIER_MAP;

    static {
        Map<String, Supplier<WebDriver>> map = new HashMap<>();
        map.put("chrome", () -> {
            ChromeOptions chromeOptions = new ChromeOptions();
            if(!System.getProperty("os.name").toLowerCase().contains("win")) {
                chromeOptions.addArguments("--headless");
            }
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("--disable-gpu");
            chromeOptions.addArguments("--disable-dev-shm-usage");
            ChromeDriver driver = new ChromeDriver(chromeOptions);
//            driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
//            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
//            driver.manage().timeouts().setScriptTimeout(5, TimeUnit.SECONDS);
            return driver;
        });
        map.put("firefox", FirefoxDriver::new);
        ENGINE_SUPPLIER_MAP = Collections.unmodifiableMap(map);
    }

    private String engineType;

    public WebDriverFactory(String engineType) {
        setEngineType(engineType);
    }

    public void setEngineType(String engineType) {
        if (!ENGINE_SUPPLIER_MAP.containsKey(engineType)) {
            throw new RuntimeException("can not find engine: " + engineType);
        }
        this.engineType = engineType;
    }

    public WebDriver newWebDriver() {
        return ENGINE_SUPPLIER_MAP.get(engineType).get();
    }

}
