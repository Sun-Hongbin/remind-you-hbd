package sunhongbin.util;

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
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.util.UriBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * created by SunHongbin on 2020/4/28
 */
public class UriRequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(UriRequestUtil.class);

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
                logger.info("Get method success");
                // parse message
                // response.getEntity() output content: [Content-Type: text/javascript,Content-Length: 64,Chunked: false]
                requestResult = EntityUtils.toString(response.getEntity(), "UTF-8");
            } else {
                logger.error("Get method failed");
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException ioException) {
                logger.error(ioException.getLocalizedMessage(), ioException);
            }
        }

        return requestResult;
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
            logger.error(e.getLocalizedMessage(), e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException ioException) {
                logger.error(ioException.getLocalizedMessage(), ioException);
            }
        }
        return requestResult;
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
            logger.error(e.getLocalizedMessage(), e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException ioException) {
                logger.error(ioException.getLocalizedMessage(), ioException);
            }
        }
        return file;
    }

    public static String doGetGlobalParams(String uri) {

        URL url = null;

        StringBuilder session = new StringBuilder();

        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return "";
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", session.toString());
            connection.setRequestProperty("Connection", "keep-alive");
            connection.connect();

            List<String> list = connection.getHeaderFields().get("Set-Cookie");
            if (list != null && list.size() != 0) {
                for (String cookie : list) {
                    session.append(cookie.substring(0, cookie.indexOf(";") + 1));
                }
            }
            if (connection.getResponseCode() == 200) {
                // 将InputStreamReader转化成byte[]
                return new String(FileUtil.translateInputStreamToByte(connection.getInputStream()), "UTF-8");
            }
        } catch (IOException ioException) {
            logger.error(ioException.getLocalizedMessage(), ioException);
        }
        return "";
    }


}
