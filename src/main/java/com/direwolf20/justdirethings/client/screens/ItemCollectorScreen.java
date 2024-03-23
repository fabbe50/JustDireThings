package com.direwolf20.justdirethings.client.screens;

import com.direwolf20.justdirethings.JustDireThings;
import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.GrayscaleButton;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.common.containers.ItemCollectorContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ItemCollectorScreen extends BaseMachineScreen<ItemCollectorContainer> {
    private final ResourceLocation GUI = new ResourceLocation(JustDireThings.MODID, "textures/gui/itemcollector.png");

    public ItemCollectorScreen(ItemCollectorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(ToggleButtonFactory.REDSTONEBUTTON(getGuiLeft() + 134, getGuiTop() + 45, redstoneMode.ordinal(), b -> {
            redstoneMode = redstoneMode.next();
            ((ToggleButton) b).nextTexturePosition();
            saveSettings();
        }));

        addRenderableWidget(ToggleButtonFactory.ALLOWLISTBUTTON(getGuiLeft() + 8, getGuiTop() + 45, allowlist, b -> {
            allowlist = !allowlist;
            ((GrayscaleButton) b).toggleActive();
            saveSettings();
        }));

        addRenderableWidget(ToggleButtonFactory.COMPARENBTBUTTON(getGuiLeft() + 26, getGuiTop() + 45, compareNBT, b -> {
            compareNBT = !compareNBT;
            ((GrayscaleButton) b).toggleActive();
            saveSettings();
        }));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        return super.mouseClicked(x, y, btn);
    }
}
