package dev.liftmarker.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.liftmarker.LiftMarker;
import dev.liftmarker.client.LiftMarkerDiagramScreen;
import dev.liftmarker.client.LiftMarkerGUITextures;
import dev.liftmarker.client.LiftToggleButton;
import dev.liftmarker.diagram.CenterOfLiftCalculator;
import dev.liftmarker.network.LiftMarkerDataPacket;
import dev.liftmarker.network.RequestLiftMarkerPacket;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramButton;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.index.SimGUITextures;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(DiagramScreen.class)
public abstract class DiagramScreenMixin implements LiftMarkerDiagramScreen {

    @Shadow
    @Final
    private static Vector3d LOCAL_CAMERA_POSITION;

    @Shadow
    @Final
    private static Matrix4f PROJECTION_MAT;

    @Shadow
    @Final
    private static int TOOLTIP_LABEL_COLOR;

    @Shadow
    @Final
    private DiagramEntity diagram;

    @Unique
    private boolean liftMarker$displayCenterOfLift = false;

    @Unique
    @Nullable
    private LiftMarkerDataPacket liftMarker$liftMarkerData = null;

    @Unique
    private int liftMarker$ticksWithoutUpdate = 0;

    @Inject(method = "init", at = @At("TAIL"))
    private void liftMarker$addCenterOfLiftButton(final CallbackInfo ci) {
        final Screen screen = (Screen) (Object) this;

        // the side buttons are locals of init, so the new one goes one slot below the mass button
        DiagramButton massButton = null;
        for (final GuiEventListener child : screen.children()) {
            if (child instanceof final DiagramButton button && button.getTexture() == SimGUITextures.DIAGRAM_ICON_MASS) {
                massButton = button;
            }
        }

        if (massButton == null) {
            return;
        }

        final LiftToggleButton centerOfLiftButton = new LiftToggleButton(LiftMarkerGUITextures.DIAGRAM_ICON_COL_TOGGLE, massButton.getX(), massButton.getY() + 20, () -> {
            this.liftMarker$displayCenterOfLift = !this.liftMarker$displayCenterOfLift;
            this.liftMarker$sendRequest(Optional.of(this.liftMarker$displayCenterOfLift));
        }, () -> {
            return LiftMarker.translate("contraption_diagram.center_of_lift").color(TOOLTIP_LABEL_COLOR).add(this.liftMarker$getLiftMarkerStatusText().color(0xffffffff)).component();
        });

        ((ScreenInvoker) screen).liftMarker$addRenderableWidget(centerOfLiftButton);

        this.liftMarker$sendRequest(Optional.empty());
    }

    // keeps greebles clear of the extra button
    @ModifyConstant(method = "addGreebles", constant = @Constant(doubleValue = 66.0))
    private double liftMarker$extendButtonRegion(final double original) {
        return 86.0;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void liftMarker$tick(final CallbackInfo ci) {
        if (this.liftMarker$ticksWithoutUpdate++ > DiagramScreen.UPDATE_REQUEST_INTERVAL) {
            this.liftMarker$ticksWithoutUpdate = 0;

            if (this.liftMarker$displayCenterOfLift) {
                this.liftMarker$sendRequest(Optional.empty());
            }
        }
    }

    @Inject(method = "renderWindow", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 2))
    private void liftMarker$renderCenterOfLift(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks, final CallbackInfo ci) {
        final LiftMarkerDataPacket data = this.liftMarker$getVisibleLiftMarker();

        if (data == null) {
            return;
        }

        final Vector2d screenCoords = DiagramScreen.getScreenCoords(new Vector3d(data.position()), DiagramScreen.LOCAL_ORIENTATION, LOCAL_CAMERA_POSITION, PROJECTION_MAT, DiagramScreen.DIAGRAM_TEXTURE.width, DiagramScreen.DIAGRAM_TEXTURE.height);

        final LiftMarkerGUITextures tex = LiftMarkerGUITextures.DIAGRAM_ICON_COL;

        final PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(screenCoords.x - 8, screenCoords.y - 8, 0);
        graphics.blit(tex.location, 0, 0, 5, tex.startX, tex.startY, tex.width, tex.height, tex.texWidth, tex.texHeight);
        pose.popPose();
    }

    @Unique
    private LangBuilder liftMarker$getLiftMarkerStatusText() {
        if (!this.liftMarker$displayCenterOfLift) {
            return SimLang.translate("contraption_diagram.hidden");
        }

        final LiftMarkerDataPacket data = this.liftMarker$liftMarkerData;
        if (data == null || data.status() == CenterOfLiftCalculator.Status.OK) {
            return SimLang.translate("contraption_diagram.shown");
        }

        return LiftMarker.translate(data.status() == CenterOfLiftCalculator.Status.NO_SURFACES
                ? "contraption_diagram.col_no_surfaces"
                : "contraption_diagram.col_cancelled");
    }

    @Unique
    private void liftMarker$sendRequest(final Optional<Boolean> displayCenterOfLift) {
        PacketDistributor.sendToServer(new RequestLiftMarkerPacket(this.diagram.getId(), displayCenterOfLift));
    }

    @Override
    public void liftMarker$updateLiftMarker(final LiftMarkerDataPacket data) {
        if (this.diagram.getId() == data.entityID()) {
            this.liftMarker$displayCenterOfLift = data.displayCenterOfLift();
            this.liftMarker$liftMarkerData = data;
        }
    }

    @Override
    @Nullable
    public LiftMarkerDataPacket liftMarker$getVisibleLiftMarker() {
        final LiftMarkerDataPacket data = this.liftMarker$liftMarkerData;

        if (!this.liftMarker$displayCenterOfLift || data == null || data.status() != CenterOfLiftCalculator.Status.OK) {
            return null;
        }

        return data;
    }
}
