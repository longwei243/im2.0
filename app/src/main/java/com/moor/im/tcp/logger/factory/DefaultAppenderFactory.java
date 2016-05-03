package com.moor.im.tcp.logger.factory;

import com.moor.im.tcp.logger.appender.Appender;
import com.moor.im.tcp.logger.appender.LogCatAppender;
import com.moor.im.tcp.logger.format.PatternFormatter;

public enum DefaultAppenderFactory {
	;
	
	public static Appender createDefaultAppender() {
		Appender appender = new LogCatAppender();
		appender.setFormatter(new PatternFormatter());
		
		return appender;
	}
}
