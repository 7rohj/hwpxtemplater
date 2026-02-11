package io.github.mumberrymountain.render;

import io.github.mumberrymountain.model.table.*;
import io.github.mumberrymountain.util.HWPXUnitUtil;
import kr.dogfoot.hwpxlib.object.content.section_xml.SubList;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.LineWrapMethod;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.TextDirection;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.VerticalAlign2;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.Para;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.Run;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.T;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.object.table.Tc;

import java.util.Map;

public class TableCellRenderer {

    private final HWPXRenderer rootRenderer;
    private final Tc renderingCell;
    private final int rowIdx;
    private final int colIdx;
    private final Table tableParam;
    private final Cell cell;
    private final Col col;
    private final Row row;
    
    public TableCellRenderer(HWPXRenderer rootRenderer, Tc renderingCell, int rowIdx, int colIdx, Table tableParam) {
        this.rootRenderer = rootRenderer;
        this.renderingCell = renderingCell;
        this.rowIdx = rowIdx;
        this.colIdx = colIdx;
        this.tableParam = tableParam;
        this.cell = tableParam.getCell(rowIdx, colIdx);
        this.row = tableParam.getRow(rowIdx);
        this.col = tableParam.getCol(colIdx);
    }

    /*
        tc: 테이블 셀 요소. 하위 요소로 글 내용을 담은 subList 요소를 가짐.
         - name: 셀 필드 이름
         - header: 제목 셀인지 여부
         - hasMargin: 테이블의 기본 셀 여백이 아닌 독자적인 여백을 사용하는지 여부
         - protect: 사용자 편집을 막을지 여부
         - editable: 읽기 전용 상태에서도 수정 가능한지 여부
         - dirty: 마지막 업데이트된 이후 사용자가 내용을 변경했는지 여부
         - borderFillIDRef: 테두리/배경 아이디 참조값
    */
    private void setTc(){
        renderingCell.name("");

        boolean isHeader = (row != null && row.getRowType() == RowType.Header);
        renderingCell.header(isHeader);
    
        renderingCell.hasMargin(false);
        renderingCell.protect(false);
        renderingCell.editable(false);
        renderingCell.dirty(false);

        renderingCell.borderFillIDRef(
            rootRenderer.styleRenderer().renderBorderStyle(cell, tableParam, rowIdx, colIdx, resolveBackgroundColor())
        );
    }

    private String resolveBackgroundColor() {
        Map<String, Object> cellStyle = getCellStyleMapForCurrentRow();
        if (cellStyle == null) return null;

        Object bg = cellStyle.get("backgroundColor");
        if (bg == null) return null;

        String s = String.valueOf(bg).trim();
        return s.isEmpty() ? null : s;
    }

    /*
        cellAddr: 표에서 하나의 열이 차지하는 영역을 지정하기 위한 요소
         - colAddr: 셀의 열 주소. 주소는 0부터 시작. 표에서 제일 왼쪽 셀이 0부터 시작하여 1씩 증가
         - rowAddr: 셀의 행 주소. 주소는 0부터 시작. 표에서 제일 위쪽 셀이 0부터 시작하여 1씩 증가
    */
    private void setCellAddr(){
        renderingCell.createCellAddr();
        renderingCell.cellAddr().colAddr((short) colIdx);
        renderingCell.cellAddr().rowAddr((short) rowIdx);
    }

    /*
        cellSpan: 표에서 하나의 열이 하나의 셀 대신 여러 개의 셀로 구성되어 있다면 병합된 셀 정보를 표현하기 위해 사용되는 요소
         - colSpan: 열 병합 개수
         - rowSpan: 행 병합 개수
    */
    private void setCellSpan() {
        renderingCell.createCellSpan();

        if (isCovered(cell)) {
            renderingCell.cellSpan().colSpan((short) 0);
            renderingCell.cellSpan().rowSpan((short) 0);
            return;
        }

        int cs = Math.max(reflectInt(cell, "getColSpan", 1), 1);
        int rs = Math.max(reflectInt(cell, "getRowSpan", 1), 1);

        renderingCell.cellSpan().colSpan((short) cs);
        renderingCell.cellSpan().rowSpan((short) rs);
    }

