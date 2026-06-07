package qouteall.imm_ptl.core.mixin.client.render.shader;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameRenderer.class)
public class MixinGameRenderer_Shaders {
    /*@Shadow
    @Final
    private Map<String, CompiledShaderProgram> shaders;
    
    @Inject(
        method = "reloadShaders", at = @At("RETURN")
    )
    private void onLoadShaders(ResourceProvider resourceProvider, CallbackInfo ci) {
        MyRenderHelper.loadShaderSignal.emit(
            resourceProvider, (shader) -> {
                shaders.put(shader.getName(), shader);
            }
        );
    }*/
}
