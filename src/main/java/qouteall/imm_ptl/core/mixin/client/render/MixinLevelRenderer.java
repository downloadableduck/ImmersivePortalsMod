package qouteall.imm_ptl.core.mixin.client.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledSectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.IPCGlobal;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.compat.iris_compatibility.IrisInterface;
import qouteall.imm_ptl.core.compat.sodium_compatibility.SodiumInterface;
import qouteall.imm_ptl.core.ducks.IEWorldRenderer;
import qouteall.imm_ptl.core.miscellaneous.IPVanillaCopy;
import qouteall.imm_ptl.core.render.CrossPortalEntityRenderer;
import qouteall.imm_ptl.core.render.FrontClipping;
import qouteall.imm_ptl.core.render.ImmPtlViewArea;
import qouteall.imm_ptl.core.render.MyGameRenderer;
import qouteall.imm_ptl.core.render.MyRenderHelper;
import qouteall.imm_ptl.core.render.VisibleSectionDiscovery;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import qouteall.imm_ptl.core.render.context_management.RenderStates;
import qouteall.imm_ptl.core.render.context_management.WorldRenderInfo;
import qouteall.q_misc_util.Helper;

import static net.minecraft.client.renderer.chunk.CompiledSectionMesh.UNCOMPILED;
import static qouteall.imm_ptl.core.render.CrossPortalEntityRenderer.renderEntity;

@SuppressWarnings("JavadocReference")
@Mixin(value = LevelRenderer.class)
public abstract class MixinLevelRenderer implements IEWorldRenderer {

    @Shadow
    private ClientLevel level;

    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private ViewArea viewArea;

    @Mutable
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Shadow
    private int lastViewDistance;


    @Shadow
    private @Nullable SectionRenderDispatcher sectionRenderDispatcher;

    @Shadow
    @Final
    @Mutable
    private ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections;

    @Shadow
    @org.jspecify.annotations.Nullable
    protected abstract PostChain getTransparencyChain();

    @Shadow
    protected abstract EntityRenderState extractEntity(Entity entity, float f);

    @Shadow
    private Frustum capturedFrustum;

    @Shadow
    private @org.jspecify.annotations.Nullable RenderTarget entityOutlineTarget;
    @Shadow
    private @org.jspecify.annotations.Nullable SkyRenderer skyRenderer;
    @Shadow
    private @org.jspecify.annotations.Nullable GpuSampler chunkLayerSampler;
    @Shadow
    @Final
    private CloudRenderer cloudRenderer;

