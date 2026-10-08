package emiresources.entry;

import net.minecraft.ItemStack;

/**
 * One villager trade: up to two things the player hands over, and the one thing they get back.
 * <p>
 * MITE picks most trade amounts at random within a range, so each of the three slots carries a
 * {@code min}/{@code max} pair rather than a single count. Only one slot varies in any given trade,
 * but which one differs: {@code addMerchantItem} varies what the player pays, the negative branch of
 * {@code addBlacksmithItem} varies what they receive, and the positive branch varies the emerald
 * price. Ranges are inclusive, already accounting for MITE's exclusive {@code nextInt} bound.
 */
public class TradeEntry {
    private final int profession;
    private final Slot first;
    private final Slot second;
    private final Slot sell;
    private final String noteKey;

    /**
     * @param profession MITE's profession id, 0-4
     * @param second     the second thing the player pays, or null for a single-input trade
     * @param noteKey    translation key for a caveat such as a random enchantment, or null for none
     */
    public TradeEntry(int profession, Slot first, Slot second, Slot sell, String noteKey) {
        this.profession = profession;
        this.first = first;
        this.second = second;
        this.sell = sell;
        this.noteKey = noteKey;
    }

    public int getProfession() {
        return profession;
    }

    public Slot getFirst() {
        return first;
    }

    /** @return the second thing the player pays, or null for a single-input trade. */
    public Slot getSecond() {
        return second;
    }

    public Slot getSell() {
        return sell;
    }

    /** @return a translation key describing what varies about this trade, or null. */
    public String getNoteKey() {
        return noteKey;
    }

    /** An item together with the range of counts MITE may ask for or hand out. */
    public static class Slot {
        private final ItemStack stack;
        private final int min;
        private final int max;

        public Slot(ItemStack stack, int min, int max) {
            this.stack = stack;
            this.min = Math.min(min, max);
            this.max = Math.max(min, max);
        }

        /** A slot whose count MITE does not vary. */
        public static Slot fixed(ItemStack stack, int count) {
            return new Slot(stack, count, count);
        }

        /**
         * The stack as shown when only one count fits. Uses {@link #getMin()} so the number on the
         * item is one the player can actually be asked for.
         */
        public ItemStack getStack() {
            ItemStack shown = stack.copy();
            shown.stackSize = min;
            return shown;
        }

        public ItemStack getRawStack() {
            return stack;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        public boolean isRange() {
            return min != max;
        }
    }
}
