package qouteall.imm_ptl.core.portal;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import qouteall.q_misc_util.my_util.Mesh2D;

public class GeometryPortalShape {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static final int MAX_TRIANGLE_NUM = 10000;
    
    public static @Nullable Mesh2D readOldMeshFromTag(CompoundTag tag) {
        int size = 6;
        if (size % 6 != 0) {
           // LOGGER.error("Invalid Portal Shape Data {}", tag);
            return null;
        }
        
        Mesh2D mesh = new Mesh2D();
        
        int triangleNum = size / 6;
        
        triangleNum = Math.min(triangleNum, MAX_TRIANGLE_NUM);
        
        for (int i = 0; i < triangleNum; i++) {
            mesh.addTriangle(
                i * 6 + 0,
                i * 6 + 1,
                i * 6 + 2,
                i * 6 + 3,
                i * 6 + 4,
                i * 6 + 5
            );
        }
        
        if (mesh.getStoredTriangleNum() == 0) {
            return null;
        }
        return mesh;
    }
    
    public static @Nullable Mesh2D readOldMeshFromTagNonNormalized(
        ListTag tag, double halfWidth, double halfHeight
    ) {
        int size = tag.size();
        if (size % 6 != 0) {
            LOGGER.error("Invalid Portal Shape Data {}", tag);
            return null;
        }
        
        Mesh2D mesh = new Mesh2D();
        
        int triangleNum = size / 6;
        
        triangleNum = Math.min(triangleNum, MAX_TRIANGLE_NUM);
        
        for (int i = 0; i < triangleNum; i++) {
            mesh.addTriangle(
                tag.getDouble(i * 6 + 0).get() / halfWidth,
                tag.getDouble(i * 6 + 1).get() / halfHeight,
                tag.getDouble(i * 6 + 2).get() / halfWidth,
                tag.getDouble(i * 6 + 3).get() / halfHeight,
                tag.getDouble(i * 6 + 4).get() / halfWidth,
                tag.getDouble(i * 6 + 5).get() / halfHeight
            );
        }
        
        if (mesh.getStoredTriangleNum() == 0) {
            return null;
        }
        return mesh;
    }
}
