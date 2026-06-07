package qouteall.imm_ptl.core.mixin.client.interfaces;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import qouteall.imm_ptl.core.ducks.RenderTargetImpl;

@Mixin(RenderTarget.class)
public class RenderTargetMixin implements RenderTargetImpl {

    @Shadow
    public int width;

    @Shadow
    public int height;
}
