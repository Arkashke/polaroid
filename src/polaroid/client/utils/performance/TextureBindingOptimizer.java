package polaroid.client.utils.performance;

import net.minecraft.util.ResourceLocation;
import net.minecraft.client.Minecraft;

/**
 * Оптимизатор биндинга текстур - избегаем повторных bind вызовов
 */
public class TextureBindingOptimizer {
    
    private static final TextureBindingOptimizer INSTANCE = new TextureBindingOptimizer();
    private ResourceLocation currentTexture = null;
    
    public static TextureBindingOptimizer getInstance() {
        return INSTANCE;
    }
    
    /**
     * Биндит текстуру только если она отличается от текущей
     */
    public void bindTexture(ResourceLocation texture) {
        if (texture == null) return;
        
        if (!texture.equals(currentTexture)) {
            Minecraft.getInstance().getTextureManager().bindTexture(texture);
            currentTexture = texture;
        }
    }
    
    /**
     * Сбрасывает текущую текстуру (вызывать при смене контекста)
     */
    public void reset() {
        currentTexture = null;
    }
}


