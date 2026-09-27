package dev.liftmarker.client;

import dev.liftmarker.network.LiftMarkerDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class LiftMarkerClient {

    public static void handle(final LiftMarkerDataPacket packet) {
        final Minecraft minecraft = Minecraft.getInstance();
        final Screen screen = minecraft.screen;

        if (screen instanceof final LiftMarkerDiagramScreen diagramScreen) {
            diagramScreen.liftMarker$updateLiftMarker(packet);
        }
    }
}
