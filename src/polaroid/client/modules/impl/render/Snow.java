package polaroid.client.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import polaroid.client.events.EventUpdate;
import polaroid.client.events.WorldEvent;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;

import polaroid.client.utils.math.MathUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import polaroid.client.utils.render.ColorUtils;

import java.util.concurrent.CopyOnWriteArrayList;

import static net.minecraft.client.renderer.WorldRenderer.frustum;

@ModuleSystem(name = "WorldParticles", type = Category.Render, description = "Визуальные частицы в мире", server = ServerCategory.NO)
public class Snow extends Module {
    private final ModeSetting fallModeSetting = new ModeSetting("Мод", "Простой", "Простой", "Отскоки", "Взлет");
    public static final ModeSetting setting = new ModeSetting("Вид", "Звездочки", "Звездочки", "Снежинки", "Орбизы");
    public final SliderSetting size = new SliderSetting("Количество", 350f, 100f, 5000f, 50f);
    private final ModeSetting colorMode = new ModeSetting("Цвет", "Клиентский", "Клиентский", "Кастомный");
    private final polaroid.client.modules.settings.impl.ColorSetting customColor = new polaroid.client.modules.settings.impl.ColorSetting("Свой цвет", -1).setVisible(() -> colorMode.is("Кастомный"));
    private final SliderSetting speed = new SliderSetting("Скорость", 0.4f, 0.1f, 2.0f, 0.1f);
    private final SliderSetting lifeTime = new SliderSetting("Длительность", 160f, 60f, 300f, 10f);
    public final SliderSetting spreadStrength = new SliderSetting("Сила разлета", 48.0f, 10.0f, 100.0f, 5.0f);
    MatrixStack matrixStack = new MatrixStack();
    private static final CopyOnWriteArrayList<ParticleBase> particles = new CopyOnWriteArrayList<>();
    private float dynamicSpeed = (fallModeSetting.is("Отскоки")) ? 0.1f : 0.4f;
    public Snow() {
        addSettings(fallModeSetting, setting, size, colorMode, customColor, speed, lifeTime, spreadStrength);
    }

    private boolean isInView(Vector3d pos) {
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x,
                mc.getRenderManager().info.getProjectedView().y,
                mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(new AxisAlignedBB(pos.add(-0.2, -0.2, -0.2), pos.add(0.2, 0.2, 0.2)));
    }
    @Subscribe
    private void onUpdate(EventUpdate e) {
        particles.removeIf(ParticleBase::tick);
        for (int n = particles.size(); (float) n < size.get(); ++n) {
            if (mc.currentScreen instanceof IngameMenuScreen) return;
            // Инвертируем spread: чем больше значение настройки, тем больше разлет
            float spread = 110.0f - spreadStrength.get();
            float yRange = fallModeSetting.is("Взлет") ? 0 : 48.0f; // Фиксированная высота спавна
            particles.add(new ParticleBase(
                (float) (mc.player.getPosX() + (double) MathUtil.random(-spread, spread)), 
                (float) (mc.player.getPosY() + (double) MathUtil.random(-20.0F, yRange)), 
                (float) (mc.player.getPosZ() + (double) MathUtil.random(-spread, spread)), 
                MathUtil.random(-dynamicSpeed, dynamicSpeed) * speed.get(), 
                MathUtil.random(-0.1F, 0.1F) * speed.get(), 
                MathUtil.random(-dynamicSpeed, dynamicSpeed) * speed.get()
            ));
        }
    }

    @Subscribe
    private void onRender(WorldEvent e) {
        render(matrixStack);
    }

