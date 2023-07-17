package nullblade.dimensionalitemcannons.canon;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import nullblade.dimensionalitemcannons.DimensionalItemCannons;
import nullblade.dimensionalitemcannons.shell.DimensionalShell;
import nullblade.dimensionalitemcannons.shell.DimensionalStone;
import org.jetbrains.annotations.Nullable;

public class DimensionalItemCannonScreenHandler extends ScreenHandler {

    public Inventory inventory;
    protected DimensionalItemCannonScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory player, Inventory entity) {
        super(type, syncId);
        inventory = entity;
        inventory.onOpen(player.player);


        this.addSlot(new Slot(inventory, 1, 99, 33));
        this.addSlot(new Slot(inventory, 0, 41, 33) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof DimensionalShell;
            }
        });

        this.addSlot(new Slot(inventory, 2, 152, 61) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof DimensionalStone;
            }
        });


        int m;
        int l;
        //The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(player, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
        //The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(player, m, 8 + m * 18, 142));
        }

    }

    public DimensionalItemCannonScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(DimensionalItemCannons.screenHandler, syncId, playerInventory, new SimpleInventory(3));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        inventory.onClose(player);
        super.onClosed(player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }
}
