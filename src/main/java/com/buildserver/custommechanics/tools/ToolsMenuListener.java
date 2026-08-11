package com.buildserver.custommechanics.tools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

    public static final String USE_PERMISSION = "custommechanics.tools";

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ToolsMenu menu)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
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

        if (!player.hasPermission(USE_PERMISSION)) {
            player.closeInventory();
            player.sendMessage(Component.text("You no longer have permission to use the builder tools.",
                    NamedTextColor.RED));
            return;
        }

        int slot = event.getRawSlot();

        if (menu.isPreviousButton(slot) || menu.isNextButton(slot)) {
            if (menu.turnPage(menu.isNextButton(slot) ? 1 : -1)) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.4f, 1.2f);
            }
            return;
        }

        BuilderTool tool = menu.toolAt(slot);
        if (tool == null) {
            return;
        }

        ItemStack given = tool.give(event.isShiftClick());
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(given);
        for (ItemStack overflow : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), overflow);
        }
        if (!leftover.isEmpty()) {
            player.sendMessage(Component.text("Your inventory was full - the rest was dropped at your feet.",
                    NamedTextColor.YELLOW));
        }
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.6f);
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
