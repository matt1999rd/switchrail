package fr.mattmouss.switchrail.item;

import com.dannyandson.tinyredstone.api.AbstractPanelCellItem;

import net.minecraft.world.item.Item.Properties;

public class TerminalCellItem extends AbstractPanelCellItem {
    public TerminalCellItem(Properties properties) {
        super(properties);
        this.setRegistryName("terminal_cell_item");
    }

}
