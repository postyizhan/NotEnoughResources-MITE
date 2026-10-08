package emiresources.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import emiresources.EmiResources;
import emiresources.config.EmirConfig;
import emiresources.emi.EmiRCategories;
import emiresources.emi.widget.DistributionGraphWidget;
import emiresources.entry.OreMatchEntry;
import net.minecraft.ItemStack;
import net.minecraft.ResourceLocation;
import net.minecraft.StatCollector;
import org.jetbrains.annotations.Nullable;
import shims.java.net.minecraft.client.gui.tooltip.TooltipComponent;
import shims.java.net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** One ore's height distribution: the ores and drops on the left, the curve on the right. */
public class OreEmiRecipe implements EmiRecipe {
    private static final int WIDTH = 160;
    private static final int HEIGHT = 74;
    private static final int ITEM_X = 4;
    private static final int ITEM_Y = 4;
    private static final int GRAPH_X = 58;
    private static final int GRAPH_BASELINE_Y = 60;
    private static final int GRAPH_WIDTH = 92;
    private static final int GRAPH_HEIGHT = 42;

    private final OreMatchEntry entry;
    private final ResourceLocation id;
    private final List<EmiStack> outputs = new ArrayList<EmiStack>();
    private final float[] values;
    private final float maxValue;
    private final int startY;

    public OreEmiRecipe(OreMatchEntry entry) {
        this.entry = entry;
        this.id = new ResourceLocation(EmiResources.MOD_ID,
                "ore/" + entry.getPrimaryOre().itemID
                        + "/" + entry.getRestriction().getDimensionRestriction().getValidDimensions());

        int extraRange = EmirConfig.getExtraYRange();
        this.values = entry.getGraphSlice(extraRange);
        this.startY = entry.getGraphStartY(extraRange);

        float max = 0.0F;
        for (float value : values) {
            max = Math.max(max, value);
        }
        this.maxValue = max;

        for (ItemStack stack : entry.getOresAndDrops()) {
            outputs.add(EmiStack.of(stack));
        }
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EmiRCategories.ORE;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    /** Ore is found, not crafted, so it has no inputs. */
    @Override
    public List<EmiIngredient> getInputs() {
        return Collections.emptyList();
    }

    /** Listing the ores and drops as outputs is what makes "where does this come from" find this page. */
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
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        addItemSlots(widgets);
        addBestHeightLabel(widgets);
        addGraph(widgets);
        addAxisLabels(widgets);
    }

    private void addItemSlots(WidgetHolder widgets) {
        int x = ITEM_X;
        int y = ITEM_Y;
        for (EmiStack output : outputs) {
            widgets.addSlot(output, x, y).recipeContext(this);
            x += 18;
            if (x + 18 > GRAPH_X) {
                x = ITEM_X;
                y += 18;
            }
        }
    }

    private void addBestHeightLabel(WidgetHolder widgets) {
        String label = StatCollector.translateToLocal("emir.ore.bestY") + ": " + entry.getBestY();
        widgets.addText(Text.literal(label).asOrderedText(), ITEM_X, HEIGHT - 12, 0xFFFFFFFF, false);
    }

    private void addGraph(WidgetHolder widgets) {
        widgets.addDrawable(0, 0, WIDTH, HEIGHT, (draw, mouseX, mouseY, delta) ->
                DistributionGraphWidget.draw(GRAPH_X, GRAPH_BASELINE_Y, GRAPH_WIDTH, GRAPH_HEIGHT,
                        values, maxValue, entry.getColour()));

        // A separate hover region over the plot reports the exact chance at the hovered height.
        widgets.add(widgets.addTooltip((mouseX, mouseY) -> hoverTooltip(mouseX),
                GRAPH_X, GRAPH_BASELINE_Y - GRAPH_HEIGHT, GRAPH_WIDTH + 1, GRAPH_HEIGHT + 1));
    }

    private List<TooltipComponent> hoverTooltip(int mouseX) {
        List<TooltipComponent> tooltip = new ArrayList<TooltipComponent>();
        if (values.length < 2) {
            return tooltip;
        }
        int index = Math.round((mouseX - GRAPH_X) * (values.length - 1) / (float) GRAPH_WIDTH);
        index = Math.max(0, Math.min(index, values.length - 1));
        String height = StatCollector.translateToLocal("emir.ore.hoverY") + ": " + (startY + index);
        String chance = StatCollector.translateToLocal("emir.ore.chance") + ": "
                + String.format(Locale.ROOT, "%.3f%%", values[index] * 100.0F);
        tooltip.add(TooltipComponent.of(Text.literal(height).asOrderedText()));
        tooltip.add(TooltipComponent.of(Text.literal(chance).asOrderedText()));
        return tooltip;
    }

    private void addAxisLabels(WidgetHolder widgets) {
        widgets.addText(Text.literal("0%").asOrderedText(), GRAPH_X - 2, GRAPH_BASELINE_Y - 4,
                        0xFFA0A0A0, false)
                .horizontalAlign(TextWidget.Alignment.END);
        widgets.addText(Text.literal(formatPeak()).asOrderedText(), GRAPH_X - 2,
                        GRAPH_BASELINE_Y - GRAPH_HEIGHT - 2, 0xFFA0A0A0, false)
                .horizontalAlign(TextWidget.Alignment.END);
        widgets.addText(Text.literal(String.valueOf(startY)).asOrderedText(), GRAPH_X,
                GRAPH_BASELINE_Y + 2, 0xFFA0A0A0, false);
        widgets.addText(Text.literal(String.valueOf(startY + values.length - 1)).asOrderedText(),
                        GRAPH_X + GRAPH_WIDTH, GRAPH_BASELINE_Y + 2, 0xFFA0A0A0, false)
                .horizontalAlign(TextWidget.Alignment.END);
    }

    private String formatPeak() {
        return String.format("%.2f%%", maxValue * 100.0F).replace(',', '.');
    }
}
