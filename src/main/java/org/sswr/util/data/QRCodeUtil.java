package org.sswr.util.data;

import java.awt.image.BufferedImage;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCodeUtil
{
	public static BufferedImage generateStringQRCode(String text, int width, int height)
	{
		QRCodeWriter writer = new QRCodeWriter();
		try
		{
			BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);
			return MatrixToImageWriter.toBufferedImage(matrix);
		}
		catch (WriterException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
}
