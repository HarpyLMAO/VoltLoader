package net.kerfu.volt.loader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import net.kerfu.volt.Volt;
import net.kerfu.volt.file.FileDownloader;
import net.kerfu.volt.plugin.VoltPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
public class Loader {

    @Getter
    private static final String prefix = "cf452975308c85d5";

    private ScheduledExecutorService executorService;

    private final List<String> plugins = Lists.newArrayList();
    private final List<File> files = Lists.newArrayList();

    private final Map<String, VoltPlugin> voltedPlugins = Maps.newHashMap();

    public void start() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();

        plugins.addAll(
                Arrays.asList(
                        "meetup",
                        "hub",
                        "gkits"
                )
        );

        executorService.execute(() -> {
            /*ConfigCursor configCursor = new ConfigCursor(Volt.getInstance().getLicenseConfig(), "licenses");
            for (String key : configCursor.getKeys()) {
                String license = configCursor.getString("licenses.key");

                List<String> validPlugins = Lists.newArrayList();
                if (!license.equalsIgnoreCase("none")) {
                    if (License.isvalid(license)) {
                        validPlugins.add(key);
                    }
                }

                validPlugins.forEach(this::loadPlugin);
            }*/
            this.loadPlugin("Alcachofa");
        });
    }

    public void shutdown() {
        voltedPlugins.forEach((p, b) -> {
            if (b != null) {
                b.disable();
                b.overwrite(p);
            }
        });
        executorService.shutdownNow();
    }

    public void reloadPlugin(String plugin) {
        if (voltedPlugins.containsKey(plugin)) {
            if (voltedPlugins.get(plugin) != null) {
                voltedPlugins.get(plugin).disable();
                loadPlugin(plugin);
            }
        }
    }

    public void disablePlugin(String plugin) {
        if (voltedPlugins.containsKey(plugin)) {
            voltedPlugins.get(plugin).disable();
        }
    }

    public void loadPlugin(String plugin) {
        if (!voltedPlugins.containsKey(plugin)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String URL = "https://kerfu.net/license/cf452975308c85d5.jar";

                    VoltPlugin voltPlugin = new VoltPlugin(new FileDownloader(URL).download());

                    voltedPlugins.put(plugin, voltPlugin);
                    voltedPlugins.get(plugin).enable(Volt.getInstance());
                    files.add(voltPlugin.getPlugin());
                }
            }.runTask(Volt.getInstance());
        }
    }
}
