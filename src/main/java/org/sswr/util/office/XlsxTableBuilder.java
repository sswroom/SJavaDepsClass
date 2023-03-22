package org.sswr.util.office;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sswr.util.data.TableBuilder;

public class XlsxTableBuilder implements TableBuilder
{
	private XSSFWorkbook wb;
	private XSSFSheet sheet;
	private int currRow;

	public XlsxTableBuilder(String sheetName)
	{
		this.wb = new XSSFWorkbook();
		this.sheet = this.wb.createSheet(sheetName);
		this.currRow = 0;
	}
	@Override
	public void appendRow()
	{
		this.currRow++;
	}

	@Override
	public void appendRow(Iterable<?> rowData)
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
			}
			else if (o instanceof String)
			{
				cell = row.createCell(col, CellType.STRING);
				cell.setCellValue((String)o);
			}
			else
			{
				cell = row.createCell(col, CellType.STRING);
				cell.setCellValue(o.toString());
			}
			col++;
		}
	}

	@Override
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
			return null;
		}
	}
		
}
