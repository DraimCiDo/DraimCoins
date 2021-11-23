package me.draimgoose.draimcoins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public final class DCoins extends JavaPlugin implements CommandExecutor, Listener {

    File coinsHolder = new File(this.getDataFolder() + File.separator + "coinsHolder.yml");
    public Map<String, Integer> coins = new HashMap<>();
    public String prefix;
    public ArrayList<String> lang = new ArrayList<>();
    File records = new File(this.getDataFolder() + File.separator + "records");
    Date now = new Date();
    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat formatExact = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    File today = new File(records + File.separator + format.format(now) + ".txt");
    public ArrayList<String> transactions = new ArrayList<>();


    @Override
    public void onEnable() {
        Objects.requireNonNull(this.getCommand("coins")).setExecutor(this);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        if (!coinsHolder.exists()){
            try {
                coinsHolder.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!records.exists()){
            records.mkdir();
        }
        if (!today.exists()){
            try {
                today.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Scanner scanner = new Scanner(today);
                while (scanner.hasNextLine()){
                    String data = scanner.nextLine();
                    transactions.add(data);
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new Coins(this).register();
        }

        loadConfig();

        new BukkitRunnable(){
            @Override
            public void run() {
                try {
                    saveTokens();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(this,5000,5000);
    }

    public void loadConfig(){
        lang.clear();
        if (this.getConfig().getString("prefix") != null)
            prefix = color(this.getConfig().getString("prefix"));
        //0 balance
        lang.add(this.getConfig().getString("balance"));
        //1 admin-add
        lang.add(this.getConfig().getString("admin-add"));
        //2 player-receive
        lang.add(this.getConfig().getString("player-receive"));
        //3 admin-remove
        lang.add(this.getConfig().getString("admin-remove"));
        //4 player-take
        lang.add(this.getConfig().getString("player-take"));
        //5 unknown-command
        lang.add(this.getConfig().getString("unknown-command"));
        //6 unknown-player
        lang.add(this.getConfig().getString("unknown-player"));
    }

    public String color(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            saveTokens();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("coins")){
            if (sender.hasPermission("draimcoins.admin")){
                if (args.length == 0){
                    String text = lang.get(0);
                    if (text.contains("{coins}"))
                        text = text.replace("{coins}", getTokens((Player) sender) + "");
                    sender.sendMessage(prefix + color(text));
                } else if (args[0].equalsIgnoreCase("reload")){
                    loadConfig();
                    try {
                        saveTokens();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sender.sendMessage(prefix + ChatColor.GREEN + "Перезагрузка DraimCoins | Версия плагина" + this.getDescription().getVersion() + ".");
                } else if (args[0].equalsIgnoreCase("help")){
                    sender.sendMessage(prefix + ChatColor.GREEN + "Админ-команды:");
                    sender.sendMessage(ChatColor.YELLOW + "/coins help" + ChatColor.GOLD + " Показать эту информацию");
                    sender.sendMessage(ChatColor.YELLOW + "/coins reload" + ChatColor.GOLD + " Перезапустить плагин");
                    sender.sendMessage(ChatColor.YELLOW + "/coins give (игрок) (кол-во)" + ChatColor.GOLD + " Выдать игроку койны");
                    sender.sendMessage(ChatColor.YELLOW + "/coins remove (игрок) (кол-во)" + ChatColor.GOLD + " Убрать у игрока койны");
                    sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "----------------------------------------");
                } else if (args[0].equalsIgnoreCase("give")){
                    if (args.length >= 2){
                        Player p = Bukkit.getPlayer(args[1]);
                        if (p == null){
                            sender.sendMessage(prefix + color(lang.get(6)));
                        } else {
                            if (args.length >= 3){
                                int number = -1;
                                try
                                {
                                    number = Integer.parseInt(args[2]);
                                }
                                catch (NumberFormatException e)
                                {
                                    sender.sendMessage(prefix + ChatColor.GREEN + "Используйте: ");
                                    sender.sendMessage(ChatColor.YELLOW + "/coins give (игрок) (кол-во)" + ChatColor.GOLD + " Выдать игроку койны");
                                }
                                if (number != -1){
                                    addTokens(Objects.requireNonNull(p), number);
                                    transactions.add("[" +formatExact.format(now) + "] " + p.getName() + " получил " + number + " койнов от " + sender.getName() + ". Всего:" + getTokens(p));
                                    String text = lang.get(1);
                                    if (text.contains("{player}")){
                                        text =  text.replace("{player}", Objects.requireNonNull(Bukkit.getPlayer(args[1])).getName());
                                    }
                                    if (text.contains("{coins")){
                                        text = text.replace("{coins}", number + "");
                                    }
                                    sender.sendMessage(prefix + color(text));
                                    String text2 = lang.get(2);
                                    if (text2.contains("{coins")){
                                        text2 = text2.replace("{coins}", number + "");
                                    }
                                    Objects.requireNonNull(Bukkit.getPlayer(args[1])).sendMessage(prefix + color(text2));
                                }
                            } else {
                                sender.sendMessage(prefix + ChatColor.GREEN + "Используйте: ");
                                sender.sendMessage(ChatColor.YELLOW + "/coins give (игрок) (кол-во)" + ChatColor.GOLD + " Выдать игроку койны");
                            }
                        }
                    } else {
                        sender.sendMessage(prefix + ChatColor.GREEN + "Используйте:: ");
                        sender.sendMessage(ChatColor.YELLOW + "/coins give (игрок) (кол-во)" + ChatColor.GOLD + " Выдать игроку койны");
                    }
                } else if (args[0].equalsIgnoreCase("remove")){
                    if (args.length >= 2){
                        Player p = Bukkit.getPlayer(args[1]);
                        if (p == null){
                            sender.sendMessage(prefix + color(lang.get(6)));
                        } else {
                            if (args.length >= 3){
                                int number = -1;
                                try
                                {
                                    number = Integer.parseInt(args[2]);
                                }
                                catch (NumberFormatException e)
                                {
                                    sender.sendMessage(prefix + ChatColor.GREEN + "Используйте: ");
                                    sender.sendMessage(ChatColor.YELLOW + "/coins remove (игрок) (кол-во)" + ChatColor.GOLD + "Убрать у игрока койны");
                                }
                                if (number != -1){
                                    removeTokens(Objects.requireNonNull(Bukkit.getPlayer(args[1])), number);
                                    transactions.add("[" +formatExact.format(now) + "] " + p.getName() + " удалил " + number + " койнов у " + sender.getName() + ". Всего:" + getTokens(p));
                                    String text = lang.get(3);
                                    if (text.contains("{player}")){
                                        text =  text.replace("{player}", Objects.requireNonNull(Bukkit.getPlayer(args[1])).getName());
                                    }
                                    if (text.contains("{coins")){
                                        text = text.replace("{coins}", number + "");
                                    }
                                    sender.sendMessage(prefix + color(text));
                                    String text2 = lang.get(4);
                                    if (text2.contains("{coins")){
                                        text2 = text2.replace("{coins}", number + "");
                                    }
                                    Objects.requireNonNull(Bukkit.getPlayer(args[1])).sendMessage(prefix + color(text2));
                                }
                            } else {
                                sender.sendMessage(prefix + ChatColor.GREEN + "Используйте: ");
                                sender.sendMessage(ChatColor.YELLOW + "/coins remove (игрок) (кол-во)" + ChatColor.GOLD + " Убрать у игрока койны");
                            }
                        }
                    } else {
                        sender.sendMessage(prefix + ChatColor.GREEN + "Используйте: ");
                        sender.sendMessage(ChatColor.YELLOW + "/coins remove (игрок) (кол-во)" + ChatColor.GOLD + " Убрать у игрока койны");
                    }
                }
                else {
                    sender.sendMessage(prefix + color(lang.get(5)));
                }
            } else {
                String text = lang.get(0);
                if (text.contains("{coins}"))
                    text = text.replace("{coins}", getTokens((Player) sender) + "");
                sender.sendMessage(prefix + color(text));
            }
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("coins")) {
            if (sender.hasPermission("draimcoins.admin")) {
                List<String> autoComplete = new ArrayList<>();
                if (args.length == 1) {
                    autoComplete.add("reload");
                    autoComplete.add("help");
                    autoComplete.add("give");
                    autoComplete.add("remove");
                    return autoComplete;
                }
            }
        }
        return null;
    }

    public void addTokens(Player p, int amount){
        coins.replace(p.getUniqueId().toString(), coins.get(p.getUniqueId().toString()) + amount);
    }

    public void removeTokens(Player p, int amount){
        coins.replace(p.getUniqueId().toString(), coins.get(p.getUniqueId().toString()) - amount);
    }

    public int getTokens(Player p){
        return coins.get(p.getUniqueId().toString());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!coins.containsKey(event.getPlayer().getUniqueId().toString())){
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(coinsHolder);
            if (configuration.get(event.getPlayer().getUniqueId().toString()) == null){
                coins.put(event.getPlayer().getUniqueId().toString(), 0);
            } else {
                coins.put(event.getPlayer().getUniqueId().toString(), configuration.getInt(event.getPlayer().getUniqueId().toString()));
            }
        }
    }

    public void saveTokens() throws IOException {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(coinsHolder);
        for (Map.Entry<String, Integer> entry : coins.entrySet()) {
            configuration.set(entry.getKey(), entry.getValue());
        }
        configuration.save(coinsHolder);
        try{
            PrintWriter writer = new PrintWriter(today.getPath(), "UTF-8");
            for (String s : transactions){
                writer.println(s);
            }
            writer.close();
        } catch (IOException e) {
        }

    }
}
