package studio.magemonkey.fabled.parties.command;

import studio.magemonkey.codex.mccore.commands.ConfigurableCommand;
import studio.magemonkey.codex.mccore.commands.IFunction;
import studio.magemonkey.codex.mccore.config.CustomFilter;
import studio.magemonkey.codex.mccore.util.TextSizer;
import studio.magemonkey.fabled.parties.FabledParties;
import studio.magemonkey.fabled.parties.Party;
import studio.magemonkey.fabled.parties.lang.ErrorNodes;
import studio.magemonkey.fabled.parties.lang.IndividualNodes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Command to display party information
 */
public class CmdInfo implements IFunction {

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
        if (party != null && party.isMember(player)) {
            StringBuilder members = new StringBuilder();
            for (UUID id : party.getMembers()) {
                members.append(Bukkit.getOfflinePlayer(id).getName());
                members.append(", ");
            }
            fabledParties.sendMessage(
                    player,
                    IndividualNodes.INFO,
                    new CustomFilter("{leader}", party.getLeader().getName()),
                    new CustomFilter("{members}", members.substring(0, members.length() - 2)),
                    new CustomFilter("{size}", party.getPartySize() + ""),
                    new CustomFilter("{break}", TextSizer.createLine("", "-", ChatColor.DARK_GRAY))
            );
        } else {
            fabledParties.sendMessage(player, ErrorNodes.NO_PARTY);
        }
    }
}
