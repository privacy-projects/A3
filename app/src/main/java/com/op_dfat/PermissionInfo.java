package com.op_dfat;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by Dawid Wowniuk
 */

class PermissionInfo implements Comparable<PermissionInfo>{
    
    public String permission;
    public String count;
    List<AnomalyInfo> anomalyInfos;

    PermissionInfo(String permission, String count, List<AnomalyInfo> anomalyInfos){
        this.count = count;
        this.permission = permission;
        this.anomalyInfos = anomalyInfos;
    }

    @Override
    public int compareTo(@NonNull PermissionInfo permissions) {
        int countValue1 = Integer.parseInt(count);
        int countValue2 = Integer.parseInt(permissions.count);
        return countValue2 - countValue1;
    }

    @Override
    public String toString() {
        return count + "x " + permission + ":\n" + anomalyInfos.toString();
    }
}
