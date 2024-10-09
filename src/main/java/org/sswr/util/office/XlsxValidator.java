package org.sswr.util.office;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.StreamUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.jpa.repository.JpaRepository;

public class XlsxValidator {
	private Workbook wb;
	private Sheet sheet;
	private String headers[];
	private String lastError;
	private boolean fileValid;
	private int headerRow;
	private int nextRowInd;
	private Row currRow;
	private boolean trimStr;

	public XlsxValidator(@Nonnull InputStream stm, @Nonnull String headers[])
	{
		this(stm, headers, 0, false);
	}

	public XlsxValidator(@Nonnull InputStream stm, @Nonnull String headers[], int headerRow)
	{
		this(stm, headers, headerRow, false);
	}

	public XlsxValidator(@Nonnull InputStream stm, @Nonnull String headers[], int headerRow, boolean supportXls)
	{
		this.fileValid = false;
		this.lastError = null;
		this.headers = headers;
		this.trimStr = false;
		this.headerRow = headerRow;
		try
		{
			this.wb = new XSSFWorkbook(stm);
		}
		catch (IOException ex)
		{
			this.lastError = "File not found";
			return;
		}
		catch (Exception ex)
		{
			this.lastError = "File is not Xlsx";
		}
		if (this.wb == null && supportXls)
		{
			try
			{
				StreamUtil.seekFromBeginning(stm, 0);
				this.wb = new HSSFWorkbook(stm);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				this.lastError = "File is not Xlsx or Xls";
			}
		}

		if (this.wb != null)
		{
			this.sheet = wb.getSheetAt(0);
			if (this.sheet == null)
			{
				this.lastError = "No sheets found";
				return;
			}

			Row row = this.sheet.getRow(headerRow);
			Cell cell;
			if (row == null)
			{
				this.lastError = "Header row not found";
				return;
			}
			int i = 0;
			int j = this.headers.length;
			while (i < j)
			{
				if (StringUtil.isNullOrEmpty(this.headers[i]))
				{

				}
				else if ((cell = row.getCell(i)) == null || cell.getCellType() != CellType.STRING || !cell.getStringCellValue().toUpperCase().startsWith(this.headers[i].toUpperCase()))
				{
					this.lastError = "Column \""+this.headers[i]+"\" not found, in file is \""+getCellAsString(row, i)+"\"";
					return;
				}
				i++;
			}
			this.nextRowInd = this.headerRow + 1;
			this.fileValid = true;
		}
	}

	public boolean orHeaders(@Nonnull String headers[])
	{
		if (this.fileValid)
		{
			return true;
		}
		if (this.sheet == null)
		{
			this.lastError = "No sheets found";
			return false;
		}

		Row row = this.sheet.getRow(this.headerRow);
		Cell cell;
		if (row == null)
		{
			this.lastError = "Header row not found";
			return false;
		}
		int i = 0;
		int j = headers.length;
		while (i < j)
		{
			if ((cell = row.getCell(i)) == null || cell.getCellType() != CellType.STRING || !cell.getStringCellValue().toUpperCase().startsWith(headers[i].toUpperCase()))
			{
				this.lastError = "Column \""+headers[i]+"\" not found, in file is \""+getCellAsString(row, i)+"\"";
				return false;
			}
			i++;
		}
		this.headers = headers;
		this.nextRowInd = this.headerRow + 1;
		this.fileValid = true;
		return true;
	}

	public void setTrimStr(boolean trimStr)
	{
		this.trimStr = trimStr;
	}

	public boolean moveFirstRow()
	{
		if (!this.fileValid)
			return false;
		this.nextRowInd = this.headerRow + 1;
		this.currRow = null;
		return true;
	}

	public boolean nextRow()
	{
		if (!this.fileValid)
			return false;
		if ((this.currRow = this.sheet.getRow(this.nextRowInd)) == null)
		{
			return false;
		}
		this.nextRowInd++;
		return true;
	}

	public boolean isCellNull(int index)
	{
		if (this.currRow == null)
			return true;
		if (index < 0 || index >= this.headers.length)
			return true;
		Cell cell = this.currRow.getCell(index);
		return cell == null || cell.getCellType() == CellType.BLANK;
	}

