package neresources.util;

import net.minecraft.Block;
import net.minecraft.Item;
import net.minecraft.ItemStack;

/** Small helpers for building and comparing stacks without tripping over MITE's null-heavy registries. */
public final class StackHelper {
    private StackHelper() {
    }

    /**
     * Compares item identity, ignoring stack size and durability.
     * MITE has no instance-level {@code isItemEqual}, and its static {@code areItemStacksEqual}
     * compares more than we want.
     */
    public static boolean isSameItem(ItemStack a, ItemStack b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a.itemID != b.itemID) {
            return false;
        }
        return !a.getHasSubtypes() || a.getItemSubtype() == b.getItemSubtype();
    }

    /** @return a stack for the item id, or null if the id is not registered. */
    public static ItemStack of(int itemId, int subtype, int amount) {
        if (itemId < 1) {
            return null;
        }
        if (itemId < Item.itemsList.length && Item.itemsList[itemId] != null) {
            return new ItemStack(itemId, amount, subtype);
        }
        return null;
    }

    public static ItemStack of(Item item, int amount) {
        return item == null ? null : new ItemStack(item, amount);
    }

    public static ItemStack of(Block block, int amount) {
        return block == null ? null : new ItemStack(block, amount);
    }

    /** @return the localised item name, falling back to the raw id when the name is unavailable. */
    public static String nameOf(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return "?";
        }
        try {
            String name = stack.getDisplayName();
            return name == null || name.isEmpty() ? String.valueOf(stack.itemID) : name;
        } catch (Throwable ignored) {
            // Some items build their display name from world state we do not have here.
            return String.valueOf(stack.itemID);
        }
    }
}
