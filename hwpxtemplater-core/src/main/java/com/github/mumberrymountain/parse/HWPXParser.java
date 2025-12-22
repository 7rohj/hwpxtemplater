package com.github.mumberrymountain.parse;

import com.github.mumberrymountain.linkedobj.LinkedObj;
import com.github.mumberrymountain.linkedobj.LinkedPara;
import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.object.common.ObjectType;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.*;
import kr.dogfoot.hwpxlib.tool.finder.ObjectFinder;
import com.github.mumberrymountain.Config;
import com.github.mumberrymountain.ConfigOption;
import com.github.mumberrymountain.delim.DelimParser;
import com.github.mumberrymountain.delim.DelimPos;
import com.github.mumberrymountain.linkedobj.*;
import com.github.mumberrymountain.util.ParaUtil;
import com.github.mumberrymountain.util.FinderUtil;

import java.util.ArrayList;
import java.util.List;

public class HWPXParser {
    private final HWPXFile hwpxFile;
    private final Config config;

    public HWPXParser(HWPXFile hwpxFile, Config config){
        this.hwpxFile = hwpxFile;
        this.config = config;
    }

    public void parse() throws Exception {
        parsePara(ObjectType.hs_sec);
        parsePara(ObjectType.hp_subList);
    }

    private void parsePara(ObjectType parentsType) throws Exception {
        ObjectFinder.Result[] results = FinderUtil.findTag(hwpxFile, ObjectType.hp_p, parentsType);
        for (ObjectFinder.Result result: results) {
            parseParaEach(new LinkedPara((Para) result.thisObject(), result.parentsPath().get(result.parentsPath().size() - 1)));
        }
    }

    private void parseParaEach(LinkedPara linkedPara){
        linkedPara.data().removeLineSegArray();
        String str = ParaUtil.getParaText(linkedPara.data());
        String delimStart = (String) config.get(ConfigOption.DELIM_PREFIX.getType());
        String delimEnd = (String) config.get(ConfigOption.DELIM_SUFFIX.getType());

        TextIsolater textIsolater = new TextIsolater(linkedPara);
        textIsolater.reformatParaToIsolateText(linkedPara);

        DelimParser delimParser = new DelimParser(delimStart, delimEnd);
        List<DelimPos> delims = delimParser.parse(str);

        for (DelimPos delim : delims) {
            ArrayList<LinkedObj> items = new ItemWithDelimSearcher(delim).find(linkedPara);
            new ItemRemapper(items).reMap();
        }
    }
}
