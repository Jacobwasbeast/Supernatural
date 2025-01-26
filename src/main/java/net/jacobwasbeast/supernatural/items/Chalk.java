package net.jacobwasbeast.supernatural.items;

import net.jacobwasbeast.supernatural.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class Chalk extends Item {
    private static final int MAX_DURABILITY = 100;  // Set max durability for the chalk

    public Chalk(Settings settings) {
        super(settings.maxDamage(MAX_DURABILITY));  // Define chalk's durability
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack chalkStack = player.getStackInHand(hand);

        if (!world.isClient) {
            // Logic for drawing symbols, check player’s book for which ritual to use
            player.sendMessage(Text.of("Open your ritual book to begin marking symbols with chalk."),true);
        }

        return new TypedActionResult<>(ActionResult.SUCCESS, chalkStack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        Direction side = context.getSide();
        PlayerEntity player = context.getPlayer();
        ItemStack chalkStack = context.getStack();

        if (!world.isClient) {
            // Check player's book to determine the correct symbol for the ritual
            if (isValidSymbolPlacement(player, world, pos)) {
                // Mark the block with chalk (you could create a special block state for chalk symbols)
                BlockState markedState = getSymbolForCurrentRitual(player);  // Define symbols for rituals

                world.setBlockState(pos, markedState);
                chalkStack.damage(1, player, (p) -> p.sendToolBreakStatus(context.getHand()));  // Reduce chalk durability

                player.playSound(SoundEvents.BLOCK_STONE_PLACE, 1.0F, 1.0F);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.FAIL;
    }

    // Check if the block is a valid place to draw a symbol
    private boolean isValidSymbolPlacement(PlayerEntity player, World world, BlockPos pos) {
        // Add your validation logic here, e.g., checking for flat surfaces, part of the ritual, etc.
        return true;  // Simplified for now
    }

    // Get the symbol block state for the current ritual
    private BlockState getSymbolForCurrentRitual(PlayerEntity player) {
        // This would be based on the player's selected ritual from the book
        // Create your custom block states or particle effects to simulate symbols
        return ModBlocks.CHALK_SYMBOL.getDefaultState();
    }
}
