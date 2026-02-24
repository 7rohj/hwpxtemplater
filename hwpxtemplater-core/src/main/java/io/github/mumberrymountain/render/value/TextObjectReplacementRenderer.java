package io.github.mumberrymountain.render.value;

import io.github.mumberrymountain.interceptor.InterceptorType;
import io.github.mumberrymountain.interceptor.NullValueInterceptor;
import io.github.mumberrymountain.interceptor.ValueInterceptor;
import io.github.mumberrymountain.interceptor.ValueStylingInterceptor;
import io.github.mumberrymountain.linkedobj.LinkedRunItem;
import io.github.mumberrymountain.model.Text;
import io.github.mumberrymountain.render.HWPXRenderer;
import io.github.mumberrymountain.render.RendererUtil;
import io.github.mumberrymountain.render.placeholder.PlaceHolder;
import io.github.mumberrymountain.render.placeholder.PlaceHolderRangeStack;

public class TextObjectReplacementRenderer implements ValueReplacementRenderer {
    private Text value;
    private final LinkedRunItem linkedRunItem;
    private final PlaceHolder placeHolder;
    private final HWPXRenderer rootRenderer;
    private final PlaceHolderRangeStack rangeStack;

    public TextObjectReplacementRenderer(Text value, LinkedRunItem linkedRunItem, PlaceHolder placeHolder, HWPXRenderer rootRenderer, PlaceHolderRangeStack rangeStack) {
        this.value = value;
        this.linkedRunItem = linkedRunItem;
        this.placeHolder = placeHolder;
        this.rootRenderer = rootRenderer;
        this.rangeStack = rangeStack;
    }

    @Override
    public void render() {
        if(value.getValue() == null) executeNullValueInterceptor();
        executeValueInterceptor();
        executeValueStylingInterceptor();
        if (value.getValue() == null) return;
        executeTrim();
        renderReplacement();
    }

    @Override
    public void executeNullValueInterceptor() {
        NullValueInterceptor nullValueInterceptor = (NullValueInterceptor) rootRenderer.interceptorHandler().get(InterceptorType.NullValueInterceptor);
        if (nullValueInterceptor != null) value.setValue(nullValueInterceptor.intercept(value.getValue(), placeHolder.data()));
    }

    @Override
    public void executeValueInterceptor() {
        ValueInterceptor valueInterceptor = (ValueInterceptor) rootRenderer.interceptorHandler().get(InterceptorType.ValueInterceptor);
        if (valueInterceptor != null) value.setValue(valueInterceptor.intercept(value.getValue(), placeHolder.data()));
    }

    public void executeValueStylingInterceptor() {
        ValueStylingInterceptor valueStylingInterceptor = (ValueStylingInterceptor) rootRenderer.interceptorHandler().get(InterceptorType.ValueStylingInterceptor);
        if (valueStylingInterceptor != null) {
            Text result = valueStylingInterceptor.intercept(value, placeHolder.data());
            if (result != null) value = result;
        }
    }

    @Override
    public void executeTrim() {
        if (RendererUtil.isAutoTrim(rootRenderer.config())) value.setValue(value.getValue().trim());
    }

    @Override
    public void renderReplacement() {
        placeHolder.t().clear();
        placeHolder.t().addText(value.getValue());
        linkedRunItem.parent().data().charPrIDRef(
            rootRenderer.styleRenderer().renderCharStyleAndReturnCharPrId(value)
        );

        String al = value.getAlign();
        if (al != null && !al.isBlank()) {
            io.github.mumberrymountain.model.table.Align a = io.github.mumberrymountain.model.table.Align.Left;
            switch (al.trim().toLowerCase()) {
                case "center": a = io.github.mumberrymountain.model.table.Align.Center; break;
                case "right":  a = io.github.mumberrymountain.model.table.Align.Right;  break;
                default:       a = io.github.mumberrymountain.model.table.Align.Left;   break;
            }

            kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.Para para =
                (kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.Para)
                    linkedRunItem.parent().parent().data();

            para.paraPrIDRef(rootRenderer.styleRenderer().renderParaStyleAndReturnParaPrId(a));
        }

        if (RendererUtil.isCurrentRangeProcessing(rangeStack.current())) {
            rangeStack.add(linkedRunItem.parent(), placeHolder);
        }
    }
}
