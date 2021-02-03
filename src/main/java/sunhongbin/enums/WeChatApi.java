package sunhongbin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * created by SunHongbin on 2020/5/2
 */
@Getter
@AllArgsConstructor
public enum WeChatApi {

    /**
     * 1、获取uuid
     * 说明：用于获取显示二维码以及登录所需的uuid，标识获取二维码和扫码的为同一个用户
     * 请求方式：GET
     */
    GET_UUID("https://login.wx.qq.com/jslogin"),

    /**
     * 2、获取登陆微信二维码方式1
     * 将 String (https://login.weixin.qq.com/l/{$UUID}) 转换成二维码
     * 扫描二维码，也就是扫描 https://login.weixin.qq.com/l/{$UUID}
     * <p>
     * 自己猜测隐藏的弊端：
     * 可能这个URL的格式（login.weixin.qq.com/l/）是一直在变的
     * 优点：
     * 减少一次URL请求
     */
    GET_QR_CODE("https://login.weixin.qq.com/qrcode"),

    /**
     * 获取登陆微信二维码方式2
     * 先发送请求：https://login.weixin.qq.com/qrcode/{$UUID}
     * 微信返回我们 URL（InputStream），内容为 https://login.weixin.qq.com/l/{$UUID}
     * 将微信返回给我们的 InputStream 转换为 File
     * 将 File 中的内容解析成 String ： https://login.weixin.qq.com/l/{$UUID}
     * 将 String 转换成二维码
     * 扫描二维码，也就是扫描 https://login.weixin.qq.com/l/{$UUID}
     */
    GET_QR_CODE_WAY2("https://login.weixin.qq.com/l/"),

    /**
     * 尝试登录。若此时用户手机已完成扫码并点击登录，则返回一个真正用于登录的url地址。
     * 否则接口大概10s后返回未扫码或未登录的状态码
     */
    IS_SCAN_QR_CODE("https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login"),

    /**
     * 3、微信初始化
     * 说明：初始化微信首页栏的联系人、公众号等（不是通讯录里的联系人），
     * 初始化登录者自己的信息（包括昵称等），初始化同步消息所用的SycnKey
     * 请求方式：POST
     */
    WEB_WX_INIT("https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxinit"),

    /**
     * 4、微信消息状态提醒
     */
    WX_STATUS_NOTIFY("https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxstatusnotify"),

    /**
     * 5、获取联系人列表
     * 说明：获取手机通讯录中的所有联系人（包括人、群、公众号等）
     * 请求方式：POST
     */
    GET_CONTACT("https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxgetcontact"),

    /**
     * 6、批量获取联系人详情
     * 说明：批量获取联系人详情，人或群均可。获取群详情主要是获取群内联系人列表。获取人详情主要是获取群内的某个人的详细信息。
     * 请求方式：POST
     */
    BATCH_GET_CONTACT("https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxbatchgetcontact"),

    /**
     * 7、消息检查
     * 说明：同步消息检查。这里只做检查不做同步，如果检查出有新消息，再掉具体同步的接口。
     * 请求方式：POST
     */
    SYNC_CHK("https://webpush2.weixin.qq.com/cgi-bin/mmwebwx-bin/synccheck"),

    /**
     * 8、获取最新消息
     * 当同步检查接口显示有新消息时，调用该接口获取具体的新消息。此处的新消息为广义的，包括消息，修改群名，群内成员变化，加好友等。
     * 请求方式：POST
     */
    SYNC_MSG("https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxsync"),

    /**
     * 9、发送消息
     * 说明：发送文本消息（包括表情），不能发送图片或文件。
     * 请求方式：POST
     */
    SND_MSG("https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxsendmsg"),

    SND_IMG_TO_SERVER("https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxuploadmedia"),

    SND_IMG("https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxsendmsgimg");

    private String url;
}
