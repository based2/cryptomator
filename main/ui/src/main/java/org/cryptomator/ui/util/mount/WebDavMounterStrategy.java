/*******************************************************************************
 * Copyright (c) 2014 Markus Kreusch
 * This file is licensed under the terms of the MIT license.
 * See the LICENSE.txt file for more info.
 * 
 * Contributors:
 *     Markus Kreusch - Refactored WebDavMounter to use strategy pattern
 *     Sebastian Stenzel - minor strategy fine tuning
 ******************************************************************************/
package org.cryptomator.ui.util.mount;


/**
 * A strategy able to mount a webdav share and display it to the user.
 * 
 * @author Markus Kreusch
 */
interface WebDavMounterStrategy {

	/**
	 * @return {@code false} if this {@code WebDavMounterStrategy} can not work on the local machine, {@code true} if it could work
	 */
	boolean shouldWork();

	/**
	 * Tries to mount a given webdav share.
	 * 
	 * @param localPort local TCP port of the webdav share
	 * @return a {@link WebDavMount} representing the mounted share
	 * @throws CommandFailedException if the mount operation fails
	 */
	WebDavMount mount(int localPort) throws CommandFailedException;

}
