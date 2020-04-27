package sunhongbin.service;

/**
 * created by SunHongbin on 2020/4/27
 */
public interface SmartRobotService {

    /**
     * listening in message
     */
    void listeningInMsg();

    /**
     * send message
     */
    void sendMsgToWeChatFriend();

    /**
     * weChat message notification
     */
    void weChatMsgNotify();
}
