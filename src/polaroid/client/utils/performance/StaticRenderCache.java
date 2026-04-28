package polaroid.client.utils.performance;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

/**
 * Кэш для статичных элементов рендеринга (Display Lists)
 * Используется для элементов, которые не меняются часто
 */
public class StaticRenderCache {
    
    private static final StaticRenderCache INSTANCE = new StaticRenderCache();
    
    private final Map<String, Integer> displayLists = new HashMap<>();
    private final Map<String, Long> lastUpdateTime = new HashMap<>();
    
    // Время жизни кэша в миллисекундах (5 секунд)
    private static final long CACHE_TTL = 5000;
    
    public static StaticRenderCache getInstance() {
        return INSTANCE;
    }
    
    /**
     * Получает или создает Display List для статичного элемента
     */
    public int getOrCreateDisplayList(String key, Runnable renderCode) {
        Long lastUpdate = lastUpdateTime.get(key);
        long currentTime = System.currentTimeMillis();
        
        // Если кэш устарел или не существует - пересоздаем
        if (lastUpdate == null || (currentTime - lastUpdate) > CACHE_TTL) {
            // Удаляем старый Display List если есть
            Integer oldList = displayLists.get(key);
            if (oldList != null) {
                GL11.glDeleteLists(oldList, 1);
            }
            
            // Создаем новый Display List
            int displayList = GL11.glGenLists(1);
            GL11.glNewList(displayList, GL11.GL_COMPILE);
            renderCode.run();
            GL11.glEndList();
            
            displayLists.put(key, displayList);
            lastUpdateTime.put(key, currentTime);
            
            return displayList;
        }
        
        return displayLists.get(key);
    }
    
    /**
     * Вызывает Display List
     */
    public void callDisplayList(String key) {
        Integer displayList = displayLists.get(key);
        if (displayList != null) {
            GL11.glCallList(displayList);
        }
    }
    
    /**
     * Инвалидирует кэш для конкретного ключа
     */
    public void invalidate(String key) {
        Integer displayList = displayLists.remove(key);
        if (displayList != null) {
            GL11.glDeleteLists(displayList, 1);
        }
        lastUpdateTime.remove(key);
    }
    
    /**
     * Очищает весь кэш
     */
    public void clear() {
        for (Integer displayList : displayLists.values()) {
            GL11.glDeleteLists(displayList, 1);
        }
        displayLists.clear();
        lastUpdateTime.clear();
    }
}


