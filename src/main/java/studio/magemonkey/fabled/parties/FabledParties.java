package studio.magemonkey.fabled.parties;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import studio.magemonkey.codex.mccore.commands.CommandManager;
import studio.magemonkey.codex.mccore.commands.ConfigurableCommand;
import studio.magemonkey.codex.mccore.commands.SenderType;
import studio.magemonkey.codex.mccore.config.CommentedConfig;
import studio.magemonkey.codex.mccore.config.CommentedLanguageConfig;
import studio.magemonkey.codex.mccore.config.CustomFilter;
import studio.magemonkey.codex.mccore.config.FilterType;
import studio.magemonkey.codex.mccore.config.parse.DataSection;
import studio.magemonkey.fabled.Fabled;
import studio.magemonkey.fabled.api.SkillPlugin;
import studio.magemonkey.fabled.dynamic.custom.CustomEffectComponent;
import studio.magemonkey.fabled.parties.command.*;
import studio.magemonkey.fabled.parties.components.PartyTarget;
import studio.magemonkey.fabled.parties.hook.Hooks;
import studio.magemonkey.fabled.parties.hook.PlaceholderHook;
import studio.magemonkey.fabled.parties.mccore.PartyBoardManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Add-on plugin for Fabled allowing parties with shared experience
 */
public class FabledParties extends JavaPlugin implements SkillPlugin {
    public static final Random        RNG = new Random();
    private static      FabledParties inst;

    private final List<Party>             parties     = new ArrayList<>();
    private final List<Party>             partiesRead = Collections.unmodifiableList(parties);
    private final List<String>            toggled     = new ArrayList<>();
    private       CommentedLanguageConfig language;
    private       UpdateTask              task;
    private       String                  sharing;
    private       double                  itemShareRadius;
    private       boolean                 removeOnDc;
    private       boolean                 newLeaderOnDc;
    private       boolean                 leaderInviteOnly;
    private       boolean                 friendlyFire;
    private       boolean                 partyAlly;
    private       boolean                 useScoreboard;
    private       boolean                 levelScoreboard;
    private       boolean                 debug;
    private       double                  expShareRadius;
    private       double                  expShareRadiusSq;
    private       double                  memberModifier;
    private       double                  levelModifier;
    private       long                    inviteTimeout;
    private       int                     maxSize;

    public FabledParties() {
        super();
        inst = this;
    }

    public FabledParties(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        inst = this;
    }

    public static FabledParties inst() {
        return inst;
    }

