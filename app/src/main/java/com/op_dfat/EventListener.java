package com.op_dfat;

import java.util.Date;

/**
 * Created by arrigo on 1/18/17.
 */

public interface EventListener {

    void OnPermissionLogRead (String packageName, int opID, Date accessTime);
}
