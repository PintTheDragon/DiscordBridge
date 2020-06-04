package com.PintTheDragon.DiscordBridge;

import java.util.logging.*;

public class ConsoleHandler {

	public ConsoleHandler(DiscordBridge p) {
		Formatter f = new SimpleFormatter();
		Logger.getLogger("Minecraft").addHandler(new Handler() {

			@Override
			public void close() throws SecurityException {
				// TODO Auto-generated method stub

			}

			@Override
			public void flush() {
				// TODO Auto-generated method stub

			}

			@Override
			public void publish(LogRecord arg0) {
				// TODO Auto-generated method stub
				if (p.sm.login)
					p.sm.write("SENDAD " + f.format(arg0));

			}

		});
	}
}
