/**
 * Parties
 * hook.studio.magemonkey.fabled.parties.InstancesHook
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

import studio.magemonkey.fabled.parties.FabledParties;
import studio.magemonkey.fabled.parties.IParty;
import org.bukkit.entity.Player;
import org.cyberiantiger.minecraft.instances.Instances;
import org.cyberiantiger.minecraft.instances.Party;

import java.util.HashMap;
import java.util.Map;

public class InstancesHook {
    private static FabledParties fabledParties;
    private static Instances     instances;

    private static final Map<Player, InstancesParty> playerMap  = new HashMap<>();
    private static final Map<Party, InstancesParty>  conversion = new HashMap<>();

    public static void init(FabledParties plugin) {
        fabledParties = plugin;
        instances = Instances.getPlugin(Instances.class);
    }

    public static IParty getParty(Player player) {
        InstancesParty watcher = playerMap.get(player);
        if (watcher == null || watcher.isEmpty()) {
            if (watcher != null) {
                conversion.remove(watcher.getParty());
            }

            Party party = instances.getParty(player);
            if (party == null) {
                return null;
            }

            watcher = conversion.get(party);
            if (watcher == null) {
                watcher = new InstancesParty(fabledParties, party);
                conversion.put(party, watcher);
            }
            playerMap.put(player, watcher);
            return watcher;
        } else {
            return watcher;
        }
    }

    public static void unload(Player player) {
        playerMap.remove(player);
    }
}
