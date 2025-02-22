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
 * Command to check the leader or change it
 */
public class CmdLeader implements IFunction {
    @Override
    public void execute(ConfigurableCommand command, Plugin plugin, CommandSender sender, String[] args) {
        FabledParties fabledParties = (FabledParties) plugin;
        Player        player        = (Player) sender;

        // Check the sender's party status
        Party party = fabledParties.getParty(player);
        if (party == null) {
            fabledParties.sendMessage(player, ErrorNodes.NO_PARTY);
            return;
        }

        // No arguments, display the current leader
        if (args.length == 0) {
            fabledParties.sendMessage(player,
                    IndividualNodes.PARTY_LEADER,
                    Filter.PLAYER.setReplacement(party.getLeader().getName()));
            return;
        }

        // Doesn't have permission
        if (!party.isLeader(player)) {
            fabledParties.sendMessage(player, ErrorNodes.NOT_LEADER);
            return;
        }

        // Validate the player
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            fabledParties.sendMessage(player, ErrorNodes.NOT_ONLINE);
            return;
        }

        // Check the target's party status
        if (!party.isMember(target)) {
            fabledParties.sendMessage(player, ErrorNodes.NOT_IN_PARTY);
            return;
        }

        // Make the player the new leader
        party.changeLeader(target);
    }
}
