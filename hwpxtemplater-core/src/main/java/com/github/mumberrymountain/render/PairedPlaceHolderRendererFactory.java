package com.github.mumberrymountain.render;

import com.github.mumberrymountain.render.placeholder.PlaceHolderRangeStack;
import com.github.mumberrymountain.render.placeholder.PlaceHolderType;

public class PairedPlaceHolderRendererFactory<H> {
    public static PairedPlaceHolderRendererFactory pairedPlaceHolderRendererFactory;

    public static PairedPlaceHolderRendererFactory getInstance() {
        if (pairedPlaceHolderRendererFactory == null) pairedPlaceHolderRendererFactory = new PairedPlaceHolderRendererFactory();
        return pairedPlaceHolderRendererFactory;
    }

    public PairedPlaceHolderRenderer create(PlaceHolderType type, PlaceHolderRangeStack rangeStack, HWPXRenderer rootRenderer) {
        switch (type) {
            case CONDITION:
                return new ConditionRenderer<>(rangeStack, rootRenderer);
            case LOOP:
                return new LoopRenderer<>(rangeStack, rootRenderer);
            default:
                throw new IllegalArgumentException("Unsupported renderer type: " + type);
        }
    }
}
