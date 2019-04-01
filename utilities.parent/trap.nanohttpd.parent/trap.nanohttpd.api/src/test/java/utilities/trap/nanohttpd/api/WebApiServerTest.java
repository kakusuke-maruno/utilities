package utilities.trap.nanohttpd.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.research.trap.nhttpd.Request;
import com.ericsson.research.trap.nhttpd.Response;
import com.ericsson.research.trap.nhttpd.StatusCodes;

public class WebApiServerTest {
	static {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
		System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
		System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd HH:mm:ss:SSS Z");
	}
	private static Logger LOG = LoggerFactory.getLogger(WebApiServerTest.class);
	private static WebApiServer webApiServer;

	@BeforeClass
	public static void setUp() throws Exception {
		webApiServer = WebApiServer.builder().port(8080).bind("/hogehoge/", new WebApiAdapter() {

			@Override
			public void execute(Request request, Response response) throws WebApiException {
				response.setStatus(StatusCodes.OK).setData("Good!");
			}
		}).build();
		LOG.info("started");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		webApiServer.stop();
		LOG.info("stopped");
	}

	@Test
	public void test_001() {
		CloseableHttpResponse response2 = null;
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost("http://localhost:8080/hogehoge/");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("username", "vip"));
			nvps.add(new BasicNameValuePair("password", "secret"));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			response2 = httpclient.execute(httpPost);

			LOG.info("{}", response2.getStatusLine());
			HttpEntity entity2 = response2.getEntity();
			EntityUtils.consume(entity2);
		} catch (UnsupportedEncodingException e) {
			LOG.error("", e);
		} catch (ClientProtocolException e) {
			LOG.error("", e);
		} catch (IOException e) {
			LOG.error("", e);
		} finally {
			try {
				response2.close();
			} catch (IOException ignore) {
			}
		}
	}

	@Test
	public void test_002() {
		CloseableHttpResponse response2 = null;
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet("http://localhost:8080/mekemeke/");
			response2 = httpclient.execute(httpGet);

			LOG.info("{}", response2.getStatusLine());
			HttpEntity entity2 = response2.getEntity();
			EntityUtils.consume(entity2);
		} catch (UnsupportedEncodingException e) {
			LOG.error("", e);
		} catch (ClientProtocolException e) {
			LOG.error("", e);
		} catch (IOException e) {
			LOG.error("", e);
		} finally {
			try {
				response2.close();
			} catch (IOException ignore) {
			}
		}
	}

	@Test
	public void test_003() {
		CloseableHttpResponse response2 = null;
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet("http://localhost:8080/shutdown/");
			response2 = httpclient.execute(httpGet);

			LOG.info("{}", response2.getStatusLine());
			HttpEntity entity2 = response2.getEntity();
			EntityUtils.consume(entity2);
		} catch (UnsupportedEncodingException e) {
			LOG.error("", e);
		} catch (ClientProtocolException e) {
			LOG.error("", e);
		} catch (IOException e) {
			LOG.error("", e);
		} finally {
			try {
				response2.close();
			} catch (IOException ignore) {
			}
		}
	}
}
