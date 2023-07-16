package nullblade.dimensionalitemcannons;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import nullblade.dimensionalitemcannons.canon.DimensionalCannon;
import nullblade.dimensionalitemcannons.canon.DimensionalCannonEntity;
import nullblade.dimensionalitemcannons.canon.DimensionalItemCannonScreenHandler;
import nullblade.dimensionalitemcannons.shell.DimensionalShell;
import nullblade.dimensionalitemcannons.shell.DimensionalStone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DimensionalItemCannons implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("dimensional-item-cannons");

	public static Item[] itemCanonShell;

	public static Block dimensionItemCanon;

	public static BlockEntityType<DimensionalCannonEntity> dimensionalItemCanonEntity;

	public static final String id = "dimensional_item_cannons";
	public static ScreenHandlerType<DimensionalItemCannonScreenHandler> screenHandler;

	public static Item dimensionalStone;


	@Override
	public void onInitialize() {
		LOGGER.info("Hello, World!");


		screenHandler = Registry.register(Registries.SCREEN_HANDLER, new Identifier(id, "dimensional_item_canon"), new ScreenHandlerType<>(DimensionalItemCannonScreenHandler::new, FeatureSet.empty()));

		dimensionItemCanon = Registry.register(Registries.BLOCK, new Identifier(id, "dimensional_item_canon"), new DimensionalCannon());
		Registry.register(Registries.ITEM, new Identifier(id, "dimensional_item_canon"), new BlockItem(dimensionItemCanon, new Item.Settings()));

		dimensionalItemCanonEntity = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				new Identifier(id, "dimensional_item_canon_entity"),
				FabricBlockEntityTypeBuilder.create((blockPos, blockState) -> new DimensionalCannonEntity(dimensionalItemCanonEntity, blockPos, blockState), dimensionItemCanon).build());

		int amountOfShells = 5;
		itemCanonShell = new Item[amountOfShells];
		for (int x = 0 ; x < amountOfShells ; x++) {
			itemCanonShell[x] = new DimensionalShell(x);
		}

		dimensionalStone =Registry.register(Registries.ITEM, new Identifier(id, "dimensional_stone"), new DimensionalStone(new Item.Settings().maxCount(1)));

		var tab = Registry.register(Registries.ITEM_GROUP, new Identifier(id, "tab"),
				FabricItemGroup
						.builder()
						.displayName(Text.translatable(id + ".tab"))
						.icon(() -> dimensionItemCanon.asItem().getDefaultStack())
						.noScrollbar()
						.entries((displayContext, entries) -> {
							entries.add(dimensionItemCanon);
							entries.add(dimensionalStone);
							for (Item shell : itemCanonShell) {
								entries.add(shell);
							}
						})
						.build());



	}
}