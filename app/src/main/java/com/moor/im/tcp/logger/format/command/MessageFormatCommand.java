/*
 * Copyright 2008 The Microlog project @sourceforge.net
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moor.im.tcp.logger.format.command;

import com.moor.im.tcp.logger.Level;

/**
 * Convert the logged message.
 *
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 */
public class MessageFormatCommand implements FormatCommandInterface {
	
	
	
	/**
	 * @see FormatCommandInterface#init(String)
	 */
	public void init(String initString) {
		// Do nothing on purpose.
	}

	/**
	 * @see FormatCommandInterface#execute(String, String, long, Level, Object, Throwable)
	 */
	public String execute(String clientID, String name, long time, Level level, Object message, Throwable throwable) {
	
		String convertedData = "";
		if (message != null) {
			convertedData = message.toString();
		}
		
		return convertedData;
	}
	
}
