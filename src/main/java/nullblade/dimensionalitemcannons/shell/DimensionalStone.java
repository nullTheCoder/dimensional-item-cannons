package nullblade.dimensionalitemcannons.shell;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nullblade.dimensionalitemcannons.DimensionalItemCannons;
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
            var registry = world.getRegistryManager().get(RegistryKeys.WORLD);
            World worldTo = registry.get(new Identifier(worldId));

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

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack item = context.getStack();
        BlockState state =  context.getWorld().getBlockState(context.getBlockPos());
        if (context.getWorld().getBlockEntity(context.getBlockPos()) instanceof Inventory) {
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("written", true);
            nbt.putInt("x", context.getBlockPos().getX());
            nbt.putInt("y", context.getBlockPos().getY());
            nbt.putInt("z", context.getBlockPos().getZ());
            nbt.putString("world", context.getWorld().getRegistryKey().getValue().toString());

            item.setNbt(nbt);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
