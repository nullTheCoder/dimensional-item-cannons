package nullblade.dimensionalitemcannons.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import nullblade.dimensionalitemcannons.DimensionalItemCannons;
import nullblade.dimensionalitemcannons.canon.DimensionalItemCannonScreenHandler;

public class DimensionalCanonScreen extends HandledScreen<DimensionalItemCannonScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(DimensionalItemCannons.id, "textures/gui/canon.png");

    public DimensionalCanonScreen(DimensionalItemCannonScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }


    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
