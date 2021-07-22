package sunhongbin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sunhongbin.entity.BaseRequest;
import sunhongbin.entity.GlobalParam;
import sunhongbin.entity.User;
import sunhongbin.enums.RetCodeEnum;
import sunhongbin.enums.SelectorEnum;
import sunhongbin.enums.WeChatApi;
import sunhongbin.exception.WeChatException;
import sunhongbin.service.SmartRobotService;
import sunhongbin.service.WeChatService;
import sunhongbin.util.*;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.zxing.BarcodeFormat.QR_CODE;
import static sunhongbin.enums.error.InitErrorEnum.*;

/**
 * created by SunHongbin on 2020/4/27
 */
@Service
public class WeChatServiceImpl implements WeChatService {

    private static final Logger LOG = LoggerFactory.getLogger(WeChatServiceImpl.class);
    /**
     * CODE_WIDTH：二维码宽度，单位像素
     * CODE_HEIGHT：二维码高度，单位像素
     * FRONT_COLOR：二维码前景色，0x000000 表示黑色
     * BACKGROUND_COLOR：二维码背景色，0xFFFFFF 表示白色
     * 演示用 16 进制表示，和前端页面 CSS 的取色是一样的，注意前后景颜色应该对比明显，如常见的黑白
     */
    private static final int QR_CODE_WIDTH = 200;

    private static final int QR_CODE_HEIGHT = 200;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private SmartRobotService smartRobotService;

    private BaseRequest baseRequest;

    private JSONArray contactList;

    private String syncKey;

    private User user;

    private String sendToGroup, sendToSomeone;

    @Override
    public String getUUID() throws WeChatException {

        LOG.info("开始获取UUID……");

        // getUuidRes = "window.QRLogin.code = 200; window.QRLogin.uuid = "Ya1aKyCy9A==";"
        String getUuidRes = HttpUtil.doGet(WeChatApi.GET_UUID.getUrl() + "&_=" + System.currentTimeMillis());

        if (!StringUtils.isEmpty(getUuidRes)) {
            // window.QRLogin.code = 200; window.QRLogin.uuid = "YeNy9w_Sgw==";
            String code = StringOperationUtil.match(getUuidRes, "window.QRLogin.code = (\\d+);");

            if (!StringUtils.isEmpty(code) && StringUtils.equals(code, "200")) {
                String uuid = StringOperationUtil.match(getUuidRes, "window.QRLogin.uuid = \"(.*)\";");
                if (!StringUtils.isEmpty(uuid)) {
                    LOG.info("获取得到UUID成功，UUID：" + uuid);
                    return uuid;
                }
            } else {
                throw new WeChatException("获取UUID码报错，错误码：" + code);
            }
        }
        return null;
    }

    @Override
    public void showQRCode(String uuid) {

        LOG.info("开始获取登陆微信二维码……");

        // download QR code
        File file = HttpUtil.doGetFile(WeChatApi.GET_QR_CODE.getUrl() + "/" + uuid + "?t=webwx");

        // using Google open source ZXING tool
        Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);

        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        //默认的容错级别是L,代码中注释是7，设置成M/H的话二维码就越长了
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        // 二维码四周的边缘大小，值设置的越大，边缘一圈就越厚
        hintMap.put(EncodeHintType.MARGIN, 1);

        // qrContent: // https://login.weixin.qq.com/l/IYGBnzQjqA==
        String qrContent = QRCodeUtil.translateFileToQrContent(file, hintMap);

