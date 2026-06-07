package qouteall.imm_ptl.core.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuSampler;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.render.FrontClipping;
import qouteall.imm_ptl.core.render.MyRenderHelper;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;

@Mixin(ChunkSectionsToRender.class)
public class ChunkSectionsToRenderMixin {
    @Inject(
            method = "renderGroup",
            at = @At(
                    value = "HEAD"
            )
    )
    private void onBeforeRenderingLayer(
            ChunkSectionLayerGroup chunkSectionLayerGroup, GpuSampler gpuSampler, CallbackInfo ci
    ) {

        if (PortalRendering.isRendering()) {
            FrontClipping.setupInnerClipping(
                    PortalRendering.getActiveClippingPlane(),
                    RenderSystem.getModelViewStack(),
                    -FrontClipping.ADJUSTMENT
                    // move the clipping plane a little back, to make world wrapping portal not z-fight
            );

            if (PortalRendering.isRenderingOddNumberOfMirrors()) {
                MyRenderHelper.applyMirrorFaceCulling();
            }

            if (IPGlobal.enableDepthClampForPortalRendering) {
                CHelper.enableDepthClamp();
            }
        }
    }
    @Inject(
            method = "renderGroup",
            at = @At(
                    value = "TAIL",
                    shift = At.Shift.AFTER
            )
    )
    private void onAfterRenderingLayer(
            ChunkSectionLayerGroup chunkSectionLayerGroup, GpuSampler gpuSampler, CallbackInfo ci
    ) {
        if (PortalRendering.isRendering()) {
            FrontClipping.disableClipping();
            MyRenderHelper.recoverFaceCulling();

            if (IPGlobal.enableDepthClampForPortalRendering) {
                CHelper.disableDepthClamp();
            }
        }
    }
}
