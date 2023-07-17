package nullblade.dimensionalitemcannons.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class GuideItem extends Item {
    public GuideItem() {
        super(new Settings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            user.sendMessage(Text.translatable("dimensional_item_cannons.how_to_use"));
        }

        return TypedActionResult.consume(user.getStackInHand(hand));
    }
}
