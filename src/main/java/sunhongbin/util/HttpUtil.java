package sunhongbin.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * created by SunHongbin on 2020/4/28
 */
public class HttpUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpUtil.class);

    private static StringBuilder sessions = new StringBuilder();

    public static String deGet(String url, Map<String, String> paramsMap) {

        // 1、Create Http Client Object and Response Object
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String requestResult = "";

        try {
            // 2、build URL and parameters to an URI
            URIBuilder uriBuilder = new URIBuilder(url);
            if (paramsMap != null) {
                for (String paramKey : paramsMap.keySet()) {
                    uriBuilder.addParameter(paramKey, paramsMap.get(paramKey));
                }
            }
            URI uri = uriBuilder.build();

            // 3、Get method
            HttpGet httpGet = new HttpGet(uri);

            response = httpClient.execute(httpGet);

            // 4、analysis whether the HTTP return code is 200
            if (response.getStatusLine().getStatusCode() == 200) {
                LOG.info("Get method success");
                // parse message
                // response.getEntity() output content: [Content-Type: text/javascript,Content-Length: 64,Chunked: false]
                requestResult = EntityUtils.toString(response.getEntity(), "UTF-8");
            } else {
                LOG.error("Get method failed");
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException ioException) {
                LOG.error(ioException.getLocalizedMessage(), ioException);
            }
        }

        return requestResult;
    }

    public static String doGetGlobalParams(String uri) {

        try {
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", sessions.toString());
            connection.setRequestProperty("Connection", "keep-alive");
            connection.connect();

            List<String> list = connection.getHeaderFields().get("Set-Cookie");
            if (list != null && list.size() != 0) {
                for (String cookie : list) {
                    sessions.append(cookie, 0, cookie.indexOf(";") + 1);
                }
            }
            if (connection.getResponseCode() == 200) {
                // 将InputStreamReader转化成byte[]
                return new String(FileUtil.translateInputStreamToByte(connection.getInputStream()), StandardCharsets.UTF_8);
            }
        } catch (IOException ioException) {
            LOG.error(ioException.getLocalizedMessage(), ioException);
        }
        return "";
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
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException ioException) {
                LOG.error(ioException.getLocalizedMessage(), ioException);
            }
        }
        return file;
    }

    public static String doPost(String url, Map<String, String> paramsMap) {
        // 1、Create Http Client Object and Response Object
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String requestResult = "";

        try {
            // 2、create post
            HttpPost httpPost = new HttpPost(url);

            // 3、create params List
            if (paramsMap != null) {
                List<NameValuePair> paramsList = new ArrayList<>();
                for (String paramKey : paramsMap.keySet()) {
                    paramsList.add(new BasicNameValuePair(paramKey, paramsMap.get(paramKey)));
                }
                // 4、simulate from entity
                UrlEncodedFormEntity encodedFormEntity = new UrlEncodedFormEntity(paramsList, "UTF-8");
                httpPost.setEntity(encodedFormEntity);
            }

            // 5、execute post request
            response = httpClient.execute(httpPost);

            requestResult = EntityUtils.toString(response.getEntity(), "UTF-8");

        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException ioException) {
                LOG.error(ioException.getLocalizedMessage(), ioException);
            }
        }
        return requestResult;
    }

    /**
     *
     * @param url
     * @param jsonObject
     * "DeviceID" -> "e878871051103203"
     * "Skey" -> "@crypt_1af1bef4_ce420e20a91faba907fd24b254d414a0"
     * "Uin" -> "1469893904"
     * "Sid" -> "O0Uy9JLwXSVEFXY4"
     * @return
     */
    public static String doPost(String url, JSONObject jsonObject) throws IOException {

            URL urlEntity = new URL(url);

            HttpURLConnection connection = (HttpURLConnection)urlEntity.openConnection();

            connection.setRequestMethod("POST");

            connection.setRequestProperty("Cookie", sessions.toString());
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type","application/json;charset=UTF-8");

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);

            connection.connect();

            DataOutputStream outputStream =new DataOutputStream(connection.getOutputStream());

            /**
             * {"BaseRequest":{"DeviceID":"e878871051103203","Skey":"@crypt_1af1bef4_ce420e20a91faba907fd24b254d414a0","Uin":"1469893904","Sid":"O0Uy9JLwXSVEFXY4"}}
             */
            outputStream.write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));

            outputStream.flush();

            outputStream.close();

            return new String(FileUtil.translateInputStreamToByte(connection.getInputStream()), StandardCharsets.UTF_8);
    }


}