    /**
     * Loads settings and sets up the listeners
     */
    @Override
    public void onEnable() {
        task = new UpdateTask(this);

        loadConfiguration();

        new PartyListener(this);

        // Set up commands
        registerCommands();

        Hooks.init(this);
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).register();
            getLogger().info("Hooked to PlaceholderAPI");
        }
    }

    private void registerCommands() {
        ConfigurableCommand root = new ConfigurableCommand(this, "pt", SenderType.ANYONE);
        root.addSubCommands(
                new ConfigurableCommand(this,
                        "accept",
                        SenderType.PLAYER_ONLY,
                        new CmdAccept(),
                        "Accepts a party request",
                        "",
                        PermissionNode.GENERAL),
                new ConfigurableCommand(this,
                        "decline",
                        SenderType.PLAYER_ONLY,
                        new CmdDecline(),
                        "Declines a party request",
                        "",
                        PermissionNode.GENERAL),
                new ConfigurableCommand(this,
                        "info",
                        SenderType.PLAYER_ONLY,
                        new CmdInfo(),
                        "Views party information",
                        "",
                        PermissionNode.GENERAL),
                new ConfigurableCommand(this,
                        "invite",
                        SenderType.PLAYER_ONLY,
                        new CmdInvite(),
                        "Invites a player to a party",
                        "<player>",
                        PermissionNode.GENERAL),
                new ConfigurableCommand(this,
                        "kick",
                        SenderType.PLAYER_ONLY,
                        new CmdKick(),
                        "Kicks a player from the party",
                        "<player>",
                        PermissionNode.GENERAL),
                new ConfigurableCommand(this,
                        "leave",
                        SenderType.PLAYER_ONLY,
                        new CmdLeave(),
                        "Leaves your party",
                        "",
                        PermissionNode.GENERAL),
                new ConfigurableCommand(this,
                        "leader",
                        SenderType.PLAYER_ONLY,
                        new CmdLeader(),
                        "Checks or changes the party leader",
                        "[player]",
                        PermissionNode.GENERAL),
                new ConfigurableCommand(this,
                        "message",
                        SenderType.PLAYER_ONLY,
                        new CmdMsg(),
                        "Sends a message to your party",
                        "<message>",
                        PermissionNode.GENERAL),
                new ConfigurableCommand(this,
                        "toggle",
                        SenderType.PLAYER_ONLY,
                        new CmdToggle(),
                        "Toggles party chat on/off",
                        "",
                        PermissionNode.GENERAL),
                new ConfigurableCommand(this,
                        "reload",
                        SenderType.ANYONE,
                        new CmdReload(),
                        "Reloads the plugin's config.yml and language.yml",
                        "",
                        PermissionNode.RELOAD)
        );
        CommandManager.registerCommand(root);
    }

    public void loadConfiguration() {
        CommentedConfig config = new CommentedConfig(this, "config");
        config.saveDefaultConfig();
        config.trim();
        config.checkDefaults();
        config.save();
        DataSection settings = config.getConfig();

        language = new CommentedLanguageConfig(this, "language");

        sharing = settings.getString("sharing.type", "none");
        itemShareRadius = settings.getDouble("sharing.radius", 0);
        removeOnDc = settings.getBoolean("remove-on-dc", false);
        newLeaderOnDc = settings.getBoolean("new-leader-on-dc", true);
        leaderInviteOnly = settings.getBoolean("only-leader-invites", true);
        friendlyFire = settings.getBoolean("friendly-fire", true);
        useScoreboard = settings.getBoolean("use-scoreboard", false);
        levelScoreboard = settings.getBoolean("level-scoreboard", false);
        expShareRadius = settings.getDouble("exp-modifications.radius", 0);
        expShareRadiusSq = expShareRadius > 0 ? Math.pow(expShareRadius, 2) : expShareRadius;
        memberModifier = settings.getDouble("exp-modifications.members", 1.0);
        levelModifier = settings.getDouble("exp-modifications.level", 0.0);
        inviteTimeout = settings.getInt("invite-timeout", 30) * 1000L;
        maxSize = settings.getInt("max-size", 4);
        partyAlly = settings.getBoolean("party-ally", true);
        debug = settings.getBoolean("debug-messages", false);
    }

    /**
     * Clears plugin data
     */
    @Override
    public void onDisable() {
        task.cancel();
        PartyBoardManager.clearBoards(this);
        HandlerList.unregisterAll(this);
        parties.clear();
    }

    /**
     * @return retrieves the type of sharing being used
     */
    public String getShareMode() {
        return sharing;
    }

    /**
     * @return the maximum distance between party members for the item sharing to take effect, in blocks
     */
    public double getItemShareRadius() {return itemShareRadius;}

    /**
     * @return whether party members are removed upon disconnect
     */
    public boolean isRemoveOnDc() {
        return removeOnDc;
    }

    /**
     * @return whether a new party leader is chosen upon disconnect
     */
    public boolean isNewLeaderOnDc() {
        return newLeaderOnDc;
    }

    /**
     * @return whether only the leader can invite new party members
     */
    public boolean isLeaderInviteOnly() {
        return leaderInviteOnly;
    }

    /**
     * @return whether members of the same party can hurt each other
     */
    public boolean isFriendlyFireEnabled() {return friendlyFire;}

    /**
     * @return whether party members should be treated as allies in Fabled
     */
    public boolean isPartyAllyEnabled() {return partyAlly;}

    /**
     * @return whether scoreboards are being used
     */
    public boolean isUsingScoreboard() {
        return useScoreboard;
    }

    /**
     * @return whether levels are shown in the scoreboard over health
     */
    public boolean isLevelScoreboard() {
        return levelScoreboard;
    }

    /**
     * @return how long in milliseconds a party invitation lasts before expiring
     */
    public long getInviteTimeout() {
        return inviteTimeout;
    }

    /**
     * @return max size of the party
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * @return the maximum distance between party members for the exp sharing to take effect, in blocks
     */
    public double getExpShareRadius() {return expShareRadius;}

    /**
     * @return the squared maximum distance between party members for the exp sharing to take effect, in blocks
     */
    public double getExpShareRadiusSquared() {return expShareRadiusSq;}

    /**
     * @return the value for the member experience modifier
     */
    public double getMemberModifier() {
        return memberModifier;
    }

    /**
     * @return the value for the level experience modifier
     */
    public double getLevelModifier() {
        return levelModifier;
    }

    /**
     * whether debug messages are enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Retrieves teh party the player is in
     *
     * @param player player to get for
     * @return joined party
     */
    public Party getJoinedParty(OfflinePlayer player) {
        for (Party party : parties) {
            if (party.isMember(player)) {
                return party;
            }
        }
        return null;
    }

    /**
     * Retrieves the party that the player is in or is invited to
     *
     * @param player player to check
     * @return party the player is in or null if not found
     */
    public Party getParty(Player player) {
        return parties.stream()
                .filter(party -> party.isMember(player) || party.isInvited(player))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return an unmodifiable list containing all the existing parties
     */
    public List<Party> getParties() {
        return partiesRead;
    }

    /**
     * Adds a new party to the list
     *
     * @param party party to add
     */
    public void addParty(Party party) {
        parties.add(party);
    }

    /**
     * Removes a party from the list
     *
     * @param party party to remove
     */
    public void removeParty(Party party) {
        parties.remove(party);
    }

    /**
     * Updates invitations for parties
     */
    public void update() {
        for (Party party : parties) {
            party.checkInvitations();
        }
    }

    /**
     * Checks if the player is toggled
     *
     * @param playerName name of the player to check
     * @return true if toggled, false otherwise
     */
    public boolean isToggled(String playerName) {
        return toggled.contains(playerName.toLowerCase());
    }

    /**
     * Toggles the player's party chat
     *
     * @param playerName name of player to toggle
     */
    public void toggle(String playerName) {
        if (isToggled(playerName)) {
            toggled.remove(playerName.toLowerCase());
        } else {
            toggled.add(playerName.toLowerCase());
        }
    }

    /**
     * <p>Gets the message at the given key in the language configuration.</p>
     *
     * <p>Colors are applied and the message returned contains a list of all
     * lines from the configuration. If it was just a single value, there will
     * be only one element. If it was a string list, it will contain each line.</p>
     *
     * @param key     language key
     * @param player  whether the message is for a player
     * @param filters filters to apply
     * @return list of message lines
     */
    public List<String> getMessage(String key, boolean player, CustomFilter... filters) {
        return language.getMessage(key, player, FilterType.COLOR, filters);
    }

    /**
     * <p>Sends a message to the target based on the message at the given
     * language key.</p>
     *
     * @param target  recipient of the message
     * @param key     message key
     * @param filters filters to use
     */
    public void sendMessage(Player target, String key, CustomFilter... filters) {
        language.sendMessage(key, target, FilterType.COLOR, filters);
    }

    @Override
    public void registerSkills(Fabled api) {
    }

    @Override
    public void registerClasses(Fabled api) {
    }

    @Override
    public List<CustomEffectComponent> getComponents() {
        return List.of(new PartyTarget());
    }
}
