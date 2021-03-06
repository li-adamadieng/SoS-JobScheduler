/**
 * Copyright (C) 2014 BigLoupe http://bigloupe.github.io/SoS-JobScheduler/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
/********************************************************* begin of preamble
**
** Copyright (C) 2003-2012 Software- und Organisations-Service GmbH. 
** All rights reserved.
**
** This file may be used under the terms of either the 
**
**   GNU General Public License version 2.0 (GPL)
**
**   as published by the Free Software Foundation
**   http://www.gnu.org/licenses/gpl-2.0.txt and appearing in the file
**   LICENSE.GPL included in the packaging of this file. 
**
** or the
**  
**   Agreement for Purchase and Licensing
**
**   as offered by Software- und Organisations-Service GmbH
**   in the respective terms of supply that ship with this file.
**
** THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
** IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
** THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
** PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
** BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
** CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
** SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
** INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
** CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
** ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
** POSSIBILITY OF SUCH DAMAGE.
********************************************************** end of preamble*/
package com.sos.JSHelper.Logging;

//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.net.SMTPAppender;

import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.io.Files.JSTextFile;

/**
 * \file Log4JHelper.java
 * \brief Erweiterung des Log4J-Apis
 *
 * \class Log4JHelper
 * \brief Erweiterung des Log4J-Apis
 *
 * \details
 * Diese Klasse erweitert das Log4J-Api um einige n�tzliche Funktionen. Sie betreffen vorallem
 * die automatische Anlage einer Konfigurationsdatei, sowie die M�glichkeit komplette Logfiles
 * (auf Wunsch mit Anhang) per Mail zu versenden.
 *
 * Eine Instanz dieser Klasse sollte i.d.R. 1x an oberster Stelle in der Klassenhierachie (Main-Klasse)
 * gebildet werden. Ist das Logging-Environment einmal �ber diese Klasse konfiguriert worden, k�nnen alle
 * untergeordneten Klassen eine Loggerinstanz mit der folgenden Zuweisung abrufen:
 * \code
 * Logger logger = Logger.getRootLogger();
 * \endcode
 *
 * \note
 * Es ist also \b nicht notwendig, eine Referenz auf das Logging-Objekt an untergeordnete Klassen weiterzugeben.
 *
 * Dieses Beispiel zeigt, wie diese Klasse in der Main-Klasse verwendet werden sollte:
 * \code
 * import org.apache.log4j.Logger;
 * import org.apache.log4j.Level;
 * import com.sos.logging.Log4JHelper;
 *
 * public class TestLog {
 *
 *  private Log4JHelper objLogger = null;
 *  private Logger      logger    = null;
 *
 *	public TestLog {
 *      objLogger = new Log4JHelper("log4j-TestLog.properties");
 *      logger = Logger.getRootLogger();
 *		objLogger.message("Aufruf der message-Methode");		// JSHelper-konform
 *		objLogger(Level.WARN);
 *		logger.debug("dieser Nachricht darf nicht ausgegeben werden");
 *		logger.debug("dieser Nachricht darf nicht ausgegeben werden");
 *		logger.warn("dieser Nachricht MUSS ausgegeben werden");
 *		logger.error("dieser Nachricht MUSS ausgegeben werden");
 *		logger.fatal("dieser Nachricht MUSS ausgegeben werden");
 *      SubClass = new SubClass();
 *	}
 *
 * }
 * \endcode
 *
 * Einzelheiten zur Anwendung dieser Klasse in Log4JHelperTest.java.
 *
 * \author EQCPN
* @version $Id: Log4JHelper.java 20722 2013-07-18 18:19:03Z kb $04.03.2009
 *
 *
 * <div class="sos_branding">
 *   <p>� 2009 APL/Software GmbH - Berlin - generated by ClaviusXPress (<a style="color:silver" href="http://www.sos-berlin.com" target="_blank">http://www.sos-berlin.com</a>)</p>
 * </div>
 */
public class Log4JHelper implements JSListener {

