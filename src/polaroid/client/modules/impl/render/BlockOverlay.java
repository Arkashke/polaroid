package polaroid.client.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import polaroid.client.events.WorldEvent;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.render.ColorUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

/**
 * BlockOverlay - модуль для отображения обводки блока на который смотрит игрок
 * Добавляет плавную анимацию перемещения обводки между блоками
 */
@ModuleSystem(
    name = "BlockOverlay",
    type = Category.Render,
    server = ServerCategory.NO,
    description = "Отображает обводку блока на который вы смотрите"
)
public class BlockOverlay extends Module {

    // Настройки отображения
    private final BooleanSetting fill = new BooleanSetting("Заливка", true);
    private final SliderSetting fillAlpha = new SliderSetting(
        "Прозрачность заливки", 
        50.0f, 
        0.0f, 
        100.0f, 
        5.0f
    ).setVisible(() -> fill.get());
    
    private final BooleanSetting outline = new BooleanSetting("Обводка", true);
    private final SliderSetting outlineAlpha = new SliderSetting(
        "Прозрачность обводки", 
        100.0f, 
        0.0f, 
        100.0f, 
        5.0f
    ).setVisible(() -> outline.get());
    
    // Настройка плавности перемещения
    private final SliderSetting smoothness = new SliderSetting(
        "Плавность", 
        5.0f, 
        1.0f, 
        10.0f, 
        0.5f
    );

    // Текущая и целевая позиция для плавной анимации
    private BlockPos currentPos = null;
    private BlockPos targetPos = null;
    
    // Интерполированные координаты для плавного перемещения
    private double animX = 0;
    private double animY = 0;
    private double animZ = 0;
    
    // Интерполированные размеры bounding box
    private double animMinX = 0, animMinY = 0, animMinZ = 0;
    private double animMaxX = 0, animMaxY = 0, animMaxZ = 0;

    public BlockOverlay() {
        addSettings(fill, fillAlpha, outline, outlineAlpha, smoothness);
    }

    @Subscribe
    public void onWorldRender(WorldEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        // Проверяем что игрок смотрит на блок
        if (mc.objectMouseOver instanceof BlockRayTraceResult) {
            BlockRayTraceResult result = (BlockRayTraceResult) mc.objectMouseOver;
            
            if (result.getType() == RayTraceResult.Type.BLOCK) {
                targetPos = result.getPos();
                
                // Получаем форму блока
                VoxelShape shape = mc.world.getBlockState(targetPos).getShape(mc.world, targetPos);
                
                if (!shape.isEmpty()) {
                    // Получаем bounding box блока
                    AxisAlignedBB box = shape.getBoundingBox();
                    
                    // Целевые координаты
                    double targetMinX = targetPos.getX() + box.minX;
                    double targetMinY = targetPos.getY() + box.minY;
                    double targetMinZ = targetPos.getZ() + box.minZ;
                    double targetMaxX = targetPos.getX() + box.maxX;
                    double targetMaxY = targetPos.getY() + box.maxY;
                    double targetMaxZ = targetPos.getZ() + box.maxZ;
                    
                    // Инициализируем при первом запуске
                    if (currentPos == null) {
                        currentPos = targetPos;
                        animMinX = targetMinX;
                        animMinY = targetMinY;
                        animMinZ = targetMinZ;
                        animMaxX = targetMaxX;
                        animMaxY = targetMaxY;
                        animMaxZ = targetMaxZ;
                    }
                    
                    // Плавная интерполяция
                    // Чем выше значение плавности, тем медленнее движение (больше инерция)
                    // smoothness 1 = быстро (speed 0.5)
                    // smoothness 10 = медленно (speed 0.05)
                    double speed = 0.5 / smoothness.get();
                    
                    // Простая линейная интерполяция без deltaTime
                    animMinX += (targetMinX - animMinX) * speed;
                    animMinY += (targetMinY - animMinY) * speed;
                    animMinZ += (targetMinZ - animMinZ) * speed;
                    animMaxX += (targetMaxX - animMaxX) * speed;
                    animMaxY += (targetMaxY - animMaxY) * speed;
                    animMaxZ += (targetMaxZ - animMaxZ) * speed;
                    
                    // Обновляем текущую позицию
                    currentPos = targetPos;
                    
                    // Создаем интерполированный bounding box
                    AxisAlignedBB renderBox = new AxisAlignedBB(
                        animMinX, animMinY, animMinZ,
                        animMaxX, animMaxY, animMaxZ
                    );
                    
                    // Смещаем относительно камеры
                    Vector3d cam = mc.getRenderManager().info.getProjectedView();
                    renderBox = renderBox.offset(-cam.x, -cam.y, -cam.z);
                    
                    // Получаем цвет из темы клиента
                    int color = Theme.getColor(0);
                    
                    // Рендерим заливку
                    if (fill.get()) {
                        int fillColor = ColorUtils.setAlpha(color, (int) (fillAlpha.get() * 2.55f));
                        renderBlockFill(renderBox, fillColor);
                    }
                    
                    // Рендерим обводку
                    if (outline.get()) {
                        int outlineColor = ColorUtils.setAlpha(color, (int) (outlineAlpha.get() * 2.55f));
                        renderBlockOutline(renderBox, outlineColor);
                    }
                }
            }
        } else {
            // Сбрасываем позицию если не смотрим на блок
            currentPos = null;
            targetPos = null;
        }
    }

    /**
     * Рендерит заливку блока с заданным цветом
     */
    private void renderBlockFill(AxisAlignedBB box, int color) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        
        float[] rgba = ColorUtils.rgba(color);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        
        // Нижняя грань
        buffer.pos(box.minX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        // Верхняя грань
        buffer.pos(box.minX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        // Передняя грань (Z-)
        buffer.pos(box.minX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        // Задняя грань (Z+)
        buffer.pos(box.minX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        // Левая грань (X-)
        buffer.pos(box.minX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        // Правая грань (X+)
        buffer.pos(box.maxX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        Tessellator.getInstance().draw();
        
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    /**
     * Рендерит обводку блока с заданным цветом
     */
    private void renderBlockOutline(AxisAlignedBB box, int color) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        
        // Устанавливаем толщину линии
        GL11.glLineWidth(2.0f);
        
        float[] rgba = ColorUtils.rgba(color);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        
        // Нижние линии (Y = minY)
        buffer.pos(box.minX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        buffer.pos(box.maxX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        buffer.pos(box.maxX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        buffer.pos(box.minX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        // Верхние линии (Y = maxY)
        buffer.pos(box.minX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        buffer.pos(box.maxX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        buffer.pos(box.minX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        // Вертикальные линии (соединяют верх и низ)
        buffer.pos(box.minX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        buffer.pos(box.maxX, box.minY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        buffer.pos(box.maxX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        buffer.pos(box.minX, box.minY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        
        Tessellator.getInstance().draw();
        
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    @Override
    public boolean onDisable() {
        // Сбрасываем позиции при выключении модуля
        currentPos = null;
        targetPos = null;
        animMinX = 0;
        animMinY = 0;
        animMinZ = 0;
        animMaxX = 0;
        animMaxY = 0;
        animMaxZ = 0;
        return super.onDisable();
    }
}


