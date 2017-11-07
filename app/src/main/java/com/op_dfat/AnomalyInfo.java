package com.op_dfat;

/**
 * Created by Arrigo Paterno
 */

class AnomalyInfo {

    String isAnomalous;
    String anomalyReason;
    String accessTime;

    AnomalyInfo(String isAnomalous, String anomalyReason, String accessTime) {
        this.isAnomalous = isAnomalous;
        this.anomalyReason = anomalyReason;
        this.accessTime = accessTime;
    }

    @Override
    public String toString() {
        return accessTime + " - " + isAnomalous + "\n";
    }
}
