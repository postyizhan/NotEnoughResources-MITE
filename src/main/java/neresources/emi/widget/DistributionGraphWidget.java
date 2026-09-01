package neresources.emi.widget;

import neresources.api.util.ColorHelper;
import net.minecraft.Minecraft;
import net.minecraft.ScaledResolution;
import org.lwjgl.opengl.GL11;

/**
 * Draws the ore distribution curve.
 * <p>
 * {@code EmiDrawContext} has no line primitive, so this drops to LWJGL immediate mode the same way
 * NotEnoughResources did. Every GL state this touches is restored before returning: leaving
 * {@code GL_TEXTURE_2D} disabled or a stale colour bound would corrupt the EMI widgets drawn after
 * this one, which looks like a bug in EMI rather than here.
 */
public final class DistributionGraphWidget {
    private DistributionGraphWidget() {
    }

    /**
     * Draws the axes and the curve.
     *
     * @param originX  left edge of the plot, in the recipe's coordinate space
     * @param originY  the baseline, with the curve rising above it
     * @param width    length of the horizontal axis
     * @param height   height of the vertical axis
     * @param values   one chance per Y level, drawn left to right
     * @param maxValue the value that reaches the top of the plot
     * @param colour   the curve colour, 0xAARRGGBB
     */
    public static void draw(int originX, int originY, int width, int height,
                            float[] values, float maxValue, int colour) {
        if (values == null || values.length < 2 || maxValue <= 0.0F) {
            return;
        }

        float scale = guiScale();

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_LINE_BIT);
        try {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glLineWidth(Math.max(1.0F, scale));

            drawAxes(originX, originY, width, height, scale);
            drawCurve(originX, originY, width, height, values, maxValue, colour);
        } finally {
            // glPopAttrib restores the enable/colour/line state; the colour and texturing are also set
            // explicitly because EMI's own drawing assumes opaque white with texturing enabled.
            GL11.glPopAttrib();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }

    private static void drawAxes(int originX, int originY, int width, int height, float scale) {
        setColour(ColorHelper.GRAY);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(originX, originY);
        GL11.glVertex2f(originX + width, originY);
        GL11.glVertex2f(originX, originY);
        GL11.glVertex2f(originX, originY - height);
        GL11.glEnd();

        arrowHead(originX + width, originY, 0.0F);
        arrowHead(originX, originY - height, -90.0F);
    }

    /** A small filled triangle at the tip of an axis, rotated to point along it. */
    private static void arrowHead(float x, float y, float degrees) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);
        GL11.glRotatef(degrees, 0.0F, 0.0F, 1.0F);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(3.0F, 0.0F);
        GL11.glVertex2f(0.0F, -1.5F);
        GL11.glVertex2f(0.0F, 1.5F);
        GL11.glEnd();
        GL11.glPopMatrix();
    }

    private static void drawCurve(int originX, int originY, int width, int height,
                                  float[] values, float maxValue, int colour) {
        setColour(colour);
        float step = (float) width / (values.length - 1);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i < values.length; i++) {
            float x = originX + i * step;
            float y = originY - Math.min(values[i] / maxValue, 1.0F) * height;
            GL11.glVertex2f(x, y);
        }
        GL11.glEnd();
    }

    private static void setColour(int colour) {
        GL11.glColor4f(ColorHelper.getRed(colour), ColorHelper.getGreen(colour),
                ColorHelper.getBlue(colour), ColorHelper.getAlpha(colour));
    }

    /** Lines are widened by the GUI scale so the curve stays visible when the GUI is scaled up. */
    private static float guiScale() {
        try {
            Minecraft client = Minecraft.getMinecraft();
            return new ScaledResolution(client.gameSettings, client.displayWidth, client.displayHeight)
                    .getScaleFactor();
        } catch (Throwable ignored) {
            return 1.0F;
        }
    }
}
