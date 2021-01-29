package sunhongbin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sunhongbin.entity.User;
import sunhongbin.enums.*;
import sunhongbin.service.WeChatService;
import sunhongbin.util.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

/**
 * created by SunHongbin on 2020/4/27
 */
@Service
public class WeChatServiceImpl implements WeChatService {

    private static final Logger LOG = LoggerFactory.getLogger(WeChatServiceImpl.class);

    @Autowired
    private ExecutorService executorService;

    /**
     * 存放请求redirect_uri获得的公参
     */
    private Map<String, String> globalParamsMap;

    private JSONObject baseRequest, SyncKey;

    private JSONArray contactLst;

    private User user;

    private String sendToGroup, sendToSomeone;

    @Override
    public String getUUID() {

        LOG.info("开始获取UUID……");

        Map<String, String> requestParamMap = new HashMap<>(16);

        // 固定为wx782c26e4c19acffb
        requestParamMap.put("appid", "wx782c26e4c19acffb");
        // 固定为new
        requestParamMap.put("fun", "new");
        // 语言格式
        requestParamMap.put("lang", "zh_CN");

        String result = HttpUtil.deGet(WeChatApi.GET_UUID.getUrl(), requestParamMap);

        String uuid = "";

        if (!StringUtils.isEmpty(result)) {
            // window.QRLogin.code = 200; window.QRLogin.uuid = "YeNy9w_Sgw==";
            String code = StringOperationUtil.match(result, "window.QRLogin.code = (\\d+);");

            if (!StringUtils.isEmpty(code) && StringUtils.equals(code, "200")) {
                uuid = StringOperationUtil.match(result, "window.QRLogin.uuid = \"(.*)\";");
                return uuid;
            } else {
                LOG.error("获取UUID码报错，错误码：" + code);
            }
        }
        return uuid;
    }

    @Override
    public void showQRCode(String uuid) {

        // download QR code
        String uri = WeChatApi.GET_QR_CODE.getUrl() + "/" + uuid + "?t=webwx";

        File file = HttpUtil.doGetFile(uri);

        // using Google open source ZXING tool
        Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);

        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        //默认的容错级别是L,代码中注释是7，设置成M/H的话二维码就越长了
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        // 二维码四周的边缘大小，值设置的越大，边缘一圈就越厚
        hintMap.put(EncodeHintType.MARGIN, 1);

        try {
            // qrContent: // https://login.weixin.qq.com/l/IYGBnzQjqA==
            String qrContent = QRCodeUtil.translateFileToQrContent(file, hintMap);

            // encode file to bit matrix
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE,
                    QrCodeProperties.QR_CODE_WIDTH.getValue(),
                    QrCodeProperties.QR_CODE_HEIGHT.getValue(), hintMap);

