package qouteall.imm_ptl.core.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import qouteall.imm_ptl.core.ducks.IEAbstractClientPlayer;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends Player implements IEAbstractClientPlayer {


    private Level level;

    public MixinAbstractClientPlayer(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
        this.level = level;
    }

    @Override
    public void ip_setClientLevel(ClientLevel clientWorld) {
        this.level = clientWorld;
    }
}
