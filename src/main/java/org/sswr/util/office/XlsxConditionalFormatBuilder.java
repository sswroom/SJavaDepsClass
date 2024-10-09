package org.sswr.util.office;

import java.util.ArrayList;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class XlsxConditionalFormatBuilder
{
	class RuleEntry
	{
		public byte operation;
		public String formula1;
		public String formula2;

		public boolean hasPattern;
		public Color bgColor;
		public Color fgColor;
		public FillPatternType fillPattern;
	}

	private ArrayList<RuleEntry> entries;

	public XlsxConditionalFormatBuilder()
	{
		this.entries = new ArrayList<RuleEntry>();
	}

	@Nonnull
	public XlsxConditionalFormatBuilder empty(@Nonnull CellStyle style)
	{
		RuleEntry ent = this.createEntry(style);
		ent.operation = ComparisonOperator.EQUAL;
		ent.formula1 = "\"\"";
		ent.formula2 = null;
		this.entries.add(ent);
		return this;
	}

	@Nonnull
	public XlsxConditionalFormatBuilder between(double v1, double v2, @Nonnull CellStyle style)
	{
		RuleEntry ent = this.createEntry(style);
		ent.operation = ComparisonOperator.BETWEEN;
		ent.formula1 = String.valueOf(v1);
		ent.formula2 = String.valueOf(v2);
		this.entries.add(ent);
		return this;
	}

	@Nonnull
	public XlsxConditionalFormatBuilder notBetween(double v1, double v2, @Nonnull CellStyle style)
	{
		RuleEntry ent = this.createEntry(style);
		ent.operation = ComparisonOperator.NOT_BETWEEN;
		ent.formula1 = String.valueOf(v1);
		ent.formula2 = String.valueOf(v2);
		this.entries.add(ent);
		return this;
	}

	@Nonnull
	private RuleEntry createEntry(@Nonnull CellStyle style)
	{
		RuleEntry ent = new RuleEntry();
		ent.operation = 0;
		ent.fillPattern = style.getFillPattern();
		ent.fgColor = style.getFillForegroundColorColor();
		ent.bgColor = style.getFillForegroundColorColor();
		ent.hasPattern = ent.fgColor != null || ent.bgColor != null;
		return ent;
	}

	@Nonnull
	private ConditionalFormattingRule createRule(@Nonnull SheetConditionalFormatting sCondFmt, @Nonnull RuleEntry entry)
	{
		ConditionalFormattingRule rule;
		if (entry.operation == ComparisonOperator.BETWEEN || entry.operation == ComparisonOperator.NOT_BETWEEN)
		{
			rule = sCondFmt.createConditionalFormattingRule(entry.operation, entry.formula1, entry.formula2);
		}
		else
		{
			rule = sCondFmt.createConditionalFormattingRule(entry.operation, entry.formula1);
		}
		if (entry.hasPattern)
		{
			PatternFormatting pFmt = rule.createPatternFormatting();
			if (entry.bgColor != null) pFmt.setFillBackgroundColor(entry.bgColor);
			if (entry.fgColor != null) pFmt.setFillForegroundColor(entry.fgColor);
			pFmt.setFillPattern(getFillPattern(entry.fillPattern));
		}
		return rule;
	}

	private boolean isEquals(@Nonnull RuleEntry entry, @Nonnull ConditionalFormattingRule rule)
	{
		if (entry.operation != rule.getComparisonOperation())
		{
			return false;
		}
		if (!entry.formula1.equals(rule.getFormula1()))
		{
			return false;
		}
		if (entry.formula2 == null && rule.getFormula2() == null)
		{
		}
		else if (entry.formula2 == null)
		{
			return false;
		}
		else if (rule.getFormula2() == null)
		{
			return false;
		}
		else if (!entry.formula2.equals(rule.getFormula2()))
		{
			return false;
		}
		if (entry.hasPattern)
		{
			PatternFormatting fmt = rule.getPatternFormatting();
			if (fmt == null)
			{
				return false;
			}
			if (entry.bgColor != null && !entry.bgColor.equals(fmt.getFillBackgroundColorColor()))
			{
				return false;
			}
			if (entry.fgColor != null && !entry.fgColor.equals(fmt.getFillForegroundColorColor()))
			{
				return false;
			}
/*			if (entry.bgIndex != fmt.getFillBackgroundColor())
			{
				return false;
			}
			if (entry.fgIndex != fmt.getFillForegroundColor())
			{
				return false;
			}*/
			if (getFillPattern(entry.fillPattern) != fmt.getFillPattern())
			{
				return false;
			}
		}
		else if (rule.getPatternFormatting() != null)
		{
			return false;
		}
		return true;
	}

	private short getFillPattern(@Nonnull FillPatternType fType)
	{
		switch (fType)
		{
		case ALT_BARS:
			return PatternFormatting.ALT_BARS;
		case BIG_SPOTS:
			return PatternFormatting.BIG_SPOTS;
		case BRICKS:
			return PatternFormatting.BRICKS;
		case DIAMONDS:
			return PatternFormatting.DIAMONDS;
		case FINE_DOTS:
			return PatternFormatting.FINE_DOTS;
		case LEAST_DOTS:
			return PatternFormatting.LEAST_DOTS;
		case LESS_DOTS:
			return PatternFormatting.LESS_DOTS;
		case NO_FILL:
			return PatternFormatting.NO_FILL;
		case SOLID_FOREGROUND:
			return PatternFormatting.SOLID_FOREGROUND;
		case SPARSE_DOTS:
			return PatternFormatting.SPARSE_DOTS;
		case SQUARES:
			return PatternFormatting.SQUARES;
		case THICK_BACKWARD_DIAG:
			return PatternFormatting.THICK_BACKWARD_DIAG;
		case THICK_FORWARD_DIAG:
			return PatternFormatting.THICK_FORWARD_DIAG;
		case THICK_HORZ_BANDS:
			return PatternFormatting.THICK_HORZ_BANDS;
		case THICK_VERT_BANDS:
			return PatternFormatting.THICK_VERT_BANDS;
		case THIN_BACKWARD_DIAG:
			return PatternFormatting.THIN_BACKWARD_DIAG;
		case THIN_FORWARD_DIAG:
			return PatternFormatting.THIN_FORWARD_DIAG;
		case THIN_HORZ_BANDS:
			return PatternFormatting.THIN_HORZ_BANDS;
		case THIN_VERT_BANDS:
			return PatternFormatting.THIN_VERT_BANDS;
		}
		return 0;
	}

	@Nullable
	public ConditionalFormatting getFormatting(@Nonnull SheetConditionalFormatting sCondFmt)
	{
		ConditionalFormatting condFmt;
		int i = 0;
		int j = sCondFmt.getNumConditionalFormattings();
		while (i < j)
		{
			condFmt = sCondFmt.getConditionalFormattingAt(i);
			if (this.isEquals(condFmt))
			{
				return condFmt;
			}
			i++;
		}
		i = 0;
		j = this.entries.size();
		ConditionalFormattingRule rules[] = new ConditionalFormattingRule[j];
		while (i < j)
		{
			rules[i] = this.createRule(sCondFmt, this.entries.get(i));
			i++;
		}
		return sCondFmt.getConditionalFormattingAt(sCondFmt.addConditionalFormatting(new CellRangeAddress[0], rules));
	}

	public boolean isEquals(@Nonnull ConditionalFormatting condFmt)
	{
		ConditionalFormattingRule rule;
		int i = 0;
		int j = condFmt.getNumberOfRules();
		if (this.entries.size() != j)
		{
			return false;
		}
		while (i < j)
		{
			rule = condFmt.getRule(i);
			if (!this.isEquals(this.entries.get(i), rule))
			{
				return false;
			}
			i++;
		}
		return true;
	}
}
