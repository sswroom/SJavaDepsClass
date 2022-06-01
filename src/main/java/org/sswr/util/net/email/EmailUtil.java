package org.sswr.util.net.email;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.CMSProcessableBodyPartInbound;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;

public class EmailUtil
{
	private static boolean debug = false;

	public static Message loadFromEml(File file)
	{
		Session mailSession = Session.getDefaultInstance(new Properties(), null);
		try
		{
			return new MimeMessage(mailSession, new FileInputStream(file));
		}
		catch (FileNotFoundException ex)
		{
			if (debug)
			{
				ex.printStackTrace();
			}
			return null;
		}
		catch (MessagingException ex)
		{
			if (debug)
				ex.printStackTrace();
			return null;
		}
	}

	public static boolean isSMIME(Message msg)
	{
		try
		{
			return msg.isMimeType("multipart/signed");
		}
		catch (MessagingException ex)
		{
			ex.printStackTrace();
		}
		return false;
	}

	public static ReceivedEmail toReceivedEmail(Message msg)
	{
		ReceivedEmail email = new ReceivedEmail();
		try
		{
			Enumeration<?> enumer = msg.getAllHeaders();
			Header header;
			while (enumer.hasMoreElements())
			{
				header = (Header)enumer.nextElement();
				email.addHeader(header.getName(), header.getValue());
			}
			if (msg.isMimeType("multipart/signed"))
			{
				Multipart mpart = (Multipart)msg.getContent();
				int i = 0;
				int j = mpart.getCount();
				while (i < j)
				{
					BodyPart part = mpart.getBodyPart(i);
					if (part.isMimeType("application/pkcs7-signature"))
					{
						String fileName = part.getFileName();
						if (fileName.equals("smime.p7s"))
						{
							byte[] signature = part.getInputStream().readAllBytes();
							email.setSignVerified(verifySign(mpart.getBodyPart(0), signature));
						}
						else
						{
							/////////////////////////////////////////////
							System.out.println("Unknown Signature file");
						}
					}
					else
					{
						String disposition = part.getDisposition();
						if (disposition == null)
						{
							email.setContent(part.getInputStream().readAllBytes());
							email.setContentType(part.getContentType());
						}
						else
						{
							System.out.println("Content Type: "+part.getContentType());
							System.out.println("Description: "+part.getDescription());
							System.out.println("Disposition: "+disposition);
							System.out.println("FileName: "+part.getFileName());
							System.out.println("Size: "+part.getSize());
							System.out.println("----------------");
							/////////////////////////////////////////////
						}
					}
					i++;					
				}
			}
			else if (msg.isMimeType("multipart/*"))
			{
				/////////////////////////////////////////////
			}
			else
			{
				email.setContent(msg.getInputStream().readAllBytes());
			}
			return email;
		}
		catch (MessagingException ex)
		{
			if (debug)
			{
				ex.printStackTrace();
			}
			return null;
		}
		catch (IOException ex)
		{
			if (debug)
			{
				ex.printStackTrace();
			}
			return null;
		}
	}

	public static boolean verifySign(BodyPart data, byte[] signature)
	{
		Security.addProvider(new BouncyCastleProvider());
		try
		{
			CMSSignedData s = new CMSSignedData(new CMSProcessableBodyPartInbound(data), signature);
			Store<X509CertificateHolder> certs = s.getCertificates();
			SignerInformationStore signers = s.getSignerInfos();
			Iterator<SignerInformation> itSigner = signers.getSigners().iterator();
			while (itSigner.hasNext())
			{
				SignerInformation signer = itSigner.next();
				@SuppressWarnings("unchecked")
				Selector<X509CertificateHolder> selector = signer.getSID();
				Collection<X509CertificateHolder> certCollection = certs.getMatches(selector);
				Iterator<X509CertificateHolder> itCert = certCollection.iterator();
				try
				{
					X509Certificate cert = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(itCert.next());
					if (signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build(cert)))
						return true;
				}
				catch (CertificateException ex)
				{
					ex.printStackTrace();
				}
				catch (OperatorCreationException ex)
				{
					ex.printStackTrace();
				}
			}
			return false;
		}
		catch (CMSException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
}
