package neresources.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import neresources.NEResources;
import neresources.emi.NERCategories;
import neresources.emi.widget.EntityRenderHelper;
import neresources.emi.widget.RangedAmountIngredient;
import neresources.entry.TradeEntry;
import neresources.entry.TradeEntry.Slot;
import neresources.mite.MITETradeData;
import net.minecraft.EntityVillager;
import net.minecraft.Minecraft;
import net.minecraft.ResourceLocation;
import net.minecraft.StatCollector;
import org.jetbrains.annotations.Nullable;
import shims.java.net.minecraft.client.gui.tooltip.TooltipComponent;
import shims.java.net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One villager trade, laid out as EMI Trades does it: the villager on the left, then what the player
 * pays, an arrow, and what they get back.
 * <p>
 * MITE gives no experience for trading and allows the same seven uses for every trade, so the
 * experience line EMI Trades draws is left out and the use count is a constant.
 */
public class TradeEmiRecipe implements EmiRecipe {
    private static final int MIN_WIDTH = 106;
    private static final int HEIGHT = 37;
    private static final int ENTITY_PANEL_WIDTH = 20;
    private static final int CONTENT_X = 21;
    private static final int SLOT_Y = 10;
    private static final int SECOND_SLOT_X = 41;
    private static final int ARROW_X = 60;
    private static final int OUTPUT_X = 85;

    /**
     * One villager per profession, shared by every recipe so the models are built once. A profession
     * that could not be built is remembered as an absent value, because EMI rebuilds a recipe's widgets
     * on every frame it previews it and retrying there would fill the log.
     */
    private static final Map<Integer, EntityVillager> VILLAGERS = new HashMap<Integer, EntityVillager>();

    private static final String[] PROFESSION_KEYS = {
            "farmer", "librarian", "priest", "smith", "butcher",
    };

    private final TradeEntry entry;
    private final ResourceLocation id;
    private final List<EmiIngredient> inputs = new ArrayList<EmiIngredient>();
    private final List<EmiStack> outputs = new ArrayList<EmiStack>();

    /**
     * What the output slot draws. Kept apart from {@link #outputs} because EMI indexes that list for
     * lookups, where a plain stack is wanted, while the slot itself should show the range of counts a
     * villager may hand over.
     */
    private final EmiIngredient outputDisplay;

    public TradeEmiRecipe(TradeEntry entry, int index) {
        this.entry = entry;
        this.id = new ResourceLocation(NEResources.MOD_ID,
                "trade/" + professionName(entry.getProfession()) + "/" + index);

        inputs.add(ingredient(entry.getFirst()));
        if (entry.getSecond() != null) {
            inputs.add(ingredient(entry.getSecond()));
        }
        outputs.add(EmiStack.of(entry.getSell().getStack()));
        outputDisplay = ingredient(entry.getSell());
    }

    /** A slot MITE varies becomes a range; a fixed one is an ordinary stack. */
    private static EmiIngredient ingredient(Slot slot) {
        EmiStack stack = EmiStack.of(slot.getRawStack());
        if (slot.isRange()) {
            return new RangedAmountIngredient(stack, slot.getMin(), slot.getMax());
        }
        return stack.copy().setAmount(slot.getMin());
    }

    private static String professionName(int profession) {
        return profession >= 0 && profession < PROFESSION_KEYS.length
                ? PROFESSION_KEYS[profession]
                : "villager";
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return NERCategories.TRADE;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return Collections.emptyList();
    }

    /** Trades are not a crafting step, so EMI should not offer to walk into them. */
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
        int titleWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(title());
        return Math.max(MIN_WIDTH, CONTENT_X + titleWidth);
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        addVillager(widgets);
        widgets.addText(Text.literal(title()).asOrderedText(), CONTENT_X, 0, 0xFFFFFFFF, true);

        widgets.addSlot(inputs.get(0), CONTENT_X, SLOT_Y);
        if (inputs.size() > 1) {
            widgets.addSlot(inputs.get(1), SECOND_SLOT_X, SLOT_Y);
        } else {
            // EMI treats an empty stack's zero chance as a special slot background. Use the
            // ordinary input chance so an unused second input slot has the same frame colour.
            widgets.addSlot(EmiStack.EMPTY.copy().setChance(1.0F), SECOND_SLOT_X, SLOT_Y);
        }
        widgets.addTexture(EmiTexture.EMPTY_ARROW, ARROW_X, SLOT_Y);

        SlotWidget output = widgets.addSlot(outputDisplay, OUTPUT_X, SLOT_Y).recipeContext(this);
        final String noteKey = entry.getNoteKey();
        if (noteKey != null) {
            output.appendTooltip(() -> TooltipComponent.of(
                    Text.translatable(noteKey).asOrderedText()));
        }

        String uses = StatCollector.translateToLocalFormatted("ner.trade.maxUses", MITETradeData.MAX_USES);
        widgets.addText(Text.literal(uses).asOrderedText(), CONTENT_X, HEIGHT - 8, 0xFFD48333, true);
    }

    private void addVillager(WidgetHolder widgets) {
        final EntityVillager villager = villager(entry.getProfession());
        if (villager == null) {
            return;
        }
        final int feetY = HEIGHT - 3;
        final float scale = EntityRenderHelper.scaleFor(villager, HEIGHT - 11);
        widgets.addDrawable(0, 0, ENTITY_PANEL_WIDTH, HEIGHT, (draw, mouseX, mouseY, delta) ->
                EntityRenderHelper.render(ENTITY_PANEL_WIDTH / 2, feetY, scale,
                        ENTITY_PANEL_WIDTH / 2.0F - mouseX, feetY - 20.0F - mouseY, villager));
    }

    /** @return the shared villager for a profession, or null if one could not be built. */
    private static EntityVillager villager(int profession) {
        if (VILLAGERS.containsKey(profession)) {
            return VILLAGERS.get(profession);
        }
        EntityVillager villager = null;
        try {
            villager = new EntityVillager(Minecraft.getMinecraft().theWorld, profession);
        } catch (Throwable t) {
            NEResources.LOGGER.warn("Could not build a villager for profession {}", profession, t);
        }
        VILLAGERS.put(profession, villager);
        return villager;
    }

    private String title() {
        return StatCollector.translateToLocal(
                "ner.trade.profession." + professionName(entry.getProfession()));
    }
}
