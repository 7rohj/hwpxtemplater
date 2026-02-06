package io.github.mumberrymountain.render.style;

import kr.dogfoot.hwpxlib.object.content.header_xml.HeaderXMLFile;
import kr.dogfoot.hwpxlib.object.content.header_xml.enumtype.LineType2;
import kr.dogfoot.hwpxlib.object.content.header_xml.enumtype.LineWidth;
import kr.dogfoot.hwpxlib.object.content.header_xml.enumtype.SlashType;
import kr.dogfoot.hwpxlib.object.content.header_xml.references.BorderFill;
import kr.dogfoot.hwpxlib.object.content.header_xml.references.borderfill.FillBrush;
import io.github.mumberrymountain.model.table.Cell;

import java.util.Map;

public class BorderRenderer {
    private final HeaderXMLFile headerXMLFile;
    private final Cell cell;
    private final BorderFill borderFill;

    private final Map<String, Object> leftSpec;
    private final Map<String, Object> rightSpec;
    private final Map<String, Object> topSpec;
    private final Map<String, Object> bottomSpec;
    
    public BorderRenderer (HeaderXMLFile headerXMLFile, Cell cell){
        this(headerXMLFile, cell, null, null, null, null);
    }

    public BorderRenderer(
            HeaderXMLFile headerXMLFile,
            Cell cell,
            Map<String, Object> leftSpec,
            Map<String, Object> rightSpec,
            Map<String, Object> topSpec,
            Map<String, Object> bottomSpec
    ){
        this.headerXMLFile = headerXMLFile;
        this.cell = cell;
        this.borderFill = new BorderFill();

        this.leftSpec = leftSpec;
        this.rightSpec = rightSpec;
        this.topSpec = topSpec;
        this.bottomSpec = bottomSpec;
    }

    private void setId(){
        String bfId = Integer.toString(headerXMLFile.refList().borderFills().count() + 1);
        borderFill.id(bfId);
    }

    private void setLeftBorder(LineType2 lineType, LineWidth lineWidth, String color){
        borderFill.createLeftBorder();
        borderFill.leftBorder().type(lineType);
        borderFill.leftBorder().width(lineWidth);
        borderFill.leftBorder().color(color);
    }

    private void setRightBorder(LineType2 lineType, LineWidth lineWidth, String color){
        borderFill.createRightBorder();
        borderFill.rightBorder().type(lineType);
        borderFill.rightBorder().width(lineWidth);
        borderFill.rightBorder().color(color);
    }

    private void setTopBorder(LineType2 lineType, LineWidth lineWidth, String color){
        borderFill.createTopBorder();
        borderFill.topBorder().type(lineType);
        borderFill.topBorder().width(lineWidth);
        borderFill.topBorder().color(color);
    }

    private void setBottomBorder(LineType2 lineType, LineWidth lineWidth, String color){
        borderFill.createBottomBorder();
        borderFill.bottomBorder().type(lineType);
        borderFill.bottomBorder().width(lineWidth);
        borderFill.bottomBorder().color(color);
    }

    private void setSlash(){
        borderFill.createSlash();
        borderFill.slash().Crooked(false);
        borderFill.slash().type(SlashType.NONE);
        borderFill.slash().isCounter(false);
    }

    private void setBorderAll(LineType2 lineType, LineWidth lineWidth, String color){
        setLeftBorder(lineType, lineWidth, color);
        setRightBorder(lineType, lineWidth, color);
        setTopBorder(lineType, lineWidth, color);
        setBottomBorder(lineType, lineWidth, color);
    }

    private void setBackSlash(){
        borderFill.createBackSlash();
        borderFill.backSlash().Crooked(false);
        borderFill.backSlash().type(SlashType.NONE);
        borderFill.backSlash().isCounter(false);
    }

    private void setFillBrush(String faceColor, String hatchColor){
        borderFill.createFillBrush();

        FillBrush fillBrush = borderFill.fillBrush();
        fillBrush.createWinBrush();
        fillBrush.winBrush().faceColor(faceColor); // 배경색상
        fillBrush.winBrush().hatchColor(hatchColor);
        fillBrush.winBrush().alpha((float) 0);
    }

