package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import com.vincenthuto.mnagnosis.common.spell.gravity.GravityLensMath;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class GravityMirageMath {

    private GravityMirageMath() {
    }

    public static Vector3f projectToNdc(
            Vec3 worldPosition,
            Vec3 cameraPosition,
            Vector3f forward,
            Vector3f up,
            Vector3f left,
            Matrix4f projection
    ) {
        Vector3f viewPosition = GravityLensMath.toViewSpace(
                worldPosition.subtract(cameraPosition),
                forward,
                up,
                left
        );
        Vector4f clip = projection.transform(
                new Vector4f(viewPosition, 1.0F)
        );
        if (!Float.isFinite(clip.w()) || clip.w() <= 1.0E-4F) {
            return null;
        }
        float inverseW = 1.0F / clip.w();
        float x = clip.x() * inverseW;
        float y = clip.y() * inverseW;
        float z = clip.z() * inverseW;
        return Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z)
                ? new Vector3f(x, y, z)
                : null;
    }
}
