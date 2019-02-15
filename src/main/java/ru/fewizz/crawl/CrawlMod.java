package ru.fewizz.crawl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.fabricmc.fabric.impl.network.ServerSidePacketRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class CrawlMod implements ModInitializer, ClientModInitializer {
    static final Logger LOGGER = LogManager.getLogger("theotherside");
    static final Identifier CRAWL_IDENTIFIER = Identifier.create("mod_crawl");

    public void onInitializeClient() {
    	Client.init();
    }
    
	@Override
	public void onInitialize() {
        ServerSidePacketRegistryImpl.INSTANCE.register(CRAWL_IDENTIFIER, (context, buf) -> {
        	boolean val = buf.readByte() == 1;
        	Shared.trySetPlayerCrawling(context.getPlayer(), val);
        });
	}
	
	public static class Shared {
		public static final TrackedData<Boolean> IS_CRAWLING = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
		
		public static boolean isPlayerCrawling(PlayerEntity player) {
	        return player.getDataTracker().get(IS_CRAWLING);
	    }
		
		public static boolean trySetPlayerCrawling(PlayerEntity player, boolean b) {
			if(b && (player.isSwimming() || player.isFallFlying())) {
				return false;
			}
			if(isPlayerCrawling(player) && !b && !player.getEntityWorld().isEntityColliding(
					player, player.getBoundingBox().expand(0, 0.6F, 0).offset(0, 0.6F, 0)
				)
			) {
				return false;
			}
			player.setSprinting(false);
	    	player.getDataTracker().set(IS_CRAWLING, b);
	    	return true;
	    }
	}
	
	public static class Client {
		public static FabricKeyBinding keyCrawl;
		
		public static void init() {
			keyCrawl =
					FabricKeyBinding.Builder.create(
							new Identifier("crawl:key"),
							InputUtil.Type.KEY_KEYBOARD,
							GLFW.GLFW_KEY_V,
							"key.categories.movement"
					).build();
			KeyBindingRegistryImpl.INSTANCE.register(keyCrawl);
		}
		
		public static void logic(ClientPlayerEntity player) {
			if(!Shared.isPlayerCrawling(player) && Client.keyCrawl.isPressed()) {
    			MinecraftClient.getInstance().getNetworkHandler().sendPacket(
    				new CustomPayloadC2SPacket(CRAWL_IDENTIFIER, new PacketByteBuf(Unpooled.wrappedBuffer(new byte[] {1}))));
    		}
    		else if(Shared.isPlayerCrawling(player) && !Client.keyCrawl.isPressed()) {
    			MinecraftClient.getInstance().getNetworkHandler().sendPacket(
    				new CustomPayloadC2SPacket(CRAWL_IDENTIFIER, new PacketByteBuf(Unpooled.wrappedBuffer(new byte[] {0}))));
    		}
			
			if(Shared.isPlayerCrawling(player)) {
				player.setSprinting(false);
				player.input.field_3905*=0.25F;
				player.input.field_3907*=0.25F;
			}
		}
		
		static float func(double rad) {
			rad = rad % (Math.PI * 2);
			if(rad <= Math.PI / 2.0)
				return (float) Math.cos(rad*2.0);
			return (float)(
				-Math.cos((rad - Math.PI / 2.0) * (2.0 / 3.0))
			);
		}
		
		@SuppressWarnings("rawtypes")
		public static void postTransformModel(BipedEntityModel model, LivingEntity e, float dist) {
			if(!(e instanceof PlayerEntity))
				return;
			PlayerEntity player = (PlayerEntity)e;
			if(Shared.isPlayerCrawling(player)) {
				model.body.pitch = 0.0F;
		        model.legRight.rotationPointZ = 0.1F;
		        model.legLeft.rotationPointZ = 0.1F;
		        model.legRight.rotationPointY = 12.0F;
		        model.legLeft.rotationPointY = 12.0F;
		        model.head.rotationPointY = 0.0F;
		        
		        float yOffset = 20 + (e.isSneaking() ? -((0.125F + 0.2F) * 16.0F) : 0);
		        float as = 1.2F;

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
		        model.legLeft.rotationPointY = yOffset;
		        model.legLeft.pitch = (float) (Math.PI / 2);
		        model.legLeft.yaw = (float) (Math.cos(dist * as) + .7F) / 3F;
		        
		        model.legRight.rotationPointX = -1.9F + -(float)Math.sin(dist * as);
		        model.legRight.rotationPointZ = 6 + (float) -(Math.cos(dist * as) + 1)*2;
		        model.legRight.rotationPointY = yOffset;
		        model.legRight.pitch = (float) (Math.PI / 2);
		        model.legRight.yaw = (float) (Math.sin(dist * as) - .7F) / 3F;
		        
		        model.armLeft.rotationPointX = 6;
		        model.armLeft.rotationPointY = 2 + yOffset;
		        model.armLeft.rotationPointZ = -4 + -2 + (float) Math.cos(dist * as)*3;
		        if(!player.isUsingItem()) {
		        	model.armLeft.roll = (float) (-Math.PI / 2);
		        	model.armLeft.yaw = 0;
		        	model.armLeft.pitch = -1.3F + (float) func(dist * as + Math.PI / 2.0);
		        }
		        
		        model.armRight.rotationPointX = -6;
		        model.armRight.rotationPointY = 2 + yOffset;
		        model.armRight.rotationPointZ = -4 + -2 + (float) Math.sin(dist*as)*3;
		        if(!player.isUsingItem()) {
		        	model.armRight.roll = (float) (Math.PI / 2 + 0.2);
		        	model.armRight.yaw = 0;
		        	model.armRight.pitch = -1.3F + (float) func(dist * as - Math.PI / 2.0);
		        }

			}
			else {
				model.head.rotationPointY = 0;
		        model.head.rotationPointZ = 0;
		        model.head.rotationPointX = 0;
		        
		        model.body.roll = 0;
				model.body.rotationPointY = 0;
		        model.body.rotationPointZ = 0;
		        model.body.rotationPointX = 0;
		        
		        model.armLeft.rotationPointY = 2;
		        model.armRight.rotationPointY = 2;
			}
		}
	}
}
