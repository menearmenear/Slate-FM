package menear.nclient.slate.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

public final class ItemStackModelUtil {

    public static List<Component> getLore(ItemStack stack) {
        List<Component> lore = new ArrayList<>();
        CompoundTag display = stack.getTagElement("display");
        if (display == null) {
            return lore;
        }

        ListTag loreTag = display.getList("Lore", Tag.TAG_STRING);
        for (int i = 0; i < loreTag.size(); i++) {
            try {
                Component component = Component.Serializer.fromJson(loreTag.getString(i));
                if (component != null) {
                    lore.add(component);
                }
            } catch (Exception ignored) {
            }
        }
        return lore;
    }

    public static GameProfile getSkullProfile(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("SkullOwner", Tag.TAG_COMPOUND)) {
            return NbtUtils.readGameProfile(tag.getCompound("SkullOwner"));
        }
        return null;
    }

    public static String getSkullName(GameProfile profile) {
        if (profile != null && profile.getName() != null && !profile.getName().trim().isEmpty()) {
            return profile.getName();
        }
        return "Skull";
    }

    public static String getSkullTextureUrl(GameProfile profile) {
        if (profile == null) {
            return null;
        }

        PropertyMap properties = profile.getProperties();
        Collection<Property> textures = properties.get("textures");
        if (textures == null || textures.isEmpty()) {
            return null;
        }

        String value = textures.iterator().next().getValue();
        if (value == null || value.isEmpty()) {
            return null;
        }

        String decodedJson;
        try {
            decodedJson = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        // Base64-decoded texture payload is JSON: {"textures":{"SKIN":{"url":"https://textures.minecraft.net/texture/<hash>",...}}}
        JsonElement root = JsonParser.parseString(decodedJson);
        if (root.isJsonObject()) {
            JsonObject textureObject = root.getAsJsonObject().getAsJsonObject("textures");
            if (textureObject != null) {
                JsonObject skinObject = textureObject.getAsJsonObject("SKIN");
                if (skinObject != null && skinObject.has("url")) {
                    return skinObject.get("url").getAsString();
                }
            }
        }

        int textureUrlIndex = decodedJson.indexOf("textures.minecraft.net/texture/");
        if (textureUrlIndex != -1) {
            int idStart = textureUrlIndex + "textures.minecraft.net/texture/".length();
            int idEnd = decodedJson.indexOf("\"", idStart);
            if (idEnd != -1 && idStart < idEnd) {
                return "https://textures.minecraft.net/texture/" + decodedJson.substring(idStart, idEnd);
            }
        }
        return null;
    }
}