package qouteall.imm_ptl.core.ducks;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Matrix4f;

public interface GameRendererImpl {
    default void loadProjectionMatrix(Matrix4f matrix4f) {
        GpuBufferSlice buffer = ((GameRenderer) this).fogRenderer.getBuffer(FogRenderer.FogMode.NONE);
        RenderSystem.setProjectionMatrix(buffer, ProjectionType.PERSPECTIVE);
    }
}
