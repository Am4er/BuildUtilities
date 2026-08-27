package com.am4er.buildutilities.tools;

import com.am4er.buildutilities.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class BuilderTool {
    private final String label;
    private final ItemStack stock;
    private final ItemStack icon;

    BuilderTool(String label, List<String> lore, ItemStack stock) {
        this.label = label;
        this.stock = stock;
        this.icon = dress(stock, label, lore);
    }

    public ItemStack icon() { return icon; }

    public ItemStack give(boolean fullStack) {
        ItemStack out = stock.clone();
        if (fullStack) {
            out.setAmount(Math.max(1, out.getMaxStackSize()));
        }
        return out;
    }

    private static ItemStack dress(ItemStack stock, String label, List<String> lore) {
        ItemStack icon = stock.clone();
        icon.editMeta(meta -> {
            meta.displayName(Msg.label(label, Msg.BRAND));

            List<Component> lines = new ArrayList<>(lore.size() + 3);
            for (String line : lore) {
                lines.add(Msg.label(line, Msg.BODY));
            }
            lines.add(Component.empty());
            lines.add(Msg.label("Click", Msg.WARN).append(Component.text(" for one", Msg.FAINT)));
            lines.add(Msg.label("Shift-click", Msg.WARN).append(Component.text(" for a stack", Msg.FAINT)));
            meta.lore(lines);
        });
        return icon;
    }
}
