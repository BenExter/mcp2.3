package dev.truchosafio4.network;
import dev.truchosafio4.Truchosafio4;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
public record FlashPayload(int durationTicks) implements CustomPayload {
    public static final Id<FlashPayload> ID = new Id<>(Truchosafio4.id("flash"));
    public static final PacketCodec<RegistryByteBuf, FlashPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, FlashPayload::durationTicks, FlashPayload::new);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
