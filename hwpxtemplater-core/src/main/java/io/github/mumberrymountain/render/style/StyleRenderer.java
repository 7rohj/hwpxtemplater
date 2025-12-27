package io.github.mumberrymountain.render.style;

import kr.dogfoot.hwpxlib.object.content.header_xml.HeaderXMLFile;
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
        return new BorderRenderer(headerXMLFile, cell).render();
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
}
