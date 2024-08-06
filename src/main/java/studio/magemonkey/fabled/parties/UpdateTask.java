package studio.magemonkey.fabled.parties;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Update task for partiestttttt
 */
public class UpdateTask extends BukkitRunnable {

    FabledParties plugin;

    /**
     * Constructor
     *
     * @param plugin plugin reference
     */
    public UpdateTask(FabledParties plugin) {
        this.plugin = plugin;
        runTaskTimer(plugin, 20, 20);
    }

    /**
     * Update the parties
     */
    @Override
    public void run() {
        plugin.update();
    }
}
