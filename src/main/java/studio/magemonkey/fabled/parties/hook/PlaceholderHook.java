package studio.magemonkey.fabled.parties.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.fabled.parties.FabledParties;
import studio.magemonkey.fabled.parties.IParty;

public class PlaceholderHook extends PlaceholderExpansion {
    private final FabledParties plugin;

    public PlaceholderHook(FabledParties plugin) {this.plugin = plugin;}

    @Override
    @NotNull
    public String getIdentifier() {return "fabled-parties";}

    @Override
    @NotNull
    public String getAuthor() {return "Sentropic";}

    @Override
    @NotNull
    public String getVersion() {return "1.0.0";}

    @Override
    public boolean persist() {return true;}

    @Override
    @Nullable
    public String onRequest(@Nullable OfflinePlayer player, @NotNull String placeholder) {
        placeholder = PlaceholderAPI.setBracketPlaceholders(player, placeholder);

        // Placeholders available without a party
        switch (placeholder) {
            case "max_size": {
                return String.valueOf(plugin.getMaxSize());
            }
            case "invite_timeout": {
                return String.valueOf(plugin.getInviteTimeout());
            }
            case "exp_share_radius": {
                return String.valueOf(plugin.getExpShareRadius());
            }
            case "exp_member_modifier": {
                return String.valueOf(plugin.getMemberModifier());
            }
            case "exp_level_modifier": {
                return String.valueOf(plugin.getLevelModifier());
            }
            case "item_share_radius": {
                return String.valueOf(plugin.getItemShareRadius());
            }
        }

        // Placeholders that require a Party
        if (player == null) {
            return null;
        }
        IParty party = plugin.getJoinedParty(player);
        if (party == null) {
            return "";
        }
        String[] args = placeholder.split("_");

        try {
            switch (args[0]) {
                case "leader": {
                    switch (args[1]) {
                        case "name": {
                            return party.getLeader().getName();
                        }
                        case "uuid": {
                            return party.getLeader().getUniqueId().toString();
                        }
                    }
                }
                case "size": {
                    return String.valueOf(party.getMembers().size());
                }
                case "name": {
                    return Bukkit.getOfflinePlayer(party.getMembers().get(Integer.parseInt(args[1]) - 1)).getName();
                }
                case "uuid": {
                    return party.getMembers().get(Integer.parseInt(args[1]) - 1).toString();
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        } catch (IndexOutOfBoundsException ignored) {
        }
        return "";
    }
}
