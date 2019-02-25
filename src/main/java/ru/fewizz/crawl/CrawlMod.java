package ru.fewizz.crawl;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.fabricmc.fabric.impl.network.ServerSidePacketRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BoundingBox;

public class CrawlMod implements ModInitializer {
    public static final Identifier CRAWL_IDENTIFIER = Identifier.create("mod_crawl");
    
	@Override
	public void onInitialize() {
		registerListener();
	}
	
	void registerListener() {
		ServerSidePacketRegistryImpl.INSTANCE.register(CRAWL_IDENTIFIER, (context, buf) -> {
			boolean val = buf.readByte() == 1;
			context.getTaskQueue().execute(() -> {
				Shared.trySetCrawling(context.getPlayer(), val);
			});
		});
	}
	
	public static class Shared {
		public static final EntityPose CRAWLING = Enum.valueOf(EntityPose.class, "CRAWLING");//EnumHack.addEnum(EntityPose.class, "CRAWLING");
		public static final EntitySize CRAWLING_SIZE = new EntitySize(0.6F, 0.6F, false);
		public static final TrackedData<Boolean> IS_CRAWLING = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
		
		public static boolean isCrawling(PlayerEntity player) {
	        return player.getDataTracker().get(IS_CRAWLING);
	    }
		
		public static void setCrawling(PlayerEntity player, boolean b) {
			player.getDataTracker().set(IS_CRAWLING, b);
		}
		
		public static void setCrawlingForce(PlayerEntity player) {
			player.setSneaking(false);
			player.setSprinting(false);
			setCrawling(player, true);
		}
		
		public static boolean trySetCrawling(PlayerEntity player, boolean b) {
			if(b && !canCrawl(player))
				return false;
			if(!b && shouldCrawl(player))
				return false; 
			if(b) {
				player.setSneaking(false);
				player.setSprinting(false);
			}
	    	setCrawling(player, b);
	    	return true;
	    }
		
		public static boolean canCrawl(PlayerEntity p) {
			return !p.isSwimming() && !p.isFallFlying() && p.onGround;
		}
		
		public static boolean shouldCrawl(PlayerEntity p) {
			BoundingBox bb = p.getBoundingBox();
			if(isCrawling(p))
				bb = bb.expand(0, 0.6F, 0).offset(0, 0.6F, 0);
			
			if(!p.getEntityWorld().isEntityColliding(p, bb))
				return true;
			return false;
		}
	}
	
	@Environment(EnvType.CLIENT)
	public static class Client implements ClientModInitializer {
		public static FabricKeyBinding keyCrawl;
		
		@Override
		public void onInitializeClient() {
			keyCrawl =
					FabricKeyBinding.Builder.create(
							new Identifier("crawl:key"),
							InputUtil.Type.KEY_KEYBOARD,
							GLFW.GLFW_KEY_C,
							"key.categories.movement"
					).build();
			KeyBindingRegistryImpl.INSTANCE.register(keyCrawl);
		}
		
		static float func(double rad) {
			rad = rad % (Math.PI * 2);
			if(rad <= Math.PI / 2.0)
				return (float) Math.cos(rad*2.0);
			return (float)(
				-Math.cos((rad - Math.PI / 2.0) * (2.0 / 3.0))
			);
		}
		
		// That's bad for comp. with other mods, temp. solution..
		public static <E extends LivingEntity> void postTransformModel(PlayerEntityModel<E> model, LivingEntity e, float dist) {
			PlayerEntity player = (PlayerEntity)e;
			
			MinecraftClient mc = MinecraftClient.getInstance();
			
			if((player == mc.player && mc.options.perspective == 0) || !Shared.isCrawling(player)) {
				tryRestorePlayerModel(model);
				return;
			}
			
		    float yOffset = 20;
		    float as = 1F;

		    model.head.rotationPointY = yOffset;
		    model.head.rotationPointZ = -6;
		    model.head.rotationPointX = 0;
		    
		    model.body.rotationPointY = yOffset;
		    model.body.rotationPointZ = -6;
		    model.body.rotationPointX = 0;
		    model.body.pitch = (float) (Math.PI / 2);
		    model.body.yaw = (float) -(Math.sin(dist * as)) / 10F;
		    model.body.roll = (float) -(Math.sin(dist * as)) / 5F;
		    
		    model.legLeft.rotationPointX = 1.9F + -(float)Math.sin(dist * as);
		    model.legLeft.rotationPointZ = 6 + (float) -(Math.sin(dist * as) + 1)*2;
		    model.legLeft.rotationPointY = yOffset + 0.2F;
		    model.legLeft.pitch = (float) (Math.PI / 2);
		    model.legLeft.yaw = (float) (Math.cos(dist * as) + .7F) / 3F;
		    
		    model.legRight.rotationPointX = -1.9F + -(float)Math.sin(dist * as);
		    model.legRight.rotationPointZ = 6 + (float) -(Math.cos(dist * as) + 1)*2;
		    model.legRight.rotationPointY = yOffset + 0.2F;
		    model.legRight.pitch = (float) (Math.PI / 2);
		    model.legRight.yaw = (float) (Math.sin(dist * as) - .7F) / 3F;
		    
		    model.armLeft.rotationPointX = 5;
		    model.armLeft.rotationPointY = 2 + yOffset;
		    model.armLeft.rotationPointZ = -4 + -2 + (float) Math.cos(dist * as)*3;

		    model.armRight.rotationPointX = -5;
		    model.armRight.rotationPointY = 2 + yOffset;
		    model.armRight.rotationPointZ = -4 + -2 + (float) Math.sin(dist*as)*3;
		    
		    if(player.isUsingItem())
		    	return;
		    
		    if(model.swingProgress <= 0 || player.preferredHand != Hand.OFF) {
		    	model.armLeft.roll = (float) (-Math.PI / 2);
		    	model.armLeft.yaw = 0;
		    	model.armLeft.pitch = -1.3F + (float) func(dist * as + Math.PI / 2.0);
		    }
		    if(model.swingProgress <= 0 || player.preferredHand != Hand.MAIN) {
		    	model.armRight.roll = (float) (Math.PI / 2);
		    	model.armRight.yaw = 0;
		    	model.armRight.pitch = -1.3F + (float) func(dist * as - Math.PI / 2.0);
		    }
		}
		
		public static <E extends LivingEntity> void tryRestorePlayerModel(BipedEntityModel<E> model) {
			model.head.rotationPointY = 0;
	        model.head.rotationPointZ = 0;
	        model.head.rotationPointX = 0;
	        
	        model.body.roll = 0;
			model.body.rotationPointY = 0;
	        model.body.rotationPointZ = 0;
	        model.body.rotationPointX = 0;
	        
	        model.legLeft.rotationPointX = 1.9F;
	        model.legRight.rotationPointX = -1.9F;
	        
	        model.armLeft.rotationPointX = 5;
	        model.armLeft.rotationPointY = 2;
	        model.armLeft.rotationPointZ = 0;
	        
	        model.armRight.rotationPointX = -5;
	        model.armRight.rotationPointY = 2;
	        model.armRight.rotationPointZ = 0;
		}
	}
}
