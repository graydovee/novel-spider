package cn.graydove.ndovel.spider.core;

import cn.graydove.ndovel.spider.core.engine.CacheSpiderEngine;
import cn.graydove.ndovel.spider.core.engine.NovelSpiderEngine;
import cn.graydove.ndovel.spider.core.engine.SpiderEngine;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.lang.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author graydove
 */
@Slf4j
@Component
public class EngineManager {

    private final static String SINGLETON_KEY = "cn.graydove.ndovel.spider.core.engine.SpiderEngine";

    private Map<String, SpiderEngine> map = new ConcurrentHashMap<>();

    private ThreadLocal<String> id = ThreadLocal.withInitial(() -> UUID.randomUUID().toString(true));

    private EngineStrategy engineStrategy = EngineStrategy.NEW;

    private WebDriverFactory webDriverFactory;

    private StringRedisTemplate stringRedisTemplate;

    private ObjectMapper objectMapper;

    public EngineManager(WebDriverFactory webDriverFactory, StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.webDriverFactory = webDriverFactory;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public void setEngineStrategy(EngineStrategy engineStrategy) {
        this.engineStrategy = engineStrategy;
    }

    public SpiderEngine getEngine() {
        switch (engineStrategy) {
            case POOL:
                return map.computeIfAbsent(id.get(), (k) -> newEngine());
            case NEW:
                return newEngine();
            case SINGLETON:
                return Singleton.get(SINGLETON_KEY, this::getEngine);
            default:
        }
        throw new RuntimeException("engineStrategy is null");
    }

    public SpiderEngine newEngine() {
        return newCacheSpiderEngine(this::newNovelSpiderEngine);
    }

    public SpiderEngine newCacheSpiderEngine(Supplier<SpiderEngine> engineSupplier) {
        return new CacheSpiderEngine(engineSupplier, stringRedisTemplate, objectMapper);
    }

    public SpiderEngine newNovelSpiderEngine() {
        return new NovelSpiderEngine(webDriverFactory.newWebDriver());
    }

    public void destroy() {
        map.values().parallelStream().forEach(spiderEngine -> {
            try {
                spiderEngine.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });

        Optional.ofNullable(Singleton.get(SINGLETON_KEY, () -> null)).ifPresent(o -> {
            if (o instanceof SpiderEngine) {
                try {
                    ((SpiderEngine) o).close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }
}
