package io.github.mumberrymountain.render;

import io.github.mumberrymountain.interceptor.InterceptorType;
import io.github.mumberrymountain.interceptor.ValueStylingInterceptor;
import io.github.mumberrymountain.linkedobj.LinkedRunItem;
import io.github.mumberrymountain.model.Text;
import io.github.mumberrymountain.render.placeholder.PlaceHolder;
import io.github.mumberrymountain.render.placeholder.PlaceHolderRangeStack;
import io.github.mumberrymountain.render.value.ValueReplacementRendererFactory;

public class ReplacementRenderer<H> implements SinglePlaceHolderRenderer {

    private final PlaceHolderRangeStack rangeStack;
    private final HWPXRenderer rootRenderer;

    public ReplacementRenderer(PlaceHolderRangeStack rangeStack, HWPXRenderer rootRenderer) {
        this.rangeStack = rangeStack;
        this.rootRenderer = rootRenderer;
    }

    @Override
    public void renderReplacement(LinkedRunItem linkedRunItem, PlaceHolder placeHolder, Object value){
        if(RendererUtil.isCurrentRangeLoop(rangeStack.current()) && RendererUtil.isCurrentRangeProcessing(rangeStack.current())) {
            rangeStack.add(linkedRunItem.parent(), placeHolder);
            return;
        }

        // ValueStylingInterceptor를 사용하는 경우에는 value 값을 Text 객체로 변환 처리한다.
        ValueStylingInterceptor valueStylingInterceptor = (ValueStylingInterceptor) rootRenderer.interceptorHandler().get(InterceptorType.ValueStylingInterceptor);
        if (valueStylingInterceptor != null && value != null && !(value instanceof Text)) value = new Text(String.valueOf(value));

        ValueReplacementRendererFactory.create(value, linkedRunItem, placeHolder, rootRenderer, rangeStack).render();
    }
}
