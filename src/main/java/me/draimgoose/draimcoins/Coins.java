package me.draimgoose.draimcoins;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Coins extends PlaceholderExpansion {
    private File coinsHodler;
    private DCoins pl;
    public Coins(DCoins pl){
        this.pl = pl;
        coinsHodler = new File(pl.getDataFolder() + File.separator + "coinsHolder.yml");
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "draimcoins";
    }

    @Override
    public @NotNull String getAuthor() {
        return pl.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return pl.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier){
        if(player == null){
            return "";
        }
        // %example_placeholder1%
        if(identifier.equals("amount")){
            return pl.getTokens(player) + "";
        }

        // %example_placeholder2%


        // We return null if an invalid placeholder (f.e. %example_placeholder3%)
        // was provided
        return null;
    }
}
