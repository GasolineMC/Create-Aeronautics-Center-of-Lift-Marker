package dev.liftmarker.client;

import dev.liftmarker.network.LiftMarkerDataPacket;
import org.jetbrains.annotations.Nullable;

public interface LiftMarkerDiagramScreen {

    void liftMarker$updateLiftMarker(LiftMarkerDataPacket data);

    /**
     * The center of lift to draw, or null while it is hidden or has not arrived yet
     */
    @Nullable
    LiftMarkerDataPacket liftMarker$getVisibleLiftMarker();
}
