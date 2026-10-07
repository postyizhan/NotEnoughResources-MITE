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
import net.minecraft.Minecraft;
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
    private static final int FOOTER_HEIGHT = 24;
    private static final int PAGE_SIZE = 3;
    private static final int DROP_LABEL_X = DROP_X + ROW_HEIGHT + 2;
    private static final int DROP_LABEL_WIDTH = WIDTH - DROP_LABEL_X - 2;

    private final MobEntry entry;
    private final ResourceLocation id;
    private final List<DropItem> pageDrops = new ArrayList<DropItem>();
    private final int page;
    private final int pageCount;
    private final List<EmiStack> outputs = new ArrayList<EmiStack>();

    public MobEmiRecipe(MobEntry entry) {
        this(entry, 0, 1);
    }

    public MobEmiRecipe(MobEntry entry, int page, int pageCount) {
        this.entry = entry;
        this.page = page;
        this.pageCount = pageCount;
        this.id = new ResourceLocation(NEResources.MOD_ID, "mob/" + entry.getNameKey() + "/" + page);
        int first = page * PAGE_SIZE;
        int last = Math.min(first + PAGE_SIZE, entry.getDrops().size());
        for (int index = first; index < last; index++) {
            DropItem drop = entry.getDrops().get(index);
            pageDrops.add(drop);
            outputs.add(EmiStack.of(drop.item).setChance(Math.min(drop.chance, 1.0F)));
        }
    }

    public static int getPageSize() {
        return PAGE_SIZE;
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
        // Keep the footer below the final drop row; the old four-pixel tail let the row label
        // overlap the experience line when a mob had several drops.
        return Math.max(MIN_HEIGHT,
                FIRST_DROP_Y + PAGE_SIZE * ROW_HEIGHT + FOOTER_HEIGHT);
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
        String name = StatCollector.translateToLocal(entry.getNameKey());
        if (pageCount > 1) {
            name += " (" + (page + 1) + "/" + pageCount + ")";
        }
        widgets.addText(Text.literal(name).asOrderedText(), 2, panelHeight - 20,
                0xFFFFFFFF, false);
        widgets.addText(Text.literal(entry.getLightLevel().toString()).asOrderedText(), 2, panelHeight - 10,
                0xFFA0A0A0, false);

        String experience = StatCollector.translateToLocal("ner.mob.exp") + ": " + entry.getExperience();
        widgets.addText(Text.literal(experience).asOrderedText(), DROP_X, panelHeight - 10,
                0xFFA0A0A0, false);
    }

    private void addDrops(WidgetHolder widgets) {
        List<DropItem> drops = pageDrops;
        for (int i = 0; i < drops.size(); i++) {
            DropItem drop = drops.get(i);
            int y = FIRST_DROP_Y + i * ROW_HEIGHT;

            SlotWidget slot = widgets.addSlot(outputs.get(i), DROP_X, y).recipeContext(this);
            for (final String conditional : drop.conditionals) {
                slot.appendTooltip(() -> TooltipComponent.of(Text.literal(conditional).asOrderedText()));
            }

            widgets.addText(Text.literal(trimDropLabel(drop.toString())).asOrderedText(),
                    DROP_LABEL_X, y + 5, 0xFFFFFFFF, false);
        }
    }

    /** Keep long range/chance labels inside the recipe panel instead of overpainting its footer. */
    private static String trimDropLabel(String label) {
        return Minecraft.getMinecraft().fontRenderer.trimStringToWidth(label, DROP_LABEL_WIDTH);
    }
}
