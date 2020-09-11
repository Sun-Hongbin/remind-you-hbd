package sunhongbin.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * created by SunHongbin on 2020/4/28
 */
public class StringOperationUtil {

    /**
     * .（点） 匹配任何单个字符。例如正则表达式r.t匹配这些字符内串：rat、rut、r t，但是不匹配root。
     * + 匹配1或多个正好在它之前的那个字符。例如正则表达式9+匹配9、99、999等。注意：这个元字容符不是所有的软件都支持的。
     *
     * \d  是匹配一个数字(0到9)
     * \\d 前面多了第一个\ 是为了在程序中转义第二个\，这个你可以忽略
     * + 表示 1个或多个
     * 组合起来
     * \\d+ 就表示多个数字，形如 12、44、6763……
     * \\. 匹配一个小数点
     * \\d+\\.\\d+ 就表示小数，形如12.334、0.12、87.343……
     * | 表示或者，一个竖线内就够了
     * () 括号在这里表示分组，实际不匹配任何字符，此处不要括号也可以
     *
     *
     * "\\s+" 正则表达式
     *  正则表达式中\s匹配任何空白字符，包括空格、制表符、换页符等等, 等价于[ \f\n\r\t\v]
     *
     * \f -> 匹配一个换页
     * \n -> 匹配一个换行符
     * \r -> 匹配一个回车符
     * \t -> 匹配一个制表符
     * \v -> 匹配一个垂直制表符
     * 而“\s+”则表示匹配任意多个上面的字符。另因为反斜杠在Java里是转义字符，所以在Java里，我们要这么用“\\s+”.
     *
     * 比如，当碰到想用空格来分割字符串时，就可以这样写：
     * String[] s = str.split("\\s+")
     *
     * 总结
     * \\d+||(\\d+\\.\\d+)  多个连续的容数字或者 多个连续的数+小数点+多个连续的数
     * (\\d+) 匹配多位数字，如200
     *
     * 从文本中提取匹配符中的信息
     * @param inputText
     * @param regex
     * @return
     */
    public static String match(String inputText, String regex) {

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(inputText);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}
