package cn.graydove.ndovel.spider.core.engine;

import cn.graydove.ndovel.spider.exception.SpiderException;
import cn.graydove.ndovel.spider.model.NovelChapter;
import cn.graydove.ndovel.spider.model.NovelIndex;
import cn.graydove.ndovel.spider.model.SearchResult;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * @author graydove
 */
@Slf4j
public class CacheSpiderEngine implements SpiderEngine{

    private static final String PREFIX = "ndovel:spider:engine:";

    private Supplier<SpiderEngine> spiderEngineSupplier;

    private volatile SpiderEngine spiderEngine;

    private StringRedisTemplate stringRedisTemplate;

    private ObjectMapper objectMapper;

    private Map<String, Lock> lockMap = new ConcurrentHashMap<>();

    public CacheSpiderEngine(Supplier<SpiderEngine> spiderEngineSupplier, StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.spiderEngineSupplier = spiderEngineSupplier;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    private SpiderEngine getSupperEngine() {
        if (null == this.spiderEngine) {
            synchronized (CacheSpiderEngine.class) {
                if (null == this.spiderEngine) {
                    this.spiderEngine = spiderEngineSupplier.get();
                }
            }
        }
        return this.spiderEngine;
    }

    @Override
    public SearchResult search(String keyword) {
        String redisKey = PREFIX + "search:" + keyword;
        return getFromCacheOrSupplier(redisKey, SearchResult.class, () -> getSupperEngine().search(keyword));
    }

    @Override
    public SearchResult getSearchResult(String url) {
        String redisKey = PREFIX + "getSearchResult:" + url;
        return getFromCacheOrSupplier(redisKey, SearchResult.class, () -> getSupperEngine().getSearchResult(url));
    }

    @Override
    public NovelIndex getNovelIndex(String url) throws SpiderException {
        String redisKey = PREFIX + "getNovelIndex:" + url;
        return getFromCacheOrSupplier(redisKey, NovelIndex.class, () -> getSupperEngine().getNovelIndex(url));
    }

    @Override
    public NovelChapter getNovelChapter(String url) throws SpiderException {
        String redisKey = PREFIX + "getNovelChapter:" + url;
        return getFromCacheOrSupplier(redisKey, NovelChapter.class, () -> getSupperEngine().getNovelChapter(url));
    }

    @Override
    public void close() {
        if (null != this.spiderEngine) {
            this.spiderEngine.close();
        }
    }

    private String writeToString(Object o) {
        if (null == o) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private <T> T readAsObject(String s, Class<T> clazz) {
        if (StrUtil.isBlank(s)) {
            return null;
        }
        try {
            return objectMapper.readValue(s, clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private <T> T getFromCacheOrSupplier(String key, Class<T> resultClass, ResultGetter<T> resultGetter) {
        T data = null;
        try {
            String s = stringRedisTemplate.opsForValue().get(key);
            data = readAsObject(s, resultClass);
        } catch (Exception e) {
            log.error("get from redis error", e);
        }
        if (null != data) {
            log.info("cache hit: " + key);
            return data;
        }
        Lock lock = lockMap.computeIfAbsent(key, (k) -> new ReentrantLock());
        lock.lock();
        try {
            try {
                String s = stringRedisTemplate.opsForValue().get(key);
                data = readAsObject(s, resultClass);
            } catch (Exception e) {
                log.error("get from redis error", e);
            }
            if (null != data) {
                log.info("cache hit on double check: " + key);
                return data;
            }
            log.info("cache miss: " + key);
            try {
                data = resultGetter.get();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            String dataStr = writeToString(data);
            if (StrUtil.isNotBlank(dataStr)) {
                log.info("update cache: " + key);
                try {
                    stringRedisTemplate.opsForValue().set(key, dataStr, 12, TimeUnit.HOURS);
                } catch (Exception e) {
                    log.error("cache save to redis error");
                }
            }
        } finally {
            lock.unlock();
            lockMap.remove(key);
        }
        return data;
    }

    @FunctionalInterface
    public static interface ResultGetter<T> {

        /**
         * 获取数据
         * @return
         * @throws Exception
         */
        T get() throws Exception;
    }
}
