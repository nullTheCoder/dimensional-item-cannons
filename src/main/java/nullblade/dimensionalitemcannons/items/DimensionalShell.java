package nullblade.dimensionalitemcannons.items;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import nullblade.dimensionalitemcannons.DimensionalItemCannons;

public class DimensionalShell extends Item {

    public final int tier;
    public DimensionalShell(int tier) {
        super(new Settings());
        this.tier = tier;

        Registry.register(Registries.ITEM, new Identifier(DimensionalItemCannons.id, "dimensional_shell_tier" + tier), this);
    }
}
