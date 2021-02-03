package sunhongbin.enums.error;

/**
 * created by SunHongbin on 2020/9/11
 */
public enum InitErrorEnum {

    /**
     * 加载联系人异常
     */
    LOAD_CONTACT_PERSON_EXCEPTION("加载联系人异常"),

    /**
     * [微信状态提醒] 开启失败
     */
    INIT_ERROR_STATUS_NOTIFY_FAILED("[微信状态提醒] 开启失败"),

    /**
     * 加载联系人失败
     */
    LOAD_CONTACT_PERSON_FAILED("加载联系人失败"),

    /**
     *
     */
    WX_STATUS_NOTIFY_EXCEPTION("开启微信状态通知异常"),

    /**
     * 获取最新消息异常：当同步检查接口显示有新消息时，调用该接口获取具体的新消息。此处的新消息为广义的，包括消息，修改群名，群内成员变化，加好友等。
     */
    SYNC_MSG_EXCEPTION("获取最新消息异常（当同步检查接口显示有新消息时，会获取具体的新消息。此处的新消息为广义的，包括消息，修改群名，群内成员变化，加好友等）"),

    /**
     * 发送信息异常
     */
    SND_MSG_EXCEPTION("发送信息异常"),

    /**
     * 同步消息异常
     */
    SYNC_CHK_EXCEPTION("同步消息异常")


    ;
    
    private final String desc;

    InitErrorEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
