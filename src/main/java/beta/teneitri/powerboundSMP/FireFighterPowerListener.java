package beta.teneitri.powerboundSMP;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class FireFighterPowerListener implements Listener {

    private final HashMap<UUID, Long> blastCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 15000;

    @EventHandler
    public void onFireFighterDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (!player.getScoreboardTags().contains("fire fighter")) return;

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.LAVA || cause == EntityDamageEvent.DamageCause.HOT_FLOOR) {
            event.setCancelled(true);
            player.setFireTicks(0);

            if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, false, false, false));
            }
        }
    }

    @EventHandler
    public void onFireFighterKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("fire fighter")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (blastCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = blastCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.RED + "Water tank refilling! Wait " + secondsLeft + "s");
                return;
            }
        }

        int radius = 6;
        org.bukkit.Location origin = player.getLocation();

        for (int x = -radius; x  0) {
                    living.setFireTicks(0);
                }
                String typeName = entity.getType().name();
                if (!entity.equals(player) && (typeName.contains("BLAZE") || typeName.contains("GHAST"))) {
                    living.damage(6.0, player);
                }
            }
        }
        player.sendMessage(ChatColor.AQUA + "PRESSURE BLAST: You cleared nearby flames!");
        blastCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
    }
}
