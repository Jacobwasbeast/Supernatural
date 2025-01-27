package net.jacobwasbeast.supernatural.items;

import net.jacobwasbeast.supernatural.entities.DemonEntity;
import net.jacobwasbeast.supernatural.entities.DemonVillager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;

public class RubyKnife extends SwordItem {
    public RubyKnife(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target instanceof DemonVillager || target instanceof DemonEntity) {
            target.damage(attacker.getDamageSources().mobAttack(attacker),50);
        }
        return super.postHit(stack, target, attacker);
    }
}