    /*
        cellSz: 개별 셀의 크기 정보를 가진 요소
         - width: 셀의 너비. 단위는 HWPUNIT.
         - height: 셀의 높이. 단위는 HWPUNIT.
    */
    private void setCellSz() {
        renderingCell.createCellSz();

        // covered 셀은 0으로 두는 게 안전
        if (cell != null && cell.isCovered()) {
            renderingCell.cellSz().width(0L);
            renderingCell.cellSz().height(0L);
            return;
        }

        int cs = Math.max(reflectInt(cell, "getColSpan", 1), 1);
        int rs = Math.max(reflectInt(cell, "getRowSpan", 1), 1);

        int widthPx = 0;
        for (int c = colIdx; c < colIdx + cs; c++) {
            Col cObj = tableParam.getCol(c);
            if (cObj != null) widthPx += cObj.getWidth();
        }

        int heightPx = 0;
        for (int r = rowIdx; r < rowIdx + rs; r++) {
            Row rObj = tableParam.getRow(r);
            if (rObj != null) heightPx += rObj.getHeight();
        }

        int heightPx = 0;
        for (int r = rowIdx; r < rowIdx + rs; r++) {
            Row rObj = tableParam.getRow(r);
            if (rObj != null) heightPx += rObj.getHeight();
        }
        renderingCell.cellSz().width((long) HWPXUnitUtil.pxToHwpxUnit(widthPx);
        renderingCell.cellSz().height((long) HWPXUnitUtil.pxToHwpxUnit(heightPx);
    }

    /*
        cellMargin: 셀 여백 정보
         - left: 왼쪽 여백. 단위는 HWPUNIT.
         - right: 오른쪽 여백. 단위는 HWPUNIT.
         - top: 위쪽 여백. 단위는 HWPUNIT.
         - bottom: 아래쪽 여백. 단위는 HWPUNIT.
    */
    private void setCellMargin() {
        renderingCell.createCellMargin();

        if (isCovered(cell)) {
            renderingCell.cellMargin().left(0L);
            renderingCell.cellMargin().right(0L);
            renderingCell.cellMargin().top(0L);
            renderingCell.cellMargin().bottom(0L);
            return;
        }

        Map<String, Object> padding = getPaddingMapFromCellStyle();

        // 기본값(mm) - 필요하면 조정
        long left   = asHwpxFromMmOrDefault(padding, "left",   1.8);
        long right  = asHwpxFromMmOrDefault(padding, "right",  1.8);
        long top    = asHwpxFromMmOrDefault(padding, "top",    0.5);
        long bottom = asHwpxFromMmOrDefault(padding, "bottom", 0.5);

        renderingCell.cellMargin().left(left);
        renderingCell.cellMargin().right(right);
        renderingCell.cellMargin().top(top);
        renderingCell.cellMargin().bottom(bottom);
    }

    private SubList cellSubListDef(){
        renderingCell.createSubList();
        SubList sl = renderingCell.subList();
        sl.id("");
        sl.textDirection(TextDirection.HORIZONTAL);
        sl.lineWrap(LineWrapMethod.BREAK);

        sl.vertAlign(resolveVerticalAlignFromStyle());
        
        sl.linkListIDRef("0");
        sl.linkListNextIDRef("0");
        sl.textWidth(0);
        sl.textHeight(0);
        sl.hasTextRef(false);
        sl.hasNumRef(false);
        return sl;
    }

    private void setSubList(){
        setParagraph(cellSubListDef());
    }

    private Align resolveHorizontalAlign(Cell cell, Col col) {
        Map<String, Object> ta = getTextAlignMapFromCellStyle();
        if (ta != null) {
            Object hObj = ta.get("horizontal");
            if (hObj != null) {
                String h = String.valueOf(hObj).trim().toLowerCase();
                switch (h) {
                    case "left":
                        return Align.Left;
                    case "center":
                    case "centre":
                    case "middle":
                        return Align.Center;
                    case "right":
                        return Align.Right;
                    default:
                        break;
                }
            }
        }

        // fallback: 기존 로직
        if (cell != null && cell.getAlign() != null) return cell.getAlign();
        if (col != null && col.getAlign() != null) return col.getAlign();
        return Align.Left;
    }

    private Para cellParaDef(SubList sl) {
        Para cellPara = sl.addNewPara();
        cellPara.id("0");
        cellPara.paraPrIDRef(
                rootRenderer.styleRenderer()
                        .renderParaStyleAndReturnParaPrId(resolveHorizontalAlign(cell, col))
        );
        cellPara.styleIDRef("0");
        cellPara.pageBreak(false);
        cellPara.columnBreak(false);
        cellPara.merged(false);
        return cellPara;
    }

    private void setParagraph(SubList sl) {
        setRun(cellParaDef(sl));
    }

    private void setRun(Para cellPara) {
        Run cellRun = cellPara.addNewRun();
        T cellT = cellRun.addNewT();

        String val = (cell == null) ? "" : cell.getText().getValue();
        if (RendererUtil.isAutoTrim(rootRenderer.config())) val = val.trim();

        cellT.addText(val);
        if (cell != null) {
            cellRun.charPrIDRef(rootRenderer.styleRenderer().renderCharStyleAndReturnCharPrId(cell.getText()));
        }
    }

    public void render(){
        setTc();

        if (!isCovered(cell)) {
            setSubList();
        }

        setCellAddr();
        setCellSpan();
        setCellSz();
        setCellMargin();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getStyleMap() {
        if (tableParam == null) return null;
        Object styleObj = tableParam.getConfig("style");
        if (styleObj instanceof Map) return (Map<String, Object>) styleObj;
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getCellStyleMapForCurrentRow() {
        Map<String, Object> style = getStyleMap();
        if (style == null) return null;

        boolean isHeader = (row != null && row.getRowType() == RowType.Header);
        Object obj = isHeader ? style.get("headerCell") : style.get("bodyCell");
        if (obj instanceof Map) return (Map<String, Object>) obj;

        // 하위호환: style.cell
        Object legacy = style.get("cell");
        if (legacy instanceof Map) return (Map<String, Object>) legacy;

        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getPaddingMapFromCellStyle() {
        Map<String, Object> cellStyle = getCellStyleMapForCurrentRow();
        if (cellStyle == null) return null;

        Object paddingObj = cellStyle.get("padding");
        if (!(paddingObj instanceof Map)) return null;

        return (Map<String, Object>) paddingObj;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getTextAlignMapFromCellStyle() {
        Map<String, Object> cellStyle = getCellStyleMapForCurrentRow();
        if (cellStyle == null) return null;

        Object taObj = cellStyle.get("textAlign");
        if (!(taObj instanceof Map)) return null;

        return (Map<String, Object>) taObj;
    }

    public static long mmToHwpxUnit(double mm) {
        // HWPX 단위: 1/7200 inch (dogfoot hwpx에서 width/height/margin에 쓰는 단위)
        // 1 inch = 25.4 mm
        // => hwpxUnit = mm / 25.4 * 7200
        return Math.round(mm * 7200.0 / 25.4);
    }

    private long asHwpxFromMmOrDefault(Map<String, Object> m, String key, double defMm) {
        double mm = defMm;
        if (m != null) {
            Double parsed = tryParseDouble(m.get(key));
            if (parsed != null) mm = parsed;
        }
        return Math.round(mm * 7200.0 / 25.4);
    }

    private Double tryParseDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try {
            String s = String.valueOf(v).trim().toLowerCase();
            if (s.endsWith("mm")) s = s.substring(0, s.length() - 2).trim();
            if (s.isEmpty()) return null;
            return Double.parseDouble(s);
        } catch (Exception ignored) {
        }
        return null;
    }

    // -------------------------
    // reflection helpers
    // -------------------------

    private boolean isCovered(Object cellObj) {
        if (cellObj == null) return false;
        try {
            java.lang.reflect.Method m = cellObj.getClass().getMethod("isCovered");
            Object r = m.invoke(cellObj);
            return (r instanceof Boolean) && (Boolean) r;
        } catch (Exception ignored) {
        }
        return false;
    }

    private int reflectInt(Object target, String method, int fallback) {
        try {
            java.lang.reflect.Method m = target.getClass().getMethod(method);
            Object r = m.invoke(target);
            if (r instanceof Number) return ((Number) r).intValue();
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private VerticalAlign2 resolveVerticalAlignFromStyle() {
        // default = 가운데(기존 동작 유지)
        Map<String, Object> ta = getTextAlignMapFromCellStyle();
        if (ta == null) return VerticalAlign2.CENTER;

        Object vObj = ta.get("vertical");
        if (vObj == null) return VerticalAlign2.CENTER;

        String v = String.valueOf(vObj).trim().toLowerCase();
        if (v.isEmpty()) return VerticalAlign2.CENTER;

        switch (v) {
            case "top":
            case "upper":
                return VerticalAlign2.TOP;

            case "middle":
            case "center":
            case "centre":
                return VerticalAlign2.CENTER;

            case "bottom":
            case "lower":
                return VerticalAlign2.BOTTOM;

            default:
                return VerticalAlign2.CENTER;
        }
    }
}
