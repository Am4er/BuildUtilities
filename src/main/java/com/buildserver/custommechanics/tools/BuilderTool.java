package com.buildserver.custommechanics.tools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record BuilderTool(String label, List<String> description, ItemStack prototype) {

    public ItemStack icon() {
        ItemStack icon = prototype.clone();
        icon.editMeta(meta -> {
            meta.displayName(plain(label, NamedTextColor.AQUA));

            List<Component> lore = new ArrayList<>();
            for (String line : description) {
                lore.add(plain(line, NamedTextColor.GRAY));
            }
            lore.add(Component.empty());
            lore.add(plain("Click", NamedTextColor.YELLOW)
                    .append(Component.text(" for one", NamedTextColor.GRAY)));
            lore.add(plain("Shift-click", NamedTextColor.YELLOW)
                    .append(Component.text(" for a full stack", NamedTextColor.GRAY)));
            meta.lore(lore);
        });
        return icon;
    }

    public ItemStack give(boolean fullStack) {
        ItemStack item = prototype.clone();
        item.setAmount(fullStack ? Math.max(1, item.getMaxStackSize()) : 1);
        return item;
    }

    private static Component plain(String text, NamedTextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }
}
