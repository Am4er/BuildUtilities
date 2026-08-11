package com.buildserver.custommechanics.tools;

import org.bukkit.Material;
import org.bukkit.block.data.Levelled;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class BuilderToolRegistry {

    private BuilderToolRegistry() {
    }

    public static List<BuilderTool> build(Logger logger) {
        List<BuilderTool> tools = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        add(tools, skipped, Material.BARRIER, "Barrier",
                "Invisible solid block.", "Only visible while holding one.");
        add(tools, skipped, Material.STRUCTURE_VOID, "Structure Void",
                "Invisible and walk-through.", "Marks 'keep existing block' in structures.");

        for (int level = 0; level <= 15; level++) {
            BuilderTool light = light(level);
            if (light != null) {
                tools.add(light);
            }
        }

        add(tools, skipped, Material.BEDROCK, "Bedrock",
                "Unbreakable in survival.");
        add(tools, skipped, Material.END_PORTAL_FRAME, "End Portal Frame",
                "Place eyes to activate an end portal.");
        add(tools, skipped, Material.SPAWNER, "Monster Spawner",
                "Empty spawner; set the mob with a spawn egg.");
        add(tools, skipped, Material.TRIAL_SPAWNER, "Trial Spawner",
                "Trial chamber spawner.");
        add(tools, skipped, Material.VAULT, "Vault",
                "Trial chamber reward vault.");
        add(tools, skipped, Material.BUDDING_AMETHYST, "Budding Amethyst",
                "Grows amethyst clusters.", "Never drops when mined.");
        add(tools, skipped, Material.REINFORCED_DEEPSLATE, "Reinforced Deepslate",
                "Ancient city frame block.");
        add(tools, skipped, Material.PETRIFIED_OAK_SLAB, "Petrified Oak Slab",
                "Stone-textured slab kept for legacy worlds.");
        add(tools, skipped, Material.DRAGON_EGG, "Dragon Egg",
                "Teleports when hit; push with a piston to collect.");
        add(tools, skipped, Material.CHORUS_PLANT, "Chorus Plant",
                "Stem block of a chorus tree.");
        add(tools, skipped, Material.SCULK_SHRIEKER, "Sculk Shrieker",
                "Summons a warden when it can.");
        add(tools, skipped, Material.SUSPICIOUS_SAND, "Suspicious Sand",
                "Brushable archaeology block.");
        add(tools, skipped, Material.SUSPICIOUS_GRAVEL, "Suspicious Gravel",
                "Brushable archaeology block.");
        add(tools, skipped, Material.FARMLAND, "Farmland",
                "Tilled soil.", "Placed directly, without a hoe.");
        add(tools, skipped, Material.DIRT_PATH, "Dirt Path",
                "Packed path block.", "Placed directly, without a shovel.");

        add(tools, skipped, Material.INFESTED_STONE, "Infested Stone",
                "Releases a silverfish when broken.");
        add(tools, skipped, Material.INFESTED_COBBLESTONE, "Infested Cobblestone",
                "Releases a silverfish when broken.");
        add(tools, skipped, Material.INFESTED_STONE_BRICKS, "Infested Stone Bricks",
                "Releases a silverfish when broken.");
        add(tools, skipped, Material.INFESTED_MOSSY_STONE_BRICKS, "Infested Mossy Stone Bricks",
                "Releases a silverfish when broken.");
        add(tools, skipped, Material.INFESTED_CRACKED_STONE_BRICKS, "Infested Cracked Stone Bricks",
                "Releases a silverfish when broken.");
        add(tools, skipped, Material.INFESTED_CHISELED_STONE_BRICKS, "Infested Chiseled Stone Bricks",
                "Releases a silverfish when broken.");
        add(tools, skipped, Material.INFESTED_DEEPSLATE, "Infested Deepslate",
                "Releases a silverfish when broken.");

        add(tools, skipped, Material.COMMAND_BLOCK, "Command Block",
                "Impulse command block.");
        add(tools, skipped, Material.CHAIN_COMMAND_BLOCK, "Chain Command Block",
                "Runs after the block pointing into it.");
        add(tools, skipped, Material.REPEATING_COMMAND_BLOCK, "Repeating Command Block",
                "Runs every tick while powered.");
        add(tools, skipped, Material.COMMAND_BLOCK_MINECART, "Command Block Minecart",
                "Runs its command while on rails.");
        add(tools, skipped, Material.STRUCTURE_BLOCK, "Structure Block",
                "Save and load structures.");
        add(tools, skipped, Material.JIGSAW, "Jigsaw Block",
                "Connects structure pieces during generation.");
        add(tools, skipped, Material.DEBUG_STICK, "Debug Stick",
                "Left-click to pick a property.", "Right-click to cycle its value.");
        add(tools, skipped, Material.KNOWLEDGE_BOOK, "Knowledge Book",
                "Grants recipes when used.");
        add(tools, skipped, Material.END_CRYSTAL, "End Crystal",
                "Place on obsidian or bedrock.");

        if (!skipped.isEmpty()) {
            logger.warning("Skipped " + skipped.size() + " builder tool(s) with no item form on this "
                    + "server version: " + String.join(", ", skipped));
        }
        return List.copyOf(tools);
    }

    private static void add(List<BuilderTool> tools, List<String> skipped, Material material,
                            String label, String... description) {
        if (!material.isItem()) {
            skipped.add(material.name());
            return;
        }
        tools.add(new BuilderTool(label, List.of(description), new ItemStack(material)));
    }

    private static BuilderTool light(int level) {
        if (!Material.LIGHT.isItem()) {
            return null;
        }
        ItemStack item = new ItemStack(Material.LIGHT);
        item.editMeta(BlockDataMeta.class, meta -> {
            Levelled data = (Levelled) Material.LIGHT.createBlockData();
            data.setLevel(level);
            meta.setBlockData(data);
        });
        return new BuilderTool("Light " + level,
                List.of("Invisible light source.", "Emits light level " + level + "."), item);
    }
}
