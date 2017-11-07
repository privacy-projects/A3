package com.op_dfat;

import android.graphics.drawable.Drawable;
import java.util.Comparator;

/**
 * Created by Dawid Wowniuk
 */

class Apps {

    public Drawable icon;
    public String appName;
    public String packageName;
    public String count;
    public String resource;
    String score;
    String iconURL;
    private int anomalyFound;

    // Constructor with drawable icons
    Apps (Drawable icon, String appName, String packageName) {
        this.icon = icon;
        this.appName = appName;
        this.packageName = packageName;
        this.count = null;
        this.resource = null;
        this.anomalyFound = 0;
    }

    // Constructor with icons url and score
    Apps (String icon, String appName, String packageName, String score) {
        this.iconURL = icon;
        this.appName = appName;
        this.packageName = packageName;
        this.score = score;
    }


    enum AppComparator implements Comparator<Apps> {
        SORT_BY_NAME {
            @Override
            public int compare(Apps apps, Apps t1) {
                return apps.appName.toLowerCase().compareTo(t1.appName.toLowerCase());
            }
        },
        SORT_BY_ANOMALY {
            @Override
            public int compare(Apps apps, Apps t1) {
                int comp = apps.anomalyFound > t1.anomalyFound ? -1 : 0;
                if (comp == 0) {
                    comp = apps.anomalyFound == t1.anomalyFound ? 0 : 1;
                }
                return comp;
            }
        };

        public static Comparator<Apps> decending(final Comparator<Apps> other) {
            return new Comparator<Apps>() {
                public int compare(Apps o1, Apps o2) {
                    return other.compare(o1, o2);
                }
            };
        }

        public static Comparator<Apps> getComparator(final AppComparator... multipleOptions) {
            return new Comparator<Apps>() {
                public int compare(Apps o1, Apps o2) {
                    for (AppComparator option : multipleOptions) {
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
