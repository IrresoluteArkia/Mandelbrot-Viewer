package com.irar.mbviewer.external;

import com.irar.mbviewer.util.MBInfo;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.arikia.dev.drpc.DiscordEventHandlers.Builder;

public class DiscordHandler {
	
	private static boolean ready = false;
	private static MBInfo info;

	public static void setup() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			DiscordRPC.discordShutdown();
		}));
		DiscordEventHandlers handlers = new Builder().setReadyEventHandler((e) -> {
			ready = true;
			if(info != null) {
				createNewPresence(info);
			}
			System.out.println("Discord ready");
		}).setErroredEventHandler((e, e2) -> {
			System.out.println("Discord error");
		}).build();
		DiscordRPC.discordInitialize("579475848176992293", handlers, false);
		DiscordRPC.discordRegister("579475848176992293", "");
		new Thread(() -> {
			while(true) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				DiscordRPC.discordRunCallbacks();
			}
		}, "discord update thread").start();
	}

	public static void createNewPresence(MBInfo info) {
		if(!ready) {
			DiscordHandler.info = info;
			return;
		}
		DiscordRichPresence rich = new DiscordRichPresence.Builder("Current zoom scale: " + -info.getZoom().size)
				.setDetails("Exploring the Mandelbrot Set")
				.build();
		DiscordRPC.discordUpdatePresence(rich);
	}

}
