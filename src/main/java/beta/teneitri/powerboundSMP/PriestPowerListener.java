package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class PriestPowerListener implements Listener {

    @EventHandler
    public void onPriestJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getScoreboardTags().contains("priest")) {
            if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(4.0);
            }
        }
    }

    @EventHandler
    public void onVampireBitePriest(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player priest = (Player) event.getEntity();

        if (!priest.getScoreboardTags().contains("priest")) return;

        if (event.getDamager() instanceof Player) {
            Player vampire = (Player) event.getDamager();
            if (vampire.getScoreboardTags().contains("vampire")) {

                event.setCancelled(true);

                priest.getWorld().playSound(priest.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.5f);
                priest.getWorld().spawnParticle(Particle.INSTANT_EFFECT, priest.getLocation().add(0, 1, 0), 15, 0.2, 0.2, 0.2, 0.1);

                priest.sendMessage(ChatColor.GOLD + " HOLY BARRIER: You are immune to the Vampire's unholy bite!");
                vampire.sendMessage(ChatColor.GRAY + " Your fangs cannot pierce the skin of a holy Priest!");
            }
        }
    }

    @EventHandler
    public void onPriestPurifyKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("priest")) return;
        event.setCancelled(true);

        Player vampireTarget = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : player.getNearbyEntities(10.0, 10.0, 10.0)) {
            if (entity instanceof Player) {
                Player potentialVampire = (Player) entity;
                if (potentialVampire.getScoreboardTags().contains("vampire") && !potentialVampire.equals(player)) {
                    double dist = player.getLocation().distance(potentialVampire.getLocation());
                    if (dist < closestDistance) {
                        closestDistance = dist;
                        vampireTarget = potentialVampire;
                    }
                }
            }
        }

        if (vampireTarget != null) {
            double currentVampireHealth = vampireTarget.getHealth();
            vampireTarget.setHealth(Math.max(1.0, currentVampireHealth - 4.0));

            vampireTarget.getWorld().playSound(vampireTarget.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.5f, 0.5f);
            vampireTarget.getWorld().spawnParticle(Particle.INSTANT_EFFECT, vampireTarget.getLocation().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.1);
            
            player.sendMessage(ChatColor.GOLD + " PRIEST: You purged the stolen lifeforce from " + vampireTarget.getName() + "!");
            vampireTarget.sendMessage(ChatColor.RED + " A Priest has purified your stolen blood, damaging your hearts!");

            Player victim = null;
            double closestVictimDist = Double.MAX_VALUE;

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.getScoreboardTags().contains("vampire") && !onlinePlayer.equals(player)) {
                    double dist = vampireTarget.getLocation().distance(onlinePlayer.getLocation());
                    if (dist < closestVictimDist && dist <= 25.0) {   
                        closestVictimDist = dist;
                        victim = onlinePlayer;
                    }
                }
            }

            if (victim != null) {
                double victimHealth = victim.getHealth();
                double victimMax = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                victim.setHealth(Math.min(victimMax, victimHealth + 4.0));

                victim.getWorld().spawnParticle(Particle.HEART, victim.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.05);
                victim.sendMessage(ChatColor.GREEN + " A Priest has returned the hearts stolen from you by the Vampire!");
            }
        } else {
            player.sendMessage(ChatColor.GRAY + "No vampires found nearby to purify.");
        }
    }
}
