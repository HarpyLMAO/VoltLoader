package net.kerfu.volt.plugin;

import lombok.Getter;
import lombok.SneakyThrows;
import net.kerfu.volt.Volt;
import net.kerfu.volt.access.FieldAccess;
import net.kerfu.volt.access.Instance;
import net.kerfu.volt.loader.Loader;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Getter
@SuppressWarnings("ResultOfMethodCallIgnored")
public class VoltPlugin {

    private final File plugin;
    private File root;
    private Plugin handler;

    public VoltPlugin(File plugin) {
        this.plugin = plugin;

        File[] listFiles = this.plugin.getParentFile().listFiles();
        assert listFiles != null;

        Arrays.stream(listFiles).forEach(file -> {
            String fileName = file.getName();
            if (fileName.startsWith(Loader.getPrefix()) && fileName.endsWith(".jar") && !file.equals(this.plugin)) {
                file.delete();
            }
        });
    }

    public void delete() {
        plugin.delete();
    }

    @SneakyThrows
    public void enable(Plugin parent) {
        this.handler = parent.getPluginLoader().loadPlugin(plugin);
        this.fixAccess(parent);
        this.checkFields(parent);
        this.fixConfiguration();
        parent.getServer().getPluginManager().enablePlugin(handler);
    }

    @SneakyThrows
    public void overwrite(String pluginName) {
        List<String> lines = Collections.singletonList(UUID.randomUUID().toString());
        Path file = Paths.get(String.valueOf(Volt.getInstance().getLoader().getVoltedPlugins().get(pluginName).getPlugin()));
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    public void disable() {
        this.unload();
        if (plugin.exists()) plugin.delete();
    }

    public void unload() {
        String name = handler.getName();
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.disablePlugin(handler);

        List<Plugin> plugins = new FieldAccess(pluginManager.getClass(), "plugins").read(pluginManager);
        Map<String, Plugin> names = new FieldAccess(pluginManager.getClass(), "lookupNames").read(pluginManager);
        SimpleCommandMap commandMap = new FieldAccess(pluginManager.getClass(), "commandMap").read(pluginManager);
        Map<String, Command> commands = new FieldAccess(SimpleCommandMap.class, "knownCommands").read(commandMap);

        pluginManager.disablePlugin(handler);

        if (plugins != null) plugins.remove(handler);
        if (name != null) names.remove(name);

        if (commandMap != null) {
            for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Command> entry = it.next();
                if (entry.getValue() instanceof PluginCommand) {
                    PluginCommand command = (PluginCommand) entry.getValue();
                    if (command.getPlugin() == handler) {
                        command.unregister(commandMap);
                        it.remove();
                    }
                }
            }
        }

        ClassLoader classLoader = handler.getClass().getClassLoader();;
        if (classLoader instanceof URLClassLoader) {
            new FieldAccess(classLoader.getClass(), "plugin").set(classLoader, null);
            this.closeLoader((URLClassLoader) classLoader);
        }
    }

    @SneakyThrows
    private void checkFields(Plugin parent) {
        Arrays.stream(handler.getClass().getFields()).filter(field -> field.isAnnotationPresent(Instance.class)).forEach(field -> {
            try {
                field.setAccessible(true);
                field.set(handler, parent);
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            }
        });
        Arrays.stream(handler.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(Instance.class)).forEach(field -> {
            try {
                field.setAccessible(true);
                field.set(handler, parent);
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            }
        });
    }

    @SneakyThrows
    private void fixConfiguration() {
        File file;
        //for (File files : Objects.requireNonNull(handler.getDataFolder().listFiles())) {
            new FieldAccess(JavaPlugin.class, "configFile").set(handler, (file = new File(root, "config.yml")));
            new FieldAccess(JavaPlugin.class, "newConfig").set(handler, YamlConfiguration.loadConfiguration(file));
        //}
    }

    @SneakyThrows
    public void fixAccess(Plugin parent) {
        new FieldAccess(JavaPlugin.class, "dataFolder").set(handler, (root = new File(parent.getDataFolder().getParentFile(), handler.getDescription().getName().replaceAll(" ", "_"))));
    }

    @SneakyThrows
    public void closeLoader(URLClassLoader classLoader) {
        classLoader.close();
    }
}
