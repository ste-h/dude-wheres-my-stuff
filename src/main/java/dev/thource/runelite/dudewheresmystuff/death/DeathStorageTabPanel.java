package dev.thource.runelite.dudewheresmystuff.death;

import dev.thource.runelite.dudewheresmystuff.DudeWheresMyStuffPlugin;
import dev.thource.runelite.dudewheresmystuff.EnhancedSwingUtilities;
import dev.thource.runelite.dudewheresmystuff.StorageTabPanel;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;

/** DeathStorageTabPanel is responsible for displaying death storage data to the player. */
@Slf4j
public class DeathStorageTabPanel
    extends StorageTabPanel<DeathStorageType, DeathStorage, DeathStorageManager> {

  public DeathStorageTabPanel(DudeWheresMyStuffPlugin plugin, DeathStorageManager storageManager) {
    super(plugin, storageManager);
  }

  @Override
  protected Comparator<DeathStorage> getStorageSorter() {
    return Comparator.comparingLong(
        s -> {
          if (s instanceof Deathpile) {
            Deathpile deathpile = (Deathpile) s;

            // Move expired deathpiles to the bottom of the list and sort them the opposite way
            // (newest first)
            if (deathpile.hasExpired()) {
              return Long.MAX_VALUE - deathpile.getExpiryMs();
            }

            return Long.MIN_VALUE + deathpile.getExpiryMs();
          } else if (s instanceof DeathItems) {
            return Long.MIN_VALUE;
          } else {
            Deathbank deathbank = (Deathbank) s;

            if (deathbank.getLostAt() != -1L) {
              return Long.MAX_VALUE - deathbank.getLostAt();
            }

            return Long.MIN_VALUE + 1;
          }
        });
  }
}
