package sunhongbin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sunhongbin.annotation.RecordRequestLog;
import sunhongbin.entity.GlobalParam;
import sunhongbin.enums.RequestSourceEnum;
import sunhongbin.exception.WeChatException;
import sunhongbin.service.WeChatService;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by SunHongbin on 2020/4/27
 */

//@RestController 不能用这个，否则不能返回图片只能返回JSON
@Controller
@RequestMapping("/ai")
public class WebController {
    /**
     * 第一次请求登陆微信时，就将该值锁住，直到有注销微信的指令到来
     */
    private AtomicBoolean locked = new AtomicBoolean(false);

    @Autowired
    private WeChatService weChatService;

    @GetMapping(value = "/execute")
    @RecordRequestLog(source = RequestSourceEnum.FRONT_END)
    public String execute() throws Exception {

        String uuid = weChatService.getUUID();

        weChatService.showQRCode(uuid);

        GlobalParam globalParam = weChatService.pollForScanRes(uuid);

        // 登录成功后就把登录标志置为true。第一次登陆时，将 false 置为 true，在没注销之前再次请求 UUID 都不会成功
        if (!locked.compareAndSet(false, true)) {
            throw new WeChatException("不可重复登陆微信！");
        }

        weChatService.initializeWeChat(globalParam);

        weChatService.wxStatusNotify(globalParam);

        weChatService.loadContactPerson(globalParam);

        weChatService.listeningInMsg(globalParam);

        return "index";
    }

    @GetMapping(value = "/logOut")
    @ResponseBody
    public String doLogOut() {

        locked = new AtomicBoolean(false);

        return "退出登陆";
    }

}
