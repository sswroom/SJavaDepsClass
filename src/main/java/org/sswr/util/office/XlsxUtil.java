package org.sswr.util.office;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.StringUtil;
import org.sswr.util.math.unit.Distance;
import org.sswr.util.math.unit.Distance.DistanceUnit;
import org.sswr.util.media.ImageUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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
import org.apache.poi.xddf.usermodel.chart.AxisTickLabelPosition;
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
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDispBlanksAs;
import org.openxmlformats.schemas.drawingml.x2006.chart.STDispBlanksAs;

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
		NONE,
		SINGLE,
		DOUBLE
	}

	public enum AxisType
	{
		DATE,
		CATEGORY,
		NUMERIC
	}

	public static final XDDFColor seriesColor[] = {
		XDDFColor.from(PresetColor.DARK_BLUE),
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

	public static void addPicture(@Nonnull Sheet sheet, @Nonnull byte img[], int format, @Nonnull DistanceUnit dUnit, double x, double y, double w, double h)
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

	public static void addWordArt(@Nonnull Sheet sheet, @Nonnull String text, int fontColor, int borderColor, @Nonnull DistanceUnit dUnit, double x, double y, double w, double h)
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

	public static double getColumnWidthInch(@Nonnull Sheet sheet, int colIndex)
	{
		return sheet.getColumnWidth(colIndex) * 3 / 10000.0;
	}

	public static void setColumnWidthInch(@Nonnull Sheet sheet, int colIndex, double inchWidth)
	{
		sheet.setColumnWidth(colIndex, (int)(inchWidth * 10000 / 3));
	}

	public static void setColumnWidthInchs(@Nonnull Sheet sheet, double inchWidths[])
	{
		int i = 0;
		int j = inchWidths.length;
		while (i < j)
		{
			setColumnWidthInch(sheet, i, inchWidths[i]);
			i++;
		}
	}

	@Nonnull
	public static Row getRow(@Nonnull Sheet sheet, int rowIndex)
	{
		Row row = sheet.getRow(rowIndex);
		if (row == null)
		{
			row = sheet.createRow(rowIndex);
		}
		return row;
	}

	public static void setRowHeight(@Nonnull Sheet sheet, int index, @Nonnull DistanceUnit du, double val)
	{
		Row row = getRow(sheet, index);
		row.setHeightInPoints((float)Distance.convert(du, DistanceUnit.Point, val));
	}

	public static void setRowHeightAuto(@Nonnull Sheet sheet, int index)
	{
		Row row = getRow(sheet, index);
		row.setHeight((short)-1);
	}

	public static double getRowHeight(@Nonnull Sheet sheet, int rowNum, @Nonnull DistanceUnit dUnit)
	{
		return Distance.convert(DistanceUnit.Point, dUnit, getRow(sheet, rowNum).getHeightInPoints());
	}

	public static double getRowsHeight(@Nonnull Sheet sheet, int rowStart, int rowEnd, @Nonnull DistanceUnit dUnit)
	{
		double ptHeight = 0;
		Row row;
		while (rowStart <= rowEnd)
		{
			row = getRow(sheet, rowStart);
			ptHeight += row.getHeightInPoints();
			rowStart++;
		}
		return Distance.convert(DistanceUnit.Point, dUnit, ptHeight);
	}

	@Nonnull
	public static Font createFont(@Nonnull Workbook wb, @Nonnull String fontName, double fontSize, boolean bold)
	{
		Font f = wb.createFont();
		f.setFontName(fontName);
		f.setFontHeight((short)(fontSize * 20.0));
		if (bold) f.setBold(true);
		return f;
	}

	@Nonnull
	public static Font setFontColor(@Nonnull Font f, @Nonnull IndexedColors color)
	{
		f.setColor(color.getIndex());
		return f;
	}

	@Nonnull
	public static Font setFontColorIndex(@Nonnull Font f, short colorIndex)
	{
		f.setColor(colorIndex);
		return f;
	}

	public static Font setFontUnderline(@Nonnull Font f, @Nonnull UnderlineType underline)
	{
		byte ulByte = Font.U_NONE;
		switch (underline)
		{
		case NONE:
			ulByte = Font.U_NONE;
			break;
		case SINGLE:
			ulByte = Font.U_SINGLE;
			break;
		case DOUBLE:
			ulByte = Font.U_DOUBLE;
			break;
		}
		f.setUnderline(ulByte);
		return f;
	}

	@Nonnull
	public static CellStyle createCellStyle(@Nonnull Workbook wb, @Nullable Font f, @Nullable HorizontalAlignment halign, @Nullable VerticalAlignment valign, @Nullable String dataFormat)
	{
		CellStyle style = wb.createCellStyle();
		if (f != null) style.setFont(f);
		if (halign != null) style.setAlignment(halign);
		if (valign != null) style.setVerticalAlignment(valign);
		if (dataFormat != null) style.setDataFormat(wb.createDataFormat().getFormat(dataFormat));
		return style;
	}

	@Nonnull
	public static CellStyle setStyleWrapText(@Nonnull CellStyle style, boolean wrapped)
	{
		style.setWrapText(wrapped);
		return style;
	}

	@Nonnull
	public static CellStyle setStyleBgColor(@Nonnull Workbook wb, @Nonnull CellStyle style, int colorArgb)
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

	public static boolean isStylePropertyValid(@Nonnull CellStyle style, @Nonnull StyleProperty property, @Nullable Object value)
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

	@Nullable
	public static Object getStyleProperty(@Nonnull CellStyle style, @Nonnull StyleProperty property)
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
			return style.getFontIndex();
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

	public static void setStyleProperty(@Nonnull Workbook wb, @Nonnull CellStyle style, @Nonnull StyleProperty property, @Nullable Object value)
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

	@Nonnull
	public static Map<StyleProperty, Object> getStyleProperties(@Nonnull CellStyle style)
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

	public static void setStyleProperties(@Nonnull Workbook wb, @Nonnull CellStyle style, @Nonnull Map<StyleProperty, Object> propMap)
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

	@Nonnull
	public static Cell getCell(@Nonnull Row row, int cellIndex)
	{
		Cell cell = row.getCell(cellIndex);
		if (cell == null)
		{
			cell = row.createCell(cellIndex);
		}
		return cell;
	}

	@Nullable
	public static String getCellStr(@Nonnull Row row, int cellIndex)
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

	public static void setCell(@Nonnull Row row, int cellIndex, @Nonnull CellStyle style, @Nonnull String val)
	{
		Cell cell = getCell(row, cellIndex);
		cell.setCellValue(val);
		cell.setCellStyle(style);
	}

	public static void setCell(@Nonnull Row row, int cellIndex, @Nullable CellStyle style, @Nonnull RichTextString val)
	{
		Cell cell = getCell(row, cellIndex);
		cell.setCellValue(val);
		if (style != null)
		{
			cell.setCellStyle(style);
		}
	}

	public static void setCell(@Nonnull Row row, int cellIndex, @Nonnull CellStyle style, @Nullable Timestamp val)
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

	public static void setCell(@Nonnull Row row, int cellIndex, @Nonnull CellStyle style, @Nullable Double val)
	{
		Cell cell = getCell(row, cellIndex);
		if (val != null)
		{
			cell.setCellValue(val);
		}
		cell.setCellStyle(style);
	}

	public static void setCell(@Nonnull Row row, int cellIndex, @Nonnull CellStyle style, @Nullable Integer val)
	{
		Cell cell = getCell(row, cellIndex);
		if (val != null)
		{
			cell.setCellValue(val);
		}
		cell.setCellStyle(style);
	}

	public static void setCell(@Nonnull Row row, int cellIndex, @Nonnull CellStyle style, double val, @Nullable String formula)
	{
		Cell cell = getCell(row, cellIndex);
		cell.setCellValue(val);
		if (formula != null)
		{
			cell.setCellFormula(formula);
		}
		cell.setCellStyle(style);
	}

	public static void setCellStyle(@Nonnull Row row, int cellIndex, @Nonnull CellStyle style)
	{
		getCell(row, cellIndex).setCellStyle(style);
	}

	public static void setCellBgColor(@Nonnull Row row, int cellIndex, int colorArgb)
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

	public static void setCells(@Nonnull Row row, @Nonnull CellStyle style, @Nonnull String vals[], boolean autoFilter)
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


	public static void setCellsBorderLeft(@Nonnull Sheet sheet, int colIndex, int rowStart, int rowEnd, @Nonnull BorderStyle border)
	{
		while (rowStart <= rowEnd)
		{
			Row row = getRow(sheet, rowStart++);
			Cell cell = getCell(row, colIndex);
			setCellProperty(cell, StyleProperty.SP_BORDER_LEFT, border);
		}
	}

	public static void setCellsBorderTop(@Nonnull Sheet sheet, int rowIndex, int colStart, int colEnd, @Nonnull BorderStyle border)
	{
		Row row = getRow(sheet, rowIndex);
		while (colStart <= colEnd)
		{
			Cell cell = getCell(row, colStart++);
			setCellProperty(cell, StyleProperty.SP_BORDER_TOP, border);
		}
	}

	public static void setCellsBorderRight(@Nonnull Sheet sheet, int colIndex, int rowStart, int rowEnd, @Nonnull BorderStyle border)
	{
		while (rowStart <= rowEnd)
		{
			Row row = getRow(sheet, rowStart++);
			Cell cell = getCell(row, colIndex);
			setCellProperty(cell, StyleProperty.SP_BORDER_RIGHT, border);
		}
	}

	public static void setCellsBorderBottom(@Nonnull Sheet sheet, int rowIndex, int colStart, int colEnd, @Nonnull BorderStyle border)
	{
		Row row = getRow(sheet, rowIndex);
		while (colStart <= colEnd)
		{
			Cell cell = getCell(row, colStart++);
			setCellProperty(cell, StyleProperty.SP_BORDER_BOTTOM, border);
		}
	}

	public static void setCellProperty(@Nonnull Cell cell, @Nonnull StyleProperty property, @Nullable Object value)
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


	@Nonnull
	public static XlsxConditionalFormatBuilder buildCondFormat()
	{
		return new XlsxConditionalFormatBuilder();
	}

	public static void setConditionalFormat(@Nonnull Row row, int colIndex, @Nonnull XlsxConditionalFormatBuilder builder)
	{
		Sheet sheet = row.getSheet();
		SheetConditionalFormatting sCondFmt = sheet.getSheetConditionalFormatting();
		ConditionalFormatting condFmt = builder.getFormatting(sCondFmt);
		if (condFmt != null)
			condFmt.setFormattingRanges(cellRangesAddCell(condFmt.getFormattingRanges(), row.getRowNum(), colIndex));
	}

	@Nonnull
	public static CellRangeAddress[] cellRangesAddCell(@Nonnull CellRangeAddress[] cellRanges, int rowIndex, int colIndex)
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

	@Nonnull
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

	public static int findSimilarColor(@Nonnull IndexedColorMap colorMap, int color, int maxDiff)
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

	public static int getImgFormat(@Nonnull byte imgBuff[])
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

	@Nonnull
	public static ClientAnchor createAnchor(@Nonnull Sheet sheet, @Nonnull DistanceUnit du, double x, double y, double w, double h)
	{
		DistanceUnit emu = DistanceUnit.Emu;
		DistanceUnit inch = DistanceUnit.Inch;
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
			size = getRowHeight(sheet, row1, DistanceUnit.Inch);
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
			size = getRowHeight(sheet, row2, DistanceUnit.Inch);
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

	public static XSSFChart createChart(@Nonnull Sheet sheet, @Nonnull DistanceUnit du, double x, double y, double w, double h, @Nullable String title)
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

	public static void chartAddLegend(@Nonnull XSSFChart chart, @Nonnull LegendPosition pos)
	{
		XDDFChartLegend legend = chart.getOrAddLegend();
		legend.setPosition(pos);
	}

	@Nonnull
	public static XDDFLineChartData lineChart(@Nonnull XSSFChart chart, @Nullable String leftAxisName, @Nullable String bottomAxisName, @Nonnull AxisType bottomType)
	{
		XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
		if (leftAxisName != null) leftAxis.setTitle(leftAxisName);
		leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
		leftAxis.getOrAddMajorGridProperties().setLineProperties(new XDDFLineProperties(new XDDFSolidFillProperties(XDDFColor.from(PresetColor.LIGHT_GRAY))));
		leftAxis.getOrAddShapeProperties().setLineProperties(new XDDFLineProperties(new XDDFSolidFillProperties(XDDFColor.from(PresetColor.BLACK))));
		XDDFChartAxis bottomAxis = null;
		switch (bottomType)
		{
		case CATEGORY:
			bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
			break;
		case DATE:
			bottomAxis = chart.createDateAxis(AxisPosition.BOTTOM);
			break;
		case NUMERIC:
			bottomAxis = chart.createValueAxis(AxisPosition.BOTTOM);
			break;
		}
		if (bottomAxis != null)
		{
			if (bottomAxisName != null) bottomAxis.setTitle(bottomAxisName);
			bottomAxis.getOrAddShapeProperties().setLineProperties(new XDDFLineProperties(new XDDFSolidFillProperties(XDDFColor.from(PresetColor.BLACK))));
			bottomAxis.setTickLabelPosition(AxisTickLabelPosition.LOW);
		}
		return (XDDFLineChartData)chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
	}

	public static void addLineChartSeries(@Nonnull XDDFLineChartData data, @Nonnull XDDFDataSource<?> category, @Nonnull XDDFNumericalDataSource<? extends Number> values, @Nullable String name, boolean showMarker)
	{
		int i = data.getSeriesCount();
		XDDFLineChartData.Series series = (XDDFLineChartData.Series)data.addSeries(category, values);
		if (name != null) series.setTitle(name, null);
		series.setSmooth(false);
		if (showMarker)
		{
			series.setMarkerSize((short)3);
			series.setMarkerStyle(MarkerStyle.CIRCLE);
		}
		else
		{
			series.setMarkerStyle(MarkerStyle.NONE);
		}
		series.setLineProperties(new XDDFLineProperties(new XDDFSolidFillProperties(seriesColor[i % seriesColor.length])));
	}

	public static void closeWb(@Nonnull Workbook wb)
	{
		try
		{
			wb.close();
		}
		catch (IOException ex2)
		{

		}
	}

	private  static int calcLineCnt(@Nonnull String s, double fontSize, double cellWidthInch)
	{
		int lineCharCnt = (int)(cellWidthInch / fontSize / 0.0066);
		int sLen = s.length();
		int ret = 0;
		while (sLen > lineCharCnt)
		{
			sLen -= lineCharCnt;
			ret++;
		}
		return ret + 1;
	}

	public static double calcCellHeight(@Nonnull String s, double fontSize, double cellWidthInch)
	{
		int lineCnt;
		if (StringUtil.isNullOrEmpty(s))
		{
			lineCnt = 1;
		}
		else
		{
			lineCnt = 0;
			int i = s.indexOf("\r");
			while (i >= 0)
			{
				lineCnt += calcLineCnt(s.substring(0, i), fontSize, cellWidthInch);
				s = s.substring(i + 1);
				if (s.startsWith("\n"))
				{
					s.substring(1);
				}
				if (s.length() <= 0)
				{
					break;
				}
				i = s.indexOf("\r");
			}
			if (s.length() > 0)
			{
				lineCnt += calcLineCnt(s, fontSize, cellWidthInch);
			}
		}
		return lineCnt * fontSize * 0.018 + 0.02;
	}

	public static double calcCellHeight(@Nonnull String s, double fontSize, @Nonnull Sheet sheet, int firstCol, int lastCol)
	{
		double totalWidth = 0;
		while (firstCol <= lastCol)
		{
			totalWidth += XlsxUtil.getColumnWidthInch(sheet, firstCol);
			firstCol++;
		}
		return calcCellHeight(s, fontSize, totalWidth);
	}

	public static void setDisplayBlankAs(@Nonnull CTChart chart, @Nonnull STDispBlanksAs.Enum blankAs)
	{
		CTDispBlanksAs val = CTDispBlanksAs.Factory.newInstance();
		val.setVal(blankAs);
		chart.setDispBlanksAs(val);
	}
}
