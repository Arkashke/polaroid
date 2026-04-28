package polaroid.client.proxy;

import io.netty.channel.Channel;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import lombok.Getter;
import java.net.InetSocketAddress;

public class ProxyConnection {
    @Getter 
    private ProxyType proxyType = ProxyType.DIRECT;
    
    @Getter 
    private InetSocketAddress proxyAddr = null;
    
    private String username = null;
    private String password = null;

    public void setup(ProxyType proxyType, InetSocketAddress proxyAddr) {
        this.setup(proxyType, proxyAddr, null, null);
    }
    
    public void setup(ProxyType proxyType, InetSocketAddress proxyAddr, String username, String password) {
        this.proxyType = proxyType;
        this.proxyAddr = proxyAddr;
        this.username = username;
        this.password = password;
    }

    public void reset() {
        proxyType = ProxyType.DIRECT;
        proxyAddr = null;
        username = null;
        password = null;
    }
    
    public void applyProxy(Channel channel) {
        if (proxyAddr == null || proxyType == ProxyType.DIRECT) {
            System.out.println("[Proxy] No proxy configured or DIRECT mode");
            return;
        }
        
        try {
            System.out.println("[Proxy] Applying proxy: " + proxyType + " at " + proxyAddr);
            System.out.println("[Proxy] Auth: " + (username != null ? "YES (user: " + username + ")" : "NO"));
            
            switch (proxyType) {
                case SOCKS4:
                    System.out.println("[Proxy] Using SOCKS4" + (username != null ? " with username" : ""));
                    if (username != null && !username.isEmpty()) {
                        Socks4ProxyHandler handler = new Socks4ProxyHandler(proxyAddr, username);
                        handler.setConnectTimeoutMillis(30000);
                        channel.pipeline().addFirst("proxy", handler);
                    } else {
                        Socks4ProxyHandler handler = new Socks4ProxyHandler(proxyAddr);
                        handler.setConnectTimeoutMillis(30000);
                        channel.pipeline().addFirst("proxy", handler);
                    }
                    break;
                case SOCKS5:
                    System.out.println("[Proxy] Using SOCKS5" + (username != null ? " with authentication" : ""));
                    if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                        Socks5ProxyHandler handler = new Socks5ProxyHandler(proxyAddr, username, password);
                        handler.setConnectTimeoutMillis(30000);
                        channel.pipeline().addFirst("proxy", handler);
                    } else {
                        Socks5ProxyHandler handler = new Socks5ProxyHandler(proxyAddr);
                        handler.setConnectTimeoutMillis(30000);
                        channel.pipeline().addFirst("proxy", handler);
                    }
                    break;
                case HTTP:
                    System.out.println("[Proxy] Using HTTP" + (username != null ? " with authentication" : ""));
                    if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                        HttpProxyHandler handler = new HttpProxyHandler(proxyAddr, username, password);
                        handler.setConnectTimeoutMillis(30000);
                        channel.pipeline().addFirst("proxy", handler);
                    } else {
                        HttpProxyHandler handler = new HttpProxyHandler(proxyAddr);
                        handler.setConnectTimeoutMillis(30000);
                        channel.pipeline().addFirst("proxy", handler);
                    }
                    break;
                default:
                    System.out.println("[Proxy] Unknown proxy type: " + proxyType);
                    break;
            }
            System.out.println("[Proxy] Successfully added proxy handler to pipeline");
        } catch (Exception e) {
            System.err.println("[Proxy] Failed to apply proxy: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


