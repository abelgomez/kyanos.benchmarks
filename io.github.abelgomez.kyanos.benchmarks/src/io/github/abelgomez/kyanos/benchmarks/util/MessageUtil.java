/*******************************************************************************
 * Copyright (c) 2014 Abel G�mez.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Abel G�mez - initial API and implementation
 ******************************************************************************/
package io.github.abelgomez.kyanos.benchmarks.util;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

public class MessageUtil {
	
	public static void showError(String message) {
		System.err.println(message);
	}
	
	public static String formatMillis(long millis) {
	return String.format("%02d:%02d:%02d", 
		    TimeUnit.MILLISECONDS.toHours(millis),
		    TimeUnit.MILLISECONDS.toMinutes(millis) - 
		    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
		    TimeUnit.MILLISECONDS.toSeconds(millis) - 
		    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
	}
	
	public static String byteCountToDisplaySize(long size) {
		int MB = 1024 * 1024;
		return MessageFormat.format("{0} MB", size / MB);
	}
}
