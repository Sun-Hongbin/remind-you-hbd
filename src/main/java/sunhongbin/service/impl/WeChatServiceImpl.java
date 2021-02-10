package sunhongbin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.BarcodeFormat;
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
import sunhongbin.enums.QrCodeProperties;
import sunhongbin.enums.SyncChecSelectorEnum;
import sunhongbin.enums.SyncCheckRetCodeEnum;
import sunhongbin.enums.WeChatApi;
import sunhongbin.exception.WeChatException;
import sunhongbin.service.WeChatService;
import sunhongbin.util.*;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static sunhongbin.enums.error.InitErrorEnum.*;

/**
 * created by SunHongbin on 2020/4/27
 */
@Service
public class WeChatServiceImpl implements WeChatService {

    private static final Logger LOG = LoggerFactory.getLogger(WeChatServiceImpl.class);

    @Autowired
    private ExecutorService executorService;

    private BaseRequest baseRequest;

    private JSONArray contactLst;

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
            throw new WeChatException("二维码获取失败: " + e.getMessage());
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
    public String initializeWeChat(GlobalParam globalParam) {

        LOG.info("开始初始化微信……");

        //初始化微信首页栏的联系人、公众号等（不是通讯录里的联系人），初始化登录者自己的信息（包括昵称等），初始化同步消息所用的SycnKey
        String url = WeChatApi.WEB_WX_INIT.getUrl() + "?r=" + (~System.currentTimeMillis()) + "&pass_ticket" + globalParam.getPass_ticket();
        JSONObject param = new JSONObject();
        param.put("BaseRequest", baseRequest);
        String res = HttpUtil.doPost(url, param);

        JSONObject jsonRes = JSON.parseObject(res);

        JSONObject user = jsonRes.getJSONObject("User");

        this.user = new User(user);

        // {"List":[{"Val":723272423,"Key":1},{"Val":723273065,"Key":2},{"Val":723273028,"Key":3},{"Val":1612955521,"Key":1000}],"Count":4}
        JSONArray list = jsonRes.getJSONObject("SyncKey").getJSONArray("List");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            // {"Val":723272423,"Key":1}
            JSONObject object =list.getJSONObject(i);
            stringBuilder.append("|").append(object.getString("Key")).append("_").append(object.getString("Val"));
        }
        LOG.info("⭐⭐初始化成功，欢迎登陆!");

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
                        contactLst = friendList;
                    }
                });
                LOG.info("联系人加载成功！共有联系人数量：" + contactLst.size());
            }
        } else {
            throw new WeChatException(LOAD_CONTACT_PERSON_FAILED.getDesc() + "：返回码不等于0");
        }
    }

    @Override
    public void listeningInMsg(GlobalParam globalParam, String syncKey) {

        LOG.info("开始监听消息……");

        executorService.execute(() -> {
            while (true) {
                // 监听消息时调用API返回的信息
                int[] syncRes = syncCheck(globalParam, syncKey);

                if (syncRes[0] == SyncCheckRetCodeEnum.SUCCESS.getIndex()) {
                    if (syncRes[1] == SyncChecSelectorEnum.NEW_MSG.getIndex() || syncRes[1] == SyncChecSelectorEnum.ADD_OR_DEL_CONTACT.getIndex()) {
                        String msg = webwxSync(globalParam, syncKey);
                        sendMsgToWeChatFriend(msg, sendToGroup, globalParam);
                    } else if (syncRes[1] == SyncChecSelectorEnum.NORMAL.getIndex()) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException e) {
                            LOG.error(e.getLocalizedMessage());
                        }
                    } else if (syncRes[1] == SyncChecSelectorEnum.MOD_CONTACT.getIndex()) {
                        LOG.info(SyncChecSelectorEnum.MOD_CONTACT.getDesc());
                    }
                } else {
                    LOG.error("接收信息错误！ returnCode: " + Objects.requireNonNull(SyncCheckRetCodeEnum.stateOf(syncRes[0])).getDesc() +
                            " selector: " + Objects.requireNonNull(SyncChecSelectorEnum.stateOf(syncRes[1])).getDesc());
                    break;
                }

            }
        });

        // 1、chk msg

        // 2、if data, sync msg

        // 3、handle msg and send Msg To WeChat Friend
    }

    private int[] syncCheck(GlobalParam globalParam, String syncKey) {

        String url = WeChatApi.SYNC_CHK.getUrl()
                + "?r=" + System.currentTimeMillis()
                + "&skey=" + globalParam.getSkey()
                + "&sid=" + globalParam.getWxsid()
                + "&uin=" + globalParam.getWxuin()
                + "&deviceid=" + "e" + String.valueOf(new Random().nextLong()).substring(1, 16)
                + "&synckey=" + syncKey
                + "&_=" + System.currentTimeMillis();

        // window.synccheck={retcode:"1100",selector:"0"}
        String syncCheckRes = HttpUtil.doGet(url);

        JSONObject jsonRes = JSON.parseObject(syncCheckRes.substring(syncCheckRes.indexOf("{")));
        if (jsonRes.size() != 2) {
            throw new WeChatException(SYNC_CHK_EXCEPTION.getDesc() + jsonRes);
        }
        int[] syncRes = new int[2];
        syncRes[0] = jsonRes.getInteger("retcode");
        syncRes[1] = jsonRes.getInteger("selector");

        LOG.info("===>> 消息同步检查成功" + Arrays.toString(syncRes));

        return syncRes;
    }

    private String webwxSync(GlobalParam globalParam, String syncKey) {
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

        JSONObject jsonObject = JSON.parseObject(response);
        if (jsonObject != null) {
            String ret = jsonObject.getString("Ret");
            if (StringUtils.equals(ret, "0")) {
                syncKey = jsonObject.getJSONObject("SyncKey").toString();


            }
        }
        return null;
    }

    @Override
    public void sendMsgToWeChatFriend(String msg, String toWho, GlobalParam globalParam) {

        String url = WeChatApi.SND_MSG + "?lang=zh_CN&?pass_ticket=" + globalParam.getPass_ticket();

        JSONObject paramJson = new JSONObject();
        JSONObject MsgJson = new JSONObject();
        String id = RamdonIdUtil.getRandomIdWithUsrNam(user.getUserName());
        MsgJson.put("Type", 1);
        MsgJson.put("Content", msg);
        MsgJson.put("FromUserName", user.getUserName());
        MsgJson.put("ToUserName", toWho);
        MsgJson.put("LocalID", id);
        MsgJson.put("ClientMsgId", id);

        paramJson.put("BaseRequest", baseRequest);
        paramJson.put("Msg", MsgJson);

        String res = HttpUtil.doPost(url, paramJson);

        LOG.info("[sendMsgToWeChatFriend] " + res);
    }

    /**
     *
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
            globalParam.setSkey(getBaseReqRes.substring(getBaseReqRes.indexOf("<skey>") + "<skey>".length(), getBaseReqRes.indexOf("</skey>")));
            globalParam.setWxsid(getBaseReqRes.substring(getBaseReqRes.indexOf("<wxsid>") + "<wxsid>".length(), getBaseReqRes.indexOf("</wxsid>")));
            globalParam.setWxuin(getBaseReqRes.substring(getBaseReqRes.indexOf("<wxuin>") + "<wxuin>".length(), getBaseReqRes.indexOf("</wxuin>")));
            globalParam.setPass_ticket(getBaseReqRes.substring(getBaseReqRes.indexOf("<pass_ticket>") + "<pass_ticket>".length(), getBaseReqRes.indexOf("</pass_ticket>")));
        }

        if (globalParam != null) {
            this.baseRequest = new BaseRequest();
            baseRequest.setSkey(globalParam.getSkey());
            baseRequest.setSid(globalParam.getWxsid());
            baseRequest.setUin(globalParam.getWxuin());
            baseRequest.setDeviceID("e" + String.valueOf(new Random().nextLong()).substring(1, 16));
        } else {
            throw new WeChatException("全局参数为空");
        }

        return globalParam;
    }


}
