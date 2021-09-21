package cn.graydove.ndovel.spider.core;

/**
 * 引擎获取策略
 * @author graydove
 */
public enum EngineStrategy {

    /**
     * 每次新建
     * 需要手动关闭Engine！
     * 并发较低，性能较差建议使用
     */
    NEW,

    /**
     * 每个线程都分配一个
     * 不需要手动关闭！
     * 内存较大建议使用
     */
    POOL,

    /**
     * 共用一个
     * 不需要关闭
     * 需要注意并发问题
     */
    SINGLETON
}
