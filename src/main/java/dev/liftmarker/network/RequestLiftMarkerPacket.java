package dev.liftmarker.network;

import com.mojang.logging.LogUtils;
import dev.liftmarker.LiftMarker;
import dev.liftmarker.diagram.CenterOfLiftCalculator;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3d;

import java.util.Optional;

public record RequestLiftMarkerPacket(int entityID, Optional<Boolean> displayCenterOfLift) implements CustomPacketPayload {

    public static final Type<RequestLiftMarkerPacket> TYPE = new Type<>(LiftMarker.path("request_lift_marker"));

    public static final StreamCodec<ByteBuf, RequestLiftMarkerPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RequestLiftMarkerPacket::entityID,
            ByteBufCodecs.optional(ByteBufCodecs.BOOL), RequestLiftMarkerPacket::displayCenterOfLift,
            RequestLiftMarkerPacket::new);

    private static boolean incompatible = false;

    public void handle(final IPayloadContext context) {
        if (incompatible) {
            return;
        }

        // an unknown Simulated or Sable version turns the marker off instead of erroring on every request
        try {
            this.handleRequest((ServerPlayer) context.player());
        } catch (final LinkageError e) {
            incompatible = true;
            LogUtils.getLogger().warn("Lift Marker does not recognise this version of Simulated or Sable, so the Center of Lift marker is turned off", e);
        }
    }

    private void handleRequest(final ServerPlayer player) {
        final Entity entity = player.level().getEntity(this.entityID);

        // same reach Simulated allows when saving a diagram's config
        if (!(entity instanceof final DiagramEntity diagram) || entity.distanceToSqr(player) >= 4096.0) {
            return;
        }

        final SubLevel subLevel = Sable.HELPER.getContaining(diagram);

        if (!(subLevel instanceof final ServerSubLevel serverSubLevel)) {
            return;
        }

        this.displayCenterOfLift.ifPresent(display -> diagram.setData(LiftMarker.DISPLAY_CENTER_OF_LIFT, display));

        final boolean displayCenterOfLift = diagram.getData(LiftMarker.DISPLAY_CENTER_OF_LIFT);
        final CenterOfLiftCalculator.Result result = displayCenterOfLift
                ? CenterOfLiftCalculator.compute(serverSubLevel)
                : new CenterOfLiftCalculator.Result(CenterOfLiftCalculator.Status.NO_SURFACES, new Vector3d());

        PacketDistributor.sendToPlayer(player, new LiftMarkerDataPacket(this.entityID, displayCenterOfLift, result.status(), result.position()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
