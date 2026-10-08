package emiresources.mite;

import emiresources.EmiResources;
import emiresources.entry.TradeEntry;
import emiresources.entry.TradeEntry.Slot;
import net.minecraft.Block;
import net.minecraft.Enchantment;
import net.minecraft.EnchantmentData;
import net.minecraft.EntityVillager;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Every trade a MITE villager can offer, transcribed from {@code addDefaultEquipmentAndRecipies}.
 * <p>
 * That method is private and hands out one random trade per call, so it cannot be asked for the full
 * set. Which items each profession deals in is therefore written out here, while the amounts are read
 * from MITE's own {@code villagerStockList} and {@code blacksmithSellingList} so retuned prices carry
 * over without changes to this file.
 * <p>
 * MITE has no trade levels and no wandering trader: a villager starts with one trade and unlocks the
 * next after the last one is used, drawn from the same shuffled pool. Every trade allows seven uses.
 */
public final class MITETradeData {
    /** {@code MerchantRecipe} fixes this in its constructor; MITE never varies it. */
    public static final int MAX_USES = 7;

    public static final int FARMER = 0;
    public static final int LIBRARIAN = 1;
    public static final int PRIEST = 2;
    public static final int SMITH = 3;
    public static final int BUTCHER = 4;

    private MITETradeData() {
    }

    /** @return one entry per possible trade; any that fails to build is skipped with a warning. */
    public static List<TradeEntry> collect() {
        List<TradeEntry> trades = new ArrayList<TradeEntry>();
        farmer(trades);
        librarian(trades);
        priest(trades);
        smith(trades);
        butcher(trades);
        return trades;
    }

    /**
     * A trade from {@code addMerchantItem}: some number of one item buys a single emerald. The count
     * was already clamped to the item's stack limit when the table was built.
     */
    private static TradeEntry stockTrade(int profession, int itemId) {
        Tuple tuple = stockTuple(itemId);
        int[] amount = tuple == null ? new int[]{1, 1} : range(tuple);
        return new TradeEntry(profession,
                new Slot(new ItemStack(itemId, 1, 0), amount[0], amount[1]),
                null,
                Slot.fixed(new ItemStack(Item.emerald), 1),
                null);
    }

    /**
     * A trade from {@code addBlacksmithItem}. A negative amount means one emerald buys that many
     * items; a positive one means that many emeralds buy a single item. Either way the varying side is
     * clamped to its stack limit, as MITE does when it builds the recipe.
     */
    private static TradeEntry smithTrade(int profession, int itemId) {
        Tuple tuple = smithTuple(itemId);
        int[] amount = tuple == null ? new int[]{1, 1} : range(tuple);
        ItemStack item = new ItemStack(itemId, 1, 0);

        if (amount[1] < 0) {
            int low = Math.min(-amount[1], item.getMaxStackSize());
            int high = Math.min(-amount[0], item.getMaxStackSize());
            return new TradeEntry(profession,
                    Slot.fixed(new ItemStack(Item.emerald), 1),
                    null,
                    new Slot(item, low, high),
                    null);
        }

        int limit = new ItemStack(Item.emerald).getMaxStackSize();
        return new TradeEntry(profession,
                new Slot(new ItemStack(Item.emerald), Math.min(amount[0], limit), Math.min(amount[1], limit)),
                null,
                Slot.fixed(item, 1),
                null);
    }

    /** The longest list: copper and iron tools, then their plate and chain armour. */
    private static void smith(List<TradeEntry> trades) {
        add(trades, () -> stockTrade(SMITH, Item.coal.itemID));
        add(trades, () -> stockTrade(SMITH, Item.ingotIron.itemID));
        add(trades, () -> stockTrade(SMITH, Item.ingotGold.itemID));

        Item[] wares = {
                Item.swordIron, Item.axeIron, Item.pickaxeIron, Item.shovelIron, Item.hoeIron,
                Item.helmetIron, Item.plateIron, Item.legsIron, Item.bootsIron,
                Item.pickaxeCopper, Item.shovelCopper, Item.axeCopper, Item.hoeCopper,
                Item.daggerCopper, Item.swordCopper, Item.daggerIron,
                Item.helmetCopper, Item.plateCopper, Item.legsCopper, Item.bootsCopper,
                Item.helmetChainCopper, Item.plateChainCopper, Item.legsChainCopper, Item.bootsChainCopper,
                Item.helmetChainIron, Item.plateChainIron, Item.legsChainIron, Item.bootsChainIron,
        };
        for (Item ware : wares) {
            final Item item = ware;
            add(trades, () -> smithTrade(SMITH, item.itemID));
        }
    }

    private static void butcher(List<TradeEntry> trades) {
        add(trades, () -> stockTrade(BUTCHER, Item.coal.itemID));
        add(trades, () -> stockTrade(BUTCHER, Item.porkRaw.itemID));
        add(trades, () -> stockTrade(BUTCHER, Item.beefRaw.itemID));
        add(trades, () -> stockTrade(BUTCHER, Item.lambchopRaw.itemID));

        add(trades, () -> smithTrade(BUTCHER, Item.saddle.itemID));
        add(trades, () -> smithTrade(BUTCHER, Item.plateLeather.itemID));
        add(trades, () -> smithTrade(BUTCHER, Item.bootsLeather.itemID));
        add(trades, () -> smithTrade(BUTCHER, Item.helmetLeather.itemID));
        add(trades, () -> smithTrade(BUTCHER, Item.legsLeather.itemID));
        add(trades, () -> smithTrade(BUTCHER, Item.porkCooked.itemID));
        add(trades, () -> smithTrade(BUTCHER, Item.beefCooked.itemID));
        add(trades, () -> smithTrade(BUTCHER, Item.lambchopCooked.itemID));
    }

