package qouteall.imm_ptl.core.ducks;

import net.minecraft.server.level.Ticket;
import net.minecraft.util.SortedArraySet;

import java.util.List;

public interface IEDistanceManager {
    
    List<Ticket> portal_getTicketSet(long chunkPos);
    
    
}
