package com.tailormade.tailor.utils;

import com.tailormade.tailor.data.DesignDataClientCache;
import com.tailormade.tailor.data.DesignDataRecord;

import java.util.UUID;

public class DesignGuard {
    public static boolean canEdit(UUID patternId, UUID playerId) {
        return canEdit(patternId, playerId, false);
    }

    public static boolean canEdit(UUID patternId, UUID playerId, boolean acceptNullData) {
        // 型紙がロックされていない場合は一律OK
        // ロックされている場合は、本人ならOK
        DesignDataRecord record = DesignDataClientCache.get(patternId);
        if (record == null) return acceptNullData;
        return !record.isLocked() || record.userId().equals(playerId);
    }
}
