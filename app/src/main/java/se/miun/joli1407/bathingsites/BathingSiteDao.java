package se.miun.joli1407.bathingsites;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import java.util.List;

/**
 * Database queries
 */
@Dao
public interface BathingSiteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long addBathingSite(BathingSite bathingSite);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long[] addBathingSites(List<BathingSite> bathingSites);

    @Query("SELECT * FROM bathing_site WHERE id = :bsId")
    BathingSite getBathingSite(long bsId);

    @Query("SELECT * FROM bathing_site")
    List<BathingSite> getAllBathingSites();

    @Query("SELECT COUNT(*) FROM bathing_site")
    long countBathingSites();
}