	private static final String	conClassName					= "Log4JHelper";
	public static final String	conPropfile						= "log4j.properties";

	// Standardappender f�r die Mailkonfiguration
	public static final String	conMailAppender					= "mail";

	//
	public static final String	conFileAppender					= "mailfile";

	public static boolean		flgUseJobSchedulerLog4JAppender	= false;

	@SuppressWarnings("unused")
	private String				strFileAppender					= conFileAppender;
	@SuppressWarnings("unused")
	private final SMTPAppender		objMailApp						= null;
	private final Vector<String>		objFiles						= new Vector<String>();

	public String				Subject							= conClassName;
	private String				strName							= conClassName;
	private Level				objLevel;
	private boolean				flgPrintComputerName			= false;

	private static String		strPropfileName					= null;
	private static Logger		objCurrentLog					= null;				/*!<  Ausgabe via individuellem Logger (log4j.category.xxx) */
	private static Logger		objRootLog						= null;				/*!<  Ausgabe via RootLogger (log4j.rootCategory) */

	/**
	 * \brief Standard-Instanziierung des Logging-Objektes
	 *
	 * \details
	 * Wird die Instanz des Logging-Objektes �ber diesen Konstruktor erzeugt, erfolgt die Konfiguration
	 * implizit �ber den Properties-File  \c log4j.properties. Existiert diese Datei nicht, wird sie
	 * automatisch erzeugt. Sie enth�lt dann eine minimale Konfiguration des Log4J_Environments, welche
	 * lediglich Testausgaben auf der Konsole erlaubt.
	 * \verbatim
		log4j.rootCategory=debug, stdout
		log4j.appender.stdout=org.apache.log4j.ConsoleAppender
		log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
		log4j.appender.stdout.layout.ConversionPattern=%5p [%t] (%F:%L) - %m%n
	   \endverbatim
	 * Bei Bedarf ist diese Datei entsprechend zu erweitern.
	 */
	public Log4JHelper() {
		configure(conPropfile, conClassName);
	}

	/**
	 * \brief Instanziierung des Logging-Objektes mit vorgegebener Konfigurationsdatei
	 *
	 * \details
	 * Wird die Instanz des Logging-Objektes �ber diesen Konstruktor erzeugt, erfolgt die Konfiguration
	 * �ber den Properties-File, der mit dem Parameter \c pstrProperties �bergeben worden ist. Existiert diese Datei nicht, wird sie
	 * automatisch erzeugt. Sie enth�lt dann eine minimale Konfiguration des Log4J_Environments, welche
	 * lediglich Testausgaben auf der Konsole erlaubt.
	 * \verbatim
		log4j.rootCategory=debug, stdout
		log4j.appender.stdout=org.apache.log4j.ConsoleAppender
		log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
		log4j.appender.stdout.layout.ConversionPattern=%5p [%t] (%F:%L) - %m%n
	   \endverbatim
	 * Bei Bedarf ist diese Datei entsprechend zu erweitern.
	 */
	public Log4JHelper(final String pstrPropFile) {
		configure(pstrPropFile, conClassName);
	}

	/**
	 * \brief Instanziierung des Logging-Objektes mit vorgegebener Konfigurationsdatei und Loggernamen
	 *
	 * \details
	 * Entspricht dem Konstruktor Log4JHelper(String pstrPropFile) mit der Erweiterung, dass mit dem
	 * Parameter \c pstrName ein eigener Name f�r die Logging-Instanz vergeben werden kann (Standard ist
	 * \c Log4JHelper).
	 * Die Verwendung dieses Konstruktors ist sinnvoll, wenn mehrere Logging-Instanzen parallel
	 * verwendet werden sollen.
	 */
	public Log4JHelper(final String pstrPropFile, final String pstrName) {
		configure(pstrPropFile, pstrName);
	}