    public static void render(MatrixStack matrixStack) {
        if (mc.currentScreen instanceof IngameMenuScreen) return;
        matrixStack.push();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
        particles.forEach((particleBase) -> {
            particleBase.render(bufferBuilder);
        });
        bufferBuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferBuilder);
        RenderSystem.depthMask(true);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        matrixStack.pop();
    }

    public class ParticleBase {
        public long time;
        protected float prevposX, prevposY, prevposZ;
        protected float posX, posY, posZ;
        protected float motionX, motionY, motionZ;
        protected int age, maxAge;
        private float alpha;
        private long collisionTime = -1L;

        public ParticleBase(float x, float y, float z, float motionX, float motionY, float motionZ) {
            this.posX = x;
            this.posY = y;
            this.posZ = z;
            this.prevposX = x;
            this.prevposY = y;
            this.prevposZ = z;
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
            this.time = System.currentTimeMillis();
            // maxAge используется для tick(), не зависит от настройки длительности
            this.maxAge = this.age = (int) MathUtil.random(120, 200);
        }

        public void update() {
            alpha = MathUtil.fast(alpha, 1, 10);
            if (fallModeSetting.is("Отскоки")) updateWithBounce();
        }

        public boolean tick() {
            // Проверяем время жизни частицы
            long elapsed = System.currentTimeMillis() - time;
            if (elapsed > lifeTime.get() * 1000) {
                return true; // Удаляем частицу
            }
            
            this.age = mc.player.getDistanceSq((double)this.posX, (double)this.posY, (double)this.posZ) > 4096.0 ? (this.age -= 8) : --this.age;
            if (this.age < 0) {
                return true;
            } else {
                this.prevposX = this.posX;
                this.prevposY = this.posY;
                this.prevposZ = this.posZ;
                this.posX += this.motionX;
                this.posY += this.motionY;
                this.posZ += this.motionZ;
                if (fallModeSetting.is("Простой")) {
                    this.motionX *= 0.9F;
                    this.motionY *= 0.9F;
                    this.motionZ *= 0.9F;
                    this.motionY -= 0.001F * speed.get();
                } else {
                    if (fallModeSetting.is("Взлет")) motionY += 0.1f * speed.get();
                    this.motionX = 0;
                    this.motionZ = 0;
                }
                return false;
            }
        }

        private void updateWithBounce() {
            if (this.collisionTime != -1L) {
                long timeSinceCollision = System.currentTimeMillis() - this.collisionTime;
                this.alpha = Math.max(0.0f, 1.0f - (float) timeSinceCollision / 3000.0f);
            }

            this.motionY -= 8.0E-4;
            float newPosX = this.posX + this.motionX;
            float newPosY = this.posY + this.motionY;
            float newPosZ = this.posZ + this.motionZ;

            BlockPos particlePos = new BlockPos(newPosX, newPosY, newPosZ);
            BlockState blockState = mc.world.getBlockState(particlePos);

            if (!blockState.isAir()) {
                if (this.collisionTime == -1L) {
                    this.collisionTime = System.currentTimeMillis();
                }

                if (!mc.world.getBlockState(new BlockPos(this.posX + this.motionX, this.posY, this.posZ)).isAir()) {
                    this.motionX = 0.0f;
                }
                if (!mc.world.getBlockState(new BlockPos(this.posX, this.posY + this.motionY, this.posZ)).isAir()) {
                    this.motionY = -this.motionY * 0.8f;
                }
                if (!mc.world.getBlockState(new BlockPos(this.posX, this.posY, this.posZ + this.motionZ)).isAir()) {
                    this.motionZ = 0.0f;
                }

                this.posX += this.motionX;
                this.posY += this.motionY;
                this.posZ += this.motionZ;
            } else {
                this.posX = newPosX;
                this.posY = newPosY;
                this.posZ = newPosZ;
            }
        }

        public void render(BufferBuilder bufferBuilder) {
            if (setting.is("Звездочки")) {
                mc.getTextureManager().bindTexture(new ResourceLocation("polaroid/images/modules/particle/star.png"));
            } else if (setting.is("Снежинки")) {
                mc.getTextureManager().bindTexture(new ResourceLocation("polaroid/images/modules/particle/snow.png"));
            } else if (setting.is("Орбизы")) {
                mc.getTextureManager().bindTexture(new ResourceLocation("polaroid/images/modules/particle/glow.png"));
            }

            // Плавная анимация уменьшения размера на основе времени жизни
            long elapsed = System.currentTimeMillis() - time;
            float maxLifeMs = lifeTime.get() * 1000;
            float sizeMultiplier = 1 - (elapsed / maxLifeMs);
            sizeMultiplier = Math.max(0, Math.min(1, sizeMultiplier)); // Clamp между 0 и 1
            
            update();
            ActiveRenderInfo camera = mc.gameRenderer.getActiveRenderInfo();
            
            // Определяем цвет с плавным затуханием альфы
            int color;
            if (colorMode.is("Кастомный")) {
                color = ColorUtils.setAlpha(customColor.get(), (int) (alpha * 255 * sizeMultiplier));
            } else {
                color = ColorUtils.setAlpha(Theme.getColor(0), (int) (alpha * 255 * sizeMultiplier));
            }
            
            Vector3d pos = MathUtil.interpolatePos(prevposX, prevposY, prevposZ, posX, posY, posZ);

            MatrixStack matrices = new MatrixStack();
            matrices.translate(pos.x, pos.y, pos.z);
            matrices.rotate(Vector3f.YP.rotationDegrees(-camera.getYaw()));
            matrices.rotate(Vector3f.XP.rotationDegrees(camera.getPitch()));

            Matrix4f matrix1 = matrices.getLast().getMatrix();
            
            // Фиксированный размер 0.9f
            float fixedSize = 0.9f;

            bufferBuilder.pos(matrix1, 0, -fixedSize * sizeMultiplier, 0).color(color).tex(0, 1).lightmap(0, 240).endVertex();
            bufferBuilder.pos(matrix1, -fixedSize * sizeMultiplier, -fixedSize * sizeMultiplier, 0).color(color).tex(1, 1).lightmap(0, 240).endVertex();
            bufferBuilder.pos(matrix1, -fixedSize * sizeMultiplier, 0, 0).color(color).tex(1, 0).lightmap(0, 240).endVertex();
            bufferBuilder.pos(matrix1, 0, 0, 0).color(color).tex(0, 0).lightmap(0, 240).endVertex();
        }
    }
}

