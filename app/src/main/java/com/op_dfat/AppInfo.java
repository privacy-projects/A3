package com.op_dfat;

import android.graphics.drawable.Drawable;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Arrigo Paterno
 */

class AppInfo {

    String packageName;
    String appName;
    Drawable appIcon;
    int anomalyFound;
    int permisionsUsed;
    List<PermissionInfo> permissionInfos;

    AppInfo(String packageName, String appName, Drawable appIcon, int anomalyFound, int permisionsUsed, List<PermissionInfo> permissionInfos) {
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.appName = appName;
        this.permissionInfos = permissionInfos;
        this.anomalyFound = anomalyFound;
        this.permisionsUsed = permisionsUsed;
    }

    enum AppComparator implements Comparator<AppInfo> {
        SORT_BY_NAME {
            @Override
            public int compare(AppInfo apps, AppInfo t1) {
                return apps.appName.toLowerCase().compareTo(t1.appName.toLowerCase());
            }
        },
        SORT_BY_ANOMALY {
            @Override
            public int compare(AppInfo apps, AppInfo t1) {
                int comp = apps.anomalyFound > t1.anomalyFound ? -1 : 0;
                if (comp == 0) {
                    comp = apps.anomalyFound == t1.anomalyFound ? 0 : 1;
                }
                return comp;
            }
        };

        public static Comparator<AppInfo> decending(final Comparator<AppInfo> other) {
            return new Comparator<AppInfo>() {
                public int compare(AppInfo o1, AppInfo o2) {
                    return other.compare(o1, o2);
                }
            };
        }

        public static Comparator<AppInfo> getComparator(final AppInfo.AppComparator... multipleOptions) {
            return new Comparator<AppInfo>() {
                public int compare(AppInfo o1, AppInfo o2) {
                    for (AppInfo.AppComparator option : multipleOptions) {
                        int result = option.compare(o1, o2);
                        if (result != 0) {
                            return result;
                        }
                    }
                    return 0;
                }
            };
        }
    }
}