	@Nullable
	public Double getCellDouble(int index)
	{
		if (this.currRow == null)
			return null;
		if (index < 0 || index >= this.headers.length)
			return null;
		Cell cell = this.currRow.getCell(index);
		if (cell == null)
		{
			this.lastError = this.headers[index] + " is required";
			return null;
		}
		else if (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC)
		{

		}
		else if (cell.getCellType() != CellType.NUMERIC)
		{
			this.lastError = this.headers[index] + " is not numeric ("+cell.getCellType()+")";
			return null;
		}
		return cell.getNumericCellValue();
	}

	@Nullable
	public Double getCellAsDouble(int index)
	{
		if (this.currRow == null)
			return null;
		if (index < 0 || index >= this.headers.length)
			return null;
		Cell cell = this.currRow.getCell(index);
		if (cell == null)
		{
			this.lastError = this.headers[index] + " is required";
			return null;
		}
		else if (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC)
		{
			return cell.getNumericCellValue();
		}
		else if (cell.getCellType() == CellType.NUMERIC)
		{
			return cell.getNumericCellValue();
		}
		else if (cell.getCellType() == CellType.STRING || (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.STRING))
		{
			String s = cell.getStringCellValue();
			Double v = StringUtil.toDouble(s);
			if (v == null)
			{
				this.lastError = this.headers[index] + " is not numeric value ("+s+")";
			}
			return v;
		}
		this.lastError = this.headers[index] + " is not numeric ("+cell.getCellType()+")";
		return null;
	}

	@Nullable
	public String getCellString(int index)
	{
		if (this.currRow == null)
			return null;
		if (index < 0 || index >= this.headers.length)
			return null;
		Cell cell = this.currRow.getCell(index);
		if (cell == null)
		{
			this.lastError = this.headers[index] + " is required";
			return null;
		}
		else if (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.STRING)
		{
			
		}
		else if (cell.getCellType() != CellType.STRING)
		{
			this.lastError = this.headers[index] + " is not string";
			return null;
		}
		String str = cell.getStringCellValue();
		if (this.trimStr && str != null)
		{
			str = str.trim();
		}
		return str;
	}

	@Nullable
	private static String getCellAsString(@Nonnull Row row, int index)
	{
		Cell cell = row.getCell(index);
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
		if (cell.getCellType() == CellType.FORMULA)
		{
			if (cell.getCachedFormulaResultType() == CellType.STRING)
			{
				return cell.getStringCellValue();
			}
			else if (cell.getCachedFormulaResultType() == CellType.NUMERIC)
			{
				return ""+cell.getNumericCellValue();
			}
		}
		return null;
	}

