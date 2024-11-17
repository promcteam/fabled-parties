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
import studio.magemonkey.fabled.parties.lang.PartyNodes;

/**
 * Command to invite other players to a party
 */
public class CmdInvite implements IFunction {

    /**
     * Executes the command
     *
     * @param command owning command
     * @param plugin  plugin reference
     * @param sender  sender of the command
     * @param args    arguments provided
     */
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
            fabledParties.sendMessage(player, ErrorNodes.NO_INVITE_SELF);
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
        if (party != null) {

            // Party is full
            if (party.isFull()) {
                fabledParties.sendMessage(player, ErrorNodes.PARTY_FULL);
                return;
            }

            // Doesn't have permission
            if (fabledParties.isLeaderInviteOnly() && !party.isLeader(player)) {
                fabledParties.sendMessage(player, ErrorNodes.NOT_LEADER);
                return;
            }
        }

        // Check the target's party status
        Party targetParty = fabledParties.getParty(target);
        if (targetParty != null && !targetParty.isEmpty()) {
            fabledParties.sendMessage(player, ErrorNodes.IN_OTHER_PARTY);
            return;
        }

        // Clear the target's party
        if (targetParty != null) {
            fabledParties.removeParty(targetParty);
        }

        // Initialize a new party if it doesn't exist
        if (party == null) {
            party = new Party(fabledParties, player);
            fabledParties.addParty(party);
        }

        // Invite the target
        party.invite(target);
        party.sendMessages(fabledParties.getMessage(PartyNodes.PLAYER_INVITED,
                true,
                Filter.PLAYER.setReplacement(target.getName())));
        fabledParties.sendMessage(target, IndividualNodes.INVITED, Filter.PLAYER.setReplacement(player.getName()));
    }
}
