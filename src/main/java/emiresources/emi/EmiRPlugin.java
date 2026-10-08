package emiresources.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.runtime.EmiReloadLog;
import emiresources.EmiResources;
import emiresources.emi.recipe.DungeonEmiRecipe;
import emiresources.emi.recipe.EnchantmentEmiRecipe;
import emiresources.emi.recipe.MobEmiRecipe;
import emiresources.emi.recipe.OreEmiRecipe;
import emiresources.emi.recipe.PlantEmiRecipe;
import emiresources.emi.recipe.TradeEmiRecipe;
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
import net.minecraft.Enchantment;
import net.minecraft.Item;
import net.minecraft.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers this mod's categories with EMI.
 * <p>
 * EMI runs plugins on its own reload thread, so each data source is added through
 * {@link #addAllSafely} — reading MITE's static loot tables or constructing an entity there can fail,
 * and that should cost one category rather than the whole reload.
 */
public class EmiRPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        EmiResources.LOGGER.info("EMI plugin registration started.");

        registry.addCategory(EmiRCategories.ORE);
        registry.addCategory(EmiRCategories.MOB);
        registry.addCategory(EmiRCategories.PLANT);
        registry.addCategory(EmiRCategories.DUNGEON);
        registry.addCategory(EmiRCategories.ENCHANTMENT);
        registry.addCategory(EmiRCategories.TRADE);

        // Placeholder entries keep each category visible while the data sources are wired up; they
        // are replaced one at a time as the scrapers land.
        addAllSafely(registry, "ore", () -> {
            List<EmiRecipe> recipes = new ArrayList<EmiRecipe>();
            for (OreMatchEntry entry : MITEOreScraper.scrape()) {
                recipes.add(new OreEmiRecipe(entry));
            }
            return recipes;
        });
        addAllSafely(registry, "mob", () -> {
            List<EmiRecipe> recipes = new ArrayList<EmiRecipe>();
            for (MobEntry entry : MITEMobData.collect()) {
                int pageSize = MobEmiRecipe.getPageSize();
                int pageCount = Math.max(1, (entry.getDrops().size() + pageSize - 1) / pageSize);
                for (int page = 0; page < pageCount; page++) {
                    recipes.add(new MobEmiRecipe(entry, page, pageCount));
                }
            }
            return recipes;
        });
        addAllSafely(registry, "plant", () -> {
            List<EmiRecipe> recipes = new ArrayList<EmiRecipe>();
            for (PlantEntry entry : MITEPlantData.collect()) {
                recipes.add(new PlantEmiRecipe(entry));
            }
            return recipes;
        });
        addAllSafely(registry, "dungeon", () -> {
            List<EmiRecipe> recipes = new ArrayList<EmiRecipe>();
            for (DungeonEntry entry : MITEChestScraper.scrape()) {
                int pageSize = DungeonEmiRecipe.getPageSize();
                int pageCount = (entry.getChestDrops().size() + pageSize - 1) / pageSize;
                for (int page = 0; page < pageCount; page++) {
                    recipes.add(new DungeonEmiRecipe(entry, page, pageCount));
                }
            }
            return recipes;
        });
        addAllSafely(registry, "enchantment", EmiRPlugin::enchantmentRecipes);
        addAllSafely(registry, "trade", () -> {
            List<EmiRecipe> recipes = new ArrayList<EmiRecipe>();
            int index = 0;
            for (TradeEntry entry : MITETradeData.collect()) {
                recipes.add(new TradeEmiRecipe(entry, index++));
            }
            return recipes;
        });

        EmiResources.LOGGER.info("EMI plugin registration finished.");
    }

    /**
     * Adds the recipes from one data source, containing any failure to that source.
     *
     * @param source a short name used in the log if the source fails
     */
    private static void addAllSafely(EmiRegistry registry, String source, RecipeSupplier supplier) {
        try {
            for (EmiRecipe recipe : supplier.get()) {
                registry.addRecipe(recipe);
            }
        } catch (Throwable t) {
            EmiReloadLog.warn("EmiResources: failed to build " + source + " entries", t);
            EmiResources.LOGGER.warn("Failed to build {} entries", source, t);
        }
    }

    /**
     * One page per enchantable item, listing what it accepts. Items are walked once and asked
     * directly, since MITE decides enchantability per item rather than by tool class.
     */
    private static Iterable<EmiRecipe> enchantmentRecipes() {
        List<EmiRecipe> recipes = new ArrayList<EmiRecipe>();
        for (Item item : Item.itemsList) {
            if (item == null) {
                continue;
            }
            List<Enchantment> accepted = new ArrayList<Enchantment>();
            for (Enchantment enchantment : Enchantment.enchantmentsList) {
                if (enchantment != null && enchantment.canEnchantItem(item)) {
                    accepted.add(enchantment);
                }
            }
            if (accepted.isEmpty()) {
                continue;
            }
            EnchantmentEmiRecipe recipe = new EnchantmentEmiRecipe(new ItemStack(item), accepted);
            if (!recipe.isEmpty()) {
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    private interface RecipeSupplier {
        Iterable<EmiRecipe> get();
    }
}
