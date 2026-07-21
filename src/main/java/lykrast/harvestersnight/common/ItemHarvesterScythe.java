package lykrast.harvestersnight.common;


import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeHooks;

import com.deeperdepths.common.DeeperDepths;

import org.sporotofpoorety.eternitymode.core.EternityModeSoundEvents;
import org.sporotofpoorety.eternitymode.entity.EntityDemonScythe;
import org.sporotofpoorety.eternitymode.interfacemixins.IMixinEntityMob;
import org.sporotofpoorety.eternitymode.util.PacketUtil;
import org.sporotofpoorety.eternitymode.util.PlayerMeleeUtil;




public class ItemHarvesterScythe extends ItemSword 
{

	public ItemHarvesterScythe(ToolMaterial material) 
    {
		super(material);
	}
	

    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) 
    {
        if (state.getBlockHardness(worldIn, pos) != 0) stack.damageItem(1, entityLiving);
        return true;
    }
    

    public boolean canHarvestBlock(IBlockState blockIn) 
    {
        Block block = blockIn.getBlock();

        if (block == Blocks.WEB || block == Blocks.VINE || block == Blocks.LEAVES || block == Blocks.LEAVES2) 
        { 
            return true; 
        }
        else
        {
            Material material = blockIn.getMaterial();
            return material == Material.LEAVES || material == Material.PLANTS || material == Material.VINE || material == Material.WEB;
        }
    }
    

    @Override
    public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) 
    {
    	return true;
    }
    

    //Adapted from the CoFH Core sickles
    //https://github.com/CoFH/CoFHCore/blob/1.12/src/main/java/cofh/core/item/tool/ItemSickleCore.java
    @Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) 
    {
		World world = player.world;
		IBlockState state = world.getBlockState(pos);

		if (!canHarvestBlock(state, stack))
        {			
            if (!player.capabilities.isCreativeMode)
            {				
                stack.damageItem(1, player);
			}
			return false;
		}
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		int used = 0;
//world.playEvent(2001, pos, Block.getStateId(state));
		if (player.isSneaking()) 
        {
			if (!player.capabilities.isCreativeMode)
            {				
                stack.damageItem(1, player);
			}
			return false;
		}
//7x3x7
		for (int i = x - 3; i <= x + 3; i++)
        {			
            for (int j = z - 3; j <= z + 3; j++)
            {				
                for (int k = y - 1; k <= y + 1; k++)
                {					
                    if (harvestBlock(world, new BlockPos(i, k, j), player)) { used++; }
				}
			}
		}
		if (used > 0 && !player.capabilities.isCreativeMode)
        {			
            stack.damageItem(used, player);
		}

		return false;
	}
    

    //Sickle logic uses it so copied it too, still from CoFH Core
    //https://github.com/CoFH/CoFHCore/blob/1.12/src/main/java/cofh/core/item/tool/ItemToolCore.java
    protected boolean harvestBlock(World world, BlockPos pos, EntityPlayer player) 
    {
		if (world.isAirBlock(pos)) 
        {
			return false;
		}


		EntityPlayerMP playerMP = null;
		if (player instanceof EntityPlayerMP) 
        {
			playerMP = (EntityPlayerMP) player;
		}


		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

//Only effective materials
		if (!canHarvestBlock(state, player.getHeldItemMainhand())) 
        {
			return false;
		}
		if (!ForgeHooks.canHarvestBlock(block, player, world, pos)) 
        {
			return false;
		}
//Send the blockbreak event
		int xpToDrop = 0;
		if (playerMP != null) 
        {
			xpToDrop = ForgeHooks.onBlockBreakEvent(world, playerMP.interactionManager.getGameType(), playerMP, pos);
			if (xpToDrop == -1) 
            {
				return false;
			}
		}
		if (!world.isRemote)
        {			
            if (block.removedByPlayer(state, world, pos, player, !player.capabilities.isCreativeMode))
            {				
                block.onPlayerDestroy(world, pos, state);
				
                if (!player.capabilities.isCreativeMode)
                {					
                    block.harvestBlock(world, player, pos, state, world.getTileEntity(pos), player.getHeldItemMainhand());
					
                    if (xpToDrop > 0)
                    {						
                        block.dropXpOnBlockBreak(world, pos, xpToDrop);
					}
				}
			}
//Always send block update to client
			playerMP.connection.sendPacket(new SPacketBlockChange(world, pos));
		} 
        else 
        {
			if (block.removedByPlayer(state, world, pos, player, !player.capabilities.isCreativeMode)) 
            {
				block.onPlayerDestroy(world, pos, state);
			}
			Minecraft.getMinecraft().getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, Minecraft.getMinecraft().objectMouseOver.sideHit));
		}
		return true;
	}
    

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.enchantment.Enchantment enchantment) 
    {
		if (enchantment == Enchantments.FORTUNE) { return true; }
		    else { return super.canApplyAtEnchantingTable(stack, enchantment); }
	}




