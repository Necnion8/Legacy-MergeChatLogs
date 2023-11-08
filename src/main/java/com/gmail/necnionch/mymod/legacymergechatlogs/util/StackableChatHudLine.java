package com.gmail.necnionch.mymod.legacymergechatlogs.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class StackableChatHudLine extends ChatHudLine {
    public static final int STACKED_MARKING_TICK = 2;
    private Text overrideText;
    private int overrideCreationTick;
    private int stacks;
    private boolean marking = true;

    public StackableChatHudLine(int creationTick, Text text, int id, int stacks) {
        super(creationTick, text, id);
        this.overrideCreationTick = creationTick;
        this.stacks = stacks;
        this.overrideText = formatStackedMessage();
    }

    @Override
    public Text getText() {
        return this.overrideText;
    }

    public void setText(Text text) {
        this.overrideText = text;
    }

    public int getStacks() {
        return stacks;
    }

    public void setStacks(int stacks) {
        this.stacks = stacks;
    }

    public int getCreationTick() {
        return overrideCreationTick;
    }

    public void setCreationTick(int ticks) {
        this.overrideCreationTick = ticks;
    }

    public Text getOriginalText() {
        return super.getText();
    }

    public int getOriginalCreationTick() {
        return super.getCreationTick();
    }

    public void increment(int ticks) {
        stacks++;
        marking = true;
        setText(formatStackedMessage());
        setCreationTick(ticks);
    }

    public Text formatStackedMessage() {
        return getOriginalText().copy()
                .append(withText(" [", Formatting.DARK_GRAY))
                .append(withText("x" + stacks, marking ? Formatting.RED : Formatting.DARK_RED))
                .append(withText("]", Formatting.DARK_GRAY));
    }

    public void update(int ticks) {
        if (STACKED_MARKING_TICK > ticks - overrideCreationTick) {  // is marking time
            if (!marking) {
                marking = true;
                setText(formatStackedMessage());
            }
        } else {
            if (marking) {
                marking = false;
                setText(formatStackedMessage());
            }
        }
    }

    private static Text withText(String text, Formatting formatting) {
        return new LiteralText(text).setStyle(new Style().setFormatting(formatting));
    }

}
