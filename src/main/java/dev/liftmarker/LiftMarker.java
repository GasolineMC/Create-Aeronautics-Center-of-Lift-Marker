package dev.liftmarker;

import com.mojang.serialization.Codec;
import dev.liftmarker.network.LiftMarkerDataPacket;
import dev.liftmarker.network.RequestLiftMarkerPacket;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(LiftMarker.MOD_ID)
public class LiftMarker {

    public static final String MOD_ID = "lift_marker";

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> DISPLAY_CENTER_OF_LIFT = ATTACHMENT_TYPES.register("display_center_of_lift",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());

    public LiftMarker(final IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(LiftMarker::registerPackets);
    }

    public static ResourceLocation path(final String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static LangBuilder translate(final String key, final Object... args) {
        return new LangBuilder(MOD_ID).translate(key, args);
    }

    private static void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(RequestLiftMarkerPacket.TYPE, RequestLiftMarkerPacket.CODEC, RequestLiftMarkerPacket::handle);
        registrar.playToClient(LiftMarkerDataPacket.TYPE, LiftMarkerDataPacket.CODEC, LiftMarkerDataPacket::handle);
    }
}
