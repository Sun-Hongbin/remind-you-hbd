package sunhongbin.service;

import com.alibaba.fastjson.JSONObject;
import sunhongbin.entity.GlobalParam;
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
    void showQRCode(String uuid);

    /**
     * 3、Polling for scan results
     * check whether scan the QR code and press login button by polling
     */
    GlobalParam pollForScanRes(String uuid);

    /**
     * 4、initialize weChat
     */
    void initializeWeChat(GlobalParam globalParam);

    /**
     * 5、WeChat Msg status notify
     */
    void wxStatusNotify(GlobalParam globalParam);

    /**
     * 5、load contact person
     */
    void loadContactPerson(GlobalParam globalParam);

    /**
     * listening in message
     */
    void listeningInMsg(GlobalParam globalParam);

    /**
     * send message
     */
    void sendMsgToWeChatFriend(String msg, String toWho, GlobalParam globalParam);

}