    @Shadow
    protected abstract boolean shouldShowEntityOutlines();

    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(Minecraft minecraft, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, RenderBuffers renderBuffers, LevelRenderState levelRenderState, FeatureRenderDispatcher featureRenderDispatcher, CallbackInfo ci) {
         //this.transparencyChain = this.minecraft.getShaderManager().getPostChain(Identifier.withDefaultNamespace("transparency"), LevelTargetBundle.SORTING_TARGETS);
    }
    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;addMainPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/culling/Frustum;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZLnet/minecraft/client/renderer/state/LevelRenderState;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/util/profiling/ProfilerFiller;)V"
        )
    )
    private void onAfterCutoutRendering(
            GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci
    ) {
//        IPCGlobal.renderer.onBeforeTranslucentRendering(matrices);

        CrossPortalEntityRenderer.onBeginRenderingEntitiesAndBlockEntities(matrix4f);
    }

    @Inject(
        method = "method_62214",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V"
        )
    )
    private void onMyBeforeTranslucentRendering(
            GpuBufferSlice gpuBufferSlice, LevelRenderState levelRenderState, ProfilerFiller profilerFiller, Matrix4f matrix4f, ResourceHandle resourceHandle, ResourceHandle resourceHandle2, boolean bl, ResourceHandle resourceHandle3, ResourceHandle resourceHandle4, CallbackInfo ci
    ) {
        IPCGlobal.renderer.onBeforeTranslucentRendering(matrix4f);

        MyGameRenderer.updateFogColor();
        MyGameRenderer.resetFogState();

        MyGameRenderer.resetDiffuseLighting();

        FrontClipping.disableClipping();
    }

    @IPVanillaCopy
    @Inject(
        method = "method_62214",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endLastBatch()V",
            ordinal = 1, // the second occurrence
            shift = At.Shift.AFTER
        ), remap = false
    )
    private void onEndRenderingEntities(
            GpuBufferSlice gpuBufferSlice, LevelRenderState levelRenderState, ProfilerFiller profilerFiller, Matrix4f matrix4f, ResourceHandle resourceHandle, ResourceHandle resourceHandle2, boolean bl, ResourceHandle resourceHandle3, ResourceHandle resourceHandle4, CallbackInfo ci, @Local PoseStack poseStack
    ) {
        CrossPortalEntityRenderer.onEndRenderingEntitiesAndBlockEntities(poseStack);
    }

    @Inject(
        method = "renderLevel",
        at = @At("RETURN")
    )
    private void onAfterTranslucentRendering(
            GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci
    ) {
        IPCGlobal.renderer.onAfterTranslucentRendering(matrix4f);

        // make hand rendering normal
        new Lighting().setupFor(Lighting.Entry.LEVEL);
    }

    @Inject(
        method = "cullTerrain",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onSetupTerrainBegin(
            Camera camera, Frustum frustum, boolean bl, CallbackInfo ci
    ) {
        if (WorldRenderInfo.isRendering()) {
            if (level.dimension() != RenderStates.originalPlayerDimension) {
                sectionRenderDispatcher.setCameraPosition(camera.position());
            }
        }

        if (ip_allowOverrideTerrainSetup()) {
            if (WorldRenderInfo.isRendering()) {
                ProfilerFiller filler = Profiler.get();
                filler.push("ip_terrain_setup");
                VisibleSectionDiscovery.discoverVisibleSections(
                    level, ((ImmPtlViewArea) viewArea),
                    camera,
                    new Frustum(frustum).offsetToFullyIncludeCameraCube(8),
                    visibleSections
                );
                filler.pop();

                ci.cancel();
            }
        }
    }

    private boolean ip_allowOverrideTerrainSetup() {
        return !SodiumInterface.invoker.isSodiumPresent()
            && !IrisInterface.invoker.isRenderingShadowMap();
    }

    @Inject(
        method = "cullTerrain",
        at = @At("RETURN"),
        cancellable = true
    )
    private void onSetupTerrainEnd(
            Camera camera, Frustum frustum, boolean bl, CallbackInfo ci
    ) {

        ProfilerFiller profiler = Profiler.get();

        if (!WorldRenderInfo.isRendering()) {
            if (ip_allowOverrideTerrainSetup()) {
                if (MyGameRenderer.vanillaTerrainSetupOverride > 0) {
                    MyGameRenderer.vanillaTerrainSetupOverride--;

                    profiler.push("ip_terrain_setup");
                    VisibleSectionDiscovery.discoverVisibleSections(
                        level, ((ImmPtlViewArea) viewArea),
                        camera,
                        new Frustum(frustum).offsetToFullyIncludeCameraCube(8),
                        visibleSections
                    );
                    profiler.pop();
                }
                else if (IPGlobal.alwaysOverrideTerrainSetup) {
                    // debug
                    profiler.push("ip_terrain_setup_debug");
                    VisibleSectionDiscovery.discoverVisibleSections(
                        level, ((ImmPtlViewArea) viewArea),
                        camera,
                        new Frustum(frustum).offsetToFullyIncludeCameraCube(8),
                        visibleSections
                    );
                    profiler.pop();
                }
            }
        }
    }

    @Redirect(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelTargetBundle;clear()V",
            remap = false
        )
    )
    private void redirectClearing(LevelTargetBundle instance) {
        if (!IPCGlobal.renderer.replaceFrameBufferClearing()) {
            instance.clear();
        }
    }

    @Redirect(
        method = "allChanged",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher;Lnet/minecraft/world/level/Level;ILnet/minecraft/client/renderer/LevelRenderer;)Lnet/minecraft/client/renderer/ViewArea;"
        )
    )
    private ViewArea redirectConstructingBuildChunkStorage(
        SectionRenderDispatcher chunkBuilder_1,
        Level world_1,
        int int_1,
        LevelRenderer worldRenderer_1
    ) {
        if (IPCGlobal.useHackedChunkRenderDispatcher) {
            return new ImmPtlViewArea(
                chunkBuilder_1, world_1, int_1, worldRenderer_1
            );
        }
        else {
            return new ViewArea(
                chunkBuilder_1, world_1, int_1, worldRenderer_1
            );
        }
    }

    // @Inject does not allow getting the entity reference
    // maybe needs Mixin Extra
    @Redirect(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;extractVisibleEntities(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/renderer/state/LevelRenderState;)V"
        )
    )
    private void redirectRenderEntity(
            LevelRenderer instance, Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState levelRenderState
     ) {
        for (Entity entity : this.level.entitiesForRendering()) {
            CrossPortalEntityRenderer.beforeRenderingEntity(entity, new PoseStack());
            extractEntity(
                    entity,
                    deltaTracker.getGameTimeDeltaPartialTick(false)
            );
            CrossPortalEntityRenderer.afterRenderingEntity(entity);
        }
    }

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;addWeatherPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"
    ))
    private void beforeRenderingWeather(
            GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci
    ) {
        if (PortalRendering.isRendering()) {
            FrontClipping.setupInnerClipping(
                PortalRendering.getActiveClippingPlane(),
                matrix4f, 0
            );
            RenderStates.isRenderingPortalWeather = true;
        }
    }

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;addWeatherPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V",
            shift = At.Shift.AFTER
        )
    )
    private void afterRenderingWeather(
            GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci
    ) {
        if (PortalRendering.isRendering()) {
            FrontClipping.disableClipping();
            RenderStates.isRenderingPortalWeather = false;
        }
    }

    //avoid render glowing entities when rendering portal
    @Redirect(
        method = "method_62214",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;shouldShowEntityOutlines()Z"
        )
    )
    private boolean redirectGlowing(LevelRenderer instance) {
        if (WorldRenderInfo.isRendering()) {
            return false;
        }
        return this.shouldShowEntityOutlines();
    }

    // sometimes we change renderDistance but we don't want to reload it
    @Inject(method = "allChanged", at = @At("HEAD"), cancellable = true)
    private void onReloadStarted(CallbackInfo ci) {
        if (WorldRenderInfo.isRendering()) {
            Helper.log("world renderer reloading cancelled during portal rendering");
            ci.cancel();
        }
    }

    //reload other world renderers when the main world renderer is reloaded
    @Inject(method = "allChanged", at = @At("TAIL"))
    private void onReloadFinished(CallbackInfo ci) {
        LevelRenderer this_ = (LevelRenderer) (Object) this;

        if (ClientWorldLoader.getIsCreatingClientWorld()) {
            return;
        }

        Validate.isTrue(Minecraft.getInstance().levelRenderer == this_);

        ClientWorldLoader._onWorldRendererReloaded();
    }

    @Inject(
        method = "addSkyPass", at = @At("HEAD"), cancellable = true
    )
    private void onRenderSkyBegin(
            FrameGraphBuilder frameGraphBuilder, Camera camera, GpuBufferSlice gpuBufferSlice, CallbackInfo ci
    ) {
        if (WorldRenderInfo.isRendering()) {
            if (!WorldRenderInfo.getTopRenderInfo().doRenderSky) {
                if (!IrisInterface.invoker.isShaders()) {
                    ci.cancel();
                }
            }
        }

        if (PortalRendering.isRenderingOddNumberOfMirrors()) {
            MyRenderHelper.applyMirrorFaceCulling();
        }
    }

    @Inject(
        method = "addSkyPass",
        at = @At("RETURN")
    )
    private void onRenderSkyEnd(
            FrameGraphBuilder frameGraphBuilder, Camera camera, GpuBufferSlice gpuBufferSlice, CallbackInfo ci
    ) {
        MyRenderHelper.recoverFaceCulling();
    }

    // correct the eye position for sky rendering
    //commented out code
    /*@Redirect(
        method = "addSkyPass",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getEyePosition(F)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 redirectGetEyePositionInSkyRendering(LocalPlayer player, float partialTicks) {
        if (WorldRenderInfo.isRendering()) {
            return WorldRenderInfo.getCameraPos();
        }
        return player.getEyePosition(partialTicks);
    }*/

    // vanilla clears translucentFramebuffer even when transparencyShader is null
    // it makes the framebuffer to be wrongly bound in fabulous mode
    @Redirect(
        method = "renderLevel",
        at = @At(
                value = "FIELD",
                target = "Lnet/minecraft/client/renderer/LevelTargetBundle;translucent:Lcom/mojang/blaze3d/resource/ResourceHandle;",
                opcode = Opcodes.PUTFIELD)
    )
    private void redirectTranslucentFramebuffer(LevelTargetBundle instance, @org.jspecify.annotations.Nullable ResourceHandle<RenderTarget> value) {
        if (PortalRendering.isRendering()) {
            instance.translucent = null;
        }
        else {
            instance.translucent = instance.translucent;
        }
    }

    // if not in spectator mode, when the camera is in block chunk culling will cull chunks wrongly
    @ModifyVariable(
        method = "cullTerrain",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private boolean modifyIsSpectator(boolean value) {
        if (WorldRenderInfo.isRendering()) {
            return true;
        }
        return value;
    }

    // the captured lambda uses the net handler's world field
    // so switch that correctly
    @Redirect(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;pollLightUpdates()V"
        )
    )
    private void redirectRunQueuedChunkUpdates(ClientLevel world) {
        ClientWorldLoader.withSwitchedWorld(
            world, world::pollLightUpdates
        );
    }

    /**
     * when rendering portal, it won't call {@link ViewArea#repositionCamera(double, double)}
     * So {@link ViewArea#getRenderSectionAt} will return incorrect result
     */
    @Inject(
        method = "compileSections",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onIsChunkCompiled(Camera camera, CallbackInfo ci) {
        if (PortalRendering.isRendering()) {
            if (!SodiumInterface.invoker.isSodiumPresent()) {
                if (viewArea instanceof ImmPtlViewArea immPtlViewArea) {
                    //cir.setReturnValue(ip_isChunkCompiled(immPtlViewArea, camera.blockPosition()));
                }
            }
        }
    }

    private boolean ip_isChunkCompiled(ImmPtlViewArea immPtlViewArea, BlockPos blockPos) {
        SectionPos sectionPos = SectionPos.of(blockPos);
        var renderChunk = immPtlViewArea.rawGet(
            sectionPos.x(), sectionPos.y(), sectionPos.z()
        );

        return renderChunk != null
            && renderChunk.getSectionMesh() != UNCOMPILED;
    }

    @Override
    public EntityRenderDispatcher ip_getEntityRenderDispatcher() {
        return entityRenderDispatcher;
    }

    @Override
    public ViewArea ip_getBuiltChunkStorage() {
        return viewArea;
    }

    @Override
    public void ip_myRenderEntity(
        Entity entity,
        double cameraX,
        double cameraY,
        double cameraZ,
        float partialTick,
        PoseStack matrixStack,
        MultiBufferSource vertexConsumerProvider
    ) {
        extractEntity(
            entity, partialTick
        );
    }

    @Override
    public PostChain portal_getTransparencyShader() {
        return this.getTransparencyChain();
    }

    @Override
    public void portal_setTransparencyShader(PostChain arg) {
       // this.transparencyChain  = arg;
    }

    @Override
    public RenderBuffers ip_getRenderBuffers() {
        return renderBuffers;
    }

    @Override
    public void ip_setRenderBuffers(RenderBuffers arg) {
        renderBuffers = arg;
    }

    @Override
    public Frustum portal_getFrustum() {
        return capturedFrustum;
    }

    @Override
    public void portal_setFrustum(Frustum arg) {
        capturedFrustum = arg;
    }

    @Override
    public void portal_fullyDispose() {
        //deinitTransparency();

        /*if (starBuffer != null) {
            starBuffer.close();
        }
        if (skyBuffer != null) {
            skyBuffer.close();
        }
        if (darkBuffer != null) {
            darkBuffer.close();
        }
        if (cloudBuffer != null) {
            cloudBuffer.close();
        }*/
        if (this.entityOutlineTarget != null) {
            this.entityOutlineTarget.destroyBuffers();
        }

        if (this.skyRenderer != null) {
            this.skyRenderer.close();
        }

        if (this.chunkLayerSampler != null) {
            this.chunkLayerSampler.close();
        }

        this.cloudRenderer.close();
        level = null;
    }

    @Override
    public void portal_setChunkInfoList(ObjectArrayList<SectionRenderDispatcher.RenderSection> arg) {
        visibleSections = arg;
    }

    @Override
    public ObjectArrayList<SectionRenderDispatcher.RenderSection> portal_getChunkInfoList() {
        return visibleSections;
    }
}
