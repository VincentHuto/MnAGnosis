package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.vincenthuto.mnagnosis.common.authorship.cast.AuthorshipCastPermit;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;

public final class HarmInvocationScope implements AutoCloseable {
    private static final ThreadLocal<Deque<Frame>> FRAMES =
            ThreadLocal.withInitial(ArrayDeque::new);

    private final OwnerFrame owner;
    private boolean closed;

    private HarmInvocationScope(OwnerFrame owner) {
        this.owner = owner;
    }

    public static HarmInvocationScope open(
            AuthorshipCastPermit permit,
            int componentIndex,
            ResourceLocation componentId,
            Object partIdentity,
            Object contextIdentity,
            UUID targetId,
            ResourceLocation adapterId,
            HarmGate gate
    ) {
        Objects.requireNonNull(permit, "permit");
        Objects.requireNonNull(componentId, "componentId");
        Objects.requireNonNull(partIdentity, "partIdentity");
        Objects.requireNonNull(contextIdentity, "contextIdentity");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(adapterId, "adapterId");
        Objects.requireNonNull(gate, "gate");
        if (componentIndex < 0) {
            throw new IllegalArgumentException("componentIndex");
        }
        Authorization authorization = new Authorization(
                permit.castId(),
                componentIndex,
                componentId,
                partIdentity,
                contextIdentity,
                targetId,
                adapterId,
                gate
        );
        OwnerFrame owner = new OwnerFrame(authorization);
        FRAMES.get().push(owner);
        return new HarmInvocationScope(owner);
    }

    public boolean invoke(
            UUID targetId,
            HarmGate gate,
            Object nativeIdentity,
            BooleanSupplier nativeCall
    ) {
        requireOpen();
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(gate, "gate");
        Objects.requireNonNull(nativeIdentity, "nativeIdentity");
        Objects.requireNonNull(nativeCall, "nativeCall");
        Deque<Frame> frames = FRAMES.get();
        if (frames.peek() != owner) {
            throw new IllegalStateException("Harm owner is not active");
        }
        NativeFrame nativeFrame = new NativeFrame(
                owner,
                targetId,
                gate,
                nativeIdentity
        );
        frames.push(nativeFrame);
        try {
            boolean result = nativeCall.getAsBoolean();
            owner.nativeSucceeded = result;
            return result;
        } finally {
            if (frames.peek() != nativeFrame) {
                throw new IllegalStateException("Unbalanced native harm frame");
            }
            frames.pop();
        }
    }

    public static boolean consume(
            UUID targetId,
            HarmGate gate,
            Object nativeIdentity
    ) {
        Frame frame = FRAMES.get().peek();
        if (!(frame instanceof NativeFrame nativeFrame)
                || nativeFrame.owner.consumed
                || !nativeFrame.targetId.equals(targetId)
                || nativeFrame.gate != gate
                || nativeFrame.nativeIdentity != nativeIdentity) {
            return false;
        }
        Authorization authorization = nativeFrame.owner.authorization;
        if (!authorization.targetId().equals(targetId)
                || authorization.gate() != gate) {
            return false;
        }
        nativeFrame.owner.consumed = true;
        return true;
    }

    public Authorization authorization() {
        return owner.authorization;
    }

    public Outcome outcome() {
        return new Outcome(
                owner.consumed,
                owner.nativeSucceeded,
                owner.authorization.gate()
        );
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        Deque<Frame> frames = FRAMES.get();
        if (frames.peek() != owner) {
            throw new IllegalStateException("Unbalanced harm owner frame");
        }
        frames.pop();
        closed = true;
        if (frames.isEmpty()) {
            FRAMES.remove();
        }
    }

    static boolean hasFrames() {
        return !FRAMES.get().isEmpty();
    }

    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException("Harm scope is closed");
        }
    }

    public record Authorization(
            UUID castId,
            int componentIndex,
            ResourceLocation componentId,
            Object partIdentity,
            Object contextIdentity,
            UUID targetId,
            ResourceLocation adapterId,
            HarmGate gate
    ) {
    }

    public record Outcome(
            boolean gateConsumed,
            boolean nativeSucceeded,
            HarmGate gate
    ) {
    }

    private sealed interface Frame permits OwnerFrame, NativeFrame {
    }

    private static final class OwnerFrame implements Frame {
        private final Authorization authorization;
        private boolean consumed;
        private boolean nativeSucceeded;

        private OwnerFrame(Authorization authorization) {
            this.authorization = authorization;
        }
    }

    private record NativeFrame(
            OwnerFrame owner,
            UUID targetId,
            HarmGate gate,
            Object nativeIdentity
    ) implements Frame {
    }
}
