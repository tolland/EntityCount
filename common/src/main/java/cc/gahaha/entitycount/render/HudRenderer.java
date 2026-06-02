package cc.gahaha.entitycount.render;

import cc.gahaha.entitycount.event.CountEntityEvent;

import cc.gahaha.entitycount.config.ConfigManager;
import cc.gahaha.entitycount.utils.DisplayEntryEntityType;
import cc.gahaha.entitycount.utils.EntityListFilter;
import cc.gahaha.entitycount.utils.ExtendString;
import cc.gahaha.entitycount.utils.MainSwitch;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.List;

@Environment(EnvType.CLIENT)
public class HudRenderer {
    public static final Identifier ENTITY_COUNT_HUD_ID = Identifier.of("entitycount", "entitycount-hud");
    private static final int LINE_HEIGHT = 11;
    private static final int SECTION_GAP = 4;
    private static final String NONE_PLACEHOLDER = "None";

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!MainSwitch.canComputeAndRender()) return;
        renderEntityCountHUD(
                context,
                EntityListFilter.getProcessedList(CountEntityEvent.entityCountMap),
                EntityListFilter.getProcessedList(CountEntityEvent.entityChunkCountMap),
                ConfigManager.getCoord()
        );
    }

    public static void renderEntityCountHUD(DrawContext context, List<Object2IntOpenHashMap.Entry<ExtendString>> entryList, Vec3d coord) {
        renderSectionedHUD(context, List.of(
                new DisplaySection("Loaded Chunks", entryList)
        ), coord);
    }

    public static void renderEntityCountHUD(
            DrawContext context,
            List<Object2IntOpenHashMap.Entry<ExtendString>> totalEntryList,
            List<Object2IntOpenHashMap.Entry<ExtendString>> chunkEntryList,
            Vec3d coord
    ) {
        renderSectionedHUD(context, List.of(
                new DisplaySection("Current Chunk", chunkEntryList),
                new DisplaySection("Loaded Chunks", totalEntryList)
        ), coord);
    }

    private static void renderSectionedHUD(DrawContext context, List<DisplaySection> sections, Vec3d coord) {
        float scale = (float) (coord.z/10);
        int tmpx = (int)Math.floor(coord.x*MinecraftClient.getInstance().getWindow().getScaledWidth() / scale);
        int tmpy = (int)Math.floor(coord.y*MinecraftClient.getInstance().getWindow().getScaledHeight() / scale);

        int maxTextWidth = 0;
        int totalHeight = 6;
        for (int i = 0; i < sections.size(); i++) {
            DisplaySection section = sections.get(i);
            maxTextWidth = Math.max(maxTextWidth, MinecraftClient.getInstance().textRenderer.getWidth(section.title()));
            List<Object2IntOpenHashMap.Entry<ExtendString>> sectionEntries = section.entries();
            if (sectionEntries.isEmpty()) {
                maxTextWidth = Math.max(maxTextWidth, MinecraftClient.getInstance().textRenderer.getWidth(NONE_PLACEHOLDER));
                totalHeight += LINE_HEIGHT;
            } else {
                for (Object2IntOpenHashMap.Entry<ExtendString> entry : sectionEntries) {
                    String displayText = entry.getKey().value() + ": " + entry.getIntValue();
                    maxTextWidth = Math.max(maxTextWidth, MinecraftClient.getInstance().textRenderer.getWidth(displayText));
                }
                totalHeight += sectionEntries.size() * LINE_HEIGHT;
            }
            totalHeight += LINE_HEIGHT;
            if (i < sections.size() - 1) {
                totalHeight += SECTION_GAP;
            }
        }
        int hudWidth = Math.max(80, maxTextWidth + 8);

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(scale, scale);
        context.fill(tmpx, tmpy, tmpx + hudWidth, tmpy + totalHeight, ConfigManager.getBackgroundColor());

        int drawY = tmpy + 3;
        for (int i = 0; i < sections.size(); i++) {
            DisplaySection section = sections.get(i);
            context.drawText(
                    MinecraftClient.getInstance().textRenderer,
                    Text.literal(section.title()),
                    tmpx + 3,
                    drawY,
                    ConfigManager.getTextColor(),
                    true
            );
            drawY += LINE_HEIGHT;

            List<Object2IntOpenHashMap.Entry<ExtendString>> sectionEntries = section.entries();
            if (sectionEntries.isEmpty()) {
                context.drawText(
                        MinecraftClient.getInstance().textRenderer,
                        Text.literal(NONE_PLACEHOLDER),
                        tmpx + 3,
                        drawY,
                        ConfigManager.getTextColor(),
                        true
                );
                drawY += LINE_HEIGHT;
            } else {
                for (Object2IntOpenHashMap.Entry<ExtendString> entry : sectionEntries) {
                    String displayText = entry.getKey().value() + ": " + entry.getIntValue();
                    int textColor = entry.getKey().displayEntryEntityType() == DisplayEntryEntityType.ITEM
                            ? ConfigManager.getItemEntityColor()
                            : ConfigManager.getTextColor();

                    context.drawText(
                            MinecraftClient.getInstance().textRenderer,
                            Text.literal(displayText),
                            tmpx + 3,
                            drawY,
                            textColor,
                            true
                    );
                    drawY += LINE_HEIGHT;
                }
            }
            if (i < sections.size() - 1) {
                drawY += SECTION_GAP;
            }
        }
        context.getMatrices().popMatrix();
    }

    private record DisplaySection(String title, List<Object2IntOpenHashMap.Entry<ExtendString>> entries) {}
}