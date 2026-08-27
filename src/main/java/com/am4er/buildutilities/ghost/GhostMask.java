package com.am4er.buildutilities.ghost;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.UUID;

final class GhostMask {

    private static final long NO_CENTRE = Long.MIN_VALUE;

    private LongOpenHashSet hidden = new LongOpenHashSet();
    private UUID world;
    private long centre = NO_CENTRE;

    static long key(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) << 38
                | ((long) z & 0x3FFFFFFL) << 12
                | ((long) y & 0xFFFL);
    }

    static int keyX(long key) { return (int) (key >> 38); }
    static int keyY(long key) { return (int) (key << 52 >> 52); }
    static int keyZ(long key) { return (int) (key << 26 >> 38); }

    UUID world() { return world; }
    LongOpenHashSet hidden() { return hidden; }
    boolean contains(long key) { return hidden.contains(key); }
    boolean isEmpty() { return hidden.isEmpty(); }

    boolean settled(UUID worldId, long at) {
        return centre != NO_CENTRE && at == centre && worldId.equals(world);
    }

    void adopt(UUID worldId, long at, LongOpenHashSet current) {
        this.world = worldId;
        this.centre = at;
        this.hidden = current;
    }

    void forget() {
        hidden = new LongOpenHashSet();
        world = null;
        centre = NO_CENTRE;
    }
}
