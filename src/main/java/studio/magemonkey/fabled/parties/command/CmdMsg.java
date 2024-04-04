package studio.magemonkey.fabled.parties.command;

import studio.magemonkey.codex.mccore.commands.ConfigurableCommand;
import studio.magemonkey.codex.mccore.commands.IFunction;
import studio.magemonkey.fabled.parties.FabledParties;
import studio.magemonkey.fabled.parties.Party;
import studio.magemonkey.fabled.parties.event.PartyMsgEvent;
import studio.magemonkey.fabled.parties.lang.ErrorNodes;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Command to invite other players to a party
 */
public class CmdMsg implements IFunction {

    /**
     * Executes the command
     *
     * @param command handler for the commands
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

        // Check the sender's party status
        Party party = fabledParties.getParty(player);
        if (party != null && !party.isEmpty()) {
            StringBuilder text = new StringBuilder(args[0]);
            for (int i = 1; i < args.length; i++) {
                text.append(" ").append(args[i]);
            }
            String        message = text.toString();
            PartyMsgEvent event   = new PartyMsgEvent(party, player, message);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                party.sendMessage(player, message);
            }
        }

        // Not in a party
        else {
            fabledParties.sendMessage(player, ErrorNodes.NO_PARTY);
        }
    }
}
