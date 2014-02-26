package aegle.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class CustomHttpClient {
	public static final int HTTP_TIMEOUT = 30 * 1000; // Timeout(milliseconds)
	private static HttpClient httpClient;
	private static ClientConnectionManager clientConnectionManager;
	private static HttpContext context;
	private static HttpParams params;
	
	private static HttpClient getHttpClient() {
		 if (httpClient == null) {
			 //initialize and setup the HttpClient
			 setup();
			 httpClient = new DefaultHttpClient(clientConnectionManager, params);
			 //getting default parameters, and setting timeouts to: i)connection ii)socket iii)client
			 final HttpParams params2 = httpClient.getParams();
			 //set timeout parameters
			 HttpConnectionParams.setConnectionTimeout(params2, HTTP_TIMEOUT);
			 HttpConnectionParams.setSoTimeout(params2, HTTP_TIMEOUT);
			 ConnManagerParams.setTimeout(params2, HTTP_TIMEOUT);
		 }
		 return httpClient;
	 }
	   
	private static void setup(){
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		// http scheme
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		// https scheme
		schemeRegistry.register(new Scheme("https", new MySSLFactory(), 443));
		//set parameters(1 connection and enable handshake)
		params = new BasicHttpParams();
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 1);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(1));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		//protocol parameters
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf8");
		
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		//set the user credentials for our site "example.com"
		credentialsProvider.setCredentials(new AuthScope("aegle.com", AuthScope.ANY_PORT),new UsernamePasswordCredentials("", ""));
		clientConnectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
		context = new BasicHttpContext();
		context.setAttribute("http.auth.credentials-provider", credentialsProvider);
	}
	 /**
     * HTTP POST
     */
	 public static JSONObject executeHttpPost(String url, ArrayList<NameValuePair> postParameters) throws Exception {
		 BufferedReader in = null;
		 HttpEntity he=null;
		 JSONObject jo=null;
		 try {
			 HttpClient client = getHttpClient();
			 //executing post and setting encoding to UTF-8
			 HttpPost request = new HttpPost(url);
			 UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters,"UTF-8");
			 request.setEntity(formEntity);
			 //getting response from remote server as an Entity, then converting it to JSONObject
			 HttpResponse response = client.execute(request);
			 he = response.getEntity();
			 jo = new JSONObject(EntityUtils.toString(he));
			 return jo;
		 } finally {
			 if (in != null) {
				 try {
					 in.close();//close the connection
				 } catch (IOException e) {
					 e.printStackTrace();
				 }
			 }
		 }
	 }
}
