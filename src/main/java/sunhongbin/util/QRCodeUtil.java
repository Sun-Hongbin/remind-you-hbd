package sunhongbin.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import sunhongbin.exception.WeChatException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
/**
 * Created by Administrator on 2018/11/8 0008.
 * 二维码、条形码工具类
 */
public class QRCodeUtil {

    /**
     * 调用微信API获取到的登陆二维码信息，先转换成输入流后再转换成File
     * @param qrCodeFile 为inputStrean转化的File类型对象
     * @param hintMap 谷歌二维码工具 ZXING 的属性配置
     * @return
     */
    public static String translateFileToQrContent(File qrCodeFile, Map hintMap) {
        try {
            // 读取文件
            BufferedImage bufferedImage = ImageIO.read(qrCodeFile);

            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);

            Binarizer binarizer = new HybridBinarizer(source);

            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);

            MultiFormatReader reader = new MultiFormatReader();

            Result result = reader.decode(binaryBitmap, hintMap);

            return result.getText();
        } catch (Exception e) {
            throw new WeChatException("translateFileToQrContent: " + e.getMessage());
        }
    }

}