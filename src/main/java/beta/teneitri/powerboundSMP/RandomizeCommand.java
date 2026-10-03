package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomizeCommand implements CommandExecutor {

    private final String[] ROLES = {
        "archer", "beautiful stranger", "cheesecake", "chronos", "chiwawa", "dash", 
        "dreamer", "fade out", "fire fighter", "frost", "gravity", "light", 
        "lighting", "lock pot", "miner", "phoenix", "ragdoll", "shadow", 
        "tank", "telepathy", "teleport", "time", "void", "witch"
    };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + " You do not have permissions to randomize structural roles.");
            return true;
        }

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) {
            sender.sendMessage(ChatColor.RED + " Nobody is online right now to randomize tags into!");
            return true;
        }

        for (Player player : players) {
            for (String role : ROLES) {
                player.removeScoreboardTag(role);
            }
        }

        List<String> rolePool = new ArrayList<>();
        while (rolePool.size() < players.size()) {
            List<String> shuffledRoles = new ArrayList<>(List.of(ROLES));
            Collections.shuffle(shuffledRoles);
            rolePool.addAll(shuffledRoles);
        }

        for (int i = 0; i < players.size(); i++) {
            Player target = players.get(i);
            String assignedRole = rolePool.get(i);
            
            target.addScoreboardTag(assignedRole);
            
            target.sendTitle(ChatColor.GOLD + "✨ SHUFFLED ✨", ChatColor.YELLOW + "Your new role: " + assignedRole.toUpperCase(), 10, 70, 20);
            target.sendMessage(ChatColor.GREEN + "You have been assigned : " + ChatColor.GOLD + assignedRole.toUpperCase());
            target.playSound(target.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        }

        Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "[Powerbound] All active structural player tracking roles have been completely randomized!");
        return true;
    }
}
