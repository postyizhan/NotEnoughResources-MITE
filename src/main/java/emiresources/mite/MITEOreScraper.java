package emiresources.mite;

import emiresources.EmiResources;
import emiresources.api.distribution.DistributionMITEVein;
import emiresources.api.restriction.Restriction;
import emiresources.entry.OreMatchEntry;
import emiresources.mite.MITEOreTable.Generator;
import emiresources.mite.MITEOreTable.OreDef;
import net.minecraft.BiomeDecorator;
import net.minecraft.BiomeGenBase;
import net.minecraft.Block;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.WorldGenMinable;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns {@link MITEOreTable} into ore graphs.
 * <p>
 * Vein sizes are read from a throwaway {@code BiomeDecorator} rather than hard-coded, so retuning
 * them in MITE is reflected here without a code change. Its constructor only builds generators and
 * reads {@code Block} fields — it never touches a world — so constructing one here is safe.
 */
public final class MITEOreScraper {
    private MITEOreScraper() {
    }

    /** @return one entry per ore per dimension, ready to be turned into recipes. */
    public static List<OreMatchEntry> scrape() {
        List<OreMatchEntry> entries = new ArrayList<OreMatchEntry>();
        BiomeDecorator decorator = createDecorator();

        for (OreDef ore : MITEOreTable.getOres()) {
            Block block = blockFor(ore.generator);
            if (block == null) {
                EmiResources.LOGGER.warn("No block for ore generator {}; skipping.", ore.generator);
                continue;
            }

            int veinSize = veinSizeOf(decorator, ore);
            DistributionMITEVein distribution = new DistributionMITEVein(
                    block.blockID, ore.minY, ore.maxY, veinSize, ore.veinsPerChunk, ore.deep, ore.shape);

            // Silverfish blocks disguise themselves as stone and yield nothing when broken, so the
            // block itself is what the player is looking for.
            OreMatchEntry entry = new OreMatchEntry(
                    new ItemStack(block), distribution, Restriction.forDimension(ore.dimensionId),
                    false, ore.graphColour);

            for (ItemStack drop : dropsOf(block)) {
                entry.addDrop(drop, false);
            }
            entries.add(entry);
        }

        return entries;
    }

    /**
     * Reads the vein size MITE actually configured.
     *
     * @return the generator's {@code numberOfBlocks}, or the table's fallback if it cannot be read
     */
    private static int veinSizeOf(BiomeDecorator decorator, OreDef ore) {
        if (decorator == null) {
            return ore.fallbackVeinSize;
        }
        try {
            WorldGenMinable generator = generatorFor(decorator, ore.generator);
            if (generator == null) {
                return ore.fallbackVeinSize;
            }
            int size = generator.numberOfBlocks;
            return size > 0 ? size : ore.fallbackVeinSize;
        } catch (Throwable t) {
            EmiResources.LOGGER.warn("Could not read vein size for {}; using {}.",
                    ore.generator, ore.fallbackVeinSize, t);
            return ore.fallbackVeinSize;
        }
    }

    private static BiomeDecorator createDecorator() {
        try {
            return new BiomeDecorator(BiomeGenBase.plains);
        } catch (Throwable t) {
            EmiResources.LOGGER.warn("Could not build a BiomeDecorator; falling back to built-in vein sizes.", t);
            return null;
        }
    }

    private static WorldGenMinable generatorFor(BiomeDecorator decorator, Generator generator) {
        switch (generator) {
            case DIRT:
                return decorator.dirtGen;
            case GRAVEL:
                return decorator.gravelGen;
            case COAL:
                return decorator.coalGen;
            case COPPER:
                return decorator.copperGen;
            case SILVER:
                return decorator.silverGen;
            case GOLD:
                return decorator.goldGen;
            case IRON:
                return decorator.ironGen;
            case MITHRIL:
                return decorator.mithrilGen;
            case ADAMANTITE:
                return decorator.adamantiteGen;
            case REDSTONE:
                return decorator.redstoneGen;
            case DIAMOND:
                return decorator.diamondGen;
            case LAPIS:
                return decorator.lapisGen;
            case SILVERFISH:
                return decorator.silverfishGen;
            default:
                return null;
        }
    }

    private static Block blockFor(Generator generator) {
        switch (generator) {
            case DIRT:
                return Block.dirt;
            case GRAVEL:
                return Block.gravel;
            case COAL:
                return Block.oreCoal;
            case COPPER:
                return Block.oreCopper;
            case SILVER:
                return Block.oreSilver;
            case GOLD:
                return Block.oreGold;
            case IRON:
                return Block.oreIron;
            case MITHRIL:
                return Block.oreMithril;
            case ADAMANTITE:
                return Block.oreAdamantium;
            case REDSTONE:
                return Block.oreRedstone;
            case DIAMOND:
                return Block.oreDiamond;
            case LAPIS:
                return Block.oreLapis;
            case SILVERFISH:
                return Block.silverfish;
            default:
                return null;
        }
    }

    /**
     * What an ore yields when mined normally, from {@code BlockOre.dropBlockAsEntityItem} and the
     * overrides in {@code BlockRedstoneOre} and {@code BlockGravel}. Ores that drop themselves — the
     * metals, which MITE requires you to smelt — return nothing extra.
     */
    private static List<ItemStack> dropsOf(Block block) {
        List<ItemStack> drops = new ArrayList<ItemStack>();
        if (block == Block.oreCoal) {
            drops.add(new ItemStack(Item.coal));
        } else if (block == Block.oreDiamond) {
            drops.add(new ItemStack(Item.diamond));
        } else if (block == Block.oreLapis) {
            // dropBlockAsEntityItem(info, dyePowder, 4, 3 + rand.nextInt(3), ...)
            drops.add(new ItemStack(Item.dyePowder, 4, 4));
        } else if (block == Block.oreRedstone) {
            // BlockRedstoneOre: 3 + rand.nextInt(3), scaled by Fortune.
            drops.add(new ItemStack(Item.redstone, 4));
        } else if (block == Block.gravel) {
            // BlockGravel: flint on rand.nextInt(12 - fortune * 2) <= 2, otherwise the gravel itself.
            drops.add(new ItemStack(Item.flint));
        }
        return drops;
    }
}
