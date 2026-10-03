package beta.teneitri.powerboundSMP;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JoinMatchCommand implements CommandExecutor {

    private final String[] ROLES = {
        "archer", "beautiful stranger", "cheesecake", "chronos", "chiwawa", "dash", 
        "dreamer", "fade out", "fire fighter", "frost", "gravity", "light", 
        "lighting", "lock pot", "miner", "phoenix", "ragdoll", "shadow", 
        "tank", "telepathy", "teleport", "time", "void", "witch"
    };
    
    private final Random random = new Random();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only physical players can execute this match entry command.");
            return true;
        }

        Player player = (Player) sender;

        for (String role : ROLES) {
            if (player.getScoreboardTags().contains(role)) {
                player.sendMessage(ChatColor.RED + "You are already assigned to a power: " + ChatColor.YELLOW + role.toUpperCase());
                return true;
            }
        }

        String assignedRole = ROLES[random.nextInt(ROLES.length)];
        player.addScoreboardTag(assignedRole);

        player.sendTitle(ChatColor.AQUA + "MATCH JOINED ", ChatColor.GREEN + "Assigned Role: " + assignedRole.toUpperCase(), 10, 60, 10);
        player.sendMessage(ChatColor.GREEN + " You joined late but successfully rolled a power : " + ChatColor.GOLD + assignedRole.toUpperCase());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        return true;
    }
}
