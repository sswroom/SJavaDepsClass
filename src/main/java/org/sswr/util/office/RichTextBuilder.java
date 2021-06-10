package org.sswr.util.office;

import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class RichTextBuilder
{
	public class TextPart
	{
		public String s;
		public Font f;
		public int index;

		public TextPart(String s, Font f)
		{
			this.s = s;
			this.f = f;
		}
	}

	private ArrayList<TextPart> parts;

	public RichTextBuilder()
	{
		this.parts = new ArrayList<TextPart>();
	}

	public RichTextBuilder append(String s, Font f)
	{
		this.parts.add(new TextPart(s, f));
		return this;
	}

	public RichTextString build(Sheet sheet)
	{
		Class<?> cls = sheet.getClass();
		RichTextString ret;
		StringBuilder sb = new StringBuilder();
		int i = 0;
		int j = this.parts.size();
		TextPart part;
		while (i < j)
		{
			part = this.parts.get(i);
			part.index = sb.length();
			sb.append(part.s);
			i++;
		}

		if (cls.equals(XSSFSheet.class))
		{
			ret = new XSSFRichTextString(sb.toString());
		}
		else if (cls.equals(HSSFSheet.class))
		{
			ret = new HSSFRichTextString(sb.toString());
		}
		else
		{
			return null;
		}
		i = 0;
		while (i < j)
		{
			part = this.parts.get(i);
			if (part.f != null)
			{
				ret.applyFont(part.index, part.index + part.s.length(), part.f);
			}
			i++;
		}
		return ret;
	}
}
