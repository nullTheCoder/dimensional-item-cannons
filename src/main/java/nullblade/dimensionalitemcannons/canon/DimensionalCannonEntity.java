package nullblade.dimensionalitemcannons.canon;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.*;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nullblade.dimensionalitemcannons.DimensionalItemCannons;
import nullblade.dimensionalitemcannons.Utils;
import nullblade.dimensionalitemcannons.items.DimensionalShell;
import nullblade.dimensionalitemcannons.items.DimensionalStone;
import org.jetbrains.annotations.Nullable;

public class DimensionalCannonEntity extends BlockEntity implements Inventory, NamedScreenHandlerFactory {

    public ItemStack toSend = ItemStack.EMPTY;
    public ItemStack fuel = ItemStack.EMPTY;

    public ItemStack dimensionStone = ItemStack.EMPTY;
    
    public static final int FUEL = 0;
    public static final int STONE = 1;
    public static final int SEND = 2;

    public DimensionalCannonEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    @Override
    public int size() {
        return 3;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (slot == FUEL) {
            return stack.getItem() instanceof DimensionalShell;
        } else if (slot == STONE) {
            return stack.getItem() instanceof DimensionalStone;
        } else return slot == SEND;
    }

    @Override
    public boolean isEmpty() {
        return (toSend.isEmpty()) && (fuel.isEmpty()) && dimensionStone.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot == SEND) {
            return toSend;
        } else if (slot == STONE) {
            return dimensionStone;
        }else {
            return fuel;
        }
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (world != null)
            world.updateComparators(pos, DimensionalItemCannons.dimensionItemCanon);
        this.markDirty();
        if (slot == SEND) {
            return !toSend.isEmpty() && amount > 0 ? toSend.split(amount) : ItemStack.EMPTY;
        } else if (slot == STONE) {
            return !dimensionStone.isEmpty() && amount > 0 ? dimensionStone.split(amount) : ItemStack.EMPTY;
        } else {
            return !fuel.isEmpty() && amount > 0 ? fuel.split(amount) : ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (world != null)
            world.updateComparators(pos, DimensionalItemCannons.dimensionItemCanon);
        ItemStack ret;
        if (slot == SEND) {
            ret = toSend;
            toSend = ItemStack.EMPTY;
        } else if (slot == STONE) {
            ret = dimensionStone;
            dimensionStone = ItemStack.EMPTY;
        } else {
            ret = fuel;
            fuel = ItemStack.EMPTY;
        }
        this.markDirty();
        return ret;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (world != null)
            world.updateComparators(pos, DimensionalItemCannons.dimensionItemCanon);
        if (slot == SEND) {
            toSend = stack;
        } else if (slot == STONE) {
            dimensionStone = stack;
        } else {
            fuel = stack;
        }
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {

    }

    public static double SLOPE = 0.5;

    public void activate(BlockState state) {
        if (world instanceof ServerWorld serverWorld) {
            if (dimensionStone.isEmpty()) {
                serverWorld.spawnParticles(ParticleTypes.ANGRY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ()+ 0.5, 16, 0.5, 0.5, 0.5, 1.0);
                return;
            }
            var goal = DimensionalStone.getLocation(dimensionStone, world);
            if (goal == null || goal.getLeft() == null || goal.getRight() == null) {
                serverWorld.spawnParticles(ParticleTypes.ANGRY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ()+ 0.5, 16, 0.5, 0.5, 0.5, 1.0);
                return;
            }

            double xM = 0;
            double zM = 0;
            switch (state.get(Properties.HORIZONTAL_FACING)) {
                case NORTH -> zM = -1;
                case SOUTH -> zM = 1;
                case WEST -> xM = -1;
                case EAST -> xM = 1;
            }

            int tierNeeded = Math.max(Math.max(Utils.getTierNeeded(goal.getLeft()), Utils.getTierNeeded(world)), Utils.getTierNeededForCurrentCoordinates(world, goal, pos));
            int tier = -1;
            if (fuel.getItem() instanceof DimensionalShell shell) {
                tier = shell.tier;
            }
            if (tier == -1 || toSend.isEmpty()) {
                serverWorld.spawnParticles(ParticleTypes.CLOUD, pos.getX() + 0.5 + xM, pos.getY() + 1.2, pos.getZ() + 0.5 + zM, 4, 0, 0, 0, 0.05);
                return;
            }
            markDirty();
            world.updateComparators(pos, state.getBlock());

            boolean sendParticles = true;
            if (tierNeeded > tier) {
                sendParticles = 0 < serverWorld.spawnParticles(ParticleTypes.ANGRY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ()+ 0.5, 16, 0.5, 0.5, 0.5, 1.0);
            } else {
                BlockEntity inventoryEntity = goal.getLeft().getBlockEntity(goal.getRight());

                if (inventoryEntity instanceof Inventory inventory) {
                    Utils.insert(toSend, inventory);
                }
            }

            var e = world.createExplosion(null, null, null, new Vec3d(pos.getX() + 0.5 + xM, pos.getY() + 1.0, pos.getZ() + 0.5 + zM), 1.5F, false, World.ExplosionSourceType.NONE);
            e.affectWorld(true);

            fuel.decrement(1);

            if (sendParticles) {
                if (sendParticles) {
                    sendParticles = 0 < serverWorld.spawnParticles(ParticleTypes.DRAGON_BREATH, pos.getX() + 0.5 + xM * 10, pos.getY() + 0.75 + SLOPE * 10, pos.getZ() + 0.5 + zM * 10,
                            20, 0, 0, 0, 0.1);
                }
                for (int i = 1 ; i < 10 ; i+=1) {
                    serverWorld.spawnParticles(ParticleTypes.DRIPPING_WATER, pos.getX() + 0.5 + xM * i, pos.getY() + 0.75 + i * SLOPE, pos.getZ() + 0.5 + zM * i, 1,
                            0, 0, 0, 0.1);
                }
            }
            if (!toSend.isEmpty()) {
                world.spawnEntity(new ItemEntity(world,
                        pos.getX() + 0.5 + xM * 10, pos.getY() + 0.75 + SLOPE * 10, pos.getZ() + 0.5 + zM * 10,
                        toSend
                ));
                if (sendParticles) {
                    serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, pos.getX() + 0.5 + xM * 10, pos.getY() + 0.75 + SLOPE * 10, pos.getZ() + 0.5 + zM * 10,
                            20, 2.0, 2.0, 2.0, 0.1);
                }
                toSend = ItemStack.EMPTY;
            } else if (goal.getLeft() instanceof ServerWorld wrld) {
                BlockPos pos = goal.getRight();
                sendParticles = 0 < wrld.spawnParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        10, 0.5, 0.5, 0.5, 0.1);
                if (sendParticles) {
                    wrld.spawnParticles(ParticleTypes.DRAGON_BREATH, pos.getX() + 0.5, pos.getY() + 10.5, pos.getZ() + 0.5,
                            10, 0.5, 0.5, 0.5, 0.1);

                    for (int i = 1 ; i < 10 ; i+=1) {
                        wrld.spawnParticles(ParticleTypes.DRIPPING_WATER, pos.getX() + 0.5, pos.getY() + 0.5 + i, pos.getZ() + 0.5,
                                1, 0.0, 0.0, 0.0, 0.1);
                    }
                }

            }

        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        var fuelRead = nbt.getCompound("fuel");
        if (fuelRead != null) {
            fuel = ItemStack.fromNbt(fuelRead);
        } else {
            fuel = ItemStack.EMPTY;
        }

        var itemRead = nbt.getCompound("toSend");
        if (itemRead != null) {
            toSend = ItemStack.fromNbt(itemRead);
        } else {
            toSend = ItemStack.EMPTY;
        }

        var stoneRead = nbt.getCompound("stone");
        if (itemRead != null) {
            dimensionStone = ItemStack.fromNbt(stoneRead);
        } else {
            dimensionStone = ItemStack.EMPTY;
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtCompound save = new NbtCompound();
        fuel.writeNbt(save);
        nbt.put("fuel", save);
        save = new NbtCompound();
        toSend.writeNbt(save);
        nbt.put("toSend", save);
        save = new NbtCompound();
        dimensionStone.writeNbt(save);
        nbt.put("stone", save);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.dimensional_item_cannons.dimensional_item_cannon");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new DimensionalItemCannonScreenHandler(DimensionalItemCannons.screenHandler, syncId, player.getInventory(), this);
    }
}
