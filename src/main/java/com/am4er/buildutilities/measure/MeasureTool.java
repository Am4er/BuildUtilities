package com.am4er.buildutilities.measure;

import com.am4er.buildutilities.Msg;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MeasureTool {

    private static final Material MATERIAL = Material.STICK;

    private final NamespacedKey key;

    public MeasureTool(Plugin plugin) {
        this.key = new NamespacedKey(plugin, "measuring-tape");
    }

    public ItemStack item() {
        ItemStack item = new ItemStack(MATERIAL);
        item.editMeta(meta -> {
            meta.displayName(Msg.label("Measuring Tape", Msg.BRAND));
            meta.lore(List.of(
                    Msg.label("Left-click a block to pin one corner.", Msg.BODY),
                    Msg.label("Right-click another to measure the span.", Msg.BODY)));
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        });
        return item;
    }

    public boolean is(@Nullable ItemStack item) {
        if (item == null || item.getType() != MATERIAL || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}
