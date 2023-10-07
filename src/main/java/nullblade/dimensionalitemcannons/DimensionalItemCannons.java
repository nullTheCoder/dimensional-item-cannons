package nullblade.dimensionalitemcannons;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import nullblade.dimensionalitemcannons.canon.DimensionalCannon;
import nullblade.dimensionalitemcannons.canon.DimensionalCannonEntity;
import nullblade.dimensionalitemcannons.canon.DimensionalItemCannonScreenHandler;
import nullblade.dimensionalitemcannons.items.DimensionalShell;
import nullblade.dimensionalitemcannons.items.DimensionalStone;
import nullblade.dimensionalitemcannons.items.GuideItem;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class DimensionalItemCannons implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("dimensional-item-cannons");

	public static Item[] itemCanonShell;

	public static Block dimensionItemCanon;

	public static BlockEntityType<DimensionalCannonEntity> dimensionalItemCanonEntity;

	public static final String id = "dimensional_item_cannons";
	public static ScreenHandlerType<DimensionalItemCannonScreenHandler> screenHandler;

	public static Item dimensionalStone;

	public static int amountOfShells = 5;

	public static int max3DDistance = 25;
	public static int max3DDistancePerShellTier = 10;

	@Override
	public void onInitialize() {
		LOGGER.info("Hello, World!");
		loadConfigs(null);


		screenHandler = Registry.register(Registries.SCREEN_HANDLER, new Identifier(id, "dimensional_item_cannon"), new ScreenHandlerType<>(DimensionalItemCannonScreenHandler::new, FeatureSet.empty()));

		dimensionItemCanon = Registry.register(Registries.BLOCK, new Identifier(id, "dimensional_item_cannon"), new DimensionalCannon());
		Registry.register(Registries.ITEM, new Identifier(id, "dimensional_item_cannon"), new BlockItem(dimensionItemCanon, new Item.Settings()));



		dimensionalItemCanonEntity = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				new Identifier(id, "dimensional_item_canon_entity"),
				FabricBlockEntityTypeBuilder.create((blockPos, blockState) -> new DimensionalCannonEntity(dimensionalItemCanonEntity, blockPos, blockState), dimensionItemCanon).build());

		itemCanonShell = new Item[amountOfShells];
		for (int x = 0 ; x < amountOfShells ; x++) {
			itemCanonShell[x] = new DimensionalShell(x);
		}

		Block explosionResistantStone = Registry.register(Registries.BLOCK, new Identifier(id, "explosion_resistant_stone"), new Block(AbstractBlock.Settings.copy(Blocks.OBSIDIAN).requiresTool().strength(8.0F, 1200.0F)));
		Registry.register(Registries.ITEM, new Identifier(id, "explosion_resistant_stone"), new BlockItem(explosionResistantStone, new Item.Settings()));

		dimensionalStone = Registry.register(Registries.ITEM, new Identifier(id, "dimensional_stone"), new DimensionalStone(new Item.Settings().maxCount(1)));

		Item guide = Registry.register(Registries.ITEM, new Identifier(id, "guide"), new GuideItem());

		Registry.register(Registries.ITEM_GROUP, new Identifier(id, "tab"),
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
							entries.add(guide);
							entries.add(explosionResistantStone);
						})
						.build());

		ServerLifecycleEvents.SERVER_STARTED.register((this::loadConfigs));

		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> loadConfigs(server));


	}

	private boolean noReload = false;

	private void loadConfigs(@Nullable MinecraftServer server) {
		if (noReload)
			return;

		File cfgFile = new File("./config/dimensional-item-cannons.txt");
		if (cfgFile.exists()) {

			Utils.tierNeeded = new HashMap<>();
			try {
				String text = Files.readString(cfgFile.toPath());

				int lineNum = 0;
				for (String line : text.split("\n")) {
					try {
						String[] params = line.split(" ");
						switch (params[0]) {
							case "shell_tiers" -> amountOfShells = Integer.parseInt(params[1]);
							case "dimension" -> {
								Utils.tierNeeded.put(new Identifier(params[1]), Integer.parseInt(params[2]));
								if (server != null) {
									if (server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(params[1]))) == null) {
										LOGGER.warn("Unknown world in line " + lineNum + " in mod's config");
									}
								}
							}
							case "max_3d_distance" -> max3DDistance = Integer.parseInt(params[1]);
							case "additional_3d_distance_per_tier" -> max3DDistancePerShellTier = Integer.parseInt(params[1]);
							case "no_reload" -> noReload = true;
						}

					} catch (Exception e) {
						LOGGER.error("Error parsing line " + lineNum + " in mod's config");
					}
					lineNum++;
				}

				StringBuilder toWrite = new StringBuilder();
				if (server != null) {
					for (World world : server.getWorlds()) {
						if (!Utils.tierNeeded.containsKey(world.getDimensionKey().getValue())) {
							toWrite.append("\ndimension ").append(world.getDimensionKey().getValue()).append(" 2");
						}
					}
					if (!toWrite.isEmpty()) {
						FileWriter writer = new FileWriter(cfgFile, true);
						writer.append(toWrite);
						writer.flush();
						writer.close();
					}
				}

			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		} else {
			try {
				cfgFile.getParentFile().mkdirs();
				Files.write(cfgFile.toPath(), """
				# How many tiers of dimensional shells there should be (the mod has recipes upto tier 2 and textures upto tier 5):
				shell_tiers 3
				# uncomment the following line to disable this config's reloading when entering a world
				# or using /reload command. May be useful to do in very large modpacks when you want to decrease load times as much as possible
				# reloading takes more times the more worlds there are, however even with a 1000 worlds it will probably take a few miliseconds.
				# no_reload
				
				# max 3 dimensional distance between the cannon and its goal chest
				max_3d_distance 25
				
				# additional 3 dimensional distance between the cannon and its goal chest per tier of shell
				additional_3d_distance_per_tier 10
				
				# dimension shell tier requirements settings:
				dimension minecraft:overworld 0
				dimension minecraft:the_nether 0
				dimension minecraft:the_end 1
				""".getBytes());
				loadConfigs(server);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}