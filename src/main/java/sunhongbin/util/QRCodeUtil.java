package sunhongbin.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by Administrator on 2018/11/8 0008.
 * 二维码、条形码工具类
 */
public class QRCodeUtil {

    /**
     * 调用微信API获取到的登陆二维码，为inputStrean转化的File类型对象
     * @param qrCodeFile
     * @param hintMap 谷歌二维码工具 ZXING 的属性配置
     * @return
     */
    public static String translateFileToQrContent(File qrCodeFile, Map hintMap) throws IOException, NotFoundException {

        // 读取文件
        BufferedImage bufferedImage = ImageIO.read(qrCodeFile);

        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);

        Binarizer binarizer = new HybridBinarizer(source);

        BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);

        MultiFormatReader reader = new MultiFormatReader();

        Result result = reader.decode(binaryBitmap, hintMap);

        return result.getText();
    }


}