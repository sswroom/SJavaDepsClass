package org.sswr.util.office;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.StringUtil;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.jpa.repository.JpaRepository;

public class XlsxValidator {
	XSSFWorkbook wb;
	XSSFSheet sheet;
	String headers[];
	String lastError;
	boolean fileValid;
	int nextRowInd;
	XSSFRow currRow;
	boolean trimStr;

	public XlsxValidator(InputStream stm, String headers[])
	{
		try
		{
			this.fileValid = false;
			this.lastError = null;
			this.headers = headers;
			this.trimStr = false;
			this.wb = new XSSFWorkbook(stm);
			this.sheet = wb.getSheetAt(0);
			if (this.sheet == null)
			{
				this.lastError = "No sheets found";
				return;
			}

			XSSFRow row = this.sheet.getRow(0);
			XSSFCell cell;
			if (row == null)
			{
				this.lastError = "Header row not found";
				return;
			}
			int i = 0;
			int j = this.headers.length;
			while (i < j)
			{
				if ((cell = row.getCell(i)) == null || cell.getCellType() != CellType.STRING || !cell.getStringCellValue().toUpperCase().startsWith(this.headers[i].toUpperCase()))
				{
					this.lastError = "Column \""+this.headers[i]+"\" not found, in file is \""+getCellAsString(row, i)+"\"";
					return;
				}
				i++;
			}
			this.nextRowInd = 1;
			this.fileValid = true;
		}
		catch (IOException ex)
		{
			this.lastError = "File is not Xlsx";
		}
	}

	public boolean orHeaders(String headers[])
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

		XSSFRow row = this.sheet.getRow(0);
		XSSFCell cell;
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
		this.nextRowInd = 1;
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
		this.nextRowInd = 1;
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
		XSSFCell cell = this.currRow.getCell(index);
		return cell == null || cell.getCellType() == CellType.BLANK;
	}

	public Double getCellDouble(int index)
	{
		if (this.currRow == null)
			return null;
		if (index < 0 || index >= this.headers.length)
			return null;
		XSSFCell cell = this.currRow.getCell(index);
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

	public Double getCellAsDouble(int index)
	{
		if (this.currRow == null)
			return null;
		if (index < 0 || index >= this.headers.length)
			return null;
		XSSFCell cell = this.currRow.getCell(index);
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

	public String getCellString(int index)
	{
		if (this.currRow == null)
			return null;
		if (index < 0 || index >= this.headers.length)
			return null;
		XSSFCell cell = this.currRow.getCell(index);
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

	private static String getCellAsString(XSSFRow row, int index)
	{
		XSSFCell cell = row.getCell(index);
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

	public String getCellAsString(int index)
	{
		if (this.currRow == null)
			return null;
		if (index < 0 || index >= this.headers.length)
			return null;
		XSSFCell cell = this.currRow.getCell(index);
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

	public String getCellDisp(int index)
	{
		if (this.currRow == null)
			return null;
		if (index < 0 || index >= this.headers.length)
			return null;
		XSSFCell cell = this.currRow.getCell(index);
		DataFormatter formatter = new DataFormatter();
		String str = formatter.formatCellValue(cell);
		if (this.trimStr && str != null)
		{
			str = str.trim();
		}
		return str;
	}

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

	public Timestamp getCellTimestamp(int index)
	{
		XSSFCell cell = this.currRow.getCell(index);
		if (cell == null)
		{
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
		String v = getCellDisp(index);
		if (v == null)
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

	public <T extends Enum<T>> T getCellEnum(int index, Class<T> cls)
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

	public <T> T getEntityBy(JpaRepository<T, Integer> repo, String varName, String varValue, Enum<?> checkStatus, boolean invalidCheck)
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

	public <T> boolean checkInList(String varName, T varValue, List<T> validList)
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

	public <T> boolean checkInSet(String varName, T varValue, Set<T> validSet)
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

	public static <T> String getRepoClassName(JpaRepository<T, Integer> repo)
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
	
	private static <T> Class<?> getRepoClass(JpaRepository<T, Integer> repo)
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

	private static <T> String getRepoEntityName(JpaRepository<T, Integer> repo)
	{
		Class<?> cls = getRepoClass(repo);
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
}
