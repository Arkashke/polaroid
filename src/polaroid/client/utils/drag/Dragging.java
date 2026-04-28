package polaroid.client.utils.drag;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.MainWindow;
import via.MouseUtils;
import polaroid.client.modules.api.Module;
import polaroid.client.utils.client.ClientUtil;
import polaroid.client.utils.client.Vec2i;
import polaroid.client.utils.render.DisplayUtils;

public class Dragging {
    @Expose
    @SerializedName("x")
    private float xPos;
    @Expose
    @SerializedName("y")
    private float yPos;
    public float initialXVal;
    public float initialYVal;
    private float startX;
    private float startY;
    private boolean dragging;
    private float width;
    private float height;
    @Expose
    @SerializedName("name")
    private final String name;
    private final Module Function;
    private static final float CENTER_LINE_WIDTH = 1.0F;
    private static final float SNAP_THRESHOLD = 10.0F;
    private float lineAlpha = 0.0F;
    private long lastUpdateTime;

    public Dragging(Module Function, String name, float initialXVal, float initialYVal) {
        this.Function = Function;
        this.name = name;
        this.xPos = initialXVal;
        this.yPos = initialYVal;
        this.initialXVal = initialXVal;
        this.initialYVal = initialYVal;
    }

    public Module getFunction() {
        return this.Function;
    }

    public String getName() {
        return this.name;
    }

    public float getWidth() {
        return this.width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return this.height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getX() {
        return this.xPos;
    }

    public void setX(float x) {
        this.xPos = x;
    }

    public float getY() {
        return this.yPos;
    }

    public void setY(float y) {
        this.yPos = y;
    }

    public final void onDraw(int mouseX, int mouseY, MainWindow res) {
        Vec2i fixed = ClientUtil.getMouse(mouseX, mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();
        float centerX = (float)res.scaledWidth() / 2.0F;
        float centerY = (float)res.scaledHeight() / 2.0F;
        if (this.dragging) {
            this.xPos = (float)mouseX - this.startX;
            this.yPos = (float)mouseY - this.startY;
            float quarterX = centerX / 2.0F;
            float threeQuarterX = centerX + centerX / 2.0F;
            float quarterY = centerY / 2.0F;
            float threeQuarterY = centerY + centerY / 2.0F;
            if (Math.abs(this.yPos + this.height / 2.0F - centerY) < 10.0F) {
                this.yPos = centerY - this.height / 2.0F;
            }

            if (Math.abs(this.xPos + this.width / 2.0F - centerX) < 10.0F) {
                this.xPos = centerX - this.width / 2.0F;
            }

            if (Math.abs(this.yPos + this.height / 2.0F - quarterY) < 10.0F) {
                this.yPos = quarterY - this.height / 2.0F;
            }

            if (Math.abs(this.yPos + this.height / 2.0F - threeQuarterY) < 10.0F) {
                this.yPos = threeQuarterY - this.height / 2.0F;
            }

            if (Math.abs(this.xPos + this.width / 2.0F - quarterX) < 10.0F) {
                this.xPos = quarterX - this.width / 2.0F;
            }

            if (Math.abs(this.xPos + this.width / 2.0F - threeQuarterX) < 10.0F) {
                this.xPos = threeQuarterX - this.width / 2.0F;
            }

            if (this.xPos + this.width > (float)res.scaledWidth()) {
                this.xPos = (float)res.scaledWidth() - this.width;
            }

            if (this.yPos + this.height > (float)res.scaledHeight()) {
                this.yPos = (float)res.scaledHeight() - this.height;
            }

            if (this.xPos < 0.0F) {
                this.xPos = 0.0F;
            }

            if (this.yPos < 0.0F) {
                this.yPos = 0.0F;
            }

            this.updateLineAlpha(true);
        } else {
            this.updateLineAlpha(false);
        }

        this.drawCenterLines(res);
    }

    private void updateLineAlpha(boolean increasing) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (float)(currentTime - this.lastUpdateTime) / 1000.0F;
        this.lastUpdateTime = currentTime;
        if (increasing) {
            this.lineAlpha += deltaTime * 2.0F;
            if (this.lineAlpha > 1.0F) {
                this.lineAlpha = 1.0F;
            }
        } else {
            this.lineAlpha -= deltaTime * 2.0F;
            if (this.lineAlpha < 0.0F) {
                this.lineAlpha = 0.0F;
            }
        }

    }

    private void drawCenterLines(MainWindow res) {
        if (this.lineAlpha > 0.0F) {
            float centerX = (float)res.scaledWidth() / 2.0F;
            float centerY = (float)res.scaledHeight() / 2.0F;
            int color = (int)(this.lineAlpha * 255.0F) << 24 | 16777215;
            DisplayUtils.drawRoundedRect(centerX - 0.5F, 0.0F, 1.0F, (float)res.scaledHeight(), 1.0F, color);
            DisplayUtils.drawRoundedRect(0.0F, centerY - 0.5F, (float)res.scaledWidth(), 1.0F, 1.0F, color);
            float quarterY = centerY / 2.0F;
            DisplayUtils.drawRoundedRect(0.0F, quarterY - 0.5F, (float)res.scaledWidth(), 1.0F, 1.0F, color);
            float threeQuarterY = centerY + centerY / 2.0F;
            DisplayUtils.drawRoundedRect(0.0F, threeQuarterY - 0.5F, (float)res.scaledWidth(), 1.0F, 1.0F, color);
            float quarterX = centerX / 2.0F;
            DisplayUtils.drawRoundedRect(quarterX - 0.5F, 0.0F, 1.0F, (float)res.scaledHeight(), 1.0F, color);
            float threeQuarterX = centerX + centerX / 2.0F;
            DisplayUtils.drawRoundedRect(threeQuarterX - 0.5F, 0.0F, 1.0F, (float)res.scaledHeight(), 1.0F, color);
        }

    }

    public final boolean onClick(double mouseX, double mouseY, int button) {
        Vec2i fixed = ClientUtil.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        if (button == 0 && DisplayUtils.isInRegion((float) mouseX, (float) mouseY, xPos, yPos, width, height)) {
            dragging = true;
            startX = (int) (mouseX - xPos);
            startY = (int) (mouseY - yPos);
            lastUpdateTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public final void onRelease(int button) {
        if (button == 0) {
            this.dragging = false;
        }

    }
}

