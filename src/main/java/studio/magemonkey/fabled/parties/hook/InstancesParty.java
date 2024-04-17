/**
 * Parties
 * hook.studio.magemonkey.fabled.parties.InstancesParty
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2024 MageMonkeyStudio
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package studio.magemonkey.fabled.parties.hook;

import studio.magemonkey.codex.mccore.config.Filter;
import studio.magemonkey.fabled.Fabled;
import studio.magemonkey.fabled.api.enums.ExpSource;
import studio.magemonkey.fabled.api.player.PlayerClass;
import studio.magemonkey.fabled.api.player.PlayerData;
import studio.magemonkey.fabled.parties.FabledParties;
import studio.magemonkey.fabled.parties.IParty;
import studio.magemonkey.fabled.parties.lang.PartyNodes;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.cyberiantiger.minecraft.instances.Party;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class InstancesParty implements IParty {
    private final FabledParties plugin;
    private final Party         party;

    private int nextId = -1;

    public InstancesParty(FabledParties plugin, Party party) {
        this.plugin = plugin;
        this.party = party;
    }

    public Party getParty() {
        return party;
    }

    @Override
    public List<UUID> getMembers() {
        List<UUID> members = new ArrayList<>(party.getMembers().size());
        for (Player member : party.getMembers()) {
            members.add(member.getUniqueId());
        }
        return Collections.unmodifiableList(members);
    }

    @Override
    public OfflinePlayer getLeader() {return party.getLeader();}

    @Override
    public Player getSequentialPlayer() {
        nextId = (nextId + 1) % party.getMembers().size();
        return party.getMembers().get(nextId);
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
        int size = party.getMembers().size();
        for (int i = 0; i < size; i++) {
            nextId = (nextId + 1) % party.getMembers().size();
            Player player = party.getMembers().get(nextId);
            if (radius >= 0 && (!player.getWorld().equals(location.getWorld())
                    || player.getLocation().distanceSquared(location) > radius)) {
                continue;
            }
            return player;
        }
        return null;
    }

    @Override
    public Player getRandomPlayer() {
        return party.getMembers().get((int) (party.getMembers().size() * Math.random()));
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
        List<Player> members = new ArrayList<>(party.getMembers());
        int          size    = party.getMembers().size();
        for (int i = 0; i < size; i++) {
            Player player = members.remove(FabledParties.RNG.nextInt(members.size()));
            if (radius >= 0 && (!player.getWorld().equals(location.getWorld())
                    || player.getLocation().distanceSquared(location) > radius)) {
                continue;
            }
            return player;
        }
        return null;
    }

    @Override
    public void giveExp(Player source, double amount, ExpSource expSource) {
        if (isEmpty()) {
            return;
        }
        double       radiusSq       = plugin.getExpShareRadiusSquared();
        List<Player> reachedPlayers = new ArrayList<>();
        for (Player member : party.getMembers()) {
            if (radiusSq >= 0 && (!member.getWorld().equals(source.getWorld())
                    || member.getLocation().distanceSquared(source.getLocation()) > radiusSq)) {
                continue;
            }
            reachedPlayers.add(member);
        }


        // Member modifier
        amount = amount / (1 + (reachedPlayers.size() - 1) * plugin.getMemberModifier());
        PlayerData  data  = Fabled.getPlayerData(source);
        PlayerClass main  = data.getMainClass();
        int         level = main == null ? 0 : main.getLevel();

        // Grant exp to all members
        for (Player member : reachedPlayers) {
            // Player must be online
            PlayerData info = Fabled.getPlayerData(member);
            main = info.getMainClass();
            int lvl = main == null ? 0 : main.getLevel();
            int exp;

            // Level modifier
            if (plugin.getLevelModifier() > 0) {
                int dl = lvl - level;
                exp = (int) Math.ceil(amount * Math.pow(2, -plugin.getLevelModifier() * dl * dl));
            } else {
                exp = (int) Math.ceil(amount);
            }

            info.giveExp(exp, expSource);
        }
    }

    /**
     * Sends a message to all members in the party
     *
     * @param sender  the player who sent a party message
     * @param message message the player typed
     */
    @Override
    public void sendMessage(Player sender, String message) {
        List<String> messages = plugin.getMessage(PartyNodes.CHAT_MESSAGE,
                true,
                Filter.PLAYER.setReplacement(sender.getName()),
                Filter.MESSAGE.setReplacement(message));
        for (String line : messages) {
            party.sendAll(line);
        }
    }

    /**
     * Checks whether or not no players are left in the party
     *
     * @return true if empty
     */
    @Override
    public boolean isEmpty() {
        return party.getMembers().size() == 0;
    }
}
