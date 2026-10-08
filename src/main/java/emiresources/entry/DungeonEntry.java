package emiresources.entry;

import net.minecraft.ItemStack;
import net.minecraft.WeightedRandomChestContent;
import emiresources.util.StackHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One kind of generated chest and what it can contain.
 * <p>
 * The number shown per item is the expected number of times that entry is drawn for one chest:
 * {@code averageRolls * weight / totalWeight}. For rare items this reads naturally as "the chance
 * this chest holds one"; for common ones it can exceed 100%, meaning more than one is expected.
 * <p>
 * MITE can also reject a drawn entry and redraw up to four times — tools and ingots are withheld
 * until a certain day of the world, and hoes and fishing rods are withheld below y=48
 * ({@code WeightedRandomChestContent.generateChestContents}). Those rules depend on world state, so
 * the figures here describe an unrestricted roll.
 */
public class DungeonEntry {
    private final String nameKey;
    private final int minRolls;
    private final int maxRolls;
    private final Map<ItemStack, Float> chestDrops = new LinkedHashMap<ItemStack, Float>();

    /**
     * @param nameKey  translation key for the chest's name
     * @param contents the structure's loot table
     * @param minRolls fewest draws made for one chest
     * @param maxRolls most draws made for one chest
     */
    public DungeonEntry(String nameKey, WeightedRandomChestContent[] contents, int minRolls, int maxRolls) {
        this.nameKey = nameKey;
        this.minRolls = minRolls;
        this.maxRolls = maxRolls;

        if (contents == null || contents.length == 0) {
            return;
        }

        int totalWeight = 0;
        for (WeightedRandomChestContent content : contents) {
            totalWeight += content.itemWeight;
        }
        if (totalWeight <= 0) {
            return;
        }

        float averageRolls = (minRolls + maxRolls) / 2.0F;
        List<WeightedRandomChestContent> sorted = new ArrayList<WeightedRandomChestContent>(Arrays.asList(contents));
        Collections.sort(sorted, (a, b) -> Integer.compare(b.itemWeight, a.itemWeight));

        for (WeightedRandomChestContent content : sorted) {
            ItemStack stack = content.theItemId;
            if (stack == null || stack.getItem() == null) {
                continue;
            }
            float chance = averageRolls * content.itemWeight / totalWeight;
            ItemStack display = withAverageQuantity(stack, content.min_quantity, content.max_quantity);
            // The same item can appear more than once with different weights, e.g. the two flint
            // hatchets in the bonus chest; show it once with the combined chance.
            ItemStack existing = findSameItem(display);
            if (existing == null) {
                chestDrops.put(display, chance);
            } else {
                chestDrops.put(existing, chestDrops.get(existing) + chance);
            }
        }
    }

    /** @return a copy of the stack sized to the average quantity, so the slot shows a useful count. */
    private static ItemStack withAverageQuantity(ItemStack stack, int minQuantity, int maxQuantity) {
        ItemStack display = stack.copy();
        display.stackSize = Math.max(1, (minQuantity + maxQuantity) / 2);
        return display;
    }

    private ItemStack findSameItem(ItemStack stack) {
        for (ItemStack existing : chestDrops.keySet()) {
            if (StackHelper.isSameItem(existing, stack)) {
                return existing;
            }
        }
        return null;
    }

    /** @return the translation key for this chest's name. */
    public String getNameKey() {
        return nameKey;
    }

    public int getMinRolls() {
        return minRolls;
    }

    public int getMaxRolls() {
        return maxRolls;
    }

    /** @return every possible item mapped to its expected count per chest, most common first. */
    public Map<ItemStack, Float> getChestDrops() {
        return chestDrops;
    }

    public List<ItemStack> getItemStacks() {
        return new ArrayList<ItemStack>(chestDrops.keySet());
    }

    public boolean isEmpty() {
        return chestDrops.isEmpty();
    }

    public boolean containsItem(ItemStack stack) {
        for (ItemStack candidate : chestDrops.keySet()) {
            if (StackHelper.isSameItem(candidate, stack)) {
                return true;
            }
        }
        return false;
    }
}
