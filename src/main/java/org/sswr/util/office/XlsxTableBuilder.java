package org.sswr.util.office;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sswr.util.data.TableBuilder;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class XlsxTableBuilder implements TableBuilder
{
	private XSSFWorkbook wb;
	private XSSFSheet sheet;
	private XSSFCellStyle style;
	private int currRow;

	public XlsxTableBuilder(@Nonnull String sheetName)
	{
		this.wb = new XSSFWorkbook();
		this.sheet = this.wb.createSheet(sheetName);
		this.currRow = 0;
		this.style = null;
	}

	@Nonnull
	public XSSFCellStyle getCellStyle()
	{
		if (this.style == null)
		{
			this.style = this.wb.createCellStyle();
		}
		return this.style;
	}

	@Override
	public void appendRow()
	{
		this.currRow++;
	}

	@Override
	public void appendRow(@Nullable Iterable<?> rowData)
	{
		if (rowData == null)
		{
			this.currRow++;
			return;
		}
		XSSFRow row = this.sheet.createRow(this.currRow++);
		int col = 0;
		Iterator<?> it = rowData.iterator();
		while (it.hasNext())
		{
			Object o = it.next();
			XSSFCell cell;
			if (o == null)
			{

			}
			else if (o instanceof Integer)
			{
				cell = row.createCell(col, CellType.NUMERIC);
				cell.setCellValue(((Integer)o).intValue());
				if (this.style != null) cell.setCellStyle(this.style);
			}
			else if (o instanceof String)
			{
				cell = row.createCell(col, CellType.STRING);
				cell.setCellValue((String)o);
				if (this.style != null) cell.setCellStyle(this.style);
			}
			else
			{
				cell = row.createCell(col, CellType.STRING);
				cell.setCellValue(o.toString());
				if (this.style != null) cell.setCellStyle(this.style);
			}
			col++;
		}
	}

	@Override
	@Nonnull
	public byte[] build()
	{
		ByteArrayOutputStream stm = new ByteArrayOutputStream();
		try
		{
			this.wb.write(stm);
			return stm.toByteArray();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return new byte[0];
		}
	}
		
}
