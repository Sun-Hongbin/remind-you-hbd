package sunhongbin.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.*;

/**
 * created by SunHongbin on 2020/4/28
 */
public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static File translateInputStreamToFile(InputStream inputStream, String fileName) {

        File file = new File(fileName);

        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];

            int length = 0;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException ioException) {
            logger.error(ioException.getLocalizedMessage(), ioException);
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
                logger.error(ioException.getLocalizedMessage(), ioException);
            }
            return file;
        }
    }

    /**
     * spring boot 获取static文件夹路径
     * @return
     */
    public static String getImageFilePath(String fileName)  {

        String path = "";
        try {
            path = ResourceUtils.getURL("src/main/resources/static/image").getPath();

            if (!StringUtils.isEmpty(fileName)) {
                path = path.concat("/" + fileName);
            }

            path = path.substring(1, path.length());


        } catch (FileNotFoundException fileNotFoundException) {
            logger.error(fileNotFoundException.getLocalizedMessage(), fileNotFoundException);
        }
        return path;
    }



}
