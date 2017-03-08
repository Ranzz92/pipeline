package org.daisy.dotify.formatter.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.daisy.dotify.api.formatter.BlockPosition;
import org.daisy.dotify.api.formatter.FallbackRule;
import org.daisy.dotify.api.formatter.MarginRegion;
import org.daisy.dotify.api.formatter.MarkerIndicatorRegion;
import org.daisy.dotify.api.formatter.PageAreaProperties;
import org.daisy.dotify.api.formatter.RenameFallbackRule;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.common.split.SplitPoint;
import org.daisy.dotify.common.split.SplitPointDataSource;
import org.daisy.dotify.common.split.SplitPointHandler;
import org.daisy.dotify.common.split.SplitPointSpecification;
import org.daisy.dotify.common.split.StandardSplitOption;
import org.daisy.dotify.common.split.Supplements;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.search.DocumentSpace;
import org.daisy.dotify.formatter.impl.search.PageDetails;
import org.daisy.dotify.formatter.impl.search.PageId;
import org.daisy.dotify.formatter.impl.search.SequenceId;

public class PageSequenceBuilder2 {
	private final FormatterContext context;
	private final CrossReferenceHandler crh;
	private final PageAreaContent staticAreaContent;
	private final PageAreaProperties areaProps;

	private ContentCollectionImpl collection;
	private final BlockContext blockContext;
	private final LayoutMaster master;
	private final int pageNumberOffset;
	private final List<RowGroupDataSource> dataGroups;
	private final FieldResolver fieldResolver;
	private final SequenceId seqId;
	
	private SplitPointHandler<RowGroup> sph = new SplitPointHandler<>();
	private boolean force;
	private SplitPointDataSource<RowGroup> data;

	PageImpl current;
	int keepNextSheets;
	int pageCount = 0;
	private int dataGroupsIndex;

	//From view, temporary
	private final int fromIndex;
	private int toIndex;
	
	public PageSequenceBuilder2(int fromIndex, LayoutMaster master, int pageOffset, CrossReferenceHandler crh,
	                     BlockSequence seq, FormatterContext context, DefaultContext rcontext, int sequenceId) {
		this.fromIndex = fromIndex;
		this.toIndex = fromIndex;
		this.master = master;
		this.pageNumberOffset = pageOffset;
		this.context = context;
		this.crh = crh;

		this.collection = null;
		this.areaProps = seq.getLayoutMaster().getPageArea();
		if (this.areaProps!=null) {
			this.collection = context.getCollections().get(areaProps.getCollectionId());
		}
		current = null;
		keepNextSheets = 0;
		
		this.blockContext = new BlockContext(seq.getLayoutMaster().getFlowWidth(), crh, rcontext, context);
		this.staticAreaContent = new PageAreaContent(seq.getLayoutMaster().getPageAreaBuilder(), blockContext);
		this.dataGroups = prepareResult(master, seq, blockContext, new CollectionData(blockContext));
		this.dataGroupsIndex = 0;
		this.seqId = new SequenceId(sequenceId, new DocumentSpace(blockContext.getContext().getSpace(), blockContext.getContext().getCurrentVolume()));
		PageDetails details = new PageDetails(master.duplex(), new PageId(pageCount, getGlobalStartIndex(), seqId), pageNumberOffset);
		this.fieldResolver = new FieldResolver(master, context, crh, details);
	}

	public PageSequenceBuilder2(PageSequenceBuilder2 template) {
		this.context = template.context;
		this.crh = template.crh;
		this.staticAreaContent = template.staticAreaContent;
		this.areaProps = template.areaProps;
		this.collection = template.collection; // Probably this doesn't have to be copied...
		this.blockContext = template.blockContext;
		this.master = template.master;
		this.pageNumberOffset = template.pageNumberOffset;
		this.dataGroups = template.dataGroups;
		this.dataGroupsIndex = template.dataGroupsIndex;
		this.fieldResolver = template.fieldResolver;
		this.seqId = template.seqId;
		this.sph = template.sph;
		this.force = template.force;
		this.data = RowGroupDataSource.copyUnlessNull((RowGroupDataSource)data);
		this.current = PageImpl.copyUnlessNull(template.current);
		this.keepNextSheets = template.keepNextSheets;
		this.pageCount = template.pageCount;
		this.fromIndex = template.fromIndex;
		this.toIndex = template.toIndex;
	}
	
	public static PageSequenceBuilder2 copyUnlessNull(PageSequenceBuilder2 template) {
		return template==null?null:new PageSequenceBuilder2(template);
	}

