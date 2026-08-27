package com.am4er.buildutilities.tools;

import com.am4er.buildutilities.Msg;
import com.am4er.buildutilities.Perms;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class ToolsMenuListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ToolsMenu menu)
                || !(event.getWhoClicked() instanceof Player p)) {
            return;
        }

        Inventory clicked = event.getClickedInventory();
        if (clicked == null || !clicked.equals(event.getInventory())) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
            }
            return;
        }

        event.setCancelled(true);

        if (!p.hasPermission(Perms.TOOLS)) {
            p.closeInventory();
            Msg.bad(p, "You no longer have permission to use the builder tools.");
            return;
        }

        int slot = event.getRawSlot();
        if (menu.isPrevButton(slot) || menu.isNextButton(slot)) {
            if (menu.turnPage(menu.isNextButton(slot) ? 1 : -1)) {
                p.playSound(p, Sound.UI_BUTTON_CLICK, 0.4f, 1.2f);
            }
            return;
        }

        BuilderTool tool = menu.toolAt(slot);
        if (tool == null) {
            return;
        }

        Map<Integer, ItemStack> spare = p.getInventory().addItem(tool.give(event.isShiftClick()));
        if (!spare.isEmpty()) {
            spare.values().forEach(over -> p.getWorld().dropItemNaturally(p.getLocation(), over));
            Msg.warn(p, "Inventory full, the rest is at your feet.");
        }
        p.playSound(p, Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.6f);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof ToolsMenu)) {
            return;
        }
        for (int slot : event.getRawSlots()) {
            if (slot < ToolsMenu.SIZE) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
