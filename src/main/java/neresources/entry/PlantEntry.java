package neresources.entry;

import neresources.api.util.PlantDrop;
import neresources.util.StackHelper;
import net.minecraft.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** One plant and what harvesting it yields. */
public class PlantEntry {
    private final ItemStack plant;
    private final String nameKey;
    private final List<PlantDrop> drops = new ArrayList<PlantDrop>();
    private int totalWeight;

    /**
     * @param plant   the plant as an item, for the slot and for lookups
     * @param nameKey translation key for a note about the plant, or null for none
     */
    public PlantEntry(ItemStack plant, String nameKey, PlantDrop... drops) {
        this.plant = plant;
        this.nameKey = nameKey;
        this.drops.addAll(Arrays.asList(drops));
        for (PlantDrop drop : this.drops) {
            totalWeight += drop.getWeight();
        }
    }

    public ItemStack getPlant() {
        return plant;
    }

    /** @return a translation key describing the plant's harvest rules, or null. */
    public String getNameKey() {
        return nameKey;
    }

    public List<PlantDrop> getDrops() {
        return drops;
    }

    /** @return the combined weight of the weighted drops, for turning weights into percentages. */
    public int getTotalWeight() {
        return totalWeight;
    }

    public boolean dropsItem(ItemStack stack) {
        for (PlantDrop drop : drops) {
            if (StackHelper.isSameItem(drop.getDrop(), stack)) {
                return true;
            }
        }
        return false;
    }
}
