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
    void showQRCode(String uuid);

    /**
     * 3、Polling for scan results
     * check whether scan the QR code and press login button by polling
     */
    String pollForScanRes(String uuid);

    /**
     * 4、initialize weChat
     */
    void initializeweChat();

    /**
     * 5、WeChat Msg status notify
     */
    void wxStatusNotify();

    /**
     * 5、load contact person
     */
    boolean loadContactPerson();

    /**
     * listening in message
     */
    void listeningInMsg();

    /**
     * send message
     */
    void sendMsgToWeChatFriend();

    /**
     * Logout WeChat
     */
    void logOutWeChat();

}
