package nullblade.dimensionalitemcannons.client;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import nullblade.dimensionalitemcannons.DimensionalItemCannons;

public class DimensionalItemCannonsClient {
	public static void onInitializeClient(final FMLClientSetupEvent event) {
		HandledScreens.register(DimensionalItemCannons.screenHandler.get(), DimensionalCanonScreen::new);
	}
}