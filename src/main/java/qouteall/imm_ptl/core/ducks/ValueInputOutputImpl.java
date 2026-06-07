package qouteall.imm_ptl.core.ducks;

import net.minecraft.nbt.CompoundTag;

public interface ValueInputOutputImpl {
    public default CompoundTag getCompound() {
        return new CompoundTag();
    }
}
