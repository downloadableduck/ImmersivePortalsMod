package qouteall.imm_ptl.core.ducks;

import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public interface MinecraftImpl {
    default ProfilerFiller getProfiler() {
        return Profiler.get();
    }
}
