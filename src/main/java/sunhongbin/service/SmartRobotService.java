package sunhongbin.service;

/**
 * created by SunHongbin on 2020/4/27
 */
public interface SmartRobotService {

    /**
     * 图灵机器人智能回复
     *
     * @param sendMessage 发给机器人的消息
     * @return 机器人应答的消息
     */
    String aiReply(String sendMessage);

    /**
     * 定时提醒群里的小伙伴喝水
     */
    void notifyDrinkWater();
}