	static List<RowGroupDataSource> prepareResult(LayoutMaster master, BlockSequence in, BlockContext blockContext, CollectionData cd) {
		//TODO: This assumes that all page templates have margin regions that are of the same width  
		final BlockContext bc = new BlockContext(in.getLayoutMaster().getFlowWidth() - master.getTemplate(1).getTotalMarginRegionWidth(), blockContext.getRefs(), blockContext.getContext(), blockContext.getFcontext());
		return ScenarioProcessor.process(master, in, bc)
				.stream()
				.map(rgs -> new RowGroupDataSource(master, bc, rgs.getBlocks(), rgs.getVerticalSpacing(), cd))
				.collect(Collectors.toList());
	}

	private PageImpl newPage() {
		PageImpl buffer = current;
		PageDetails details = new PageDetails(master.duplex(), new PageId(pageCount, getGlobalStartIndex(), seqId), pageNumberOffset);
		current = new PageImpl(fieldResolver, details, master, context, staticAreaContent.getBefore(), staticAreaContent.getAfter());
		pageCount ++;
		if (keepNextSheets>0) {
			currentPage().setAllowsVolumeBreak(false);
		}
		if (!master.duplex() || pageCount%2==0) {
			if (keepNextSheets>0) {
				keepNextSheets--;
			}
		}
		return buffer;
	}

	private void setKeepWithPreviousSheets(int value) {
		currentPage().setKeepWithPreviousSheets(value);
	}

	private void setKeepWithNextSheets(int value) {
		keepNextSheets = Math.max(value, keepNextSheets);
		if (keepNextSheets>0) {
			currentPage().setAllowsVolumeBreak(false);
		}
	}
	
	private PageImpl currentPage() {
		return current;
	}

	/**
	 * Space used, in rows
	 * 
	 * @return
	 */
	private int spaceUsedOnPage(int offs) {
		return currentPage().spaceUsedOnPage(offs);
	}

	private void newRow(RowImpl row) {
		if (spaceUsedOnPage(1) > currentPage().getFlowHeight()) {
			throw new RuntimeException("Error in code.");
			//newPage();
		}
		currentPage().newRow(row);
	}

	private void insertIdentifier(String id) {
		crh.setPageNumber(id, currentPage().getPageIndex() + 1);
		currentPage().addIdentifier(id);
	}
	
	public boolean hasNext() {
		return dataGroupsIndex<dataGroups.size() || (data!=null && !data.isEmpty()) || current!=null;
	}
	
	public PageImpl nextPage() throws PaginatorException, RestartPaginationException // pagination must be restarted in PageStructBuilder.paginateInner
	{
		PageImpl ret = nextPageInner();
		crh.keepPageDetails(ret.getDetails());
		//This is for pre/post volume contents, where the volume number is known
		if (blockContext.getContext().getCurrentVolume()!=null) {
			for (String id : ret.getIdentifiers()) {
				crh.setVolumeNumber(id, blockContext.getContext().getCurrentVolume());
			}
		}
		toIndex++;
		return ret;
	}

