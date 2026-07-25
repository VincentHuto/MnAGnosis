package com.vincenthuto.mnagnosis.client.truth;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.entity.TruthEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public final class TruthSceneController {

    private static final ResourceLocation GRAYSCALE_EFFECT =
            MnAGnosis.rloc("shaders/post/truth_grayscale.json");

    private static boolean serverActive;
    private static TruthGiggleSound giggle;
    private static TruthAmbientSound ambient;
    private static ResourceLocation previousEffect;

    private TruthSceneController() {
    }

    public static void setServerActive(boolean active) {
        serverActive = active;
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null) {
            reset(minecraft);
            return;
        }

        boolean shouldBeActive = serverActive || hasVisibleOwnedTruth(
                minecraft, minecraft.player.getUUID()
        );
        if (shouldBeActive) {
            activate(minecraft);
        } else {
            deactivate(minecraft);
        }
    }

    public static void reset(Minecraft minecraft) {
        serverActive = false;
        deactivate(minecraft);
    }

    private static boolean hasVisibleOwnedTruth(Minecraft minecraft, UUID playerId) {
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof TruthEntity truth
                    && !truth.isRemoved()
                    && truth.getOwnerId().filter(playerId::equals).isPresent()) {
                return true;
            }
        }
        return false;
    }

    private static void activate(Minecraft minecraft) {
        if (giggle == null || giggle.isStopped()) {
            giggle = new TruthGiggleSound();
            minecraft.getSoundManager().play(giggle);
        }
        if (ambient == null || ambient.isStopped()) {
            ambient = new TruthAmbientSound();
            minecraft.getSoundManager().play(ambient);
        }

        PostChain current = minecraft.gameRenderer.currentEffect();
        if (isTruthEffect(current)) {
            return;
        }
        previousEffect = current == null ? null : ResourceLocation.tryParse(current.getName());
        minecraft.gameRenderer.loadEffect(GRAYSCALE_EFFECT);
        if (!isTruthEffect(minecraft.gameRenderer.currentEffect()) && previousEffect != null) {
            minecraft.gameRenderer.loadEffect(previousEffect);
            previousEffect = null;
        }
    }

    private static void deactivate(Minecraft minecraft) {
        if (giggle != null) {
            giggle.finish();
            giggle = null;
        }
        if (ambient != null) {
            ambient.finish();
            ambient = null;
        }

        if (!isTruthEffect(minecraft.gameRenderer.currentEffect())) {
            previousEffect = null;
            return;
        }
        if (previousEffect != null) {
            ResourceLocation restore = previousEffect;
            previousEffect = null;
            minecraft.gameRenderer.loadEffect(restore);
        } else {
            minecraft.gameRenderer.shutdownEffect();
        }
    }

    private static boolean isTruthEffect(PostChain effect) {
        return effect != null && GRAYSCALE_EFFECT.toString().equals(effect.getName());
    }
}
