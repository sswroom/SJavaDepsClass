package org.sswr.util.office;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class PDFUtil
{
	public static boolean append(@Nonnull PDDocument workingDoc, @Nonnull PDDocument documentToAppend)
	{
		int i = 0;
		int j = documentToAppend.getNumberOfPages();
		while (i < j)
		{
			workingDoc.addPage(documentToAppend.getPage(i));
			i++;
		}
		return true;
	}

/*	public static boolean append(PDDocument workingDoc, XWPFDocument documentToAppend)
	{
		PDPage page = new PDPage();
		page.
		workingDoc.addPage(new PDPage());
		fr.opensagres.xdocreport.converter.IConverter
		documentToAppend
		int i = 0;
		int j = documentToAppend.getNumberOfPages();
		while (i < j)
		{
			workingDoc.addPage(documentToAppend.getPage(i));
			i++;
		}
		return true;
	}*/

	public static void close(@Nullable PDDocument doc)
	{
		if (doc != null)
		{
			try
			{
				doc.close();
			}
			catch (IOException ex)
			{
			}
		}
	}
}