	@Nullable
	public String getCellAsString(int index)
	{
		if (this.currRow == null)
			return null;
		if (index < 0 || index >= this.headers.length)
			return null;
		Cell cell = this.currRow.getCell(index);
		if (cell == null)
		{
			this.lastError = this.headers[index] + " is required";
			return null;
		}
		if (cell.getCellType() == CellType.STRING)
		{
			String str = cell.getStringCellValue();
			if (this.trimStr && str != null)
			{
				str = str.trim();
			}
			return str;
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
		if (cell.getCellType() == CellType.FORMULA)
		{
			if (cell.getCachedFormulaResultType() == CellType.STRING)
			{
				String str = cell.getStringCellValue();
				if (this.trimStr && str != null)
				{
					str = str.trim();
				}
				return str;
			}
			else if (cell.getCachedFormulaResultType() == CellType.NUMERIC)
			{
				return ""+cell.getNumericCellValue();
			}
		}
		this.lastError = this.headers[index] + " is unknown type: "+cell.getCellType();
		return null;
	}

	@Nullable
	public String getCellDisp(int index)
	{
		if (this.currRow == null)
			return null;
		if (index < 0 || index >= this.headers.length)
			return null;
		Cell cell = this.currRow.getCell(index);
		if (cell != null && cell.getCellType() == CellType.FORMULA)
		{
			FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
			DataFormatter formatter = new DataFormatter();
			String str = formatter.formatCellValue(cell, evaluator);
			if (this.trimStr && str != null)
			{
				str = str.trim();
			}
			return str;
		}
		else
		{
			DataFormatter formatter = new DataFormatter();
			String str = formatter.formatCellValue(cell);
			if (this.trimStr && str != null)
			{
				str = str.trim();
			}
			return str;
		}
	}

	@Nullable
	public Integer getCellIntRange(int index, int min, int max)
	{
		Double dVal = getCellDouble(index);
		if (dVal == null)
			return null;
		int v = (int)dVal.doubleValue();
		if (v < min || v > max)
		{
			this.lastError = this.headers[index] +" is out of range";
			return null;
		}
		return v;
	}

	@Nullable
	public String getCellStringCharLen(int index, int minLen, int maxLen, boolean allowEmpty)
	{
		String v;
		if (allowEmpty)
		{
			v = getCellAsString(index);
			if (v == null)
			{
				v = "";
			}
		}
		else
		{
			v = getCellString(index);
			if (v == null)
				return v;
		}
		int len = v.length();
		if (len < minLen)
		{
			this.lastError = this.headers[index] +" is too short";
			return null;
		}
		if (len > maxLen)
		{
			this.lastError = this.headers[index] +" is too long";
			return null;
		}
		return v;
	}

	@Nullable
	public String getCellDispCharLen(int index, int minLen, int maxLen)
	{
		String v;
		v = getCellDisp(index);
		if (v == null)
		{
			v = "";
		}
		int len = v.length();
		if (len < minLen)
		{
			this.lastError = this.headers[index] +" is too short";
			return null;
		}
		if (len > maxLen)
		{
			this.lastError = this.headers[index] +" is too long";
			return null;
		}
		return v;
	}

	@Nullable
	public Timestamp getCellTimestamp(int index)
	{
		Cell cell = this.currRow.getCell(index);
		if (cell == null)
		{
			this.lastError = this.headers[index] + " is required";
			return null;
		}
		else if (cell.getCellType() == CellType.NUMERIC)
		{
			try
			{
				return Timestamp.valueOf(cell.getLocalDateTimeCellValue());
			}
			catch (Exception ex)
			{

			}
		}
		else if (cell.getCellType() == CellType.BLANK)
		{
			this.lastError = this.headers[index] + " is blank";
			return null;
		}
		String v = getCellDisp(index);
		if (v == null || v.length() == 0)
			return null;
		try
		{
			return DateTimeUtil.toTimestamp(DateTimeUtil.parse(v));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			this.lastError = this.headers[index] +" is not valid time format: "+v;
			return null;
		}
	}

	@Nullable
	public Integer getCellYN(int index)
	{
		String v = getCellString(index);
		if (v == null)
			return null;
		if (v.equals("Y"))
		{
			return 1;
		}
		else if (v.equals("N"))
		{
			return 0;
		}
		else
		{
			this.lastError = this.headers[index] +" is not Y/N: "+v;
			return null;
		}
	}

	@Nullable
	public <T extends Enum<T>> T getCellEnum(int index, @Nonnull Class<T> cls)
	{
		String v = getCellAsString(index);
		if (v == null)
			return null;
		if (v.length() == 0)
		{
			this.lastError = this.headers[index] +" is required";
			return null;
		}
		T ret = DataTools.getEnum(cls, v);
		if (ret == null)
		{
			this.lastError = this.headers[index] +" is not valid: "+v;
			return null;
		}
		return ret;
	}

	@Nullable
	public Double getCellDoubleRange(int index, double min, double max)
	{
		Double v = getCellDouble(index);
		if (v == null) return null;
		if (v < min || v > max)
		{
			this.lastError = this.headers[index] +" is out of valid range("+min+"-"+max+"): "+v;
			return null;
		}
		return v;
	}

	@Nullable
	public <T> T getEntityBy(@Nonnull JpaRepository<T, Integer> repo, @Nonnull String varName, @Nonnull String varValue, @Nonnull Enum<?> checkStatus, boolean invalidCheck)
	{
		String findFunc = "findBy"+Character.toUpperCase(varName.charAt(0))+varName.substring(1);
		try
		{
			Method findMeth = repo.getClass().getMethod(findFunc, new Class<?>[]{String.class});
			@SuppressWarnings("unchecked")
			List<T> entityList = (List<T>)findMeth.invoke(repo, varValue);
			int i = entityList.size();
			if (i > 0)
			{
				Class<?> entityCls = entityList.get(0).getClass();
				Method statusMeth = entityCls.getMethod("getStatus", new Class<?>[0]);
				if (!statusMeth.getReturnType().equals(checkStatus.getClass()))
				{
					this.lastError = entityCls.getSimpleName()+".getStatus does not returning correct type: "+varValue;
					return null;
				}
				while (i-- > 0)
				{
					T entity = entityList.get(i);
					Enum<?> status = (Enum<?>)statusMeth.invoke(entity);
					if (invalidCheck)
					{
						if (status != checkStatus)
						{
							return entity;
						}
					}
					else
					{
						if (status == checkStatus)
						{
							return entity;
						}
					}
				}
			}
			this.lastError = getRepoClassName(repo)+" is not found: "+varValue;
			return null;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			this.lastError = "Method "+getRepoClassName(repo)+"."+findFunc+"(String) is not found";
			return null;
		}
	}

	public <T> boolean checkInList(@Nonnull String varName, @Nonnull T varValue, @Nonnull List<T> validList)
	{
		boolean found = false;
		int i = validList.size();
		while (i-- > 0)
		{
			if (varValue.equals(validList.get(i)))
			{
				found = true;
				break;
			}
		}
		if (!found)
		{
			this.lastError = varName+" is not valid: "+varValue;
			return true;
		}
		return false;
	}

	public <T> boolean checkInSet(@Nonnull String varName, @Nonnull T varValue, @Nonnull Set<T> validSet)
	{
		if (!validSet.contains(varValue))
		{
			this.lastError = varName+" is not valid: "+varValue;
			return true;
		}
		return false;
	}

	public boolean isFileValid()
	{
		return this.fileValid;
	}

	public int getRowNum()
	{
		return this.nextRowInd;
	}

	@Nullable
	public String getLastError()
	{
		return this.lastError;
	}

	public void close()
	{
		if (this.wb != null)
		{
			try
			{
				this.wb.close();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			this.wb = null;
		}
	}

	@Nonnull
	public static <T> String getRepoClassName(@Nonnull JpaRepository<T, Integer> repo)
	{
		String clsName = getRepoEntityName(repo);
		if (clsName != null)
		{
			return clsName;
		}
		Class<?> repoClass = getRepoClass(repo);
		if (repoClass != null)
		{
			return repoClass.getSimpleName();
		}
		return repo.getClass().getSimpleName();
	}
	
	@Nullable
	private static <T> Class<?> getRepoClass(@Nonnull JpaRepository<T, Integer> repo)
	{
		Class<?> cls = repo.getClass();
		Class<?> interfaces[] = cls.getInterfaces();
		int i = 0;
		int j = interfaces.length;
		while (i < j)
		{
			if (interfaces[i].getPackageName().startsWith("org.springframework"))
			{
			}
			else
			{
				return interfaces[i];
			}
			i++;
		}
		return null;
	}

	@Nullable
	private static <T> String getRepoEntityName(@Nonnull JpaRepository<T, Integer> repo)
	{
		Class<?> cls = getRepoClass(repo);
		if (cls == null) return null;
		Type [] interfaces = cls.getGenericInterfaces();
		int i = interfaces.length;
		while (i-- > 0)
		{
			if (interfaces[i] instanceof ParameterizedType)
			{
				ParameterizedType pt = (ParameterizedType)interfaces[i];
				return ((Class<?>)pt.getActualTypeArguments()[0]).getSimpleName();
			}
		}
		return null;
	}

	@Nonnull
	public Map<String, String> getRowAsMap()
	{
		Map<String, String> ret = new HashMap<String, String>();
		int i = 0;
		int j = this.headers.length;
		while (i < j)
		{
			ret.put(this.headers[i], this.getCellDisp(i));
			i++;
		}
		return ret;
	}
}
