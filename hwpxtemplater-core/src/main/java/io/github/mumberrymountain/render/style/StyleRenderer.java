package io.github.mumberrymountain.render.style;

import kr.dogfoot.hwpxlib.object.content.header_xml.HeaderXMLFile;
import kr.dogfoot.hwpxlib.object.content.header_xml.references.BorderFill;
import kr.dogfoot.hwpxlib.object.content.header_xml.references.CharPr;
import kr.dogfoot.hwpxlib.object.content.header_xml.references.ParaPr;
import io.github.mumberrymountain.model.Text;
import io.github.mumberrymountain.model.table.Align;
import io.github.mumberrymountain.model.table.Cell;
import io.github.mumberrymountain.render.RendererUtil;

import java.util.HashMap;
import java.util.Map;

public class StyleRenderer {
    private final HeaderXMLFile headerXMLFile;
    private final Map<String, CharPr> charPrs = new HashMap<String, CharPr>();
    private final Map<String, ParaPr> paraPrs = new HashMap<String, ParaPr>();
    private final Map<String, BorderFill> borderFillIds = new HashMap<String, BorderFill>();

    public StyleRenderer (HeaderXMLFile headerXMLFile){
        this.headerXMLFile = headerXMLFile;
    }

    public String renderCharStyleAndReturnCharPrId(Text text) {
        String key = RendererUtil.createCharStyleKey(text);

        if (charPrs.containsKey(key)) return charPrs.get(key).id();

        String fontId = new FontRenderer(headerXMLFile, text.getFontFamily()).render();
        CharPr charPr = new CharPrRenderer(headerXMLFile, fontId, text).render();

        headerXMLFile.refList().charProperties().add(charPr);
        charPrs.put(key, charPr);

        return charPr.id();
    }

    public String renderBorderStyle(Cell cell) {
        String key = RendererUtil.createBorderStyleKey(cell);
        if (borderFillIds.containsKey(key)) return borderFillIds.get(key).id();

        BorderFill borderFill = new BorderRenderer(headerXMLFile, cell).render();
        borderFillIds.put(key, borderFill);

        return borderFill.id();
    }

    public String renderBorderStyle(Cell cell, Table table, int rowIdx, int colIdx) {
        if (cell == null || table == null) return renderBorderStyle(cell);

        Map<String, Object> styleMap = safeMap(table.getConfig("style"));
        Object borderObj = null;

        if (styleMap != null) borderObj = styleMap.get("border");
        if (!(borderObj instanceof Map)) borderObj = table.getConfig("border");

        if (!(borderObj instanceof Map)) {
            return renderBorderStyle(cell);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> border = (Map<String, Object>) borderObj;

        Map<String, Object> outer = safeMap(border.get("outer"));
        Map<String, Object> inner = safeMap(border.get("inner"));

        if (outer == null && inner == null) return renderBorderStyle(cell);
        if (outer == null) outer = inner;
        if (inner == null) inner = outer;

        int cs = Math.max(reflectInt(cell, "getColSpan", 1), 1);
        int rs = Math.max(reflectInt(cell, "getRowSpan", 1), 1);
        int endCol = colIdx + cs - 1;
        int endRow = rowIdx + rs - 1;

        boolean touchesTop = (rowIdx == 0);
        boolean touchesLeft = (colIdx == 0);
        boolean touchesBottom = (endRow == table.getRowCount() - 1);
        boolean touchesRight = (endCol == table.getColCount() - 1);

        Map<String, Object> leftSpec   = touchesLeft   ? outer : inner;
        Map<String, Object> rightSpec  = touchesRight  ? outer : inner;
        Map<String, Object> topSpec    = touchesTop    ? outer : inner;
        Map<String, Object> bottomSpec = touchesBottom ? outer : inner;

        String key =
                RendererUtil.createBorderStyleKey(cell) +
                "|L=" + borderSpecKey(leftSpec) +
                "|R=" + borderSpecKey(rightSpec) +
                "|T=" + borderSpecKey(topSpec) +
                "|B=" + borderSpecKey(bottomSpec);

        if (borderFillIds.containsKey(key)) return borderFillIds.get(key).id();

        BorderFill borderFill = new BorderRenderer(
                headerXMLFile,
                cell,
                leftSpec, rightSpec, topSpec, bottomSpec
        ).render();

        borderFillIds.put(key, borderFill);
        return borderFill.id();
    }

    public String renderParaStyleAndReturnParaPrId(Align align) {
        if (align == null) align = Align.Left;
        String key = RendererUtil.createParaStyleKey(align);
        if (paraPrs.containsKey(key)) return paraPrs.get(key).id();

        ParaPr paraPr = new ParaPrRenderer(headerXMLFile, align).render();

        headerXMLFile.refList().paraProperties().add(paraPr);
        paraPrs.put(key, paraPr);

        return paraPr.id();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> safeMap(Object o) {
        if (o instanceof Map) return (Map<String, Object>) o;
        return null;
    }

    private String borderSpecKey(Map<String, Object> spec) {
        if (spec == null) return "null";
        Object st = spec.get("style");
        Object w = spec.get("width");
        Object c = spec.get("color");
        return String.valueOf(st) + "|" + String.valueOf(w) + "|" + String.valueOf(c);
    }

    private int reflectInt(Object target, String method, int fallback) {
        try {
            java.lang.reflect.Method m = target.getClass().getMethod(method);
            Object r = m.invoke(target);
            if (r instanceof Number) return ((Number) r).intValue();
        } catch (Exception ignored) {}
        return fallback;
    }
}
