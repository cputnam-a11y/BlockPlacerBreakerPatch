package blockplacerbreakerpatch.renderer;

import blockplacerbreakerpatch.BlockPlacerBreakerPatchClient;
import blockplacerbreakerpatch.duck.BreakerBlockEntityExtension;
import com.khazoda.breakerplacer.registry.RegBlocks;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

import java.util.Objects;

public class BreakerBlockEntityConfigurationRenderer implements WorldRenderEvents.End {
    @Override
    public void onEnd(WorldRenderContext context) {
        if (!BlockPlacerBreakerPatchClient.IS_CONFIGURING || !(MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult bhr) || !MinecraftClient.getInstance().world.getBlockState(bhr.getBlockPos()).isOf(RegBlocks.BREAKER_BLOCK))
            return;
        if (!(MinecraftClient.getInstance().world.getBlockEntity(bhr.getBlockPos()) instanceof BreakerBlockEntityExtension bbee))
            return;
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        MatrixStack matrixStack = Objects.requireNonNull(context.matrixStack());
        try (var frame1 = matrixStack.frame()) {
            // apply camera rotation as END doesn't do it for us
            matrixStack.multiply(camera.getRotation().conjugate(new Quaternionf()));
            // translate to camera position so we don't have to do it for every vertex
            matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
            Vec3d blockCenter = bhr.getBlockPos().toCenterPos();
            matrixStack.translate(blockCenter.x, blockCenter.y, blockCenter.z);
            // 0.5, 0.51, 0.5
            Vec3d pos1 = Vec3d.ZERO.add(0.5, 0.51, 0.5);
            // -1.5, 0.51, 0.5
            Vec3d pos2 = Vec3d.ZERO.add(0.5, 0.51, 0.5).subtract(1, 0, 0);
            // -1.5, 0.51, -1.5
            Vec3d pos3 = Vec3d.ZERO.add(0.5, 0.51, 0.5).subtract(1, 0, 1);
            // 0.5, 0.51, -1.5
            Vec3d pos4 = Vec3d.ZERO.add(0.5, 0.51, 0.5).subtract(0, 0, 1);
            // generally set up the render system the way I want it
            setupRenderSystem();
            // get the global tessellator instance
            var tessellator = Tessellator.getInstance();
            // start a new buffer with the vertex format we want
            // QUADS means 4 vertices per quad
            // POSITION_COLOR means we have a position and a color for each vertex
            var buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            for (Direction dir : Direction.values()) {
                try (var frame2 = matrixStack.frame()) {
                    int[] color = getColor(bbee, dir);
                    matrixStack.multiply(dir.getRotationQuaternion());
                    buffer.vertex(matrixStack.peek(), (float) pos1.getX(), (float) pos1.getY(), (float) pos1.getZ()).color(color[0], color[1], color[2], color[3]);
                    buffer.vertex(matrixStack.peek(), (float) pos2.getX(), (float) pos2.getY(), (float) pos2.getZ()).color(color[0], color[1], color[2], color[3]);
                    buffer.vertex(matrixStack.peek(), (float) pos3.getX(), (float) pos3.getY(), (float) pos3.getZ()).color(color[0], color[1], color[2], color[3]);
                    buffer.vertex(matrixStack.peek(), (float) pos4.getX(), (float) pos4.getY(), (float) pos4.getZ()).color(color[0], color[1], color[2], color[3]);
                }
            }

            // draw the buffer with the current program in the render system
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            // cleanup for the next person
            resetRenderSystem();
        }
    }

    private static int[] getColor(BreakerBlockEntityExtension bbee, Direction direction) {
        if (!Objects.requireNonNull(MinecraftClient.getInstance().player).isSneaking()) {
            return switch (bbee.blockPlacerBreakerPatch$getBehavior(direction).getFirst()) {
                case NONE -> new int[]{0, 0, 0, 0};
                case DEFAULT -> new int[]{0x33, 0x33, 0x33, 125};
                case TOOL -> new int[]{0x2c, 0xc7, 0xaa, 125};
                case TOOL_AND_INVENTORY -> new int[]{0x50, 0xc8, 0x78, 125};
                case INVENTORY -> new int[]{0x06, 0x42, 0x73, 125};
            };
        } else {
            return switch (bbee.blockPlacerBreakerPatch$getBehavior(direction).getSecond()) {
                case NONE -> new int[]{0, 0, 0, 0};
                case DEFAULT -> new int[]{0x33, 0x33, 0x33, 125};
                case EXTRACT -> new int[]{0x2c, 0xc7, 0xaa, 125};
                case INSERT -> new int[]{0x50, 0xc8, 0x78, 125};
            };
        }
    }

    private static void setupRenderSystem() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
    }

    private static void resetRenderSystem() {
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }
}
