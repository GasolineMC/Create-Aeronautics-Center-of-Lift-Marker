package dev.liftmarker.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.liftmarker.client.LiftMarkerDiagramScreen;
import dev.liftmarker.client.LiftMarkerGUITextures;
import dev.liftmarker.network.LiftMarkerDataPacket;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DiagramStickyNote.class)
public abstract class DiagramStickyNoteMixin {

    @Shadow
    @Final
    private static int SUBLEVEL_RENDER_WIDTH_PIXELS;

    @Shadow
    @Final
    private static int SUBLEVEL_RENDER_HEIGHT_PIXELS;

    @Shadow
    @Final
    private static Vector3d NOTE_LOCAL_CAM_POS;

    @Shadow
    @Final
    private static Matrix4f NOTE_PROJ_MAT;

    @Shadow
    @Final
    private static Quaternionf NOTE_ORIENTATION;

    @Shadow
    private DiagramScreen parent;

    @Inject(method = "renderCustomCOM", at = @At("TAIL"))
    private void liftMarker$renderCustomCOL(final GuiGraphics guiGraphics, final PoseStack stack, final CallbackInfo ci) {
        final LiftMarkerDataPacket data = ((LiftMarkerDiagramScreen) this.parent).liftMarker$getVisibleLiftMarker();

        if (data == null) {
            return;
        }

        stack.pushPose();
        final Vector2d screenCoords = DiagramScreen.getScreenCoords(new Vector3d(data.position()), NOTE_ORIENTATION, NOTE_LOCAL_CAM_POS, NOTE_PROJ_MAT, SUBLEVEL_RENDER_WIDTH_PIXELS, SUBLEVEL_RENDER_HEIGHT_PIXELS);

        final double offsetX = screenCoords.x - 8;
        final double offsetY = screenCoords.y - 8;

        // there is no pointer variant of this icon, so out of scope it is left undrawn
        if (offsetY > 0 && offsetX > 0 && offsetY < SUBLEVEL_RENDER_HEIGHT_PIXELS && offsetX < SUBLEVEL_RENDER_WIDTH_PIXELS) {
            final LiftMarkerGUITextures tex = LiftMarkerGUITextures.DIAGRAM_ICON_COL_TINY;
            stack.translate(offsetX, offsetY, 0);
            guiGraphics.blit(tex.location, 0, 0, 5, tex.startX, tex.startY, tex.width, tex.height, tex.texWidth, tex.texHeight);
        }

        stack.popPose();
    }
}
