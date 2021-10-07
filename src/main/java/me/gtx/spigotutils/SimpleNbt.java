package me.gtx.spigotutils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"ConstantConditions", "SameParameterValue"})
public class SimpleNbt {

    private static final Class<?> NBT_COMPOUND = getNmsClass("NBTTagCompound");
    private static final Constructor<?> NBT_CONSTRUCTOR = getConstructor(NBT_COMPOUND);
    private static final Class<?> NMS_STACK = getNmsClass("ItemStack");
    private static final Class<?> CRAFT_STACK = getBukkitClass("inventory.CraftItemStack");
    private static final Field STACK_HANDLE = getField(CRAFT_STACK, "handle");
    private static final Method HAS_TAG = getMethod(NMS_STACK, "hasTag");
    private static final Method GET_TAG = getMethod(NMS_STACK, "getTag");
    private static final Method SET_TAG = getMethod(NMS_STACK, "setTag", NBT_COMPOUND);

    private static final Method HAS_KEY = getMethod(NBT_COMPOUND, "hasKey", String.class);

    private static final Map<Class<?>, Method> NBT_GET_METHODS;
    private static final Map<Class<?>, Method> NBT_SET_METHODS;

    static {
        NBT_GET_METHODS = new HashMap<>();
        NBT_SET_METHODS = new HashMap<>();

        for(Method method : NBT_COMPOUND.getDeclaredMethods()) {
            if(method.getParameterTypes().length > 0) {
                if(method.getParameterTypes()[0] == String.class) {
                    if(method.getParameterTypes().length == 2
                            && method.getReturnType() == void.class
                            && method.getName().startsWith("set")) {
                        NBT_SET_METHODS.put(method.getParameterTypes()[1], method);
                    } else if(method.getParameterTypes().length == 1
                            && method.getName().startsWith("get")) {
                        NBT_GET_METHODS.put(method.getReturnType(), method);
                    }
                }
            }
        }
    }

    private static void write(ItemStack stack, Class<?> type, String key, Object value) {
        try {
            Object nmsStack = STACK_HANDLE.get(stack);
            boolean hasTag = (boolean) HAS_TAG.invoke(nmsStack);
            Object tagCompound = hasTag ? GET_TAG.invoke(nmsStack) : NBT_CONSTRUCTOR.newInstance();
            Method method = NBT_SET_METHODS.get(type);
            if(method != null) {
                method.invoke(tagCompound, key, value);
                SET_TAG.invoke(nmsStack, tagCompound);
            } else {
                throw new RuntimeException("Unsupported type, check " + NBT_COMPOUND.getName() + " for supported types");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object read(ItemStack stack, Class<?> type, String key) {
        try {
            Object nmsStack = STACK_HANDLE.get(stack);
            boolean hasTag = (boolean) HAS_TAG.invoke(nmsStack);
            if(hasTag) {
                Object tagCompound = GET_TAG.invoke(nmsStack);
                Method method = NBT_GET_METHODS.get(type);
                if(method != null) {
                    return method.invoke(tagCompound, key);
                } else {
                    throw new RuntimeException("Unsupported type, check " + NBT_COMPOUND.getName() + " for supported types");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean hasKey(ItemStack stack, String key) {
        try {
            Object nmsStack = STACK_HANDLE.get(stack);
            boolean hasTag = (boolean) HAS_TAG.invoke(nmsStack);
            if(hasTag) {
                Object tagCompound = GET_TAG.invoke(nmsStack);
                return (boolean) HAS_KEY.invoke(tagCompound, key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void writeString(ItemStack stack, String key, String value) {
        write(stack, String.class, key, value);
    }

    public static void writeInt(ItemStack stack, String key, int value) {
        write(stack, int.class, key, value);
    }

    public static void writeFloat(ItemStack stack, String key, float value) {
        write(stack, float.class, key, value);
    }

    public static void writeDouble(ItemStack stack, String key, double value) {
        write(stack, double.class, key, value);
    }

    public static void writeBoolean(ItemStack stack, String key, boolean value) {
        write(stack, boolean.class, key, value);
    }

    public static void writeShort(ItemStack stack, String key, short value) {
        write(stack, short.class, key, value);
    }

    public static void writeLong(ItemStack stack, String key, long value) {
        write(stack, long.class, key, value);
    }

    public static void writeByteArray(ItemStack stack, String key, byte[] value) {
        write(stack, byte[].class, key, value);
    }

    public static void writeIntArray(ItemStack stack, String key, int[] value) {
        write(stack, int[].class, key, value);
    }

    public static String readString(ItemStack stack, String key) {
        return (String) read(stack, String.class, key);
    }

    public static int readInt(ItemStack stack, String key) {
        return (int) read(stack, int.class, key);
    }

    public static float readFloat(ItemStack stack, String key) {
        return (float) read(stack, float.class, key);
    }

    public static double readDouble(ItemStack stack, String key) {
        return (double) read(stack, double.class, key);
    }

    public static boolean readBoolean(ItemStack stack, String key) {
        return (boolean) read(stack, boolean.class, key);
    }

    public static short readShort(ItemStack stack, String key) {
        return (short) read(stack, short.class, key);
    }

    public static long readLong(ItemStack stack, String key) {
        return (long) read(stack, long.class, key);
    }

    public static byte[] readByteArray(ItemStack stack, String key) {
        return (byte[]) read(stack, byte[].class, key);
    }

    public static int[] readIntArray(ItemStack stack, String key) {
        return (int[]) read(stack, int[].class, key);
    }

    private static String getNmsVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    private static Class<?> getBukkitClass(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + getNmsVersion() + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Class<?> getNmsClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + getNmsVersion() + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Field getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

}
