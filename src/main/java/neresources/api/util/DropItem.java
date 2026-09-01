package neresources.api.util;

import net.minecraft.Item;
import net.minecraft.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** One entry in a mob's drop table: what drops, how much of it, how often, and under what conditions. */
public class DropItem implements Comparable<DropItem> {
    public final ItemStack item;
    public final int minDrop;
    public final int maxDrop;
    public final float chance;
    public final List<String> conditionals = new ArrayList<String>();

    private final float sortIndex;

    /**
     * @param item   the dropped stack
     * @param chance how often it drops at all, {@code 0..1}
     */
    public DropItem(ItemStack item, float chance, Conditional... conditionals) {
        this(item, 1, 1, chance, conditionals);
    }

    /** A drop that always happens, in a quantity between {@code minDrop} and {@code maxDrop}. */
    public DropItem(ItemStack item, int minDrop, int maxDrop, Conditional... conditionals) {
        this(item, minDrop, maxDrop, 1.0F, conditionals);
    }

    /**
     * @param item    the dropped stack
     * @param minDrop smallest quantity dropped
     * @param maxDrop largest quantity dropped
     * @param chance  how often it drops at all, {@code 0..1}
     */
    public DropItem(ItemStack item, int minDrop, int maxDrop, float chance, Conditional... conditionals) {
        this.item = item;
        this.minDrop = minDrop;
        this.maxDrop = maxDrop;
        this.chance = chance;
        this.sortIndex = Math.min(chance, 1.0F) * (minDrop + maxDrop);
        for (Conditional conditional : conditionals) {
            this.conditionals.add(conditional.toString());
        }
    }

    public DropItem(Item item, int minDrop, int maxDrop, Conditional... conditionals) {
        this(new ItemStack(item), minDrop, maxDrop, 1.0F, conditionals);
    }

    public DropItem(Item item, int minDrop, int maxDrop, float chance, Conditional... conditionals) {
        this(new ItemStack(item), minDrop, maxDrop, chance, conditionals);
    }

    public DropItem(Item item, int subtype, int minDrop, int maxDrop, Conditional... conditionals) {
        this(new ItemStack(item, 1, subtype), minDrop, maxDrop, 1.0F, conditionals);
    }

    public DropItem(Item item, int subtype, int minDrop, int maxDrop, float chance, Conditional... conditionals) {
        this(new ItemStack(item, 1, subtype), minDrop, maxDrop, chance, conditionals);
    }

    public void addConditionals(List<String> extra) {
        this.conditionals.addAll(extra);
    }

    /** @return the quantity and chance as shown next to the item, e.g. {@code "0-2 (25%)"}. */
    @Override
    public String toString() {
        String amount = minDrop == maxDrop ? String.valueOf(minDrop) : minDrop + "-" + maxDrop;
        return amount + formatChance();
    }

    private String formatChance() {
        if (chance >= 1.0F) {
            return "";
        }
        float percent = chance * 100.0F;
        String formatted = percent < 10.0F
                ? String.format("%.1f", percent)
                : String.format("%d", (int) percent);
        return " (" + formatted.replace(',', '.') + "%)";
    }

    /** Most-likely, largest drops first. */
    @Override
    public int compareTo(DropItem other) {
        return Float.compare(other.sortIndex, this.sortIndex);
    }
}
