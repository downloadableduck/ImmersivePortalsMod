package qouteall.imm_ptl.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.ResourceLocation;
import qouteall.imm_ptl.core.IPCGlobal;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.mc_utils.WireRenderingHelper;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;

@Environment(EnvType.CLIENT)
public class PortalEntityRenderer extends EntityRenderer<Portal, LivingEntityRenderState> {
    
    public PortalEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(
            LivingEntityRenderState state,
            PoseStack matrixStack,
            MultiBufferSource bufferSource,
            int light
    ) {
        Portal portal = PortalRendering.getRenderingPortal();

        if (portal != null) {
            IPCGlobal.renderer.renderPortalInEntityRenderer(portal);

            if (OverlayRendering.shouldRenderOverlay(portal)) {
                OverlayRendering.onRenderPortalEntity(portal, matrixStack, bufferSource);
            }

            if (IPGlobal.debugRenderPortalShapeMesh && !PortalRendering.isRendering()) {
                VertexConsumer lineVertexConsumer = bufferSource.getBuffer(RenderType.lines());
                WireRenderingHelper.renderPortalShapeMeshDebug(
                        matrixStack, lineVertexConsumer, portal
                );
            }
           }

        super.render(state, matrixStack, bufferSource, light);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    public ResourceLocation getTextureLocation(Portal portal) {
//        if (portal instanceof BreakablePortalEntity) {
//            if (((BreakablePortalEntity) portal).overlayBlockState != null) {
//                return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
//            }
//        }
        return null;
    }
    
    
}
