package emiresources.probe;

import net.minecraft.Block;
import net.minecraft.ComponentScatteredFeatureDesertPyramid;
import net.minecraft.ComponentStrongholdChestCorridor;
import net.minecraft.ComponentStrongholdLibrary;
import net.minecraft.ComponentStrongholdRoomCrossing;
import net.minecraft.ComponentVillageHouse2;
import net.minecraft.EntityBat;
import net.minecraft.EntityBlaze;
import net.minecraft.EntityChicken;
import net.minecraft.EntityCow;
import net.minecraft.EntityCreeper;
import net.minecraft.EntityEarthElemental;
import net.minecraft.EntityEnderman;
import net.minecraft.EntityGhast;
import net.minecraft.EntityHorse;
import net.minecraft.EntityIronGolem;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityMagmaCube;
import net.minecraft.EntityPig;
import net.minecraft.EntitySheep;
import net.minecraft.EntitySkeleton;
import net.minecraft.EntitySpider;
import net.minecraft.EntitySquid;
import net.minecraft.EntityWitch;
import net.minecraft.EntityWither;
import net.minecraft.EntityWolf;
import net.minecraft.EntityZombie;
import net.minecraft.Item;
import net.minecraft.StructureMineshaftPieces;
import net.minecraft.WeightedRandomChestContent;
import net.minecraft.WorldGenDungeons;
import net.minecraft.WorldServer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.emi.emi.api.EmiPlugin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import emiresources.api.util.DropItem;
import emiresources.api.util.PlantDrop;
import emiresources.entry.DungeonEntry;
import emiresources.entry.MobEntry;
import emiresources.entry.OreMatchEntry;
import emiresources.entry.PlantEntry;
import emiresources.entry.TradeEntry;
import emiresources.mite.MITEChestScraper;
import emiresources.mite.MITEMobData;
import emiresources.mite.MITEOreScraper;
import emiresources.mite.MITEPlantData;
import emiresources.mite.MITETradeData;
import emiresources.util.StackHelper;
import net.minecraft.Enchantment;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A development diagnostic, run with {@code ./gradlew probe}. Not part of the mod's behaviour and
 * never referenced at runtime.
 * <p>
 * EMI builds its recipes on a background reload thread, which means this mod reads MITE's static loot
 * tables and constructs entities at a time and on a thread MITE never anticipated. Both are the kind
 * of thing that fails with an {@code ExceptionInInitializerError} pointing deep into MITE, so this
 * probe exercises them in isolation and reports exactly which ones survive.
 * <p>
 * A failure here is real. A pass is good evidence but not proof, because the full client applies FML
 * transformations this probe does not.
 */
public final class HeadlessProbe {
    private static int passed;
    private static int failed;

    private HeadlessProbe() {
    }