	private PageImpl nextPageInner() throws PaginatorException, RestartPaginationException // pagination must be restarted in PageStructBuilder.paginateInner
	{
		while (dataGroupsIndex<dataGroups.size() || (data!=null && !data.isEmpty())) {
			if ((data==null || data.isEmpty()) && dataGroupsIndex<dataGroups.size()) {
				//pick up next group
				data = dataGroups.get(dataGroupsIndex);
				dataGroupsIndex++;
				if (((RowGroupDataSource)data).getVerticalSpacing()!=null) {
					VerticalSpacing vSpacing = ((RowGroupDataSource)data).getVerticalSpacing();
					if (pageCount==0) {
						// we know newPage returns null
						newPage();
					}
					float size = 0;
					for (RowGroup g : data.getRemaining()) {
						size += g.getUnitSize();
					}
					int pos = calculateVerticalSpace(vSpacing.getBlockPosition(), (int)Math.ceil(size));
					for (int i = 0; i < pos; i++) {
						RowImpl ri = vSpacing.getEmptyRow();
						newRow(new RowImpl(ri.getChars(), ri.getLeftMargin(), ri.getRightMargin()));
					}
				} else {
					PageImpl p = newPage();
					if (p!=null) {
						return p;
					}
				}
				force = false;
			}
			/*
			((RowGroupDataSource)data).setContext(blockContext.copyWithContext(
					DefaultContext.from(blockContext.getContext()).currentPage(currentPage().getDetails().getPageNumber()).build()));*/
			if (!data.isEmpty()) {
				data = discardSkippableLeading(data);
				int flowHeight = currentPage().getFlowHeight();
				// Using a copy to find the break point so that only the required data is rendered
				SplitPointSpecification spec = sph.find(flowHeight, new RowGroupDataSource((RowGroupDataSource)data), force?StandardSplitOption.ALLOW_FORCE:null);
				// Now apply the information to the live data
				SplitPoint<RowGroup> res = sph.split(spec, data);
				if (res.getHead().size()==0 && force) {
					if (firstUnitHasSupplements(data) && hasPageAreaCollection()) {
						reassignCollection();
						throw new RestartPaginationException();
					} else {
						throw new RuntimeException("A layout unit was too big for the page.");
					}
				}
				for (RowGroup rg : res.getSupplements()) {
					currentPage().addToPageArea(rg.getRows());
				}
				force = res.getHead().size()==0;
				data = res.getTail();
				List<RowGroup> head = res.getHead();
				for (RowGroup rg : head) {
					addProperties(rg);
					for (RowImpl r : rg.getRows()) { 
						if (r.shouldAdjustForMargin()) {
							// clone the row as not to append the margins twice
							r = new RowImpl(r);
							for (MarginRegion mr : currentPage().getPageTemplate().getLeftMarginRegion()) {
								r.setLeftMargin(getMarginRegionValue(mr, r, false).append(r.getLeftMargin()));
							}
							for (MarginRegion mr : currentPage().getPageTemplate().getRightMarginRegion()) {
								r.setRightMargin(r.getRightMargin().append(getMarginRegionValue(mr, r, true)));
							}
						}
						currentPage().newRow(r);
					}
				}
				Integer lastPriority = getLastPriority(head);
				if (!res.getDiscarded().isEmpty()) {
					//override if not empty
					lastPriority = getLastPriority(res.getDiscarded());
				}
				currentPage().setAvoidVolumeBreakAfter(lastPriority);
				for (RowGroup rg : res.getDiscarded()) {
					addProperties(rg);
				}
				if (hasPageAreaCollection() && currentPage().pageAreaSpaceNeeded() > master.getPageArea().getMaxHeight()) {
					reassignCollection();
					throw new RestartPaginationException();
				}
				if (!data.isEmpty()) {
					return newPage();
				}
			}
		}
		//flush current page
		PageImpl ret = current;
		current = null;
		return ret;
	}
	
	/**
	 * Discards leading skippable row groups, but retains their properties (via side effect).
	 * @param data the data
	 * @return returns the tail
	 */
	private SplitPointDataSource<RowGroup> discardSkippableLeading(SplitPointDataSource<RowGroup> data) {
		// Using a copy to find the skippable data, so that only the required data is rendered
		int index = SplitPointHandler.findLeading(new RowGroupDataSource((RowGroupDataSource)data));
		// Now apply the information to the live data
		SplitPoint<RowGroup> sl = SplitPointHandler.skipLeading(data, index);
		for (RowGroup rg : sl.getDiscarded()) {
			addProperties(rg);
		}
		return sl.getTail();
	}
	
	private static Integer getLastPriority(List<RowGroup> list) {
		if (!list.isEmpty()) {
			return list.get(list.size()-1).getAvoidVolumeBreakAfterPriority();
		} else {
			return null;
		}
	}
	
	private boolean firstUnitHasSupplements(SplitPointDataSource<RowGroup> spd) {
		return !spd.isEmpty() && !spd.get(0).getSupplementaryIDs().isEmpty();
	}
	
	private boolean hasPageAreaCollection() {
		return master.getPageArea()!=null && collection!=null;
	}
	
	private MarginProperties getMarginRegionValue(MarginRegion mr, RowImpl r, boolean rightSide) throws PaginatorException {
		String ret = "";
		int w = mr.getWidth();
		if (mr instanceof MarkerIndicatorRegion) {
			ret = firstMarkerForRow(r, (MarkerIndicatorRegion)mr);
			if (ret.length()>0) {
				try {
					ret = context.getDefaultTranslator().translate(Translatable.text(context.getConfiguration().isMarkingCapitalLetters()?ret:ret.toLowerCase()).build()).getTranslatedRemainder();
				} catch (TranslationException e) {
					throw new PaginatorException("Failed to translate: " + ret, e);
				}
			}
			boolean spaceOnly = ret.length()==0;
			if (ret.length()<w) {
				StringBuilder sb = new StringBuilder();
				if (rightSide) {
					while (sb.length()<w-ret.length()) { sb.append(context.getSpaceCharacter()); }
					sb.append(ret);
				} else {
					sb.append(ret);				
					while (sb.length()<w) { sb.append(context.getSpaceCharacter()); }
				}
				ret = sb.toString();
			} else if (ret.length()>w) {
				throw new PaginatorException("Cannot fit " + ret + " into a margin-region of size "+ mr.getWidth());
			}
			return new MarginProperties(ret, spaceOnly);
		} else {
			throw new PaginatorException("Unsupported margin-region type: " + mr.getClass().getName());
		}
	}
	
