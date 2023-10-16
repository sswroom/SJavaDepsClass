package org.sswr.util.net.email;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.CMSProcessableBodyPartInbound;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
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

	private static void setEmailContent(ReceivedEmail email, Part content) throws IOException, MessagingException
	{
		if (content.isMimeType("multipart/signed"))
		{
			Multipart mpart = (Multipart)content.getContent();
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
					setEmailContent(email, part);
				}
				i++;					
			}
		}
		else if (content.isMimeType("multipart/*"))
		{
			Multipart mpart = (Multipart)content.getContent();
			int i = 0;
			int j = mpart.getCount();
			while (i < j)
			{
				BodyPart part = mpart.getBodyPart(i);
				String disposition = part.getDisposition();
				if (disposition == null)
				{
					setEmailContent(email, part);
				}
				else
				{
					email.addAttachment(disposition.equals("inline"), part.getFileName(), part.getContentType(), part.getInputStream().readAllBytes());
				}
				i++;					
			}
		}
		else
		{
			email.setContent(content.getInputStream().readAllBytes());
			email.setContentType(content.getContentType());
		}
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
			setEmailContent(email, msg);
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

	public static MimeMessage signEmail(MimeMessage msg, X509Certificate cert, PrivateKey key)
	{
		Properties props = System.getProperties();
		Session session = Session.getDefaultInstance(props, null);

		SMIMECapabilityVector capabilities = new SMIMECapabilityVector();
		capabilities.addCapability(SMIMECapability.dES_EDE3_CBC);
		capabilities.addCapability(SMIMECapability.rC2_CBC, 128);
		capabilities.addCapability(SMIMECapability.dES_CBC);
		
		ASN1EncodableVector attributes = new ASN1EncodableVector();
		attributes.add(new SMIMEEncryptionKeyPreferenceAttribute(
				new IssuerAndSerialNumber(
						new X500Name(cert.getIssuerX500Principal().getName()),
								cert.getSerialNumber())));
		attributes.add(new SMIMECapabilitiesAttribute(capabilities));
		
		try
		{
			SMIMESignedGenerator signer = new SMIMESignedGenerator();
			signer.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME)
					.setSignedAttributeGenerator(new AttributeTable(attributes)).build("SHA1withRSA", key, cert));

			List<X509Certificate> certList = new ArrayList<X509Certificate>();
			certList.add(cert);
			Store<?> certs = new JcaCertStore(certList);
			signer.addCertificates(certs);
			
			MimeMultipart mm = signer.generate(msg);
			MimeMessage signedMessage = new MimeMessage(session);
			
			Enumeration<?> headers = msg.getAllHeaderLines();
			while (headers.hasMoreElements()) {
				signedMessage.addHeaderLine((String)headers.nextElement());
			}
			signedMessage.setContent(mm);
			signedMessage.saveChanges();
			return signedMessage;
		}
		catch (MessagingException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (CertificateEncodingException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (SMIMEException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (OperatorCreationException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
}
