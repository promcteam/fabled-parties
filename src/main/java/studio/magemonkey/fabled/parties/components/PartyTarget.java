package studio.magemonkey.fabled.parties.components;

import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import studio.magemonkey.codex.mccore.config.parse.DataSection;
import studio.magemonkey.fabled.Fabled;
import studio.magemonkey.fabled.api.target.TargetHelper;
import studio.magemonkey.fabled.dynamic.ComponentType;
import studio.magemonkey.fabled.dynamic.DynamicSkill;
import studio.magemonkey.fabled.dynamic.TempEntity;
import studio.magemonkey.fabled.dynamic.custom.CustomEffectComponent;
import studio.magemonkey.fabled.dynamic.custom.EditorOption;
import studio.magemonkey.fabled.dynamic.target.NearestTarget;
import studio.magemonkey.fabled.dynamic.target.TargetComponent;
import studio.magemonkey.fabled.listener.MechanicListener;
import studio.magemonkey.fabled.parties.FabledParties;
import studio.magemonkey.fabled.parties.Party;

import java.util.*;
import java.util.function.Function;

/**
 * Applies child components to all the player's party members.
 * This requires that FabledParties be installed.
 */
public class PartyTarget extends CustomEffectComponent {
    private static final   String WALL         = "wall";
    private static final   String CASTER       = "caster";
    protected static final String MAX          = "max";
    private static final   String INVULNERABLE = "invulnerable";

    private boolean                       throughWall;
    private boolean                       invulnerable;
    private TargetComponent.IncludeCaster self;

    @Override
    public String getKey() {
        return "party";
    }

    @Override
    public ComponentType getType() {
        return ComponentType.TARGET;
    }

    @Override
    public String getDescription() {
        return "Gets the party members of the targeted entity and applies the child components to them";
    }

    @Override
    public List<EditorOption> getOptions() {
        // TODO Fill this in
        return List.of();
    }

    @Override
    public void load(DynamicSkill skill, DataSection config) {
        super.load(skill, config);

        throughWall = settings.getString(WALL, "false").equalsIgnoreCase("true");
        invulnerable = settings.getString(INVULNERABLE, "false").equalsIgnoreCase("true");
        self = TargetComponent.IncludeCaster.valueOf(settings.getString(CASTER, "false")
                .toUpperCase(Locale.US)
                .replace(' ', '_'));
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets, boolean force) {
        final List<LivingEntity> list = getTargets(caster, level, targets);
        DynamicSkill.getCastData(caster).put("api-num-targets", list.size());
        return (!list.isEmpty() && executeChildren(caster, level, list, force));
    }

    private List<LivingEntity> getTargets(final LivingEntity caster,
                                          final int level,
                                          final List<LivingEntity> targets) {
        final double range = parseValues(caster, "range", level, 3.0);

        return determineTargets(caster, level, targets, t -> getOnlinePartyMembers(t, range));
    }

    private List<LivingEntity> getOnlinePartyMembers(LivingEntity entity, double range) {
        if (!(entity instanceof Player)) return List.of();

        FabledParties plugin = FabledParties.inst();
        Party         party  = plugin.getParty((Player) entity);
        if (party == null) return List.of();

        final Comparator<LivingEntity> comparator = new NearestTarget.DistanceComparator(entity.getLocation());

        List<LivingEntity> list   = new ArrayList<>();
        Server             server = plugin.getServer();
        for (UUID uuid : party.getMembers()) {
            Player p = server.getPlayer(uuid);
            if (p == null || !p.isOnline()) continue;

            if (p.getLocation().distanceSquared(entity.getLocation()) <= range * range) {
                list.add(p);
            }
        }

        list.sort(comparator);
        return list;
    }

    // The following two methods were copied from TargetComponent
    private List<LivingEntity> determineTargets(final LivingEntity caster,
                                                final int level,
                                                final List<LivingEntity> from,
                                                final Function<LivingEntity, List<LivingEntity>> conversion) {

        final double max = parseValues(caster, MAX, level, 99);

        final List<LivingEntity> list = new ArrayList<>();
        from.forEach(target -> {
            final List<LivingEntity> found = conversion.apply(target);
            int                      count = 0;
            for (LivingEntity entity : found) {
                if (count >= max) break;
                if (isValidTarget(caster, target, entity) || (self.equals(TargetComponent.IncludeCaster.IN_AREA)
                        && caster == entity)) {
                    list.add(entity);
                    count++;
                }
            }
        });
        if (self.equals(TargetComponent.IncludeCaster.TRUE)) list.add(caster);
        return list;
    }

    private boolean isValidTarget(final LivingEntity caster, final LivingEntity from, final LivingEntity target) {
        if (Fabled.getMeta(target, MechanicListener.ARMOR_STAND) != null) return false;
        if (target instanceof TempEntity) return true;
        if (target.isInvulnerable() && !invulnerable) return false;
        if (target instanceof Player && (((Player) target).getGameMode() == GameMode.SPECTATOR
                || ((Player) target).getGameMode() == GameMode.CREATIVE)) return false;

        return target != caster && Fabled.getSettings().isValidTarget(target) && (throughWall
                || !TargetHelper.isObstructed(from.getEyeLocation(), target.getEyeLocation()));
    }

}
