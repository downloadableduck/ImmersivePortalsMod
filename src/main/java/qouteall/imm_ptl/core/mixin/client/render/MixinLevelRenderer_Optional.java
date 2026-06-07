package qouteall.imm_ptl.core.mixin.client.render;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.render.FrontClipping;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import qouteall.imm_ptl.core.render.context_management.RenderStates;
import qouteall.imm_ptl.core.render.context_management.WorldRenderInfo;

// avoid crashing with sodium
// the overwrite has priority of 1000
@Mixin(value = LevelRenderer.class, priority = 1100)
public class MixinLevelRenderer_Optional {
    @Shadow
    private ViewArea viewArea;
    
    @Shadow
    @Final
    private Minecraft minecraft;
    
    //avoid translucent sort while rendering portal
    @Redirect(
        method = "renderSectionLayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderType;translucent()Lnet/minecraft/client/renderer/RenderType;",
            ordinal = 0
        ),
        require = 0
    )
    private RenderType redirectGetTranslucent() {
        if (PortalRendering.isRendering()) {
            return null;
        }
        return RenderType.translucent();
    }
    
    //the camera position is used for translucent sort
    //avoid messing it
    @Redirect(
        method = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher;setCamera(Lnet/minecraft/world/phys/Vec3;)V"
        ),
        require = 0
    )
    private void onSetChunkBuilderCameraPosition(
        SectionRenderDispatcher chunkBuilder, Vec3 cameraPosition
    ) {
        if (PortalRendering.isRendering()) {
            if (minecraft.level.dimension() == RenderStates.originalPlayerDimension) {
                return;
            }
        }
        chunkBuilder.setCamera(cameraPosition);
    }
    
    @Inject(
        method = "renderSectionLayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/CompiledShaderProgram;apply()V"
        ),
        require = 0
    )
    private void onGetShaderInRenderingLayer(
        RenderType renderType, double x, double y, double z, Matrix4f projectionMatrix, Matrix4f frustrumMatrix, CallbackInfo ci
    ) {
        FrontClipping.updateClippingEquationUniformForCurrentShader(false);
    }
    
    // correct the position of updating ViewArea
    @Redirect(
        method = "setupRender",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getXRot()F"),
        require = 0
    )
    private float redirectGetXInSetupRender(Camera instance) {
        if (WorldRenderInfo.isRendering()) {
            return (float) WorldRenderInfo.getCameraPos().x;
        }
        return instance.getXRot();
    }
    
    // biolerplate
    @Redirect(
        method = "setupRender",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getYRot()F"),
        require = 0
    )
    private float redirectGetYInSetupRender(Camera instance) {
        if (WorldRenderInfo.isRendering()) {
            return (float) WorldRenderInfo.getCameraPos().y;
        }
        return instance.getYRot();
    }
    
    // biolerplate
    @Redirect(
        method = "setupRender",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;z()D"),
        require = 0
    )
    private double redirectGetZInSetupRender(Vec3 instance) {
        if (WorldRenderInfo.isRendering()) {
            return WorldRenderInfo.getCameraPos().z;
        }
        return instance.z();
    }
}
