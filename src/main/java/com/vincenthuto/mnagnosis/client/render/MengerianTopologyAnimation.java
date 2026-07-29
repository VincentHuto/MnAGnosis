package com.vincenthuto.mnagnosis.client.render;

import java.util.List;

public final class MengerianTopologyAnimation {

    private static volatile Settings settings =
            new Settings(2, 1.35F, 0.10F);

    private MengerianTopologyAnimation() {
    }

    public static Settings settings() {
        return settings;
    }

    public static synchronized void setDepth(int depth) {
        Settings current = settings;
        settings = new Settings(
                depth,
                current.secondsPerDivision(),
                current.separation()
        );
    }

    public static synchronized void setSecondsPerDivision(float seconds) {
        Settings current = settings;
        settings = new Settings(
                current.depth(),
                seconds,
                current.separation()
        );
    }

    public static synchronized void setSeparation(float separation) {
        Settings current = settings;
        settings = new Settings(
                current.depth(),
                current.secondsPerDivision(),
                separation
        );
    }

    /**
     * Returns a frame for elapsed real time in seconds.
     */
    public static Frame frame(float elapsedSeconds) {
        Settings current = settings;
        if (!Float.isFinite(elapsedSeconds)) {
            elapsedSeconds = 0.0F;
        }
        int segmentCount = current.depth() * 2;
        float position = normalize(
                elapsedSeconds / current.secondsPerDivision(),
                segmentCount
        );
        int segment = Math.min(segmentCount - 1, (int) Math.floor(position));
        float progress = position - segment;

        if (segment < current.depth()) {
            return subdivision(segment + 1, progress, current);
        }

        int collapsingDepth = segmentCount - segment;
        return subdivision(collapsingDepth, 1.0F - progress, current);
    }

    private static Frame subdivision(
            int depth,
            float progress,
            Settings settings
    ) {
        float eased = smoothStep(progress);
        if (eased <= 0.000_01F) {
            return new Frame(
                    depth - 1,
                    1.0F,
                    settings.separation(),
                    settings.depth()
            );
        }
        return new Frame(
                depth,
                eased,
                settings.separation(),
                settings.depth()
        );
    }

    private static float normalize(float value, int period) {
        float wrapped = value % period;
        return wrapped < 0.0F ? wrapped + period : wrapped;
    }

    private static float smoothStep(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    public record Frame(
            int depth,
            float subdivision,
            float separation,
            int maximumDepth
    ) {

        public List<MengerianTopologyGeometry.Cell> cells() {
            return MengerianTopologyGeometry.cells(depth);
        }

        public float cellSize() {
            validateDepth();
            float sourceSize = depth == 0
                    ? 1.0F
                    : 1.0F / MengerianTopologyGeometry.gridSize(depth - 1);
            float targetSize =
                    1.0F / MengerianTopologyGeometry.gridSize(depth);
            float subdivided = lerp(sourceSize, targetSize, subdivision);
            return subdivided * (1.0F - separation * subdivision);
        }

        public float center(int coordinate) {
            validateDepth();
            if (depth == 0) {
                return 0.0F;
            }
            float target = normalizedCenter(coordinate, depth);
            float source = depth == 1
                    ? 0.0F
                    : normalizedCenter(coordinate / 3, depth - 1);
            float subdivided = lerp(source, target, subdivision);
            return subdivided * (1.0F + separation * subdivision * 0.35F);
        }

        public float depthProgress() {
            if (maximumDepth <= 0) {
                return 0.0F;
            }
            float completedDepth = depth == 0
                    ? 0.0F
                    : depth - 1.0F + subdivision;
            return Math.max(
                    0.0F,
                    Math.min(1.0F, completedDepth / maximumDepth)
            );
        }

        public float recursiveDepth() {
            if (depth == 0) {
                return 0.0F;
            }
            return depth - 1.0F + subdivision;
        }

        private static float normalizedCenter(int coordinate, int depth) {
            float grid = MengerianTopologyGeometry.gridSize(depth);
            return (coordinate + 0.5F) / grid - 0.5F;
        }

        private static float lerp(float from, float to, float progress) {
            return from + (to - from) * progress;
        }

        private void validateDepth() {
            if (depth < 0 || depth > MengerianTopologyGeometry.MAX_DEPTH) {
                throw new IllegalArgumentException(
                        "Menger animation depth must be between 0 and "
                                + MengerianTopologyGeometry.MAX_DEPTH
                );
            }
        }
    }

    public record Settings(
            int depth,
            float secondsPerDivision,
            float separation
    ) {

        public Settings {
            if (depth < 1 || depth > MengerianTopologyGeometry.MAX_DEPTH) {
                throw new IllegalArgumentException(
                        "depth must be between 1 and "
                                + MengerianTopologyGeometry.MAX_DEPTH
                );
            }
            if (!Float.isFinite(secondsPerDivision)
                    || secondsPerDivision <= 0.0F
                    || secondsPerDivision > 30.0F) {
                throw new IllegalArgumentException(
                        "secondsPerDivision must be finite, above 0, "
                                + "and no greater than 30"
                );
            }
            if (!Float.isFinite(separation)
                    || separation < 0.0F
                    || separation > 0.45F) {
                throw new IllegalArgumentException(
                        "separation must be finite and between 0 and 0.45"
                );
            }
        }
    }
}
