package nullblade.dimensionalitemcannons.canon;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import nullblade.dimensionalitemcannons.DimensionalItemCannons;
import nullblade.dimensionalitemcannons.shell.DimensionalShell;
import nullblade.dimensionalitemcannons.shell.DimensionalStone;
import org.jetbrains.annotations.Nullable;

public class DimensionalCannonEntity extends BlockEntity implements Inventory, NamedScreenHandlerFactory {

    public ItemStack toSend = ItemStack.EMPTY;
    public ItemStack fuel = ItemStack.EMPTY;

    public ItemStack dimensionStone = ItemStack.EMPTY;

    public DimensionalCannonEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    @Override
    public int size() {
        return 3;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (slot == 0) {
            return stack.getItem() instanceof DimensionalShell;
        } else if (slot == 2) {
            return stack.getItem() instanceof DimensionalStone;
        } else return slot == 1;
    }

    @Override
    public boolean isEmpty() {
        return (toSend.isEmpty()) && (fuel.isEmpty()) && dimensionStone.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot == 1) {
            return toSend;
        } else if (slot == 2) {
            return dimensionStone;
        }else {
            return fuel;
        }
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        this.markDirty();
        if (slot == 1) {
            return !toSend.isEmpty() && amount > 0 ? toSend.split(amount) : ItemStack.EMPTY;
        } else if (slot == 2) {
            return !dimensionStone.isEmpty() && amount > 0 ? dimensionStone.split(amount) : ItemStack.EMPTY;
        } else {
            return !fuel.isEmpty() && amount > 0 ? fuel.split(amount) : ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack ret;
        if (slot == 1) {
            ret = toSend;
            toSend = ItemStack.EMPTY;
        } else if (slot == 2) {
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
        if (slot == 1) {
            toSend = stack;
        } else if (slot == 2) {
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

    public void activate() {

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
        return Text.translatable("dimensional_item_canoncs.dimensional_item_canon_gui");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new DimensionalItemCannonScreenHandler(DimensionalItemCannons.screenHandler, syncId, player.getInventory(), this);
    }
}
