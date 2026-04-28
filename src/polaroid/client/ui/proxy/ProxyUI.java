package polaroid.client.ui.proxy;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.modules.impl.render.Theme;
import polaroid.client.utils.math.Vector4i;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.Polaroid;
import polaroid.client.utils.client.ClientUtil;
import polaroid.client.utils.client.IMinecraft;
import polaroid.client.utils.client.Vec2i;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.Scissor;
import polaroid.client.ui.mainscreen.TimerUtility;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import polaroid.client.utils.render.font.Fonts;
import polaroid.client.managment.Managment;
import polaroid.client.proxy.ProxyType;

import java.net.InetSocketAddress;
import java.util.*;

public class ProxyUI extends Screen implements IMinecraft {

    public ProxyUI() {
        super(new StringTextComponent(""));
    }

    public final TimerUtility timer = new TimerUtility();

    public final List<Proxy> proxies = new ArrayList<>();

    public float scroll;
    public float scrollAn;

    private String proxyInput = "";
    private String nameInput = "";
    private boolean typingProxy;
    private boolean typingName;
    private ProxyType selectedType = ProxyType.SOCKS5; // По умолчанию SOCKS5
    
    float minus = 14;
    float offset = 6f;
    float width = 300, height = 250; // Уменьшил высоту
    
    private Proxy selectedProxy = null;
    private Proxy currentProxy = null;
    private long lastClickTime = 0;
    private Proxy lastClickedProxy = null;

    private final List<Snowflake> snowflakes = new ArrayList<>();
    private final Random snowRandom = new Random();

    private static class Snowflake {
        float x, y, speed, size;
        Snowflake(float x, float y, float speed, float size) {
            this.x = x; this.y = y; this.speed = speed; this.size = size;
        }
    }

