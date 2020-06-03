package com.kuri0.chatcooldownfix;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@Mod(modid = ChatCooldownFix.MODID, version = ChatCooldownFix.VERSION)
public class ChatCooldownFix {
	public static final String MODID = "ChatCooldownFix";
	public static final String VERSION = "1.0";

	public static ArrayList<String> queue = new ArrayList<String>();

	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);

		// FIFO chat message queue thread
		(new Thread() {
			public void run() {
				while (true) {
					if (queue.size() == 0) {
						synchronized (com.kuri0.chatcooldownfix.ChatCooldownFix.queue) {
							try {
								com.kuri0.chatcooldownfix.ChatCooldownFix.queue.wait();
							} catch (InterruptedException e) { }
						}
					}

					Minecraft.getMinecraft().ingameGUI.getChatGUI().addToSentMessages(queue.get(0));
					Minecraft.getMinecraft().thePlayer.sendChatMessage(queue.get(0));

					queue.remove(0);
					try {
						Thread.sleep(3100);
					} catch (InterruptedException e) { }
				}
			}
		}).start();
	}

	// replace GUIChat with our own that has a modified sendChatMessage
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String name = "";
		try {
			name = event.gui.getClass().getName();
		} catch (NullPointerException e) {
			return;
		}

		if (name == "net.minecraft.client.gui.GuiChat") {
			String defaultInputFieldText = ReflectionHelper.getPrivateValue(GuiChat.class, (GuiChat) event.gui,
					"defaultInputFieldText", "field_146409_v");
			event.gui = new MyGuiChat(defaultInputFieldText);
		}
	}

	// needed so we dont get nullpointerexception after disconnecting
	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		queue.clear();
	}
}
