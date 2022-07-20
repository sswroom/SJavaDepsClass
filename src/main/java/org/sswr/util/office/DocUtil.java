package org.sswr.util.office;

import org.apache.poi.hwpf.HWPFDocument;
import org.sswr.util.media.PrintDocument;
import org.sswr.util.media.Printer;

public class DocUtil
{
	public static void print(Printer printer, HWPFDocument doc)
	{
		DocPrintHandler printHandler = new DocPrintHandler(doc);
		PrintDocument pdoc = printer.startPrint(printHandler);
		printer.endPrint(pdoc);
	}
}
