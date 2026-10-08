package emiresources.util;

import net.minecraft.Block;
import net.minecraft.Item;
import net.minecraft.ItemStack;

/**
 * Registry keys for item stacks.
 * <p>
 * NotEnoughResources keyed everything by Forge ore dictionary name so that, say, every mod's copper
 * ore shared one graph. MITE has no ore dictionary and a single fixed item set, so the key is just
 * the item id plus its subtype. Durability is deliberately ignored: a damaged pickaxe should look up
 * the same entries as a fresh one.
 */
public final class MapKeys {
    private MapKeys() {
    }

    /** @return a stable key for the stack, or null if the stack is empty or malformed. */
    public static String key(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return null;
        }
        return stack.getHasSubtypes()
                ? stack.itemID + ":" + stack.getItemSubtype()
                : String.valueOf(stack.itemID);
    }

    public static String key(Item item) {
        return item == null ? null : String.valueOf(item.itemID);
    }

    public static String key(Block block) {
        return block == null ? null : String.valueOf(block.blockID);
    }
}