//AoE on hitting mobs
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker)
    {
        stack.damageItem(1, attacker);


//If attacker is player
        if(attacker instanceof EntityPlayer)
        {
//Get player
            EntityPlayer player = (EntityPlayer) attacker;
//Get attack charge
            float attackCharge = PlayerMeleeUtil.getLatestCharge(player);
//Get is crit
            boolean isCrit = PlayerMeleeUtil.isCriticalHit(player, target);
            float critMult = isCrit ? 1.5F : 1.0F;
//Get base damage
            float baseDamage = (float) attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
//Multiply base damage by charge and crit, and halve for AoE
            float spreadDamage = baseDamage * attackCharge * critMult * 0.5F;


//Crisper and louder sound for crit
            if(isCrit)
            {
//Make sound packet
                SPacketSoundEffect critScytheSound = new SPacketSoundEffect
                    (EternityModeSoundEvents.ENTITY_SCYTHE_SWING, SoundCategory.PLAYERS, 
                    player.posX, player.posY, player.posZ, 6.0F, 1.0F);
//Send it
                PacketUtil.sendPacketToNearbyPlayers(player.world, player.posX, player.posY, player.posZ, 32.0D, 
                    critScytheSound);
/*
                PlayerEffectsUtil.sendPacketToNearSound(player.world, player.posX, player.posY, player.posZ, 32.0D,
                    EternityModeSoundEvents.ENTITY_SCYTHE_SWING, SoundCategory.PLAYERS, attackCharge * 6.0F, 1.0F); 
*/        
            }
//Normal sound
            else
            {
                SPacketSoundEffect normalScytheSound = new SPacketSoundEffect
                    (EternityModeSoundEvents.ENTITY_SCYTHE_SWING, SoundCategory.PLAYERS, 
                    player.posX, player.posY, player.posZ, 4.5F, 1.5F);
//Send it
                PacketUtil.sendPacketToNearbyPlayers(player.world, player.posX, player.posY, player.posZ, 32.0D, 
                    normalScytheSound);
/*
                PlayerEffectsUtil.sendPacketToNearSound(player.world, player.posX, player.posY, player.posZ, 32.0D,
                    EternityModeSoundEvents.ENTITY_SCYTHE_SWING, SoundCategory.PLAYERS, attackCharge * 4.5F, 1.5F);
*/    
            }


//Get player reach and increase
            double grownReach = 6.0D + player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
//Get near entities
            List<Entity> nearEntities 
                = attacker.world.getEntitiesWithinAABBExcludingEntity(attacker, attacker.getEntityBoundingBox().grow(grownReach, grownReach, grownReach));
//For each one
            for(Entity nearEntity : nearEntities)
            {
//If a hostile mob and not original target
                if((nearEntity instanceof EntityMob && !((IMixinEntityMob) nearEntity).isTamed()) 
                || (nearEntity instanceof IMob && !(nearEntity instanceof EntityMob)))
                {
//Attack it (if not original target)
		            if(nearEntity != target) { nearEntity.attackEntityFrom(DamageSource.causePlayerDamage(player), spreadDamage); }
//And fling away
                    double entityDistHorizontal = Math.sqrt
                    (Math.pow(nearEntity.posX - attacker.posX, 2) + Math.pow(nearEntity.posZ - attacker.posZ, 2));

                    nearEntity.motionX += 2.0D * attackCharge * critMult * (nearEntity.posX - attacker.posX) / entityDistHorizontal;
                    nearEntity.motionY += attackCharge * critMult * 0.5D;
                    nearEntity.motionZ += 2.0D * attackCharge * critMult * (nearEntity.posZ - attacker.posZ) / entityDistHorizontal;
//And particle effect at respective entity edge
                    for (int partAt = 0; partAt < 8; partAt++)
                    {
                        float range = 1.777F;
//Get angle to player
                        double radiansToPlayer = Math.atan2(attacker.posZ - nearEntity.posZ, attacker.posX -  nearEntity.posX);
                        double offsetX = Math.cos(radiansToPlayer) * ((nearEntity.width / 2.0D) + 0.0D);
                        double offsetZ = Math.sin(radiansToPlayer) * ((nearEntity.width / 2.0D) + 0.0D);
//Get entity edge pointing to player
                        double edgeX = nearEntity.posX + offsetX + (attacker.world.rand.nextFloat() * range - attacker.world.rand.nextFloat() * range);
                        double edgeY 
                            = nearEntity.posY + (nearEntity.height / 2.0D) + (attacker.world.rand.nextFloat() * range - attacker.world.rand.nextFloat() * range) / 2.0D;
                        double edgeZ = nearEntity.posZ + offsetZ + (attacker.world.rand.nextFloat() * range - attacker.world.rand.nextFloat() * range);

                        DeeperDepths.proxy.spawnParticle(6, player.world, edgeX, edgeY, edgeZ, 0, 0, 0);
                    }
                }
            }
        }


        return true;
    }


