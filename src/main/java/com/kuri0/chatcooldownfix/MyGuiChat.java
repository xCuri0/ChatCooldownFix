package com.kuri0.chatcooldownfix;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class MyGuiChat extends GuiChat {

	public MyGuiChat(String defaultText)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		ReflectionHelper.setPrivateValue(GuiChat.class, this, defaultText, "defaultInputFieldText", "field_146409_v");
	}

	@Override
	public void sendChatMessage(String msg) {
		if (net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer,
				msg) != 0) {
			Minecraft.getMinecraft().ingameGUI.getChatGUI().addToSentMessages(msg);
			return;
		}
		com.kuri0.chatcooldownfix.ChatCooldownFix.queue.add(msg);
		synchronized (com.kuri0.chatcooldownfix.ChatCooldownFix.queue) {
			com.kuri0.chatcooldownfix.ChatCooldownFix.queue.notify();
		}
	}
}
