package dev.truchosafio4.network;
import dev.truchosafio4.Truchosafio4;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
/** "move" packet is the only repetitive packet; all actions validated server-side. */
public record QumaraControlPayload(int action, int yaw, int forward, int strafe) implements CustomPayload {
    public static final Id<QumaraControlPayload> ID = new Id<>(Truchosafio4.id("qumara_input"));
    public static final PacketCodec<RegistryByteBuf, QumaraControlPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER, QumaraControlPayload::action,
        PacketCodecs.INTEGER, QumaraControlPayload::yaw,
        PacketCodecs.INTEGER, QumaraControlPayload::forward,
        PacketCodecs.INTEGER, QumaraControlPayload::strafe, QumaraControlPayload::new);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
