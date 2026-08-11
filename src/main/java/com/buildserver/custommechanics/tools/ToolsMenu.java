package com.buildserver.custommechanics.tools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ToolsMenu implements InventoryHolder {

    public static final int SIZE = 54;
    public static final int PAGE_SIZE = 45;

    private static final int SLOT_PREVIOUS = 45;
    private static final int SLOT_INFO = 49;
    private static final int SLOT_NEXT = 53;

    private final List<BuilderTool> tools;
    private final Inventory inventory;
    private int page;

    public ToolsMenu(List<BuilderTool> tools) {
        this.tools = tools;
        this.inventory = Bukkit.createInventory(this, SIZE,
                Component.text("Builder Tools", NamedTextColor.DARK_AQUA));
        render();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public int pageCount() {
        return Math.max(1, (tools.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    public int page() {
        return page;
    }

    public boolean turnPage(int delta) {
        int target = Math.clamp((long) page + delta, 0, pageCount() - 1);
        if (target == page) {
            return false;
        }
        page = target;
        render();
        return true;
    }

    public @Nullable BuilderTool toolAt(int slot) {
        if (slot < 0 || slot >= PAGE_SIZE) {
            return null;
        }
        int index = page * PAGE_SIZE + slot;
        return index < tools.size() ? tools.get(index) : null;
    }

    public boolean isPreviousButton(int slot) {
        return slot == SLOT_PREVIOUS;
    }

    public boolean isNextButton(int slot) {
        return slot == SLOT_NEXT;
    }

    private void render() {
        inventory.clear();

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, tools.size());
        for (int index = start; index < end; index++) {
            inventory.setItem(index - start, tools.get(index).icon());
        }

        ItemStack filler = button(Material.GRAY_STAINED_GLASS_PANE, Component.empty());
        for (int slot = PAGE_SIZE; slot < SIZE; slot++) {
            inventory.setItem(slot, filler);
        }

        if (page > 0) {
            inventory.setItem(SLOT_PREVIOUS, button(Material.ARROW,
                    label("Previous page", NamedTextColor.YELLOW)));
        }
        if (page < pageCount() - 1) {
            inventory.setItem(SLOT_NEXT, button(Material.ARROW,
                    label("Next page", NamedTextColor.YELLOW)));
        }
        inventory.setItem(SLOT_INFO, button(Material.BOOK,
                label("Page " + (page + 1) + " of " + pageCount(), NamedTextColor.AQUA)));
    }

    private static ItemStack button(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> meta.displayName(name));
        return item;
    }

    private static Component label(String text, NamedTextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }
}
