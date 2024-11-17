package studio.magemonkey.fabled.parties.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import studio.magemonkey.codex.mccore.commands.ConfigurableCommand;
import studio.magemonkey.codex.mccore.commands.IFunction;
import studio.magemonkey.codex.mccore.config.Filter;
import studio.magemonkey.fabled.parties.FabledParties;
import studio.magemonkey.fabled.parties.Party;
import studio.magemonkey.fabled.parties.lang.ErrorNodes;
import studio.magemonkey.fabled.parties.lang.PartyNodes;

/**
 * Command to accept a party invitation
 */
public class CmdAccept implements IFunction {

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

        // Check the sender's party status
        Party party = fabledParties.getParty(player);
        if (party != null && party.isInvited(player)) {
            party.accept(player);

            // Join message
            party.sendMessages(fabledParties.getMessage(PartyNodes.PLAYER_JOINED,
                    true,
                    Filter.PLAYER.setReplacement(player.getName())));
        } else {
            fabledParties.sendMessage(player, ErrorNodes.NO_INVITES);
        }
    }
}