    public static void main(String[] args) {
        System.out.println("=== entrypoints declared in fml.mod.json ===");
        checkEntrypoints();

        System.out.println();
        System.out.println("=== MITE registry bootstrap ===");
        check("Block.<clinit>", () -> Block.stone.getLocalizedName());
        check("Item.<clinit>", () -> Item.coal.itemID);

        System.out.println();
        System.out.println("=== chest loot static tables ===");
        for (Map.Entry<String, Supplier<WeightedRandomChestContent[]>> entry : chestTables().entrySet()) {
            check(entry.getKey(), () -> describe(entry.getValue().get()));
        }

        System.out.println();
        System.out.println("=== null-world entity construction ===");
        for (Map.Entry<String, Supplier<EntityLivingBase>> entry : entities().entrySet()) {
            check(entry.getKey(), () -> {
                EntityLivingBase entity = entry.getValue().get();
                return entity.width + "x" + entity.height + ", xp=" + entity.getExperienceValue();
            });
        }

        System.out.println();
        System.out.println("=== scrapers end to end ===");
        check("MITEChestScraper.scrape", () -> {
            List<DungeonEntry> entries = MITEChestScraper.scrape();
            StringBuilder summary = new StringBuilder(entries.size() + " chests");
            for (DungeonEntry entry : entries) {
                summary.append("\n          ").append(String.format("%-40s %2d items, rolls %d-%d",
                        entry.getNameKey(), entry.getChestDrops().size(), entry.getMinRolls(), entry.getMaxRolls()));
            }
            return summary.toString();
        });

        check("MITEOreScraper.scrape", () -> {
            long start = System.nanoTime();
            List<OreMatchEntry> entries = MITEOreScraper.scrape();
            long millis = (System.nanoTime() - start) / 1_000_000L;
            StringBuilder summary = new StringBuilder(entries.size() + " ores in " + millis + " ms");
            for (OreMatchEntry entry : entries) {
                float peak = 0.0F;
                for (float chance : entry.getChances()) {
                    peak = Math.max(peak, chance);
                }
                summary.append("\n          ").append(String.format("%-28s y %3d-%3d, best %3d, peak %6.3f%%, %d drops",
                        StackHelper.nameOf(entry.getPrimaryOre()) + " " + entry.getRestriction().getDimensionRestriction().getValidDimensions(),
                        entry.getMinY(), entry.getMaxY(), entry.getBestY(), peak * 100.0F, entry.getDrops().size()));
            }
            return summary.toString();
        });

        check("MITEMobData.collect", () -> {
            List<MobEntry> mobs = MITEMobData.collect();
            StringBuilder summary = new StringBuilder(mobs.size() + " mobs");
            for (MobEntry mob : mobs) {
                StringBuilder drops = new StringBuilder();
                for (DropItem drop : mob.getDrops()) {
                    if (drops.length() > 0) {
                        drops.append(", ");
                    }
                    drops.append(StackHelper.nameOf(drop.item)).append(' ').append(drop);
                }
                summary.append("\n          ").append(String.format("%-24s xp=%-3d %s",
                        mob.getNameKey(), mob.getExperience(),
                        drops.length() == 0 ? "(nothing)" : drops));
            }
            return summary.toString();
        });

        check("MITEPlantData.collect", () -> {
            List<PlantEntry> plants = MITEPlantData.collect();
            StringBuilder summary = new StringBuilder(plants.size() + " plants");
            for (PlantEntry plant : plants) {
                StringBuilder drops = new StringBuilder();
                for (PlantDrop drop : plant.getDrops()) {
                    if (drops.length() > 0) {
                        drops.append(", ");
                    }
                    drops.append(StackHelper.nameOf(drop.getDrop()))
                            .append(' ').append(drop.describe(plant.getTotalWeight()));
                }
                summary.append("\n          ").append(String.format("%-28s %s",
                        StackHelper.nameOf(plant.getPlant()), drops));
            }
            return summary.toString();
        });

        check("MITETradeData.collect", () -> {
            List<TradeEntry> trades = MITETradeData.collect();
            int[] perProfession = new int[5];
            for (TradeEntry trade : trades) {
                int profession = trade.getProfession();
                if (profession >= 0 && profession < perProfession.length) {
                    perProfession[profession]++;
                }
            }
            String[] names = {"farmer", "librarian", "priest", "smith", "butcher"};
            StringBuilder summary = new StringBuilder(trades.size() + " trades");
            for (int profession = 0; profession < names.length; profession++) {
                summary.append("\n          ").append(String.format("%-12s %3d",
                        names[profession], perProfession[profession]));
            }
            return summary.toString();
        });

        check("enchantable items", () -> {
            int enchantable = 0;
            int totalPairs = 0;
            for (Item item : Item.itemsList) {
                if (item == null) {
                    continue;
                }
                int accepted = 0;
                for (Enchantment enchantment : Enchantment.enchantmentsList) {
                    if (enchantment != null && enchantment.canEnchantItem(item)) {
                        accepted++;
                    }
                }
                if (accepted > 0) {
                    enchantable++;
                    totalPairs += accepted;
                }
            }
            return enchantable + " items, " + totalPairs + " item/enchantment pairs";
        });

        System.out.println();
        System.out.printf("%d passed, %d failed%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    /** Every loot table this mod needs, each behind a supplier so one failure does not hide the rest. */
    private static Map<String, Supplier<WeightedRandomChestContent[]>> chestTables() {
        Map<String, Supplier<WeightedRandomChestContent[]>> tables =
                new LinkedHashMap<String, Supplier<WeightedRandomChestContent[]>>();
        tables.put("WorldGenDungeons.field_111189_a", () -> WorldGenDungeons.field_111189_a);
        tables.put("WorldGenDungeons.chest_contents_for_underworld", () -> WorldGenDungeons.chest_contents_for_underworld);
        tables.put("StructureMineshaftPieces.mineshaftChestContents", () -> StructureMineshaftPieces.mineshaftChestContents);
        tables.put("ComponentScatteredFeatureDesertPyramid.itemsToGenerateInTemple", () -> ComponentScatteredFeatureDesertPyramid.itemsToGenerateInTemple);
        tables.put("ComponentStrongholdChestCorridor.strongholdChestContents", () -> ComponentStrongholdChestCorridor.strongholdChestContents);
        tables.put("ComponentStrongholdLibrary.strongholdLibraryChestContents", () -> ComponentStrongholdLibrary.strongholdLibraryChestContents);
        tables.put("ComponentStrongholdRoomCrossing.strongholdRoomCrossingChestContents", () -> ComponentStrongholdRoomCrossing.strongholdRoomCrossingChestContents);
        tables.put("ComponentVillageHouse2.villageBlacksmithChestContents", () -> ComponentVillageHouse2.villageBlacksmithChestContents);
        // Highest risk of the nine: forces WorldServer to initialise outside a server start-up.
        tables.put("WorldServer.bonusChestContent", () -> WorldServer.bonusChestContent);
        return tables;
    }

    /** One entity per constructor shape, since that is what varies in its handling of a null world. */
    private static Map<String, Supplier<EntityLivingBase>> entities() {
        Map<String, Supplier<EntityLivingBase>> mobs = new LinkedHashMap<String, Supplier<EntityLivingBase>>();
        mobs.put("EntityZombie", () -> new EntityZombie(null));
        mobs.put("EntitySkeleton", () -> new EntitySkeleton(null));
        mobs.put("EntityCreeper", () -> new EntityCreeper(null));
        mobs.put("EntitySpider", () -> new EntitySpider(null));
        mobs.put("EntityEnderman", () -> new EntityEnderman(null));
        mobs.put("EntityBlaze", () -> new EntityBlaze(null));
        mobs.put("EntityGhast", () -> new EntityGhast(null));
        mobs.put("EntityWitch", () -> new EntityWitch(null));
        mobs.put("EntityMagmaCube", () -> new EntityMagmaCube(null));
        mobs.put("EntityWither", () -> new EntityWither(null));
        mobs.put("EntityIronGolem", () -> new EntityIronGolem(null));
        mobs.put("EntityEarthElemental", () -> new EntityEarthElemental(null));
        mobs.put("EntityBat", () -> new EntityBat(null));
        mobs.put("EntitySquid", () -> new EntitySquid(null));
        mobs.put("EntityWolf", () -> new EntityWolf(null));
        mobs.put("EntityCow", () -> new EntityCow(null));
        mobs.put("EntityPig", () -> new EntityPig(null));
        mobs.put("EntitySheep", () -> new EntitySheep(null));
        mobs.put("EntityChicken", () -> new EntityChicken(null));
        mobs.put("EntityHorse", () -> new EntityHorse(null));
        return mobs;
    }

    /**
     * Checks that every class named in {@code fml.mod.json} can actually be loaded and instantiated
     * the way a loader would do it.
     * <p>
     * This cannot prove that EMI calls our plugin — only a running client shows that. What it does
     * rule out is the set of mistakes that would make the entrypoint silently never fire: a
     * misspelled class name, a class that is not public, a missing no-argument constructor, or one
     * that does not implement the interface the loader casts it to.
     */
    private static void checkEntrypoints() {
        Map<String, Class<?>> expectedTypes = new LinkedHashMap<String, Class<?>>();
        expectedTypes.put("main", ModInitializer.class);
        expectedTypes.put("client", ClientModInitializer.class);
        expectedTypes.put("emi", EmiPlugin.class);

        JsonObject metadata;
        try (InputStream stream = HeadlessProbe.class.getClassLoader().getResourceAsStream("fml.mod.json")) {
            if (stream == null) {
                failed++;
                System.out.println("  FAIL  fml.mod.json not found on the classpath");
                return;
            }
            metadata = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
        } catch (Exception e) {
            failed++;
            System.out.println("  FAIL  could not read fml.mod.json: " + e);
            return;
        }

        // processResources expands ${id}; an unexpanded placeholder here would mean a broken jar.
        String modId = metadata.get("id").getAsString();
        check("mod id expanded", () -> modId.startsWith("${") ? fail("still a placeholder: " + modId) : modId);

        JsonObject entrypoints = metadata.getAsJsonObject("entrypoints");
        for (Map.Entry<String, Class<?>> expected : expectedTypes.entrySet()) {
            JsonArray declared = entrypoints.getAsJsonArray(expected.getKey());
            if (declared == null || declared.size() == 0) {
                failed++;
                System.out.printf("  FAIL  %-70s no %s entrypoint declared%n",
                        expected.getKey(), expected.getKey());
                continue;
            }
            for (int i = 0; i < declared.size(); i++) {
                String className = declared.get(i).getAsString();
                check(expected.getKey() + " -> " + className,
                        () -> instantiate(className, expected.getValue()));
            }
        }
    }

    /** Loads and constructs the class exactly as a loader's default language adapter would. */
    private static String instantiate(String className, Class<?> expectedType) throws Exception {
        Class<?> type = Class.forName(className);
        if (!expectedType.isAssignableFrom(type)) {
            return fail("does not implement " + expectedType.getSimpleName());
        }
        Object instance = type.getDeclaredConstructor().newInstance();
        return "instantiated as " + expectedType.getSimpleName() + " (" + instance.getClass().getSimpleName() + ")";
    }

    private static String fail(String reason) {
        throw new IllegalStateException(reason);
    }

    private static String describe(WeightedRandomChestContent[] table) {
        if (table == null) {
            return "null";
        }
        int weight = 0;
        for (WeightedRandomChestContent content : table) {
            weight += content.itemWeight;
        }
        return table.length + " entries, total weight " + weight;
    }

    private static void check(String name, ThrowingSupplier<Object> probe) {
        try {
            Object result = probe.get();
            passed++;
            System.out.printf("  ok    %-70s %s%n", name, result);
        } catch (Throwable t) {
            failed++;
            System.out.printf("  FAIL  %-70s %s: %s%n", name, t.getClass().getName(), t.getMessage());
            Throwable cause = t.getCause();
            if (cause != null) {
                System.out.printf("        caused by %s: %s%n", cause.getClass().getName(), cause.getMessage());
            }
            StackTraceElement[] trace = t.getStackTrace();
            for (int i = 0; i < Math.min(3, trace.length); i++) {
                System.out.println("        at " + trace[i]);
            }
        }
    }

    private interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }
}
