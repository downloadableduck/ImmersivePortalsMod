package qouteall.imm_ptl.core.mixin.client.render.optimization;

import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.miscellaneous.IPVanillaCopy;
import qouteall.imm_ptl.core.render.context_management.CloudContext;
import qouteall.imm_ptl.core.render.context_management.RenderStates;

// Optimize cloud rendering by storing the context and
// avoiding rebuild the cloud mesh every time
@Mixin(CloudRenderer.class)
public abstract class MixinLevelRenderer_Clouds {
    
    @Shadow
    private int prevCellX;

    private int prevCloudY = 1;
    
    @Shadow
    private int prevCellZ;
    
    @Mutable
    @Final
    @Shadow
    @Nullable
    public VertexBuffer vertexBuffer;

    private ClientLevel level = Minecraft.getInstance().level;
    
    @Shadow
    private boolean vertexBufferEmpty;
    
    private int ticks = 1;
    
    @Inject(
        method = "render",
        at = @At("HEAD")
    )
    private void onBeginRenderClouds(
            int i, CloudStatus cloudStatus, float f, Matrix4f matrix4f, Matrix4f matrix4f2, Vec3 cameraPos, float partialTick, CallbackInfo ci
    ) {
        if (RenderStates.getRenderedPortalNum() == 0) {
            return;
        }
        
        if (IPGlobal.cloudOptimization) {
            portal_onBeginCloudRendering(partialTick, cameraPos.x, cameraPos.y, cameraPos.z);
        }
    }
    
    @Inject(
        method = "render",
        at = @At("RETURN")
    )
    private void onEndRenderClouds(int i, CloudStatus cloudStatus, float f, Matrix4f matrix4f, Matrix4f matrix4f2, Vec3 vec3, float g, CallbackInfo ci) {
        if (RenderStates.getRenderedPortalNum() == 0) {
            return;
        }
        
        if (IPGlobal.cloudOptimization) {
            portal_onEndCloudRendering();
        }
    }
    
    private void portal_yieldCloudContext(CloudContext context) {
        Vec3 cloudsColor = this.getCloudColor(this.level, RenderStates.getPartialTick());
        
        context.lastCloudsBlockX = prevCellX;
        context.lastCloudsBlockY = prevCloudY;
        context.lastCloudsBlockZ = prevCellZ;
        context.cloudsBuffer = vertexBuffer;
        context.dimension = level.dimension();
        context.cloudColor = cloudsColor;
        
        vertexBuffer = null;
        vertexBufferEmpty = true;
    }
    
    private void portal_loadCloudContext(CloudContext context) {
        Validate.isTrue(context.dimension == level.dimension());
        
        prevCellX = context.lastCloudsBlockX;
        prevCloudY = context.lastCloudsBlockY;
        prevCellZ = context.lastCloudsBlockZ;
        vertexBuffer = context.cloudsBuffer;
        
        vertexBufferEmpty = false;
    }
    
    /**
     * {@link LevelRenderer#renderClouds}
     */
    @IPVanillaCopy
    private void portal_onBeginCloudRendering(
        float partialTick, double cameraX, double cameraY, double cameraZ
    ) {
        float f = this.level.effects().getCloudHeight();
        float g = 12.0F;
        float h = 4.0F;
        double d = 2.0E-4D;
        double e = (double) (((float) this.ticks + partialTick) * 0.03F);
        double i = (cameraX + e) / 12.0D;
        double j = (double) (f - (float) cameraY + 0.33F);
        double k = cameraZ / 12.0D + 0.33000001311302185D;
        i -= (double) (Mth.floor(i / 2048.0D) * 2048);
        k -= (double) (Mth.floor(k / 2048.0D) * 2048);
        float l = (float) (i - (double) Mth.floor(i));
        float m = (float) (j / 4.0D - (double) Mth.floor(j / 4.0D)) * 4.0F;
        float n = (float) (k - (double) Mth.floor(k));
        Vec3 cloudsColor = this.getCloudColor(this.level, partialTick);
        int kx = (int) Math.floor(i);
        int ky = (int) Math.floor(j / 4.0D);
        int kz = (int) Math.floor(k);
        
        @Nullable CloudContext context = CloudContext.findAndTakeContext(
            kx, ky, kz, level.dimension(), cloudsColor
        );
        
        if (context != null) {
            portal_loadCloudContext(context);
        }
    }
    
    private void portal_onEndCloudRendering() {
        if (!vertexBufferEmpty) {
            final CloudContext newContext = new CloudContext();
            portal_yieldCloudContext(newContext);
            
            CloudContext.appendContext(newContext);
        }
    }

    public Vec3 getCloudColor(ClientLevel this_, float f) {
        float g = this_.getTimeOfDay(f);
        float h = Mth.cos(g * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
        h = Mth.clamp(h, 0.0F, 1.0F);
        float i = 1.0F;
        float j = 1.0F;
        float k = 1.0F;
        float l = this_.getRainLevel(f);
        if (l > 0.0F) {
            float m = (i * 0.3F + j * 0.59F + k * 0.11F) * 0.6F;
            float n = 1.0F - l * 0.95F;
            i = i * n + m * (1.0F - n);
            j = j * n + m * (1.0F - n);
            k = k * n + m * (1.0F - n);
        }

        i *= h * 0.9F + 0.1F;
        j *= h * 0.9F + 0.1F;
        k *= h * 0.85F + 0.15F;
        float m = this_.getThunderLevel(f);
        if (m > 0.0F) {
            float n = (i * 0.3F + j * 0.59F + k * 0.11F) * 0.2F;
            float o = 1.0F - m * 0.95F;
            i = i * o + n * (1.0F - o);
            j = j * o + n * (1.0F - o);
            k = k * o + n * (1.0F - o);
        }

        return new Vec3((double)i, (double)j, (double)k);
    }
}
