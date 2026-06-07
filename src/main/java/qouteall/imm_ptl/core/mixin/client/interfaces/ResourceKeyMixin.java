package qouteall.imm_ptl.core.mixin.client.interfaces;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import qouteall.imm_ptl.core.ducks.ResourceKeyImpl;

@Mixin(ResourceKey.class)
public abstract class ResourceKeyMixin implements ResourceKeyImpl {

}
