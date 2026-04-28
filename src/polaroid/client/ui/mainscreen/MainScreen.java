package polaroid.client.ui.mainscreen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.modules.api.ModuleRegistry;
import polaroid.client.modules.impl.render.InterFace;
import polaroid.client.modules.impl.render.Theme;
import polaroid.client.utils.math.Vector4i;
import polaroid.client.utils.render.font.Fonts;
import polaroid.client.utils.client.Vec2i;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.Scissor;
import com.mojang.blaze3d.matrix.MatrixStack;

import polaroid.client.Polaroid;
import polaroid.client.utils.client.ClientUtil;
import polaroid.client.utils.client.IMinecraft;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.math.StopWatch;
import polaroid.client.utils.render.ColorUtils;
import lombok.Getter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.client.renderer.RenderSkybox;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainScreen extends Screen implements IMinecraft {
    public MainScreen() {
        super(ITextComponent.getTextComponentOrEmpty(""));
    }

    public final StopWatch timer = new StopWatch();
    public static float o = 0;

    // Фон из assets/polaroid/images/mainScreen/ (положи BackGround.png, night_minecraft.png и т.д.)
    private static final ResourceLocation[] BACKGROUNDS = new ResourceLocation[]{
        new ResourceLocation("polaroid", "images/mainScreen/BackGround.png"),
        new ResourceLocation("polaroid", "images/mainScreen/night_minecraft.png"),
        new ResourceLocation("polaroid", "images/mainScreen/menu_background.png"),
        new ResourceLocation("minecraft", "polaroid/images/mainScreen/BackGround.png"),
        new ResourceLocation("minecraft", "polaroid/images/mainScreen/night_minecraft.png")
    };
    private static final RenderSkyboxCube PANORAMA_RESOURCES = new RenderSkyboxCube(new ResourceLocation("minecraft", "textures/gui/title/background/panorama"));
    private final RenderSkybox panorama = new RenderSkybox(PANORAMA_RESOURCES);
    private final List<Button> buttons = new ArrayList<>();

    private final List<Snowflake> snowflakes = new ArrayList<>();
    private final java.util.Random snowRandom = new java.util.Random();
    private float screenFadeProgress = 0f;
    
    private static class Snowflake {
        float x, y;
        float speed;
        float size;
        
        Snowflake(float x, float y, float speed, float size) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.size = size;
        }
    }


    @Override
    public void init(Minecraft minecraft, int width, int height) {
        MainWindow mainWindow = mc.getMainWindow();
        super.init(minecraft, width, height);
        screenFadeProgress = 0f;

        if (snowflakes.isEmpty()) {
            for (int i = 0; i < 100; i++) {
                float x = snowRandom.nextFloat() * width;
                float y = snowRandom.nextFloat() * height;
                float speed = 0.5f + snowRandom.nextFloat() * 1.5f;
                float size = 2.0f + snowRandom.nextFloat() * 3.0f;
                snowflakes.add(new Snowflake(x, y, speed, size));
            }
        }
        
        // Адаптивный лейаут: размеры и позиция кнопок рассчитываются от высоты окна и текстового блока
        float baseButtonWidth = 200f;
        float baseButtonHeight = 22f;
        float baseGap = 8f;

        // При очень маленьких окнах немного уменьшаем масштаб кнопок
        float verticalScale = Math.min(1.0f, height / 320f);
        float widthButton = baseButtonWidth * verticalScale;
        float heightButton = baseButtonHeight * verticalScale;
        float gap = baseGap * verticalScale;

        int buttonCount = 5;
        float totalButtons = buttonCount * heightButton + (buttonCount - 1) * gap;

        // Оцениваем высоту текстового блока (логотип + приветствие + строка с названием клиента)
        float logoY = 42f * verticalScale;
        float textBlockTop = logoY + 32f * verticalScale;
        float textBlockBottom = textBlockTop + 32f * verticalScale;

        // Минимальный отступ между текстом и первой кнопкой
        float minTextToButtonsGap = 10f * verticalScale;

        float availableBelowText = height - textBlockBottom - 10f;
        float minButtonY = textBlockBottom + minTextToButtonsGap;

        float centerY = textBlockBottom + (availableBelowText - totalButtons) / 2f;
        float maxButtonY = height - 10f - totalButtons;

        // Если места не хватает, прижимаем блок кнопок вниз, но не даём им залезть на текст
        float y = Math.max(minButtonY, Math.min(centerY, maxButtonY));

        float x = width / 2f - widthButton / 2f;

        buttons.clear();
        buttons.add(new Button(x, y, widthButton, heightButton, "Одиночная игра", false, () -> {
            mc.displayGuiScreen(new WorldSelectionScreen(this));
        }));
        buttons.add(new Button(x, y + (heightButton + gap), widthButton, heightButton, "Сетевая игра", false, () -> {
            mc.displayGuiScreen(new MultiplayerScreen(this));
        }));
        buttons.add(new Button(x, y + 2 * (heightButton + gap), widthButton, heightButton, "Смена аккаунта", true, () -> {
            mc.displayGuiScreen(Polaroid.getInstance().getAltScreen());
        }));
        buttons.add(new Button(x, y + 3 * (heightButton + gap), widthButton, heightButton, "Настройки", false, () -> {
            mc.displayGuiScreen(new OptionsScreen(this, mc.gameSettings));
        }));
        buttons.add(new Button(x, y + 4 * (heightButton + gap), widthButton, heightButton, "Выход", false, mc::shutdownMinecraftApplet));
    }


    private final StopWatch stopWatch = new StopWatch();
    static boolean start = false;

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        screenFadeProgress = Math.min(1f, screenFadeProgress + 0.06f);
        drawMenuBackground(matrixStack, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        Scissor.push();
        Scissor.setFromComponentCoordinates(0, 0, width, height);

        String username = mc.getSession() != null ? mc.getSession().getUsername() : null;
        if (username == null || username.isEmpty()) username = "Player";
        drawLoggedAs(matrixStack, username);

        // Логотип "P" сверху, текст и кнопки с отступами — без наложения
        float verticalScale = Math.min(1.0f, height / 320f);
        float logoY = 42f * verticalScale;
        Fonts.otwindowsa.drawCenteredText(matrixStack, "P", width / 2f, logoY, ColorUtils.rgba(255, 255, 255, 255), 20 * verticalScale);

        float blockTop = logoY + 32f * verticalScale;
        String welcomeText = "Добро пожаловать, " + username + "!";
        if (Fonts.otwindowsa.getWidth(welcomeText, 10 * verticalScale) > width - 20) {
            welcomeText = username.length() > 12 ? username.substring(0, 10) + "..." : welcomeText;
        }
        Fonts.otwindowsa.drawCenteredText(matrixStack, welcomeText, width / 2f, blockTop, ColorUtils.rgba(255, 255, 255, 255), 10 * verticalScale);
        String line2 = "Вы в ";
        String brand = Polaroid.CLIENT_NAME;
        String line3 = ". Лучший клиент Minecraft.";
        float textSize = 9 * verticalScale;
        float l2w = Fonts.otwindowsa.getWidth(line2, textSize);
        float bw = Fonts.otwindowsa.getWidth(brand, textSize);
        float l3w = Fonts.otwindowsa.getWidth(line3, textSize);
        float totalW = Math.min(l2w + bw + l3w, width - 24);
        float startX = width / 2f - totalW / 2f;
        float secondaryY = blockTop + 17f * verticalScale;
        Fonts.otwindowsa.drawText(matrixStack, line2, Math.max(4, startX), secondaryY, ColorUtils.rgba(255, 255, 255, 255), textSize);
        Fonts.otwindowsa.drawText(matrixStack, brand, startX + l2w, secondaryY, Theme.getColor(0), textSize);
        Fonts.otwindowsa.drawText(matrixStack, line3, startX + l2w + bw, secondaryY, ColorUtils.rgba(255, 255, 255, 255), textSize);

        drawButtons(matrixStack, mouseX, mouseY, partialTicks);

        Scissor.unset();
        Scissor.pop();

        if (screenFadeProgress < 1f) {
            int overlayAlpha = (int)((1f - screenFadeProgress) * 220);
            fill(matrixStack, 0, 0, width, height, (overlayAlpha << 24));
        }

        mc.gameRenderer.setupOverlayRendering();
    }

    private void drawLoggedAs(MatrixStack matrixStack, String username) {
        if (width < 130) return;
        float avatarSize = 24;
        float x = 12;
        float y = 12;

        ResourceLocation skin = DefaultPlayerSkin.getDefaultSkin(UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes()));
        mc.getTextureManager().bindTexture(skin);
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        AbstractGui.blit(matrixStack, (int)x, (int)y, (int)avatarSize, (int)avatarSize, 8, 8, 8, 8, 64, 64);
        AbstractGui.blit(matrixStack, (int)x, (int)y, (int)avatarSize, (int)avatarSize, 40, 8, 8, 8, 64, 64);
        
        Fonts.otwindowsa.drawText(matrixStack, "Вход выполнен", x + avatarSize + 6, y + 2, ColorUtils.rgba(180, 180, 180, 255), 7);
        Fonts.otwindowsa.drawText(matrixStack, username, x + avatarSize + 6, y + 12, ColorUtils.rgba(255, 255, 255, 255), 8);
    }

    private void drawMenuBackground(MatrixStack matrixStack, float partialTicks) {
        boolean hasPng = false;
        for (ResourceLocation bg : BACKGROUNDS) {
            boolean exists = false;
            try { exists = mc.getResourceManager().hasResource(bg); } catch (Exception ignored) {}
            if (!exists) continue;
            try {
                mc.getTextureManager().bindTexture(bg);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                AbstractGui.blit(matrixStack, 0, 0, width, height, 0, 0, 256, 256, 256, 256);
                hasPng = true;
                break;
            } catch (Exception ignored) {}
        }
        if (!hasPng) {
            panorama.render(partialTicks, 1.0F);
        }
        // Сброс GL-состояния после панорамы — иначе цвет "течёт" на кнопки
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        // Тёмный полупрозрачный слой для читаемости текста
        this.fillGradient(matrixStack, 0, 0, width, height, 0x88000000, 0xAA000000);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (Snowflake snowflake : snowflakes) {
            snowflake.y += snowflake.speed;
            if (snowflake.y > height) {
                snowflake.y = -10;
                snowflake.x = snowRandom.nextFloat() * width;
            }
            DisplayUtils.drawCircle(snowflake.x, snowflake.y, snowflake.size,
                ColorUtils.rgba(255, 255, 255, 120));
        }
        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixed = MathUtil.getMouse2i((int) mouseX, (int) mouseY);
        int fixedX = fixed.getX();
        int fixedY = fixed.getY();
        buttons.forEach(b -> b.click(fixedX, fixedY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawButtons(MatrixStack stack, int mX, int mY, float pt) {
        for (Button b : buttons) {
            b.render(stack, mX, mY, pt);
        }
    }

    private class Button {
        @Getter
        private final float x, y, width, height;
        private String text;
        private Runnable action;
        private boolean accent;
        private float hoverProgress = 0f;
        private boolean wasHovered = false;
        private static final float ANIM_SPEED = 0.2f;

        public Button(float x, float y, float width, float height, String text, boolean accent, Runnable action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
            this.accent = accent;
            this.action = action;
        }

        public void render(MatrixStack stack, int mouseX, int mouseY, float pt) {
            boolean isHovered = MathUtil.isHovered(mouseX, mouseY, (int) x, (int) y, (int) width, (int) height);
            if (isHovered && !wasHovered) {
                mc.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0F));
            }
            wasHovered = isHovered;
            hoverProgress = isHovered ? Math.min(hoverProgress + ANIM_SPEED, 1f) : Math.max(hoverProgress - ANIM_SPEED, 0f);

            int radius = 4;
            int buttonBg;
            int borderColor;
            int textColor;
            if (accent) {
                int themeBg = Theme.getColor(0);
                int themeBorder = Theme.getColor(90);
                int r = (themeBg >> 16) & 0xFF, g = (themeBg >> 8) & 0xFF, b = themeBg & 0xFF;
                int br = (themeBorder >> 16) & 0xFF, bg = (themeBorder >> 8) & 0xFF, bb = themeBorder & 0xFF;
                buttonBg = ColorUtils.rgba(r, g, b, isHovered ? 220 : 180);
                borderColor = ColorUtils.rgba(br, bg, bb, isHovered ? (int)(180 + 75 * hoverProgress) : 150);
                textColor = ColorUtils.rgba(255, 255, 255, 255);
            } else {
                int bgAlpha = isHovered ? 80 : 50;
                buttonBg = ColorUtils.rgba(30, 30, 40, bgAlpha);
                int borderAlpha = isHovered ? (int)(100 + 80 * hoverProgress) : 100;
                borderColor = ColorUtils.rgba(100, 100, 120, borderAlpha);
                textColor = ColorUtils.rgba(230, 230, 230, 255);
            }

            DisplayUtils.drawRoundedRect(x, y, width, height,
                new Vector4f(radius, radius, radius, radius), buttonBg);
            DisplayUtils.drawRoundedRectOutline(x, y, width, height, radius, 1f, borderColor);

            Fonts.otwindowsa.drawCenteredText(stack, text, x + width / 2, y + height / 2f - 4f, textColor, 9f);
        }

        public void click(int mouseX, int mouseY, int button) {
            if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height)) {
                mc.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0F));
                action.run();
            }
        }
    }
}