	/**
	 * \brief Vorbereitung des Logging-Environments
	 *
	 * \details
	 * Liest die Logging-Einstellungen aus der dem Properties-File. Existiert die Datei nicht, wird eine
	 * einfache Properties-Datei angelegt, welche die Ausgaben auf der Konsole erzeugt.
	 *
	 * @param pstrPropFileName
	 */
	private void configure(final String pstrPropFileName, final String pstrName) {
		if (pstrPropFileName == null || pstrPropFileName.equalsIgnoreCase("./log4j.properties")) {
			JSOptionsClass objO = new JSOptionsClass();
			String strF = objO.log4jPropertyFileName.Value();
			if (strF != null) {
				strPropfileName = strF;
			}
		}
		else {
			strPropfileName = pstrPropFileName;
			if (strPropfileName.equalsIgnoreCase("null") == true) {
				BasicConfigurator.configure();
				return;
			}
		}
		strName = pstrName;

		/**
		 * Anlegen der Properties-Datei, falls diese fehlt
		 */
		JSTextFile objFile = new JSTextFile(strPropfileName);
		boolean flgNew = false;
		boolean flgPropFileIsOk = false;

		/**
		 * canWrite is not working on a non-existing file.
		 * that's why we check the parent of the file
		 */
		if (objFile.exists() == false && objFile.getParentFile().canWrite() == true) { // if we can't write we should avoid an exception
			try {
				objFile.WriteLine("log4j.rootCategory=info, stdout");
				if (flgUseJobSchedulerLog4JAppender == false) {
					objFile.WriteLine("log4j.appender.stdout=org.apache.log4j.ConsoleAppender");
				}
				else {
					/**
					 * von aussen steuern. bei junit-tests ist der consoleappender richtig, sonst nicht.
					 */
					objFile.WriteLine("log4j.appender.stdout=com.sos.JSHelper.Logging.JobSchedulerLog4JAppender");
				}
				objFile.WriteLine("log4j.appender.stdout.layout=org.apache.log4j.PatternLayout");
				objFile.WriteLine("log4j.appender.stdout.layout.ConversionPattern=%5p [%t] (%F:%L) - %m%n");
				objFile.close();
				flgNew = true;
				flgPropFileIsOk = true;
			}
			catch (Exception e) {
				System.err.println(conClassName + ": unable to create the log4j-property-file " + objFile.getAbsolutePath());
				e.printStackTrace();
				flgPropFileIsOk = false;
			}
		}
		else {
			flgPropFileIsOk = true;
		}

		/**
		 * Vorbereitung des Logging-Environments
		 */
		objRootLog = Logger.getRootLogger();
		if (flgPropFileIsOk == true) {
			PropertyConfigurator.configure(objFile.getAbsolutePath());
			// TODO exception abfangen und direkt konfigurieren
		}
		else {
			/**
			 * hier jetzt direkt konfigurieren
			 */
			try {
				PatternLayout layout = new PatternLayout();
				layout.setConversionPattern("%5p [%t] (%p-%F::%M:%L) - %m%n");
				Appender consoleAppender = null;
				if (flgUseJobSchedulerLog4JAppender == true) {
					consoleAppender = new JobSchedulerLog4JAppender(layout);
				}
				else {
					consoleAppender = new ConsoleAppender(layout);
				}
				objRootLog.addAppender(consoleAppender);
				// ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
				objRootLog.setLevel(Level.INFO);
				objRootLog.debug("Log4J configured programmatically");
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (pstrName.equals(conClassName)) {
			objCurrentLog = objRootLog;
		}
		else {
			objCurrentLog = Logger.getLogger(strName);
		}

		if (flgNew) {
			objRootLog.warn("log4j-property-file '" + objFile.getAbsolutePath() + "' does not exist - a simple default-file was created");
			objRootLog.debug("using log4j-property-file " + objFile.getAbsolutePath());
			objRootLog.warn("all log-entries will be written to the console");
		}
		objLevel = objCurrentLog.getLevel();
	}

	/**
	 * \brief message-Methode aus JSListener �berschreiben
	 *
	 * \details
	 * Diese Klasse implementiert das Interface JSListener, welches die Methode \c message()
	 * verlangt. Damit kann der vielfach im JSHelper verwendeten Methode \c registerMessageListener()
	 * eine Instanz dieser Klasse �bergeben werden, die dann die Ausgabe �bernimmt.
	 * Da das Interface JSListener keine Unterscheidung zwischen einfachen Testausgaben (debug), Hinweisen (info),
	 * Warnungen (warn) und Fehlern (error) macht, erfolgt die Ausgabe in Log4J bei Aufruf von message()
	 * immer mit \c warn um sicherzustellen, dass i.d.R. alle Ausgaben auch im Logging sichtbar sind, selbst
	 * wenn die Log4J-Konfiguration \c debug und \c info unterdr�ckt.
	 * Nach M�glichkeit sollten die Methoden des Log4J-Api verwendet werden, die diese in ihren Ausgaben den Namen
	 * des Java-Files und die Zeile, welche die Ausgabe erzeugt hat korrekt wiedergeben (wenn die Property
	 * ConversionPattern im Konfigurationsfile entsprechend festgelegt worden ist).
	 * \return
	 *
	 * @param pstrMsg
	 */
	@Override
	public void message(final String pstrMsg) {
		objCurrentLog.warn(pstrMsg);
	}

	/**
	 * \brief Ver�ndern des Ausgabelevels zur Laufzeit
	 *
	 * \details
	 * F�r gew�hnlich wird der Ausgabelevel (debug, info, warn, error) einmalig in der log4J-Konfigurationsdatei
	 * vorgegeben. Der Aufruf dieser Methode erlaubt das �ndern des Ausgabelevels zur Laufzeit. Als Parameter
	 * mu� einer der in Log4J zugelassenen Ausgabelevel der Klasse Level �bergeben werden.
	 *
	 * @param intLevel
	 */
	public void setLevel(final Level intLevel) {
		objCurrentLog.setLevel(intLevel);
	}

	/**
	 * \brief Zur�cksetzen des Ausgabelevels
	 *
	 * \details
	 * Setzt (nach einem vorhergehenden Aufruf der Methode setLevel()) den Ausgabelevel auf den Ausgangswert der Konfigurationsdatei zur�ck.
	 *
	 * \return void
	 *
	 */
	public void restoreLevel() {
		setLevel(objLevel);
	}

	/**
	 * \brief Anf�gen einer (externen) Datei als Mail-Anhang
	 *
	 * \details
	 * Neben der Logdatei k�nnen ein oder mehrere Dateien (beispielweise Datenfiles) als Anhang im
	 * Mail mitgeschickt werden.
	 *
	 * @param pstrFilename
	 * @throws Exception
	 */
	public void attachFile(final String pstrFilename) throws Exception {
		JSTextFile objF = new JSTextFile(pstrFilename);
		objF.MustExist();
		objFiles.add(pstrFilename);
	}

	/**
	 * \brief Logfile als Attachment f�r das Mail vormerken
	 *
	 * \details
	 * Mit dieser Methode wird ein Logfile als Attachment f�r das zu versendende Mail vorgemerkt. Als
	 * Parameter wird \b nicht der Name des Files, sondern der Name des Appenders aus der Log4J-Konfiguration
	 * angegeben. Voraussetzung ist, dass es sich um einen \c RollingFileAppender handelt.
	 * \verbatim
		log4j.appender.file=org.apache.log4j.RollingFileAppender
		log4j.appender.file.File=APOTransformer.log
		log4j.appender.file.MaxFileSize=1000KB
		log4j.appender.file.MaxBackupIndex=30
		log4j.appender.file.layout=org.apache.log4j.PatternLayout
		log4j.appender.mailfile.layout.ConversionPattern=%t %-5p %-21d{hh:mm:ss,SSS} (%F:%L) %m %n
	   \endverbatim
	 * Mit dem Aufruf dieser Methode wird der Standard-Appender conFileAppender �berschrieben.
	 *
	 * @param pstrFileAppender - Name des FileAppenders
	 */
	public void setFileAppenderForMail(final String pstrFileAppender) {
		strFileAppender = pstrFileAppender;
	}

	/**
	 * \brief Mailverand des Loggings
	 *
	 * \details
	 * Mit dem Aufruf dieser Methode wird eine Mail versendet, die mindestens einen Logfile als Anhang hat.
	 * Voraussetzung ist, dass die Log4J-Konfigurationdatei den Appender \c conMailAppender vom Typ SMTPAppender
	 * enth�lt.
	 *
	 * \see sendMail(String pstrSubject, String pstrMailAppender)
	 *
	 *  Existiert
	 * \return void
	 *
	 * @param pstrSubject
	 * @throws MissingAppenderException
	 * @throws IllegalAppenderTypeException
	 * @throws FileNotFoundException
	 */
	// public void sendMail(String pstrSubject) throws MissingAppenderException, IllegalAppenderTypeException, FileNotFoundException {
	// sendMail(pstrSubject, conMailAppender);
	// }

	/**
	 * \brief Mailverand des Loggings
	 *
	 * \details
	 * Mit dem Aufruf dieser Methode wird eine Mail versendet, die mindestens einen Logfile als Anhang hat.
	 * Voraussetzung ist, dass die Log4J-Konfigurationdatei den Appender \c pstrMailAppender vom Typ SMTPAppender
	 * enth�lt:
	 * \verbatim
		log4j.appender.mail=org.apache.log4j.net.SMTPAppender
		log4j.appender.mail.BufferSize=1
		log4j.appender.mail.SMTPHost=hostname.com
		log4j.appender.mail.From=sender@root.com
		log4j.appender.mail.To=receiver.address@global.com
		log4j.appender.mail.Subject=an error was detected
		log4j.appender.mail.threshold=error
		log4j.appender.mail.layout=org.apache.log4j.PatternLayout
		log4j.appender.mail.layout.ConversionPattern=%d{ABSOLUTE} %5p %c{1}:%L - %m%n
	   \endverbatim
	 *
	 * Beispielhafter Code f�r den Mailversand:
	 * \code
	 	Log4JMail objMail = logger.newMail();
	 	logger.setFileAppenderForMail("file");
	 	objMail.attachFile(objOptions.apoProdDataFile()); // Datendatei als Attachment
		logger.sendMail("APOTransformer: daily logfile");
	   \endcode
	 *
	 *  Existiert
	 * \return void
	 *
	 * @param pstrSubject
	 * @throws MissingAppenderException
	 * @throws IllegalAppenderTypeException
	 * @throws FileNotFoundException
	 */
	// public void sendMail(String pstrSubject, String pstrMailAppender) throws MissingAppenderException, IllegalAppenderTypeException,
	// FileNotFoundException {
	//
	// objMailApp = checkMailAppender(pstrMailAppender);
	// FileAppender objFile = checkFileAppender(strFileAppender);
	// attachFile(objFile.getFile());
	// Subject = pstrSubject;
	//
	// if (isSubjectPraefix()) {
	// String strPre = (System.getenv("COMPUTERNAME") != null) ? System.getenv("COMPUTERNAME") : "???";
	// Subject = "[" + strPre + "] " + Subject;
	// }
	//
	// if (objFiles.size() == 0) {
	// throw new
	// MissingAppenderException("Kein Mailversand, da kein Logfile in den log4j-properties definiert (Appender 'file' nicht vorhanden");
	// }
	//
	// objRootLog.debug("Mailversand an " + objMailApp.getTo());
	//
	// Properties objProp = new Properties();
	// objProp.put("mail.smtp.host", objMailApp.getSMTPHost());
	// Session objSess = Session.getInstance(objProp);
	// MimeMessage objMess = new MimeMessage(objSess);
	//
	// // Absenderadresse setzen
	// InternetAddress objFrom;
	// try {
	// objFrom = new InternetAddress(objMailApp.getFrom());
	// objMess.setFrom(objFrom);
	// }
	// catch (AddressException e) {
	// objRootLog.error(objMailApp.getFrom() + " ist (formal) keine g�ltige Mailadresse");
	// e.printStackTrace();
	// }
	// catch (MessagingException e) {
	// objRootLog.error("Interner Fehler beim Setzen der Absenderadresse " + objMailApp.getFrom() + "im Message-Objekt der Mail-Api.");
	// e.printStackTrace();
	// }
	//
	// // Emp�ngeradressen setzen
	// String arrTo[] = objMailApp.getTo().split(",");
	// for (int i = 0; i < arrTo.length; i++) {
	// InternetAddress rcpt;
	// try {
	// rcpt = new InternetAddress(arrTo[i]);
	// objMess.addRecipient(Message.RecipientType.TO, rcpt);
	// }
	// catch (AddressException e) {
	// objRootLog.error(arrTo[i] + " ist (formal) keine g�ltige Mailadresse");
	// e.printStackTrace();
	// }
	// catch (MessagingException e) {
	// objRootLog.error("Interner Fehler beim Setzen der Emp�ngeradresse " + objMailApp.getFrom() + "im Message-Objekt der Mail-Api.");
	// e.printStackTrace();
	// }
	// }
	//
	// // Subject setzen
	// try {
	// objMess.setSubject(Subject);
	// }
	// catch (MessagingException e) {
	// objRootLog.error("Interner Fehler beim Setzen des Subjects im Message-Objekt der Mail-Api.");
	// e.printStackTrace();
	// }
	//
	// // Body setzen
	// try {
	//
	// // Bodytext
	// BodyPart objBody = new MimeBodyPart();
	// objBody.setText("logfile created from " + conClassName + "\n");
	// Multipart multipart = new MimeMultipart();
	// multipart.addBodyPart(objBody);
	//
	// // Attachments verarbeiten
	// Enumeration<String> objE = objFiles.elements();
	// while (objE.hasMoreElements()) {
	// objBody = new MimeBodyPart();
	// String strFilename = objE.nextElement();
	// objRootLog.debug("File " + strFilename + " an Mail angehangen");
	// DataSource objSource = new FileDataSource(strFilename);
	// objBody.setDataHandler(new DataHandler(objSource));
	// objBody.setFileName(strFilename);
	// multipart.addBodyPart(objBody);
	// }
	//
	// // Schreiben in das Message-Objekt
	// objMess.setContent(multipart);
	// }
	// catch (MessagingException e1) {
	// objRootLog.error("Interner Fehler beim Schreiben des Bodys im Message-Objekt der Mail-Api.");
	// e1.printStackTrace();
	// }
	//
	// // Mailversand
	// try {
	// Transport.send(objMess);
	// }
	// catch (MessagingException e) {
	// objRootLog.error("Interner Fehler beim Absenden des Mails im Message-Objekt der Mail-Api.");
	// e.printStackTrace();
	// }
	//
	// }

	/**
	 * \brief Ausgabe des Aufrufstacks
	 *
	 * \details
	 * Der Aufrufstack wird normalerweise nicht in den Logfiles ausgegeben.
	 * Diese Methode schreibt den komplette Aufrufstack, so wie er mit dem Kommando
	 * Exception.printStackTrace() auf der Konsole ausgegeben wird, auf die Loggingkan�le.
	 *
	 * Aufrufbeispiel:
	 * \code
		try {
			...
		}
		catch (Throwable e) {
			logger.error(e.getMessage());
			logger.logStackTrace(e);
			e.printStackTrace();
		}
	   \endcode
	 * \return void
	 *
	 * @param e
	 */
	public void logStackTrace(final Throwable e) {
		StackTraceElement objEle[] = e.getStackTrace();
		setLevel(Level.DEBUG);
		for (StackTraceElement s : objEle) {
			String strTarget = s.getFileName() + ":" + s.getLineNumber();
			if (s.getLineNumber() == -1)
				strTarget = "Unknown Source";
			if (s.getLineNumber() == -2)
				strTarget = "Native Method";
			objRootLog.debug("     at " + s.getClassName() + "." + s.getMethodName() + "(" + strTarget + ")");
		}
		restoreLevel();
	}

	/**
	 * \brief Name des Property-Files
	 *
	 * \details
	 * Liefert den Namen der Konfigurationsdatei f�r das Logging.
	 *
	 * \return String
	 */
	public static String getPropertyFile() {
		return strPropfileName;
	}

	/**
	 * \brief Existenz eines Appenders vom Typ \c FileAppender pr�fen
	 *
	 * \details
	 * Pr�ft das Vorhandensein und den Typ des Appenders \c pstrAppender in der Log4J-Konfigurationsdatei.
	 *
	 * \return FileAppender
	 *
	 * @param pstrAppender
	 * @throws MissingAppenderException - Appender ist nicht vorhanden
	 * @throws IllegalAppenderTypeException - Appender ist nicht vom Typ FileAppender
	 */
	public FileAppender checkFileAppender(final String pstrAppender) throws MissingAppenderException, IllegalAppenderTypeException {
		Appender objApp = checkAppender(pstrAppender);
		if (!(objApp instanceof FileAppender)) {
			throw new IllegalAppenderTypeException("Kein Mailversand, da der Appender '" + pstrAppender + "' in '" + getPropertyFile()
					+ " nicht vom Typ 'FileAppender' ist");
		}
		return (FileAppender) objApp;
	}

	/**
	 * \brief Existenz eines Appenders vom Typ \c SMTPAppender pr�fen
	 *
	 * \details
	 * Pr�ft das Vorhandensein und den Typ des Appenders \c pstrAppender in der Log4J-Konfigurationsdatei.
	 *
	 * \return FileAppender
	 *
	 * @param pstrAppender
	 * @throws MissingAppenderException - Appender ist nicht vorhanden
	 * @throws IllegalAppenderTypeException - Appender ist nicht vom Typ SMTPAppender
	 */
	public SMTPAppender checkMailAppender(final String pstrAppender) throws MissingAppenderException, IllegalAppenderTypeException {
		Appender objApp = checkAppender(pstrAppender);
		if (!(objApp instanceof SMTPAppender)) {
			throw new IllegalAppenderTypeException("Kein Mailversand, da der Appender '" + pstrAppender + "' in '" + getPropertyFile()
					+ " nicht vom Typ 'SMTPAppender' ist");
		}
		return (SMTPAppender) objApp;
	}

	/**
	 * \brief Existenz eines Appenders pr�fen
	 *
	 * \details
	 * Pr�ft das Vorhandensein des Appenders \c pstrAppender in der Log4J-Konfigurationsdatei.
	 *
	 * \return FileAppender
	 *
	 * @param pstrAppender
	 * @throws MissingAppenderException - Appender ist nicht vorhanden
	 */
	public Appender checkAppender(final String pstrAppender) throws MissingAppenderException {
		Appender objApp = null;
		objApp = objRootLog.getAppender(pstrAppender);
		if (objApp == null) {
			throw new MissingAppenderException("Kein Mailversand, da keine Mailkonfiguration in den log4j-properties definiert ist (Appender '" + pstrAppender
					+ "' ist nicht in '" + getPropertyFile() + "' vorhanden.");
		}
		return objApp;
	}

	public static void debugAppenders(final Logger pobjLogger) {
		objRootLog.debug("Alle Appenders des Loggers " + pobjLogger.getName());
		Enumeration objE = pobjLogger.getAllAppenders();
		while (objE.hasMoreElements()) {
			Appender objA = (Appender) objE.nextElement();
			objRootLog.debug(objA.getName());
		}
	}

	public boolean isSubjectPraefix() {
		return flgPrintComputerName;
	}

	public void setSubjectPraefix(final boolean pflgPrintComputerName) {
		flgPrintComputerName = pflgPrintComputerName;
	}

}
