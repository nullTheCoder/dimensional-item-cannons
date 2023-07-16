package nullblade.dimensionalitemcannons.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.SpriteIdentifier;
import nullblade.dimensionalitemcannons.DimensionalItemCannons;

public class DimensionalItemCannonsClient implements ClientModInitializer {

	public static SpriteIdentifier chestSprite;
	public static EntityModelLayer chestLayer;

	@Override
	public void onInitializeClient() {
		HandledScreens.register(DimensionalItemCannons.screenHandler, DimensionalCanonScreen::new);
	}
}