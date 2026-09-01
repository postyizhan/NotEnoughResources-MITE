package neresources.emi.widget;

import net.minecraft.EntityBat;
import net.minecraft.EntityDragon;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntitySquid;
import net.minecraft.Minecraft;
import net.minecraft.OpenGlHelper;
import net.minecraft.RenderHelper;
import net.minecraft.RenderManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * Renders a live entity model into the recipe area, ported from NotEnoughResources.
 * <p>
 * The entity's rotation fields are written before rendering and put back afterwards, because these
 * are the same instances every frame and EMI is not the only thing that may look at them. The flying
 * and swimming mobs need extra rotation to be recognisable at rest.
 */
public final class EntityRenderHelper {
    private EntityRenderHelper() {
    }

    /**
     * @param x      screen position to draw at
     * @param y      screen position of the entity's feet
     * @param scale  model scale; smaller mobs need a larger number
     * @param yaw    horizontal look offset, usually derived from the mouse
     * @param pitch  vertical look offset
     * @param entity the mob to draw, which may have been built with a null world
     */
    public static void render(int x, int y, float scale, float yaw, float pitch, EntityLivingBase entity) {
        if (entity == null) {
            return;
        }
        // Rendering reads worldObj for lighting; a null-world entity borrows the client's world.
        if (entity.worldObj == null) {
            entity.worldObj = Minecraft.getMinecraft().theWorld;
            if (entity.worldObj == null) {
                return;
            }
        }

        float renderYawOffset = entity.renderYawOffset;
        float rotationYaw = entity.rotationYaw;
        float rotationPitch = entity.rotationPitch;
        float rotationYawHead = entity.rotationYawHead;
        float prevRotationYawHead = entity.prevRotationYawHead;

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(x, y, 50.0F);
            GL11.glScalef(-scale, scale, scale);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            RenderHelper.enableStandardItemLighting();

            float adjustedPitch = pitch;
            if (entity instanceof EntityDragon || entity instanceof EntityBat) {
                GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                adjustedPitch = entity instanceof EntityDragon ? 20.0F - pitch : -pitch;
                if (entity instanceof EntityDragon) {
                    GL11.glRotatef(yaw < 90.0F ? (yaw < -90.0F ? 90.0F : -yaw) : -90.0F, 0.0F, 1.0F, 0.0F);
                }
            }
            if (entity instanceof EntitySquid) {
                GL11.glRotatef(50.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
            }

            GL11.glRotatef(-((float) Math.atan(adjustedPitch / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
            entity.renderYawOffset = (float) Math.atan(yaw / 40.0F) * 20.0F;
            entity.rotationYaw = (float) Math.atan(yaw / 40.0F) * 40.0F;
            entity.rotationPitch = -((float) Math.atan(adjustedPitch / 40.0F)) * 20.0F;
            entity.rotationYawHead = entity.rotationYaw;
            entity.prevRotationYawHead = entity.rotationYaw;
            GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);

            RenderManager.instance.playerViewY = 180.0F;
            RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        } finally {
            entity.renderYawOffset = renderYawOffset;
            entity.rotationYaw = rotationYaw;
            entity.rotationPitch = rotationPitch;
            entity.rotationYawHead = rotationYawHead;
            entity.prevRotationYawHead = prevRotationYawHead;

            GL11.glPopMatrix();
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glDisable(GL11.GL_COLOR_MATERIAL);
            OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    /**
     * Picks a scale that keeps the mob inside the space available.
     *
     * @param maxHeight the height in pixels the model should fit within
     */
    public static float scaleFor(EntityLivingBase entity, int maxHeight) {
        float extent = Math.max(entity.width, entity.height);
        if (extent <= 0.0F) {
            return maxHeight;
        }
        return maxHeight / extent;
    }
}
