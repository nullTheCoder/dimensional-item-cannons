package nullblade.dimensionalitemcannons;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;
import nullblade.dimensionalitemcannons.canon.DimensionalCannon;
import nullblade.dimensionalitemcannons.canon.DimensionalCannonEntity;
import nullblade.dimensionalitemcannons.canon.DimensionalItemCannonScreenHandler;
import nullblade.dimensionalitemcannons.client.DimensionalItemCannonsClient;
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

@Mod(DimensionalItemCannons.id)
public class DimensionalItemCannons {
    public static final Logger LOGGER = LoggerFactory.getLogger("dimensional-item-cannons");

	public static RegistryObject<Item>[] itemCanonShell;

	public static RegistryObject<Block> dimensionItemCanon;

	public static RegistryObject<BlockEntityType<DimensionalCannonEntity>> dimensionalItemCanonEntity;

	public static final String id = "dimensional_item_cannons";
	public static RegistryObject<ScreenHandlerType<DimensionalItemCannonScreenHandler>> screenHandler;

	public static RegistryObject<Item> dimensionalStone;

	public static int amountOfShells = 5;

	public static int max3DDistance = 25;
	public static int max3DDistancePerShellTier = 10;

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, id);
	private static final DeferredRegister<ScreenHandlerType<?>> SCREEN_HANDLER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, id);
	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, id);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, id);
	private static final DeferredRegister<ItemGroup> ITEM_GROUP = DeferredRegister.create(Registries.ITEM_GROUP.getKey(), id);

	public DimensionalItemCannons() {
		LOGGER.info("Hello, World!");
		loadConfigs(null);

		screenHandler = SCREEN_HANDLER.register("dimensional_item_cannon", () -> new ScreenHandlerType<>(DimensionalItemCannonScreenHandler::new, FeatureSet.empty()));

		dimensionItemCanon = BLOCKS.register("dimensional_item_cannon", DimensionalCannon::new);
		ITEMS.register("dimensional_item_cannon", () -> new BlockItem(dimensionItemCanon.get(), new Item.Settings()));

		dimensionalItemCanonEntity = BLOCK_ENTITY_TYPES.register("dimensional_item_canon_entity",
				() -> BlockEntityType.Builder.create((blockPos, blockState) -> 
						new DimensionalCannonEntity(dimensionalItemCanonEntity.get(), blockPos, blockState), dimensionItemCanon.get()).build(null));

		itemCanonShell = new RegistryObject[amountOfShells];
		for (int x = 0 ; x < amountOfShells ; x++) {
			int finalX = x;
			itemCanonShell[x] = ITEMS.register("dimensional_shell_tier" + x, () -> new DimensionalShell(finalX));
		}

		RegistryObject<Block> explosionResistantStone = BLOCKS.register("explosion_resistant_stone", () -> new Block(AbstractBlock.Settings.copy(Blocks.OBSIDIAN).requiresTool().strength(8.0F, 1200.0F)));
		ITEMS.register("explosion_resistant_stone", () -> new BlockItem(explosionResistantStone.get(), new Item.Settings()));

		dimensionalStone = ITEMS.register("dimensional_stone", () -> new DimensionalStone(new Item.Settings().maxCount(1)));

		RegistryObject<Item> guide = ITEMS.register("guide", GuideItem::new);

		ITEM_GROUP.register("dimensional_item_cannons_tab", () ->
				ItemGroup
						.builder()
						.displayName(Text.translatable(id + ".tab"))
						.icon(() -> dimensionItemCanon.get().asItem().getDefaultStack())
						.noScrollbar()
						.entries((displayContext, entries) -> {
							entries.add(dimensionItemCanon.get());
							entries.add(dimensionalStone.get());
							for (RegistryObject<Item> shell : itemCanonShell) {
								entries.add(shell.get());
							}
							entries.add(guide.get());
							entries.add(explosionResistantStone.get());
						})
						.build());
		
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

		bus.register(this);
		bus.addListener(DimensionalItemCannonsClient::onInitializeClient);
		
		SCREEN_HANDLER.register(bus);
		BLOCKS.register(bus);
		BLOCK_ENTITY_TYPES.register(bus);
		ITEMS.register(bus);
		ITEM_GROUP.register(bus);
	}

	private boolean noReload = false;
	
	@SubscribeEvent
	public void commonSetup(FMLCommonSetupEvent e) {
		loadConfigs(ServerLifecycleHooks.getCurrentServer());
	}

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