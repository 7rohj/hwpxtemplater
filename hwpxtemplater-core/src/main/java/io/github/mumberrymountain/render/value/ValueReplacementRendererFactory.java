package io.github.mumberrymountain.render.value;

import io.github.mumberrymountain.linkedobj.LinkedRunItem;
import io.github.mumberrymountain.model.Text;
import io.github.mumberrymountain.render.HWPXRenderer;
import io.github.mumberrymountain.render.placeholder.PlaceHolder;
import io.github.mumberrymountain.render.placeholder.PlaceHolderRangeStack;

import java.util.Objects;

public class ValueReplacementRendererFactory {

    public static ValueReplacementRenderer create(Object value, LinkedRunItem linkedRunItem, PlaceHolder placeHolder, HWPXRenderer rootRenderer, PlaceHolderRangeStack rangeStack) {
        if (value instanceof Text) return new TextObjectReplacementRenderer((Text) value, linkedRunItem, placeHolder, rootRenderer, rangeStack);
        else return new StringValueReplacementRenderer(Objects.toString(value, null), linkedRunItem, placeHolder, rootRenderer, rangeStack);
    }
}
