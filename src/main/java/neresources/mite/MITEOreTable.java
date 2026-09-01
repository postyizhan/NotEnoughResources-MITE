package neresources.mite;

import neresources.api.distribution.VeinShape;
import neresources.api.util.ColorHelper;
import net.minecraft.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MITE's ore generation parameters, transcribed from {@code BiomeDecorator.generateOres},
 * {@code BiomeDecorator.genMinable} and {@code WorldGenMinable}.
 * <p>
 * This is a table rather than something read from the game because the height bounds and the height
 * distribution both come from methods that need a {@code World}
 * ({@code getMinVeinHeight}/{@code getMaxVeinHeight} branch on {@code isUnderworld}), and no world is
 * available while EMI reloads. Vein sizes are the exception: those are read off a real
 * {@code BiomeDecorator} by {@link MITEOreScraper}, with the values here as the fallback.
 * <p>
 * Two details of {@code genMinable} are folded into {@link OreDef#veinsPerChunk}:
 * <ul>
 *     <li>The loop runs {@code count} times but only generates on {@code rand.nextInt(10) == 0}, so
 *         the expected attempts per chunk are {@code count / 10}.</li>
 *     <li>In the underworld {@code count} is multiplied by 8 for everything except gravel, and
 *         adamantite is multiplied by a further 2.</li>
 * </ul>
 */
public final class MITEOreTable {
    /** Which {@code BiomeDecorator} field holds an ore's generator, for reading the real vein size. */
    public enum Generator {
        DIRT, GRAVEL, COAL, COPPER, SILVER, GOLD, IRON, MITHRIL, ADAMANTITE, REDSTONE, DIAMOND, LAPIS, SILVERFISH
    }

    /** One ore in one dimension. */
    public static final class OreDef {
        public final int dimensionId;
        public final Generator generator;
        public final int minY;
        public final int maxY;
        public final int fallbackVeinSize;
        /** Expected vein attempts per chunk, after the dimension multipliers and the 1-in-10 gate. */
        public final float veinsPerChunk;
        /** The {@code vein_size_increases_with_depth} argument {@code generateOres} passes. */
        public final boolean deep;
        public final VeinShape shape;
        public final int graphColour;

        OreDef(int dimensionId, Generator generator, int minY, int maxY, int fallbackVeinSize,
               float veinsPerChunk, boolean deep, VeinShape shape, int graphColour) {
            this.dimensionId = dimensionId;
            this.generator = generator;
            this.minY = minY;
            this.maxY = maxY;
            this.fallbackVeinSize = fallbackVeinSize;
            this.veinsPerChunk = veinsPerChunk;
            this.deep = deep;
            this.shape = shape;
            this.graphColour = graphColour;
        }
    }

    private static final int OVERWORLD = World.DIMENSION_ID_OVERWORLD;
    private static final int UNDERWORLD = World.DIMENSION_ID_UNDERWORLD;

    private static final List<OreDef> ORES;

    static {
        List<OreDef> ores = new ArrayList<OreDef>();

        // Overworld — generateOres() when world.isOverworld().
        // genMinable(count, gen[, deep]); veinsPerChunk = count / 10.
        ores.add(new OreDef(OVERWORLD, Generator.DIRT, 32, 128, 32, 20.0F, false, VeinShape.ASCENDING, ColorHelper.BROWN));
        ores.add(new OreDef(OVERWORLD, Generator.GRAVEL, 24, 128, 32, 20.0F, false, VeinShape.ASCENDING, ColorHelper.LIGHT_GRAY));
        ores.add(new OreDef(OVERWORLD, Generator.COAL, 16, 96, 16, 5.0F, false, VeinShape.ASCENDING, ColorHelper.DARK_GRAY));
        ores.add(new OreDef(OVERWORLD, Generator.COPPER, 0, 128, 6, 4.0F, true, VeinShape.COPPER_MIX, ColorHelper.ORANGE));
        ores.add(new OreDef(OVERWORLD, Generator.SILVER, 0, 96, 6, 1.0F, true, VeinShape.DESCENDING, ColorHelper.WHITE));
        ores.add(new OreDef(OVERWORLD, Generator.GOLD, 0, 48, 4, 2.0F, true, VeinShape.DESCENDING, ColorHelper.YELLOW));
        ores.add(new OreDef(OVERWORLD, Generator.IRON, 0, 64, 6, 6.0F, true, VeinShape.DESCENDING, ColorHelper.PINK));
        ores.add(new OreDef(OVERWORLD, Generator.MITHRIL, 0, 32, 3, 1.0F, true, VeinShape.DESCENDING, ColorHelper.TEAL));
        ores.add(new OreDef(OVERWORLD, Generator.SILVERFISH, 0, 24, 3, 0.5F, true, VeinShape.DESCENDING, ColorHelper.GRAY));
        ores.add(new OreDef(OVERWORLD, Generator.REDSTONE, 0, 24, 5, 1.0F, false, VeinShape.DESCENDING, ColorHelper.RED));
        ores.add(new OreDef(OVERWORLD, Generator.DIAMOND, 0, 32, 3, 0.5F, false, VeinShape.DESCENDING, ColorHelper.CYAN));
        ores.add(new OreDef(OVERWORLD, Generator.LAPIS, 8, 40, 3, 0.5F, false, VeinShape.TRIANGULAR, ColorHelper.BLUE));

        // Underworld — generateOres() when world.isUnderworld(). Height bounds are 0..255 for every
        // ore, and underworld_y_offset is always 120, so getRandomVeinHeight takes its offset branch:
        // adamantite is always uniform over [0,136), other BlockOre are 75% [0,136) and otherwise
        // [0,256), and everything else is uniform over [0,256).
        // BlockRedstoneOre extends Block rather than BlockOre, so redstone is in the last group.
        ores.add(new OreDef(UNDERWORLD, Generator.GRAVEL, 0, 255, 32, 30.0F, false, VeinShape.UNDERWORLD_UNIFORM, ColorHelper.LIGHT_GRAY));
        ores.add(new OreDef(UNDERWORLD, Generator.COPPER, 0, 255, 6, 32.0F, true, VeinShape.UNDERWORLD_ORE, ColorHelper.ORANGE));
        ores.add(new OreDef(UNDERWORLD, Generator.SILVER, 0, 255, 6, 8.0F, true, VeinShape.UNDERWORLD_ORE, ColorHelper.WHITE));
        ores.add(new OreDef(UNDERWORLD, Generator.GOLD, 0, 255, 4, 16.0F, true, VeinShape.UNDERWORLD_ORE, ColorHelper.YELLOW));
        ores.add(new OreDef(UNDERWORLD, Generator.IRON, 0, 255, 6, 48.0F, true, VeinShape.UNDERWORLD_ORE, ColorHelper.PINK));
        ores.add(new OreDef(UNDERWORLD, Generator.MITHRIL, 0, 255, 3, 8.0F, true, VeinShape.UNDERWORLD_ORE, ColorHelper.TEAL));
        ores.add(new OreDef(UNDERWORLD, Generator.ADAMANTITE, 0, 255, 3, 8.0F, true, VeinShape.UNDERWORLD_ADAMANTITE, ColorHelper.MAGENTA));
        ores.add(new OreDef(UNDERWORLD, Generator.REDSTONE, 0, 255, 5, 8.0F, false, VeinShape.UNDERWORLD_UNIFORM, ColorHelper.RED));
        ores.add(new OreDef(UNDERWORLD, Generator.DIAMOND, 0, 255, 3, 4.0F, false, VeinShape.UNDERWORLD_ORE, ColorHelper.CYAN));
        ores.add(new OreDef(UNDERWORLD, Generator.LAPIS, 0, 255, 3, 4.0F, false, VeinShape.UNDERWORLD_ORE, ColorHelper.BLUE));
        // genMinable(50, silverfishGen) — the two-argument form, so deep is false, and the x8 applies.
        ores.add(new OreDef(UNDERWORLD, Generator.SILVERFISH, 0, 255, 3, 40.0F, false, VeinShape.UNDERWORLD_UNIFORM, ColorHelper.GRAY));

        ORES = Collections.unmodifiableList(ores);
    }

    private MITEOreTable() {
    }

    public static List<OreDef> getOres() {
        return ORES;
    }
}
