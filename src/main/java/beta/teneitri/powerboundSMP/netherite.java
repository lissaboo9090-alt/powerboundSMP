package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

public class netherite {

    public static void registerNetheriteRecipe(Plugin plugin) {
        ItemStack netheriteIngot = new ItemStack(Material.NETHERITE_INGOT, 1);

        NamespacedKey key = new NamespacedKey(plugin, "custom_netherite_ingot_recipe");
        ShapedRecipe recipe = new ShapedRecipe(key, netheriteIngot);

        recipe.shape(
            "G G G",
            "D S D",
            "O B O"
        );

        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('S', Material.NETHERITE_SCRAP);
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('B', Material.BLAZE_POWDER);

        Bukkit.addRecipe(recipe);
    }
}
