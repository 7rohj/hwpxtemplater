package com.github.mumberrymountain.render;

import com.github.mumberrymountain.render.placeholder.PlaceHolder;
import com.github.mumberrymountain.render.placeholder.PlaceHolderRangeStack;
import kr.dogfoot.hwpxlib.object.common.HWPXObject;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.Para;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.Run;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.T;
import kr.dogfoot.hwpxlib.tool.finder.ObjectFinder;
import com.github.mumberrymountain.linkedobj.LinkedPara;
import com.github.mumberrymountain.linkedobj.LinkedRun;
import com.github.mumberrymountain.linkedobj.LinkedRunItem;
import com.github.mumberrymountain.render.placeholder.*;
import com.github.mumberrymountain.util.ParaUtil;

import static com.github.mumberrymountain.render.RendererUtil.isCurrentRangeProcessing;

public class PlaceHolderRenderer <H> {
    private final String delimStart;
    private final String delimEnd;

    private final ObjectFinder.Result[] results;

    private final PlaceHolderRangeStack rangeStack = new PlaceHolderRangeStack();

    private final PairedEdgePlaceHolderRenderer edgePlaceHolderRenderer;
    private final SinglePlaceHolderRendererFactory singlePlaceHolderRendererFactory;
    private final HWPXRenderer rootRenderer;

    public PlaceHolderRenderer(ObjectFinder.Result[] results, String delimStart, String delimEnd, HWPXRenderer rootRenderer) {
        this.results = results;
        this.delimStart = delimStart;
        this.delimEnd = delimEnd;
        this.rootRenderer = rootRenderer;
        this.edgePlaceHolderRenderer = new PairedEdgePlaceHolderRenderer(rangeStack, rootRenderer);
        this.singlePlaceHolderRendererFactory = SinglePlaceHolderRendererFactory.getInstance();
    }

    private LinkedRunItem setLinkage(ObjectFinder.Result result) {
        HWPXObject paraParent = result.parentsPath().get(result.parentsPath().size() - 3);
        Para para = (Para) result.parentsPath().get(result.parentsPath().size() - 2);
        Run run = (Run) result.parentsPath().get(result.parentsPath().size() - 1);
        T text = (T) result.thisObject();

        LinkedPara linkedPara = new LinkedPara(para, paraParent);
        LinkedRun linkedRun = new LinkedRun(run, linkedPara);
        LinkedRunItem linkedRunItem = new LinkedRunItem(text, linkedRun);

        return linkedRunItem;
    }

    public void mapPlaceholder(LinkedRunItem linkedRunItem) {
        PlaceHolder placeHolder = new PlaceHolder(linkedRunItem, (T) linkedRunItem.data(), delimStart, delimEnd);

        switch (placeHolder.type()) {
            case CONDITION:
            case LOOP:
                edgePlaceHolderRenderer.initPlaceHolderRange(linkedRunItem, placeHolder);
                break;
            case REPLACEMENT:
            case IMAGE_REPLACEMENT:
            case TABLE_REPLACEMENT:
                SinglePlaceHolderRenderer singlePlaceHolderRenderer = singlePlaceHolderRendererFactory.create(placeHolder.type(), rangeStack, rootRenderer);
                singlePlaceHolderRenderer.renderReplacement(linkedRunItem, placeHolder, rootRenderer.data().get(placeHolder.data()));
                break;
            case CLOSURE:
                edgePlaceHolderRenderer.renderClosure(linkedRunItem, placeHolder);
                break;
        }
    }

    public void renderEach(ObjectFinder.Result result) {
        LinkedRunItem linkedRunItem = setLinkage(result);
        if (!ParaUtil.containsDelim((T) linkedRunItem.data(), delimStart, delimEnd)) {
            if (isCurrentRangeProcessing(rangeStack.current())) rangeStack.add(linkedRunItem.parent());
            return;
        }

        mapPlaceholder(linkedRunItem);
    }

    public void render() throws Exception {
        for (ObjectFinder.Result result : results) renderEach(result);
    }
}
