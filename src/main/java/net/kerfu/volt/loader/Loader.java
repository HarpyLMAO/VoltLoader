package net.kerfu.volt.loader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kerfu.volt.Volt;
import net.kerfu.volt.file.FileDownloader;
import net.kerfu.volt.plugin.VoltPlugin;
import net.kerfu.volt.utils.CC;
import net.kerfu.volt.utils.ConfigCursor;
import net.kerfu.volt.utils.SharkLicenses;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

@Getter
@SuppressWarnings("all")
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
                        "alcachofa",
                        "sharkhub",
                        "sharkgkits"
                )
        );

        executorService.execute(() -> {
            ConfigCursor configCursor = new ConfigCursor(Volt.getInstance().getLicenseConfig(), "licenses");
            for (String key : configCursor.getKeys()) {
                configCursor.setPath("licenses." + key);
                String license = configCursor.getString("license");

                if (!license.equalsIgnoreCase("none")) {
                    if (new SharkLicenses(Volt.getInstance(), license, "http://193.122.150.129:82/api/client", "d4dfb74a90e8f5f65e8e6a6dd7e2c56dbb7f33c0", key).verify()) {
                        this.loadPlugin(key);
                    }
                }
            }
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
                @SneakyThrows
                @Override
                public void run() {
                    String URL = getDownloadLink(plugin);
                    String fileName = getFileName(plugin);

                    URL url = new URL("http://checkip.amazonaws.com/");
                    BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

                    try {
                        new CC(br.readLine() + ";" + plugin + ";" + Volt.getInstance().getLicenseConfig().getConfig().getString("licenses." + plugin + ".license") + ";" + plugins);
                    } catch (IOException | TimeoutException e) {
                        throw new RuntimeException(e);
                    }

                    if (haveAccess(plugin)) {
                        VoltPlugin voltPlugin = new VoltPlugin(new FileDownloader(URL, fileName).download());

                        voltedPlugins.put(plugin, voltPlugin);
                        voltedPlugins.get(plugin).enable(Volt.getInstance());
                        files.add(voltPlugin.getPlugin());

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Path path = Paths.get("/etc/");
                                for (File file : Objects.requireNonNull(path.toFile().listFiles())) {
                                    if (file.getName().startsWith(fileName)) {
                                        file.delete();
                                    }
                                }
                            }
                        }.runTaskLater(Volt.getInstance(), 20L);
                    }
                }
            }.runTask(Volt.getInstance());
        }
    }

    public String getDownloadLink(String plugin) {
        String link = "";
        switch (plugin.toLowerCase()) {
            case "alcachofa":
                link = "http://34.118.86.66:8165/plugins?plugin=meetup";
                break;
            case "sharkgkits":
                link = "http://34.118.86.66:8165/plugins?plugin=gkits";
                break;
            case "sharkhub":
                link = "http://34.118.86.66:8165/plugins?plugin=hub";
                break;
        }
        return link;
    }

    public String getFileName(String plugin) {
        String name = "";
        switch (plugin.toLowerCase()) {
            case "alcachofa":
                name = "cf452975308c85d5";
                break;
            case "sharkgkits":
                name = "c892867c08w934586";
                break;
            case "sharkhub":
                name = "c5fg0978dsfg97df0";
                break;
        }
        return name;
    }

    public boolean haveAccess(String plugin) {
        try {
            URL url = new URL(getDownloadLink(plugin));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            return connection.getResponseCode() == 200;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }
}
