package emiresources.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.Block;
import net.minecraft.Item;
import net.minecraft.ResourceLocation;
import emiresources.EmiResources;

/**
 * The five categories this mod adds to EMI's sidebar.
 * <p>
 * Names come from {@code emi.category.emiresources.<path>}, which is how
 * {@code EmiUtil.translateId} derives a category's translation key.
 */
public final class EmiRCategories {
    public static final EmiRecipeCategory ORE = category("ore", EmiStack.of(Block.oreIron));
    public static final EmiRecipeCategory MOB = category("mob", EmiStack.of(Item.swordIron));
    public static final EmiRecipeCategory PLANT = category("plant", EmiStack.of(Block.tallGrass));
    public static final EmiRecipeCategory DUNGEON = category("dungeon", EmiStack.of(Block.chest));
    public static final EmiRecipeCategory ENCHANTMENT = category("enchantment", EmiStack.of(Block.enchantmentTable));
    public static final EmiRecipeCategory TRADE = category("trade", EmiStack.of(Item.emerald));

    private EmiRCategories() {
    }

    private static EmiRecipeCategory category(String path, EmiStack icon) {
        return new EmiRecipeCategory(new ResourceLocation(EmiResources.MOD_ID, path), icon);
    }
}
