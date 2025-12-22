package com.github.mumberrymountain.render;

import kr.dogfoot.hwpxlib.object.content.section_xml.SectionXMLFile;
import com.github.mumberrymountain.Config;
import com.github.mumberrymountain.ConfigOption;
import com.github.mumberrymountain.model.Text;
import com.github.mumberrymountain.model.table.Align;
import com.github.mumberrymountain.render.placeholder.PlaceHolderRange;
import com.github.mumberrymountain.render.placeholder.Pos;
import com.github.mumberrymountain.util.Status;

import java.util.UUID;

import static com.github.mumberrymountain.render.placeholder.PlaceHolderType.LOOP;

public class RendererUtil {

    public static boolean isRangeVertical(PlaceHolderRange currentRange){
        return currentRange != null && currentRange.pairAlignment() == Pos.VERTICAL
                && currentRange.start().parent().parent() instanceof SectionXMLFile;
    }

    public static boolean isCurrentRangeProcessing(PlaceHolderRange currentRange){
        return currentRange != null && currentRange.status() == Status.PROCESSING;
    }

    public static boolean isCurrentRangeLoop(PlaceHolderRange currentRange){
        return currentRange != null && currentRange.type() == LOOP;
    }

    public static String getRandomId() {
        return UUID.randomUUID().toString();
    }

    public static Boolean isAutoTrim(Config config){
        return (Boolean) config.get(ConfigOption.AUTO_TRIM.getType());
    }

    public static String createCharStyleKey(Text text) {
        return String.join(";",
                    String.valueOf(text.getFontSize()),
                    String.valueOf(text.getFontColor()),
                    String.valueOf(text.getFontFamily()),
                    String.valueOf(text.getBackgroundColor()),
                    String.valueOf(text.isBold()),
                    String.valueOf(text.isItalic()),
                    String.valueOf(text.isUnderLine()),
                    String.valueOf(text.isStrikeOut()),
                    String.valueOf(text.isOutline()),
                    String.valueOf(text.isShadow()),
                    String.valueOf(text.isEmboss()),
                    String.valueOf(text.isEngrave())
                );
    }

    public static String createParaStyleKey(Align align) {
        return String.join(";",
                String.valueOf(align)
               );
    }
}
