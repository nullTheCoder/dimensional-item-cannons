package nullblade.dimensionalitemcannons.items;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nullblade.dimensionalitemcannons.DimensionalItemCannons;
import nullblade.dimensionalitemcannons.Utils;
import nullblade.dimensionalitemcannons.canon.DimensionalCannonEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DimensionalStone extends Item {

    public DimensionalStone(Settings settings) {
        super(settings);
    }

    public static Pair<World, BlockPos> getLocation(ItemStack stack, World world) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) {
            return null;
        }
        if (nbt.getBoolean("written")) {
            int x = nbt.getInt("x");
            int y = nbt.getInt("y");
            int z = nbt.getInt("z");
            String worldId = nbt.getString("world");
            if (world.getServer() == null) {
                return null;
            }
            World worldTo = world.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(worldId)));

            return new Pair<>(worldTo, new BlockPos(x, y, z));
        }
        return null;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.getBoolean("written")) {
            tooltip.add(Text.translatable(DimensionalItemCannons.id + ".dimstone.unset"));
        } else {
            tooltip.add(Text.translatable(DimensionalItemCannons.id + ".dimstone.cords", nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z")));
            tooltip.add(Text.translatable(DimensionalItemCannons.id + ".dimstone.world", nbt.getString("world")));
        }

        super.appendTooltip(stack, world, tooltip, context);
    }

    public static void printRequirements(@Nullable PlayerEntity player, ItemStack stack, @Nullable BlockPos pos) {
        if (player == null || player.getWorld().isClient()) {
            return;
        }
        var data = getLocation(stack, player.getWorld());
        if (data != null) {
            int tierNeeded = Math.max(Utils.getTierNeeded(player.getWorld()), Utils.getTierNeeded(data.getLeft()));
            if (pos != null) {
                int tier = Utils.getTierNeededForCurrentCoordinates(player.getWorld(), data, pos);
                if (tier > tierNeeded) {
                    player.sendMessage(Text.translatable(DimensionalItemCannons.id + ".dimstone.posWrite",
                            DimensionalItemCannons.max3DDistance + DimensionalItemCannons.max3DDistancePerShellTier * tierNeeded,
                            Utils.stringify(Utils.getAboutNeededCoords(player.getWorld(), data)),
                            tierNeeded
                    ));
                    tierNeeded = tier;
                }
            }
            BlockPos p = data.getRight();
            player.sendMessage(Text.translatable(DimensionalItemCannons.id + ".dimstone.write",
                    data.getLeft().getDimensionKey().getValue().toString(),
                    Utils.stringify(p),
                    tierNeeded
                    ));
        } else {
            player.sendMessage(Text.translatable(DimensionalItemCannons.id + ".dimstone.unset"));
        }

    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack item = context.getStack();
        BlockEntity entity = context.getWorld().getBlockEntity(context.getBlockPos());
        if (entity instanceof DimensionalCannonEntity) {
            printRequirements(context.getPlayer(), item, context.getBlockPos());
            return ActionResult.SUCCESS;
        } else if (entity instanceof Inventory) {
            NbtCompound nbt = new NbtCompound();
            item.addEnchantment(Enchantments.AQUA_AFFINITY, 1);
            item.addHideFlag(ItemStack.TooltipSection.ENCHANTMENTS);
            nbt.putBoolean("written", true);
            nbt.putInt("x", context.getBlockPos().getX());
            nbt.putInt("y", context.getBlockPos().getY());
            nbt.putInt("z", context.getBlockPos().getZ());
            nbt.putString("world", context.getWorld().getRegistryKey().getValue().toString());

            item.setNbt(nbt);
            return ActionResult.SUCCESS;
        }
        printRequirements(context.getPlayer(), item, context.getBlockPos());
        return ActionResult.PASS;
    }

}
