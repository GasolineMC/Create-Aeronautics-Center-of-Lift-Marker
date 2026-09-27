package dev.liftmarker.network;

import dev.liftmarker.LiftMarker;
import dev.liftmarker.client.LiftMarkerClient;
import dev.liftmarker.diagram.CenterOfLiftCalculator;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3d;

public record LiftMarkerDataPacket(int entityID, boolean displayCenterOfLift, CenterOfLiftCalculator.Status status, Vector3d position) implements CustomPacketPayload {
    public static final Type<LiftMarkerDataPacket> TYPE = new Type<>(LiftMarker.path("lift_marker_data"));

    private static final StreamCodec<ByteBuf, CenterOfLiftCalculator.Status> STATUS_CODEC =
            ByteBufCodecs.VAR_INT.map(i -> CenterOfLiftCalculator.Status.values()[i], CenterOfLiftCalculator.Status::ordinal);

    private static final StreamCodec<ByteBuf, Vector3d> VECTOR3D_CODEC = StreamCodec.of(
            (buf, vector) -> {
                buf.writeDouble(vector.x);
                buf.writeDouble(vector.y);
                buf.writeDouble(vector.z);
            },
            buf -> new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));

    public static final StreamCodec<ByteBuf, LiftMarkerDataPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, LiftMarkerDataPacket::entityID,
            ByteBufCodecs.BOOL, LiftMarkerDataPacket::displayCenterOfLift,
            STATUS_CODEC, LiftMarkerDataPacket::status,
            VECTOR3D_CODEC, LiftMarkerDataPacket::position,
            LiftMarkerDataPacket::new);

    public void handle(final IPayloadContext context) {
        // kept in its own class so dedicated servers never load client code
        LiftMarkerClient.handle(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
