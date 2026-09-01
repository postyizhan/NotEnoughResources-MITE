package neresources.mite;

import neresources.NEResources;
import neresources.api.util.PlantDrop;
import neresources.entry.PlantEntry;
import net.minecraft.Block;
import net.minecraft.Item;
import net.minecraft.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * MITE's plant drops, transcribed from each block's {@code dropBlockAsEntityItem}.
 * <p>
 * The crops share {@code BlockCrops}: an immature plant returns its seed, and a mature one returns
 * {@code getMatureYield()} of its crop, scaled by the Harvesting enchantment. Carrots, potatoes and
 * onions override the yield with {@code Math.random() < 0.25 ? 3 : 2}. A blighted crop drops nothing,
 * and a blighted or immature potato drops a poisonous one instead.
 */
public final class MITEPlantData {
    private MITEPlantData() {
    }

    /** @return one entry per plant; any that fails to build is skipped with a warning. */
    public static List<PlantEntry> collect() {
        List<PlantEntry> plants = new ArrayList<PlantEntry>();

        // BlockTallGrass: seeds on a 20% roll, and only when a player harvested it.
        add(plants, () -> new PlantEntry(new ItemStack(Block.tallGrass, 1, 1), "ner.plant.tallGrass",
                new PlantDrop(new ItemStack(Item.seeds), 0.2F)));

        // BlockCrops: wheat with getMatureYield() == 1, or one seed while still growing.
        add(plants, () -> new PlantEntry(new ItemStack(Item.wheat), "ner.plant.crop",
                new PlantDrop(new ItemStack(Item.wheat), 1, 1),
                new PlantDrop(new ItemStack(Item.seeds), 1, 1)));

        // BlockCarrot: getMatureYield() is 2, or 3 a quarter of the time.
        add(plants, () -> new PlantEntry(new ItemStack(Item.carrot), "ner.plant.crop",
                new PlantDrop(new ItemStack(Item.carrot), 2, 3)));

        // BlockPotato: same yield, plus a poisonous potato from a blighted or immature plant.
        add(plants, () -> new PlantEntry(new ItemStack(Item.potato), "ner.plant.potato",
                new PlantDrop(new ItemStack(Item.potato), 2, 3),
                new PlantDrop(new ItemStack(Item.poisonousPotato), 1, 1)));

        // BlockOnion: same yield as carrots.
        add(plants, () -> new PlantEntry(new ItemStack(Item.onion), "ner.plant.crop",
                new PlantDrop(new ItemStack(Item.onion), 2, 3)));

        // BlockMelon: four melon slices, or seeds if an explosion broke it.
        add(plants, () -> new PlantEntry(new ItemStack(Block.melon), "ner.plant.melon",
                new PlantDrop(new ItemStack(Item.melon), 4, 4),
                new PlantDrop(new ItemStack(Item.melonSeeds), 1, 1)));

        // BlockStem: a stem yields its own seed.
        add(plants, () -> new PlantEntry(new ItemStack(Item.melonSeeds), "ner.plant.stem",
                new PlantDrop(new ItemStack(Item.melonSeeds), 1, 1)));
        add(plants, () -> new PlantEntry(new ItemStack(Item.pumpkinSeeds), "ner.plant.stem",
                new PlantDrop(new ItemStack(Item.pumpkinSeeds), 1, 1)));

        return plants;
    }

    private static void add(List<PlantEntry> plants, Supplier<PlantEntry> supplier) {
        try {
            PlantEntry entry = supplier.get();
            if (entry.getPlant() != null && entry.getPlant().getItem() != null) {
                plants.add(entry);
            }
        } catch (Throwable t) {
            NEResources.LOGGER.warn("Could not build a plant entry", t);
        }
    }
}
