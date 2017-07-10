package org.daisy.dotify.formatter.impl;

import java.util.HashMap;
import java.util.Map;

import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.LayoutMasterBuilder;
import org.daisy.dotify.api.formatter.LayoutMasterProperties;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;

/**
 * Provides formatter context data.
 * @author Joel Håkansson
 *
 */
public class FormatterContext extends FormatterCoreContext {
	private final Map<String, LayoutMaster> masters;
	private final Map<String, ContentCollectionImpl> collections;
	

	FormatterContext(BrailleTranslatorFactoryMakerService translatorFactory, TextBorderFactoryMakerService tbf, MarkerProcessorFactoryMakerService mpf, FormatterConfiguration config) {
		super(translatorFactory, tbf, config, mpf);
		this.masters = new HashMap<>();
		this.collections = new HashMap<>();
	}
	
	public LayoutMasterBuilder newLayoutMaster(String name, LayoutMasterProperties properties) {
		LayoutMaster master = new LayoutMaster(this, properties);
		masters.put(name, master);
		return master;
	}
	
	public ContentCollectionImpl newContentCollection(String collectionId) {
		ContentCollectionImpl collection = new ContentCollectionImpl(this);
		collections.put(collectionId, collection);
		return collection;
	}
	
	public Map<String, LayoutMaster> getMasters() {
		return masters;
	}
	
	public Map<String, ContentCollectionImpl> getCollections() {
		return collections;
	}
	
}
