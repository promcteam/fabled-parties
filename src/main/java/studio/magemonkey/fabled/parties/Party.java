package studio.magemonkey.fabled.parties;

import studio.magemonkey.fabled.parties.event.PartyExpEvent;
import studio.magemonkey.fabled.parties.event.PlayerJoinPartyEvent;
import studio.magemonkey.fabled.parties.event.PlayerLeavePartyEvent;
import studio.magemonkey.fabled.parties.inject.Server;
import studio.magemonkey.fabled.parties.lang.IndividualNodes;
import studio.magemonkey.fabled.parties.lang.PartyNodes;
import studio.magemonkey.fabled.parties.mccore.PartyBoardManager;
import studio.magemonkey.fabled.api.enums.ExpSource;
import studio.magemonkey.fabled.api.player.PlayerClass;
import studio.magemonkey.fabled.api.player.PlayerData;
import studio.magemonkey.codex.mccore.config.Filter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Data for a party
 */
public class Party implements IParty {
    private final List<UUID>      members     = new ArrayList<>();
    private final Map<UUID, Long> invitations = new HashMap<>();
    private final FabledParties   plugin;
    private       UUID            leaderId;
    private       int             nextId      = -1;

    /**
     * Constructor
     *
     * @param plugin plugin reference
     * @param leader leader of the party
     */
    public Party(FabledParties plugin, Player leader) {
        this.plugin = plugin;
        this.leaderId = leader.getUniqueId();
        this.members.add(leader.getUniqueId());
    }

    /**
     * @return true if full, false otherwise
     */
    public boolean isFull() {
        checkInvitations();
        return invitations.size() + members.size() >= plugin.getMaxSize();
    }

    /**
     * @return true if the party is empty, false otherwise
     */
    public boolean isEmpty() {
        checkInvitations();
        return invitations.size() + members.size() <= 1;
    }

    /**
     * Retrieves the leader of the party
     *
     * @return party leader
     */
    public OfflinePlayer getLeader() {
        return Bukkit.getOfflinePlayer(this.leaderId);
    }

    /**
     * Gets the next member in a sequential order
     *
     * @return next player sequentially
     */
    public Player getSequentialPlayer() {
        Player member;
        do {
            nextId = (nextId + 1) % members.size();
        }
        while ((member = Bukkit.getPlayer(members.get(nextId))) == null);
        return member;
    }

    /**
     * Gets the next member that is within a sphere, in a sequential order
     *
     * @param location center of the sphere
     * @param radius   radius of the sphere
     * @return the next player within the sphere, or null if none was found
     */
    @Override
    @Nullable
    public Player getSequentialPlayer(Location location, double radius) {
        if (radius > 0) {
            radius *= radius;
        }
        int size = members.size();
        for (int i = 0; i < size; i++) {
            nextId = (nextId + 1) % size;
            Player player = Bukkit.getPlayer(members.get(nextId));
            if (player == null) {
                continue;
            }
            if (radius >= 0 && (!player.getWorld().equals(location.getWorld())
                    || player.getLocation().distanceSquared(location) > radius)) {
                continue;
            }
            return player;
        }
        return null;
    }

    /**
     * Gets a random player in the party
     *
     * @return random player in the party
     */
    public Player getRandomPlayer() {
        Player member;
        do {
            member = Bukkit.getPlayer(members.get(FabledParties.RNG.nextInt(members.size())));
        } while (member == null);
        return member;
    }

    /**
     * Gets a random member that is within a sphere, in a sequential order
     *
     * @param location center of the sphere
     * @param radius   radius of the sphere
     * @return random member within the sphere, or null if none was found
     */
    @Override
    @Nullable
    public Player getRandomPlayer(Location location, double radius) {
        if (radius > 0) {
            radius *= radius;
        }
        List<UUID> members = new ArrayList<>(this.members);
        int        size    = this.members.size();
        for (int i = 0; i < size; i++) {
            Player player = Bukkit.getPlayer(members.remove(FabledParties.RNG.nextInt(members.size())));
            if (player == null) {
                continue;
            }
            if (radius >= 0 && (!player.getWorld().equals(location.getWorld())
                    || player.getLocation().distanceSquared(location) > radius)) {
                continue;
            }
            return player;
        }
        return null;
    }

