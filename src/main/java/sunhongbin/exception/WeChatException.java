package sunhongbin.exception;

/**
 * created by SunHongbin on 2021/1/31
 */
public class WeChatException extends RuntimeException{

    public WeChatException(String message) {
        super(message);
    }

    public WeChatException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
