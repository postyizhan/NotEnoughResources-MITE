package neresources.emi.widget;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.Minecraft;
import shims.java.net.minecraft.client.gui.DrawContext;
import shims.java.net.minecraft.client.gui.tooltip.TooltipComponent;
import shims.java.net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * An item whose count MITE rolls within a range, drawn as {@code 4-5} in place of a single number.
 * <p>
 * Every count in the range is offered as a separate stack so EMI's lookups still match the item at any
 * of the amounts a villager might ask for. A range needs more room than the corner of a slot allows, so
 * it is drawn at half scale, which is how EMI Trades presents the same thing.
 */
public class RangedAmountIngredient implements EmiIngredient {
    private final EmiStack stack;
    private final int min;
    private final int max;
    private List<EmiStack> stacks;

    public RangedAmountIngredient(EmiStack stack, int min, int max) {
        this.stack = stack;
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    /** Built once and kept, since EMI asks for this while filtering and a wide range is many stacks. */
    @Override
    public List<EmiStack> getEmiStacks() {
        if (stacks == null) {
            List<EmiStack> built = new ArrayList<EmiStack>(max - min + 1);
            for (int amount = min; amount <= max; amount++) {
                built.add(stack.copy().setAmount(amount));
            }
            stacks = built;
        }
        return stacks;
    }

    @Override
    public EmiIngredient copy() {
        return new RangedAmountIngredient(stack, min, max);
    }

    /** @return the largest count, so EMI sizes anything derived from this for the worst case. */
    @Override
    public long getAmount() {
        return max;
    }

    @Override
    public EmiIngredient setAmount(long amount) {
        return this;
    }

    @Override
    public float getChance() {
        return stack.getChance();
    }

    @Override
    public EmiIngredient setChance(float chance) {
        stack.setChance(chance);
        return this;
    }

    @Override
    public void render(DrawContext draw, int x, int y, float delta, int flags) {
        // The item draws itself, minus the count, which is replaced with the range below.
        stack.render(draw, x, y, delta, flags & ~RENDER_AMOUNT);
        if ((flags & RENDER_AMOUNT) == 0) {
            return;
        }

        String label = min == max ? String.valueOf(min) : min + "-" + max;
        int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(label);
        EmiDrawContext context = EmiDrawContext.wrap(draw);
        context.push();
        context.matrices().translate(0, 0, 200);
        if (min == max) {
            context.drawTextWithShadow(Text.literal(label), x + 17 - width, y + 9, -1);
        } else {
            // Half scale halves both the text and the coordinates, so they are doubled here to land
            // where they were meant to. Bottom-aligning four pixels of text against the slot's inner
            // edge at y + 17 puts its top at y + 13, which is where the full-size number sits too.
            context.matrices().scale(0.5D, 0.5D, 1.0D);
            context.drawTextWithShadow(Text.literal(label), (x + 17) * 2 - width, (y + 13) * 2, -1);
        }
        context.pop();
    }

    @Override
    public List<TooltipComponent> getTooltip() {
        return stack.getTooltip();
    }
}
