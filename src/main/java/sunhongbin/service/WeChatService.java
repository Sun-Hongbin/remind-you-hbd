package sunhongbin.service;

import sunhongbin.enums.MemberTypeEnum;
import sunhongbin.exception.WeChatException;

/**
 * created by SunHongbin on 2020/4/27
 */
public interface WeChatService {

    /**
     * 1、get UUID
     */
    String getUUID() throws WeChatException;

    /**
     * 2、show QR code
     */
    void showQRCode(String uuid) throws WeChatException;

    /**
     * 3、Polling for scan results
     * check whether scan the QR code and press login button by polling
     */
    void pollForScanRes(String uuid);

    /**
     * 4、initialize weChat
     */
    void initializeweChat();

    /**
     * 5、WeChat Msg status notify
     */
    void wxStatusNotify() throws WeChatException;

    /**
     * 5、load contact person
     */
    void loadContactPerson() throws WeChatException;

    /**
     * listening in message
     */
    void listeningInMsg();

    /**
     * send message
     */
    void sendMsgToWeChatFriend(String msg, MemberTypeEnum toWho);


}
