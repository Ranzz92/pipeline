package org.daisy.dotify.engine.impl;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Ignore;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class RowByRowTest extends AbstractFormatterEngineTest {

	@Test
	@Ignore("There is still an issue with current page.")
	public void testCurrentPage() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/current-page-input.obfl", "resource-files/current-page-expected.pef", false);
	}

}
