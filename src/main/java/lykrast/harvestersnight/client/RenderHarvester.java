package lykrast.harvestersnight.client;


import lykrast.harvestersnight.common.EntityHarvester;
import lykrast.harvestersnight.common.HarvestersNight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import org.sporotofpoorety.eternitymode.client.LaserRenderer;




public class RenderHarvester extends RenderBiped<EntityHarvester> 
{
	public static final ResourceLocation TEXTURES = new ResourceLocation(HarvestersNight.MODID, "textures/entity/harvester.png"),
			EYES = new ResourceLocation(HarvestersNight.MODID, "textures/entity/harvester_eyes.png");

	public RenderHarvester(RenderManager rendermanagerIn) 
    {
		super(rendermanagerIn, new ModelHarvester(), 0.5F);
        addLayer(new LayerEyesHarvester(this));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityHarvester entity) 
    {
		return TEXTURES;
	}

//Testing clone
    public void doRender(EntityHarvester entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        Vec3d targetOffset = entity.getTargetPos();


        if (targetOffset.x < 3000.0D)
        {
/*
//Save original rotation
            float savedRenderYaw = entity.renderYawOffset;
            float savedPrevRenderYaw = entity.prevRenderYawOffset;
            float savedHeadYaw = entity.rotationYawHead;
            float savedPrevHeadYaw = entity.prevRotationYawHead;

//Flip clone rotation
            entity.renderYawOffset += 180.0F;
            entity.prevRenderYawOffset += 180.0F;
            entity.rotationYawHead += 180.0F;
            entity.prevRotationYawHead += 180.0F;

//Render clone
            super.doRender(entity, x + (targetOffset.x * 2.0D), y + (targetOffset.y * 2.0D), z + (targetOffset.z * 2.0D), entityYaw + 180.0F, partialTicks);

//Restore rotation
            entity.renderYawOffset = savedRenderYaw;
            entity.prevRenderYawOffset = savedPrevRenderYaw;
            entity.rotationYawHead = savedHeadYaw;
            entity.prevRotationYawHead = savedPrevHeadYaw;
*/


//TESTING LASER PLEASE WORK PLEASE
//          Minecraft.getMinecraft().getTextureManager().bindTexture(LaserRenderer.DEFAULT_BEAM_TEXTURE);
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("eternitymode:textures/lavamodern.png"));


            LaserRenderer.renderLaser
            (
                x, y, z,
                targetOffset.x, targetOffset.y, targetOffset.z,
                20.0D,
                2.0D,
                4.0D,
                2.0D, 2.5D,
                1.0F, 1.0F, 1.0F,
                0.125F,
                entity.world.getTotalWorldTime(),
                partialTicks,
                1.0D,
                40.0D,
                20.0D
            );

//Might need this extra cleanup
            this.setLightmap(entity);
        }
    }


/*
    protected boolean isVisible(EntityHarvester p_193115_1_)
    {
        return true;
    }
*/
}