            // translate into qrCode.png
            Path path = new File(FileUtil.getImageFilePath("qrCode.png")).toPath();
            MatrixToImageWriter.writeToPath(bitMatrix, "png", path);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        LOG.info("二维码成功保存到本地！");
    }

    @Override
    public String pollForScanRes(String uuid) {

        Map<String, String> paramMap = new HashMap<>();
        // 参数tip : 1表示未扫描 0表示已扫描
        paramMap.put("tip", "1");
        paramMap.put("uuid", uuid);

        // response：
        // window.code 扫描结果，201表示扫描成功，200表示确认登录，还有一个408，表示超时（一直没有扫码）
        // window.userAvatar用户头像base64编码
        // window.redirect_uri获取初始化信息的重定向Url
        String requestRes = HttpUtil.deGet(WeChatApi.IS_SCAN_QR_CODE.getUrl(), paramMap);

        if (StringUtils.isEmpty(requestRes)) {
            LOG.error("扫描二维码失败");
            return null;
        }

        // TODO 不以日志形式出现，将信息返回前端
        String code = StringOperationUtil.match(requestRes, "window.code=(\\d+);");
        if (StringUtils.isEmpty(code)) {

            LOG.error("无法从返回报文中获取返回码！");

        } else if (StringUtils.equals(code, "201")) {

            LOG.error("扫描成功，请点击确认按钮!");

        } else if (StringUtils.equals(code, "200")) {

            LOG.info("扫描成功，正在登陆，请稍候……");

            setGlobalParams(requestRes);

        } else if (StringUtils.equals(code, "408")) {

            LOG.error("登陆超时（您一直没有扫码）!");

        } else {

            LOG.error("HTTP 返回码：" + code);
        }
        return code;
    }

    @Override
    public void initializeweChat() {

        //初始化微信首页栏的联系人、公众号等（不是通讯录里的联系人），初始化登录者自己的信息（包括昵称等），初始化同步消息所用的SycnKey
        String url = WeChatApi.WEB_WX_INIT.getUrl() + "?r=" + (~System.currentTimeMillis()) + "pass_ticket" + globalParamsMap.get("pass_ticket");

        JSONObject param = new JSONObject();

        param.put("BaseRequest", baseRequest);
        String res;
        try {
            res = HttpUtil.doPost(url, param);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            return;
        }

        JSONObject jsonRes = JSON.parseObject(res);

        JSONObject user = jsonRes.getJSONObject("User");

        this.user = new User(user);

        this.SyncKey = jsonRes.getJSONObject("SyncKey");

        LOG.info("[SyncKey] " + SyncKey.toJSONString());

        //TODO logger转前端
        LOG.info("微信初始化成功，欢迎登陆：" + this.user.getNickName());
    }

    @Override
    public boolean wxStatusNotify() {
        String url = WeChatApi.WX_STATUS_NOTIFY.getUrl() + "?lang=zh_CN&pass_ticket=" + globalParamsMap.get("pass_ticket");

        JSONObject paramJson = new JSONObject();
        paramJson.put("BaseRequest", baseRequest);
        paramJson.put("Code", 3);
        paramJson.put("FromUserName", user.getUserName());
        paramJson.put("ToUserName", user.getUserName());
        paramJson.put("ClientMsgId", System.currentTimeMillis());
        String res;
        try {
            res = HttpUtil.doPost(url, paramJson);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            return false;
        }

        JSONObject jsonRes = JSON.parseObject(res);
        JSONObject baseResponse = jsonRes.getJSONObject("BaseResponse");
        if (null != baseResponse) {
            int ret = baseResponse.getInteger("Ret");
            // ret为0则开启成功
            return ret == 0;
        }
        return false;
    }

    @Override
    public boolean loadContactPerson() {

        String url = WeChatApi.GET_CONTACT.getUrl() +
                "?pass_ticket=" + globalParamsMap.get("pass_ticket") + "&skey=" + globalParamsMap.get("skey") +
                "&r=" + System.currentTimeMillis();

        JSONObject paramJson = new JSONObject();
        paramJson.put("BaseRequest", baseRequest);
        String res;
        try {
            res = HttpUtil.doPost(url, paramJson);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            return false;
        }

        JSONObject jsonRes = JSON.parseObject(res);
        JSONObject baseResponse = jsonRes.getJSONObject("BaseResponse");
        if (null != baseResponse) {
            int ret = baseResponse.getInteger("Ret");
            if (0 == ret) {
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
//                        System.out.println("姓名：" + msg.getString("NickName") + "  个性签名：" + msg.getString("Signature"));
                        friendList.add(msg);
                        contactLst = friendList;
                    }
                });
                LOG.info("共有联系人数量：" + contactLst.size());
                return true;
            }
        }
        return false;
    }

    @Override
    public void listeningInMsg() {

        executorService.execute(() -> {
            while (true) {
                int[] syncRes = synccheck();
                if (syncRes.length != 2) {
                    LOG.error("接收错误");
                    break;
                }
                if (syncRes[0] == SyncCheckRetCodeEnum.SUCCESS.getIndex()) {
                    if (syncRes[1] == SyncChecSelectorEnum.NEW_MSG.getIndex() || syncRes[1] == SyncChecSelectorEnum.ADD_OR_DEL_CONTACT.getIndex()) {
                        webwxSync();
                        sendMsgToWeChatFriend("", MemberTypeEnum.GROUP);
                    } else if (syncRes[1] == SyncChecSelectorEnum.NORMAL.getIndex()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            LOG.error(e.getLocalizedMessage());
                        }
                    } else if (syncRes[1] == SyncChecSelectorEnum.MOD_CONTACT.getIndex()) {
                        LOG.info(SyncChecSelectorEnum.MOD_CONTACT.getDesc());
                    }
                } else {
                    LOG.error("returnCode: " + SyncCheckRetCodeEnum.stateOf(syncRes[0]).getDesc() +
                            " selector: " + SyncChecSelectorEnum.stateOf(syncRes[1]).getDesc());
                    break;
                }

            }
        });

        // 1、chk msg

        // 2、if data, sync msg

        // 3、handle msg and send Msg To WeChat Friend
    }

    private int[] synccheck() {
        String url = WeChatApi.SYNC_CHK.getUrl();

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("r", RamdonIdUtil.getRandomIdWithUsrNam(user.getNickName()));
        paramMap.put("skey", globalParamsMap.get("skey"));
        paramMap.put("sid", globalParamsMap.get("wxSid"));
        paramMap.put("uin", globalParamsMap.get("wxUin"));
        paramMap.put("deviceid", "e" + String.valueOf(new Random().nextLong()).substring(1, 16));
        paramMap.put("synckey", SyncKey.toJSONString());
        paramMap.put("_", RamdonIdUtil.getRandomIdWithUsrNam(user.getNickName()));

        // window.synccheck={retcode:"1100",selector:"0"}
        String response = HttpUtil.deGet(url, paramMap);

        JSONObject jsonRes = JSON.parseObject(response.substring(response.indexOf("{")));
        int[] res = new int[2];
        res[0] = jsonRes.getInteger("retcode");
        res[1] = jsonRes.getInteger("selector");
        return res;

    }

    private JSONObject webwxSync() {
        String url = WeChatApi.SYNC_MSG.getUrl()
                + "?lang=zh_CN"
                + "&pass_ticket=" + globalParamsMap.get("pass_ticket")
                + "&skey=" + globalParamsMap.get("skey")
                + "&sid=" + globalParamsMap.get("wxSid")
                + "&r=" + System.currentTimeMillis();

        JSONObject paramJon = new JSONObject();
        paramJon.put("BaseRequest", baseRequest);
        paramJon.put("SyncKey", SyncKey.toJSONString());
        paramJon.put("rr", System.currentTimeMillis());

        String response = "";
        try {
            response = HttpUtil.doPost(url, paramJon);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
        }

        JSONObject resJson = JSON.parseObject(response);
        if (resJson != null) {
            int ret = resJson.getInteger("Ret");
            if (ret == 0) {
                this.SyncKey = resJson.getJSONObject("SyncKey");


            }
        }


        return null;
    }

    @Override
    public void sendMsgToWeChatFriend(String msg, MemberTypeEnum memberTypeEnum) {
        String targetUser = "";
        switch (memberTypeEnum) {
            case GROUP:
                targetUser = sendToGroup;
                break;
            case PEOPLE:
                targetUser = sendToSomeone;
            default:
                break;
        }

        String url = WeChatApi.SND_MSG + "?lang=zh_CN&?pass_ticket=" + globalParamsMap.get("pass_ticket");

        JSONObject paramJson = new JSONObject();
        JSONObject MsgJson = new JSONObject();
        String id = RamdonIdUtil.getRandomIdWithUsrNam(user.getUserName());
        MsgJson.put("Type", 1);
        MsgJson.put("Content", msg);
        MsgJson.put("FromUserName", user.getUserName());
        MsgJson.put("ToUserName", targetUser);
        MsgJson.put("LocalID", id);
        MsgJson.put("ClientMsgId", id);

        paramJson.put("BaseRequest", baseRequest);
        paramJson.put("Msg", MsgJson);

        String res;
        try {
            res = HttpUtil.doPost(url, paramJson);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            return;
        }

        LOG.info("[sendMsgToWeChatFriend] " + res);
    }

    private void setGlobalParams(String requestRes) {

        // 公共参数集合
        this.globalParamsMap = new HashMap<>();

        // res: window.code=200;\nwindow.redirect_uri="https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage?ticket=Ab6YXYE91BuiEBIfL9YJ8w-k@qrticket_0&uuid=4ZrteSt-zg==&lang=zh_CN&scan=1588342601";
        String redirect_uri = StringOperationUtil.match(requestRes, "window.redirect_uri=\"(\\S+?)\";") + "&fun=new";

        // 扫描成功则根据返回结果，解析返回的包括redirect_uri并获取一系列的URL，base_uri，webpush_url
        if (StringUtils.isEmpty(redirect_uri)) {
            LOG.error("获取重定向 URL 失败");
            return;
        }

        String xml = HttpUtil.doGetGlobalParams(redirect_uri);

        if (!StringUtils.isEmpty(xml)) {
            globalParamsMap.put("skey", xml.substring(xml.indexOf("<skey>") + "<skey>".length(), xml.indexOf("</skey>")));
            globalParamsMap.put("wxSid", xml.substring(xml.indexOf("<wxsid>") + "<wxsid>".length(), xml.indexOf("</wxsid>")));
            globalParamsMap.put("wxUin", xml.substring(xml.indexOf("<wxuin>") + "<wxuin>".length(), xml.indexOf("</wxuin>")));
            globalParamsMap.put("pass_ticket", xml.substring(xml.indexOf("<pass_ticket>") + "<pass_ticket>".length(), xml.indexOf("</pass_ticket>")));
        } else {
            throw new IllegalArgumentException("参数解析错误");
        }

        if (globalParamsMap.size() != 0) {
            this.baseRequest = new JSONObject();
            baseRequest.put("Skey", globalParamsMap.get("skey"));
            baseRequest.put("Sid", globalParamsMap.get("wxSid"));
            baseRequest.put("Uin", globalParamsMap.get("wxUin"));
            baseRequest.put("DeviceID", "e" + String.valueOf(new Random().nextLong()).substring(1, 16));
        } else {
            throw new IllegalArgumentException("全局参数为空");
        }
    }

}
