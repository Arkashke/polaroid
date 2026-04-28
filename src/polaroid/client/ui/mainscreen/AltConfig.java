package polaroid.client.ui.mainscreen;

import com.google.gson.*;
import polaroid.client.Polaroid;
import polaroid.client.utils.client.IMinecraft;
import net.minecraft.util.Session;

import java.io.*;
import java.util.UUID;

public class AltConfig implements IMinecraft {

    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final File file = new File(mc.gameDir, "\\polaroid\\files\\alts.cfg");

    public void init() throws Exception {
        if (!file.exists()) {
            file.createNewFile();
        } else {
            readAlts();
        }
    }

    public static void updateFile() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("last", mc.session.getUsername());

        JsonArray altsArray = new JsonArray();
        for (polaroid.client.ui.mainscreen.Alt alt : Polaroid.getInstance().getAltScreen().alts) {
            JsonObject altObject = new JsonObject();
            altObject.addProperty("name", alt.name);
            altObject.addProperty("favorite", alt.favorite);
            altsArray.add(altObject);
        }

        jsonObject.add("alts", altsArray);

        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.println(gson.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readAlts() throws FileNotFoundException {
        JsonElement jsonElement = new JsonParser().parse(new BufferedReader(new FileReader(file)));

        if (jsonElement.isJsonNull()) return;

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        if (jsonObject.has("last")) {
            mc.session = new Session(jsonObject.get("last").getAsString(), UUID.randomUUID().toString(), "", "mojang");
        }

        if (jsonObject.has("alts")) {
            for (JsonElement element : jsonObject.get("alts").getAsJsonArray()) {
                if (element.isJsonObject()) {
                    JsonObject altObject = element.getAsJsonObject();
                    String name = altObject.get("name").getAsString();
                    boolean favorite = altObject.has("favorite") ? altObject.get("favorite").getAsBoolean() : false;
                    Polaroid.getInstance().getAltScreen().alts.add(new polaroid.client.ui.mainscreen.Alt(name, favorite));
                } else {
                    // Обратная совместимость со старым форматом
                    String name = element.getAsString();
                    Polaroid.getInstance().getAltScreen().alts.add(new polaroid.client.ui.mainscreen.Alt(name));
                }
            }
        }
    }
}


