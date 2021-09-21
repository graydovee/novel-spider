package cn.graydove.ndovel.spider.runner;

import cn.graydove.ndovel.spider.core.EngineManager;
import cn.graydove.ndovel.spider.core.EngineStrategy;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author graydove
 */
@Component
public class SpiderRunner implements ApplicationRunner {

    private EngineManager engineManager;

    @Value("${spider.engine.strategy}")
    private EngineStrategy engineStrategy;

    public SpiderRunner(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (engineStrategy != null) {
            engineManager.setEngineStrategy(engineStrategy);
        }
    }
}
