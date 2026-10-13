package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class PoseidonPowerListener implements Listener {

    private final HashMap<UUID, Long> rainCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 10000; 
    private ItemStack createPoseidonTrident() {
        ItemStack trident = new ItemStack(Material.TRIDENT);
        ItemMeta meta = trident.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Maelstrom Trident");
            meta.addEnchant(Enchantment.RIPTIDE, 3, true);
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.setUnbreakable(true);
            trident.setItemMeta(meta);
        }
        return trident;
    }
    private void ensureTrident(Player player) {
        if (!player.getScoreboardTags().contains("poseidon")) return;
        if (!player.getInventory().contains(Material.TRIDENT)) {
            player.getInventory().addItem(createPoseidonTrident());
            player.sendMessage(ChatColor.AQUA + " Your Maelstrom Trident returns to your hands.");
        }
    }

    @EventHandler
    public void onPoseidonJoin(PlayerJoinEvent event) {
        ensureTrident(event.getPlayer());
        
        Player player = event.getPlayer();
        if (player.getScoreboardTags().contains("poseidon") && player.isInWater()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 0, false, false, true));
        }
    }

    @EventHandler
    public void onPoseidonMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("poseidon")) return;

        if (player.getTicksLived() % 100 == 0) {
            ensureTrident(player);
        }

        if (player.isInWater()) {
            if (!player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 0, false, false, true));
            }
        } else {
            if (player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE)) {
                player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
            }
        }
    }

    @EventHandler
    public void onTridentDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("poseidon")) return;

        ItemStack item = event.getItemDrop().getItemStack();
        if (item.getType() == Material.TRIDENT && item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot discard your bound weapon!");
        }
    }

    @EventHandler
    public void onTridentMoveInventory(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!player.getScoreboardTags().contains("poseidon")) return;

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null && currentItem.getType() == Material.TRIDENT) {
            if (event.getClickedInventory() != player.getInventory()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "The Maelstrom Trident cannot leave your possession.");
            }
        }
    }

    @EventHandler
    public void onPoseidonDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!player.getScoreboardTags().contains("poseidon")) return;

        event.getDrops().removeIf(item -> item.getType() == Material.TRIDENT);
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("powerboundSMP"), () -> {
            if (player.isOnline()) ensureTrident(player);
        }, 10L);
    }

    @EventHandler
    public void onPoseidonWeatherKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("poseidon")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (rainCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = rainCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.AQUA + " The tides are adjusting! Wait " + secondsLeft + "s");
                return;
            }
        }

        boolean isRaining = player.getWorld().hasStorm();

        if (!isRaining) {
            player.getWorld().setStorm(true);
            player.getWorld().setWeatherDuration(12000); 
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);
            player.getWorld().spawnParticle(Particle.DRIPPING_WATER, player.getLocation().add(0, 2, 0), 40, 0.5, 0.5, 0.5, 0.1);
            player.sendMessage(ChatColor.AQUA + " POSEIDON: You summon the wrath of the storm skies!");
        } else {
      
            player.getWorld().setStorm(false);
            player.getWorld().setThundering(false);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL_DRAGON_BREATH, 1.0f, 1.2f);
            player.sendMessage(ChatColor.YELLOW + " POSEIDON: You quell the tides and clear the storm.");
        }

        rainCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
    }
}
