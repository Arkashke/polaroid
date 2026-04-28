package polaroid.client.ui.proxy;

import polaroid.client.proxy.ProxyType;

public class Proxy {
    public String name;
    public ProxyType type;
    public String host;
    public int port;
    public String username; // Для авторизации
    public String password; // Для авторизации
    public boolean favorite = false;

    public Proxy(String name, ProxyType type, String host, int port) {
        this(name, type, host, port, null, null, false);
    }

    public Proxy(String name, ProxyType type, String host, int port, boolean favorite) {
        this(name, type, host, port, null, null, favorite);
    }
    
    public Proxy(String name, ProxyType type, String host, int port, String username, String password, boolean favorite) {
        this.name = name;
        this.type = type;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.favorite = favorite;
    }

    @Override
    public String toString() {
        return name + " (" + type.name().toLowerCase() + "://" + host + ":" + port + ")";
    }

    public String getFullAddress() {
        return type.name().toLowerCase() + "://" + host + ":" + port;
    }
}


