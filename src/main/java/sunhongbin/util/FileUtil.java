package sunhongbin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
