package com.vincenthuto.mnagnosis.common.authorship.law;

import com.mna.api.spells.base.ISpellDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class SpellFingerprint {

    private SpellFingerprint() {
    }

    public static String of(ISpellDefinition spell) {
        CompoundTag serialized = new CompoundTag();
        spell.writeToNBT(serialized);
        return ofTag(serialized);
    }

    public static String ofTag(CompoundTag serialized) {
        StringBuilder canonical = new StringBuilder();
        appendCanonical(serialized, canonical);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static void appendCanonical(Tag tag, StringBuilder output) {
        output.append(tag.getId()).append(':');
        if (tag instanceof CompoundTag compound) {
            output.append('{');
            compound.getAllKeys().stream().sorted().forEach(key -> {
                output.append(key.length()).append(':').append(key);
                appendCanonical(compound.get(key), output);
            });
            output.append('}');
        } else if (tag instanceof ListTag list) {
            output.append('[').append(list.size()).append(':');
            for (Tag element : list) {
                appendCanonical(element, output);
            }
            output.append(']');
        } else {
            String value = tag.getAsString();
            output.append(value.length()).append(':').append(value);
        }
    }
}
