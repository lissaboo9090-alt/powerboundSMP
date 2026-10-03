package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

public class mace {

    public static void registerMaceRecipe(Plugin plugin) {
        ItemStack customMace = new ItemStack(Material.MACE, 1);

        NamespacedKey key = new NamespacedKey(plugin, "custom_hard_mace_recipe");
        ShapedRecipe recipe = new ShapedRecipe(key, customMace);

        recipe.shape(
            "DND",
            "NCN",
            " C "
        );

        recipe.setIngredient('N', Material.NETHERITE_INGOT); // 3 Netherite Ingots
        recipe.setIngredient('C', Material.HEAVY_CORE);       // 2 Mace Cores
        recipe.setIngredient('D', Material.DRAGON_HEAD);     // 2 Dragon Heads

        Bukkit.addRecipe(recipe);
    }
}
