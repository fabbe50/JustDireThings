package com.direwolf20.justdirethings.common.items;

import com.direwolf20.justdirethings.common.containers.PocketGeneratorContainer;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.NBTUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class PocketGenerator extends Item {
    public static final String ENABLED = "enabled";
    public static final String COUNTER = "counter";
    public static final String MAXBURN = "maxburn";

    public PocketGenerator() {
        super(new Properties()
                .stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        if (player.isShiftKeyDown())
            NBTUtils.toggleBoolean(itemstack, ENABLED);
        else {
            player.openMenu(new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) -> new PocketGeneratorContainer(windowId, playerInventory, player, itemstack), Component.translatable("")), (buf -> {
                buf.writeItem(itemstack);
            }));
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack itemStack, @NotNull Level world, @NotNull Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof Player player && NBTUtils.getBoolean(itemStack, ENABLED)) {
            IEnergyStorage energyStorage = itemStack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (energyStorage == null) return;
            tryBurn(energyStorage, itemStack);
            if (energyStorage.getEnergyStored() >= 1000) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack slotStack = player.getInventory().getItem(i);
                    IEnergyStorage slotEnergy = slotStack.getCapability(Capabilities.EnergyStorage.ITEM);
                    if (slotEnergy != null) {
                        int acceptedEnergy = slotEnergy.receiveEnergy(1000, true);
                        if (acceptedEnergy > 0) {
                            int extractedEnergy = energyStorage.extractEnergy(acceptedEnergy, false);
                            slotEnergy.receiveEnergy(extractedEnergy, false);
                        }
                    }
                }
            }
        }
    }

    private void tryBurn(IEnergyStorage energyStorage, ItemStack itemStack) {
        boolean canInsertEnergy = energyStorage.receiveEnergy(625, true) > 0;
        if (NBTUtils.getIntValue(itemStack, COUNTER) > 0 && canInsertEnergy) {
            burn(energyStorage, itemStack);
        } else if (canInsertEnergy) {
            if (initBurn(itemStack))
                burn(energyStorage, itemStack);
        }
    }


    private void burn(IEnergyStorage energyStorage, ItemStack itemStack) {
        energyStorage.receiveEnergy(625, false);
        int counter = NBTUtils.getIntValue(itemStack, COUNTER);
        counter--;
        NBTUtils.setIntValue(itemStack, COUNTER, counter);
        if (counter == 0) {
            NBTUtils.setIntValue(itemStack, MAXBURN, 0);
            initBurn(itemStack);
        }
    }

    private boolean initBurn(ItemStack itemStack) {
        ItemStackHandler handler = itemStack.getData(Registration.HANDLER);
        ItemStack fuelStack = handler.getStackInSlot(0);

        int burnTime = CommonHooks.getBurnTime(fuelStack, RecipeType.SMELTING);
        if (burnTime > 0) {
            if (fuelStack.hasCraftingRemainingItem())
                handler.setStackInSlot(0, fuelStack.getCraftingRemainingItem());
            else
                fuelStack.shrink(1);


            int counter = (int) Math.floor(burnTime) / 50;
            int maxBurn = counter;
            NBTUtils.setIntValue(itemStack, COUNTER, counter);
            NBTUtils.setIntValue(itemStack, MAXBURN, maxBurn);
            return true;
        }
        return false;
    }

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return NBTUtils.getBoolean(itemStack, ENABLED);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }
}
