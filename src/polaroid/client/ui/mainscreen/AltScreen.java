package polaroid.client.ui.mainscreen;

import net.minecraft.client.MouseHelper;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.modules.impl.render.Theme;
import polaroid.client.utils.math.Vector4i;
import polaroid.client.utils.render.DisplayUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import polaroid.client.Polaroid;
import polaroid.client.ui.mainscreen.AltConfig;
import polaroid.client.utils.client.ClientUtil;
import polaroid.client.utils.client.IMinecraft;
import polaroid.client.utils.client.Vec2i;
import polaroid.client.utils.math.MathUtil;

import polaroid.client.utils.player.MouseUtil;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.Scissor;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import polaroid.client.utils.render.font.Fonts;

import java.util.*;
import java.util.List;

public class AltScreen extends Screen implements IMinecraft {

    public AltScreen() {
        super(new StringTextComponent(""));
    }

    public final TimerUtility timer = new TimerUtility();

    public final List<polaroid.client.ui.mainscreen.Alt> alts = new ArrayList<>();

    public float scroll;
    public float scrollAn;

    private String altName = "";
    private boolean typing;
    float minus = 14;
    float offset = 6f;
    float width = 250, height = 220;
    
    private polaroid.client.ui.mainscreen.Alt selectedAlt = null;
    private polaroid.client.ui.mainscreen.Alt currentAlt = null;
    private long lastClickTime = 0;
    private polaroid.client.ui.mainscreen.Alt lastClickedAlt = null;

    private final List<Snowflake> snowflakes = new ArrayList<>();
    private final Random snowRandom = new Random();
    private float entryProgress = 0f;
    private boolean wasHoveredAdd, wasHoveredDelete, wasHoveredFavorite, wasHoveredGenerate;

    private static class Snowflake {
        float x, y, speed, size;
        Snowflake(float x, float y, float speed, float size) {
            this.x = x; this.y = y; this.speed = speed; this.size = size;
        }
    }

