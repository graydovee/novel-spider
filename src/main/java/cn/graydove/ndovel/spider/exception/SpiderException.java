package cn.graydove.ndovel.spider.exception;

/**
 * @author graydove
 */
public class SpiderException extends Exception {
    public SpiderException() {
    }

    public SpiderException(String message) {
        super(message);
    }

    public SpiderException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpiderException(Throwable cause) {
        super(cause);
    }

    public SpiderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
