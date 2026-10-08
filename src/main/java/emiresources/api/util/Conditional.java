package emiresources.api.util;

import net.minecraft.StatCollector;

/**
 * A note attached to a drop explaining when it applies, shown in the item's tooltip — for example
 * "only when killed by a player" or "requires Silk Touch".
 */
public class Conditional {
    /** Formatting prefixes, spelled out so callers do not need the section character. */
    public static final String BOLD = "§l";
    public static final String ITALIC = "§o";
    public static final String DARK_RED = "§4";
    public static final String RED = "§c";
    public static final String GOLD = "§6";
    public static final String YELLOW = "§e";
    public static final String DARK_GREEN = "§2";
    public static final String GREEN = "§a";
    public static final String AQUA = "§b";
    public static final String DARK_AQUA = "§3";
    public static final String BLUE = "§9";
    public static final String LIGHT_PURPLE = "§d";
    public static final String DARK_PURPLE = "§5";
    public static final String GRAY = "§7";
    public static final String DARK_GRAY = "§8";

    public static final Conditional playerKill = new Conditional("emir.conditional.playerKill", LIGHT_PURPLE);
    public static final Conditional rareDrop = new Conditional("emir.conditional.rareDrop", DARK_PURPLE);
    public static final Conditional burning = new Conditional("emir.conditional.burning", RED);
    public static final Conditional notBurning = new Conditional("emir.conditional.notBurning", AQUA);
    /**
     * MITE's {@code EntityLivestock.isWell()} — the animal is fed, watered and not penned in
     * ({@code min(freedom, food, water) >= 0.25}). It gates the meat drops, not just the amount.
     */
    public static final Conditional livestockWell = new Conditional("emir.conditional.livestockWell", GREEN);
    public static final Conditional butchering = new Conditional("emir.conditional.butchering", GOLD);
    public static final Conditional looting = new Conditional("emir.conditional.looting", BLUE);
    public static final Conditional notFallDeath = new Conditional("emir.conditional.notFallDeath", GRAY);
    public static final Conditional saddled = new Conditional("emir.conditional.saddled", YELLOW);
    public static final Conditional notSheared = new Conditional("emir.conditional.notSheared", GREEN);
    public static final Conditional silkTouch = new Conditional("emir.conditional.silkTouch", DARK_AQUA);
    public static final Conditional fireball = new Conditional("emir.conditional.fireball", GOLD);
    public static final Conditional villager = new Conditional("emir.conditional.villager", GREEN);
    public static final Conditional witherSkeleton = new Conditional("emir.conditional.witherSkeleton", DARK_GRAY);
    public static final Conditional longdead = new Conditional("emir.conditional.longdead", GRAY);
    public static final Conditional explosion = new Conditional("emir.conditional.explosion", DARK_RED);
    public static final Conditional mature = new Conditional("emir.conditional.mature", GREEN);
    public static final Conditional blighted = new Conditional("emir.conditional.blighted", DARK_GREEN);
    public static final Conditional sizeOne = new Conditional("emir.conditional.sizeOne", AQUA);
    public static final Conditional web = new Conditional("emir.conditional.web", GRAY);

    private final String key;
    private final String color;

    public Conditional(String translationKey) {
        this(translationKey, "");
    }

    public Conditional(String translationKey, String color) {
        this.key = translationKey;
        this.color = color;
    }

    /** @return the localised, coloured line to append to a tooltip. */
    @Override
    public String toString() {
        return color + StatCollector.translateToLocal(key);
    }
}