    public BorderFill render(){
        setId();

        if (leftSpec == null && rightSpec == null && topSpec == null && bottomSpec == null) {
            if (cell.isBorder()) {
                setLeftBorder(LineType2.SOLID, LineWidth.MM_0_1, cell.getBorderColor());
                setRightBorder(LineType2.SOLID, LineWidth.MM_0_1, cell.getBorderColor());
                setTopBorder(LineType2.SOLID, LineWidth.MM_0_1, cell.getBorderColor());
                setBottomBorder(LineType2.SOLID, LineWidth.MM_0_1, cell.getBorderColor());
            } else {
                setLeftBorder(LineType2.NONE, LineWidth.MM_0_12, cell.getBorderColor());
                setRightBorder(LineType2.NONE, LineWidth.MM_0_12, cell.getBorderColor());
                setTopBorder(LineType2.NONE, LineWidth.MM_0_12, cell.getBorderColor());
                setBottomBorder(LineType2.NONE, LineWidth.MM_0_12, cell.getBorderColor());
                setSlash();
                setBackSlash();
            }

            setFillBrush(cell.getBackgroundColor(), "#000000");
            headerXMLFile.refList().borderFills().add(borderFill);
            return borderFill;
        }

        applySide("left", leftSpec);
        applySide("right", rightSpec);
        applySide("top", topSpec);
        applySide("bottom", bottomSpec);

        setSlash();
        setBackSlash();
        setFillBrush(cell.getBackgroundColor(), "#000000");

        headerXMLFile.refList().borderFills().add(borderFill);
        return borderFill;
    }

    private void applySide(String side, Map<String, Object> spec) {
        LineType2 type = parseLineType(spec);
        LineWidth width = parseLineWidthExact(spec);
        String color = parseColor(spec);

        if ("left".equals(side)) setLeftBorder(type, width, color);
        else if ("right".equals(side)) setRightBorder(type, width, color);
        else if ("top".equals(side)) setTopBorder(type, width, color);
        else if ("bottom".equals(side)) setBottomBorder(type, width, color);
    }

    private LineType2 parseLineType(Map<String, Object> spec) {
        if (spec == null) return LineType2.SOLID;
        Object s = spec.get("style");
        if (s == null) return LineType2.SOLID;

        String v = String.valueOf(s).trim().toLowerCase();
        if ("none".equals(v) || "hidden".equals(v)) return LineType2.NONE;
        return LineType2.SOLID;
    }

    /** - LineWidth enum에 없는 값이면 "오류 안내" (예외로 터뜨림) */
    private LineWidth parseLineWidthExact(Map<String, Object> spec) {
        if (spec == null) return LineWidth.MM_0_1;

        Object wObj = spec.get("width");
        if (wObj == null) return LineWidth.MM_0_1;

        double mm;
        if (wObj instanceof Number) {
            mm = ((Number) wObj).doubleValue();
        } else {
            try { mm = Double.parseDouble(String.valueOf(wObj).trim()); }
            catch (Exception e) {
                throw new IllegalArgumentException("border.width must be a number (mm). got=" + wObj);
            }
        }

        LineWidth lw = matchExactLineWidth(mm);
        if (lw == null) {
            throw new IllegalArgumentException(
                    "Unsupported border width(mm): " + mm +
                    ". Allowed: 0.1,0.12,0.15,0.2,0.25,0.3,0.4,0.5,0.6,0.7,1.0,1.5,2.0,3.0,4.0,5.0"
            );
        }
        return lw;
    }

    private LineWidth matchExactLineWidth(double mm) {
        String s = stripTrailingZeros(mm);

        if (s.equals("0.1")) return LineWidth.MM_0_1;
        if (s.equals("0.12")) return LineWidth.MM_0_12;
        if (s.equals("0.15")) return LineWidth.MM_0_15;
        if (s.equals("0.2")) return LineWidth.MM_0_2;
        if (s.equals("0.25")) return LineWidth.MM_0_25;
        if (s.equals("0.3")) return LineWidth.MM_0_3;
        if (s.equals("0.4")) return LineWidth.MM_0_4;
        if (s.equals("0.5")) return LineWidth.MM_0_5;
        if (s.equals("0.6")) return LineWidth.MM_0_6;
        if (s.equals("0.7")) return LineWidth.MM_0_7;
        if (s.equals("1") || s.equals("1.0")) return LineWidth.MM_1_0;
        if (s.equals("1.5")) return LineWidth.MM_1_5;
        if (s.equals("2") || s.equals("2.0")) return LineWidth.MM_2_0;
        if (s.equals("3") || s.equals("3.0")) return LineWidth.MM_3_0;
        if (s.equals("4") || s.equals("4.0")) return LineWidth.MM_4_0;
        if (s.equals("5") || s.equals("5.0")) return LineWidth.MM_5_0;

        return null;
    }

    private String stripTrailingZeros(double v) {
        String s = String.valueOf(v);
        if (s.contains("E") || s.contains("e")) return s; // 혹시 모를 케이스
        if (!s.contains(".")) return s;
        // 0.600000 -> 0.6
        while (s.endsWith("0")) s = s.substring(0, s.length() - 1);
        if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private String parseColor(Map<String, Object> spec) {
        if (spec == null) return (cell != null ? cell.getBorderColor() : "#000000");
        Object c = spec.get("color");
        if (c == null) return (cell != null ? cell.getBorderColor() : "#000000");
        return String.valueOf(c).trim();
    }
}