//Trying to implement charge attack
    @Override
    public int getMaxItemUseDuration(ItemStack stack) 
    {
        return 72000; 
    }

	@Override
	public EnumAction getItemUseAction(ItemStack stack) 
    {
		return EnumAction.BOW;
	}

//On right click
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) 
    {
//Set hand active
		playerIn.setActiveHand(handIn);
//Get this itemstack
		ItemStack stack = playerIn.getHeldItem(handIn);
//And set action successful
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) 
    {
//If hand active and hand is this item stack 
		if (entityIn instanceof EntityLivingBase && ((EntityLivingBase) entityIn).isHandActive() && ((EntityLivingBase) entityIn).getActiveItemStack() == stack) 
        {
//If has been used for 20 ticks exactly
			if(((EntityLivingBase)entityIn).getItemInUseMaxCount() == 20)
            {
//Particles
                for (int particleAt = 0; particleAt < 1000; ++particleAt)
                {
                    entityIn.world.spawnParticle(EnumParticleTypes.PORTAL, 
                    entityIn.posX + (entityIn.world.rand.nextDouble() - 0.5D) * 6.0D, 
                    entityIn.posY + (entityIn.world.rand.nextDouble() - 0.5D) * 6.0D, 
                    entityIn.posZ + (entityIn.world.rand.nextDouble() - 0.5D) * 6.0D, 
                    (entityIn.world.rand.nextDouble() - 0.5D) * 2.0D, 
                    -entityIn.world.rand.nextDouble(), 
                    (entityIn.world.rand.nextDouble() - 0.5D) * 2.0D);
                }
//Warning sound
                entityIn.world.playSound(null, entityIn.posX, entityIn.posY, entityIn.posZ,
                    EternityModeSoundEvents.ENTITY_SCYTHE_DEMON, SoundCategory.PLAYERS, 6.0F, 1.0F);  
            }
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) 
    {
		if (entityLiving instanceof EntityPlayer) 
        {
			EntityPlayer player = (EntityPlayer) entityLiving;


			if (!worldIn.isRemote) 
            {

//Uses countdown btw
                int usedTicks = getMaxItemUseDuration(stack) - timeLeft;
    
                if (usedTicks >= 20) 
                {
				    Vec3d playerEyesFullTick = player.getPositionEyes(1.0F);
				    Vec3d playerLookDir = player.getLookVec();


                    player.world.playSound(null, player.posX, player.posY, player.posZ,
                        EternityModeSoundEvents.ENTITY_SCYTHE_SWING, SoundCategory.PLAYERS, 6.0F, 1.0F);


                    EntityDemonScythe demonScythe = new EntityDemonScythe
                    (
                        player.world, playerEyesFullTick.x, playerEyesFullTick.y, playerEyesFullTick.z, 
                        player,
                        200
                    );
                    float meleeStrength = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
//20-40 ticks but 20-30 effective
                    float effectiveChargeTicks = 20.0F + (0.5F * Math.min(usedTicks - 20, 20));
                    demonScythe.damageVal = meleeStrength * (0.08F * effectiveChargeTicks);
                    demonScythe.setMovement(playerLookDir.x, playerLookDir.y, playerLookDir.z, 
                        0.0D, false, 1.02D);
                    demonScythe.rotationYaw = player.rotationYaw;          
                    demonScythe.prevRotationYaw = demonScythe.rotationYaw;
                    demonScythe.rotationPitch = player.rotationPitch;      
                    demonScythe.prevRotationPitch = demonScythe.rotationPitch;

                    player.world.spawnEntity(demonScythe);
                }
            }
            else
            {
				player.swingArm(EnumHand.MAIN_HAND);
            }
        }
	}
}
