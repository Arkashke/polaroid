package polaroid.client;

import com.google.common.eventbus.EventBus;
import polaroid.client.command.*;
import polaroid.client.command.friends.FriendStorage;
import polaroid.client.command.impl.*;
import polaroid.client.command.impl.feature.*;
import polaroid.client.command.staffs.StaffStorage;
import polaroid.client.config.ConfigStorage;
import polaroid.client.events.EventKey;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleRegistry;
import polaroid.client.scripts.client.ScriptManager;
import polaroid.client.ui.ab.factory.ItemFactory;
import polaroid.client.ui.ab.factory.ItemFactoryImpl;
import polaroid.client.ui.ab.logic.ActivationLogic;
import polaroid.client.ui.ab.model.IItem;
import polaroid.client.ui.ab.model.ItemStorage;
import polaroid.client.ui.autobuy.AutoBuyConfig;
import polaroid.client.ui.autobuy.AutoBuyHandler;
import polaroid.client.ui.clickgui.Window;
import polaroid.client.ui.mainscreen.AltConfig;
import polaroid.client.ui.mainscreen.AltScreen;
import polaroid.client.ui.proxy.ProxyUI;
import polaroid.client.ui.proxy.ProxyConfig;
import polaroid.client.ui.styles.Style;
import polaroid.client.ui.styles.StyleFactory;
import polaroid.client.ui.styles.StyleFactoryImpl;
import polaroid.client.ui.styles.StyleManager;
import polaroid.client.utils.TPSCalc;
import polaroid.client.utils.client.ServerTPS;
import polaroid.client.utils.drag.DragManager;
import polaroid.client.utils.drag.Dragging;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import via.ViaMCP;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Polaroid {

    public static UserData userData;
    public boolean playerOnServer = false;
    public static final String CLIENT_NAME = "polaroid";

    @Getter
    private static Polaroid instance;

    private ModuleRegistry functionRegistry;
    private ConfigStorage configStorage;
    private CommandDispatcher commandDispatcher;
    private ServerTPS serverTPS;
    private MacroManager macroManager;
    private StyleManager styleManager;

    // Менеджер событий и скриптов
    private final EventBus eventBus = new EventBus();
    private final ScriptManager scriptManager = new ScriptManager();

    // Директории
    private final File clientDir = new File(Minecraft.getInstance().gameDir + "\\polaroid");
    private final File filesDir = new File(Minecraft.getInstance().gameDir + "\\polaroid\\files");

    // Элементы интерфейса
    private AltScreen altScreen;
    private AltConfig altConfig;
    private ProxyUI proxyScreen;
    private ProxyConfig proxyConfig;
    private polaroid.client.ui.clickgui.Window clickGuiWindow;
    private polaroid.client.ui.ab.render.Window autoBuyUI;

    // Конфигурация и обработчики
    private AutoBuyConfig autoBuyConfig = new AutoBuyConfig();
    private AutoBuyHandler autoBuyHandler;
    private ViaMCP viaMCP;
    private TPSCalc tpsCalc;
    private ActivationLogic activationLogic;
    private ItemStorage itemStorage;

    public Polaroid() {
        instance = this;

        if (!clientDir.exists()) {
            clientDir.mkdirs();
        }
        if (!filesDir.exists()) {
            filesDir.mkdirs();
        }

        clientLoad();
        FriendStorage.load();
        StaffStorage.load();
    }



    public Dragging createDrag(Module module, String name, float x, float y) {
        DragManager.draggables.put(name, new Dragging(module, name, x, y));
        return DragManager.draggables.get(name);
    }

    private void clientLoad() {
        // УСТАНАВЛИВАЕМ GuiScale на 2 и FOV на максимум по умолчанию
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameSettings != null) {
            mc.gameSettings.guiScale = 2;
            mc.gameSettings.fov = 110.0;
            mc.getMainWindow().setGuiScale(2);
            mc.gameSettings.saveOptions();
        }
        
        viaMCP = new ViaMCP();
        serverTPS = new ServerTPS();
        functionRegistry = new ModuleRegistry();
        macroManager = new MacroManager();
        initStyles(); // Инициализируем стили ПЕРЕД configStorage
        configStorage = new ConfigStorage();
        functionRegistry.init();
        initCommands();
        altScreen = new AltScreen();
        altConfig = new AltConfig();
        proxyScreen = new ProxyUI();
        proxyConfig = new ProxyConfig();
        tpsCalc = new TPSCalc();
        userData = new UserData("paster", 1);

        try {
            autoBuyConfig.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            altConfig.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            proxyConfig.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            configStorage.init();
        } catch (IOException e) {
            System.out.println("Ошибка при подгрузке конфига.");
        }
        try {
            macroManager.init();
        } catch (IOException e) {
            System.out.println("Ошибка при подгрузке конфига макросов.");
        }
        DragManager.load();
        clickGuiWindow = new polaroid.client.ui.clickgui.Window(new StringTextComponent(""));
        initAutoBuy();
        autoBuyUI = new polaroid.client.ui.ab.render.Window(new StringTextComponent(""), itemStorage);
        //autoBuyUI = new AutoBuyUI(new StringTextComponent("A"));
        autoBuyHandler = new AutoBuyHandler();
        autoBuyConfig = new AutoBuyConfig();

        eventBus.register(this);
    }

    private final EventKey eventKey = new EventKey(-1);

    public void onKeyPressed(int key) {
        if (functionRegistry.getSelfDestruct().unhooked) return;
        eventKey.setKey(key);
        eventBus.post(eventKey);

        macroManager.onKeyPressed(key);

        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            Minecraft.getInstance().displayGuiScreen(clickGuiWindow);
        }



    }

    private void initAutoBuy() {
        ItemFactory itemFactory = new ItemFactoryImpl();
        CopyOnWriteArrayList<IItem> items = new CopyOnWriteArrayList<>();
        itemStorage = new ItemStorage(items, itemFactory);

        activationLogic = new ActivationLogic(itemStorage, eventBus);
    }
    public String randomNickname() {
        java.util.Random random = new java.util.Random();
        
        // Выбираем случайный формат генерации (0-9)
        int format = random.nextInt(10);
        String nickname = "";
        
        switch (format) {
            case 0: // Полностью рандомные буквы и цифры
                int length = 7 + random.nextInt(6); // 7-12 символов
                for (int i = 0; i < length; i++) {
                    if (random.nextInt(4) == 0) { // 25% шанс цифры
                        nickname += random.nextInt(10);
                    } else {
                        char c = (char) ('a' + random.nextInt(26));
                        if (random.nextBoolean()) c = Character.toUpperCase(c);
                        nickname += c;
                    }
                }
                break;
                
            case 1: // Рандомные буквы с подчеркиваниями
                int len1 = 3 + random.nextInt(4); // 3-6
                for (int i = 0; i < len1; i++) {
                    char c = (char) ('a' + random.nextInt(26));
                    if (i == 0 || random.nextInt(3) == 0) c = Character.toUpperCase(c);
                    nickname += c;
                }
                nickname += "_";
                int len2 = 3 + random.nextInt(4); // 3-6
                for (int i = 0; i < len2; i++) {
                    char c = (char) ('a' + random.nextInt(26));
                    if (i == 0 || random.nextInt(3) == 0) c = Character.toUpperCase(c);
                    nickname += c;
                }
                break;
                
            case 2: // Буквы вперемешку с цифрами
                int len = 8 + random.nextInt(5); // 8-12
                for (int i = 0; i < len; i++) {
                    if (i % 2 == 0 && random.nextBoolean()) {
                        nickname += random.nextInt(10);
                    } else {
                        char c = (char) ('a' + random.nextInt(26));
                        if (random.nextInt(3) == 0) c = Character.toUpperCase(c);
                        nickname += c;
                    }
                }
                break;
                
            case 3: // Рандом с двойными подчеркиваниями
                int p1 = 2 + random.nextInt(3); // 2-4
                for (int i = 0; i < p1; i++) {
                    char c = (char) ('a' + random.nextInt(26));
                    if (i == 0) c = Character.toUpperCase(c);
                    nickname += c;
                }
                nickname += "__";
                int p2 = 3 + random.nextInt(4); // 3-6
                for (int i = 0; i < p2; i++) {
                    char c = (char) ('a' + random.nextInt(26));
                    nickname += c;
                }
                nickname += random.nextInt(100);
                break;
                
            case 4: // Чередование заглавных и строчных
                int len4 = 7 + random.nextInt(5); // 7-11
                for (int i = 0; i < len4; i++) {
                    char c = (char) ('a' + random.nextInt(26));
                    if (i % 2 == 0) c = Character.toUpperCase(c);
                    nickname += c;
                }
                break;
                
            case 5: // Рандом с цифрами в середине
                int l1 = 3 + random.nextInt(3); // 3-5
                for (int i = 0; i < l1; i++) {
                    char c = (char) ('a' + random.nextInt(26));
                    if (i == 0) c = Character.toUpperCase(c);
                    nickname += c;
                }
                nickname += random.nextInt(10) + "" + random.nextInt(10);
                int l2 = 3 + random.nextInt(3); // 3-5
                for (int i = 0; i < l2; i++) {
                    char c = (char) ('a' + random.nextInt(26));
                    nickname += c;
                }
                break;
                
            case 6: // Полный хаос с подчеркиваниями
                int parts = 2 + random.nextInt(2); // 2-3 части
                for (int p = 0; p < parts; p++) {
                    if (p > 0) nickname += "_";
                    int partLen = 2 + random.nextInt(4); // 2-5
                    for (int i = 0; i < partLen; i++) {
                        if (random.nextInt(5) == 0) {
                            nickname += random.nextInt(10);
                        } else {
                            char c = (char) ('a' + random.nextInt(26));
                            if (random.nextInt(2) == 0) c = Character.toUpperCase(c);
                            nickname += c;
                        }
                    }
                }
                break;
                
            case 7: // Рандом с x, z, q (редкие буквы)
                String rare = "xzqXZQ";
                int len7 = 7 + random.nextInt(5); // 7-11
                for (int i = 0; i < len7; i++) {
                    if (random.nextInt(3) == 0) {
                        nickname += rare.charAt(random.nextInt(rare.length()));
                    } else {
                        char c = (char) ('a' + random.nextInt(26));
                        if (random.nextInt(4) == 0) c = Character.toUpperCase(c);
                        nickname += c;
                    }
                }
                break;
                
            case 8: // Цифры в начале и конце
                nickname += random.nextInt(10);
                int len8 = 5 + random.nextInt(4); // 5-8
                for (int i = 0; i < len8; i++) {
                    char c = (char) ('a' + random.nextInt(26));
                    if (random.nextInt(3) == 0) c = Character.toUpperCase(c);
                    nickname += c;
                }
                nickname += random.nextInt(100);
                break;
                
            case 9: // Максимальный хаос
                int len9 = 8 + random.nextInt(5); // 8-12
                for (int i = 0; i < len9; i++) {
                    int type = random.nextInt(10);
                    if (type < 3) { // 30% цифры
                        nickname += random.nextInt(10);
                    } else if (type < 4 && nickname.length() > 0) { // 10% подчеркивание
                        nickname += "_";
                    } else { // 60% буквы
                        char c = (char) ('a' + random.nextInt(26));
                        if (random.nextInt(2) == 0) c = Character.toUpperCase(c);
                        nickname += c;
                    }
                }
                break;
        }
        
        // Убираем двойные подчеркивания в конце
        while (nickname.endsWith("_")) {
            nickname = nickname.substring(0, nickname.length() - 1);
        }
        
        // Обрезаем до 16 символов если нужно
        if (nickname.length() > 16) {
            nickname = nickname.substring(0, 16);
        }
        
        // Проверяем минимальную длину 3 символа
        while (nickname.length() < 3) {
            nickname += (char) ('a' + random.nextInt(26));
        }
        
        return nickname;
    }
    
    private void initCommands() {
        Minecraft mc = Minecraft.getInstance();
        Logger logger = new MultiLogger(List.of(new ConsoleLogger(), new MinecraftLogger()));
        List<Command> commands = new ArrayList<>();
        Prefix prefix = new PrefixImpl();
        commands.add(new ListCommand(commands, logger));
        commands.add(new FriendCommand(prefix, logger, mc));
        commands.add(new BindCommand(prefix, logger));
        commands.add(new UnbindCommand(prefix, logger));
        commands.add(new GPSCommand(prefix, logger));
        commands.add(new ConfigCommand(configStorage, prefix, logger));
        commands.add(new MacroCommand(macroManager, prefix, logger));
        commands.add(new VClipCommand(prefix, logger, mc));
        commands.add(new HClipCommand(prefix, logger, mc));
        commands.add(new StaffCommand(prefix, logger));
        commands.add(new MemoryCommand(logger));
        commands.add(new RCTCommand(logger, mc));
        commands.add(new polaroid.client.commands.impl.PerformanceCommand(logger));

        AdviceCommandFactory adviceCommandFactory = new AdviceCommandFactoryImpl(logger);
        ParametersFactory parametersFactory = new ParametersFactoryImpl();

        commandDispatcher = new StandaloneCommandDispatcher(commands, adviceCommandFactory, prefix, parametersFactory, logger);
    }

    private void initStyles() {
        StyleFactory styleFactory = new StyleFactoryImpl();
        List<Style> styles = new ArrayList<>();

        styles.add(styleFactory.createStyle("Морской", new Color(0xE7F5DC), new Color(0xCFE1B9)));
        styles.add(styleFactory.createStyle("Малиновый", new Color(0xB6C99B), new Color(0x98A77C)));
        styles.add(styleFactory.createStyle("Черничный", new Color(0x88976C), new Color(0x728156)));
        styles.add(styleFactory.createStyle("Необычный", new Color(0xE7F5DC), new Color(0x98A77C)));
        styles.add(styleFactory.createStyle("Огненный", new Color(0xCFE1B9), new Color(0x88976C)));
        styles.add(styleFactory.createStyle("Металлический", new Color(0xB6C99B), new Color(0x728156)));
        styles.add(styleFactory.createStyle("Прикольный", new Color(0xE7F5DC), new Color(0xB6C99B)));
        styles.add(styleFactory.createStyle("Новогодний", new Color(0x98A77C), new Color(0x728156)));

     /*   styles.add(styleFactory.createStyle("Mojito", "#1D976C", "#1D976C"));
        styles.add(styleFactory.createStyle("Rose Water", "#E55D87", "#5FC3E4"));
        styles.add(styleFactory.createStyle("Anamnisar", "#9796f0", "#fbc7d4"));
        styles.add(styleFactory.createStyle("Ultra Voilet", "#654ea3", "#eaafc8"));
        styles.add(styleFactory.createStyle("Quepal", "#11998e", "#38ef7d"));
        styles.add(styleFactory.createStyle("Intergalactic", "#5cb8f", "#c657f9"));
        styles.add(styleFactory.createStyle("Blush", "#B24592", "#F15F79"));
        styles.add(styleFactory.createStyle("Back to the Future", "#C02425", "#F0CB35"));
        styles.add(styleFactory.createStyle("Green and Blue", "#52f1ab", "#42acf5"));
        styles.add(styleFactory.createStyle("Sin City Red", "#ED213A", "#93291E"));
        styles.add(styleFactory.createStyle("Evening Night", "#005AA7", "#FFFDE4"));
        styles.add(styleFactory.createStyle("Compare Now", "#EF3B36", "#FFFFFF"));
        styles.add(styleFactory.createStyle("Netflix", "#8E0E00", "#1F1C18"));
        styles.add(styleFactory.createStyle("Passion", "#e53935", "#e35d5b"));*/

        styleManager = new StyleManager(styles, styles.get(0));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserData {
        final String user;
        final int uid;
    }

}


