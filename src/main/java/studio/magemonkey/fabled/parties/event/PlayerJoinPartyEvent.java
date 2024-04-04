package studio.magemonkey.fabled.parties.event;

import studio.magemonkey.fabled.parties.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player joins an existing party
 */
public class PlayerJoinPartyEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Party   party;
    private final Player  player;
    private       boolean cancelled;

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public PlayerJoinPartyEvent(Party party, Player player) {
        this.party = party;
        this.player = player;
    }

    /**
     * @return the party the player joined to
     */
    public Party getParty() {
        return party;
    }

    /**
     * @return the player that joined the party
     */
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
