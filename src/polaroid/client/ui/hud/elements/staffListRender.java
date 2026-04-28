package polaroid.client.ui.hud.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.Polaroid;
import polaroid.client.command.staffs.StaffStorage;
import polaroid.client.events.EventDisplay;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.ModuleRegistry;
import polaroid.client.modules.impl.render.InterFace;
import polaroid.client.modules.impl.render.Theme;
import polaroid.client.ui.hud.hudRender;
import polaroid.client.ui.hud.hudUpdater;
import polaroid.client.ui.styles.Style;
import polaroid.client.utils.drag.Dragging;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.math.Vector4i;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.GaussianBlur;
import polaroid.client.utils.render.KawaseBlur;
import polaroid.client.utils.render.font.Fonts;
import polaroid.client.utils.text.GradientUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class staffListRender implements hudRender, hudUpdater {

    final Dragging dragging;

    private final List<Staff> staffPlayers = new ArrayList<>();
    private final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
    private final Pattern prefixMatches = Pattern.compile(".*(mod|der|adm|help|wne|хелп|адм|поддержка|кура|own|taf|curat|dev|supp|yt|сотруд).*");

    @Override
    public void update(EventUpdate e) {
        staffPlayers.clear();

        for (ScorePlayerTeam team : mc.world.getScoreboard().getTeams().stream().sorted(Comparator.comparing(Team::getName)).toList()) {
            String name = team.getMembershipCollection().toString().replaceAll("[\\[\\]]", "");
            boolean vanish = true;
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    vanish = false;
                }
            }
            if (namePattern.matcher(name).matches() && !name.equals(mc.player.getName().getString())) {
                if (!vanish) {
                    if (prefixMatches.matcher(team.getPrefix().getString().toLowerCase(Locale.ROOT)).matches() || StaffStorage.isStaff(name)) {
                        Staff staff = new Staff(team.getPrefix(), name, false, Status.NONE);
                        staffPlayers.add(staff);
                    }
                }
                if (vanish && !team.getPrefix().getString().isEmpty()) {
                    Staff staff = new Staff(team.getPrefix(), name, true, Status.VANISHED);
                    staffPlayers.add(staff);
                }
            }
        }
    }

    float width;
    float height;

    @Override
    public void render(EventDisplay eventDisplay) {

        float posX = dragging.getX();
        float posY = dragging.getY();
        float padding = 4;
        float fontSize = 7;
        MatrixStack ms = eventDisplay.getMatrixStack();
        ITextComponent name = GradientUtil.gradient("StaffList");


        Style style = Polaroid.getInstance().getStyleManager().getCurrentStyle();

        ModuleRegistry moduleRegistry = Polaroid.getInstance().getFunctionRegistry();
        InterFace interFace = moduleRegistry.getInterFace();
        
        // Получаем цвета в зависимости от темы
        int backgroundColor = interFace.getBackgroundColor();
        int borderColor = interFace.getBorderColor();
        int textColor = interFace.getTextColor();
        
        // Тень как у KeyBinds
        DisplayUtils.drawShadow(posX, posY, width, height, 12, ColorUtils.rgba(0, 0, 0, 50));
        
        // Blur эффект как у KeyBinds
        GaussianBlur.startBlur();
        DisplayUtils.drawRoundedRect(posX, posY, width, height, 6f, backgroundColor);
        GaussianBlur.endBlur(15, 3);
        
        // Фон и обводка как у KeyBinds
        DisplayUtils.drawRoundedRect(posX, posY, width, height, 6f, backgroundColor);
        
        // ОПТИМИЗАЦИЯ: Обводка HUD без glow эффекта (было 10 слоев)
        if (interFace.hudOutline.get()) {
            int outlineColor = interFace.getHudOutlineColor(0);
            DisplayUtils.drawRoundedRectOutline(posX, posY, width, height, 6f, 1.2f, outlineColor);
        }
        
        // Текст слева, иконка справа (темный цвет как у KeyBinds)
        Fonts.otwindowsa.drawText(ms, "StaffList", posX + padding, posY - 2 + padding + 1f, textColor, 8);
        Fonts.icons2.drawText(ms, "L", posX + width - padding - Fonts.icons2.getWidth("L", 10), posY - 1 + padding + 1f, textColor, 10, -0.1f);
        posY += fontSize + padding * 2;

        float maxWidth = Fonts.otwindowsa.getWidth(name, fontSize) + padding * 2;
        float localHeight = fontSize + padding * 2;

        // Рисуем разделительную линию с анимацией если есть стафф
        if (!staffPlayers.isEmpty()) {
            // Всегда показываем линию если есть стафф (без анимации для этого элемента)
            DisplayUtils.drawRoundedRect(posX + padding, posY - 2f, width - padding * 2, 1, 
                    new Vector4f(0.5f, 0.5f, 0.5f, 0.5f), 
                    interFace.getSeparatorColor());
        }

        posY += 1.5f;
        for (staffListRender.Staff f : staffPlayers) {

            ITextComponent prefix = f.getPrefix();
            float prefixWidth = Fonts.otwindowsa.getWidth(prefix, fontSize);
            String staff = (prefix.getString().isEmpty() ? "" : " ") + f.getName();
            float nameWidth = Fonts.otwindowsa.getWidth(staff, fontSize);


            float localWidth = prefixWidth + nameWidth + Fonts.otwindowsa.getWidth(f.getStatus().string, fontSize) + padding * 3;

            // Ранг с оригинальным цветом из Minecraft, имя с цветом темы
            Fonts.otwindowsa.drawText(ms, prefix, posX + padding, posY, fontSize, 255);
            Fonts.otwindowsa.drawText(ms, staff, posX + padding + prefixWidth, posY, textColor, fontSize);
            
            // Добавляем скобки если включена настройка
            String statusText = f.getStatus().string;
            if (interFace.brackets.get() && !statusText.isEmpty()) {
                statusText = "[" + statusText + "]";
            }
            Fonts.otwindowsa.drawText(ms, statusText, posX + width - padding - Fonts.otwindowsa.getWidth(statusText, fontSize), posY, f.getStatus().color, fontSize);

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += fontSize + padding;
            localHeight += fontSize + padding;
        }

        width = MathUtil.lerp(width, Math.max(maxWidth, 85.0f), 20.0f);
        height = MathUtil.lerp(height, Math.max(localHeight + 1f, 10.0f), 20.0f);
        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    @AllArgsConstructor
    @Data
    public static class Staff {
        ITextComponent prefix;
        String name;
        boolean isSpec;
        Status status;

        public void updateStatus() {
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    if (info.getGameType() == GameType.SPECTATOR) {
                        return;
                    }
                    status = Status.NONE;
                    return;
                }
            }
            status = Status.VANISHED;
        }
    }

    public enum Status {
        NONE("", -1),
        VANISHED("V", ColorUtils.rgb(254, 68, 68));
        public final String string;
        public final int color;

        Status(String string, int color) {
            this.string = string;
            this.color = color;
        }
    }


}

