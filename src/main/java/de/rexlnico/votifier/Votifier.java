/*
 * Copyright (C) 2012 Vex Software LLC
 * This file is part of Votifier.
 *
 * Votifier is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Votifier is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Votifier.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rexlnico.votifier;

import java.io.*;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.*;

import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import de.rexlcloud.core.config.JsonConfiguration;
import de.rexlcloud.core.database.DatabaseConnection;
import de.rexlcloud.core.database.DatabaseCredentials;
import de.rexlnico.votifier.commands.VoteCommands;
import de.rexlnico.votifier.config.Config;
import de.rexlnico.votifier.config.Messages;
import de.rexlnico.votifier.crypto.RSAIO;
import de.rexlnico.votifier.crypto.RSAKeygen;
import de.rexlnico.votifier.model.ListenerLoader;
import de.rexlnico.votifier.model.VoteListener;
import de.rexlnico.votifier.net.VoteReceiver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main Votifier plugin class.
 *
 * @author Blake Beaupain
 * @author Kramer Campbell
 */
public class Votifier extends JavaPlugin {

    /**
     * The logger instance.
     */
    private static final Logger LOG = Logger.getLogger("Votifier");

    /**
     * Log entry prefix
     */
    private static final String logPrefix = "[Votifier] ";

    /**
     * The Votifier instance.
     */
    private static Votifier instance;
    private static PaperCommandManager<CommandSender> commandManager;

    public Messages messages;
    public Config voteConfig;
    public DatabaseConnection databaseConnection;

    /**
     * The current Votifier version.
     */
    private String version;

    /**
     * The vote listeners.
     */
    private final List<VoteListener> listeners = new ArrayList<VoteListener>();

    /**
     * The vote receiver.
     */
    private VoteReceiver voteReceiver;

    /**
     * The RSA key pair.
     */
    private KeyPair keyPair;

    /**
     * Debug mode flag
     */
    private boolean debug;

    /**
     * Attach custom log filter to logger.
     */
    static {
        LOG.setFilter(new LogFilter(logPrefix));
    }

    @Override
    public void onEnable() {
        Votifier.instance = this;

        // Set the plugin version.
        version = getDescription().getVersion();

        // Handle configuration.
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File config = new File(getDataFolder() + "/config.yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(config);
        File rsaDirectory = new File(getDataFolder() + "/rsa");
        // Replace to remove a bug with Windows paths - SmilingDevil
        String listenerDirectory = getDataFolder().toString()
                .replace("\\", "/") + "/listeners";

        Path path = getDataFolder().toPath();
        messages = JsonConfiguration.loadOrCreateConfiguration(path.resolve("messages.json"), new Messages());
        voteConfig = JsonConfiguration.loadOrCreateConfiguration(path.resolve("config.json"), new Config());

        Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction = AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().build();
        try {
            commandManager = PaperCommandManager.createNative(this, executionCoordinatorFunction);
        } catch (Exception e) {
            e.printStackTrace();
        }

        AnnotationParser<CommandSender> annotationParser = new AnnotationParser<>(commandManager, CommandSender.class, args -> SimpleCommandMeta.empty());
        annotationParser.parse(new VoteCommands());

        databaseConnection = new DatabaseConnection(JsonConfiguration.loadOrCreateConfiguration(path.resolve("database.json"), new DatabaseCredentials()));
        databaseConnection.open();
        databaseConnection.execute(Const.CREATE_VOTE_TABLE);

        /*
         * Use IP address from server.properties as a default for
         * configurations. Do not use InetAddress.getLocalHost() as it most
         * likely will return the main server address instead of the address
         * assigned to the server.
         */
        String hostAddr = Bukkit.getServer().getIp();
        if (hostAddr == null || hostAddr.length() == 0)
            hostAddr = "0.0.0.0";

        /*
         * Create configuration file if it does not exists; otherwise, load it
         */
        if (!config.exists()) {
            try {
                // First time run - do some initialization.
                LOG.info("Configuring Votifier for the first time...");

                // Initialize the configuration file.
                config.createNewFile();

                cfg.set("host", hostAddr);
                cfg.set("port", 8192);
                cfg.set("debug", false);

                /*
                 * Remind hosted server admins to be sure they have the right
                 * port number.
                 */
                LOG.info("------------------------------------------------------------------------------");
                LOG.info("Assigning Votifier to listen on port 8192. If you are hosting Craftbukkit on a");
                LOG.info("shared server please check with your hosting provider to verify that this port");
                LOG.info("is available for your use. Chances are that your hosting provider will assign");
                LOG.info("a different port, which you need to specify in config.yml");
                LOG.info("------------------------------------------------------------------------------");

                cfg.set("listener_folder", listenerDirectory);
                cfg.save(config);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error creating configuration file", ex);
                gracefulExit();
                return;
            }
        } else {
            // Load configuration.
            cfg = YamlConfiguration.loadConfiguration(config);
        }

        /*
         * Create RSA directory and keys if it does not exist; otherwise, read
         * keys.
         */
        try {
            if (!rsaDirectory.exists()) {
                rsaDirectory.mkdir();
                new File(listenerDirectory).mkdir();
                keyPair = RSAKeygen.generate(2048);
                RSAIO.save(rsaDirectory, keyPair);
            } else {
                keyPair = RSAIO.load(rsaDirectory);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE,
                    "Error reading configuration file or RSA keys", ex);
            gracefulExit();
            return;
        }

        // Load the vote listeners.
        listenerDirectory = cfg.getString("listener_folder");
        listeners.addAll(ListenerLoader.load(listenerDirectory));

        // Initialize the receiver.
        String host = cfg.getString("host", hostAddr);
        int port = cfg.getInt("port", 8192);
        debug = cfg.getBoolean("debug", false);
        if (debug)
            LOG.info("DEBUG mode enabled!");

        try {
            voteReceiver = new VoteReceiver(this, host, port);
            voteReceiver.start();

            LOG.info("Votifier enabled.");
        } catch (Exception ex) {
            gracefulExit();
            return;
        }
    }

    @Override
    public void onDisable() {
        // Interrupt the vote receiver.
        if (voteReceiver != null) {
            voteReceiver.shutdown();
        }
        LOG.info("Votifier disabled.");
    }

    private void gracefulExit() {
        LOG.log(Level.SEVERE, "Votifier did not initialize properly!");
    }

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static Votifier getInstance() {
        return instance;
    }

    /**
     * Gets the version.
     *
     * @return The version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the listeners.
     *
     * @return The listeners
     */
    public List<VoteListener> getListeners() {
        return listeners;
    }

    /**
     * Gets the vote receiver.
     *
     * @return The vote receiver
     */
    public VoteReceiver getVoteReceiver() {
        return voteReceiver;
    }

    /**
     * Gets the keyPair.
     *
     * @return The keyPair
     */
    public KeyPair getKeyPair() {
        return keyPair;
    }

    public boolean isDebug() {
        return debug;
    }

}
