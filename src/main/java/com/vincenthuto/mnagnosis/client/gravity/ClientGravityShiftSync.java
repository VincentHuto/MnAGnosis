package com.vincenthuto.mnagnosis.client.gravity;

import com.vincenthuto.mnagnosis.common.network.GravityShiftStatePacket;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityAnchorSnapshot;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftStateProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class ClientGravityShiftSync {

    private ClientGravityShiftSync() {
    }

    public static void accept(GravityShiftStatePacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(packet.entityId());
        if (entity == null) {
            return;
        }
        entity.getCapability(GravityShiftStateProvider.CAPABILITY).ifPresent(state ->
                {
                    if (packet.revision() < state.revision()) {
                        return;
                    }
                    GravityDirection previousDirection = state.direction();
                    state.applySnapshot(
                            packet.mode(),
                            packet.previousDirection(),
                            packet.direction(),
                            packet.transitionTicks(),
                            packet.releaseGraceTicks(),
                            packet.revision(),
                            packet.mobileTicks(),
                            new Vec3(
                                    packet.transitionOriginX(),
                                    packet.transitionOriginY(),
                                    packet.transitionOriginZ()
                            ),
                            new Quaternionf(
                                    packet.transitionOriginQX(),
                                    packet.transitionOriginQY(),
                                    packet.transitionOriginQZ(),
                                    packet.transitionOriginQW()
                            )
                    );
                    GravityAnchorSnapshot.apply(
                            entity,
                            new Vec3(
                                    packet.anchorX(),
                                    packet.anchorY(),
                                    packet.anchorZ()
                            ),
                            previousDirection != packet.direction()
                    );
                    entity.setDeltaMovement(
                            packet.velocityX(),
                            packet.velocityY(),
                            packet.velocityZ()
                    );
                }
        );
    }
}
