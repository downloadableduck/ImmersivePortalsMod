package qouteall.imm_ptl.core.mixin.client.render.framebuffer;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import qouteall.imm_ptl.core.IPCGlobal;
import qouteall.imm_ptl.core.ducks.IEFrameBuffer;

import java.util.function.Supplier;

@Mixin(MainTarget.class)
public abstract class MixinMainTarget extends RenderTarget {
    
    public MixinMainTarget(String useDepth, boolean bl) {
        super(useDepth, bl);
        throw new RuntimeException();
    }
    
    @Redirect(
        method = "allocateDepthAttachment",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/GpuDevice;createTexture(Ljava/util/function/Supplier;ILcom/mojang/blaze3d/textures/TextureFormat;IIII)Lcom/mojang/blaze3d/textures/GpuTexture;",
                remap = false
        )
    )
    private GpuTexture redirectAllocateDepth(GpuDevice device, java.util.function.Supplier<String> label, int usage, TextureFormat format, int width, int height, int depth, int mips) {
        boolean isStencilBufferEnabled = ((IEFrameBuffer) this).ip_getIsStencilBufferEnabled();

        if (isStencilBufferEnabled) {

            return device.createTexture(label, usage, format, width, height, depth, mips);
        }

        return device.createTexture(label, usage, format, width, height, depth, mips);
    }
    
//    @Redirect(
//        method = "Lcom/mojang/blaze3d/pipeline/MainTarget;allocateDepthAttachment(Lcom/mojang/blaze3d/pipeline/MainTarget$Dimension;)Z",
//        at = @At(
//            value = "INVOKE",
//            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
//            remap = false
//        )
//    )
//    private void onTexImage2D(
//        int target, int level, int internalFormat,
//        int width, int height, int border, int format, int type, IntBuffer pixels
//    ) {
//        boolean isStencilBufferEnabled = ((IEFrameBuffer) this).getIsStencilBufferEnabled();
//
//        if (internalFormat == GL_DEPTH_COMPONENT && isStencilBufferEnabled) {
//            GlStateManager._texImage2D(
//                target,
//                level,
//                IPCGlobal.useAnotherStencilFormat ? GL_DEPTH32F_STENCIL8 : GL_DEPTH24_STENCIL8,//
//                width,
//                height,
//                border,
//                ARBFramebufferObject.GL_DEPTH_STENCIL,
//                IPCGlobal.useAnotherStencilFormat ? GL_FLOAT_32_UNSIGNED_INT_24_8_REV : GL30.GL_UNSIGNED_INT_24_8,//
//                pixels
//            );
//        }
//        else {
//            GlStateManager._texImage2D(
//                target, level, internalFormat, width, height,
//                border, format, type, pixels
//            );
//        }
//    }
    
    /*@ModifyArgs(
        method = "createFrameBuffer",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V",
            remap = false
        )
    )
    private void modifyFrameBufferTexture2d(Args args) {
        boolean isStencilBufferEnabled = ((IEFrameBuffer) this).ip_getIsStencilBufferEnabled();
        
        if (isStencilBufferEnabled) {
            if ((int) args.get(1) == GL30.GL_DEPTH_ATTACHMENT) {
                args.set(1, GL30.GL_DEPTH_STENCIL_ATTACHMENT);
            }
        }
    }*/
    
//    @Redirect(
//        method = "Lcom/mojang/blaze3d/pipeline/MainTarget;createFrameBuffer(II)V",
//        at = @At(
//            value = "INVOKE",
//            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V",
//            remap = false
//        )
//    )
//    private void redirectFrameBufferTexture2d(
//        int target, int attachment, int textureTarget, int texture, int level
//    ) {
//        boolean isStencilBufferEnabled = ((IEFrameBuffer) this).getIsStencilBufferEnabled();
//
//        if (attachment == GL30C.GL_DEPTH_ATTACHMENT && isStencilBufferEnabled) {
//            GlStateManager._glFramebufferTexture2D(
//                target, GL30.GL_DEPTH_STENCIL_ATTACHMENT, textureTarget, texture, level
//            );
//        }
//        else {
//            GlStateManager._glFramebufferTexture2D(target, attachment, textureTarget, texture, level);
//        }
//    }
    
}
