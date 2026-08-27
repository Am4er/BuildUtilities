package com.am4er.buildutilities.tools;

import com.am4er.buildutilities.Msg;
import net.kyori.adventure.text.Component;
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

    private static final int SLOT_PREV = 45;
    private static final int SLOT_INFO = 49;
    private static final int SLOT_NEXT = 53;

    private static final ItemStack FILLER =
            button(Material.GRAY_STAINED_GLASS_PANE, Component.empty());
    private static final ItemStack PREV =
            button(Material.ARROW, Msg.label("Previous page", Msg.WARN));
    private static final ItemStack NEXT =
            button(Material.ARROW, Msg.label("Next page", Msg.WARN));

    private final List<BuilderTool> tools;
    private final Inventory view;
    private final int pages;
    private int page;

    public ToolsMenu(List<BuilderTool> tools) {
        this.tools = tools;
        this.pages = Math.max(1, (tools.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        this.view = Bukkit.createInventory(this, SIZE, Msg.label("Builder Tools", Msg.BRAND));
        render();
    }

    public void open(Player p) { p.openInventory(view); }

    @Override public @NotNull Inventory getInventory() { return view; }

    public boolean turnPage(int delta) {
        int wanted = Math.clamp((long) page + delta, 0, pages - 1);
        if (wanted == page) {
            return false;
        }
        page = wanted;
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

    public boolean isPrevButton(int slot) { return slot == SLOT_PREV; }

    public boolean isNextButton(int slot) { return slot == SLOT_NEXT; }

    private void render() {
        view.clear();

        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, tools.size());
        for (int index = from; index < to; index++) {
            view.setItem(index - from, tools.get(index).icon());
        }

        for (int slot = PAGE_SIZE; slot < SIZE; slot++) {
            view.setItem(slot, FILLER);
        }

        if (page > 0) {
            view.setItem(SLOT_PREV, PREV);
        }
        if (page < pages - 1) {
            view.setItem(SLOT_NEXT, NEXT);
        }
        view.setItem(SLOT_INFO, button(Material.BOOK,
                Msg.label("Page " + (page + 1) + " of " + pages, Msg.BRAND)));
    }

    private static ItemStack button(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> meta.displayName(name));
        return item;
    }
}
