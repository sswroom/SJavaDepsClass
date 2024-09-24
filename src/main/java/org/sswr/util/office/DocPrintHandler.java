package org.sswr.util.office;

import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.sswr.util.math.unit.Distance;
import org.sswr.util.math.unit.Distance.DistanceUnit;
import org.sswr.util.media.PrintDocument;
import org.sswr.util.media.PrintHandler;

import jakarta.annotation.Nonnull;

public class DocPrintHandler implements PrintHandler
{
	private HWPFDocument doc;
	private PrintDocument printDoc;

	public DocPrintHandler(@Nonnull HWPFDocument doc)
	{
		this.doc = doc;
	}

	@Override
	public boolean beginPrint(@Nonnull PrintDocument doc)
	{
		this.printDoc = doc;
		return true;
	}

	@Override
	public boolean printPage(int pageNum, @Nonnull Graphics2D printPage)
	{
		Range range = doc.getRange();
		int i;
		int j;
		i = 0;
		j = range.numSections();
		DocPrintSession sess = new DocPrintSession();
		sess.pageNum = pageNum;
		sess.g = printPage;
		sess.doc = doc;
		sess.hasMorePages = false;
		while (sess.pageNum >= 0 && i < j)
		{
			printSection(sess, range.getSection(i));
			i++;
		}
		return sess.hasMorePages;
//		StringBuilder sb = new StringBuilder();
/* 		i = 0;
		j = range.numCharacterRuns();
		while (i < j)
		{
			CharacterRun cr = range.getCharacterRun(i);
			sb.setLength(0);
			JSText.toJSTextDQuote(sb, cr.text());
			System.out.println("CharRun "+i+": "+sb.toString()+", "+DataTools.toObjectString(cr));
			i++;
		}*/
/*		i = 0;
		j = range.numSections();
		while (i < j)
		{
			Section sec = range.getSection(i);
			System.out.println("Section "+i+": "+DataTools.toObjectString(sec));
			i++;
		}*/
/*		i = 0;
		j = range.numParagraphs();
		while (i < j)
		{
			Paragraph para = range.getParagraph(i);
			sb.setLength(0);
			JSText.toJSTextDQuote(sb, para.text());
			System.out.println("Paragraph "+i+": "+sb.toString()+", "+DataTools.toObjectString(para));
			i++;
		}
		List<Picture> pictures = doc.getPicturesTable().getAllPictures();
		System.out.println("Pictures = "+DataTools.toObjectString(pictures));
		return false;*/
	}

	@Override
	public boolean endPrint(@Nonnull PrintDocument doc)
	{
		return true;
	}
	
	private void printSection(@Nonnull DocPrintSession sess, @Nonnull Section section)
	{
		sess.pageNum--;
		///////////////////////////////////////////
	}

	@Nonnull
	private PageFormat getPageFormat(@Nonnull Section section)
	{
		PageFormat pf = new PageFormat();
		Paper paper = new Paper();
		int width = section.getPageWidth();
		int height = section.getPageHeight();
		paper.setSize(Distance.convert(DistanceUnit.Twip, DistanceUnit.Point, width), Distance.convert(DistanceUnit.Twip, DistanceUnit.Point, height));
		pf.setPaper(paper);
		if (width > height)
		{
			pf.setOrientation(PageFormat.LANDSCAPE);
		}
		else
		{
			pf.setOrientation(PageFormat.PORTRAIT);
		}
		return pf;
	}

	@Override
	public int getNumberOfPages()
	{
		Range range = doc.getRange();
		int i;
		int j;
		i = 0;
		j = range.numSections();
		DocPrintSession sess = new DocPrintSession();
		sess.pageNum = 10000;
		sess.g = null;
		sess.doc = doc;
		sess.hasMorePages = false;
		while (sess.pageNum >= 0 && i < j)
		{
			printSection(sess, range.getSection(i));
			i++;
		}
		return 10000 - sess.pageNum;
	}

	@Override
	@Nonnull
	public PageFormat getPageFormat(int pageNum)
	{
		Range range = doc.getRange();
		int i;
		int j;
		i = 0;
		j = range.numSections();
		DocPrintSession sess = new DocPrintSession();
		sess.pageNum = 10000;
		sess.g = null;
		sess.doc = doc;
		sess.hasMorePages = false;
		while (sess.pageNum >= 0 && i < j)
		{
			if (10000 - sess.pageNum == pageNum)
			{
				return getPageFormat(range.getSection(i));
			}
			printSection(sess, range.getSection(i));
			if ((10000 - sess.pageNum) > pageNum)
			{
				return getPageFormat(range.getSection(i));
			}
			i++;
		}
		return getPageFormat(range.getSection(0));
	}
}
