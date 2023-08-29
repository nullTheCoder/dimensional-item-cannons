package nullblade.dimensionalitemcannons.items;

import net.minecraft.item.Item;

public class DimensionalShell extends Item {
    public final int tier;
    
    public DimensionalShell(int tier) {
        super(new Settings());
        this.tier = tier;
    }
}
