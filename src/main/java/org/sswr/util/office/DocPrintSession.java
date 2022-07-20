package org.sswr.util.office;

import java.awt.Graphics2D;

import org.apache.poi.hwpf.HWPFDocument;

public class DocPrintSession
{
	public int pageNum;
	public Graphics2D g;
	public HWPFDocument doc;
	public boolean hasMorePages;
	public double currX;
	public double currY;
}
