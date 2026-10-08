package emiresources.mite;

import emiresources.EmiResources;
import emiresources.api.util.Conditional;
import emiresources.api.util.DropItem;
import emiresources.api.util.LightLevel;
import emiresources.entry.MobEntry;
import net.minecraft.Block;
import net.minecraft.EntityBat;
import net.minecraft.EntityBlaze;
import net.minecraft.EntityChicken;
import net.minecraft.EntityCow;
import net.minecraft.EntityCreeper;
import net.minecraft.EntityEnderman;
import net.minecraft.EntityGhast;
import net.minecraft.EntityHorse;
import net.minecraft.EntityIronGolem;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityMagmaCube;
import net.minecraft.EntityPig;
import net.minecraft.EntityPigZombie;
import net.minecraft.EntitySheep;
import net.minecraft.EntitySkeleton;
import net.minecraft.EntitySnowman;
import net.minecraft.EntitySpider;
import net.minecraft.EntitySquid;
import net.minecraft.EntityWight;
import net.minecraft.EntityWitch;
import net.minecraft.EntityWither;
import net.minecraft.EntityWolf;
import net.minecraft.EntityZombie;
import net.minecraft.Item;
import net.minecraft.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * MITE's mob drop tables, transcribed from each {@code EntityLiving.dropFewItems} implementation.
 * <p>
 * These cannot be read from the game: {@code dropFewItems} needs a live world and a
 * {@code DamageSource}, and it drops items into the world rather than returning them. So each table
 * below is derived by hand from the corresponding method, with the source quoted in a comment so the
 * arithmetic can be checked against MITE.
 * <p>
 * Quantities assume no Looting or Butchering — the enchantments widen the range, and the drops that
 * respond to them carry {@link Conditional#looting} or {@link Conditional#butchering}. Percentages
 * are for a kill by the player; MITE reduces most drops otherwise, which is marked with
 * {@link Conditional#playerKill}.
 * <p>
 * The rare-drop rolls all take the form
 * {@code rand.nextInt(getBaseChanceOfRareDrop()) < 5 + looting * 2}, and
 * {@code getBaseChanceOfRareDrop()} is 200, giving 2.5% without Looting.
 */
public final class MITEMobData {
    /** {@code EntityLivingBase.getBaseChanceOfRareDrop()} returns 200. */
    private static final float RARE_DROP_CHANCE = 5.0F / 200.0F;
    /** {@code EntityWitch} draws {@code rand.nextInt(5 + looting) + 1} items, so three on average. */
    private static final int WITCH_AVERAGE_DRAWS = 3;

    private MITEMobData() {
    }

    /** @return one entry per mob; any that fails to build is skipped with a warning. */
    public static List<MobEntry> collect() {
        List<MobEntry> mobs = new ArrayList<MobEntry>();

        // EntityZombie: rottenFlesh at 50% on a player kill, then a rare nugget from
        // rare_drops_standard = {copper, silver, gold, iron}, one of four at 2.5%.
        add(mobs, () -> new MobEntry(new EntityZombie(null), "entity.Zombie.name", LightLevel.hostile,
                new DropItem(Item.rottenFlesh, 1, 1, 0.5F, Conditional.playerKill),
                nugget(Item.copperNugget), nugget(Item.silverNugget),
                nugget(Item.goldNugget), nugget(Item.ironNugget)));

        // EntitySkeleton: arrows from rand.nextInt(2 + looting), bone from rand.nextInt(3).
        // Wither skeletons instead drop coal and, at 2.5%, their skull; the longdead drop ancient
        // metal arrows and only on a further one-in-three roll.
        add(mobs, () -> new MobEntry(new EntitySkeleton(null), "entity.Skeleton.name", LightLevel.hostile,
                new DropItem(Item.arrowRustedIron, 0, 1, Conditional.looting),
                new DropItem(Item.bone, 0, 2),
                new DropItem(Item.arrowAncientMetal, 0, 1, Conditional.longdead),
                new DropItem(Item.coal, 0, 1, Conditional.witherSkeleton, Conditional.looting),
                new DropItem(new ItemStack(Item.skull, 1, 1), 1, 1, RARE_DROP_CHANCE,
                        Conditional.witherSkeleton, Conditional.playerKill, Conditional.rareDrop)));

        // EntityCreeper: the generic EntityLiving.dropFewItems, rand.nextInt(3) drops of
        // getDropItemId(), which yields gunpowder outright on a player kill.
        add(mobs, () -> new MobEntry(new EntityCreeper(null), "entity.Creeper.name", LightLevel.hostile,
                new DropItem(Item.gunpowder, 0, 2, Conditional.playerKill, Conditional.looting)));

        // EntityArachnid.dropFewItems: one silk per web the spider never threw, then a spider eye on
        // rand.nextInt(3) == 0. num_webs is rand.nextInt(4), less one for ordinary spiders.
        add(mobs, () -> new MobEntry(new EntitySpider(null), "entity.Spider.name", LightLevel.hostile,
                new DropItem(Item.silk, 0, 2, Conditional.playerKill, Conditional.web),
                new DropItem(Item.spiderEye, 1, 1, 1.0F / 3.0F, Conditional.playerKill, Conditional.butchering)));

        // EntityEnderman: rand.nextInt(2 + looting) pearls.
        add(mobs, () -> new MobEntry(new EntityEnderman(null), "entity.Enderman.name", LightLevel.hostile,
                new DropItem(Item.enderPearl, 0, 1, Conditional.looting)));

        // EntityPigZombie: rottenFlesh at 50%, rand.nextInt(2 + looting) nuggets, gold ingot at 2.5%.
        add(mobs, () -> new MobEntry(new EntityPigZombie(null), "entity.PigZombie.name", LightLevel.any,
                new DropItem(Item.rottenFlesh, 1, 1, 0.5F, Conditional.playerKill),
                new DropItem(Item.goldNugget, 0, 1, Conditional.looting),
                new DropItem(Item.ingotGold, 1, 1, RARE_DROP_CHANCE,
                        Conditional.playerKill, Conditional.rareDrop, Conditional.notFallDeath)));

        // EntityWight: rottenFlesh at 50%, then one nugget of four at 2.5%.
        add(mobs, () -> new MobEntry(new EntityWight(null), "entity.Wight.name", LightLevel.hostile,
                new DropItem(Item.rottenFlesh, 1, 1, 0.5F, Conditional.playerKill),
                nugget(Item.copperNugget), nugget(Item.silverNugget),
                nugget(Item.goldNugget), nugget(Item.ironNugget)));

        // EntityBlaze: rand.nextInt(2 + looting) rods, and only on a player kill.
        add(mobs, () -> new MobEntry(new EntityBlaze(null), "entity.Blaze.name", LightLevel.any,
                new DropItem(Item.blazeRod, 0, 1, Conditional.playerKill, Conditional.looting)));

        // EntityGhast: rand.nextInt(2) tears, guaranteed if your own fireball killed it, plus
        // rand.nextInt(3) gunpowder.
        add(mobs, () -> new MobEntry(new EntityGhast(null), "entity.Ghast.name", LightLevel.any,
                new DropItem(Item.ghastTear, 0, 1, Conditional.fireball),
                new DropItem(Item.gunpowder, 0, 2)));

        // EntityMagmaCube: rand.nextInt(4 + looting) - 2, so nothing or one, and only above the
        // smallest size.
        add(mobs, () -> new MobEntry(new EntityMagmaCube(null), "entity.LavaSlime.name", LightLevel.any,
                new DropItem(Item.magmaCream, 0, 1, Conditional.looting)));

        // EntityWitch: rand.nextInt(5 + looting) + 1 draws from an 18-slot pool.
        add(mobs, MITEMobData::witch);

        // EntityIronGolem: rand.nextInt(3) poppies, then 3 + rand.nextInt(3 + looting) iron nuggets.
        add(mobs, () -> new MobEntry(new EntityIronGolem(null), "entity.VillagerGolem.name", LightLevel.any,
                new DropItem(new ItemStack(Block.plantRed, 1, 0), 0, 2),
                new DropItem(Item.ironNugget, 3, 5,
                        Conditional.playerKill, Conditional.notFallDeath, Conditional.looting)));

        // EntitySnowman: rand.nextInt(8 + looting * 4) snowballs.
        add(mobs, () -> new MobEntry(new EntitySnowman(null), "entity.SnowMan.name", LightLevel.any,
                new DropItem(Item.snowball, 0, 7, Conditional.looting)));

        // EntityWither: a single nether star, unconditionally.
        add(mobs, () -> new MobEntry(new EntityWither(null), "entity.WitherBoss.name", LightLevel.any,
                new DropItem(Item.netherStar, 1, 1)));

        // EntitySquid: one ink sac, and only on a player kill.
        add(mobs, () -> new MobEntry(new EntitySquid(null), "entity.Squid.name", LightLevel.any,
                new DropItem(new ItemStack(Item.dyePowder, 1, 0), 1, 1, Conditional.playerKill)));

        // EntityBat: no dropFewItems override and getDropItemId is 0, so bats drop nothing.
        add(mobs, () -> new MobEntry(new EntityBat(null), "entity.Bat.name", LightLevel.bat));

        // EntityWolf: a single hide.
        add(mobs, () -> new MobEntry(new EntityWolf(null), "entity.Wolf.name", LightLevel.animal,
                new DropItem(Item.leather, 1, 1)));

        // EntityCow: rand.nextInt(3) + 1 hides, then beef once the animal is in good condition.
        add(mobs, () -> new MobEntry(new EntityCow(null), "entity.Cow.name", LightLevel.animal,
                new DropItem(Item.leather, 1, 3),
                new DropItem(Item.beefRaw, 1, 3,
                        Conditional.livestockWell, Conditional.notBurning, Conditional.butchering),
                new DropItem(Item.beefCooked, 1, 3,
                        Conditional.livestockWell, Conditional.burning, Conditional.butchering)));

        // EntityPig: the saddle if it is wearing one, then pork once in good condition.
        add(mobs, () -> new MobEntry(new EntityPig(null), "entity.Pig.name", LightLevel.animal,
                new DropItem(Item.saddle, 1, 1, Conditional.saddled),
                new DropItem(Item.porkRaw, 1, 3,
                        Conditional.livestockWell, Conditional.notBurning, Conditional.butchering),
                new DropItem(Item.porkCooked, 1, 3,
                        Conditional.livestockWell, Conditional.burning, Conditional.butchering)));

        // EntitySheep: wool on rand.nextInt(2) == 0 while unsheared, a lambchop once in good
        // condition, and a hide on another rand.nextInt(2) == 0.
        add(mobs, () -> new MobEntry(new EntitySheep(null), "entity.Sheep.name", LightLevel.animal,
                new DropItem(new ItemStack(Block.cloth, 1, 0), 1, 1, 0.5F,
                        Conditional.notSheared, Conditional.notBurning),
                new DropItem(Item.lambchopRaw, 1, 1,
                        Conditional.livestockWell, Conditional.notBurning, Conditional.butchering),
                new DropItem(Item.lambchopCooked, 1, 1,
                        Conditional.livestockWell, Conditional.burning, Conditional.butchering),
                new DropItem(Item.leather, 1, 1, 0.5F)));

        // EntityChicken: one feather per feather it still has, then meat once in good condition.
        add(mobs, () -> new MobEntry(new EntityChicken(null), "entity.Chicken.name", LightLevel.animal,
                new DropItem(Item.feather, 0, 2),
                new DropItem(Item.chickenRaw, 1, 1, Conditional.livestockWell, Conditional.notBurning),
                new DropItem(Item.chickenCooked, 1, 1, Conditional.livestockWell, Conditional.burning)));

        // EntityHorse: rand.nextInt(3) + 1 hides, plus beef for the living breeds. Skeleton horses
        // drop bone and zombie horses rotten flesh instead of leather.
        add(mobs, () -> new MobEntry(new EntityHorse(null), "entity.EntityHorse.name", LightLevel.animal,
                new DropItem(Item.leather, 1, 3),
                new DropItem(Item.beefRaw, 1, 2, Conditional.notBurning, Conditional.butchering),
                new DropItem(Item.beefCooked, 1, 2, Conditional.burning, Conditional.butchering)));

        return mobs;
    }

    /** One of the four nuggets a zombie or wight can leave, each a quarter of the 2.5% rare roll. */
    private static DropItem nugget(Item nuggetItem) {
        return new DropItem(nuggetItem, 1, 1, RARE_DROP_CHANCE / 4.0F,
                Conditional.playerKill, Conditional.rareDrop, Conditional.notFallDeath);
    }

    /**
     * The witch's pool, from {@code EntityWitch.witchDrops}. Sticks appear twice, so they are twice as
     * likely; the chance shown is the odds of seeing the item at all across the average three draws.
     */
    private static MobEntry witch() {
        ItemStack[] pool = {
                new ItemStack(Item.glowstone), new ItemStack(Item.sugar), new ItemStack(Item.redstone),
                new ItemStack(Item.spiderEye), new ItemStack(Item.glassBottle), new ItemStack(Item.gunpowder),
                new ItemStack(Item.stick), new ItemStack(Item.knifeFlint),
                new ItemStack(Item.ironNugget), new ItemStack(Item.seeds), new ItemStack(Item.pumpkinSeeds),
                new ItemStack(Item.carrot), new ItemStack(Item.potato), new ItemStack(Item.onion),
                new ItemStack(Block.plantYellow), new ItemStack(Block.plantRed, 1, 2),
                // The potion slot is re-rolled into one of six kinds when it drops
                // (8227, 8261, 16388, 16424, 16426, 16460); fire resistance stands in for the group.
                new ItemStack(Item.potion, 1, 8227)
        };
        // The pool has 18 slots but 17 distinct items: stick occupies two of them.
        int poolSlots = 18;

        List<DropItem> drops = new ArrayList<DropItem>();
        for (ItemStack candidate : pool) {
            int weight = candidate.itemID == Item.stick.itemID ? 2 : 1;
            drops.add(new DropItem(candidate, 1, 5, poolChance(weight, poolSlots), Conditional.looting));
        }
        return new MobEntry(new EntityWitch(null), "entity.Witch.name", LightLevel.hostile,
                drops.toArray(new DropItem[0]));
    }

    /** @return the chance of drawing an entry of the given weight at least once in the average draws. */
    private static float poolChance(int weight, int poolSlots) {
        double missOnce = 1.0 - (double) weight / poolSlots;
        return (float) (1.0 - Math.pow(missOnce, WITCH_AVERAGE_DRAWS));
    }

    /**
     * Builds one entry. Entities are constructed with a null world, which every MITE mob tolerates but
     * which is worth containing per mob rather than losing the whole category to.
     */
    private static void add(List<MobEntry> mobs, Supplier<MobEntry> supplier) {
        try {
            MobEntry entry = supplier.get();
            EntityLivingBase entity = entry.getEntity();
            if (entity == null) {
                return;
            }
            mobs.add(entry);
        } catch (Throwable t) {
            EmiResources.LOGGER.warn("Could not build a mob entry", t);
        }
    }
}
