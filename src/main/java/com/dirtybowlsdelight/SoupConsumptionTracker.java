package com.dirtybowlsdelight;

/**
 * 非Mixin类，用于在不同Mixin之间共享soup食用状态
 * 避免Mixin包内的类直接引用导致的类加载冲突
 */
public class SoupConsumptionTracker {
    private static final ThreadLocal<Boolean> isProcessingSoupConsumption = ThreadLocal.withInitial(() -> false);

    public static void setProcessingSoupConsumption(boolean processing) {
        isProcessingSoupConsumption.set(processing);
    }

    public static boolean isProcessingSoupConsumption() {
        return isProcessingSoupConsumption.get();
    }
}
