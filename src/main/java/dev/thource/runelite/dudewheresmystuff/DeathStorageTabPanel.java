package dev.thource.runelite.dudewheresmystuff;

import dev.thource.runelite.dudewheresmystuff.death.Deathpile;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.ItemManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.Comparator;

@Slf4j
class DeathStorageTabPanel extends StorageTabPanel<DeathStorageType, DeathStorage, DeathStorageManager> {
    DeathStorageTabPanel(ItemManager itemManager, DudeWheresMyStuffConfig config, DudeWheresMyStuffPanel pluginPanel, DeathStorageManager storageManager) {
        super(itemManager, config, pluginPanel, storageManager);
    }

    @Override
    protected Comparator<DeathStorage> getStorageSorter() {
        return Comparator.comparingInt(s -> {
            if (s instanceof Deathpile) {
                Deathpile deathpile = ((Deathpile) s);

                // Move expired deathpiles to the bottom of the list, don't worry about int overflow, no one has 2182 years played
                if (deathpile.hasExpired())
                    return deathpile.getPlayedMinutesAtCreation() + 1000000000;

                return deathpile.getPlayedMinutesAtCreation();
            } else {
                return -1;
            }
        });
    }

    @Override
    protected void rebuildList(boolean isMember) {
        removeAll();

        itemsBoxes.clear();

        ItemsBox debugDeathBox = new ItemsBox(itemManager, "Death items debug", null, false, showPrice());
        for (ItemStack itemStack : storageManager.getDeathItems()) {
            if (itemStack.getQuantity() > 0)
                debugDeathBox.getItems().add(itemStack);
        }
        debugDeathBox.rebuild();
        itemsBoxes.add(debugDeathBox);
        add(debugDeathBox);

        storageManager.storages.stream().sorted(getStorageSorter()).forEach((storage) -> {
            if (storage.getType().isMembersOnly() && !isMember) return;

            ItemsBox itemsBox = new ItemsBox(itemManager, storage, null, false, showPrice());
            for (ItemStack itemStack : storage.getItems()) {
                if (itemStack.getQuantity() > 0)
                    itemsBox.getItems().add(itemStack);
            }
            itemsBox.rebuild();
            itemsBoxes.add(itemsBox);
            add(itemsBox);

            if (storage instanceof Deathpile) {
                Deathpile deathpile = (Deathpile) storage;
                itemsBox.addExpiry(deathpile.getExpiryMs());

                final JPopupMenu popupMenu = new JPopupMenu();
                popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
                itemsBox.setComponentPopupMenu(popupMenu);

                final JMenuItem toggle = new JMenuItem("Delete Deathpile");
                toggle.addActionListener(e -> {
                    int result = JOptionPane.OK_OPTION;

                    try
                    {
                        result = JOptionPane.showConfirmDialog(
                                this,
                                "Are you sure you want to delete this deathpile?\nThis cannot be undone.", "Confirm deletion",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                    }
                    catch (Exception err)
                    {
                        log.warn("Unexpected exception occurred while check for confirm required", err);
                    }

                    if (result == JOptionPane.OK_OPTION)
                    {
                        storageManager.storages.remove(deathpile);
                        rebuildList(isMember);
                        storageManager.refreshMapPoints();
                        storageManager.save();
                    }
                });
                popupMenu.add(toggle);

                Region region = Region.get(deathpile.getWorldPoint().getRegionID());

                if (region == null) {
                    itemsBox.setSubTitle("Unknown");
                } else {
                    itemsBox.setSubTitle(region.getName());
                }
            }
        });

        revalidate();
    }
}
