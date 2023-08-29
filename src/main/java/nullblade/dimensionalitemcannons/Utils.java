package nullblade.dimensionalitemcannons;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static Map<Identifier, Integer> tierNeeded = new HashMap<>();

    public static int getTierNeeded(World world) {
        return tierNeeded.getOrDefault(world.getDimensionKey().getValue(), 2);
    }

    public static int getTierNeededForCurrentCoordinates(World origin, Pair<World, BlockPos> goal, BlockPos coord) {
         BlockPos goalCords = getAboutNeededCoords(origin, goal);
         if (goalCords == null) {
             return Integer.MAX_VALUE;
         }
         double distance = Math.sqrt(coord.getSquaredDistance(goalCords));
         if (distance <= DimensionalItemCannons.max3DDistance) {
            return 0;
         } else if (DimensionalItemCannons.max3DDistancePerShellTier <= 0) {
             return Integer.MAX_VALUE;
         } else {
             return (int) ((distance - DimensionalItemCannons.max3DDistance) / DimensionalItemCannons.max3DDistancePerShellTier);
         }
    }

    public static String stringify(BlockPos pos) {
        return "[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]";
    }

    public static BlockPos getAboutNeededCoords(World world, Pair<World, BlockPos> goal) {
        if (goal == null || world == null || goal.getLeft() == null) {
            return null;
        }

        Vector3d pos = new Vector3d(goal.getRight().getX(), goal.getRight().getY(), goal.getRight().getZ());

        pos.div(world.getDimension().coordinateScale());
        pos.mul(goal.getLeft().getDimension().coordinateScale());
        pos.y = goal.getRight().getY();

        return new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
    }
    
    public static void insert(ItemStack stack, Inventory inventory) {
        for (int slot = 0 ; slot < inventory.size() ; slot++) {
            if (!inventory.isValid(slot, stack)) {
                continue;
            }
            ItemStack there = inventory.getStack(slot);
            if (there.isEmpty()) {
                inventory.setStack(slot, stack.copyAndEmpty());
                return;
            }
            if (ItemStack.canCombine(there, stack)) {
                int to = Math.min(stack.getCount(), there.getMaxCount() - there.getCount());
                stack.setCount(stack.getCount() - to);
                there.setCount(there.getCount() + to);
            }
            if (stack.getCount() == 0) {
                return;
            }
        }
    }
}
