package dev.liftmarker.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramButton;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.index.SimGUITextures;
import net.createmod.catnip.gui.UIRenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

public class LiftToggleButton extends DiagramButton {

    private final LiftMarkerGUITextures icon;
    private final Supplier<Component> tooltip;

    public LiftToggleButton(final LiftMarkerGUITextures icon, final int x, final int y, final Runnable onClick, final Supplier<Component> tooltip) {
        // the parent needs one of Simulated's textures, but only ours is ever drawn
        super(SimGUITextures.DIAGRAM_ICON_COM_TOGGLE, x, y, Component.empty(), onClick);
        this.icon = icon;
        this.tooltip = tooltip;
    }

    @Override
    protected void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        final LiftMarkerGUITextures tex = this.icon;

        RenderSystem.setShaderTexture(0, tex.location);
        UIRenderHelper.drawColoredTexture(graphics, this.isHovered() ? DiagramScreen.BUTTON_COLOR : DiagramScreen.DULL_BUTTON_COLOR,
                this.getX() - 1, this.getY() - 1, 0, tex.startX, tex.startY, tex.width, tex.height, tex.texWidth, tex.texHeight);

        if (this.isHovered()) {
            DiagramScreen.renderTooltip(graphics, mouseX, mouseY, List.of(this.tooltip.get()));
        }
    }
}
