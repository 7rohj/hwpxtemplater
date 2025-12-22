package com.github.mumberrymountain.render.value;

import com.github.mumberrymountain.linkedobj.LinkedRunItem;
import com.github.mumberrymountain.model.Text;
import com.github.mumberrymountain.render.HWPXRenderer;
import com.github.mumberrymountain.render.placeholder.PlaceHolder;
import com.github.mumberrymountain.render.placeholder.PlaceHolderRangeStack;

import java.util.Objects;

public class ValueReplacementRendererFactory {

    public static ValueReplacementRenderer create(Object value, LinkedRunItem linkedRunItem, PlaceHolder placeHolder, HWPXRenderer rootRenderer, PlaceHolderRangeStack rangeStack) {
        if (value instanceof Text) return new TextObjectReplacementRenderer((Text) value, linkedRunItem, placeHolder, rootRenderer, rangeStack);
        else return new StringValueReplacementRenderer(Objects.toString(value, null), linkedRunItem, placeHolder, rootRenderer, rangeStack);
    }
}
