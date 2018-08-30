package se.miun.joli1407.bathingsites;

import java.util.List;

/**
 * Callback interface for database access
 */
public interface OnDBAccessCompleted {
    void onDBAccessCompleted(Long result);
    void onDBAccessCompleted(Long[] result);
    void onDBAccessCompleted(BathingSite bathingSite);
    void onDBAccessCompleted(List<BathingSite> bathingSites);
}