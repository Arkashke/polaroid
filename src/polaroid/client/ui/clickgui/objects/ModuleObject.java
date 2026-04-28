package polaroid.client.ui.clickgui.objects;

import com.mojang.blaze3d.matrix.MatrixStack;
import polaroid.client.Polaroid;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.settings.Setting;
import polaroid.client.modules.settings.impl.*;
import polaroid.client.ui.clickgui.objects.sets.*;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.render.font.Fonts;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class ModuleObject extends Object {

    public Module module;
    public ArrayList<Object> object = new ArrayList<>();
    public float animation,animation_height;

    boolean binding;

    public ModuleObject(Module module) {
        this.module = module;
        for (Setting setting : module.getSettings()) {
            if (setting instanceof BooleanSetting option) {
                object.add(new BooleanObject(this, option));
            }
            if (setting instanceof ColorSetting option) {
                object.add(new ColorObject(this, option));
            }
            if (setting instanceof SliderSetting option) {
                object.add(new SliderObject(this, option));
            }
            if (setting instanceof ModeSetting option) {
                object.add(new ModeObject(this, option));
            }
            if (setting instanceof ModeListSetting option) {
                object.add(new MultiObject(this, option));
            }
            if (setting instanceof BindSetting option) {
                object.add(new BindObject(this, option));
            }
            if (setting instanceof StringSetting option) {
                object.add(new StringObject(this, option));
            }
        }
    }

    float lastHeight;
    public boolean isBinding = false;


    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for ( Object object1 : object) {
            object1.mouseClicked(mouseX, mouseY, mouseButton);
        }
        if (isHovered(mouseX,mouseY, 15)) {
            if (mouseButton == 0)
                module.toggle();
            if (mouseButton == 2)
                isBinding = !isBinding;
        }
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {

    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for ( Object object1 : object) {
            object1.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        if (isBinding) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE ||
                    keyCode == GLFW.GLFW_KEY_ESCAPE) {
                module.setBind(0);
            } else {
                module.setBind(keyCode);
            }
            isBinding = false;
        }

        for (Object obj : object) {
            if (obj instanceof BindObject m) {
                if (m.isBinding) {
                    if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE ||
                            keyCode == GLFW.GLFW_KEY_ESCAPE) {
                        m.set.set(0);
                        m.isBinding = false;
                        continue;
                    }
                    m.set.set(keyCode);
                    m.isBinding = false;
                }
            }
            obj.keyTyped(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for( Object obj:  object){
            obj.charTyped(codePoint, modifiers);
        }
    }

    float hover_anim;

    boolean expand = false;
    public float expand_anim;

    @Override
    public void exit() {
        super.exit();
        for (Object obj : object) {
            obj.exit();
        }
    }

    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);
        hover_anim = MathUtil.fast(hover_anim, MathUtil.isHovered(mouseX,mouseY,x, y, width, height) ? 1 : 0, 10);
        
        DisplayUtils.drawRoundedRectOutline(x, y, width, height, 1, 0f, ColorUtils.rgba(25, 26, 33, 100));
        
        animation = MathUtil.fast(animation, module.isState() ? 1 : 0, 5);
        animation_height = MathUtil.fast(animation_height, height, 5);

        String text = module.getName();
        if(binding) text += "...";
        if(module.getBind() != 0) text += GLFW.glfwGetKeyName(module.getBind(), 0);

        int textColor = module.isState() ? 
            polaroid.client.modules.impl.render.Theme.getColor(0) : 
            ColorUtils.rgba(255, 255, 255, 255);
            
        Fonts.otwindowsa.drawText(stack, text, x + 10, y + 10, textColor, 15);

        if (!module.getSettings().isEmpty()) {
            DisplayUtils.drawRoundedRect(x + 10, y + 22, width - 20, 0.5f, 
                new Vector4f(0,0,0,0), ColorUtils.rgba(32, 35, 57,255));
        }

        drawObjects(stack, mouseX, mouseY);
    }

    public void drawObjects(MatrixStack stack, int mouseX, int mouseY) {
        float offset = -4;
        for (Object obj : object) {
            if (obj.setting != null && obj.setting.visible != null) {
                try {
                    Boolean visibleValue = (Boolean) obj.setting.visible.get();
                    if (Boolean.TRUE.equals(visibleValue)) {
                        obj.x = x;
                        obj.y = y + 15 + offset;
                        obj.width = 160;
                        obj.height = 8;
                        obj.draw(stack, mouseX, mouseY);
                        offset += obj.height;
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

}