    private static void priest(List<TradeEntry> trades) {
        add(trades, () -> smithTrade(PRIEST, Item.eyeOfEnder.itemID));
        add(trades, () -> smithTrade(PRIEST, Item.redstone.itemID));
        add(trades, () -> smithTrade(PRIEST, Block.glowStone.blockID));

        // Hand over a weapon or a piece of armour with 2 + nextInt(3) emeralds and it comes back
        // enchanted, at a strength of 5 + nextInt(15). The result is shown unenchanted because the
        // enchantment is rolled per trade.
        Item[] enchantable = {
                Item.swordCopper, Item.swordIron,
                Item.plateCopper, Item.plateIron,
                Item.axeCopper, Item.axeIron,
                Item.pickaxeCopper, Item.pickaxeIron,
        };
        for (Item item : enchantable) {
            final Item equipment = item;
            add(trades, () -> new TradeEntry(PRIEST,
                    Slot.fixed(new ItemStack(equipment), 1),
                    new Slot(new ItemStack(Item.emerald), 2, 4),
                    Slot.fixed(new ItemStack(equipment), 1),
                    "emir.trade.enchantService"));
        }
    }

    private static void librarian(List<TradeEntry> trades) {
        add(trades, () -> stockTrade(LIBRARIAN, Item.paper.itemID));
        add(trades, () -> stockTrade(LIBRARIAN, Item.book.itemID));
        add(trades, () -> stockTrade(LIBRARIAN, Item.writtenBook.itemID));

        add(trades, () -> smithTrade(LIBRARIAN, Block.bookShelf.blockID));
        add(trades, () -> smithTrade(LIBRARIAN, Block.glass.blockID));
        add(trades, () -> smithTrade(LIBRARIAN, Item.compass.itemID));
        add(trades, () -> smithTrade(LIBRARIAN, Item.pocketSundial.itemID));

        enchantedBooks(trades);
    }

    /**
     * A book and a pile of emeralds for an enchanted book, one entry per enchantment and level.
     * <p>
     * Every enchantment MITE registers can appear, since {@code enchantmentsBookList} is just the
     * whole registry with the gaps removed. The price is {@code 2 + nextInt(5 + level * 10) + 3 * level},
     * and unlike the table-driven trades it is never clamped, so a high level can ask for more emeralds
     * than fit in a stack.
     */
    private static void enchantedBooks(List<TradeEntry> trades) {
        for (Enchantment enchantment : Enchantment.enchantmentsBookList) {
            if (enchantment == null) {
                continue;
            }
            for (int level = 1; level <= enchantment.getNumLevels(); level++) {
                final Enchantment book = enchantment;
                final int bookLevel = level;
                add(trades, () -> new TradeEntry(LIBRARIAN,
                        Slot.fixed(new ItemStack(Item.book), 1),
                        new Slot(new ItemStack(Item.emerald), 2 + 3 * bookLevel, 6 + 13 * bookLevel),
                        Slot.fixed(Item.enchantedBook.getEnchantedItemStack(
                                new EnchantmentData(book, bookLevel)), 1),
                        null));
            }
        }
    }

    /** Wool is stocked under its block id, which is how MITE keys it in the table. */
    private static void farmer(List<TradeEntry> trades) {
        add(trades, () -> stockTrade(FARMER, Item.wheat.itemID));
        add(trades, () -> stockTrade(FARMER, Block.cloth.blockID));
        add(trades, () -> stockTrade(FARMER, Item.chickenRaw.itemID));
        add(trades, () -> stockTrade(FARMER, Item.fishCooked.itemID));

        add(trades, () -> smithTrade(FARMER, Item.bread.itemID));
        add(trades, () -> smithTrade(FARMER, Item.melon.itemID));
        add(trades, () -> smithTrade(FARMER, Item.appleRed.itemID));
        add(trades, () -> smithTrade(FARMER, Item.cookie.itemID));
        add(trades, () -> smithTrade(FARMER, Item.shears.itemID));
        add(trades, () -> smithTrade(FARMER, Item.flintAndSteel.itemID));
        add(trades, () -> smithTrade(FARMER, Item.chickenCooked.itemID));
        add(trades, () -> smithTrade(FARMER, Item.arrowFlint.itemID));

        // The one trade with a hardcoded price: four gravel and an emerald for 4 + nextInt(2) flint.
        add(trades, () -> new TradeEntry(FARMER,
                Slot.fixed(new ItemStack(Block.gravel), 4),
                Slot.fixed(new ItemStack(Item.emerald), 1),
                new Slot(new ItemStack(Item.flint), 4, 5),
                null));
    }

    /**
     * The inclusive count range MITE rolls for a table entry. The tables store the bounds of
     * {@code min + nextInt(max - min)}, which never reaches {@code max}, and collapses to {@code min}
     * when the bounds are already touching.
     */
    private static int[] range(Tuple tuple) {
        int min = (Integer) tuple.getFirst();
        int max = (Integer) tuple.getSecond();
        return min >= max ? new int[]{min, min} : new int[]{min, max - 1};
    }

    private static Tuple stockTuple(int itemId) {
        return (Tuple) ((Map) EntityVillager.villagerStockList).get(itemId);
    }

    private static Tuple smithTuple(int itemId) {
        return (Tuple) ((Map) EntityVillager.blacksmithSellingList).get(itemId);
    }

    private static void add(List<TradeEntry> trades, Supplier<TradeEntry> supplier) {
        try {
            TradeEntry entry = supplier.get();
            if (entry != null) {
                trades.add(entry);
            }
        } catch (Throwable t) {
            EmiResources.LOGGER.warn("Could not build a villager trade entry", t);
        }
    }
}
