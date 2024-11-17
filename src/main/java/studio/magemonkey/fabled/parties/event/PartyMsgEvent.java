package studio.magemonkey.fabled.parties.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import studio.magemonkey.fabled.parties.Party;

/**
 * Called when a player sends a message to their party
 */
public class PartyMsgEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Party   party;
    private final Player  sender;
    private final String  message;
    private       boolean cancelled;

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public PartyMsgEvent(Party party, Player sender, String message) {
        this.party = party;
        this.sender = sender;
        this.message = message;
    }

    /**
     * @return the affected party
     */
    public Party getParty() {
        return party;
    }

    /**
     * @return the player that sent the message
     */
    public Player getSender() {
        return sender;
    }

    /**
     * @return the sent message
     */
    public String getMessage() {
        return message;
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
