package com.gmail.necnionch.mymod.legacymergechatlogs.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

    @Nullable
    private Text lastText;
    private int lastCount;

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

    private void processNewMessage(Text message, CallbackInfo ci) {
        if (lastText == null || !lastText.asUnformattedString().equals(message.asUnformattedString()) || message.asUnformattedString().isEmpty()) {
            lastText = message;
            lastCount = 1;
            return;
        }

        ci.cancel();
        lastText = message;
        lastCount++;

        Text mergedLine = message.copy()
                .append(withText(" [", Formatting.DARK_GRAY))
                .append(withText("x" + lastCount, Formatting.DARK_RED))
                .append(withText("]", Formatting.DARK_GRAY));

        ChatHudLine lastLine = visibleMessages.get(0);
        visibleMessages.set(0, new ChatHudLine(lastLine.getCreationTick(), mergedLine, lastLine.getId()));
    }

    private static Text withText(String text, Formatting formatting) {
        return new LiteralText(text).setStyle(new Style().setFormatting(formatting));
    }
}
