package sunhongbin.enums;

/**
 * created by SunHongbin on 2020/5/2
 */
public enum WeChatApiUrl {

    GET_UUID("https://login.weixin.qq.com/jslogin",
            "https://login.wx.qq.com/jslogin?appid=wx782c26e4c19acffb&fun=new&lang=zh_CN"),

    /**
     * 获取登陆微信二维码方式1
     * 将 String (https://login.weixin.qq.com/l/{$UUID}) 转换成二维码
     * 扫描二维码，也就是扫描 https://login.weixin.qq.com/l/{$UUID}
     * <p>
     * 自己猜测隐藏的弊端：
     * 可能这个URL的格式（login.weixin.qq.com/l/）是一直在变的
     * 优点：
     * 减少一次URL请求
     */
    GET_QR_CODE("https://login.weixin.qq.com/qrcode", ""),

    /**
     * 获取登陆微信二维码方式2
     * 先发送请求：https://login.weixin.qq.com/qrcode/{$UUID}
     * 微信返回我们 URL（InputStream），内容为 https://login.weixin.qq.com/l/{$UUID}
     * 将微信返回给我们的 InputStream 转换为 File
     * 将 File 中的内容解析成 String ： https://login.weixin.qq.com/l/{$UUID}
     * 将 String 转换成二维码
     * 扫描二维码，也就是扫描 https://login.weixin.qq.com/l/{$UUID}
     */
    GET_QR_CODE_WAY2("https://login.weixin.qq.com/l/",
              "https://login.weixin.qq.com/l/IYGBnzQjqA=="),

    //尝试登录。若此时用户手机已完成扫码并点击登录，则返回一个真正用于登录的url地址。否则接口大概10s后返回未扫码或未登录的状态码
    IS_SCAN_QR_CODE("https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login", "");





    private String url;

    private String urlWithParam;

    WeChatApiUrl(String url, String urlWithParam) {
        this.url = url;
        this.urlWithParam = urlWithParam;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlWithParam() {
        return urlWithParam;
    }

}
