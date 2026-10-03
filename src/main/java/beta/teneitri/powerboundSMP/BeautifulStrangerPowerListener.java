package beta.teneitri.powerboundSMP;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class BeautifulStrangerPowerListener implements Listener {

    private final HashMap<UUID, Long> charmCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 35000; 

    @EventHandler
    public void onStrangerTakeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (!player.getScoreboardTags().contains("beautiful stranger")) return;

        if (event.getDamager() instanceof Arrow) {
            double originalDamage = event.getDamage();
            event.setDamage(originalDamage * 0.7); 
            player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.5, 0), 3, 0.2, 0.2, 0.2, 0.01);
        }
    }

    @EventHandler
    public void onStrangerKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("beautiful stranger")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (charmCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = charmCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Charm allure is on cool down! Wait " + secondsLeft + "s");
                return;
            }
        }

        Player target = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : player.getNearbyEntities(8.0, 8.0, 8.0)) {
            if (entity instanceof Player && !entity.equals(player)) {
                double dist = player.getLocation().distance(entity.getLocation());
                if (dist < closestDistance) {
                    closestDistance = dist;
                    target = (Player) entity;
                }
            }
        }

        if (target != null) {
            Location targetLoc = target.getLocation();
            targetLoc.setYaw(targetLoc.getYaw() + 180f);
            target.teleport(targetLoc);

            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false, true));

            ItemStack heldItem = target.getInventory().getItemInMainHand();
            if (heldItem != null && heldItem.getType() != Material.AIR) {
                target.getWorld().dropItemNaturally(target.getLocation(), heldItem.clone());
                target.getInventory().setItemInMainHand(null);
                target.sendMessage(ChatColor.RED + "You were so stunned by the stranger that you dropped your item!");
            }

            target.getWorld().spawnParticle(Particle.HEART, target.getLocation().add(0, 1, 0), 25, 0.4, 0.5, 0.4, 0.02);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.5f, 0.5f);

            player.sendMessage(ChatColor.LIGHT_PURPLE + "BEAUTIFUL STRANGER: You completely disoriented " + target.getName() + "!");
            charmCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        } else {
            player.sendMessage(ChatColor.GRAY + "Nobody is close enough to be enchanted.loser.");
        }
    }
}
