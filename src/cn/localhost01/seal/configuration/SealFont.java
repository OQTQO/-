package cn.localhost01.seal.configuration;

import java.awt.*;

/**
 * @Description: 印章字体类
 * @Author Ran.chunlin
 * @Date: Created in 12:17 2018-10-04
 */
public class SealFont {

    public SealFont(String fontText) {
        this.fontText = fontText;
    }

    public SealFont() {
    }

    /**
     * 字体内容
     */
    private String fontText;
    /**
     * 是否加粗
     */
    private Boolean isBold = true;
    /**
     * 字形名，默认为宋体
     */
    private String fontFamily = "宋体";
    /**
     * 字体大小
     */
    private Integer fontSize;
    /**
     * 字距
     */
    private Double fontSpace;
    /**
     * 边距（环边距或上边距）
     */
    private Integer marginSize;
    /**
     * 文字整体水平偏移。正值向右，负值向左。
     */
    private Integer offsetX = 0;
    /**
     * 文字整体垂直偏移。正值向下，负值向上。
     */
    private Integer offsetY = 0;

    /**
     * 获取系统支持的字形名集合
     */
    public static String[] getSupportFontNames() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }

    public SealFont setFontSpace(Double fontSpace) {
        this.fontSpace = fontSpace;
        return this;
    }

    public SealFont setMarginSize(Integer marginSize) {
        this.marginSize = marginSize;
        return this;
    }

    public SealFont setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        return this;
    }

    public SealFont setFontText(String fontText) {
        this.fontText = fontText;
        return this;
    }

    public SealFont setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public SealFont setBold(Boolean bold) {
        isBold = bold;
        return this;
    }

    public SealFont setOffsetX(Integer offsetX) {
        this.offsetX = offsetX == null ? 0 : offsetX;
        return this;
    }

    public SealFont setOffsetY(Integer offsetY) {
        this.offsetY = offsetY == null ? 0 : offsetY;
        return this;
    }

    public String getFontText() {
        return fontText;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public Double getFontSpace() {
        return fontSpace;
    }

    public Integer getMarginSize() {
        return marginSize;
    }

    public Boolean isBold() {
        return isBold;
    }

    public Integer getOffsetX() {
        return offsetX == null ? 0 : offsetX;
    }

    public Integer getOffsetY() {
        return offsetY == null ? 0 : offsetY;
    }
}
