package dev.thource.runelite.dudewheresmystuff.death;

import dev.thource.runelite.dudewheresmystuff.DudeWheresMyStuffConfig;
import dev.thource.runelite.dudewheresmystuff.DudeWheresMyStuffPlugin;
import dev.thource.runelite.dudewheresmystuff.Region;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;

public class SoonestDeathpileOverlay extends Overlay {
  private final DudeWheresMyStuffPlugin plugin;
  private final DudeWheresMyStuffConfig config;
  private DeathStorageManager deathStorageManager;
  private int soonestExpiringDeathpileMinutesLeft = -1;
  private boolean soonestExpiringDeathpileColor = false;
  private Deathpile soonestExpiringDeathpile;
  private String regionName = "Unknown";

  @Inject
  public SoonestDeathpileOverlay(DeathStorageManager deathStorageManager,
      DudeWheresMyStuffPlugin plugin, DudeWheresMyStuffConfig config) {
    setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
    this.deathStorageManager = deathStorageManager;
    this.plugin = plugin;
    this.config = config;
  }

  private void updateSoonestDeathPileOverlay() {
    // This doesn't work, death pile is always null, unsure how to fix
//    soonestExpiringDeathpile = deathStorageManager.findSoonestExpiringDeathpile();

    // This works
    soonestExpiringDeathpile = plugin.soonestDeathpile;

    if (soonestExpiringDeathpile != null) {
      Region region = Region.get(soonestExpiringDeathpile.getWorldPoint().getRegionID());
      regionName = (region == null ? "Unknown" : region.getName());

      // Switches between two overlay colors
      soonestExpiringDeathpileColor = !soonestExpiringDeathpileColor;
      soonestExpiringDeathpileMinutesLeft = (int) Math.floor(
          (soonestExpiringDeathpile.getExpiryMs() - System.currentTimeMillis()) / 60_000f);
    } else {
      // Reset / clear variables if there's no death pile
      System.out.println("Death pile is null");
      regionName = "Unknown";
      soonestExpiringDeathpileMinutesLeft = -1;
    }
  }

  // If there is a Death pile expiring soon that matches the config criteria
  private boolean shouldRenderOverlay() {
    updateSoonestDeathPileOverlay();

    return soonestExpiringDeathpile != null &&
        soonestExpiringDeathpileMinutesLeft <= config.timeUntilDeathpileExpires() &&
        config.warnDeathPileExpiring();
  }

  @Override
  public Dimension render(Graphics2D graphics) {

    if (shouldRenderOverlay()) {
      return renderText(graphics);
    }
    return null;
  }

  private Dimension renderText(Graphics2D graphics) {
    Font font = FontManager.getRunescapeFont()
        .deriveFont(Font.PLAIN, config.warnDeathpileExpiringFontSize());
    graphics.setFont(font);
    // Change to suggestion
//    String deathpileExpiringText = "Your " + regionName + " deathpile " + soonestExpiringDeathpile.getExpireText().toLowerCase();
    String deathpileExpiringText = soonestExpiringDeathpile.getExpireText().toLowerCase();

    // Alternates between two colors, this could be customized later
    Color textColor = soonestExpiringDeathpileColor ? Color.RED : Color.WHITE;
    graphics.setColor(textColor);

    FontMetrics metrics = graphics.getFontMetrics(font);

    int textWidth = metrics.stringWidth(deathpileExpiringText);
    int textHeight = metrics.getHeight();

    graphics.drawString(deathpileExpiringText, 0, textHeight);

    return new Dimension(textWidth, textHeight);
  }
}
