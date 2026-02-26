package io.github.mumberrymountain.render.style;

import kr.dogfoot.hwpxlib.object.content.header_xml.HeaderXMLFile;
import kr.dogfoot.hwpxlib.object.content.header_xml.references.BorderFill;
import kr.dogfoot.hwpxlib.object.content.header_xml.references.CharPr;
import kr.dogfoot.hwpxlib.object.content.header_xml.references.ParaPr;
import kr.dogfoot.hwpxlib.object.content.header_xml.enumtype.HorizontalAlign2; //추가
import kr.dogfoot.hwpxlib.object.content.header_xml.enumtype.VerticalAlign1; // 추가
import io.github.mumberrymountain.model.Text;
import io.github.mumberrymountain.model.table.Align;
import io.github.mumberrymountain.model.table.Cell;
import io.github.mumberrymountain.model.table.Table;
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

    public String renderBorderStyle(Cell cell, Table table, int rowIdx, int colIdx, String bgColor) {
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
                "|B=" + borderSpecKey(bottomSpec) +
                "|BG=" + String.valueOf(normalizeHex(bgColor));

        if (borderFillIds.containsKey(key)) return borderFillIds.get(key).id();

        BorderFill borderFill = new BorderRenderer(
                headerXMLFile,
                cell,
                leftSpec, rightSpec, topSpec, bottomSpec
        ).render();

        applyBackgroundColor(borderFill, bgColor);

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

    private void applyBackgroundColor(BorderFill borderFill, String bgColor) {
        String rgb = normalizeHex(bgColor);
        if (rgb == null) return;

        if (borderFill.fillBrush() == null) borderFill.createFillBrush();
        if (borderFill.fillBrush().winBrush() == null) borderFill.fillBrush().createWinBrush();

        // RGB -> BGR
        String bgr6 = rgbToBgr(rgb);

        String bgr8 = "00" + bgr6;

        borderFill.fillBrush().winBrush().faceColor(bgr8);
        borderFill.fillBrush().winBrush().hatchColor(bgr8);
    }

    // "#eeeeee" / "eeeeee" / null -> "EEEEEE" (6자리) or null
    private String normalizeHex(String hex) {
        if (hex == null) return null;
        String s = hex.trim();
        if (s.isEmpty()) return null;

        if (s.startsWith("#")) s = s.substring(1);
        s = s.trim();

        if (s.length() == 3) {
            char r = s.charAt(0), g = s.charAt(1), b = s.charAt(2);
            s = "" + r + r + g + g + b + b;
        }

        if (s.length() != 6) return null;

        // hex 체크
        for (int i = 0; i < 6; i++) {
            char ch = s.charAt(i);
            boolean ok = (ch >= '0' && ch <= '9')
                    || (ch >= 'a' && ch <= 'f')
                    || (ch >= 'A' && ch <= 'F');
            if (!ok) return null;
        }

        return s.toUpperCase();
    }

    private String rgbToBgr(String rgb6) {
        if (rgb6 == null || rgb6.length() != 6) return rgb6;
        String rr = rgb6.substring(0, 2);
        String gg = rgb6.substring(2, 4);
        String bb = rgb6.substring(4, 6);
        return (bb + gg + rr).toUpperCase(); // BGR
    }

    public String renderParaStyleFromBaseAndReturnParaPrId(String baseParaPrId, Align align) {
        return renderParaStyleFromBaseAndReturnParaPrId(baseParaPrId, align, null);
    }

    public String renderParaStyleFromBaseAndReturnParaPrId(String baseParaPrId, Align align, Integer lineSpacingPercent) {
        if (align == null) align = Align.Left;

        if (baseParaPrId == null || baseParaPrId.isBlank()) {
            return renderParaStyleAndReturnParaPrId(align);
        }

        String key = "BASE=" + baseParaPrId
                + "|ALIGN=" + align.name()
                + "|LS=" + (lineSpacingPercent == null ? "null" : lineSpacingPercent);

        if (paraPrs.containsKey(key)) return paraPrs.get(key).id();

        ParaPr base = null;
        for (ParaPr p : headerXMLFile.refList().paraProperties().items()) {
            if (baseParaPrId.equals(p.id())) {
                base = p;
                break;
            }
        }
        if (base == null) {
            return renderParaStyleAndReturnParaPrId(align);
        }

        ParaPr cloned = base.clone();

        String newId = Integer.toString(headerXMLFile.refList().paraProperties().count());
        cloned.id(newId);

        HorizontalAlign2 ha = HorizontalAlign2.LEFT;
        switch (align) {
            case Center: ha = HorizontalAlign2.CENTER; break;
            case Right:  ha = HorizontalAlign2.RIGHT;  break;
            case Left:
            default:     ha = HorizontalAlign2.LEFT;   break;
        }
        cloned.createAlign();
        cloned.align().horizontal(ha);
        cloned.align().vertical(VerticalAlign1.BASELINE);

        if (lineSpacingPercent != null) {
            cloned.createLineSpacing();
            cloned.lineSpacing().type(LineSpacingType.PERCENT);
            cloned.lineSpacing().value(lineSpacingPercent);
        }

        headerXMLFile.refList().paraProperties().add(cloned);
        paraPrs.put(key, cloned);

        return cloned.id();
    }
    
}
