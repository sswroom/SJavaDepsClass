package org.sswr.util.office;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.media.ImageUtil;
import org.sswr.util.unit.Distance;
import org.sswr.util.unit.Distance.DistanceUnit;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.PresetColor;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFColorRgbBinary;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFChartAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xddf.usermodel.text.XDDFTextBody;
import org.apache.poi.xddf.usermodel.text.XDDFTextParagraph;
import org.apache.poi.xddf.usermodel.text.XDDFTextRun;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSimpleShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XlsxUtil {
	public enum StyleProperty
	{
		SP_ALIGNMENT,
		SP_VERTICAL_ALIGNMENT,
		SP_BORDER_BOTTOM,
		SP_BORDER_LEFT,
		SP_BORDER_RIGHT,
		SP_BORDER_TOP,
		SP_BOTTOM_BORDER_COLOR,
		SP_DATA_FORMAT,
		SP_FILL_PATTERN,
		SP_FILL_FOREGROUND_COLOR,
		SP_FILL_BACKGROUND_COLOR,
		SP_FONT,
		SP_HIDDEN,
		SP_INDENTION,
		SP_LEFT_BORDER_COLOR,
		SP_LOCKED,
		SP_RIGHT_BORDER_COLOR,
		SP_ROTATION,
		SP_TOP_BORDER_COLOR,
		SP_WRAP_TEXT
	}

	public enum UnderlineType
	{
		UT_NONE,
		UT_SINGLE,
		UT_DOUBLE
	}

	public enum AxisType
	{
		AT_DATE,
		AT_CATEGORY,
		AT_NUMERIC
	}

	public static final XDDFColor seriesColor[] = {
		XDDFColor.from(PresetColor.PALE_GOLDENROD),
		XDDFColor.from(PresetColor.AQUA),
		XDDFColor.from(PresetColor.FUCHSIA),
		XDDFColor.from(PresetColor.BLUE_VIOLET),
		XDDFColor.from(PresetColor.LAVENDER),
		XDDFColor.from(PresetColor.GREEN_YELLOW),
		XDDFColor.from(PresetColor.KHAKI),
		XDDFColor.from(PresetColor.HONEYDEW),
		XDDFColor.from(PresetColor.MAGENTA),
		XDDFColor.from(PresetColor.ORCHID),
		XDDFColor.from(PresetColor.THISTLE)
	};

	public static void addPicture(Sheet sheet, byte img[], int format, DistanceUnit dUnit, double x, double y, double w, double h)
	{
		Workbook wb = sheet.getWorkbook();
		int pictureIndex = wb.addPicture(img, format);
		Drawing<?> drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = createAnchor(sheet, dUnit, x, y, w, h);
/*		ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
		anchor.setAnchorType(AnchorType.MOVE_AND_RESIZE);
		anchor.setDx1((int)Distance.convert(dUnit, DistanceUnit.DU_EMU, x));
		anchor.setDy1((int)Distance.convert(dUnit, DistanceUnit.DU_EMU, y));
		anchor.setDx2((int)Distance.convert(dUnit, DistanceUnit.DU_EMU, x + w));
		anchor.setDy2((int)Distance.convert(dUnit, DistanceUnit.DU_EMU, y + h));*/
		drawing.createPicture(anchor, pictureIndex);
	}

	public static void addWordArt(Sheet sheet, String text, int fontColor, int borderColor, DistanceUnit dUnit, double x, double y, double w, double h)
	{
		if (sheet instanceof XSSFSheet)
		{
			Drawing<?> drawing = sheet.createDrawingPatriarch();
			ClientAnchor anchor = createAnchor(sheet, dUnit, x, y, w, h);
			XSSFDrawing xssfDrawing = (XSSFDrawing)drawing;
			XSSFSimpleShape shape = xssfDrawing.createSimpleShape((XSSFClientAnchor)anchor);
			shape.setText(text);
			XDDFTextBody body = shape.getTextBody();
			XDDFTextParagraph p = body.getParagraph(0);
			XDDFTextRun r = p.getTextRuns().get(0);
			byte []c = new byte[4];
			ByteTool.writeMInt32(c, 0, fontColor);
			r.setFontColor(new XDDFColorRgbBinary(c));
			r.setFontSize(20.0);
			XDDFLineProperties line = new XDDFLineProperties();
			XDDFSolidFillProperties lineFill = new XDDFSolidFillProperties();
			line.setWidth(0.5);
			ByteTool.writeMInt32(c, 0, borderColor);
			lineFill.setColor(new XDDFColorRgbBinary(c));
			line.setFillProperties(lineFill);
			r.setLineProperties(line);
		}
	}

	public static double getColumnWidthInch(Sheet sheet, int colIndex)
	{
		return sheet.getColumnWidth(colIndex) * 3 / 10000.0;
	}

	public static void setColumnWidthInch(Sheet sheet, int colIndex, double inchWidth)
	{
		sheet.setColumnWidth(colIndex, (int)(inchWidth * 10000 / 3));
	}

	public static void setColumnWidthInchs(Sheet sheet, double inchWidths[])
	{
		int i = 0;
		int j = inchWidths.length;
		while (i < j)
		{
			setColumnWidthInch(sheet, i, inchWidths[i]);
			i++;
		}
	}

	public static Row getRow(Sheet sheet, int rowIndex)
	{
		Row row = sheet.getRow(rowIndex);
		if (row == null)
		{
			row = sheet.createRow(rowIndex);
		}
		return row;
	}

	public static void setRowHeight(Sheet sheet, int index, DistanceUnit du, double val)
	{
		Row row = getRow(sheet, index);
		row.setHeightInPoints((float)Distance.convert(du, DistanceUnit.DU_POINT, val));
	}

	public static void setRowHeightAuto(Sheet sheet, int index)
	{
		Row row = getRow(sheet, index);
		row.setHeight((short)-1);
	}

	public static double getRowHeight(Sheet sheet, int rowNum, DistanceUnit dUnit)
	{
		return Distance.convert(DistanceUnit.DU_POINT, dUnit, getRow(sheet, rowNum).getHeightInPoints());
	}

	public static double getRowsHeight(Sheet sheet, int rowStart, int rowEnd, DistanceUnit dUnit)
	{
		double ptHeight = 0;
		Row row;
		while (rowStart <= rowEnd)
		{
			row = getRow(sheet, rowStart);
			ptHeight += row.getHeightInPoints();
			rowStart++;
		}
		return Distance.convert(DistanceUnit.DU_POINT, dUnit, ptHeight);
	}

	public static Font createFont(Workbook wb, String fontName, double fontSize, boolean bold)
	{
		Font f = wb.createFont();
		f.setFontName(fontName);
		f.setFontHeight((short)(fontSize * 20.0));
		if (bold) f.setBold(true);
		return f;
	}

	public static Font setFontColor(Font f, IndexedColors color)
	{
		f.setColor(color.getIndex());
		return f;
	}

	public static Font setFontColorIndex(Font f, short colorIndex)
	{
		f.setColor(colorIndex);
		return f;
	}

	public static Font setFontUnderline(Font f, UnderlineType underline)
	{
		byte ulByte = Font.U_NONE;
		switch (underline)
		{
		case UT_NONE:
			ulByte = Font.U_NONE;
			break;
		case UT_SINGLE:
			ulByte = Font.U_SINGLE;
			break;
		case UT_DOUBLE:
			ulByte = Font.U_DOUBLE;
			break;
		}
		f.setUnderline(ulByte);
		return f;
	}

	public static CellStyle createCellStyle(Workbook wb, Font f, HorizontalAlignment halign, VerticalAlignment valign, String dataFormat)
	{
		CellStyle style = wb.createCellStyle();
		if (f != null) style.setFont(f);
		if (halign != null) style.setAlignment(halign);
		if (valign != null) style.setVerticalAlignment(valign);
		if (dataFormat != null) style.setDataFormat(wb.createDataFormat().getFormat(dataFormat));
		return style;
	}

	public static CellStyle setStyleWrapText(CellStyle style, boolean wrapped)
	{
		style.setWrapText(wrapped);
		return style;
	}

	public static CellStyle setStyleBgColor(Workbook wb, CellStyle style, int colorArgb)
	{
		if (style.getClass().equals(XSSFCellStyle.class))
		{
			IndexedColorMap colorMap = ((XSSFWorkbook)wb).getStylesSource().getIndexedColors();
			int i = findSimilarColor(colorMap, colorArgb, 256);
			if (i >= 0)
			{
				style.setFillForegroundColor((short)i);
			}
			else
			{
				byte col[] = new byte[]{(byte)((colorArgb >> 16) & 255), (byte)((colorArgb >> 8) & 255), (byte)(colorArgb & 255)};
				XSSFColor xcolor = new XSSFColor(col, colorMap);
				((XSSFCellStyle)style).setFillForegroundColor(xcolor);
			}
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		}
		else if (style.getClass().equals(HSSFCellStyle.class))
		{
			HSSFPalette palette = ((HSSFWorkbook)wb).getCustomPalette();
			HSSFColor c = palette.findSimilarColor((colorArgb >> 16) & 255, (colorArgb >> 8) & 255, colorArgb & 255);
			style.setFillForegroundColor(c.getIndex());
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		}
		return style;
	}

	public static boolean isStylePropertyValid(CellStyle style, StyleProperty property, Object value)
	{
		if (value == null)
			return true;
		Class<?> cls = value.getClass();
		switch (property)
		{
		case SP_ALIGNMENT:
			return cls.equals(HorizontalAlignment.class);
		case SP_VERTICAL_ALIGNMENT:
			return cls.equals(VerticalAlignment.class);
		case SP_BORDER_BOTTOM:
			return cls.equals(BorderStyle.class);
		case SP_BORDER_LEFT:
			return cls.equals(BorderStyle.class);
		case SP_BORDER_RIGHT:
			return cls.equals(BorderStyle.class);
		case SP_BORDER_TOP:
			return cls.equals(BorderStyle.class);
		case SP_BOTTOM_BORDER_COLOR:
			return cls.equals(Short.class);
		case SP_DATA_FORMAT:
			return cls.equals(Short.class);
		case SP_FILL_PATTERN:
			return cls.equals(FillPatternType.class);
		case SP_FILL_FOREGROUND_COLOR:
			if (style.getClass().equals(XSSFCellStyle.class))
			{
				return cls.equals(XSSFColor.class);
			}
			else
			{
				return cls.equals(Short.class);
			}
		case SP_FILL_BACKGROUND_COLOR:
			if (style.getClass().equals(XSSFCellStyle.class))
			{
				return cls.equals(XSSFColor.class);
			}
			else
			{
				return cls.equals(Short.class);
			}
		case SP_FONT:
			return cls.equals(Integer.class);
		case SP_HIDDEN:
			return cls.equals(Boolean.class);
		case SP_INDENTION:
			return cls.equals(Short.class);
		case SP_LEFT_BORDER_COLOR:
			return cls.equals(Short.class);
		case SP_LOCKED:
			return cls.equals(Boolean.class);
		case SP_RIGHT_BORDER_COLOR:
			return cls.equals(Short.class);
		case SP_ROTATION:
			return cls.equals(Short.class);
		case SP_TOP_BORDER_COLOR:
			return cls.equals(Short.class);
		case SP_WRAP_TEXT:
			return cls.equals(Boolean.class);
		}
		return false;
	}

	public static Object getStyleProperty(CellStyle style, StyleProperty property)
	{
		switch (property)
		{
		case SP_ALIGNMENT:
			return style.getAlignment();
		case SP_VERTICAL_ALIGNMENT:
			return style.getVerticalAlignment();
		case SP_BORDER_BOTTOM:
			return style.getBorderBottom();
		case SP_BORDER_LEFT:
			return style.getBorderLeft();
		case SP_BORDER_RIGHT:
			return style.getBorderRight();
		case SP_BORDER_TOP:
			return style.getBorderTop();
		case SP_BOTTOM_BORDER_COLOR:
			return style.getBottomBorderColor();
		case SP_DATA_FORMAT:
			return style.getDataFormat();
		case SP_FILL_PATTERN:
			return style.getFillPattern();
		case SP_FILL_FOREGROUND_COLOR:
			if (style.getClass().equals(XSSFCellStyle.class))
			{
				return style.getFillForegroundColorColor();
			}
			else
			{
				return style.getFillBackgroundColor();
			}
		case SP_FILL_BACKGROUND_COLOR:
			if (style.getClass().equals(XSSFCellStyle.class))
			{
				return style.getFillBackgroundColorColor();
			}
			else
			{
				return style.getFillBackgroundColor();
			}
		case SP_FONT:
			return style.getFontIndexAsInt();
		case SP_HIDDEN:
			return style.getHidden();
		case SP_INDENTION:
			return style.getIndention();
		case SP_LEFT_BORDER_COLOR:
			return style.getLeftBorderColor();
		case SP_LOCKED:
			return style.getLocked();
		case SP_RIGHT_BORDER_COLOR:
			return style.getRightBorderColor();
		case SP_ROTATION:
			return style.getRotation();
		case SP_TOP_BORDER_COLOR:
			return style.getTopBorderColor();
		case SP_WRAP_TEXT:
			return style.getWrapText();
		}
		return null;
	}

	public static void setStyleProperty(Workbook wb, CellStyle style, StyleProperty property, Object value)
	{
		if (value == null)
		{
			return;
		}
		switch (property)
		{
		case SP_ALIGNMENT:
			style.setAlignment((HorizontalAlignment)value);
			return;
		case SP_VERTICAL_ALIGNMENT:
			style.setVerticalAlignment((VerticalAlignment)value);
			return;
		case SP_BORDER_BOTTOM:
			style.setBorderBottom((BorderStyle)value);
			return;
		case SP_BORDER_LEFT:
			style.setBorderLeft((BorderStyle)value);
			return;
		case SP_BORDER_RIGHT:
			style.setBorderRight((BorderStyle)value);
			return;
		case SP_BORDER_TOP:
			style.setBorderTop((BorderStyle)value);
			return;
		case SP_BOTTOM_BORDER_COLOR:
			style.setBottomBorderColor((Short)value);
			return;
		case SP_DATA_FORMAT:
			style.setDataFormat((Short)value);
			return;
		case SP_FILL_PATTERN:
			style.setFillPattern((FillPatternType)value);
			return;
		case SP_FILL_FOREGROUND_COLOR:
			if (style.getClass().equals(XSSFCellStyle.class))
			{
				((XSSFCellStyle)style).setFillForegroundColor((XSSFColor)value);
			}
			else
			{
				style.setFillForegroundColor((Short)value);
			}
			return;
		case SP_FILL_BACKGROUND_COLOR:
			if (style.getClass().equals(XSSFCellStyle.class))
			{
				((XSSFCellStyle)style).setFillBackgroundColor((XSSFColor)value);
			}
			else
			{
				style.setFillBackgroundColor((Short)value);
			}
			return;
		case SP_FONT:
			style.setFont(wb.getFontAt((Integer)value));
			return;
		case SP_HIDDEN:
			style.setHidden((Boolean)value);
			return;
		case SP_INDENTION:
			style.setIndention((Short)value);
			return;
		case SP_LEFT_BORDER_COLOR:
			style.setLeftBorderColor((Short)value);
			return;
		case SP_LOCKED:
			style.setLocked((Boolean)value);
			return;
		case SP_RIGHT_BORDER_COLOR:
			style.setRightBorderColor((Short)value);
			return;
		case SP_ROTATION:
			style.setRotation((Short)value);
			return;
		case SP_TOP_BORDER_COLOR:
			style.setTopBorderColor((Short)value);
			return;
		case SP_WRAP_TEXT:
			style.setWrapText((Boolean)value);
			return;
		}
	}

	public static Map<StyleProperty, Object> getStyleProperties(CellStyle style)
	{
		HashMap<StyleProperty, Object> retMap = new HashMap<StyleProperty, Object>();
		StyleProperty props[] = StyleProperty.values();
		int i = 0;
		int j = props.length;
		while (i < j)
		{
			retMap.put(props[i], getStyleProperty(style, props[i]));
			i++;
		}
		return retMap;
	}

	public static void setStyleProperties(Workbook wb, CellStyle style, Map<StyleProperty, Object> propMap)
	{
		StyleProperty props[] = StyleProperty.values();
		int i = 0;
		int j = props.length;
		while (i < j)
		{
			setStyleProperty(wb, style, props[i], propMap.get(props[i]));
			i++;
		}
	}

	public static Cell getCell(Row row, int cellIndex)
	{
		Cell cell = row.getCell(cellIndex);
		if (cell == null)
		{
			cell = row.createCell(cellIndex);
		}
		return cell;
	}

	public static String getCellStr (Row row, int cellIndex)
	{
		Cell cell = row.getCell(cellIndex);
		if (cell == null)
		{
			return null;
		}

		if (cell.getCellType() == CellType.STRING)
		{
			return cell.getStringCellValue();
		}
		if (cell.getCellType() == CellType.BLANK)
		{
			return "";
		}
		if (cell.getCellType() == CellType.NUMERIC)
		{
			return ""+cell.getNumericCellValue();
		}
		if (cell.getCellType() == CellType.BOOLEAN)
		{
			return ""+cell.getBooleanCellValue();
		}
		return null;
	}

	public static void setCell(Row row, int cellIndex, CellStyle style, String val)
	{
		Cell cell = getCell(row, cellIndex);
		cell.setCellValue(val);
		cell.setCellStyle(style);
	}

	public static void setCell(Row row, int cellIndex, CellStyle style, RichTextString val)
	{
		Cell cell = getCell(row, cellIndex);
		cell.setCellValue(val);
		if (style != null)
		{
			cell.setCellStyle(style);
		}
	}

	public static void setCell(Row row, int cellIndex, CellStyle style, Timestamp val)
	{
		Cell cell = getCell(row, cellIndex);
		if (val != null)
		{
			cell.setCellValue(DateTimeUtil.clearMs(val));
		}
		else
		{
			cell.setCellValue(val);
		}
		cell.setCellStyle(style);
	}

	public static void setCell(Row row, int cellIndex, CellStyle style, Double val)
	{
		Cell cell = getCell(row, cellIndex);
		if (val != null)
		{
			cell.setCellValue(val);
		}
		cell.setCellStyle(style);
	}

	public static void setCell(Row row, int cellIndex, CellStyle style, Integer val)
	{
		Cell cell = getCell(row, cellIndex);
		if (val != null)
		{
			cell.setCellValue(val);
		}
		cell.setCellStyle(style);
	}

	public static void setCell(Row row, int cellIndex, CellStyle style, double val, String formula)
	{
		Cell cell = getCell(row, cellIndex);
		cell.setCellValue(val);
		if (formula != null)
		{
			cell.setCellFormula(formula);
		}
		cell.setCellStyle(style);
	}

	public static void setCellStyle(Row row, int cellIndex, CellStyle style)
	{
		getCell(row, cellIndex).setCellStyle(style);
	}

	public static void setCellBgColor(Row row, int cellIndex, int colorArgb)
	{
		Sheet sheet = row.getSheet();
		Workbook wb = sheet.getWorkbook();
		Cell cell = getCell(row, cellIndex);
		if (wb.getClass().equals(XSSFWorkbook.class))
		{
			IndexedColorMap colorMap = ((XSSFWorkbook)wb).getStylesSource().getIndexedColors();
			int i = findSimilarColor(colorMap, colorArgb, 256);
			XSSFColor xcolor;
			if (i >= 0)
			{
				xcolor = new XSSFColor(IndexedColors.fromInt(i), colorMap);
			}
			else
			{
				byte col[] = new byte[]{(byte)((colorArgb >> 16) & 255), (byte)((colorArgb >> 8) & 255), (byte)(colorArgb & 255)};
				xcolor = new XSSFColor(col, colorMap);
			}
			setCellProperty(cell, StyleProperty.SP_FILL_FOREGROUND_COLOR, xcolor);
		}
		else if (wb.getClass().equals(HSSFWorkbook.class))
		{
			HSSFPalette palette = ((HSSFWorkbook)wb).getCustomPalette();
			HSSFColor c = palette.findSimilarColor((colorArgb >> 16) & 255, (colorArgb >> 8) & 255, colorArgb & 255);
			setCellProperty(cell, StyleProperty.SP_FILL_FOREGROUND_COLOR, c.getIndex());
		}
		setCellProperty(cell, StyleProperty.SP_FILL_PATTERN, FillPatternType.SOLID_FOREGROUND);
	}

	public static void setCells(Row row, CellStyle style, String vals[], boolean autoFilter)
	{
		int i = 0;
		int j = vals.length;
		while (i < j)
		{
			setCell(row, i, style, vals[i]);
			i++;
		}
		if (autoFilter)
		{
			row.getSheet().setAutoFilter(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, j - 1));
		}
	}


	public static void setCellsBorderLeft(Sheet sheet, int colIndex, int rowStart, int rowEnd, BorderStyle border)
	{
		while (rowStart <= rowEnd)
		{
			Row row = getRow(sheet, rowStart++);
			Cell cell = getCell(row, colIndex);
			setCellProperty(cell, StyleProperty.SP_BORDER_LEFT, border);
		}
	}

	public static void setCellsBorderTop(Sheet sheet, int rowIndex, int colStart, int colEnd, BorderStyle border)
	{
		Row row = getRow(sheet, rowIndex);
		while (colStart <= colEnd)
		{
			Cell cell = getCell(row, colStart++);
			setCellProperty(cell, StyleProperty.SP_BORDER_TOP, border);
		}
	}

	public static void setCellsBorderRight(Sheet sheet, int colIndex, int rowStart, int rowEnd, BorderStyle border)
	{
		while (rowStart <= rowEnd)
		{
			Row row = getRow(sheet, rowStart++);
			Cell cell = getCell(row, colIndex);
			setCellProperty(cell, StyleProperty.SP_BORDER_RIGHT, border);
		}
	}

	public static void setCellsBorderBottom(Sheet sheet, int rowIndex, int colStart, int colEnd, BorderStyle border)
	{
		Row row = getRow(sheet, rowIndex);
		while (colStart <= colEnd)
		{
			Cell cell = getCell(row, colStart++);
			setCellProperty(cell, StyleProperty.SP_BORDER_BOTTOM, border);
		}
	}

	public static void setCellProperty(Cell cell, StyleProperty property, Object value)
	{
		Workbook wb = cell.getSheet().getWorkbook();
		CellStyle oriStyle = cell.getCellStyle();
		if (!isStylePropertyValid(oriStyle, property, value))
		{
			return;
		}
		Map<StyleProperty, Object> props = getStyleProperties(oriStyle);
		props.put(property, value);
		int i = 0;
		int j = wb.getNumCellStyles();
		while (i < j)
		{
			Map<StyleProperty, Object> wbProps = getStyleProperties(wb.getCellStyleAt(i));
			if (wbProps.equals(props))
			{
				cell.setCellStyle(wb.getCellStyleAt(i));
				return;
			}
			i++;
		}
		CellStyle newStyle = wb.createCellStyle();
		setStyleProperties(wb, newStyle, props);
		cell.setCellStyle(newStyle);
	}


	public static XlsxConditionalFormatBuilder buildCondFormat()
	{
		return new XlsxConditionalFormatBuilder();
	}

	public static void setConditionalFormat(Row row, int colIndex, XlsxConditionalFormatBuilder builder)
	{
		Sheet sheet = row.getSheet();
		SheetConditionalFormatting sCondFmt = sheet.getSheetConditionalFormatting();
		ConditionalFormatting condFmt = builder.getFormatting(sCondFmt);
		condFmt.setFormattingRanges(cellRangesAddCell(condFmt.getFormattingRanges(), row.getRowNum(), colIndex));
	}

	public static CellRangeAddress[] cellRangesAddCell(CellRangeAddress[] cellRanges, int rowIndex, int colIndex)
	{
		CellRangeAddress cellRange;
		int i = 0;
		int j = cellRanges.length;
		while (i < j)
		{
			cellRange = cellRanges[i];
			if (cellRange.isInRange(rowIndex, colIndex))
			{
				return cellRanges;
			}
			if (cellRange.getFirstRow() == rowIndex && cellRange.getLastRow() == rowIndex)
			{
				if (cellRange.getFirstColumn() == colIndex + 1)
				{
					cellRange.setFirstColumn(colIndex);
					return cellRanges;
				}
				else if (cellRange.getLastColumn() == colIndex - 1)
				{
					cellRange.setLastColumn(colIndex);
					return cellRanges;
				}
			}
			else if (cellRange.getFirstColumn() == colIndex && cellRange.getLastColumn() == colIndex)
			{
				if (cellRange.getFirstRow() == rowIndex + 1)
				{
					cellRange.setFirstRow(rowIndex);
					return cellRanges;
				}
				else if (cellRange.getLastRow() == rowIndex - 1)
				{
					cellRange.setLastRow(rowIndex);
					return cellRanges;
				}
			}
			i++;
		}
		CellRangeAddress[] newCellRanges = new CellRangeAddress[j + 1];
		i = 0;
		while (i < j)
		{
			newCellRanges[i] = cellRanges[i];
			i++;
		}
		newCellRanges[j] = new CellRangeAddress(rowIndex, rowIndex, colIndex, colIndex);
		return newCellRanges;
	}

	public static String colCode(int colIndex)
	{
		char vals[];
		if (colIndex < 26)
		{
			return Character.toString(65 + colIndex);
		}
		colIndex -= 26;
		if (colIndex < 26 * 26)
		{
			vals = new char[2];
			vals[0] = (char)(65 + (colIndex / 26));
			vals[1] = (char)(65 + (colIndex % 26));
			return String.valueOf(vals);
		}
		colIndex -= 26 * 26;
		vals = new char[3];
		vals[2] = (char)(65 + (colIndex % 26));
		colIndex = colIndex / 26;
		vals[1] = (char)(65 + (colIndex % 26));
		vals[0] = (char)(65 + (colIndex / 26));
		return String.valueOf(vals);
	}

	public static int findSimilarColor(IndexedColorMap colorMap, int color, int maxDiff)
	{
		int minIndex = -1;
		int minDiff = maxDiff + 1;
		int rDiff;
		int gDiff;
		int bDiff;
		int i;
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;
		byte c[];
		i = 0;
		while (true)
		{
			c = colorMap.getRGB(i);
			if (c == null)
			{
				break;
			}
			System.out.println("i="+i+",r="+c[0]+",g="+c[1]+",b="+c[2]);
			rDiff = Math.abs(c[0] - r);
			gDiff = Math.abs(c[1] - g);
			bDiff = Math.abs(c[2] - b);
			if ((rDiff + gDiff + bDiff) < minDiff)
			{
				minDiff = rDiff + gDiff + bDiff;
				minIndex = i;
			}
			i++;
		}

		return minIndex;
	}

	public static int getImgFormat(byte imgBuff[])
	{
		String imgFmt = ImageUtil.getImageFmt(imgBuff);
		switch (imgFmt)
		{
		case "png":
			return Workbook.PICTURE_TYPE_PNG;
		case "jpg":
			return Workbook.PICTURE_TYPE_JPEG;
		default:
			return Workbook.PICTURE_TYPE_JPEG;
		}
	}

	public static ClientAnchor createAnchor(Sheet sheet, DistanceUnit du, double x, double y, double w, double h)
	{
		DistanceUnit emu = DistanceUnit.DU_EMU;
		DistanceUnit inch = DistanceUnit.DU_INCH;
		Drawing<?> drawing = sheet.createDrawingPatriarch();
		double ix = Distance.convert(du, inch, x);
		double iy = Distance.convert(du, inch, y);
		double ix2 = Distance.convert(du, inch, x + w);
		double iy2 = Distance.convert(du, inch, y + h);
		int col1 = 0;
		int col2 = 0;
		int row1 = 0;
		int row2 = 0;
		double size;
		while (true)
		{
			size = getColumnWidthInch(sheet, col1);
			if (ix < size)
			{
				break;
			}
			ix -= size;
			ix2 -= size;
			col1++;
			col2++;
		}

		while (true)
		{
			size = getColumnWidthInch(sheet, col2);
			if (ix2 < size)
			{
				break;
			}
			ix2 -= size;
			col2++;
		}

		while (true)
		{
			size = getRowHeight(sheet, row1, DistanceUnit.DU_INCH);
			if (iy < size)
			{
				break;
			}
			iy -= size;
			iy2 -= size;
			row1++;
			row2++;
		}

		while (true)
		{
			size = getRowHeight(sheet, row2, DistanceUnit.DU_INCH);
			if (iy2 < size)
			{
				break;
			}
			iy2 -= size;
			row2++;
		}

		return drawing.createAnchor((int)Distance.convert(inch, emu, ix),
			(int)Distance.convert(inch, emu, iy),
			(int)Distance.convert(inch, emu, ix2),
			(int)Distance.convert(inch, emu, iy2),
			col1, row1, col2, row2);
	}

	public static XSSFChart createChart(Sheet sheet, DistanceUnit du, double x, double y, double w, double h, String title)
	{
		if (!sheet.getClass().equals(XSSFSheet.class))
		{
			return null;
		}
		XSSFSheet sh = (XSSFSheet)sheet;
		XSSFDrawing drawing = sh.createDrawingPatriarch();
		XSSFClientAnchor anchor = (XSSFClientAnchor)createAnchor(sheet, du, x, y, w, h);
		XSSFChart chart = drawing.createChart(anchor);
		if (title != null)
		{
			chart.setTitleText(title);
		}
		XDDFShapeProperties shPr = chart.getOrAddShapeProperties();
		shPr.setFillProperties(new XDDFSolidFillProperties(XDDFColor.from(PresetColor.WHITE)));
		shPr.setLineProperties(new XDDFLineProperties(new XDDFSolidFillProperties()));
		return chart;
	}

	public static void chartAddLegend(XSSFChart chart, LegendPosition pos)
	{
		XDDFChartLegend legend = chart.getOrAddLegend();
		legend.setPosition(pos);
	}

	public static XDDFLineChartData lineChart(XSSFChart chart, String leftAxisName, String bottomAxisName, AxisType bottomType)
	{
		XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
		if (leftAxisName != null) leftAxis.setTitle(leftAxisName);
		leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
		leftAxis.getOrAddMajorGridProperties().setLineProperties(new XDDFLineProperties(new XDDFSolidFillProperties(XDDFColor.from(PresetColor.LIGHT_GRAY))));
		leftAxis.getOrAddShapeProperties().setLineProperties(new XDDFLineProperties(new XDDFSolidFillProperties(XDDFColor.from(PresetColor.BLACK))));
		XDDFChartAxis bottomAxis = null;
		switch (bottomType)
		{
		case AT_CATEGORY:
			bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
			break;
		case AT_DATE:
			bottomAxis = chart.createDateAxis(AxisPosition.BOTTOM);
			break;
		case AT_NUMERIC:
			bottomAxis = chart.createValueAxis(AxisPosition.BOTTOM);
			break;
		}
		if (bottomAxisName != null) bottomAxis.setTitle(bottomAxisName);
		bottomAxis.getOrAddShapeProperties().setLineProperties(new XDDFLineProperties(new XDDFSolidFillProperties(XDDFColor.from(PresetColor.BLACK))));
		return (XDDFLineChartData)chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
	}

	public static void addLineChartSeries(XDDFLineChartData data, XDDFDataSource<?> category, XDDFNumericalDataSource<? extends Number> values, String name)
	{
		int i = data.getSeriesCount();
		XDDFLineChartData.Series series = (XDDFLineChartData.Series)data.addSeries(category, values);
		if (name != null) series.setTitle(name, null);
		series.setSmooth(false);
		series.setMarkerSize((short)3);
		series.setMarkerStyle(MarkerStyle.CIRCLE);
		series.setLineProperties(new XDDFLineProperties(new XDDFSolidFillProperties(seriesColor[i % seriesColor.length])));
	}

	public static void closeWb(Workbook wb)
	{
		try
		{
			wb.close();
		}
		catch (IOException ex2)
		{

		}
	}
}
