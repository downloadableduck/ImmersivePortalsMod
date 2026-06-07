package qouteall.imm_ptl.core.mixin.common.chunk_sync;

import net.minecraft.server.level.ChunkTaskPriorityQueue;
import net.minecraft.util.thread.StrictQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkTaskPriorityQueue.class)
public interface IEChunkTaskPriorityQueueSorter {
    //@Accessor("mailbox")
    //ProcessorMailbox<StrictQueue.FixedPriorityQueue> ip_getMailBox();
}
