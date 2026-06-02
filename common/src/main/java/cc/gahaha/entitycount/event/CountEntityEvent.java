package cc.gahaha.entitycount.event;

import cc.gahaha.entitycount.config.ConfigManager;
import cc.gahaha.entitycount.utils.DisplayEntryEntityType;
import cc.gahaha.entitycount.utils.ExtendString;
import cc.gahaha.entitycount.utils.ExtendString2IntEntry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;

import java.util.List;

public class CountEntityEvent{

    public static final List<Object2IntMap.Entry<ExtendString>> defaultList = List.of(
            ExtendString2IntEntry.of("Gahaha", DisplayEntryEntityType.NORMAL, 1),
            ExtendString2IntEntry.of("Is", DisplayEntryEntityType.NORMAL, 1),
            ExtendString2IntEntry.of("So", DisplayEntryEntityType.ITEM, 1),
            ExtendString2IntEntry.of("Handsome", DisplayEntryEntityType.ITEM, 1)
    );

    public static final Object2IntOpenHashMap<ExtendString> entityCountMap = new Object2IntOpenHashMap<>(30);
    public static final Object2IntOpenHashMap<ExtendString> entityChunkCountMap = new Object2IntOpenHashMap<>(30);

    public static void updateEntityCount(Object ...__) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        Iterable<Entity> entities = client.world.getEntities();
        ChunkPos playerChunkPos = client.player.getChunkPos();
        entityCountMap.clear();
        entityChunkCountMap.clear();

        String entityType = ConfigManager.getEntityType();

        for (Entity entity : entities) {
            // 根據實體類型過濾實體
            if ("Living".equals(entityType) && !(entity.isLiving())) {
                continue; // 跳過非生物實體
            }
            addEntityCount(entityCountMap, entity);

            if (entity.getChunkPos().equals(playerChunkPos)) {
                addEntityCount(entityChunkCountMap, entity);
            }
        }
    }

    private static void addEntityCount(Object2IntOpenHashMap<ExtendString> targetMap, Entity entity) {
        String classTranslationKey = entity.getType().getTranslationKey();
        String className = Text.translatable(classTranslationKey).getString();

        boolean isItemEntity = entity instanceof ItemEntity;
        if (isItemEntity && ConfigManager.isExpandItemDisplay()) {
            String itemName = Text.translatable(((ItemEntity) entity).getStack().getItem().getTranslationKey()).getString();
            targetMap.addTo(new ExtendString(itemName, DisplayEntryEntityType.ITEM), ((ItemEntity) entity).getStack().getCount());
            return;
        }

        targetMap.addTo(new ExtendString(className, DisplayEntryEntityType.NORMAL), 1);
        if (entity instanceof LivingEntity livingEntity) {
            String ageSuffix = livingEntity.isBaby() ? " [baby]" : " [adult]";
            targetMap.addTo(new ExtendString(className + ageSuffix, DisplayEntryEntityType.NORMAL), 1);
        }
    }
}
