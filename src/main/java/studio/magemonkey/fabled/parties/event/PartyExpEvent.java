package studio.magemonkey.fabled.parties.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import studio.magemonkey.fabled.api.enums.ExpSource;

/**
 * Called when a party gains shared exp
 */
public class PartyExpEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player    source;
    private final double    amount;
    private final ExpSource expSource;

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public PartyExpEvent(Player source, double amount, ExpSource expSource) {
        this.source = source;
        this.amount = amount;
        this.expSource = expSource;
    }

    /**
     * @return the player that first obtained the exp
     */
    public Player getSourcePlayer() {
        return source;
    }

    /**
     * @return the amount of exp obtained
     */
    public double getAmount() {
        return amount;
    }

    /**
     * @return the source of exp
     */
    public ExpSource getExpSource() {
        return expSource;
    }
}
