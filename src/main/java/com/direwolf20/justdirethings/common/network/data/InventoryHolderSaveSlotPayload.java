package com.direwolf20.justdirethings.common.network.data;

import com.direwolf20.justdirethings.JustDireThings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record InventoryHolderSaveSlotPayload(
        int slot
) implements CustomPacketPayload {
    public static final Type<InventoryHolderSaveSlotPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JustDireThings.MODID, "inventory_holder_save_slot"));

    @Override
    public Type<InventoryHolderSaveSlotPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, InventoryHolderSaveSlotPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, InventoryHolderSaveSlotPayload::slot,
            InventoryHolderSaveSlotPayload::new
    );
}
