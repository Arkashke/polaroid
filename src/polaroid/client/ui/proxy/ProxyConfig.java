package polaroid.client.ui.proxy;

import com.google.gson.*;
import polaroid.client.Polaroid;
import polaroid.client.proxy.ProxyType;
import polaroid.client.utils.client.IMinecraft;

import java.io.*;

public class ProxyConfig implements IMinecraft {

    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final File file = new File(mc.gameDir, "\\polaroid\\files\\proxies.cfg");

    public void init() throws Exception {
        if (!file.exists()) {
            file.createNewFile();
        } else {
            readProxies();
        }
    }

    public static void updateFile() {
        JsonObject jsonObject = new JsonObject();

        JsonArray proxiesArray = new JsonArray();
        for (Proxy proxy : Polaroid.getInstance().getProxyScreen().proxies) {
            JsonObject proxyObject = new JsonObject();
            proxyObject.addProperty("name", proxy.name);
            proxyObject.addProperty("type", proxy.type.name());
            proxyObject.addProperty("host", proxy.host);
            proxyObject.addProperty("port", proxy.port);
            if (proxy.username != null && !proxy.username.isEmpty()) {
                proxyObject.addProperty("username", proxy.username);
            }
            if (proxy.password != null && !proxy.password.isEmpty()) {
                proxyObject.addProperty("password", proxy.password);
            }
            proxyObject.addProperty("favorite", proxy.favorite);
            proxiesArray.add(proxyObject);
        }

        jsonObject.add("proxies", proxiesArray);

        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.println(gson.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readProxies() throws FileNotFoundException {
        JsonElement jsonElement = new JsonParser().parse(new BufferedReader(new FileReader(file)));

        if (jsonElement.isJsonNull()) return;

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        if (jsonObject.has("proxies")) {
            for (JsonElement element : jsonObject.get("proxies").getAsJsonArray()) {
                if (element.isJsonObject()) {
                    JsonObject proxyObject = element.getAsJsonObject();
                    String name = proxyObject.get("name").getAsString();
                    ProxyType type = ProxyType.valueOf(proxyObject.get("type").getAsString());
                    String host = proxyObject.get("host").getAsString();
                    int port = proxyObject.get("port").getAsInt();
                    String username = proxyObject.has("username") ? proxyObject.get("username").getAsString() : null;
                    String password = proxyObject.has("password") ? proxyObject.get("password").getAsString() : null;
                    boolean favorite = proxyObject.has("favorite") ? proxyObject.get("favorite").getAsBoolean() : false;
                    Polaroid.getInstance().getProxyScreen().proxies.add(new Proxy(name, type, host, port, username, password, favorite));
                }
            }
        }
    }
}


