package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.config.SenderDetection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.StandardChatListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(StandardChatListener.class)
public abstract class StandardChatListenerMixin {
    @Inject(
            at = @At("HEAD"),
            method = "handle(Lnet/minecraft/network/chat/ChatType;Lnet/minecraft/network/chat/Component;Ljava/util/UUID;)V"
    )
    public void onChatMessage(ChatType messageType, Component message, UUID senderUuid, CallbackInfo callbackInfo) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();

        if (ChatHeads.CONFIG.senderDetection() != SenderDetection.HEURISTIC_ONLY) {
            // find sender via UUID
            ChatHeads.lastSender = connection.getPlayerInfo(senderUuid);

            if (ChatHeads.lastSender != null) {
                ChatHeads.serverSentUuid = true;
                return;
            }

            // no UUID, message is either not from a player or the server didn't wanna tell

            if (ChatHeads.CONFIG.senderDetection() == SenderDetection.UUID_ONLY || ChatHeads.serverSentUuid && ChatHeads.CONFIG.smartHeuristics()) {
                return;
            }
        }

        // use heuristic to find sender
        ChatHeads.lastSender = ChatHeads.detectPlayer(connection, message);
    }
}
