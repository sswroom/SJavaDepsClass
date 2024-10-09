package org.sswr.util.office;

import org.apache.poi.hwpf.HWPFDocument;
import org.sswr.util.media.PrintDocument;
import org.sswr.util.media.Printer;

import jakarta.annotation.Nonnull;

public class DocUtil
{
	public static void print(@Nonnull Printer printer, @Nonnull HWPFDocument doc)
	{
		DocPrintHandler printHandler = new DocPrintHandler(doc);
		PrintDocument pdoc = printer.startPrint(printHandler);
		if (pdoc != null)
		{
			printer.endPrint(pdoc);
		}
	}
}
