package com.direwolf20.justdirethings.common.containers.basecontainers;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.common.containers.slots.FilterBasicSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

public abstract class BaseMachineContainer extends BaseContainer {
    public static final int FILTER_SLOTS = 9;
    public int MACHINE_SLOTS = 0;
    public BaseMachineBE baseMachineBE;
    public FilterBasicHandler filterHandler;
    public ItemStackHandler machineHandler;
    protected Player player;
    protected BlockPos pos;

    public BaseMachineContainer(@Nullable MenuType<?> menuType, int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(menuType, windowId);
        this.pos = blockPos;
        this.player = playerInventory.player;
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (blockEntity instanceof BaseMachineBE baseMachineBE) {
            this.baseMachineBE = baseMachineBE;
            this.MACHINE_SLOTS = baseMachineBE.MACHINE_SLOTS;
        }
        if (blockEntity instanceof FilterableBE filterableBE) {
            filterHandler = filterableBE.getFilterHandler();
            addFilterSlots(filterHandler, 0, 8, 63, FILTER_SLOTS, 18);
        }
        if (MACHINE_SLOTS > 0)
            addMachineSlots();
    }

    //Override this if you want the slot layout to be different...
    public void addMachineSlots() {
        addSlotRange(baseMachineBE.getMachineHandler(), 0, 80, 35, 1, 18);
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (baseMachineBE instanceof FilterableBE && slotId >= 0 && slotId < FILTER_SLOTS) {
            return;
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        if (baseMachineBE instanceof FilterableBE) {
            Slot slot = this.slots.get(index);
            if (slot.hasItem()) {
                if (index >= FILTER_SLOTS) { //Only do this if we click from the players inventory
                    ItemStack currentStack = slot.getItem().copy();
                    currentStack.setCount(1);
                    return quickMoveBasicFilter(currentStack, FILTER_SLOTS);
                }
            }
        }
        if (MACHINE_SLOTS > 0) {
            Slot slot = this.slots.get(index);
            if (slot.hasItem()) {
                ItemStack currentStack = slot.getItem();
                if (index < MACHINE_SLOTS) { //Slot to Player Inventory
                    if (!this.moveItemStackTo(currentStack, MACHINE_SLOTS, Inventory.INVENTORY_SIZE + MACHINE_SLOTS, true)) {
                        return ItemStack.EMPTY;
                    }
                }
                if (index >= MACHINE_SLOTS) { //Player Inventory to Slots
                    if (!this.moveItemStackTo(currentStack, 0, MACHINE_SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                }

                if (currentStack.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }

                if (currentStack.getCount() == itemstack.getCount()) {
                    return ItemStack.EMPTY;
                }

                slot.onTake(playerIn, currentStack);
            }
        }
        return itemstack;
    }

    protected int addFilterSlots(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            if (handler instanceof FilterBasicHandler) //This should always be true, but can't hurt to check!
                addSlot(new FilterBasicSlot(handler, index, x, y));
            else
                addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

}