    /**
     * Clears all expired invitations from the map
     */
    public void checkInvitations() {
        for (UUID id : invitations.keySet()) {
            if (invitations.get(id) < System.currentTimeMillis()) {
                invitations.remove(id);
                OfflinePlayer op = Bukkit.getOfflinePlayer(id);
                sendMessages(plugin.getMessage(PartyNodes.NO_RESPONSE,
                        true,
                        Filter.PLAYER.setReplacement(op.getName())));
                if (op.isOnline()) {
                    plugin.sendMessage(op.getPlayer(), IndividualNodes.NO_RESPONSE);
                }
            }
        }
    }

    /**
     * @return size of the party
     */
    public int getPartySize() {
        return members.size();
    }

    /**
     * @return list of names of the members in the party
     */
    @Override
    public List<UUID> getMembers() {
        return Collections.unmodifiableList(members);
    }

    /**
     * @return number of online members in the party
     */
    public int getOnlinePartySize() {
        int counter = 0;
        for (UUID id : members) {
            if (Bukkit.getOfflinePlayer(id).isOnline()) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * Checks if the player is on the team
     *
     * @param player player to check
     * @return true if on the team, false otherwise
     */
    public boolean isMember(OfflinePlayer player) {
        return members.contains(player.getUniqueId());
    }

    /**
     * Checks if the player has been invited to join the party
     *
     * @param player player to check
     * @return true if invited to join the team, false otherwise
     */
    public boolean isInvited(Player player) {
        checkInvitations();
        return invitations.containsKey(player.getUniqueId());
    }

    /**
     * Checks if the player is the leader of the party
     *
     * @param player player to check
     * @return true if they're the leader, false otherwise
     */
    public boolean isLeader(Player player) {
        return leaderId.equals(player.getUniqueId());
    }

    /**
     * Adds a member to the party
     *
     * @param player player to add
     */
    public void invite(Player player) {
        if (!members.contains(player.getUniqueId()) && !invitations.containsKey(player.getUniqueId())) {
            invitations.put(player.getUniqueId(), System.currentTimeMillis() + plugin.getInviteTimeout());
        }
    }

    /**
     * Accepts a player into the party
     *
     * @param player player to accept
     */
    public void accept(Player player) {
        if (invitations.containsKey(player.getUniqueId())) {
            invitations.remove(player.getUniqueId());

            PlayerJoinPartyEvent event = new PlayerJoinPartyEvent(this, player);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            members.add(player.getUniqueId());
            if (members.size() == 2) {
                OfflinePlayer leader = getLeader();
                if (leader.isOnline())
                    PartyBoardManager.applyBoard(plugin, getLeader().getPlayer());
            }
            PartyBoardManager.applyBoard(plugin, player);
        }
    }

    /**
     * @param player player to decline
     */
    public void decline(Player player) {
        invitations.remove(player.getUniqueId());
    }

    /**
     * Removes a member from the party
     *
     * @param player player to remove
     */
    public void removeMember(Player player) {
        members.remove(player.getUniqueId());
        if (isLeader(player) && members.size() > 0) {
            changeLeader();
        }
        PartyBoardManager.clearBoard(plugin, player);
        Bukkit.getPluginManager().callEvent(new PlayerLeavePartyEvent(this, player));
        updateBoards();
    }

    /**
     * Changes the leader of the party if another member exists
     */
    public void changeLeader() {
        if (members.isEmpty()) return;
        members.stream().map(Bukkit::getOfflinePlayer)
                .filter(OfflinePlayer::isOnline)
                .findFirst()
                .ifPresent(op -> {
                    leaderId = op.getUniqueId();
                    members.remove(leaderId);
                    members.add(0, leaderId);
                    sendMessages(plugin.getMessage(PartyNodes.NEW_LEADER,
                            true,
                            Filter.PLAYER.setReplacement(op.getName())));
                });
    }

    /**
     * Removes scoreboards for the party
     */
    public void removeBoards() {
        members.stream().map(Bukkit::getOfflinePlayer)
                .filter(OfflinePlayer::isOnline)
                .forEach(player -> PartyBoardManager.clearBoard(plugin, player.getPlayer()));
    }

    /**
     * Shares experience within the party
     *
     * @param source    player who caused the experience gain
     * @param amount    amount received
     * @param expSource the source type of the gained experience
     */
    public void giveExp(Player source, double amount, ExpSource expSource) {
        if (getOnlinePartySize() == 0) {
            return;
        }
        double       radiusSq       = plugin.getExpShareRadiusSquared();
        List<Player> reachedPlayers = new ArrayList<>();
        for (UUID id : members) {
            Player player = Bukkit.getPlayer(id);
            if (player == null) {
                continue;
            }
            if (radiusSq >= 0 && (!player.getWorld().equals(source.getWorld())
                    || player.getLocation().distanceSquared(source.getLocation()) > radiusSq)) {
                continue;
            }
            reachedPlayers.add(player);
        }

        // Member modifier
        amount = amount / (1 + (reachedPlayers.size() - 1) * plugin.getMemberModifier());
        int level = Server.getLevel(source.getUniqueId());

        // Grant exp to all members
        for (Player player : reachedPlayers) {
            PlayerData  info = Server.getPlayerData(player);
            PlayerClass main = info.getMainClass();
            int         lvl  = main == null ? 0 : main.getLevel();
            int         exp;

            // Level modifier
            if (plugin.getLevelModifier() > 0) {
                int dl = lvl - level;
                exp = (int) Math.ceil(amount * Math.pow(2, -plugin.getLevelModifier() * dl * dl));
            } else {
                exp = (int) Math.ceil(amount);
            }

            info.giveExp(exp, expSource);
        }

        // Call event
        Bukkit.getPluginManager().callEvent(new PartyExpEvent(source, amount, expSource));
    }

    /**
     * Sends a message to the party
     *
     * @param message message to send
     */
    public void sendMessage(String message) {
        members.stream().map(Bukkit::getOfflinePlayer)
                .filter(OfflinePlayer::isOnline)
                .forEach(op -> op.getPlayer().sendMessage(message));
    }

    /**
     * Sends a list of messages to the party
     *
     * @param messages messages to send
     */
    public void sendMessages(List<String> messages) {
        members.stream().map(Bukkit::getOfflinePlayer)
                .filter(OfflinePlayer::isOnline)
                .forEach(op -> op.getPlayer().sendMessage(messages.toArray(new String[messages.size()])));
    }

    /**
     * Sends a message to all members in the party
     *
     * @param sender  the player who sent a party message
     * @param message message the player typed
     */
    public void sendMessage(Player sender, String message) {
        sendMessages(plugin.getMessage(PartyNodes.CHAT_MESSAGE,
                true,
                Filter.PLAYER.setReplacement(sender.getName()),
                Filter.MESSAGE.setReplacement(message)));
    }

    /**
     * Clears the party scoreboard for the player
     *
     * @param player player to clear for
     */
    public void clearBoard(Player player) {
        PartyBoardManager.clearBoard(plugin, player);
        if (isEmpty()) {
            removeBoards();
        }
    }

    /**
     * Updates all the scoreboards for the party
     */
    public void updateBoards() {
        removeBoards();
        members.stream().map(Bukkit::getOfflinePlayer)
                .filter(op -> op.isOnline())
                .forEach(op -> PartyBoardManager.applyBoard(plugin, op.getPlayer()));
    }

    void addMember(Player player) {
        members.add(player.getUniqueId());
    }
}
