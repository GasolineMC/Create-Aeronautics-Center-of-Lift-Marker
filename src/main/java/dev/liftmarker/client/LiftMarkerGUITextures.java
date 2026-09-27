package dev.liftmarker.client;

import dev.liftmarker.LiftMarker;
import net.minecraft.resources.ResourceLocation;

public enum LiftMarkerGUITextures {
    DIAGRAM_ICON_COL_TOGGLE("center_of_lift", 0, 0, 16, 16, 64, 16),
    DIAGRAM_ICON_COL("center_of_lift", 16, 0, 16, 16, 64, 16),
    DIAGRAM_ICON_COL_TINY("center_of_lift", 32, 0, 16, 16, 64, 16);

    public final ResourceLocation location;
    public final int startX;
    public final int startY;
    public final int width;
    public final int height;
    public final int texWidth;
    public final int texHeight;

    LiftMarkerGUITextures(final String location, final int startX, final int startY, final int width, final int height, final int texWidth, final int texHeight) {
        this.location = LiftMarker.path("textures/gui/" + location + ".png");
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
    }
}
