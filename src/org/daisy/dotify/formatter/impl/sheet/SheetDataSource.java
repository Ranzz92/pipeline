package org.daisy.dotify.formatter.impl.sheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.daisy.dotify.api.writer.SectionProperties;
import org.daisy.dotify.common.split.SplitPointDataSource;
import org.daisy.dotify.common.split.SplitResult;
import org.daisy.dotify.common.split.Supplements;
import org.daisy.dotify.formatter.impl.BlockSequence;
import org.daisy.dotify.formatter.impl.FormatterContext;
import org.daisy.dotify.formatter.impl.PageImpl;
import org.daisy.dotify.formatter.impl.PageSequenceBuilder2;
import org.daisy.dotify.formatter.impl.PageStruct;
import org.daisy.dotify.formatter.impl.RestartPaginationException;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.search.DocumentSpace;
import org.daisy.dotify.formatter.impl.search.SheetIdentity;

public class SheetDataSource implements SplitPointDataSource<Sheet> {
	private final PageStruct struct;
	private final CrossReferenceHandler crh;
	private final FormatterContext context;
	private final DefaultContext rcontext;
	private final List<BlockSequence> seqsIterator;
	private final int sheetsServed;
	private int seqsIndex;
	private PageSequenceBuilder2 psb;
	private SectionProperties sectionProperties;
	private Sheet.Builder s;
	private SheetIdentity si;
	private int sheetIndex;
	private int pageIndex;
	
	private List<Sheet> sheetBuffer;
	private boolean volBreakAllowed;

	public SheetDataSource(PageStruct struct, CrossReferenceHandler crh, FormatterContext context, DefaultContext rcontext, List<BlockSequence> seqsIterator) {
		this(struct, crh, context, rcontext, seqsIterator, new ArrayList<>(), true, 0, 0, null, null, null, null, 0, 0);
	}
	
	public SheetDataSource(SheetDataSource template) {
		this.struct = new PageStruct(template.struct);
		this.crh = template.crh;
		this.context = template.context;
		this.rcontext = template.rcontext;
		this.seqsIterator = template.seqsIterator;
		this.seqsIndex = template.seqsIndex;
		this.psb = PageSequenceBuilder2.copyUnlessNull(template.psb);
		this.sectionProperties = template.sectionProperties;
		this.s = Sheet.Builder.copyUnlessNull(template.s);
		this.si = template.si;
		this.sheetsServed = template.sheetsServed;
		this.sheetIndex = template.sheetIndex;
		this.pageIndex = template.pageIndex;
		this.sheetBuffer = new ArrayList<>(template.sheetBuffer);
		this.volBreakAllowed = template.volBreakAllowed;
	}
	
	SheetDataSource(PageStruct struct, CrossReferenceHandler crh, FormatterContext context, DefaultContext rcontext, List<BlockSequence> seqsIterator, List<Sheet> sheetBuffer, boolean volBreakAllowed, int sheetsServed, int seqsIndex,
			PageSequenceBuilder2 psb,
			SectionProperties sectionProperties,
			Sheet.Builder s,
			SheetIdentity si,
			int sheetIndex,
			int pageIndex) {
		this.struct = struct;
		this.crh = crh;
		this.context = context;
		this.rcontext = rcontext;
		this.seqsIterator = seqsIterator;
		this.sheetsServed = sheetsServed;
		this.seqsIndex = seqsIndex;
		this.psb = psb;
		this.sectionProperties = sectionProperties;
		this.s = s;
		this.si = si;
		this.sheetIndex = sheetIndex;
		this.pageIndex = pageIndex;
		this.sheetBuffer = sheetBuffer;
		this.volBreakAllowed = volBreakAllowed;
	}

	@Override
	public Sheet get(int index) throws RestartPaginationException {
		if (!ensureBuffer(index)) {
			throw new IndexOutOfBoundsException("" + index);
		}
		return sheetBuffer.get(index);
	}

	@Override
	public List<Sheet> head(int toIndex) throws RestartPaginationException {
		if (toIndex==0) { 
			return Collections.emptyList();
		} else if (!ensureBuffer(toIndex-1)) {
			throw new IndexOutOfBoundsException();
		}
		return sheetBuffer.subList(0, toIndex);
	}

	@Override
	public List<Sheet> getRemaining() throws RestartPaginationException {
		ensureBuffer(-1);
		return sheetBuffer;
	}

