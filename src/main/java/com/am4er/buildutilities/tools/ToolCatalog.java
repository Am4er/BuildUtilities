package com.am4er.buildutilities.tools;

import org.bukkit.Material;
import org.bukkit.block.data.Levelled;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class ToolCatalog {
    private static final int MAX_LIGHT_LEVEL = 15;

    private ToolCatalog() {
    }

    public static List<BuilderTool> load(Logger log) {
        Draft draft = new Draft();

        draft.add(Material.BARRIER, "Barrier",
                "Invisible solid block.", "Only shows while you hold one.");
        draft.add(Material.STRUCTURE_VOID, "Structure Void",
                "Invisible and walk-through.", "Means keep whatever is already there.");

        draft.lights();

        draft.add(Material.BEDROCK, "Bedrock", "Unbreakable in survival.");
        draft.add(Material.END_PORTAL_FRAME, "End Portal Frame", "Add eyes to open the portal.");
        draft.add(Material.BUDDING_AMETHYST, "Budding Amethyst",
                "Grows clusters.", "Never drops when mined.");
        draft.add(Material.REINFORCED_DEEPSLATE, "Reinforced Deepslate", "Ancient city frame block.");
        draft.add(Material.PETRIFIED_OAK_SLAB, "Petrified Oak Slab", "Stone-textured legacy slab.");
        draft.add(Material.DRAGON_EGG, "Dragon Egg", "Teleports when hit, push it with a piston.");
        draft.add(Material.CHORUS_PLANT, "Chorus Plant", "Stem block of a chorus tree.");
        draft.add(Material.END_CRYSTAL, "End Crystal", "Goes on obsidian or bedrock.");

        draft.add(Material.SPAWNER, "Monster Spawner", "Empty, set the mob with a spawn egg.");
        draft.add(Material.TRIAL_SPAWNER, "Trial Spawner", "Trial chamber spawner.");
        draft.add(Material.VAULT, "Vault", "Trial chamber reward vault.");
        draft.add(Material.SCULK_SHRIEKER, "Sculk Shrieker", "Summons a warden when it can.");

        draft.add(Material.FARMLAND, "Farmland", "Tilled soil, placed without a hoe.");
        draft.add(Material.DIRT_PATH, "Dirt Path", "Packed path, placed without a shovel.");
        draft.add(Material.SUSPICIOUS_SAND, "Suspicious Sand", "Brushable archaeology block.");
        draft.add(Material.SUSPICIOUS_GRAVEL, "Suspicious Gravel", "Brushable archaeology block.");

        draft.infested();

        draft.add(Material.COMMAND_BLOCK, "Command Block", "Impulse command block.");
        draft.add(Material.CHAIN_COMMAND_BLOCK, "Chain Command Block", "Runs after the block feeding it.");
        draft.add(Material.REPEATING_COMMAND_BLOCK, "Repeating Command Block", "Runs every tick while powered.");
        draft.add(Material.COMMAND_BLOCK_MINECART, "Command Block Minecart", "Runs its command on rails.");
        draft.add(Material.STRUCTURE_BLOCK, "Structure Block", "Save and load structures.");
        draft.add(Material.JIGSAW, "Jigsaw Block", "Joins structure pieces during generation.");
        draft.add(Material.DEBUG_STICK, "Debug Stick",
                "Left-click picks a property.", "Right-click cycles its value.");
        draft.add(Material.KNOWLEDGE_BOOK, "Knowledge Book", "Grants recipes when used.");

        return draft.finish(log);
    }

    private static final class Draft {
        private final List<BuilderTool> tools = new ArrayList<>();
        private final List<String> missing = new ArrayList<>();

        void add(Material material, String label, String... lore) {
            if (!material.isItem()) {
                missing.add(material.name());
                return;
            }
            tools.add(new BuilderTool(label, List.of(lore), new ItemStack(material)));
        }

        void lights() {
            if (!Material.LIGHT.isItem()) {
                missing.add(Material.LIGHT.name());
                return;
            }
            for (int level = 0; level <= MAX_LIGHT_LEVEL; level++) {
                tools.add(new BuilderTool("Light " + level,
                        List.of("Invisible light source.", "Emits light level " + level + "."),
                        lightItem(level)));
            }
        }

        void infested() {
            add(Material.INFESTED_STONE, "Infested Stone", SILVERFISH);
            add(Material.INFESTED_COBBLESTONE, "Infested Cobblestone", SILVERFISH);
            add(Material.INFESTED_DEEPSLATE, "Infested Deepslate", SILVERFISH);
            add(Material.INFESTED_STONE_BRICKS, "Infested Stone Bricks", SILVERFISH);
            add(Material.INFESTED_MOSSY_STONE_BRICKS, "Infested Mossy Stone Bricks", SILVERFISH);
            add(Material.INFESTED_CRACKED_STONE_BRICKS, "Infested Cracked Stone Bricks", SILVERFISH);
            add(Material.INFESTED_CHISELED_STONE_BRICKS, "Infested Chiseled Stone Bricks", SILVERFISH);
        }

        List<BuilderTool> finish(Logger log) {
            if (!missing.isEmpty()) {
                log.warning("No item form on this server version, skipped " + missing.size()
                        + ": " + String.join(", ", missing));
            }
            return List.copyOf(tools);
        }

        private static final String SILVERFISH = "Releases a silverfish when broken.";

        private static ItemStack lightItem(int level) {
            ItemStack item = new ItemStack(Material.LIGHT);
            item.editMeta(BlockDataMeta.class, meta -> {
                Levelled data = (Levelled) Material.LIGHT.createBlockData();
                data.setLevel(level);
                meta.setBlockData(data);
            });
            return item;
        }
    }
}
