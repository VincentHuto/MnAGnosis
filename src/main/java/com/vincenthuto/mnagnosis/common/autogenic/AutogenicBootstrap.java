package com.vincenthuto.mnagnosis.common.autogenic;

public final class AutogenicBootstrap {
    private AutogenicBootstrap() {
    }

    public static void bootstrap() {
        AutogenicCastRuntime.bootstrap();
    }
}