	@Override
	public SplitPointDataSource<Sheet> tail(int fromIndex) throws RestartPaginationException {
		List<Sheet> newBuffer;
		if (!ensureBuffer(fromIndex)) {
			newBuffer = new ArrayList<>();
		} else {
			newBuffer = new ArrayList<>(sheetBuffer.subList(fromIndex, sheetBuffer.size()));
		}
		return new SheetDataSource(struct, crh, context, rcontext, seqsIterator, newBuffer, volBreakAllowed, sheetsServed+fromIndex, seqsIndex, 
				psb, sectionProperties, s, si, sheetIndex, pageIndex);
	}

	@Override
	public boolean hasElementAt(int index) throws RestartPaginationException {
		return ensureBuffer(index);
	}

	@Override
	public int getSize(int limit)  throws RestartPaginationException {
		if (!ensureBuffer(limit-1))  {
			//we have buffered all elements
			return sheetBuffer.size();
		} else {
			return limit;
		}
	}

	@Override
	public boolean isEmpty() {
		return seqsIndex>=seqsIterator.size() && sheetBuffer.isEmpty();
	}

	@Override
	public Supplements<Sheet> getSupplements() {
		return null;
	}
	
	/**
	 * Ensures that there are at least index elements in the buffer.
	 * When index is -1 this method always returns false.
	 * @param index the index (or -1 to get all remaining elements)
	 * @return returns true if the index element was available, false otherwise
	 */
	private boolean ensureBuffer(int index) {
		while (index<0 || sheetBuffer.size()<=index) {
			if (psb==null || !psb.hasNext()) {
				if (seqsIndex>=seqsIterator.size()) {
					// cannot ensure buffer, return false
					return false;
				}
				// init new sequence
				BlockSequence bs = seqsIterator.get(seqsIndex);
				seqsIndex++;
				int offset = struct.getCurrentPageOffset();
				psb = new PageSequenceBuilder2(struct.getPageCount(), bs.getLayoutMaster(), bs.getInitialPageNumber()!=null?bs.getInitialPageNumber() - 1:offset, crh, bs, context, rcontext, seqsIndex);
				sectionProperties = bs.getLayoutMaster().newSectionProperties();
				s = null;
				si = null;
				sheetIndex = 0;
				pageIndex = 0;
			}
			int currentSheet = sheetBuffer.size();
			while (psb.hasNext() && currentSheet == sheetBuffer.size()) {
				PageImpl p = psb.nextPage();
				struct.increasePageCount();
				if (!sectionProperties.duplex() || pageIndex % 2 == 0) {
					volBreakAllowed = true;
					if (s!=null) {
						Sheet r = s.build();
						sheetBuffer.add(r);
					}
					s = new Sheet.Builder(sectionProperties);
					si = new SheetIdentity(rcontext.getSpace(), rcontext.getCurrentVolume()==null?0:rcontext.getCurrentVolume(), sheetsServed + sheetBuffer.size());
					sheetIndex++;
				}
				s.avoidVolumeBreakAfterPriority(p.getAvoidVolumeBreakAfter());
				if (!psb.hasNext()) {
					s.avoidVolumeBreakAfterPriority(null);
					//Don't get or store this value in crh as it is transient and not a property of the sheet context
					s.breakable(true);
				} else {
					boolean br = crh.getBreakable(si);
					//TODO: the following is a low effort way of giving existing uses of non-breakable units a high priority, but it probably shouldn't be done this way
					if (!br) {
						s.avoidVolumeBreakAfterPriority(1);
					}
					s.breakable(br);
				}

				setPreviousSheet(si.getSheetIndex()-1, Math.min(p.keepPreviousSheets(), sheetIndex-1), rcontext);
				volBreakAllowed &= p.allowsVolumeBreak();
				if (!sectionProperties.duplex() || pageIndex % 2 == 1) {
					crh.keepBreakable(si, volBreakAllowed);
				}
				s.add(p);
				pageIndex++;
			}
			if (!psb.hasNext()) {
				if (s!=null) {
					//Last page in the sequence doesn't need volume keep priority
					sheetBuffer.add(s.build());
				}
				crh.setSequenceScope(new DocumentSpace(rcontext.getSpace(), rcontext.getCurrentVolume()), seqsIndex, psb.getGlobalStartIndex(), psb.getToIndex());
				struct.setCurrentSequence(psb);
			}
		}
		return true;
	}
	
	
	private void setPreviousSheet(int start, int p, DefaultContext rcontext) {
		int i = 0;
		//TODO: simplify this?
		for (int x = start; i < p && x > 0; x--) {
			SheetIdentity si = new SheetIdentity(rcontext.getSpace(), rcontext.getCurrentVolume()==null?0:rcontext.getCurrentVolume(), x);
			crh.keepBreakable(si, false);
			i++;
		}
	}

	@Override
	public SplitResult<Sheet> split(int atIndex) {
		return new SplitResult<Sheet>(head(atIndex), tail(atIndex));
	}

}
