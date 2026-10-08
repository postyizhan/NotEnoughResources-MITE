package emiresources.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import emiresources.EmiResources;
import emiresources.emi.EmiRCategories;
import net.minecraft.Enchantment;
import net.minecraft.ItemStack;
import net.minecraft.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import shims.java.net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The enchantments one item can take.
 * <p>
 * NEI had a dedicated "usage" view for this; EMI has no equivalent, so the item is listed as a
 * catalyst instead. That makes the page reachable by looking up the item, which is the same journey
 * for the player.
 */
public class EnchantmentEmiRecipe implements EmiRecipe {
    private static final int WIDTH = 148;
    private static final int ITEM_X = 4;
    private static final int ITEM_Y = 4;
    private static final int TEXT_X = 28;
    private static final int FIRST_LINE_Y = 5;
    private static final int LINE_HEIGHT = 10;

    private final ResourceLocation id;
    private final EmiStack item;
    private final List<String> lines = new ArrayList<String>();

    /**
     * @param stack        the enchantable item
     * @param enchantments the enchantments it accepts
     */
    public EnchantmentEmiRecipe(ItemStack stack, List<Enchantment> enchantments) {
        this.id = new ResourceLocation(EmiResources.MOD_ID, "enchantment/" + stack.itemID);
        this.item = EmiStack.of(stack);
        for (Enchantment enchantment : enchantments) {
            lines.add(describe(enchantment, stack));
        }
    }

    /** @return the enchantment name with its level range, or just the name when it has one level. */
    private static String describe(Enchantment enchantment, ItemStack stack) {
        String name = enchantment.getTranslatedName(stack.getItem());
        int levels = enchantment.getNumLevels();
        if (!enchantment.hasLevels() || levels <= 1) {
            return name;
        }
        return enchantment.getTranslatedName(1, stack) + " - " + enchantment.getTranslatedName(levels, stack);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EmiRCategories.ENCHANTMENT;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return Collections.emptyList();
    }

    /** Listing the item as a catalyst is what makes looking it up find this page. */
    @Override
    public List<EmiIngredient> getCatalysts() {
        return Collections.<EmiIngredient>singletonList(item);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return Collections.emptyList();
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return Math.max(24, FIRST_LINE_Y + lines.size() * LINE_HEIGHT + 4);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(item, ITEM_X, ITEM_Y).recipeContext(this);
        for (int i = 0; i < lines.size(); i++) {
            widgets.addText(Text.literal(lines.get(i)).asOrderedText(),
                    TEXT_X, FIRST_LINE_Y + i * LINE_HEIGHT, 0xFFFFFFFF, false);
        }
    }

    /** @return true if this page would be empty and should not be registered. */
    public boolean isEmpty() {
        return lines.isEmpty();
    }
}
