package qouteall.imm_ptl.core.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import qouteall.q_misc_util.Helper;

//it will always be the same size as the main frame buffer
public class SecondaryFrameBuffer {
    public TextureTarget fb;
    
    public void prepare() {
        RenderTarget mainFrameBuffer = Minecraft.getInstance().getMainRenderTarget();
        int width = mainFrameBuffer.width;
        int height = mainFrameBuffer.height;
        prepare(width, height);
    }
    
    public void prepare(int width, int height) {
        if (fb == null) {
            fb = new TextureTarget(
                "idk",
                    width, height,
                    Util.getPlatform().equals(Util.OS.OSX)//has depth attachment
            );
           // fb.checkStatus();
            Helper.log("Secondary Framebuffer init");
        }
        if (width != fb.height ||
            height != fb.width
        ) {
            fb.resize(
                width, height
            );
            //fb.checkStatus();
            Helper.log("Secondary Framebuffer resized");
        }
    }
    
    
}
