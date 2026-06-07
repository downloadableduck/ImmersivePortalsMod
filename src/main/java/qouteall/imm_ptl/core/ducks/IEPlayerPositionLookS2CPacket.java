package qouteall.imm_ptl.core.ducks;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IEPlayerPositionLookS2CPacket {
    default ResourceKey<Level> ip_getPlayerDimension() {
        return Minecraft.getInstance().player.level().dimension();
    };
    
    default void ip_setPlayerDimension(ResourceKey<Level> dimension) {

    };
}
