package qouteall.imm_ptl.core.ducks;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;

public interface RenderTargetImpl {
     default void bindWrite(boolean bl) {
         RenderSystem.assertOnRenderThread();
         GlStateManager._glBindFramebuffer(36160, -1);
         if (bl) {
             GlStateManager._viewport(0, 0, ((RenderTarget) this).width, ((RenderTarget) this).height);
         }
     }

     default void unbindWrite() {
         GlStateManager._glBindFramebuffer(36160, 0);
     }

     default int frameBufferId() {
         return -1;
     }
}
