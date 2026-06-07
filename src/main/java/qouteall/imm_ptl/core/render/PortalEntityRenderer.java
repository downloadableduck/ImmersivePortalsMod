package qouteall.imm_ptl.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import qouteall.imm_ptl.core.IPCGlobal;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.mc_utils.WireRenderingHelper;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;

@Environment(EnvType.CLIENT)
public class PortalEntityRenderer extends EntityRenderer<Portal, EntityRenderState> {
    
    public PortalEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    public void render(
        Portal portal,
        float yaw,
        float partialTick,
        PoseStack matrixStack,
        MultiBufferSource bufferSource,
        int light
    ) {
        
        IPCGlobal.renderer.renderPortalInEntityRenderer(portal);
        
        if (OverlayRendering.shouldRenderOverlay(portal)) {
            OverlayRendering.onRenderPortalEntity(portal, matrixStack, bufferSource);
        }
    
        if (IPGlobal.debugRenderPortalShapeMesh && !PortalRendering.isRendering()) {
            VertexConsumer lineVertexConsumer = bufferSource.getBuffer(RenderTypes.lines());
            WireRenderingHelper.renderPortalShapeMeshDebug(
                matrixStack, lineVertexConsumer, portal
            );
        }
        
        super.submit(this.createRenderState(), new PoseStack(), Minecraft.getInstance().gameRenderer.getSubmitNodeStorage(), Minecraft.getInstance().gameRenderer.getLevelRenderState().cameraRenderState);
    }
    
    public Identifier getTextureLocation(EntityRenderState portal) {
//        if (portal instanceof BreakablePortalEntity) {
//            if (((BreakablePortalEntity) portal).overlayBlockState != null) {
//                return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
//            }
//        }
        return null;
    }
    
    
}
