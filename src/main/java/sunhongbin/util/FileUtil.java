package sunhongbin.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import sunhongbin.exception.WeChatException;

import java.io.*;

/**
 * created by SunHongbin on 2020/4/28
 */
public class FileUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    public static File translateInputStreamToFile(InputStream inputStream, String fileName) {

        File file = new File(fileName);

        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];

            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException ioException) {
            LOG.error(ioException.getLocalizedMessage(), ioException);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException ioException) {
                LOG.error(ioException.getLocalizedMessage(), ioException);
            }
        }
        return file;
    }

    public static byte[] translateInputStreamToByte(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];

        try {
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException ioException) {
            LOG.error(ioException.getLocalizedMessage(), ioException);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ioException) {
                LOG.error(ioException.getLocalizedMessage(), ioException);
            }
        }
        return outputStream.toByteArray();
    }

    /**
     * spring boot 获取static文件夹路径
     *
     * @return D:/MyCode/remind-you-hbd/src/main/resources/static/image/qrCode.png
     */
    public static String getImageFilePath(String fileName) {

        try {
            // 帮你补全image文件夹所在的完整路径【D:/MyCode/remind-you-hbd/src/main/resources/static/image】
            String path = ResourceUtils.getURL("src/main/resources/static/image").getPath();

            // 判断文件夹是否不存在，不存在则创建文件夹
            File directory = new File(path);
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new WeChatException("创建文件夹失败，创建路径为：" + path);
                }
            }

            String filePath = path.concat(fileName).substring(1);

            LOG.info("二维码生成路径为：" + filePath);

            return filePath;

        } catch (Exception e) {
            throw new WeChatException(e.getMessage());
        }
    }


}
