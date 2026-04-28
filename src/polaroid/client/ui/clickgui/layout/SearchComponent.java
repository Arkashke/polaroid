package polaroid.client.ui.clickgui.layout;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.Polaroid;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.font.Fonts;
import org.lwjgl.glfw.GLFW;

/**
 * Компонент поиска модулей
 */
public class SearchComponent {
    
    private float x, y, width, height;
    private String searchText = "";
    private boolean focused = false;
    private int cursorPosition = 0;
    private long lastCursorBlink = 0;
    
    private static final float SEARCH_HEIGHT = 18f; // Уменьшил с 20f до 18f
    private static final String PLACEHOLDER = "Search...";
    
    public SearchComponent(float x, float y, float width) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = SEARCH_HEIGHT;
    }
    
    public void render(MatrixStack stack, int mouseX, int mouseY) {
        int themeColor = Polaroid.getInstance().getStyleManager().getCurrentStyle().getFirstColor().getRGB();
        float radius = 7.4f; // Радиус как у панелей
        
        // Фон поиска (блюр удален, оптимальная прозрачность)
        DisplayUtils.drawRoundedRect(x, y, width, height, 
                new Vector4f(radius, radius, radius, radius), ColorUtils.rgba(16, 16, 16, 185));;
        
        DisplayUtils.drawRoundedRect(x, y, width, height, 
                new Vector4f(radius, radius, radius, radius), ColorUtils.rgba(16, 16, 16, 150));
        
        // Иконка лупы слева
        float iconX = x + 6;
        float iconY = y + (height - 7) / 2f;
        Fonts.icons2.drawText(stack, "Y", iconX, iconY, 
                focused ? ColorUtils.setAlpha(themeColor, 255) : ColorUtils.rgba(120, 120, 120, 255), 
                7, -0.1f);
        
        // Текст или placeholder
        float textX = x + 17;
        float textY = y + (height - 5.5f) / 2f;
        
        if (searchText.isEmpty() && !focused) {
            // Placeholder
            Fonts.otwindowsa.drawText(stack, PLACEHOLDER, textX, textY, 
                    ColorUtils.rgba(100, 100, 100, 255), 5.5f);
        } else {
            // Введённый текст
            Fonts.otwindowsa.drawText(stack, searchText, textX, textY, 
                    ColorUtils.rgba(255, 255, 255, 255), 5.5f);
            
            // Курсор (мигающий)
            if (focused && (System.currentTimeMillis() - lastCursorBlink) % 1000 < 500) {
                float cursorX = textX + Fonts.otwindowsa.getWidth(
                        searchText.substring(0, Math.min(cursorPosition, searchText.length())), 5.5f);
                DisplayUtils.drawRoundedRect(cursorX, textY - 1, 1, 7, 
                        new Vector4f(0, 0, 0, 0), ColorUtils.rgba(255, 255, 255, 255));
            }
        }
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean wasInside = mouseX >= x && mouseX <= x + width && 
                           mouseY >= y && mouseY <= y + height;
        
        if (button == 0) {
            focused = wasInside;
            if (focused) {
                cursorPosition = searchText.length();
            }
            return wasInside;
        }
        
        return false;
    }
    
    public boolean charTyped(char character, int modifiers) {
        if (!focused) return false;
        
        // Фильтруем недопустимые символы
        if (character < 32 || character > 126) return false;
        
        searchText = searchText.substring(0, cursorPosition) + character + 
                    searchText.substring(cursorPosition);
        cursorPosition++;
        lastCursorBlink = System.currentTimeMillis();
        
        return true;
    }
    
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused) return false;
        
        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE:
                if (cursorPosition > 0 && !searchText.isEmpty()) {
                    searchText = searchText.substring(0, cursorPosition - 1) + 
                                searchText.substring(cursorPosition);
                    cursorPosition--;
                    lastCursorBlink = System.currentTimeMillis();
                }
                return true;
                
            case GLFW.GLFW_KEY_DELETE:
                if (cursorPosition < searchText.length()) {
                    searchText = searchText.substring(0, cursorPosition) + 
                                searchText.substring(cursorPosition + 1);
                    lastCursorBlink = System.currentTimeMillis();
                }
                return true;
                
            case GLFW.GLFW_KEY_LEFT:
                if (cursorPosition > 0) {
                    cursorPosition--;
                    lastCursorBlink = System.currentTimeMillis();
                }
                return true;
                
            case GLFW.GLFW_KEY_RIGHT:
                if (cursorPosition < searchText.length()) {
                    cursorPosition++;
                    lastCursorBlink = System.currentTimeMillis();
                }
                return true;
                
            case GLFW.GLFW_KEY_HOME:
                cursorPosition = 0;
                lastCursorBlink = System.currentTimeMillis();
                return true;
                
            case GLFW.GLFW_KEY_END:
                cursorPosition = searchText.length();
                lastCursorBlink = System.currentTimeMillis();
                return true;
                
            case GLFW.GLFW_KEY_ESCAPE:
                focused = false;
                return true;
                
            case GLFW.GLFW_KEY_V:
                // Ctrl+V для вставки
                if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                    try {
                        String clipboard = GLFW.glfwGetClipboardString(0);
                        if (clipboard != null && !clipboard.isEmpty()) {
                            searchText = searchText.substring(0, cursorPosition) + clipboard + 
                                        searchText.substring(cursorPosition);
                            cursorPosition += clipboard.length();
                            lastCursorBlink = System.currentTimeMillis();
                        }
                    } catch (Exception e) {
                        // Игнорируем ошибки буфера обмена
                    }
                    return true;
                }
                break;
                
            case GLFW.GLFW_KEY_A:
                // Ctrl+A для выделения всего (упрощённо - курсор в конец)
                if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                    cursorPosition = searchText.length();
                    lastCursorBlink = System.currentTimeMillis();
                    return true;
                }
                break;
        }
        
        return false;
    }
    
    public String getSearchText() {
        return searchText.toLowerCase().trim();
    }
    
    public boolean isEmpty() {
        return searchText.trim().isEmpty();
    }
    
    public boolean isFocused() {
        return focused;
    }
    
    public void setFocused(boolean focused) {
        this.focused = focused;
    }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public float getHeight() {
        return height;
    }
}


