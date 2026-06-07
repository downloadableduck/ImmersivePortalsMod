package qouteall.imm_ptl.core.mixin.client.render;

import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import qouteall.imm_ptl.core.ClientWorldLoader;

import java.util.HashMap;
import java.util.Map;

@Mixin(CompiledShaderProgram.class)
public class MixinShaderInstanceForIris {
    // if iris is present, avoid reusing other dimensions' program in cache
    /*@Redirect(
        method = "get",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/shaders/Program$Type;getPrograms()Ljava/util/Map;"
        )
    )
    private static Map redirectGetProgramCache(CompiledShader.Type type) {
        if (ClientWorldLoader.getIsInitialized()) {
            return new HashMap();
        }
        return type.getPrograms();
    }*/
}
