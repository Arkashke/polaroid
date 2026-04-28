package polaroid.client.ui.hud.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import polaroid.client.Polaroid;
import polaroid.client.events.EventDisplay;
import polaroid.client.modules.api.ModuleRegistry;
import polaroid.client.modules.impl.render.InterFace;
import polaroid.client.modules.impl.render.Theme;
import polaroid.client.ui.hud.hudRender;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.GaussianBlur;
import polaroid.client.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.text.SimpleDateFormat;
import java.util.Date;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class waterMarkRender implements hudRender {

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();

        float posX = 8;
        float posY = 8;
        float fontSize = 8.5f;
        float padding = 4.5f;
        float iconSizeMain = 10;
        float iconSizeOther = 7;
        float iconSizeTime = 8;

        ModuleRegistry moduleRegistry = Polaroid.getInstance().getFunctionRegistry();
        InterFace interFace = moduleRegistry.getInterFace();

        int fps = Minecraft.getInstance().getDebugFPS();
        int ping = 0;
        if (mc.getConnection() != null && mc.getConnection().getPlayerInfo(mc.player.getUniqueID()) != null) {
            ping = mc.getConnection().getPlayerInfo(mc.player.getUniqueID()).getResponseTime();
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String currentTime = timeFormat.format(new Date());

        float width = 195;
        float height = fontSize + padding * 2;

        float watermarkRadius = 6f;
        
        int backgroundColor = interFace.getBackgroundColor();
        int borderColor = interFace.getBorderColor();
        int textColor = interFace.getTextColor();
        int separatorColor = interFace.getSeparatorColor();

        DisplayUtils.drawRoundedRect(posX, posY, width, height, watermarkRadius, backgroundColor);
        
        if (interFace.hudOutline.get()) {
            int outlineColor = interFace.getHudOutlineColor(0);
            DisplayUtils.drawRoundedRectOutline(posX, posY, width, height, watermarkRadius, 1.2f, outlineColor);
        }

        float currentX = posX + padding;
        float textY = posY + padding - 1f + 0.5f;
        
        float centerY = posY + height / 2f;
        float iconYMain = centerY - iconSizeMain / 2f;
        float iconYOther = centerY - iconSizeOther / 2f + 0.5f;
        float iconYTime = centerY - iconSizeTime / 2f + 0.5f;
        
        Fonts.icons2.drawText(ms, "#", currentX, iconYMain, textColor, iconSizeMain, -0.1f);
        currentX += iconSizeMain + 2;
        String clientName = Polaroid.CLIENT_NAME;
        Fonts.otwindowsa.drawText(ms, clientName, currentX, textY, textColor, fontSize);
        currentX += Fonts.otwindowsa.getWidth(clientName, fontSize) + 6;
        
        DisplayUtils.drawRoundedRect(currentX, posY + 4, 1, height - 8, 
                new net.minecraft.util.math.vector.Vector4f(0.5f, 0.5f, 0.5f, 0.5f), 
                separatorColor);
        currentX += 5;

        Fonts.icons2.drawText(ms, "X", currentX, iconYOther, textColor, iconSizeOther, -0.1f);
        currentX += iconSizeOther + 2;
        Fonts.otwindowsa.drawText(ms, fps + " FPS", currentX, textY, textColor, fontSize - 0.5f);
        currentX += Fonts.otwindowsa.getWidth("999 FPS", fontSize - 0.5f) + 4;
        
        DisplayUtils.drawRoundedRect(currentX, posY + 4, 1, height - 8, 
                new net.minecraft.util.math.vector.Vector4f(0.5f, 0.5f, 0.5f, 0.5f), 
                separatorColor);
        currentX += 5;

        Fonts.icons2.drawText(ms, "Q", currentX, iconYOther, textColor, iconSizeOther, -0.1f);
        currentX += iconSizeOther + 2;
        Fonts.otwindowsa.drawText(ms, ping + " MS", currentX, textY, textColor, fontSize - 0.5f);
        currentX += Fonts.otwindowsa.getWidth("999 MS", fontSize - 0.5f) + 4;
        
        DisplayUtils.drawRoundedRect(currentX, posY + 4, 1, height - 8, 
                new net.minecraft.util.math.vector.Vector4f(0.5f, 0.5f, 0.5f, 0.5f), 
                separatorColor);
        currentX += 5;

        Fonts.icons2.drawText(ms, "V", currentX, iconYTime, textColor, iconSizeTime, -0.1f);
        currentX += iconSizeTime + 2;
        Fonts.otwindowsa.drawText(ms, currentTime, currentX, textY, textColor, fontSize - 0.5f);
    }
}


