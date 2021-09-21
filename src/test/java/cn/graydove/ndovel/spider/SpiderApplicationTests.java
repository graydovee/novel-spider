package cn.graydove.ndovel.spider;

import cn.graydove.ndovel.spider.core.EngineManager;
import cn.graydove.ndovel.spider.core.engine.NovelSpiderEngine;
import cn.graydove.ndovel.spider.core.engine.SpiderEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpiderApplicationTests {

    @Autowired
    private EngineManager engineManager;

//    @Test
    void contextLoads() {
        try (SpiderEngine spiderEngine = engineManager.newNovelSpiderEngine()) {
            spiderEngine.search("斗破苍穹");
        }
    }

}
