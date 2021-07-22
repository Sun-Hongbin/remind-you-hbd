package sunhongbin.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sunhongbin.exception.WeChatException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * created by SunHongbin on 2020/4/28
 */
public class HttpUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpUtil.class);

    private static String cookie = "";

    public static String doGet(String url) {

        StringBuilder result = new StringBuilder();

        BufferedReader bufferedReader = null;

        InputStream inputStream = null;

        HttpURLConnection conn = null;

        try {

            URL xUrl = new URL(url);

            conn = (HttpURLConnection) xUrl.openConnection();

            // 设置请求方式
            conn.setRequestMethod("GET");

            // 设置连接主机服务器的超时时间
            conn.setConnectTimeout(2000);

            // 由于要一直监听是否二维码有被扫描。因此这里不设置读取远程返回的数据时间
//            conn.setReadTimeout(2000);

            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            if (!StringUtils.isEmpty(cookie)) {
                conn.setRequestProperty("Cookie", cookie);
            }

            // 发送请求
            conn.connect();

            // 获取所有响应头字段
            Map<String, List<String>> map = conn.getHeaderFields();

            // 遍历所有的响应头字段
//            for (String key : map.keySet()) {
//                LOG.info("doGet ===>> key: " + key + ", value: " + map.get(key));
//            }

            // 如果有cookie的话解析并保存
            List<String> list = map.get("Set-Cookie");
            if (list != null && list.size() != 0) {
                StringBuilder stringBuilder = new StringBuilder();
                for (String cookie : list) {
                    stringBuilder.append(cookie, 0, cookie.indexOf(";") + 1);
                }
                HttpUtil.cookie = stringBuilder.toString();
                LOG.info("cookie: " + cookie);
            }

            // 通过connection连接，获取输入流
            if (conn.getResponseCode() == 200) {

                inputStream = conn.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }
            }
        } catch (Exception e) {
            throw new WeChatException(e.getMessage());
        } finally {
            assert conn != null;
            conn.disconnect();
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }

        }
        return result.toString();
    }

    public static File doGetFile(String url) {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        InputStream inputStream;

        File file = null;

        try {

            URIBuilder uriBuilder = new URIBuilder(url);

            URI uri = uriBuilder.build();

            HttpGet httpGet = new HttpGet(uri);

            response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {

                // get entity
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();
                file = FileUtil.translateInputStreamToFile(inputStream, "qrCode");
            }
        } catch (Exception e) {
            throw new WeChatException(e.getMessage());
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException ioException) {
                LOG.error(ioException.getMessage());
            }
        }
        return file;
    }

    /**
     * @param url
     * @param jsonObject "DeviceID" -> "e878871051103203"
     *                   "Skey" -> "@crypt_1af1bef4_ce420e20a91faba907fd24b254d414a0"
     *                   "Uin" -> "1469893904"
     *                   "Sid" -> "O0Uy9JLwXSVEFXY4"
     * @return
     */
    public static String doPost(String url, JSONObject jsonObject) {

        try {
            URL xUrl = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) xUrl.openConnection();

            connection.setRequestMethod("POST");

            connection.setRequestProperty("Cookie", cookie);
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);

            connection.connect();

            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

            outputStream.write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));

            outputStream.flush();

            outputStream.close();

            return new String(FileUtil.translateInputStreamToByte(connection.getInputStream()), StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new WeChatException(e.getMessage());
        }
    }


}
