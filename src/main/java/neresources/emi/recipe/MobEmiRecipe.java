package neresources.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import neresources.NEResources;
import neresources.api.util.DropItem;
import neresources.emi.NERCategories;
import neresources.emi.widget.EntityRenderHelper;
import neresources.entry.MobEntry;
import net.minecraft.ResourceLocation;
import net.minecraft.StatCollector;
import org.jetbrains.annotations.Nullable;
import shims.java.net.minecraft.client.gui.tooltip.TooltipComponent;
import shims.java.net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One mob: its model and spawn conditions on the left, its drop table on the right. */
public class MobEmiRecipe implements EmiRecipe {
    private static final int WIDTH = 160;
    private static final int ENTITY_PANEL_WIDTH = 56;
    private static final int DROP_X = ENTITY_PANEL_WIDTH + 4;
    private static final int FIRST_DROP_Y = 4;
    private static final int ROW_HEIGHT = 18;
    private static final int MIN_HEIGHT = 60;

    private final MobEntry entry;
    private final ResourceLocation id;
    private final List<EmiStack> outputs = new ArrayList<EmiStack>();

    public MobEmiRecipe(MobEntry entry) {
        this.entry = entry;
        this.id = new ResourceLocation(NEResources.MOD_ID, "mob/" + entry.getNameKey());
        for (DropItem drop : entry.getDrops()) {
            outputs.add(EmiStack.of(drop.item).setChance(Math.min(drop.chance, 1.0F)));
        }
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return NERCategories.MOB;
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
        return Math.max(MIN_HEIGHT, FIRST_DROP_Y + entry.getDrops().size() * ROW_HEIGHT + 4);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        addEntity(widgets);
        addSpawnInfo(widgets);
        addDrops(widgets);
    }

    private void addEntity(WidgetHolder widgets) {
        int panelHeight = getDisplayHeight();
        int feetY = panelHeight - 24;
        float scale = EntityRenderHelper.scaleFor(entry.getEntity(), panelHeight - 40);
        widgets.addDrawable(0, 0, ENTITY_PANEL_WIDTH, panelHeight, (draw, mouseX, mouseY, delta) ->
                EntityRenderHelper.render(ENTITY_PANEL_WIDTH / 2, feetY, scale,
                        ENTITY_PANEL_WIDTH / 2.0F - mouseX, feetY - 30.0F - mouseY, entry.getEntity()));
    }

    private void addSpawnInfo(WidgetHolder widgets) {
        int panelHeight = getDisplayHeight();
        widgets.addText(Text.translatable(entry.getNameKey()).asOrderedText(), 2, panelHeight - 20,
                0xFFFFFFFF, false);
        widgets.addText(Text.literal(entry.getLightLevel().toString()).asOrderedText(), 2, panelHeight - 10,
                0xFFA0A0A0, false);

        String experience = StatCollector.translateToLocal("ner.mob.exp") + ": " + entry.getExperience();
        widgets.addText(Text.literal(experience).asOrderedText(), DROP_X, panelHeight - 10,
                0xFFA0A0A0, false);
    }

    private void addDrops(WidgetHolder widgets) {
        List<DropItem> drops = entry.getDrops();
        for (int i = 0; i < drops.size(); i++) {
            DropItem drop = drops.get(i);
            int y = FIRST_DROP_Y + i * ROW_HEIGHT;

            SlotWidget slot = widgets.addSlot(outputs.get(i), DROP_X, y).recipeContext(this);
            for (final String conditional : drop.conditionals) {
                slot.appendTooltip(() -> TooltipComponent.of(Text.literal(conditional).asOrderedText()));
            }

            widgets.addText(Text.literal(drop.toString()).asOrderedText(),
                    DROP_X + ROW_HEIGHT + 2, y + 5, 0xFFFFFFFF, false);
        }
    }
}
