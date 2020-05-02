package sunhongbin.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sunhongbin.service.SmartRobotService;
import sunhongbin.service.WeChatService;
import sunhongbin.service.impl.WeChatServiceImpl;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by SunHongbin on 2020/4/27
 */

//@RestController 不能用这个，否则不能返回图片只能返回JSON
@Controller
public class WebController {

    @Autowired
    private WeChatService weChatService;

    @Autowired
    private SmartRobotService smartRobotService;

    private final Logger logger = LoggerFactory.getLogger(WeChatServiceImpl.class);

    /**
     * 第一次请求登陆微信时，就将该值锁住，直到有注销微信的指令到来
     */
    private AtomicBoolean locked = new AtomicBoolean(false);

    private volatile static WebController instance = null;

    private WebController() {
    }

    /**
     * 单例模式
     *
     * @return
     */
    public static WebController getInstance() {
        if (instance == null) {
            synchronized (WebController.class) {
                if (instance == null) {
                    instance = new WebController();
                }
            }
        }
        return instance;
    }

    @GetMapping(value = "/login")
    public String doAiMan() {

        // 第一次登陆时，将 false 置为 true，在没注销之前再次请求 UUID 都不会成功
        if (!locked.compareAndSet(false, true)) {
            logger.error("不可重复登陆微信！");
            return "index";
        }

        String uuid = weChatService.getUUID();

        // release lock and remind user retry login operation
        if (StringUtils.isEmpty(uuid)) {
            locked = new AtomicBoolean(false);
            logger.error("获取UUID失败，请重试登陆……");
            return "index";
        }
        logger.info("获取得到UUID成功，UUID：" + uuid);

        weChatService.showQRCode(uuid);

        try {
            while (!StringUtils.equals(weChatService.pollForScanRes(uuid), "200")){
                Thread.sleep(2000);
        }
        } catch (InterruptedException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        weChatService.login();

        weChatService.initializeweChat();

        weChatService.loadContactPerson();

        return "index";
    }

    @GetMapping(value = "logOut")
    @ResponseBody
    public String doLogOut() {

        locked = new AtomicBoolean(false);
        return "退出登陆";
    }


}
