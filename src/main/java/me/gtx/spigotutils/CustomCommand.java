package me.gtx.spigotutils;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

@Getter
public class CustomCommand extends Command {

    private final String name, permission, permissionMessage, description, usage;
    private final List<String> aliases;

    protected CustomCommand(String name, String permission, String permissionMessage, String description, String usage, String... aliases) {
        super(name, description, usage, Arrays.asList(aliases));

        this.name = name;
        this.permission = permission;
        this.permissionMessage = permissionMessage;
        this.description = description;
        this.usage = usage;
        this.aliases = Arrays.asList(aliases);

        this.setPermission(permission);
        this.setPermissionMessage(permissionMessage);
    }

    public boolean onCommand(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if(this.testPermission(sender) && !this.onCommand(sender, args)) {
            sender.sendMessage(this.getUsage());
        }
        return true;
    }

    public void register() {
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName();
            Class<?> clazz = Class.forName("org.bukkit.craftbukkit." + version.substring(version.lastIndexOf('.') + 1) + ".CraftServer");
            Object craftServer = clazz.cast(Bukkit.getServer());
            Object map = craftServer.getClass().getDeclaredMethod("getCommandMap").invoke(craftServer);
            map.getClass().getDeclaredMethod("register", String.class, Command.class).invoke(map, this.getName(), this);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }

}
