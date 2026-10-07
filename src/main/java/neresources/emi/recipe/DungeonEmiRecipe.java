package neresources.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import neresources.NEResources;
import neresources.emi.NERCategories;
import neresources.entry.DungeonEntry;
import net.minecraft.ItemStack;
import net.minecraft.ResourceLocation;
import net.minecraft.StatCollector;
import org.jetbrains.annotations.Nullable;
import shims.java.net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** One generated chest: its name, how many stacks it holds, and the loot table with per-item odds. */
public class DungeonEmiRecipe implements EmiRecipe {
    private static final int COLUMNS = 8;
    private static final int SLOT_SIZE = 18;
    private static final int FIRST_ROW_Y = 26;
    /** Room for a slot plus the odds line underneath it. */
    private static final int ROW_HEIGHT = SLOT_SIZE + 8;
    private static final int PAGE_ROWS = 4;
    private static final int PAGE_SIZE = COLUMNS * PAGE_ROWS;

    private final DungeonEntry entry;
    private final ResourceLocation id;
    private final int page;
    private final int pageCount;
    private final List<EmiStack> outputs = new ArrayList<EmiStack>();
    private final List<Float> chances = new ArrayList<Float>();

    public DungeonEmiRecipe(DungeonEntry entry) {
        this(entry, 0, 1);
    }

    public DungeonEmiRecipe(DungeonEntry entry, int page, int pageCount) {
        this.entry = entry;
        this.page = page;
        this.pageCount = pageCount;
        this.id = new ResourceLocation(NEResources.MOD_ID,
                "dungeon/" + entry.getNameKey() + "/" + page);
        int first = page * PAGE_SIZE;
        int last = Math.min(first + PAGE_SIZE, entry.getChestDrops().size());
        int index = 0;
        for (Map.Entry<ItemStack, Float> drop : entry.getChestDrops().entrySet()) {
            if (index++ < first) {
                continue;
            }
            if (index > last) {
                break;
            }
            outputs.add(EmiStack.of(drop.getKey()).setChance(Math.min(drop.getValue(), 1.0F)));
            chances.add(drop.getValue());
        }
    }

    public static int getPageSize() {
        return PAGE_SIZE;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return NERCategories.DUNGEON;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return Collections.emptyList();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    /** The loot is not crafted from anything, so it must stay out of the recipe tree. */
    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public boolean hideCraftable() {
        return true;
    }

    @Override
    public int getDisplayWidth() {
        return COLUMNS * SLOT_SIZE + 4;
    }

    @Override
    public int getDisplayHeight() {
        return FIRST_ROW_Y + rowCount() * ROW_HEIGHT;
    }

    private int rowCount() {
        return (outputs.size() + COLUMNS - 1) / COLUMNS;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addText(Text.translatable(entry.getNameKey()).asOrderedText(), 2, 2, 0xFFFFFFFF, true);
        String pageLabel = pageCount > 1 ? " (" + (page + 1) + "/" + pageCount + ")" : "";
        widgets.addText(Text.literal(stackCountLabel() + pageLabel).asOrderedText(),
                2, 14, 0xFFA0A0A0, false);

        for (int i = 0; i < outputs.size(); i++) {
            int column = i % COLUMNS;
            int row = i / COLUMNS;
            int x = 2 + column * SLOT_SIZE;
            int y = FIRST_ROW_Y + row * ROW_HEIGHT;

            widgets.addSlot(outputs.get(i), x, y).recipeContext(this);
            widgets.addText(Text.literal(formatChance(chances.get(i))).asOrderedText(),
                            x + SLOT_SIZE / 2, y + SLOT_SIZE + 1, 0xFFA0A0A0, false)
                    .horizontalAlign(TextWidget.Alignment.CENTER);
        }
    }

    /** @return e.g. {@code "Stacks: 3 - 8"}, or a single number when the range is fixed. */
    private String stackCountLabel() {
        String range = entry.getMinRolls() == entry.getMaxRolls()
                ? String.valueOf(entry.getMaxRolls())
                : entry.getMinRolls() + " - " + entry.getMaxRolls();
        return StatCollector.translateToLocalFormatted("ner.stacks", range);
    }

    /**
     * @param chance expected draws per chest, which can exceed one
     * @return a percentage, with one decimal below 10% so rare loot stays legible
     */
    private static String formatChance(float chance) {
        float percent = chance * 100.0F;
        String formatted = percent < 10.0F
                ? String.format("%.1f", percent)
                : String.format("%d", Math.round(percent));
        return formatted.replace(',', '.') + "%";
    }
}
