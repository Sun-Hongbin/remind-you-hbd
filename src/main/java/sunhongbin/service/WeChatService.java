package sunhongbin.service;

/**
 * created by SunHongbin on 2020/4/27
 */
public interface WeChatService {

    /**
     * 1、get UUID
     */
    String getUUID();

    /**
     * 2、show QR code
     */
    String showQRCode(String uuid);

    /**
     * 3、showLoginState
     */
    String showLoginState();

    /**
     * 4、Login
     */
    boolean login();

    /**
     * 5、initialize weChat
     */
    void initializeweChat();

    /**
     * 6、load contact person
     */
    boolean loadContactPerson();

    /**
     * 7、Logout WeChat
     */
    void logOutWeChat();
}
