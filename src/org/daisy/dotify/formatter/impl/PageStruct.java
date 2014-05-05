package org.daisy.dotify.formatter.impl;






/**
 * Provides a page oriented structure
 * @author Joel Håkansson
 */
interface PageStruct {

	/**
	 * Gets the contents
	 * @return returns the content
	 */
	public Iterable<? extends PageSequence> getContents();

}