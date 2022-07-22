package net.kerfu.volt;

import lombok.Getter;
import net.kerfu.volt.loader.Loader;
import net.kerfu.volt.utils.FileConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class Volt extends JavaPlugin {

    @Getter private static Volt instance;

    private Loader loader;
    private FileConfig licenseConfig;

    @Override
    public void onEnable() {
        instance = this;

        boolean contains = System.getProperty("os.arch").contains("64");
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("lin") && contains) {
            this.loader = new Loader();
            this.licenseConfig = new FileConfig(this, "licenses.yml");

            loader.start();
        } else {
            System.out.println("==============================================");
            System.out.println("OS INCOMPATIBLE");
            System.out.println(" ");
            System.out.println("Contact to the developer.");
            System.out.println("==============================================");
            Bukkit.shutdown();
        }
    }

    @Override
    public void onDisable() {
        loader.shutdown();
    }
}
