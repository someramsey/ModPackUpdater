package com.ramsey.updater;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.client.gui.ScreenUtils;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Size2i;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DownloadScreen extends Screen {
    private ProgressBar progressBar;
    private Component downloadingMessage;
    private Component preparingMessage;
    private String progress;

    private final int textColor = 16777215;

    public DownloadScreen() {
        super(Component.literal("Download"));
    }

    public void updateProgress(String progressMessage) {
        this.progress = progressMessage;
    }

    @Override
    protected void init() {
        this.progressBar = new ProgressBar(100, 120, this.width - 200, 10);

        this.downloadingMessage = Component.literal("Downloading...");
        this.preparingMessage = Component.literal("Preparing the download...");
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);

        drawCenteredString(pPoseStack, this.font, getMessage(), this.width / 2, 70, textColor);
        progressBar.render(pPoseStack);

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    private Component getMessage() {
        if (progress == null) {
            return this.preparingMessage;
        }

        return Component.empty().append(downloadingMessage).append(" ").append(progress);
    }


    public class InfoPanel extends ScrollPanel {
        private final List<String> lines = new ArrayList<>();

        public InfoPanel(int width, int height, int top, int left) {
            super(minecraft, width, height, top, left);
        }

        public void setContent(String content) {
            lines.clear();

            if (content != null) {
                Collections.addAll(lines, content.split("\n"));
            }
        }

        @Override
        protected int getContentHeight() {
            return font.lineHeight * lines.size();
        }

        @Override
        protected void drawPanel(PoseStack poseStack, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
            int y = top + 4 - getScrollAmount();

            for (String line : lines) {
                if (y > top && y < bottom) {
                    drawCenteredString(poseStack, font, line, width / 2, y, textColor);
                }

                y += font.lineHeight;
            }
        }

        @Override
        public @NotNull NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(@NotNull NarrationElementOutput pNarrationElementOutput) { }
    }
}