    @Override
    public void init(Minecraft minecraft, int w, int h) {
        super.init(minecraft, w, h);
        entryProgress = 0f;
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
        int darkBg = 0xFF1a1b26;
        int darkBottom = 0xFF14151e;
        fill(matrixStack, 0, 0, w, h, darkBg);
        fillGradient(matrixStack, 0, h / 2, w, h, darkBg, darkBottom);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        for (Snowflake sf : snowflakes) {
            sf.y += sf.speed;
            if (sf.y > h) { sf.y = -10; sf.x = snowRandom.nextFloat() * w; }
            DisplayUtils.drawCircle(sf.x, sf.y, sf.size, ColorUtils.rgba(255, 255, 255, 90));
        }
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();

        entryProgress = Math.min(1f, entryProgress + 0.08f);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        scrollAn = MathUtil.lerp(scrollAn, scroll, 5);

        MainWindow mainWindow = mc.getMainWindow();
        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;


        // Квадрат фона
        float bgX = x - offset, bgY = y - offset, bgWidth = width + offset * 2, bgHeight = height + offset * 2;
        int bgRectColor = ColorUtils.rgba(40, 40, 40, 160);
       // RectUtility.getInstance().drawRoundedRectShadowed(matrixStack, bgX, bgY, bgX + bgWidth, bgY + bgHeight, 8, 5, bgRectColor, bgRectColor, bgRectColor, bgRectColor, false, false, true, true);

        int megaradiun = 12;
        int megaradiun1 = 10;
        int panelAlpha = (int)(200 * entryProgress);
        DisplayUtils.drawRoundedRect(x, y, width, height + 3, new Vector4f((float)megaradiun, (float)megaradiun, (float)megaradiun, (float)megaradiun), ColorUtils.rgba(35, 35, 45, panelAlpha));
        // alt screen name (поднята выше)
        Fonts.otwindowsa.drawCenteredText(matrixStack, "Аккаунты", x + width / 2, y + offset, Theme.getColor(0), 10);

        // Верхняя панель с кнопками (подняты еще выше)
        float buttonY = y + offset * 2 + 6;
        float buttonHeight = 18;
        float buttonSpacing = 4;
        
        // Кнопка ADD
        float addButtonWidth = Fonts.otwindowsa.getWidth("ADD", 9) + 10;
        float addButtonX = x + offset;
        boolean hoverAdd = DisplayUtils.isInRegion((double)mouseX, (double)mouseY, addButtonX, buttonY, addButtonWidth, buttonHeight);
        if (hoverAdd && !wasHoveredAdd) mc.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 0.28F, 1.0F));
        wasHoveredAdd = hoverAdd;
        DisplayUtils.drawRoundedRect(addButtonX, buttonY, addButtonWidth, buttonHeight, 3, ColorUtils.rgba(15, 15, 15, 180));
        Fonts.otwindowsa.drawCenteredText(matrixStack, "ADD", addButtonX + addButtonWidth / 2 - 2, buttonY + 5, -1, 9);

        // Кнопка удаления (мусорка)
        float deleteButtonWidth = 18;
        float deleteButtonX = addButtonX + addButtonWidth + buttonSpacing;
        boolean hoverDel = DisplayUtils.isInRegion((double)mouseX, (double)mouseY, deleteButtonX, buttonY, deleteButtonWidth, buttonHeight);
        if (hoverDel && !wasHoveredDelete) mc.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 0.28F, 1.0F));
        wasHoveredDelete = hoverDel;
        DisplayUtils.drawRoundedRect(deleteButtonX, buttonY, deleteButtonWidth, buttonHeight, 3, ColorUtils.rgba(180, 30, 30, 180));
        Fonts.icons2.drawCenteredText(matrixStack, "Z", deleteButtonX + deleteButtonWidth / 2 - 2, buttonY + 4, -1, 10);

        // Кнопка избранного
        float favoriteButtonWidth = 18;
        float favoriteButtonX = deleteButtonX + deleteButtonWidth + buttonSpacing;
        boolean hoverFav = DisplayUtils.isInRegion((double)mouseX, (double)mouseY, favoriteButtonX, buttonY, favoriteButtonWidth, buttonHeight);
        if (hoverFav && !wasHoveredFavorite) mc.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 0.28F, 1.0F));
        wasHoveredFavorite = hoverFav;
        DisplayUtils.drawRoundedRect(favoriteButtonX, buttonY, favoriteButtonWidth, buttonHeight, 3, ColorUtils.rgba(200, 180, 0, 180));
        Fonts.icons2.drawCenteredText(matrixStack, "U", favoriteButtonX + favoriteButtonWidth / 2 - 2, buttonY + 4, -1, 10);

        // Кнопка GENERATE
        float generateButtonWidth = Fonts.otwindowsa.getWidth("GENERATE", 9) + 10;
        float generateButtonX = favoriteButtonX + favoriteButtonWidth + buttonSpacing;
        boolean hoverGen = DisplayUtils.isInRegion((double)mouseX, (double)mouseY, generateButtonX, buttonY, generateButtonWidth, buttonHeight);
        if (hoverGen && !wasHoveredGenerate) mc.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 0.28F, 1.0F));
        wasHoveredGenerate = hoverGen;
        DisplayUtils.drawRoundedRect(generateButtonX, buttonY, generateButtonWidth, buttonHeight, 3, ColorUtils.rgba(15, 15, 15, 180));
        Fonts.otwindowsa.drawCenteredText(matrixStack, "GENERATE", generateButtonX + generateButtonWidth / 2 - 2, buttonY + 5, -1, 9);


        DisplayUtils.drawRoundedRect(x + offset - 1, y + offset + 64 - minus * 2.5f + 177 - offset * 2, width + 2 - offset * 2f, 20f, 3, ColorUtils.rgba(5, 5, 5, 80));

        Scissor.push();
        Scissor.setFromComponentCoordinates(x + offset, y + offset + 64 - minus * 2.5f + 177 - offset * 2, width - offset * 2f, 20f);
        Fonts.otwindowsa.drawText(matrixStack, typing ? (altName + (typing ? "_" : "")) : "Write......", x + offset + 5f,
                y + offset + 69f - minus * 2.5f + 177 - offset * 2, ColorUtils.rgba(255, 255, 255, 155), 9);
        Scissor.unset();
        Scissor.pop();

        // Вывод никнеймов
        float dick = 1;

        DisplayUtils.drawRoundedRect(x + offset - dick, y + offset + 60f - minus * 2, width - offset * 2f + dick * 2, 177.5f - minus * 2, 6, ColorUtils.rgba(20, 20, 25, 130));

        // Надпись при пустом листе аккаунтов
        if (alts.isEmpty()) Fonts.otwindowsa.drawCenteredText(matrixStack, "Пустовато как-то...", x + width / 2f, (float) (y + offset + 60f - minus * 2.5 + (177.5f - minus) / 2), -1, 10);
        float size = 0f, iter = scrollAn, offsetAccounts = 0f;

        boolean hovered = false;

        // Сортируем аккаунты: избранные сверху
        List<polaroid.client.ui.mainscreen.Alt> sortedAlts = new ArrayList<>(alts);
        sortedAlts.sort((a1, a2) -> {
            if (a1.favorite && !a2.favorite) return -1;
            if (!a1.favorite && a2.favorite) return 1;
            return 0;
        });

        Scissor.push();
        Scissor.setFromComponentCoordinates(x + offset, y + offset + 60f - minus * 2, width - offset * 2f, 177.5f - minus * 2);
        for (polaroid.client.ui.mainscreen.Alt alt : sortedAlts) {
            float scrollY = y + iter * 22f;
            
            // Определяем цвет фона
            int color;
            if (alt == selectedAlt) {
                // Выделенный аккаунт
                color = ColorUtils.rgba(60, 60, 60, 180);
            } else if (mc.session.getUsername().equals(alt.name)) {
                // Текущий аккаунт в игре
                color = ColorUtils.rgba(80, 80, 80, 145);
            } else {
                // Обычный аккаунт
                color = ColorUtils.rgba(30, 30, 30, 145);
            }

            int radius = 4;
            DisplayUtils.drawRoundedRect(x + offset + 2f, scrollY + offset + 62 + offsetAccounts - minus * 2, width - offset * 2f - 4f, 20f, new Vector4f((float)radius, (float)radius, (float)radius, (float)radius), new Vector4i(ColorUtils.setAlpha(Theme.getColor(0), 65), ColorUtils.setAlpha(Theme.getColor(0), 65), ColorUtils.setAlpha(Theme.getColor(0), 65), ColorUtils.setAlpha(Theme.getColor(0), 65)));
            if(alt == selectedAlt || mc.session.getUsername().equals(alt.name)) {
                DisplayUtils.drawRoundedRect(x + offset + 2f, scrollY + offset + 62 + offsetAccounts - minus * 2, width - offset * 2f - 4f, 20f, new Vector4f((float)radius, (float)radius, (float)radius, (float)radius), new Vector4i(ColorUtils.setAlpha(Theme.getColor(0), 135), ColorUtils.setAlpha(Theme.getColor(0), 135), ColorUtils.setAlpha(Theme.getColor(0), 135), ColorUtils.setAlpha(Theme.getColor(0), 135)));
            }
            
            // Отрисовка головы скина
            int headSize = 14;
            int headX = (int)(x + offset + 5f);
            int headY = (int)(scrollY + offset + 65 + offsetAccounts - minus * 2);
            
            mc.getTextureManager().bindTexture(alt.skin);
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            // Рисуем голову (внешний слой скина)
            net.minecraft.client.gui.AbstractGui.blit(matrixStack, headX, headY, headSize, headSize, 8, 8, 8, 8, 64, 64);
            // Рисуем второй слой головы (шлем/аксессуары)
            net.minecraft.client.gui.AbstractGui.blit(matrixStack, headX, headY, headSize, headSize, 40, 8, 8, 8, 64, 64);
            
            Fonts.otwindowsa.drawText(matrixStack, alt.name, x + offset + 24f, scrollY - 2 + offset + 69 + offsetAccounts - minus * 2, -1, 9);

            // Рисуем иконку для избранных аккаунтов
            if (alt.favorite) {
                Fonts.icons2.drawText(matrixStack, "U", x + width - offset - 18f, scrollY - 2 + offset + 68 + offsetAccounts - minus * 2, ColorUtils.rgb(255, 215, 0), 10);
            }

            iter++;
            size++;
        }
        scroll = MathHelper.clamp(scroll, size > 8 ? -size + 4 : 0, 0);
        Scissor.unset();
        Scissor.pop();
        
        // Полоса прокрутки
        if (size > 8) {
            float scrollbarX = x + width - offset - 3;
            float scrollbarY = y + offset + 60f - minus * 2;
            float scrollbarWidth = 2;
            float scrollbarHeight = 177.5f - minus * 2;
            float scrollbarThumbHeight = Math.max(20, scrollbarHeight * (8 / size));
            float scrollbarThumbY = scrollbarY - (scrollAn / size) * (scrollbarHeight - scrollbarThumbHeight);
            
            // Фон полосы прокрутки
            DisplayUtils.drawRoundedRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, 1, ColorUtils.rgba(40, 40, 40, 100));
            // Ползунок
            DisplayUtils.drawRoundedRect(scrollbarX, scrollbarThumbY, scrollbarWidth, scrollbarThumbHeight, 1, ColorUtils.setAlpha(Theme.getColor(0), 180));
        }
        
        // Отображение текущего никнейма - окно на всю ширину панели
        if (currentAlt != null) {
            String currentText = "Текущий никнейм — " + currentAlt.name;
            float boxY = y + offset + 64 - minus * 2.5f + 177 - offset * 2 + 25;
            float boxHeight = 20;
            
            // Рисуем фон на всю ширину панели
            DisplayUtils.drawRoundedRect(x + offset - 1, boxY, width - offset * 2 + 2, boxHeight, 3, ColorUtils.rgba(15, 15, 15, 200));
            
            // Центрируем текст в окне
            float textWidth = mc.fontRenderer.getStringWidth(currentText);
            float textX = x + width / 2 - textWidth / 2f;
            float textY = boxY + (boxHeight - 8) / 2; // Центрируем по вертикали
            
            // Рисуем с тенью (обводкой) белым цветом
            mc.fontRenderer.drawStringWithShadow(matrixStack, currentText, textX, textY, 0xFFFFFF);
        }

        if (entryProgress < 1f) {
            int overlayAlpha = (int)((1f - entryProgress) * 220);
            fill(matrixStack, 0, 0, w, h, (overlayAlpha << 24));
        }

        mc.gameRenderer.setupOverlayRendering();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!altName.isEmpty() && typing)
                altName = altName.substring(0, altName.length() - 1);
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            if (!altName.isEmpty() && altName.length() >= 3 && typing) {
                // Проверка на дубликаты
                boolean exists = alts.stream().anyMatch(alt -> alt.name.equalsIgnoreCase(altName));
                if (!exists) {
                    alts.add(new polaroid.client.ui.mainscreen.Alt(altName));
                    AltConfig.updateFile();
                }
                typing = false;
                altName = "";
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (typing) {
                typing = false;
                altName = "";
            }
        }

        boolean ctrlDown = GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        if (typing) {
            if (ClientUtil.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_V) {
                try {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (ClientUtil.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                try {
                    altName = "";
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (typing && altName.length() < 16) {
            // Разрешаем только буквы, цифры и подчеркивание
            if (Character.isLetterOrDigit(codePoint) || codePoint == '_') {
                altName += Character.toString(codePoint);
            }
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
            if (!altName.isEmpty() && altName.length() >= 3 && typing) {
                // Проверка на дубликаты
                boolean exists = alts.stream().anyMatch(alt -> alt.name.equalsIgnoreCase(altName));
                if (!exists) {
                    alts.add(new polaroid.client.ui.mainscreen.Alt(altName));
                    AltConfig.updateFile();
                    altName = "";
                    typing = false;
                }
            }
            return true;
        }
        
        // Кнопка удаления
        float deleteButtonWidth = 18;
        float deleteButtonX = addButtonX + addButtonWidth + buttonSpacing;
        if (button == 0 && DisplayUtils.isInRegion(mouseX, mouseY, deleteButtonX, buttonY, deleteButtonWidth, buttonHeight)) {
            if (selectedAlt != null) {
                if (currentAlt == selectedAlt) {
                    currentAlt = null;
                }
                alts.remove(selectedAlt);
                selectedAlt = null;
                AltConfig.updateFile();
            }
            return true;
        }
        
        // Кнопка избранного
        float favoriteButtonWidth = 18;
        float favoriteButtonX = deleteButtonX + deleteButtonWidth + buttonSpacing;
        if (button == 0 && DisplayUtils.isInRegion(mouseX, mouseY, favoriteButtonX, buttonY, favoriteButtonWidth, buttonHeight)) {
            if (selectedAlt != null) {
                selectedAlt.favorite = !selectedAlt.favorite;
                AltConfig.updateFile();
            }
            return true;
        }
        
        // Кнопка GENERATE
        float generateButtonWidth = Fonts.otwindowsa.getWidth("GENERATE", 9) + 10;
        float generateButtonX = favoriteButtonX + favoriteButtonWidth + buttonSpacing;
        if (button == 0 && DisplayUtils.isInRegion(mouseX, mouseY, generateButtonX, buttonY, generateButtonWidth, buttonHeight)) {
            alts.add(new polaroid.client.ui.mainscreen.Alt(Polaroid.getInstance().randomNickname()));
            AltConfig.updateFile();
            return true;
        }

        // Клик по полю ввода
        if (button == 0 && DisplayUtils.isInRegion(mouseX, mouseY, x + offset - 1, y + offset + 64 - minus * 2.5f + 177 - offset * 2, width + 2 - offset * 2f, 20f)) {
            typing = !typing;
            return true;
        }

        // Обработка кликов по аккаунтам
        float iter = scrollAn, offsetAccounts = 0f;
        
        // Сортируем аккаунты так же, как при отрисовке
        List<polaroid.client.ui.mainscreen.Alt> sortedAlts = new ArrayList<>(alts);
        sortedAlts.sort((a1, a2) -> {
            if (a1.favorite && !a2.favorite) return -1;
            if (!a1.favorite && a2.favorite) return 1;
            return 0;
        });
        
        for (polaroid.client.ui.mainscreen.Alt account : sortedAlts) {
            float scrollY = y + iter * 22f;

            if (DisplayUtils.isInRegion(mouseX, mouseY, x + offset + 2f, scrollY + offset + 62 + offsetAccounts - minus * 2, width - offset * 2f - 4f, 20f)) {
                if (button == 0) {
                    long currentTime = System.currentTimeMillis();
                    
                    // Проверка на двойной клик
                    if (lastClickedAlt == account && (currentTime - lastClickTime) < 300) {
                        // Двойной клик - выбор/снятие текущего аккаунта
                        if (currentAlt == account) {
                            currentAlt = null;
                        } else {
                            currentAlt = account;
                            mc.session = new Session(account.name, "", "", "mojang");
                        }
                        lastClickedAlt = null;
                        lastClickTime = 0;
                    } else {
                        // Одиночный клик - выделение
                        selectedAlt = account;
                        lastClickedAlt = account;
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

        if (DisplayUtils.isInRegion(mouseX, mouseY, x + offset, y + offset + 60f - minus * 2, width - offset * 2f, 177.5f - minus * 2)) scroll += delta * 1;
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


