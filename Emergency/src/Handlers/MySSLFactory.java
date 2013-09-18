package Handlers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class MySSLFactory implements SocketFactory, LayeredSocketFactory {

	private SSLContext sslcontext = null;

	private static SSLContext createMySSLContext() throws IOException {
		try{
			//set instance of SSL context
			SSLContext context = SSLContext.getInstance("TLS");
			//this is where the magic happens. We use our Custom TrustManager to accept all certificates
			context.init(null, new TrustManager[] { new MyTrustManager(null) }, null);
			return context;
		}catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}
	
	private SSLContext getSSLContext() throws IOException {
		if (this.sslcontext == null) {
			//return the context we made before
			this.sslcontext = createMySSLContext(); //this is where the magic happens pt2.
		}
		return this.sslcontext;
	}
	//overwite
	public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort, HttpParams params) 
															throws IOException, UnknownHostException, ConnectTimeoutException {
		//set parameters for connection
		int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
		int soTimeout = HttpConnectionParams.getSoTimeout(params);
		InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
		SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket());
		
		if((localAddress != null) || (localPort > 0)) {
			// Explicit bind
			if(localPort < 0) {
				//that means any port
				localPort = 0;
			}
			//bind the port
			InetSocketAddress isa = new InetSocketAddress(localAddress, localPort);
			sslsock.bind(isa);
		}
		//try connection
		sslsock.connect(remoteAddress, connTimeout);
		sslsock.setSoTimeout(soTimeout);
		return sslsock;
	}
	//overwite
	public Socket createSocket() throws IOException {
		return getSSLContext().getSocketFactory().createSocket();
	}
	//overwite to Accept all Servers(true) 
	public boolean isSecure(Socket socket) throws IllegalArgumentException {
		return true;
	}

	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,  UnknownHostException {
		return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
	}
    
	//we have to overwrite ".equals()" and ".hashCode()" so our Custom Factory works
	public boolean equals(Object obj){
		return ((obj != null) && obj.getClass().equals(MySSLFactory.class));
	}
    
	public int hashCode(){
		return MySSLFactory.class.hashCode();
	}
}
