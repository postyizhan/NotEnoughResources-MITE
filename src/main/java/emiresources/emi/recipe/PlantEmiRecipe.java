package emiresources.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import emiresources.EmiResources;
import emiresources.api.util.PlantDrop;
import emiresources.emi.EmiRCategories;
import emiresources.entry.PlantEntry;
import net.minecraft.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import shims.java.net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One plant and its harvest: the plant on the left, each drop with its yield or odds on the right. */
public class PlantEmiRecipe implements EmiRecipe {
    private static final int WIDTH = 148;
    private static final int PLANT_X = 4;
    private static final int PLANT_Y = 4;
    private static final int DROP_X = 34;
    private static final int FIRST_DROP_Y = 4;
    private static final int ROW_HEIGHT = 18;
    private static final int MIN_HEIGHT = 40;

    private final PlantEntry entry;
    private final ResourceLocation id;
    private final EmiStack plant;
    private final List<EmiStack> outputs = new ArrayList<EmiStack>();

    public PlantEmiRecipe(PlantEntry entry) {
        this.entry = entry;
        this.id = new ResourceLocation(EmiResources.MOD_ID, "plant/" + entry.getPlant().itemID
                + ":" + entry.getPlant().getItemSubtype());
        this.plant = EmiStack.of(entry.getPlant());
        for (PlantDrop drop : entry.getDrops()) {
            outputs.add(EmiStack.of(drop.getDrop()).setChance(Math.min(drop.getChance(), 1.0F)));
        }
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EmiRCategories.PLANT;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    /** The plant is the catalyst rather than an input, so it is not consumed in the recipe tree. */
    @Override
    public List<EmiIngredient> getInputs() {
        return Collections.emptyList();
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return Collections.<EmiIngredient>singletonList(plant);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

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
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return Math.max(MIN_HEIGHT, FIRST_DROP_Y + entry.getDrops().size() * ROW_HEIGHT + 14);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(plant, PLANT_X, PLANT_Y).recipeContext(this);

        List<PlantDrop> drops = entry.getDrops();
        for (int i = 0; i < drops.size(); i++) {
            int y = FIRST_DROP_Y + i * ROW_HEIGHT;
            widgets.addSlot(outputs.get(i), DROP_X, y).recipeContext(this);
            widgets.addText(Text.literal(drops.get(i).describe(entry.getTotalWeight())).asOrderedText(),
                    DROP_X + ROW_HEIGHT + 2, y + 5, 0xFFFFFFFF, false);
        }

        if (entry.getNameKey() != null) {
            widgets.addText(Text.translatable(entry.getNameKey()).asOrderedText(), 2,
                            getDisplayHeight() - 11, 0xFFA0A0A0, false)
                    .horizontalAlign(TextWidget.Alignment.START);
        }
    }
}
