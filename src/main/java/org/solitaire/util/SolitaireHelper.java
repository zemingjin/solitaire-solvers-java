package org.solitaire.util;

public class SolitaireHelper {
    private static int totalScenarios = 0;

    public static void incTotal() {
        totalScenarios++;
    }

    public static int getTotalScenarios() {
        return totalScenarios;
    }

    public static void setTotalScenarios(int totalScenarios) {
        SolitaireHelper.totalScenarios = totalScenarios;
    }
}
