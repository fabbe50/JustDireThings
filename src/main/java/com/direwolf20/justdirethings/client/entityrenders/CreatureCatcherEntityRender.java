package com.direwolf20.justdirethings.client.entityrenders;

import com.direwolf20.justdirethings.common.entities.CreatureCatcherEntity;
import com.direwolf20.justdirethings.setup.Registration;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public class CreatureCatcherEntityRender extends ThrownItemRenderer<CreatureCatcherEntity> {
    private final ItemRenderer itemRenderer;
    private final float scale;
    private final boolean fullBright;

    public CreatureCatcherEntityRender(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.itemRenderer = pContext.getItemRenderer();
        this.scale = 1f;
        this.fullBright = false;
    }

    @Override
    public void render(CreatureCatcherEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (pEntity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(pEntity) < 12.25)) {
            pPoseStack.pushPose();
            pPoseStack.scale(this.scale, this.scale, this.scale);
            pPoseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            float multiplier = (10f * pEntity.shrinkingTime() % 360);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F + multiplier));
            boolean capturing = pEntity.isCapturing();
            if (!capturing && pEntity.renderTick > 0) {
                this.itemRenderer
                        .renderStatic(
                                new ItemStack(Registration.CreatureCatcher.get()),
                                ItemDisplayContext.GROUND,
                                pPackedLight,
                                OverlayTexture.NO_OVERLAY,
                                pPoseStack,
                                pBuffer,
                                pEntity.level(),
                                pEntity.getId()
                        );
            } else {
                this.itemRenderer
                        .renderStatic(
                                pEntity.getItem(),
                                ItemDisplayContext.GROUND,
                                pPackedLight,
                                OverlayTexture.NO_OVERLAY,
                                pPoseStack,
                                pBuffer,
                                pEntity.level(),
                                pEntity.getId()
                        );
            }
            pPoseStack.popPose();
            ItemStack returnStack = pEntity.getReturnStack();
            Mob mob;
            if (!capturing)
                mob = pEntity.getEntityFromItemStack(pEntity.getItem(), pEntity.level());
            else
                mob = pEntity.getEntityFromItemStack(returnStack, pEntity.level());
            if (mob == null) return;
            mob.yBodyRot = pEntity.getBodyRot();
            mob.yBodyRotO = pEntity.getBodyRot();
            mob.yHeadRot = pEntity.getHeadRot();
            mob.yHeadRotO = pEntity.getHeadRot();
            pPoseStack.pushPose();
            // Calculate the interpolated position
            Vector3f entityPosition = new Vector3f((float) pEntity.getX(), (float) pEntity.getY() + (float) (pEntity.getBoundingBox().getYsize() / 2), (float) pEntity.getZ());
            Vector3f originalPosition;
            float fraction;
            int currentShrinkingTime = Math.min(pEntity.renderTick, pEntity.getAnimationTicks());
            int lastShrinkingTime = pEntity.renderTick == 0 ? 0 : pEntity.renderTick - 1;

            float interpolatedShrinkingTime = Mth.lerp(pPartialTicks, lastShrinkingTime, currentShrinkingTime);

            if (capturing) { //Capturing
                fraction = interpolatedShrinkingTime / (float) pEntity.getAnimationTicks();
                originalPosition = new Vector3f(pEntity.getMobPosition());
            } else { //Releasing
                fraction = (pEntity.getAnimationTicks() - interpolatedShrinkingTime) / (float) pEntity.getAnimationTicks();
                originalPosition = new Vector3f(entityPosition);
            }

            Vector3f interpolatedPosition = originalPosition.lerp(entityPosition, fraction);

            pPoseStack.translate(interpolatedPosition.x() - pEntity.getX(), interpolatedPosition.y() - pEntity.getY(), interpolatedPosition.z() - pEntity.getZ());

            float scale = 1.0f - fraction;
            pPoseStack.scale(scale, scale, scale);

            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            EntityRenderer<? super Mob> renderer = dispatcher.getRenderer(mob);
            renderer.render(mob, mob.yBodyRot, pPartialTicks, pPoseStack, pBuffer, pPackedLight);

            pPoseStack.popPose();
        }
    }
}