        // encode file to bit matrix
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT, hintMap);

            // translate into qrCode.png
            Path path = new File(FileUtil.getImageFilePath("qrCode.png")).toPath();

            MatrixToImageWriter.writeToPath(bitMatrix, "png", path);
        } catch (Exception e) {
            throw new WeChatException("二维码获取时发生异常: " + e.getMessage());
        }
        LOG.info("二维码获取成功！已保存到本地！");
    }

    @Override
    public GlobalParam pollForScanRes(String uuid) {

        String code = "";

        String redirectUri = "";

        do {
            LOG.info("监测扫描结果中……");

            // 参数tip : 1表示未扫描 0表示已扫描
            // window.code=200;window.redirect_uri="https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage?ticket=AbeQzZc-rAs5OpooVWUiwH3p@qrticket_0&uuid=4Z7BYUaK3g==&lang=zh_CN&scan=1612949076";
            String url = WeChatApi.IS_SCAN_QR_CODE.getUrl() + "?uuid=" + uuid + "&tip=1&r=" + System.currentTimeMillis() + "&_=" + System.currentTimeMillis();

            String isScanQrCodeRes = HttpUtil.doGet(url);

            if (StringUtils.isEmpty(isScanQrCodeRes)) {
                LOG.error("扫描二维码失败");
            } else {
                code = StringOperationUtil.match(isScanQrCodeRes, "window.code=(\\d+);");
            }
            switch (code) {
                case "200":
                    LOG.info("监测扫描结果：扫描成功，正在登录，请稍候……");
                    redirectUri = StringOperationUtil.match(isScanQrCodeRes, "window.redirect_uri=\"(\\S+?)\";") + "&fun=new";
                    break;
                case "201":
                    LOG.info("监测扫描结果：扫描成功，请点击确认按钮!");
                    break;
                case "408":
                    LOG.error("监测扫描结果：登陆超时（您一直没有扫码）!");
                    break;
                default:
                    LOG.info("扫描code = " + code);
                    break;
            }
            try {
                // 每次探测间隔2s用户信息
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new WeChatException(e.getMessage());
            }
        } while (!StringUtils.equals(code, "200"));

        return getGlobalParams(redirectUri);
    }

    @Override
    public void initializeWeChat(GlobalParam globalParam) {

        LOG.info("开始初始化微信……");

        //初始化微信首页栏的联系人、公众号等（不是通讯录里的联系人），初始化登录者自己的信息（包括昵称等），初始化同步消息所用的SycnKey
        String url = WeChatApi.WEB_WX_INIT.getUrl() + "?r=" + (~System.currentTimeMillis()) + "&pass_ticket" + globalParam.getPass_ticket();
        JSONObject param = new JSONObject();
        param.put("BaseRequest", baseRequest);
        String res = HttpUtil.doPost(url, param);

        JSONObject jsonRes = JSON.parseObject(res);

        JSONObject user = jsonRes.getJSONObject("User");

        this.user = new User(user);

        this.syncKey = getSyncKey(jsonRes);

        LOG.info("⭐⭐初始化成功，欢迎登陆!");
    }

    private String getSyncKey(JSONObject jsonRes) {
        // {"List":[{"Val":723272423,"Key":1},{"Val":723273065,"Key":2},{"Val":723273028,"Key":3},{"Val":1612955521,"Key":1000}],"Count":4}
        JSONArray list = jsonRes.getJSONObject("SyncKey").getJSONArray("List");

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            // {"Val":723272423,"Key":1}
            JSONObject object = list.getJSONObject(i);
            stringBuilder.append("|").append(object.getString("Key")).append("_").append(object.getString("Val"));
        }

        return stringBuilder.substring(1);
    }

    @Override
    public void wxStatusNotify(GlobalParam globalParam) {

        LOG.info("开启微信状态通知……");

        String url = WeChatApi.WX_STATUS_NOTIFY.getUrl() + "?pass_ticket=" + globalParam.getPass_ticket();

        JSONObject paramJson = new JSONObject();
        paramJson.put("BaseRequest", baseRequest);
        paramJson.put("ClientMsgId", System.currentTimeMillis());
        paramJson.put("Code", 3);
        paramJson.put("FromUserName", user.getUserName());
        paramJson.put("ToUserName", user.getUserName());

        String res = HttpUtil.doPost(url, paramJson);

        JSONObject jsonRes = JSON.parseObject(res);
        JSONObject baseResponse = jsonRes.getJSONObject("BaseResponse");
        if (null != baseResponse) {
            int ret = baseResponse.getInteger("Ret");
            // ret为0则开启成功
            LOG.info("微信状态提醒开启成功");
            if (ret != 0) {
                throw new WeChatException(WX_STATUS_NOTIFY_EXCEPTION.getDesc());
            }
        }
    }

    @Override
    public void loadContactPerson(GlobalParam globalParam) {

        LOG.info("开始加载联系人……");

        String url = WeChatApi.GET_CONTACT.getUrl()
                + "?pass_ticket=" + globalParam.getPass_ticket()
                + "&skey=" + globalParam.getSkey()
                + "&r=" + System.currentTimeMillis();

        JSONObject paramJson = new JSONObject();
        paramJson.put("BaseRequest", baseRequest);
        String res = HttpUtil.doPost(url, paramJson);

        JSONObject jsonRes = JSON.parseObject(res);
        JSONObject baseResponse = jsonRes.getJSONObject("BaseResponse");
        if (baseResponse != null) {
            int ret = baseResponse.getInteger("Ret");
            if (ret == 0) {
                JSONArray memberList = jsonRes.getJSONArray("MemberList");
                JSONArray friendList = new JSONArray();
                memberList.forEach(member -> {
                    JSONObject msg = (JSONObject) member;
                    if (msg.getInteger("VerifyFlag") != null
                            // 公众号或者服务号不加载(0: 人, 8: 公众号, 24: 服务号)
                            && msg.getInteger("VerifyFlag") != 8 && msg.getInteger("VerifyFlag") != 24
                            // 群聊不加载
                            && !msg.getString("UserName").contains("@@")
                            // 自己不加载
                            && !StringUtils.equals(msg.getString("UserName"), user.getUserName())
                    ) {
//                        LOG.info("姓名：" + msg.getString("NickName") + "  个性签名：" + msg.getString("Signature"));
                        friendList.add(msg);
                    }
                    contactList = friendList;
                });
                LOG.info("联系人加载成功！共有联系人数量：" + contactList.size());
            }
        } else {
            throw new WeChatException(LOAD_CONTACT_PERSON_FAILED.getDesc() + "：返回码不等于0");
        }
    }

    @Override
    public void listeningInMsg(GlobalParam globalParam) {

        LOG.info("开始监听消息……");

        executorService.execute(() -> {
            while (true) {
                // 监听消息时调用API返回的信息
                int[] syncRes = syncCheck(globalParam);

                if (syncRes[0] == RetCodeEnum.SUCCESS.getIndex()) {
                    if (syncRes[1] == SelectorEnum.NEW_MSG.getIndex()) {
                        JSONObject msg = webwxSync(globalParam);
                        handleMsg(msg, globalParam);
                    }
                    if (syncRes[1] == SelectorEnum.NORMAL.getIndex()) {
                       LOG.info("成功");
                    }
                    if (syncRes[1] == SelectorEnum.ADD_OR_DEL_CONTACT.getIndex()) {
                        LOG.info("存在删除或者新增的好友信息");
                    }
                    if (syncRes[1] == SelectorEnum.MOD_CONTACT.getIndex()) {
                        LOG.info(SelectorEnum.MOD_CONTACT.getDesc());
                    }
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        LOG.error(e.getLocalizedMessage());
                    }
                } else {
                    LOG.error("接收信息错误！ returnCode: " + Objects.requireNonNull(RetCodeEnum.stateOf(syncRes[0])).getDesc() +
                            " selector: " + Objects.requireNonNull(SelectorEnum.stateOf(syncRes[1])).getDesc());
                    break;
                }

            }
        });
    }

    private void handleMsg(JSONObject data, GlobalParam globalParam) {
        if (data == null) {
            return;
        }
        JSONArray AddMsgList = data.getJSONArray("AddMsgList");

        for (Object obj : AddMsgList) {
            JSONObject msg = (JSONObject) obj;
            // MsgType 说明
            // 1 文本消息; 3 图片消息;  34 语音消息; 37 VERIFYMSG; 40 POSSIBLEFRIEND_MSG
            // 42 共享名片; 43 视频通话消息; 47 动画表情; 48 位置消息; 49 分享链接
            // 50 VOIPMSG; 51 微信初始化消息; 52 VOIPNOTIFY; 53 VOIPINVITE; 62 小视频
            // 9999  SYSNOTICE; 10000  系统消息; 10002  撤回消息
            int msgType = msg.getInteger("MsgType");

            switch (msgType) {
                case 1:
                    // 获取发送者发送的内容
                    String content = msg.getString("Content");
                    LOG.info("接收到微信消息: " + content);

                    // 拿到群组的微信名以刷新缓存（有备注取备注，没备注取本名）
                    if (StringUtils.equals(getContactName(msg.getString("FromUserName")), "还是骚年")) {
                        this.sendToGroup = msg.getString("FromUserName");
                    }

                    // 发送给智能机器人并获取回复
                    String reply;
                    String[] contentArray = content.split(":<br/>");
                    if (contentArray.length == 2) {
                        content = contentArray[1].replace("孙鸿滨", "");
                        reply = smartRobotService.aiReply(content);
                    } else {
                        reply = "请@我然后加上你想对我说的话呀~";
                    }

                    // 调用发送信息的接口
                    sendMsgToWeChatFriend(reply, sendToGroup, globalParam);
                    break;
                case 3:
                    LOG.info("接收到微信图片！");
                    break;
                case 34:
                    LOG.info("接收到微信语音！");
                    break;
                case 42:
                    LOG.info("接收到微信名片！");
                    break;
                case 51:
                    LOG.info("成功截获微信初始化消息！");
                    break;
                default:
                    break;
            }
        }
    }

    private int[] syncCheck(GlobalParam globalParam) {

        String url = WeChatApi.SYNC_CHK.getUrl()
                + "?r=" + System.currentTimeMillis()
                + "&skey=" + globalParam.getSkey()
                + "&sid=" + globalParam.getWxsid()
                + "&uin=" + globalParam.getWxuin()
                + "&deviceid=" + "e" + String.valueOf(new Random().nextLong()).substring(1, 16)
                + "&synckey=" + syncKey
                + "&_=" + System.currentTimeMillis();

        // window.synccheck={retcode:"0",selector:"2"}
        String syncCheckRes = HttpUtil.doGet(url);

        JSONObject jsonRes = JSON.parseObject(syncCheckRes.substring(syncCheckRes.indexOf("{")));
        if (jsonRes.size() != 2) {
            throw new WeChatException(SYNC_CHK_EXCEPTION.getDesc() + jsonRes);
        }
        int[] syncRes = new int[2];
        syncRes[0] = jsonRes.getInteger("retcode");
        syncRes[1] = jsonRes.getInteger("selector");

        LOG.info("消息同步检查成功" + Arrays.toString(syncRes));

        return syncRes;
    }

    private JSONObject webwxSync(GlobalParam globalParam) {
        String url = WeChatApi.SYNC_MSG.getUrl()
                + "?lang=zh_CN"
                + "&pass_ticket=" + globalParam.getPass_ticket()
                + "&skey=" + globalParam.getSkey()
                + "&sid=" + globalParam.getWxsid()
                + "&r=" + System.currentTimeMillis();

        JSONObject paramJon = new JSONObject();
        paramJon.put("BaseRequest", baseRequest);
        paramJon.put("SyncKey", syncKey);
        paramJon.put("rr", System.currentTimeMillis());

        String response = HttpUtil.doPost(url, paramJon);

        if (StringUtils.isNotEmpty(response)) {

            JSONObject jsonObject = JSON.parseObject(response);

            String ret = jsonObject.getString("Ret");
            if (StringUtils.equals(ret, "0")) {
                this.syncKey = getSyncKey(jsonObject);
                return jsonObject;
            }
        }
        return null;
    }

    @Override
    public void sendMsgToWeChatFriend(String msg, String target, GlobalParam globalParam) {
        String url = WeChatApi.SND_MSG + "?lang=zh_CN&?pass_ticket=" + globalParam.getPass_ticket();

        JSONObject paramJson = new JSONObject();
        JSONObject MsgJson = new JSONObject();
        String id = RamdonIdUtil.getRandomIdWithUsrNam(user.getUserName());
        MsgJson.put("Type", 1);
        MsgJson.put("Content", msg);
        MsgJson.put("FromUserName", user.getUserName());
        MsgJson.put("ToUserName", target);
        MsgJson.put("LocalID", id);
        MsgJson.put("ClientMsgId", id);

        paramJson.put("BaseRequest", baseRequest);
        paramJson.put("Msg", MsgJson);

        String res = HttpUtil.doPost(url, paramJson);

        LOG.info("[sendMsgToWeChatFriend] " + res);
    }

    /**
     * @param redirectUrl https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage?ticket=AbeQzZc-rAs5OpooVWUiwH3p@qrticket_0&uuid=4Z7BYUaK3g==&lang=zh_CN&scan=1612949076"
     * @return
     */
    private GlobalParam getGlobalParams(String redirectUrl) {

        // 扫描成功则根据返回结果，解析返回的包括redirect_uri并获取一系列的URL，base_uri，webpush_url
        if (StringUtils.isEmpty(redirectUrl)) {
            throw new WeChatException("获取重定向 URL 失败");
        }

        // 存放请求redirect_uri获得的公参
        GlobalParam globalParam = new GlobalParam();

        LOG.info("正在获取全局参数……");
        String getBaseReqRes = HttpUtil.doGet(redirectUrl);

        if (!StringUtils.isEmpty(getBaseReqRes)) {
            String ret = getBaseReqRes.substring(getBaseReqRes.indexOf("<ret>") + "<ret>".length(), getBaseReqRes.indexOf("</ret>"));
            if (!StringUtils.equals(ret, "0")) {
                String message = getBaseReqRes.substring(getBaseReqRes.indexOf("<message>") + "<message>".length(), getBaseReqRes.indexOf("</message>"));
                throw new WeChatException(message);
            }
            this.baseRequest = new BaseRequest();

            String skey = getBaseReqRes.substring(getBaseReqRes.indexOf("<skey>") + "<skey>".length(), getBaseReqRes.indexOf("</skey>"));
            globalParam.setSkey(skey);
            baseRequest.setSkey(skey);

            String sid = getBaseReqRes.substring(getBaseReqRes.indexOf("<wxsid>") + "<wxsid>".length(), getBaseReqRes.indexOf("</wxsid>"));
            globalParam.setWxsid(sid);
            baseRequest.setSid(sid);

            String uin = getBaseReqRes.substring(getBaseReqRes.indexOf("<wxuin>") + "<wxuin>".length(), getBaseReqRes.indexOf("</wxuin>"));
            globalParam.setWxuin(uin);
            baseRequest.setUin(uin);

            globalParam.setPass_ticket(getBaseReqRes.substring(getBaseReqRes.indexOf("<pass_ticket>") + "<pass_ticket>".length(), getBaseReqRes.indexOf("</pass_ticket>")));

            baseRequest.setDeviceID("e" + String.valueOf(new Random().nextLong()).substring(1, 16));
        }

        return globalParam;
    }

    private String getContactName(String fromUserName) {

        for (Object contact : contactList) {

            JSONObject info = (JSONObject) contact;

            if (info.getString("UserName").equals(fromUserName)) {
                // 备注名
                String nickName = info.getString("RemarkName");
                if (StringUtils.isNotEmpty(nickName)) {
                    return nickName;
                }
                // 如果好友未备注就返回原本的名字
                return info.getString("NickName");
            }
        }

        return null;
    }
}
