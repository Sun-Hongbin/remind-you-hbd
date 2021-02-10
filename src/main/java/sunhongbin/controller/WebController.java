package sunhongbin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sunhongbin.service.LoginAndOutService;
import sunhongbin.service.WeChatService;

/**
 * created by SunHongbin on 2020/4/27
 */

//@RestController 不能用这个，否则不能返回图片只能返回JSON
@Controller
@RequestMapping("/ai")
public class WebController {

    @Autowired
    private LoginAndOutService loginAndOutService;

    @Autowired
    private WeChatService weChatService;

    private volatile static WebController instance = null;

    @GetMapping(value = "/login")
//    @RecordRequestLog(source = RequestSourceEnum.FRONT_END)
    public String doAiMan() throws Exception {
        loginAndOutService.doLogin();
        return "index";
    }

    @GetMapping(value = "/logOut")
    @ResponseBody
    public String doLogOut() {
        loginAndOutService.doLogOut();
        return "退出登陆";
    }

}
