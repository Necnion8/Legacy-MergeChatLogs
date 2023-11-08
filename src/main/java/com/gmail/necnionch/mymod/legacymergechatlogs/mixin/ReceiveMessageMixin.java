package com.gmail.necnionch.mymod.legacymergechatlogs.mixin;

import com.gmail.necnionch.mymod.legacymergechatlogs.util.StackableChatHudLine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatHud.class)
public class ReceiveMessageMixin {
    @Shadow
    @Final
    private List<ChatHudLine> visibleMessages;

    @Shadow
    @Final
    private MinecraftClient client;

    @Nullable
    private Text lastText;

    @Inject(
            method = "addMessage(Lnet/minecraft/text/Text;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void addMessage(Text message, int messageId, CallbackInfo ci) {
        try {
            processNewMessage(message, ci);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    public void render(int ticks, CallbackInfo ci) {
        visibleMessages.stream()
                .filter(line -> line instanceof StackableChatHudLine)
                .forEachOrdered(line -> ((StackableChatHudLine) line).update(ticks));
    }

    private void processNewMessage(Text message, CallbackInfo ci) {
        if (visibleMessages.isEmpty() || lastText == null || !lastText.asUnformattedString().equals(message.asUnformattedString()) || message.asUnformattedString().isEmpty()) {
            lastText = message;
            return;
        }

        ci.cancel();
        lastText = message;

        ChatHudLine lastLine = visibleMessages.get(0);
        StackableChatHudLine stackedLastLine;

        if (lastLine instanceof StackableChatHudLine) {
            stackedLastLine = (StackableChatHudLine) lastLine;
            stackedLastLine.increment(client.inGameHud.getTicks());
        } else {
            stackedLastLine = new StackableChatHudLine(client.inGameHud.getTicks(), message, lastLine.getId(), 2);
            visibleMessages.set(0, stackedLastLine);
        }
    }

}