	private String firstMarkerForRow(RowImpl r, MarkerIndicatorRegion mrr) {
		return mrr.getIndicators().stream()
				.filter(mi -> r.hasMarkerWithName(mi.getName()))
				.map(mi -> mi.getIndicator())
				.findFirst().orElse("");
	}
	
	private void addProperties(RowGroup rg) {
		if (rg.getIdentifier()!=null) {
			insertIdentifier(rg.getIdentifier());
		}
		currentPage().addMarkers(rg.getMarkers());
		//TODO: addGroupAnchors
		setKeepWithNextSheets(rg.getKeepWithNextSheets());
		setKeepWithPreviousSheets(rg.getKeepWithPreviousSheets());
	}
	
	private void reassignCollection() throws PaginatorException {
		//reassign collection
		if (areaProps!=null) {
			int i = 0;
			for (FallbackRule r : areaProps.getFallbackRules()) {
				i++;
				if (r instanceof RenameFallbackRule) {
					collection = context.getCollections().remove(r.applyToCollection());
					if (context.getCollections().put(((RenameFallbackRule)r).getToCollection(), collection)!=null) {
						throw new PaginatorException("Fallback id already in use:" + ((RenameFallbackRule)r).getToCollection());
					}							
				} else {
					throw new PaginatorException("Unknown fallback rule: " + r);
				}
			}
			if (i==0) {
				throw new PaginatorException("Failed to fit collection '" + areaProps.getCollectionId() + "' within the page-area boundaries, and no fallback was defined.");
			}
		}
	}
	
	private class CollectionData implements Supplements<RowGroup> {
		private PageImpl page;
		private final BlockContext c;
		private final Map<String, RowGroup> map;
		
		private CollectionData(BlockContext c) {
			this.c = c;
			this.page = null;
			this.map = new HashMap<>();
		}

		@Override
		public RowGroup get(String id) {
			if (collection!=null) {
				if (page!=currentPage()) {
					map.clear();
				}
				RowGroup ret = map.get(id);
				if (ret==null) {
					RowGroup.Builder b = new RowGroup.Builder(master.getRowSpacing());
					for (Block g : collection.getBlocks(id)) {
						AbstractBlockContentManager bcm = g.getBlockContentManager(c);
						b.addAll(bcm.getCollapsiblePreContentRows());
						b.addAll(bcm.getInnerPreContentRows());
						for (int i=0; i<bcm.getRowCount(); i++) {
							b.add(bcm.get(i));
						}
						b.addAll(bcm.getPostContentRows());
						b.addAll(bcm.getSkippablePostContentRows());
					}
					if (page==null || page!=currentPage()) {
						page = currentPage();
						b.overhead(page.staticAreaSpaceNeeded());
					}
					ret = b.build();
					map.put(id, ret);
				} 
				return ret;
			} else {
				return null;
			}
		}
		
	}
	
	private int calculateVerticalSpace(BlockPosition p, int blockSpace) {
		if (p != null) {
			int pos = p.getPosition().makeAbsolute(currentPage().getFlowHeight());
			int t = pos - spaceUsedOnPage(0);
			if (t > 0) {
				int advance = 0;
				switch (p.getAlignment()) {
				case BEFORE:
					advance = t - blockSpace;
					break;
				case CENTER:
					advance = t - blockSpace / 2;
					break;
				case AFTER:
					advance = t;
					break;
				}
				return (int)Math.floor(advance / master.getRowSpacing());
			}
		}
		return 0;
	}

	/**
	 * Gets the layout master for this sequence
	 * @return returns the layout master for this sequence
	 */
	public LayoutMaster getLayoutMaster() {
		return master;
	}

	int getCurrentPageOffset() {
		if (getLayoutMaster().duplex() && (size() % 2)==1) {
			return pageNumberOffset + size() + 1;
		} else {
			return pageNumberOffset + size();
		}
	}
	
	private int size() {
		return getToIndex()-fromIndex;
	}

	/**
	 * Gets the index for the first item in this sequence, counting all preceding items in the document, zero-based. 
	 * @return returns the first index
	 */
	public int getGlobalStartIndex() {
		return fromIndex;
	}

	public int getToIndex() {
		return toIndex;
	}

}
