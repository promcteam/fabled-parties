package studio.magemonkey.fabled.parties.mccore;

import studio.magemonkey.codex.mccore.scoreboard.BoardManager;
import studio.magemonkey.codex.mccore.scoreboard.PlayerBoards;
import studio.magemonkey.codex.mccore.scoreboard.StatBoard;
import studio.magemonkey.fabled.parties.FabledParties;
import studio.magemonkey.fabled.parties.lang.PartyNodes;
import org.bukkit.entity.Player;

/**
 * Manages giving and removing scoreboards to players through MCCore
 */
public class PartyBoardManager {

    /**
     * Applies a scoreboard for the player
     *
     * @param plugin plugin reference
     * @param player player to apply to
     */
    public static void applyBoard(FabledParties plugin, Player player) {
        if (!plugin.isUsingScoreboard()) {
            return;
        }

        String title = plugin.getMessage(PartyNodes.SCOREBOARD, false).get(0);

        StatBoard board = new StatBoard(title, plugin.getName());
        board.addStats(new PartyStats(plugin, player, plugin.isLevelScoreboard()));
        PlayerBoards boards = BoardManager.getPlayerBoards(player.getName());
        boards.removeBoards(plugin.getName());
        boards.addBoard(board);
    }

    /**
     * Removes boards for this plugin on empty party
     *
     * @param plugin plugin reference
     * @param player player to remove for
     */
    public static void clearBoard(FabledParties plugin, Player player) {
        if (!plugin.isUsingScoreboard()) {
            return;
        }
        BoardManager.getPlayerBoards(player.getName()).removeBoards(plugin.getName());
    }

    /**
     * Clears all of the scoreboards for the plugin
     *
     * @param plugin plugin reference
     */
    public static void clearBoards(FabledParties plugin) {
        if (!plugin.isUsingScoreboard()) {
            return;
        }
        BoardManager.clearPluginBoards(plugin.getName());
    }
}
