package net.jacobwasbeast.supernatural.items;

import net.jacobwasbeast.supernatural.ModEntities;
import net.jacobwasbeast.supernatural.api.Ritual;
import net.jacobwasbeast.supernatural.api.RitualManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class RitualCreator extends Item {
    public RitualCreator(Settings settings) {
        super(settings);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 12000;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        LivingEntity user = context.getPlayer();
        if (!world.isClient) {
            BlockPos userBlockPos = user.getBlockPos();
            // Probably inefficient way of seeing if a ritual is in the area
            Box ritualBox = new Box(new BlockPos(userBlockPos.getX()-25,userBlockPos.getY()-25,userBlockPos.getZ()-25), new BlockPos(userBlockPos.getX()+25,userBlockPos.getY()+25,userBlockPos.getZ()+25));
            RitualManager manager = RitualManager.getInstance();
            BlockPos pos = context.getBlockPos();
            if (manager.isValid(world,pos) == null) {
                user.sendMessage(Text.literal("There is not a proper ritual in the area please consult the ritual book to learn of available rituals."));
            }
            else {

                Ritual ritual = manager.isValid(world,pos).getRitual();
                NbtCompound compound = new NbtCompound();
                compound.putString("supernatural:ritual", ritual.reference.getPath());
                manager.applyNBTToBlocks(world,pos,compound);
                user.sendMessage(Text.literal("Created the ritual: " + ritual.name));
                context.getStack().damage(1,context.getPlayer(),playerEntity -> {});
                return ActionResult.PASS;
            }

        }
        return ActionResult.FAIL;
    }
}
