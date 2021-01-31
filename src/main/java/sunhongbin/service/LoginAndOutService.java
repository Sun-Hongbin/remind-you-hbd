package sunhongbin.service;

/**
 * created by SunHongbin on 2020/9/10
 */
public interface LoginAndOutService {

    String doLogin() throws Exception;

    void doLogOut();

    /**
     * 初始化
     * @return
     */
    String initializeWeChatInfo();

}
