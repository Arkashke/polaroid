package polaroid.client.utils.client;

import polaroid.client.modules.impl.render.Theme;
import polaroid.client.utils.math.Vector4i;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.text.GradientUtil;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public interface IMinecraft {
    Minecraft mc = Minecraft.getInstance();

    MainWindow window = mc.getMainWindow();
    BufferBuilder buffer = Tessellator.getInstance().getBuffer();
    Tessellator tessellator = Tessellator.getInstance();
    List<ITextComponent> clientMessages = new ArrayList<>();
    default void print(String input) {
        if (mc.player == null)
            return;
        ITextComponent text = GradientUtil.gradient(polaroid.client.Polaroid.CLIENT_NAME)
                .append(new StringTextComponent(TextFormatting.DARK_GRAY + " -> " + TextFormatting.RESET + input));
        clientMessages.add(text);
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(text, 0);

    }
    public static Vector4i getVector3(){return new Vector4i(ColorUtils.getColor(ColorUtils.rgba(25,25,25,200)), ColorUtils.getColor(ColorUtils.rgba(30,30,30,200)),ColorUtils.getColor(ColorUtils.rgba(35,35,35,200)),ColorUtils.getColor(ColorUtils.rgba(40,40,40,200)));}
    public static Vector4i getVector4(){return new Vector4i(ColorUtils.setAlpha(ColorUtils.getColor(ColorUtils.rgba(25,25,25,200)),200), ColorUtils.setAlpha(ColorUtils.getColor(ColorUtils.rgba(30,30,30,200)),200),ColorUtils.setAlpha(ColorUtils.getColor(ColorUtils.rgba(35,35,35,200)),200),ColorUtils.setAlpha(ColorUtils.getColor(ColorUtils.rgba(40,40,40,200)),200));}
    public static Vector4i getVector2(){return new Vector4i(Theme.getColor(120,1),Theme.getColor(90,1),Theme.getColor(60,1),Theme.getColor(30,1));}
    public static Vector4i getVector(){return new Vector4i(Theme.getColor(0,1), Theme.getColor(90,1),Theme.getColor(180,1),Theme.getColor(270,1));}
    public static Vector4i getVector5(){return new Vector4i(ColorUtils.setAlpha(Theme.getColor(0,1),200),ColorUtils.setAlpha(Theme.getColor(90,1),200),ColorUtils.setAlpha(Theme.getColor(180,1),200),ColorUtils.setAlpha(Theme.getColor(270,1),200));}
    public static Vector4i getVectoralt(){return new Vector4i(ColorUtils.rgba(25,25,25,200),ColorUtils.rgba(25,25,25,200),ColorUtils.rgba(25,25,25,200),ColorUtils.rgba(25,25,25,200));}
}


