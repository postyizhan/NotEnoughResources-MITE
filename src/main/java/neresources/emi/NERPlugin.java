package neresources.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.runtime.EmiReloadLog;
import neresources.NEResources;
import neresources.emi.recipe.DungeonEmiRecipe;
import neresources.emi.recipe.EnchantmentEmiRecipe;
import neresources.emi.recipe.MobEmiRecipe;
import neresources.emi.recipe.OreEmiRecipe;
import neresources.emi.recipe.PlantEmiRecipe;
import neresources.entry.DungeonEntry;
import neresources.entry.MobEntry;
import neresources.entry.OreMatchEntry;
import neresources.entry.PlantEntry;
import neresources.mite.MITEChestScraper;
import neresources.mite.MITEMobData;
import neresources.mite.MITEOreScraper;
import neresources.mite.MITEPlantData;
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
public class NERPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        NEResources.LOGGER.info("EMI plugin registration started.");

        registry.addCategory(NERCategories.ORE);
        registry.addCategory(NERCategories.MOB);
        registry.addCategory(NERCategories.PLANT);
        registry.addCategory(NERCategories.DUNGEON);
        registry.addCategory(NERCategories.ENCHANTMENT);

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
                recipes.add(new MobEmiRecipe(entry));
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
                recipes.add(new DungeonEmiRecipe(entry));
            }
            return recipes;
        });
        addAllSafely(registry, "enchantment", NERPlugin::enchantmentRecipes);

        NEResources.LOGGER.info("EMI plugin registration finished.");
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
            EmiReloadLog.warn("NotEnoughResources: failed to build " + source + " entries", t);
            NEResources.LOGGER.warn("Failed to build {} entries", source, t);
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