    @Override
    public void init(Minecraft minecraft, int w, int h) {
        super.init(minecraft, w, h);
        if (snowflakes.isEmpty()) {
            for (int i = 0; i < 100; i++) {
                snowflakes.add(new Snowflake(
                    snowRandom.nextFloat() * w, snowRandom.nextFloat() * h,
                    0.5f + snowRandom.nextFloat() * 1.5f, 2f + snowRandom.nextFloat() * 3f));
            }
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        mc.gameRenderer.setupOverlayRendering(2);
        int w = mc.getMainWindow().getScaledWidth();
        int h = mc.getMainWindow().getScaledHeight();
        fillGradient(matrixStack, 0, 0, w, h, Theme.getColor(0) | 0xFF000000, Theme.getColor(180) | 0xFF000000);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        for (Snowflake sf : snowflakes) {
            sf.y += sf.speed;
            if (sf.y > h) { sf.y = -10; sf.x = snowRandom.nextFloat() * w; }
            DisplayUtils.drawCircle(sf.x, sf.y, sf.size, ColorUtils.rgba(255, 255, 255, 200));
        }
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        scrollAn = MathUtil.lerp(scrollAn, scroll, 5);

        MainWindow mainWindow = mc.getMainWindow();
        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;

        int megaradiun = 12;
        DisplayUtils.drawRoundedRect(x, y, width, height + 3, new Vector4f((float)megaradiun, (float)megaradiun, (float)megaradiun, (float)megaradiun), ColorUtils.rgba(25,25,25,222));
        
        // Заголовок
        Fonts.otwindowsa.drawCenteredText(matrixStack, "Прокси", x + width / 2, y + offset, Theme.getColor(0), 10);

        // Верхняя панель с кнопками
        float buttonY = y + offset * 2 + 6;
        float buttonHeight = 18;
        float buttonSpacing = 4;
        
        // Кнопка ADD
        float addButtonWidth = Fonts.otwindowsa.getWidth("ADD", 9) + 10;
        float addButtonX = x + offset;
        DisplayUtils.drawRoundedRect(addButtonX, buttonY, addButtonWidth, buttonHeight, 3, ColorUtils.rgba(15, 15, 15, 180));
        Fonts.otwindowsa.drawCenteredText(matrixStack, "ADD", addButtonX + addButtonWidth / 2 - 2, buttonY + 5, -1, 9);
        
        // Кнопка удаления
        float deleteButtonWidth = 18;
        float deleteButtonX = addButtonX + addButtonWidth + buttonSpacing;
        DisplayUtils.drawRoundedRect(deleteButtonX, buttonY, deleteButtonWidth, buttonHeight, 3, ColorUtils.rgba(180, 30, 30, 180));
        Fonts.icons2.drawCenteredText(matrixStack, "Z", deleteButtonX + deleteButtonWidth / 2 - 2, buttonY + 4, -1, 10);
        
        // Кнопка избранного
        float favoriteButtonWidth = 18;
        float favoriteButtonX = deleteButtonX + deleteButtonWidth + buttonSpacing;
        DisplayUtils.drawRoundedRect(favoriteButtonX, buttonY, favoriteButtonWidth, buttonHeight, 3, ColorUtils.rgba(200, 180, 0, 180));
        Fonts.icons2.drawCenteredText(matrixStack, "U", favoriteButtonX + favoriteButtonWidth / 2 - 2, buttonY + 4, -1, 10);
        
        // Кнопка RESET (сброс прокси)
        float resetButtonWidth = Fonts.otwindowsa.getWidth("RESET", 9) + 10;
        float resetButtonX = favoriteButtonX + favoriteButtonWidth + buttonSpacing;
        DisplayUtils.drawRoundedRect(resetButtonX, buttonY, resetButtonWidth, buttonHeight, 3, ColorUtils.rgba(15, 15, 15, 180));
        Fonts.otwindowsa.drawCenteredText(matrixStack, "RESET", resetButtonX + resetButtonWidth / 2 - 2, buttonY + 5, -1, 9);

        // Список прокси
        float dick = 1;
        float listY = y + offset + 60f - minus * 2;
        float listHeight = 140f;
        
        DisplayUtils.drawRoundedRect(x + offset - dick, listY, width - offset * 2f + dick * 2, listHeight, 6, ColorUtils.rgba(15, 15, 15, 155));

        if (proxies.isEmpty()) {
            Fonts.otwindowsa.drawCenteredText(matrixStack, "Список прокси пуст...", x + width / 2f, listY + listHeight / 2, -1, 10);
        }
        
        float size = 0f, iter = scrollAn, offsetProxies = 0f;

        List<Proxy> sortedProxies = new ArrayList<>(proxies);
        sortedProxies.sort((p1, p2) -> {
            if (p1.favorite && !p2.favorite) return -1;
            if (!p1.favorite && p2.favorite) return 1;
            return 0;
        });

        Scissor.push();
        Scissor.setFromComponentCoordinates(x + offset, listY, width - offset * 2f, listHeight);
        for (Proxy proxy : sortedProxies) {
            float scrollY = listY + iter * 22f;
            
            int color;
            if (proxy == selectedProxy) {
                color = ColorUtils.rgba(60, 60, 60, 180);
            } else if (currentProxy != null && currentProxy.equals(proxy)) {
                color = ColorUtils.rgba(80, 80, 80, 145);
            } else {
                color = ColorUtils.rgba(30, 30, 30, 145);
            }

            int radius = 4;
            DisplayUtils.drawRoundedRect(x + offset + 2f, scrollY + 2 + offsetProxies, width - offset * 2f - 4f, 20f, new Vector4f((float)radius, (float)radius, (float)radius, (float)radius), new Vector4i(ColorUtils.setAlpha(Theme.getColor(0), 65), ColorUtils.setAlpha(Theme.getColor(0), 65), ColorUtils.setAlpha(Theme.getColor(0), 65), ColorUtils.setAlpha(Theme.getColor(0), 65)));
            if(proxy == selectedProxy || (currentProxy != null && currentProxy.equals(proxy))) {
                DisplayUtils.drawRoundedRect(x + offset + 2f, scrollY + 2 + offsetProxies, width - offset * 2f - 4f, 20f, new Vector4f((float)radius, (float)radius, (float)radius, (float)radius), new Vector4i(ColorUtils.setAlpha(Theme.getColor(0), 135), ColorUtils.setAlpha(Theme.getColor(0), 135), ColorUtils.setAlpha(Theme.getColor(0), 135), ColorUtils.setAlpha(Theme.getColor(0), 135)));
            }
            
            String displayText = proxy.name + " (" + proxy.type.name().toLowerCase() + ")";
            Fonts.otwindowsa.drawText(matrixStack, displayText, x + offset + 6f, scrollY + 7 + offsetProxies, -1, 8);

            if (proxy.favorite) {
                Fonts.icons2.drawText(matrixStack, "U", x + width - offset - 18f, scrollY + 6 + offsetProxies, ColorUtils.rgb(255, 215, 0), 10);
            }

            iter++;
            size++;
        }
        scroll = MathHelper.clamp(scroll, size > 6 ? -size + 3 : 0, 0);
        Scissor.unset();
        Scissor.pop();
        
        // Полоса прокрутки
        if (size > 6) {
            float scrollbarX = x + width - offset - 3;
            float scrollbarY = listY;
            float scrollbarWidth = 2;
            float scrollbarHeight = listHeight;
            float scrollbarThumbHeight = Math.max(20, scrollbarHeight * (6 / size));
            float scrollbarThumbY = scrollbarY - (scrollAn / size) * (scrollbarHeight - scrollbarThumbHeight);
            
            DisplayUtils.drawRoundedRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, 1, ColorUtils.rgba(40, 40, 40, 100));
            DisplayUtils.drawRoundedRect(scrollbarX, scrollbarThumbY, scrollbarWidth, scrollbarThumbHeight, 1, ColorUtils.setAlpha(Theme.getColor(0), 180));
        }

        // Поля ввода внизу
        float inputY = listY + listHeight + 5;
        
        // Поле имени
        DisplayUtils.drawRoundedRect(x + offset - 1, inputY, width - offset * 2 + 2, 20f, 3, ColorUtils.rgba(5, 5, 5, 80));
        Scissor.push();
        Scissor.setFromComponentCoordinates(x + offset, inputY, width - offset * 2f, 20f);
        String nameText = nameInput.isEmpty() && !typingName ? "Имя прокси..." : nameInput + (typingName ? "_" : "");
        Fonts.otwindowsa.drawText(matrixStack, nameText, x + offset + 5f, inputY + 5f, ColorUtils.rgba(255, 255, 255, 155), 9);
        Scissor.unset();
        Scissor.pop();
        
        // Поле адреса
        DisplayUtils.drawRoundedRect(x + offset - 1, inputY + 25, width - offset * 2 + 2, 20f, 3, ColorUtils.rgba(5, 5, 5, 80));
        Scissor.push();
        Scissor.setFromComponentCoordinates(x + offset, inputY + 25, width - offset * 2f, 20f);
        String proxyText = proxyInput.isEmpty() && !typingProxy ? "IP:PORT@LOGIN:PASS..." : proxyInput + (typingProxy ? "_" : "");
        Fonts.otwindowsa.drawText(matrixStack, proxyText, x + offset + 5f, inputY + 30f, ColorUtils.rgba(255, 255, 255, 155), 8);
        Scissor.unset();
        Scissor.pop();
        
        // Текущий прокси
        if (currentProxy != null) {
            String currentText = "Активный: " + currentProxy.name;
            float boxY = inputY + 50;
            float boxHeight = 20;
            
            DisplayUtils.drawRoundedRect(x + offset - 1, boxY, width - offset * 2 + 2, boxHeight, 3, ColorUtils.rgba(15, 15, 15, 200));
            
            float textWidth = mc.fontRenderer.getStringWidth(currentText);
            float textX = x + width / 2 - textWidth / 2f;
            float textY = boxY + (boxHeight - 8) / 2;
            
            mc.fontRenderer.drawStringWithShadow(matrixStack, currentText, textX, textY, 0xFFFFFF);
        }

        mc.gameRenderer.setupOverlayRendering();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (typingName && !nameInput.isEmpty()) {
                nameInput = nameInput.substring(0, nameInput.length() - 1);
            }
            if (typingProxy && !proxyInput.isEmpty()) {
                proxyInput = proxyInput.substring(0, proxyInput.length() - 1);
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            if (!nameInput.isEmpty() && !proxyInput.isEmpty()) {
                try {
                    String host, username = null, password = null;
                    int port;
                    
                    // Парсинг формата: HOST:PORT@LOGIN:PASSWORD или HOST:PORT
                    if (proxyInput.contains("@")) {
                        // Формат с авторизацией: HOST:PORT@LOGIN:PASSWORD
                        int atIndex = proxyInput.indexOf("@");
                        String addressPart = proxyInput.substring(0, atIndex);
                        String authPart = proxyInput.substring(atIndex + 1);
                        
                        // Парсим адрес (HOST:PORT)
                        int colonIndex = addressPart.lastIndexOf(":");
                        if (colonIndex > 0) {
                            host = addressPart.substring(0, colonIndex);
                            port = Integer.parseInt(addressPart.substring(colonIndex + 1));
                        } else {
                            System.err.println("[ProxyUI] Invalid address format: " + addressPart);
                            return super.keyPressed(keyCode, scanCode, modifiers);
                        }
                        
                        // Парсим авторизацию (LOGIN:PASSWORD)
                        int authColonIndex = authPart.lastIndexOf(":");
                        if (authColonIndex > 0) {
                            username = authPart.substring(0, authColonIndex);
                            password = authPart.substring(authColonIndex + 1);
                        } else {
                            System.err.println("[ProxyUI] Invalid auth format: " + authPart);
                            return super.keyPressed(keyCode, scanCode, modifiers);
                        }
                    } else {
                        // Обычный формат: HOST:PORT
                        int colonIndex = proxyInput.lastIndexOf(":");
                        if (colonIndex > 0) {
                            host = proxyInput.substring(0, colonIndex);
                            port = Integer.parseInt(proxyInput.substring(colonIndex + 1));
                        } else {
                            System.err.println("[ProxyUI] Invalid format: " + proxyInput);
                            return super.keyPressed(keyCode, scanCode, modifiers);
                        }
                    }
                    
                    boolean exists = proxies.stream().anyMatch(p -> p.name.equalsIgnoreCase(nameInput));
                    if (!exists) {
                        System.out.println("[ProxyUI] Adding proxy: " + nameInput);
                        System.out.println("[ProxyUI] Host: " + host + ", Port: " + port);
                        System.out.println("[ProxyUI] Username: " + (username != null ? username : "none"));
                        proxies.add(new Proxy(nameInput, selectedType, host, port, username, password, false));
                        ProxyConfig.updateFile();
                    }
                    typingProxy = false;
                    typingName = false;
                    proxyInput = "";
                    nameInput = "";
                } catch (Exception e) {
                    System.err.println("[ProxyUI] Error parsing proxy: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (typingProxy || typingName) {
                typingProxy = false;
                typingName = false;
                proxyInput = "";
                nameInput = "";
            }
        }

        if (typingProxy || typingName) {
            // Ctrl+V - вставка из буфера обмена
            if (ClientUtil.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_V) {
                try {
                    String clipboard = GLFW.glfwGetClipboardString(mc.getMainWindow().getHandle());
                    if (clipboard != null) {
                        if (typingName && nameInput.length() < 20) {
                            int remaining = 20 - nameInput.length();
                            nameInput += clipboard.substring(0, Math.min(clipboard.length(), remaining));
                        }
                        if (typingProxy) {
                            proxyInput += clipboard;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // Ctrl+Backspace - очистить поле
            if (ClientUtil.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (typingName) nameInput = "";
                if (typingProxy) proxyInput = "";
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (typingName && nameInput.length() < 20) {
            nameInput += Character.toString(codePoint);
        }
        if (typingProxy) {
            proxyInput += Character.toString(codePoint);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixed = MathUtil.getMouse2i((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;

        float buttonY = y + offset * 2 + 6;
        float buttonHeight = 18;
        float buttonSpacing = 4;
        
        // Кнопка ADD
        float addButtonWidth = Fonts.otwindowsa.getWidth("ADD", 9) + 10;
        float addButtonX = x + offset;
        if (button == 0 && DisplayUtils.isInRegion(mouseX, mouseY, addButtonX, buttonY, addButtonWidth, buttonHeight)) {
            System.out.println("[ProxyUI] ADD button clicked!");
            System.out.println("[ProxyUI] nameInput: '" + nameInput + "' (empty: " + nameInput.isEmpty() + ")");
            System.out.println("[ProxyUI] proxyInput: '" + proxyInput + "' (empty: " + proxyInput.isEmpty() + ")");
            
            if (!nameInput.isEmpty() && !proxyInput.isEmpty()) {
                try {
                    String host, username = null, password = null;
                    int port;
                    
                    // Парсинг формата: HOST:PORT@LOGIN:PASSWORD или HOST:PORT
                    if (proxyInput.contains("@")) {
                        // Формат с авторизацией: HOST:PORT@LOGIN:PASSWORD
                        int atIndex = proxyInput.indexOf("@");
                        String addressPart = proxyInput.substring(0, atIndex);
                        String authPart = proxyInput.substring(atIndex + 1);
                        
                        // Парсим адрес (HOST:PORT)
                        int colonIndex = addressPart.lastIndexOf(":");
                        if (colonIndex > 0) {
                            host = addressPart.substring(0, colonIndex);
                            port = Integer.parseInt(addressPart.substring(colonIndex + 1));
                        } else {
                            System.err.println("[ProxyUI] Invalid address format: " + addressPart);
                            return true;
                        }
                        
                        // Парсим авторизацию (LOGIN:PASSWORD)
                        int authColonIndex = authPart.lastIndexOf(":");
                        if (authColonIndex > 0) {
                            username = authPart.substring(0, authColonIndex);
                            password = authPart.substring(authColonIndex + 1);
                        } else {
                            System.err.println("[ProxyUI] Invalid auth format: " + authPart);
                            return true;
                        }
                    } else {
                        // Обычный формат: HOST:PORT
                        int colonIndex = proxyInput.lastIndexOf(":");
                        if (colonIndex > 0) {
                            host = proxyInput.substring(0, colonIndex);
                            port = Integer.parseInt(proxyInput.substring(colonIndex + 1));
                        } else {
                            System.err.println("[ProxyUI] Invalid format: " + proxyInput);
                            return true;
                        }
                    }
                    
                    boolean exists = proxies.stream().anyMatch(p -> p.name.equalsIgnoreCase(nameInput));
                    if (!exists) {
                        System.out.println("[ProxyUI] Adding proxy: " + nameInput);
                        System.out.println("[ProxyUI] Host: " + host + ", Port: " + port);
                        System.out.println("[ProxyUI] Username: " + (username != null ? username : "none"));
                        proxies.add(new Proxy(nameInput, selectedType, host, port, username, password, false));
                        ProxyConfig.updateFile();
                        proxyInput = "";
                        nameInput = "";
                        typingProxy = false;
                        typingName = false;
                        System.out.println("[ProxyUI] Proxy added successfully! Total proxies: " + proxies.size());
                    } else {
                        System.out.println("[ProxyUI] Proxy with name '" + nameInput + "' already exists!");
                    }
                } catch (Exception e) {
                    System.err.println("[ProxyUI] Error parsing proxy: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("[ProxyUI] Cannot add proxy - fields are empty!");
            }
            return true;
        }
        
        // Кнопка удаления
        float deleteButtonWidth = 18;
        float deleteButtonX = addButtonX + addButtonWidth + buttonSpacing;
        if (button == 0 && DisplayUtils.isInRegion(mouseX, mouseY, deleteButtonX, buttonY, deleteButtonWidth, buttonHeight)) {
            if (selectedProxy != null) {
                if (currentProxy == selectedProxy) {
                    currentProxy = null;
                    Managment.PROXY_CONN.reset();
                }
                proxies.remove(selectedProxy);
                selectedProxy = null;
                ProxyConfig.updateFile();
            }
            return true;
        }
        
        // Кнопка избранного
        float favoriteButtonWidth = 18;
        float favoriteButtonX = deleteButtonX + deleteButtonWidth + buttonSpacing;
        if (button == 0 && DisplayUtils.isInRegion(mouseX, mouseY, favoriteButtonX, buttonY, favoriteButtonWidth, buttonHeight)) {
            if (selectedProxy != null) {
                selectedProxy.favorite = !selectedProxy.favorite;
                ProxyConfig.updateFile();
            }
            return true;
        }
        
        // Кнопка RESET
        float resetButtonWidth = Fonts.otwindowsa.getWidth("RESET", 9) + 10;
        float resetButtonX = favoriteButtonX + favoriteButtonWidth + buttonSpacing;
        if (button == 0 && DisplayUtils.isInRegion(mouseX, mouseY, resetButtonX, buttonY, resetButtonWidth, buttonHeight)) {
            currentProxy = null;
            Managment.PROXY_CONN.reset();
            return true;
        }

        // Клик по полям ввода
        float inputY = y + offset + 60f - minus * 2 + 140f + 5;
        
        if (button == 0 && DisplayUtils.isInRegion(mouseX, mouseY, x + offset - 1, inputY, width - offset * 2 + 2, 20f)) {
            typingName = true;
            typingProxy = false;
            return true;
        }
        
        if (button == 0 && DisplayUtils.isInRegion(mouseX, mouseY, x + offset - 1, inputY + 25, width - offset * 2 + 2, 20f)) {
            typingProxy = true;
            typingName = false;
            return true;
        }

        // Клик по прокси в списке
        float listY = y + offset + 60f - minus * 2;
        float iter = scrollAn;
        
        List<Proxy> sortedProxies = new ArrayList<>(proxies);
        sortedProxies.sort((p1, p2) -> {
            if (p1.favorite && !p2.favorite) return -1;
            if (!p1.favorite && p2.favorite) return 1;
            return 0;
        });
        
        for (Proxy proxy : sortedProxies) {
            float scrollY = listY + iter * 22f;

            if (DisplayUtils.isInRegion(mouseX, mouseY, x + offset + 2f, scrollY + 2, width - offset * 2f - 4f, 20f)) {
                if (button == 0) {
                    long currentTime = System.currentTimeMillis();
                    
                    if (lastClickedProxy == proxy && (currentTime - lastClickTime) < 300) {
                        if (currentProxy == proxy) {
                            currentProxy = null;
                            Managment.PROXY_CONN.reset();
                            System.out.println("[ProxyUI] Proxy deactivated");
                        } else {
                            currentProxy = proxy;
                            System.out.println("[ProxyUI] Activating proxy: " + proxy.name);
                            System.out.println("[ProxyUI] Address: " + proxy.host + ":" + proxy.port);
                            System.out.println("[ProxyUI] Type: " + proxy.type);
                            System.out.println("[ProxyUI] Username: " + (proxy.username != null ? proxy.username : "none"));
                            System.out.println("[ProxyUI] Password: " + (proxy.password != null ? "***" : "none"));
                            Managment.PROXY_CONN.setup(proxy.type, new InetSocketAddress(proxy.host, proxy.port), proxy.username, proxy.password);
                        }
                        lastClickedProxy = null;
                        lastClickTime = 0;
                    } else {
                        selectedProxy = proxy;
                        lastClickedProxy = proxy;
                        lastClickTime = currentTime;
                    }
                }
                return true;
            }

            iter++;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Vec2i fixed = MathUtil.getMouse2i((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;
        float listY = y + offset + 60f - minus * 2;
        float listHeight = 140f;

        if (DisplayUtils.isInRegion(mouseX, mouseY, x + offset, listY, width - offset * 2f, listHeight)) {
            scroll += delta * 1;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void tick() {
        super.tick();
    }
}


