package studio.magemonkey.fabled.parties.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import studio.magemonkey.codex.mccore.commands.ConfigurableCommand;
import studio.magemonkey.codex.mccore.commands.IFunction;
import studio.magemonkey.codex.mccore.config.Filter;
import studio.magemonkey.fabled.parties.FabledParties;
import studio.magemonkey.fabled.parties.Party;
import studio.magemonkey.fabled.parties.lang.ErrorNodes;
import studio.magemonkey.fabled.parties.lang.IndividualNodes;

/**
 * Command to kick players out of the party
 */
public class CmdKick implements IFunction {
    @Override
    public void execute(ConfigurableCommand command, Plugin plugin, CommandSender sender, String[] args) {
        FabledParties fabledParties = (FabledParties) plugin;
        Player        player        = (Player) sender;

        // Requires at least one argument
        if (args.length == 0) {
            command.displayHelp(sender, 1);
            return;
        }

        // Cannot be yourself
        if (args[0].equalsIgnoreCase(player.getName())) {
            fabledParties.sendMessage(player, ErrorNodes.NO_KICK_SELF);
            return;
        }

        // Validate the player
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            fabledParties.sendMessage(player, ErrorNodes.NOT_ONLINE);
            return;
        }

        // Check the sender's party status
        Party party = fabledParties.getParty(player);
        if (party == null) {
            fabledParties.sendMessage(player, ErrorNodes.NO_PARTY);
            return;
        }

        // Doesn't have permission
        if (!party.isLeader(player)) {
            fabledParties.sendMessage(player, ErrorNodes.NOT_LEADER);
            return;
        }

        // Check the target's party status
        if (!party.isMember(target)) {
            fabledParties.sendMessage(player, ErrorNodes.NOT_IN_PARTY);
            return;
        }

        // Remove the player from the party
        party.removeMember(target);
        fabledParties.sendMessage(player,
                IndividualNodes.PLAYER_KICKED,
                Filter.PLAYER.setReplacement(target.getName()));
        fabledParties.sendMessage(target, IndividualNodes.KICKED, Filter.PLAYER.setReplacement(player.getName()));
    }
}
