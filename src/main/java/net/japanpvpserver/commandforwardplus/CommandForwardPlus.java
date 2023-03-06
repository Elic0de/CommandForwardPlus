package net.japanpvpserver.commandforwardplus;

import co.aikar.commands.BungeeCommandManager;
import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.plugin.Plugin;
import net.william278.annotaml.Annotaml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class CommandForwardPlus extends Plugin {

    @Getter
    private ForwardSettings settings;

    @Override
    public void onEnable() {
        // Plugin startup logic
        loadConfig();
        registerCommand();
    }

    private void loadConfig() throws RuntimeException {
        try {
            this.settings = Annotaml.create(new File(getDataFolder(), "config.yml"), ForwardSettings.class).get();
        } catch (IOException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            getLogger().log(Level.SEVERE, "Failed to load configuration files", e);
            throw new RuntimeException(e);
        }
    }

    private void registerCommand() {
        BungeeCommandManager commandManager = new BungeeCommandManager(this);

        commandManager.getCommandCompletions().registerCompletion("target", context -> {
            final Set<String> completions = new HashSet<>();

            completions.add("@a");
            completions.add("@s");
            completions.add("@p");
            completions.addAll(getProxy().getPlayers().stream().map(CommandSender::getName).collect(Collectors.toList()));
            completions.addAll(getProxy().getServersCopy().keySet().stream().map(s -> "@" + s).collect(Collectors.toList()));

            return completions;
        });

        commandManager.getCommandCompletions().registerCompletion("cmd", context -> getProxy().getPluginManager().getCommands()
                .stream()
                .filter(entry -> !settings.getIgnoreCommands().contains(entry.getKey()))
                .map(Map.Entry::getKey).collect(Collectors.toList()));

        commandManager.registerCommand(new ForwardCommand(this));
    }

    public Collection<ProxiedPlayer> getOwnServerPlayers(ProxiedPlayer player) {
        return player.getServer().getInfo().getPlayers();
    }

    public Collection<ProxiedPlayer> getBungeeOnlinePlayers() {
        return getProxy().getPlayers();
    }

    public Collection<ProxiedPlayer> getServerPlayers(String serverName) {
        final ServerInfo server = getProxy().getServerInfo(serverName);
        if (server == null) return new ArrayList<>();
        return server.getPlayers();
    }
}
