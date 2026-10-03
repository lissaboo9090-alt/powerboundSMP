package beta.teneitri.powerboundSMP;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class FrostPowerListener implements Listener {

    private final HashMap<UUID, Long> frostCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 35000;

    @EventHandler
    public void onFrostKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("frost")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (frostCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = frostCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.AQUA + " Absolute Zero is on cool down! Cooldown: " + secondsLeft + "s");
                return;
            }
        }

        double radius = 12.0;
        boolean foundTargets = false;

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 2.0f, 0.5f);

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity target = (LivingEntity) entity;
                foundTargets = true;

                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 120, 9, false, true, true));
                target.setFreezeTicks(120);

                target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 20, 0.4, 0.5, 0.4, 0.01);

                if (target instanceof Player) {
                    target.sendMessage(ChatColor.AQUA + " a wannabe elsa has frozen you for 6 seconds!");
                }
            }
        }

        if (foundTargets) {
            player.sendMessage(ChatColor.DARK_AQUA + " ABSOLUTE ZERO: You have frozen nearby targets!");
            frostCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        } else {
            player.sendMessage(ChatColor.GRAY + "You tried to do Absolute Zero, but nobody was nearby to freeze.");
        }
    }
}
