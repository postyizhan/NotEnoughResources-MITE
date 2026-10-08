package emiresources.entry;

import emiresources.api.util.DropItem;
import emiresources.api.util.LightLevel;
import emiresources.util.StackHelper;
import net.minecraft.EntityLivingBase;
import net.minecraft.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** One mob: what it is, where it spawns, and what it drops. */
public class MobEntry {
    private final EntityLivingBase entity;
    private final String nameKey;
    private final LightLevel lightLevel;
    private final List<DropItem> drops = new ArrayList<DropItem>();

    /**
     * @param entity     an instance built with a null world, used only for rendering and its size
     * @param nameKey    translation key for the mob's name
     * @param lightLevel the light the mob needs to spawn
     */
    public MobEntry(EntityLivingBase entity, String nameKey, LightLevel lightLevel, DropItem... drops) {
        this.entity = entity;
        this.nameKey = nameKey;
        this.lightLevel = lightLevel;
        this.drops.addAll(Arrays.asList(drops));
        Collections.sort(this.drops);
    }

    public EntityLivingBase getEntity() {
        return entity;
    }

    public String getNameKey() {
        return nameKey;
    }

    public LightLevel getLightLevel() {
        return lightLevel;
    }

    /** @return the experience the mob is worth, read from the entity itself. */
    public int getExperience() {
        try {
            return entity.getExperienceValue();
        } catch (Throwable ignored) {
            // getExperienceValue can consult entity state that a null-world instance lacks.
            return 0;
        }
    }

    public List<DropItem> getDrops() {
        return drops;
    }

    /** Adds a drop, ignoring it if the same item is already listed. */
    public boolean addDrop(DropItem drop) {
        for (DropItem existing : drops) {
            if (StackHelper.isSameItem(existing.item, drop.item)) {
                return false;
            }
        }
        drops.add(drop);
        Collections.sort(drops);
        return true;
    }

    public boolean dropsItem(ItemStack stack) {
        for (DropItem drop : drops) {
            if (StackHelper.isSameItem(drop.item, stack)) {
                return true;
            }
        }
        return false;
    }
}
