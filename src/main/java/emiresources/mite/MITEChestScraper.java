package emiresources.mite;

import net.minecraft.ComponentScatteredFeatureDesertPyramid;
import net.minecraft.ComponentStrongholdChestCorridor;
import net.minecraft.ComponentStrongholdLibrary;
import net.minecraft.ComponentStrongholdRoomCrossing;
import net.minecraft.ComponentVillageHouse2;
import net.minecraft.StructureMineshaftPieces;
import net.minecraft.WeightedRandomChestContent;
import net.minecraft.WorldGenDungeons;
import net.minecraft.WorldServer;
import emiresources.EmiResources;
import emiresources.entry.DungeonEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Collects MITE's geemirated-chest loot.
 * <p>
 * 1.6.4 has no Forge {@code ChestGenHooks}, so each structure keeps its own static
 * {@code WeightedRandomChestContent[]}; the access wideemir opens them for reading. The roll counts
 * are call-site constants, taken from where each structure invokes
 * {@code geemirateStructureChestContents}, and are noted next to each table below.
 * <p>
 * Enchanted books are absent on purpose: structures splice them in at geemiration time via
 * {@code Item.enchantedBook.func_92114_b(random)} rather than listing them in the table.
 */
public final class MITEChestScraper {
    private MITEChestScraper() {
    }

    /** @return one entry per chest kind; tables that fail to load are skipped with a warning. */
    public static List<DungeonEntry> scrape() {
        List<DungeonEntry> entries = new ArrayList<DungeonEntry>();

        // WorldGenDungeons: generateChestContents(..., 8, ...) — a flat 8 draws.
        add(entries, "emir.dungeon.dungeonChest", () -> WorldGenDungeons.field_111189_a, 8, 8);
        add(entries, "emir.dungeon.dungeonChestUnderworld", () -> WorldGenDungeons.chest_contents_for_underworld, 8, 8);
        // ComponentMineshaftCorridor: 3 + rand.nextInt(4)
        add(entries, "emir.dungeon.mineshaftCorridor", () -> StructureMineshaftPieces.mineshaftChestContents, 3, 6);
        // ComponentScatteredFeatureDesertPyramid: 2 + rand.nextInt(5)
        add(entries, "emir.dungeon.pyramidDesertChest", () -> ComponentScatteredFeatureDesertPyramid.itemsToGenerateInTemple, 2, 6);
        // ComponentStrongholdChestCorridor: 2 + rand.nextInt(2)
        add(entries, "emir.dungeon.strongholdCorridor", () -> ComponentStrongholdChestCorridor.strongholdChestContents, 2, 3);
        // ComponentStrongholdLibrary: 1 + rand.nextInt(4)
        add(entries, "emir.dungeon.strongholdLibrary", () -> ComponentStrongholdLibrary.strongholdLibraryChestContents, 1, 4);
        // ComponentStrongholdRoomCrossing: 1 + rand.nextInt(4)
        add(entries, "emir.dungeon.strongholdCrossing", () -> ComponentStrongholdRoomCrossing.strongholdRoomCrossingChestContents, 1, 4);
        // ComponentVillageHouse2: 3 + rand.nextInt(6)
        add(entries, "emir.dungeon.villageBlacksmith", () -> ComponentVillageHouse2.villageBlacksmithChestContents, 3, 8);
        // WorldServer: new WorldGeneratorBonusChest(bonusChestContent, 10)
        add(entries, "emir.dungeon.bonusChest", () -> WorldServer.bonusChestContent, 10, 10);

        return entries;
    }

    /**
     * Reads one loot table. Each is wrapped separately because touching these fields runs the owning
     * class's static initialiser, which happens here on EMI's reload thread rather than during world
     * geemiration.
     */
    private static void add(List<DungeonEntry> entries, String nameKey,
                            Supplier<WeightedRandomChestContent[]> table, int minRolls, int maxRolls) {
        try {
            WeightedRandomChestContent[] contents = table.get();
            if (contents == null || contents.length == 0) {
                EmiResources.LOGGER.warn("Chest loot table {} was empty; skipping.", nameKey);
                return;
            }
            DungeonEntry entry = new DungeonEntry(nameKey, contents, minRolls, maxRolls);
            if (!entry.isEmpty()) {
                entries.add(entry);
            }
        } catch (Throwable t) {
            EmiResources.LOGGER.warn("Could not read chest loot table {}", nameKey, t);
        }
    }
}
