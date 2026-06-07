package qouteall.imm_ptl.core.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlShaderModule;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.Uniform;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.irisshaders.iris.mixin.MixinCompiledShaderProgram;
import net.irisshaders.iris.mixinterface.ShaderInstanceInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.miscellaneous.IPVanillaCopy;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import qouteall.imm_ptl.core.render.context_management.RenderStates;
import qouteall.imm_ptl.core.render.context_management.WorldRenderInfo;
import qouteall.q_misc_util.my_util.SignalBiArged;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glReadPixels;

public class MyRenderHelper {
    
    public static final Minecraft client = Minecraft.getInstance();
    
    public static final SignalBiArged<ResourceProvider, Consumer<GlProgram>> loadShaderSignal =
        new SignalBiArged<>();

    static Tesselator tesselator = new Tesselator(1536);
    
    public static void init() {
        
        loadShaderSignal.connect((resourceManager, resultConsumer) -> {
            try {
                DrawFbInAreaShader shader = new DrawFbInAreaShader(
                    getResourceFactory(resourceManager),
                    "portal_draw_fb_in_area",
                    DefaultVertexFormat.POSITION_COLOR
                );
                resultConsumer.accept(shader);
                drawFbInAreaShader = shader;
                Minecraft.getInstance().getShaderManager();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        
        loadShaderSignal.connect((resourceManager, resultConsumer) -> {
            try {
                GlProgram shader = GlProgram.link(
                                        new GlShaderModule(GlStateManager.glCreateProgram(), Identifier.withDefaultNamespace("portal_area"), ShaderType.byLocation(Identifier.withDefaultNamespace("portal_area"))),
                                        new GlShaderModule(GlStateManager.glCreateProgram(), Identifier.withDefaultNamespace("portal_area"), ShaderType.byLocation(Identifier.withDefaultNamespace("portal_area"))),
                        DefaultVertexFormat.POSITION_COLOR,
                        "portal_area"
                                );
                resultConsumer.accept(shader);
                portalAreaShader = shader;
            } catch (ShaderManager.CompilationException e) {
                throw new RuntimeException(e);
            }
        });
        
        loadShaderSignal.connect((resourceManager, resultConsumer) -> {
            try {
                GlProgram shader = GlProgram.link(
                                        new GlShaderModule(GlStateManager.glCreateProgram(), Identifier.withDefaultNamespace("blit_screen_noblend"), ShaderType.byLocation(Identifier.withDefaultNamespace("blit_screen_noblend"))),
                                        new GlShaderModule(GlStateManager.glCreateProgram(), Identifier.withDefaultNamespace("blit_screen_noblend"), ShaderType.byLocation(Identifier.withDefaultNamespace("blit_screen_noblend"))),
                        DefaultVertexFormat.POSITION_TEX_COLOR,
                        "blit_screen_noblend"
                                );
                resultConsumer.accept(shader);
                blitScreenNoBlendShader = shader;
            } catch (ShaderManager.CompilationException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    // vanilla hardcodes the shader namespace to be "minecraft"
    private static ResourceProvider getResourceFactory(ResourceProvider resourceManager) {
        ResourceProvider resourceFactory = new ResourceProvider() {
            @Override
            public Optional<Resource> getResource(Identifier resourceLocation) {
                Identifier corrected = McHelper.newIdentifier(
                    "immersive_portals", resourceLocation.getPath());
                return resourceManager.getResource(corrected);
            }
        };
        return resourceFactory;
    }
    
    public static class DrawFbInAreaShader extends GlProgram {
        
        public Uniform uniformW;
        public Uniform uniformH;
        
        public DrawFbInAreaShader(
            ResourceProvider factory, String name, VertexFormat format
        ) throws IOException {
            super(GlStateManager.glCreateProgram(), name);

            uniformW = getUniform("w");
            uniformH = getUniform("h");
        }
        
        void loadWidthHeight(int w, int h) {
            uniformW  = new Uniform.Sampler(w, h);
            uniformH = new Uniform.Sampler(w, h);
        }

        @Override
        public void close() {

        }
    }
    
    public static DrawFbInAreaShader drawFbInAreaShader;
    public static GlProgram portalAreaShader;
    public static GlProgram blitScreenNoBlendShader;
    
    public static void drawPortalAreaWithFramebuffer(
        Portal portal,
        RenderTarget textureProvider,
        Matrix4f modelViewMatrix,
        Matrix4f projectionMatrix
    ) {
        
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._enableDepthTest();
        GlStateManager._depthMask(true);
        GlStateManager._viewport(0, 0, textureProvider.width, textureProvider.height);
        
        DrawFbInAreaShader shader = drawFbInAreaShader;
        // commented out code
        /*shader.setSampler("DiffuseSampler", textureProvider.getColorTexture());
        shader.loadWidthHeight(textureProvider.width, textureProvider.height);
        
        if (shader.MODEL_VIEW_MATRIX != null) {
            shader.MODEL_VIEW_MATRIX.set(modelViewMatrix);
        }
        
        if (shader.PROJECTION_MATRIX != null) {
            shader.PROJECTION_MATRIX.set(projectionMatrix);
        }
        
        shader.apply();*/
        
        ViewAreaRenderer.buildPortalViewAreaTrianglesBuffer(
            Vec3.ZERO,//fog
            portal,
            CHelper.getCurrentCameraPos(),
            RenderStates.getPartialTick()
        );
        
        
        shader.close();
    }
    
    public static void renderScreenTriangle() {
        renderScreenTriangle(255, 255, 255, 255);
    }
    
    public static void renderScreenTriangle(Vec3 color) {
        renderScreenTriangle(
            (int) (color.x * 255),
            (int) (color.y * 255),
            (int) (color.z * 255),
            255
        );
    }
    
    public static void testOneTriangle(int r, int g, int b, int a) {
        /*GlProgram shader = GameRenderer.getPositionColorShader();
        Validate.notNull(shader);
        
        Matrix4f identityMatrix = new Matrix4f();
        identityMatrix.identity();
        
        shader.MODEL_VIEW_MATRIX.set(identityMatrix);
        shader.PROJECTION_MATRIX.set(identityMatrix);
        
        shader.apply();*/
        
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator
            .begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        
        // upper triangle
//        bufferBuilder.addVertex(1, -1, 0).setColor(r, g, b, a)
//            ;
//        bufferBuilder.addVertex(1, 1, 0).setColor(r, g, b, a)
//            ;
//        bufferBuilder.addVertex(-1, 1, 0).setColor(r, g, b, a)
//            ;
        
        // down triangle
        bufferBuilder.addVertex(-1, 1, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex(-1, -1, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex(1, -1, 0).setColor(r, g, b, a);
        
        bufferBuilder.addVertex(1, 0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex(0, 1, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex(-1, 0, 0).setColor(r, g, b, a);

        //MultiBufferSource.BufferSource.draw(bufferBuilder.build());
        
        //shader.clear();
    }
    
    /**
     * {@link RenderTarget#blitToScreen(int, int)}
     */
    @IPVanillaCopy
    public static void renderScreenTriangle(int r, int g, int b, int a) {
        //ShaderInstance shader = GameRenderer.getPositionColorShader();
       // Validate.notNull(shader);
        
        Matrix4f identityMatrix = new Matrix4f();
        identityMatrix.identity();
        
       // shader.MODEL_VIEW_MATRIX.set(identityMatrix);
        //shader.PROJECTION_MATRIX.set(identityMatrix);
        
        //shader.apply();
        
        BufferBuilder bufferBuilder = tesselator.
            begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        
        bufferBuilder.addVertex(1, -1, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex(1, 1, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex(-1, 1, 0).setColor(r, g, b, a);
        
        bufferBuilder.addVertex(-1, 1, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex(-1, -1, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex(1, -1, 0).setColor(r, g, b, a);
        
        //BufferUploader.draw(bufferBuilder.build());
        
        //shader.clear();
    }
    
    /**
     * {@link RenderTarget#blitToScreen(int, int)}
     */
    public static void drawScreenFrameBuffer(
        RenderTarget textureProvider,
        boolean doUseAlphaBlend,
        boolean doEnableModifyAlpha
    ) {
        int x = 0;
        int y = 0;
        
        int viewportWidth = textureProvider.width;
        int viewportHeight = textureProvider.height;
        
        drawFramebufferWithCoordinatesAndDimensions(
            textureProvider, doUseAlphaBlend, doEnableModifyAlpha,
            x, y, viewportWidth, viewportHeight
        );
    }

    public static void drawFramebuffer(
            RenderTarget textureProvider, boolean doUseAlphaBlend, boolean doEnableModifyAlpha,
            float xMin, float xMax, float yMin, float yMax
    ) {
        drawFramebufferWithCoordinatesAndDimensions(
                textureProvider,
                doUseAlphaBlend, doEnableModifyAlpha,
                0, 0,
                client.getWindow().getWidth(),
                client.getWindow().getHeight()
        );
    }

    public static void drawFramebufferWithViewport(
            RenderTarget textureProvider, boolean doUseAlphaBlend, boolean doEnableModifyAlpha,
            float left, float right, float bottom, float up,
            int viewportWidth, int viewportHeight
    ) {
        drawFramebufferWithCoordinatesAndDimensions(
                textureProvider,
                doUseAlphaBlend, doEnableModifyAlpha,
                0, 0,
                viewportWidth, viewportHeight
        );
    }
    
    public static void drawFramebufferWithBounds(
        RenderTarget textureProvider, boolean doUseAlphaBlend, boolean doEnableModifyAlpha,
        int xMin, int xMax, int yMin, int yMax
    ) {

        drawFramebufferWithCoordinatesAndDimensions(
            textureProvider,
            doUseAlphaBlend, doEnableModifyAlpha,
            xMin, yMin,
            Mth.abs(xMax - xMin),
            Mth.abs(yMax - yMin)
        );
    }
    
    /**
     * {@link RenderTarget#blitToScreen(int, int)}
     */
    @IPVanillaCopy
    public static void drawFramebufferWithCoordinatesAndDimensions(
        RenderTarget textureProvider, boolean doUseAlphaBlend, boolean doEnableModifyAlpha,
        int x, int y, int viewportWidth, int viewportHeight
    ) {
        CHelper.checkGlError();

        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);
        GlStateManager._viewport(x, textureProvider.height - viewportHeight - y, viewportWidth, viewportHeight);
        
        if (doUseAlphaBlend) {
            GlStateManager._enableBlend();
            
            // this is used for rendering a FB onto screen when the FB contains translucent things
            // the FB should initialize with zero color and zero alpha
            // MC's default blend func is: color = srcColor * srcAlpha + dstColor * (1-srcAlpha)
            // then the FB's rendered content would be fbColor = contentColor * contentAlpha
            // we want the roughtly same effect of rendering the translucent thing directly onto current FB, so we want:
            // color = contentColor * contentAlpha + dstColor * (1-contentAlpha)
            // color = fbColor * 1 + dstColor * (1-contentAlpha)
            GlStateManager._blendFuncSeparate(
                    SourceFactor.ONE.ordinal(),
                    DestFactor.ONE_MINUS_SRC_ALPHA.ordinal(),
                    SourceFactor.ZERO.ordinal(),
                    DestFactor.ONE.ordinal()
            );
        }
        else {
            GlStateManager._disableBlend();
        }
        
        if (doEnableModifyAlpha) {
            GlStateManager._colorMask(true, true, true, true);
        }
        else {
            GlStateManager._colorMask(true, true, true, false);
        }
        
        GlProgram shader = /*doUseAlphaBlend ?
            client.gameRenderer. :*/ blitScreenNoBlendShader;
        
        Validate.notNull(shader, "shader is null");
        
        //shader.setSampler("DiffuseSampler", textureProvider.getColorTexture());
        //shader.apply();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, VertexFormat.builder().add("Position", VertexFormatElement.POSITION).build());
        bufferBuilder.addVertex(0.0f, 0.0f, 0.0f);
        bufferBuilder.addVertex(1.0f, 0.0f, 0.0f);
        bufferBuilder.addVertex(1.0f, 1.0f, 0.0f);
        bufferBuilder.addVertex(0.0f, 1.0f, 0.0f);
        //BufferUploader.draw(bufferBuilder.buildOrThrow());
        //shader.clear();

        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
        
        GlStateManager._enableBlend();
        GlStateManager._blendFuncSeparate(SourceFactor.SRC_ALPHA.ordinal(), DestFactor.ONE_MINUS_SRC_ALPHA.ordinal(), SourceFactor.ONE.ordinal(), DestFactor.ZERO.ordinal());
        
        CHelper.checkGlError();
    }
    
    // it will remove the light sections that are marked to be removed
    // if not, light data will cause minor memory leak
    // and wrongly remove the light data when the chunks get reloaded to client
    // this should not run before world rendering or the smooth lighting may become abnormal in section edge
    public static void lateUpdateLight() {
        if (!ClientWorldLoader.getIsInitialized()) {
            return;
        }
        
        ClientWorldLoader.getClientWorlds().forEach(world -> {
            if (!RenderStates.isDimensionRendered(world.dimension())) {
                world.getChunkSource().getLightEngine().runLightUpdates();
            }
        });
    }
    
    /**
     * If we don't do this
     * the future created in {@link SectionRenderDispatcher#uploadSectionLayer}
     * may never complete
     */
    public static void earlyRemoteUpload() {
        if (!ClientWorldLoader.getIsInitialized()) {
            return;
        }
        
        ClientWorldLoader.WORLD_RENDERER_MAP.forEach((dim, worldRenderer) -> {
            if (client.level.dimension() != dim) {
                worldRenderer.getSectionRenderDispatcher().uploadAllPendingUploads();
            }
        });
    }
    
    public static void applyMirrorFaceCulling() {
        glCullFace(GL_FRONT);
    }
    
    public static void recoverFaceCulling() {
        glCullFace(GL_BACK);
    }
    
    public static void clearAlphaTo1(RenderTarget mcFrameBuffer) {
        mcFrameBuffer.bindWrite(true);
        GlStateManager._colorMask(false, false, false, true);
        GL11.glClearColor(0, 0, 0, 1.0f);
        GlStateManager._clear(GL_COLOR_BUFFER_BIT);
        GlStateManager._colorMask(true, true, true, true);
    }
    
    public static void restoreViewPort() {
        Minecraft client = Minecraft.getInstance();
        GlStateManager._viewport(
            0,
            0,
            client.getWindow().getWidth(),
            client.getWindow().getHeight()
        );
    }
    
    public static float transformFogDistance(float value) {
        if (!WorldRenderInfo.isFogEnabled()) {
            return value * 23333;
        }
        
        // just disable fog for fuse-view portals for now
        if (PortalRendering.isRendering()) {
            Portal renderingPortal = PortalRendering.getRenderingPortal();
            
            if (renderingPortal.isFuseView()) {
                return value * 23333;
            }
        }
        
        // as non-fuse-view portals does not apply scale transformation to modelview,
        // there is no need to transform fog distance (both with and without sodium)
        
        return value;
    }
    
    private static boolean debugEnabled = false;
    
    public static void debugFramebufferDepth() {
        if (!debugEnabled) {
            return;
        }
        debugEnabled = false;
        
        int width = client.getMainRenderTarget().width;
        int height = client.getMainRenderTarget().height;
        
        
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.LITTLE_ENDIAN);
        
        FloatBuffer floatBuffer = directBuffer.asFloatBuffer();
        
        glReadPixels(
            0, 0, width, height,
            GL_DEPTH_COMPONENT, GL_FLOAT, floatBuffer
        );
        
        float[] data = new float[width * height];
        
        floatBuffer.rewind();
        floatBuffer.get(data);
        
        float maxValue = (float) IntStream.range(0, data.length)
            .mapToDouble(i -> data[i]).max().getAsDouble();
        float minValue = (float) IntStream.range(0, data.length)
            .mapToDouble(i -> data[i]).min().getAsDouble();
        
        byte[] grayData = new byte[width * height];
        for (int i = 0; i < data.length; i++) {
            float datum = data[i];
            
            datum = (datum - minValue) / (maxValue - minValue);
            
            grayData[i] = (byte) (datum * 255);
        }
        
        BufferedImage bufferedImage =
            new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        bufferedImage.setData(
            Raster.createRaster(
                bufferedImage.getSampleModel(),
                new DataBufferByte(grayData, grayData.length), new Point()
            )
        );
        
        System.out.println("oops");
    }
    
    public static void debugFramebufferColorRed() {
        if (!debugEnabled) {
            return;
        }
        debugEnabled = false;
        
        int width = client.getMainRenderTarget().width;
        int height = client.getMainRenderTarget().height;
        
        
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.LITTLE_ENDIAN);
        
        FloatBuffer floatBuffer = directBuffer.asFloatBuffer();
        
        glReadPixels(
            0, 0, width, height,
            GL_RED, GL_FLOAT, floatBuffer
        );
        
        float[] data = new float[width * height];
        
        floatBuffer.rewind();
        floatBuffer.get(data);
        
        float maxValue = (float) IntStream.range(0, data.length)
            .mapToDouble(i -> data[i]).max().getAsDouble();
        float minValue = (float) IntStream.range(0, data.length)
            .mapToDouble(i -> data[i]).min().getAsDouble();
        
        byte[] grayData = new byte[width * height];
        for (int i = 0; i < data.length; i++) {
            float datum = data[i];
            
            datum = (datum - minValue) / (maxValue - minValue);
            
            grayData[i] = (byte) (datum * 255);
        }
        
        BufferedImage bufferedImage =
            new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        bufferedImage.setData(
            Raster.createRaster(
                bufferedImage.getSampleModel(),
                new DataBufferByte(grayData, grayData.length), new Point()
            )
        );
        
        System.out.println("oops");
    }
}
