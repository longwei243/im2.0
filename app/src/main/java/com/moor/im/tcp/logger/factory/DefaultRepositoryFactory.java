package com.moor.im.tcp.logger.factory;

import com.moor.im.tcp.logger.repository.DefaultLoggerRepository;
import com.moor.im.tcp.logger.repository.LoggerRepository;

public enum DefaultRepositoryFactory {
	;
	
	public static LoggerRepository getDefaultLoggerRepository() {
		return DefaultLoggerRepository.INSTANCE;
	}
}
