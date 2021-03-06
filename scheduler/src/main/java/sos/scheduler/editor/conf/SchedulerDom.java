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
package sos.scheduler.editor.conf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;
import org.jdom.output.SAXOutputter;

import sos.scheduler.editor.app.DomParser;
import sos.scheduler.editor.app.Editor;
import sos.scheduler.editor.app.ErrorLog;
import sos.scheduler.editor.app.MainWindow;
import sos.scheduler.editor.app.MergeAllXMLinDirectory;
import sos.scheduler.editor.app.Options;
import sos.scheduler.editor.app.Utils;
import sos.scheduler.editor.conf.forms.SchedulerForm;
import sos.util.SOSFile;

public class SchedulerDom extends DomParser {
	private final static String		conSVNVersion				= "$Id: SchedulerDom.java 17997 2012-09-10 14:15:41Z ur $";
	private static Logger		logger					        = Logger.getLogger(SchedulerDom.class);

	private static final String[]	CONFIG_ELEMENTS				= { "base", "params", "security", "plugins", "cluster", "process_classes", "schedules", "locks", "script",
			"http_server", "holidays", "jobs", "job_chains", "orders", "commands" };
	private static final String[]	JOB_ELEMENTS				= { "settings", "description", "lock.use", "params", "environment", "script", "process",
			"monitor", "start_when_directory_changed", "delay_after_error", "delay_order_after_setback", "run_time", "commands" };
	private static final String[]	RUNTIME_ELEMENTS			= { "period", "at", "date", "weekdays", "monthdays", "ultimos", "month", "holidays" };
	private static final String[]	JOBCHAIN_ELEMENTS			= { "file_order_source", "job_chain_node", "job_chain_node.job_chain", "job_chain_node.end",
			"file_order_sink"									};
	private static final String[]	HOLIDAYS_ELEMENTS			= { "include", "weekdays", "holiday" };
	private static final String[]	PARAMS_ELEMENTS				= { "param", "copy_params", "include" };
	private HashMap<String, String>	changedForDirectory			= new HashMap<String, String>();
	public static final String		MODIFY						= "modify";
	public static final String		DELETE						= "delete";
	public static final String		NEW							= "new";
	private static final String[]	CONFIG_ELEMENTS_DIRECTORY	= { "process_classes", "schedules", "locks", "jobs", "job_chains", "commands" };
	public static final int			CONFIGURATION				= 0;
	private static final String[]	HTTP_SERVER					= { "web_service", "http.authentication", "http_directory" };
	private String					styleSheet					= "";

	private static final String[]	COMMANDS_ELEMENTS			= { "add_order", "order", "start_job" };

	private static final String[]	ORDER_ELEMENTS				= { "params", "environment" };

	private static final String[]	SETTINGS_ELEMENTS			= { "mail_on_error", "mail_on_warning", "mail_on_success", "mail_on_process",
			"mail_on_delay_after_error", "log_mail_to", "log_mail_cc", "log_mail_bcc", "log_level", "history", "history_on_process", "history_with_log" };

	/** live Dateien: Schreibhesch�tzte Dateien*/
	private ArrayList<String>		listOfReadOnlyFiles			= null;

	/** live Dateien: Wenn dateiname ungleich der Element Attribute Name ist, dann wird der Dateiname als Element name-Attribut gesetzt*/
	private ArrayList<String>		listOfChangeElementNames	= null;

	/** Typen der Hot Folder Dateien */
	public static final int			DIRECTORY					= 1;
	public static final int			LIVE_JOB					= 2;
	public static final int			LIVE_JOB_CHAIN				= 3;
	public static final int			LIFE_PROCESS_CLASS			= 4;
	public static final int			LIFE_LOCK					= 5;
	public static final int			LIFE_ORDER					= 6;
	public static final int			LIFE_ADD_ORDER				= 7;
	public static final int			LIFE_SCHEDULE				= 8;

	private boolean					isDirectory					= false;

	/** Gilt nur f�r Hot Folder: Dient zur �berpr�feng ob ausserhalb einer der Hot Folder Dateien von einem  anderen Process ver�ndert wurde*/
	private HashMap<String, Long>	hotFolderFiles				= null;

	public SchedulerDom() {

		super(new String[] { conSchema_SCHEDULER_EDITOR_SCHEMA }, new String[] { Options.getSchema() }, Options.getXSLT());
		putDomOrder("config", CONFIG_ELEMENTS);
		putDomOrder("job", JOB_ELEMENTS);
		putDomOrder("run_time", RUNTIME_ELEMENTS);
		putDomOrder("job_chain", JOBCHAIN_ELEMENTS);
		putDomOrder("http_server", HTTP_SERVER);
		putDomOrder("commands", COMMANDS_ELEMENTS);
		putDomOrder("start_job", ORDER_ELEMENTS);
		putDomOrder("holidays", HOLIDAYS_ELEMENTS);
		putDomOrder("params", PARAMS_ELEMENTS);
		putDomOrder("schedule", RUNTIME_ELEMENTS);
		putDomOrder("settings", SETTINGS_ELEMENTS);

		initScheduler();

	}

	public SchedulerDom(int type) {

		super(new String[] { conSchema_SCHEDULER_EDITOR_SCHEMA }, new String[] { Options.getSchema() }, Options.getXSLT());

		if (type == DIRECTORY) {
			putDomOrder("config", CONFIG_ELEMENTS_DIRECTORY);
			putDomOrder("job", JOB_ELEMENTS);
			putDomOrder("run_time", RUNTIME_ELEMENTS);
			putDomOrder("job_chain", JOBCHAIN_ELEMENTS);
			putDomOrder("commands", COMMANDS_ELEMENTS);
			putDomOrder("params", PARAMS_ELEMENTS);
			putDomOrder("schedule", RUNTIME_ELEMENTS);
			putDomOrder("holidays", HOLIDAYS_ELEMENTS);
			putDomOrder("settings", SETTINGS_ELEMENTS);
			putDomOrder("start_job", ORDER_ELEMENTS);

			isDirectory = true;
			initScheduler();
		}
		else
			if (type == LIVE_JOB) {
				putDomOrder("commands", COMMANDS_ELEMENTS);
				putDomOrder("job", JOB_ELEMENTS);
				putDomOrder("run_time", RUNTIME_ELEMENTS);
				putDomOrder("params", PARAMS_ELEMENTS);
				putDomOrder("holidays", HOLIDAYS_ELEMENTS);
				putDomOrder("settings", SETTINGS_ELEMENTS);

				putDomOrder("start_job", ORDER_ELEMENTS);
				initScheduler(type);
			}
			else
				if (type == LIVE_JOB_CHAIN) {
					// putDomOrder("job_chain", CONFIG_ELEMENTS_DIRECTORY);
					putDomOrder("job_chain", JOBCHAIN_ELEMENTS);
					initScheduler(type);
				}
				else
					if (type == LIFE_ORDER) {
						putDomOrder("commands", COMMANDS_ELEMENTS);
						putDomOrder("run_time", RUNTIME_ELEMENTS);
						putDomOrder("params", PARAMS_ELEMENTS);
						putDomOrder("holidays", HOLIDAYS_ELEMENTS);
						putDomOrder("start_job", ORDER_ELEMENTS);

						initScheduler(type);
					}
					else
						if (type == LIFE_PROCESS_CLASS) {
							putDomOrder("config", new String[] { "process_classes" });
							initScheduler(type);
						}
						else
							if (type == LIFE_LOCK) {
								putDomOrder("config", new String[] { "locks" });
								initScheduler(type);
							}
							else
								if (type == LIFE_SCHEDULE) {
									putDomOrder("config", new String[] { "schedules" });
									putDomOrder("run_time", RUNTIME_ELEMENTS);
									putDomOrder("holidays", HOLIDAYS_ELEMENTS);
									initScheduler(type);
								}
								else {
									new SchedulerDom();
									initScheduler();
								}

	}

	public void initScheduler() {
		Element config = new Element("config");
		setDoc(new Document(new Element("spooler").addContent(config)));
		Element processClasses = new Element("process_classes");
		Element defaultClass = new Element("process_class");
		defaultClass.setAttribute("max_processes", "10");
		config.addContent(processClasses.addContent(defaultClass));
	}

	public void initScheduler(int type) {
		if (type == LIFE_ORDER) {
			Element order = new Element("order");
			order.setAttribute("job_chain", "job_chain1");
			order.setAttribute("id", "id");
			setDoc(new Document(order));
			// setFilename("job1.job.xml");
		}
		else {
			Element elem = null;

			if (type == LIVE_JOB) {
				elem = new Element("job");
				elem.setAttribute("name", "job1");
			}
			else
				if (type == LIVE_JOB_CHAIN) {
					elem = new Element("job_chain");
					elem.setAttribute("name", "job_chain1");
				}
				else
					if (type == LIFE_PROCESS_CLASS) {
						elem = new Element("process_class");
						elem.setAttribute("name", "process_class1");
					}
					else
						if (type == LIFE_LOCK) {
							elem = new Element("lock");
							elem.setAttribute("name", "lock1");
						}
						else
							if (type == LIFE_ORDER) {
								elem = new Element("job_chain");
								elem.setAttribute("name", "job_chain1");
							}
							else
								if (type == LIFE_SCHEDULE) {
									elem = new Element("schedule");
									elem.setAttribute("name", "schedule1");
								}

			setDoc(new Document(elem));

		}

	}

	public boolean read(String filename) throws JDOMException, IOException {
		return read(filename, Options.isValidate());
	}

	public boolean read(String filename, boolean validate) throws JDOMException, IOException {

		StringReader sr = new StringReader(readFile(filename));

		Document doc = getBuilder(validate).build(sr);

		sr.close();
		// doc.getRootElement().getChild("config").getChild("jobs").getChild("job").getChild("params").getChild("param")
		if (doc.getDescendants() != null) {
			Iterator descendants = doc.getDescendants();
			findStyleSheet(descendants);
		}

		// if (!validate && (!doc.hasRootElement() || !doc.getRootElement().getName().equals("spooler")))
		if (!validate && !doc.hasRootElement())
			return false;

		setDoc(doc);

		// set comments as attributes
		setComments(getDoc().getContent(), null);

		setChanged(false);
		setFilename(filename);
		return true;
	}

	public boolean readString(String str, boolean validate) throws JDOMException, IOException {

		StringReader sr = new StringReader(str);
//		logger.debug(str);
		Document doc = getBuilder(validate).build(sr);
		

		sr.close();

		if (!validate && (!doc.hasRootElement() || !doc.getRootElement().getName().equals("spooler")))
			return false;

		setDoc(doc);

		// set comments as attributes
		setComments(getDoc().getContent(), null);

		setChanged(false);
		return true;
	}

	/*public boolean read_2(String filename) throws JDOMException, IOException {

	    StringReader sr = new StringReader(readFile(filename));
	    Document doc = getBuilder(false).build(sr);
	    sr.close();

	    setDoc(doc);

	    // set comments as attributes
	    setComments(getDoc().getContent());

	    setChanged(false);
	    setFilename(filename);
	    return true;
	}*/

	public boolean isEnabled(Element e) {
		String enabledAttr = Utils.getAttributeValue("enabled", e);
		boolean enabled = enabledAttr.equalsIgnoreCase("yes") || enabledAttr.length() == 0;
		return enabled;
	}

	private String readFile(String filename) throws IOException {

		String encoding = DEFAULT_ENCODING;
		String line = null;
		StringBuffer sb = new StringBuffer();

		Pattern p3 = Pattern.compile("<?xml.+encoding\\s*=\\s*\"([^\"]+)\"");

		BufferedReader br = new BufferedReader(new FileReader(filename));
		try {
			while ((line = br.readLine()) != null) {
				Matcher m3 = p3.matcher(line);
				if (m3.find()) {
					encoding = m3.group(1);
				}

				// System.out.println(line);

				sb.append(line + "\n");
			}

			String str = new String(sb.toString().getBytes(), encoding);
			Editor.SCHEDULER_ENCODING = encoding;

			setFilename(filename);
			return str;
		}
		finally {
			br.close();
		}

	}

	public void write(String filename) throws IOException, JDOMException {

		String encoding = Editor.SCHEDULER_ENCODING;
		if (encoding.equals(""))
			encoding = DEFAULT_ENCODING;
		reorderDOM();

		FormatHandler handler = new FormatHandler(this);
		handler.setStyleSheet(styleSheet);
		handler.setEnconding(encoding);

		SAXOutputter saxo = new SAXOutputter(handler);

		saxo.output(getDoc());

		// Document doc = null;
		try {
			getBuilder(true).build(new StringReader(handler.getXML()));
		}
		catch (JDOMException e) {
			try {
				new sos.scheduler.editor.app.ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), e);
			}
			catch (Exception ee) {
				// tu nichts
			}

			int res = MainWindow.message(Messages.getMsg(conMessage_MAIN_LISTENER_OUTPUT_INVALID, e.getMessage()), SWT.ICON_WARNING | SWT.YES | SWT.NO);
			if (res == SWT.NO)
				return;
		}

		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename), encoding);

		writer.write(handler.getXML());
		writer.close();

		// FileOutputStream stream = new FileOutputStream(new File(filename));
		// XMLOutputter out = new XMLOutputter(getFormat());
		// out.output(_doc, stream);
		// stream.close();

		setFilename(filename);

		setChanged(false);

		deorderDOM();

	}

	public void writeElement(String filename, Document doc) throws IOException, JDOMException {

		String encoding = Editor.SCHEDULER_ENCODING;
		if (encoding.equals(""))
			encoding = DEFAULT_ENCODING;

		reorderDOM(doc.getRootElement());

		FormatHandler handler = new FormatHandler(this);
		handler.setStyleSheet(styleSheet);
		handler.setEnconding(encoding);
		SAXOutputter saxo = new SAXOutputter(handler);
		// saxo.output(getDoc());
		saxo.output(doc);

		try {
			getBuilder(true).build(new StringReader(handler.getXML()));
		}
		catch (JDOMException e) {
			try {
				new sos.scheduler.editor.app.ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), e);
			}
			catch (Exception ee) {
				// tu nichts
			}

			//int res = MainWindow.message(Messages.getMsg(conMessage_MAIN_LISTENER_OUTPUT_INVALID, e.getMessage()), SWT.ICON_WARNING | SWT.YES | SWT.NO);
			int res = MainWindow.message( "Element is not valid. Should it still be saved?" + "\n" + e.getMessage(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
			
			if (res == SWT.NO)
				return;
		}

		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename), encoding);

		writer.write(handler.getXML());
		writer.close();

		// FileOutputStream stream = new FileOutputStream(new File(filename));
		// XMLOutputter out = new XMLOutputter(getFormat());
		// out.output(_doc, stream);
		// stream.close();

		// setFilename(filename);

		setChanged(false);
		deorderDOM();
	}

	public String getXML(Element element) throws JDOMException {

		reorderDOM(element);

		FormatHandler handler = new FormatHandler(this);
		handler.setStyleSheet(styleSheet);
		handler.setEnconding(DEFAULT_ENCODING);
		SAXOutputter saxo = new SAXOutputter(handler);
		saxo.output(element);

		deorderDOM();
		return handler.getXML();

	}

	private void setComments(List content, Element plastElement) {
		Element lastElement = plastElement;
		if (content != null) {
			String comment = null;
			for (Iterator it = content.iterator(); it.hasNext();) {
				Object o = it.next();
				if (o instanceof Comment) {
					comment = ((Comment) o).getText();
					if (lastElement != null) {
						lastElement.setAttribute("__comment__", comment.trim());
					}
				}
				else {
					if (o instanceof Element) {
						Element e = (Element) o;
						lastElement = e;
//						if (comment != null) { // set comment as value
//							e.setAttribute("__comment__", comment.trim());
//							comment = null;
//						}
						setComments(e.getContent(), lastElement); // recursion
					}
					else {
						if (!(o instanceof Text)) {
							comment = null;
						}
					}
				}
			}
		}
	}

	public void setChangedForDirectory(Element _parent, String what) {
		Element parent = Utils.getRunTimeParentElement(_parent);
		if (parent != null) {
			if (parent.getName().equals("order") || parent.getName().equals("add_order")) {
				setChangedForDirectory(parent.getName(), Utils.getAttributeValue("job_chain", parent) + "," + Utils.getAttributeValue("id", parent), what);
			}
			else {
				setChangedForDirectory(parent.getName(), Utils.getAttributeValue("name", parent), what);
			}

		}
		/*if(_parent != null) {
			if(_parent.getName().equals("schedule")){
				setChangedForDirectory(_parent.getName(), Utils.getAttributeValue("name",_parent), what);
			} else if(_parent.getParentElement().getName().equals("order")) {
				setChangedForDirectory("order", Utils.getAttributeValue("job_chain",_parent.getParentElement())+","+Utils.getAttributeValue("id",_parent.getParentElement()), what);
			} else {
				setChangedForDirectory(_parent.getParentElement().getName(), Utils.getAttributeValue("name",_parent.getParentElement()), what);
			}
		}*/
	}

	/*
	 * what is: NEW or MODIFY or DELETE
	 */
	public void setChangedForDirectory(String which, String name, String what) {
		if (!isChanged())
			return;

		changedForDirectory.put(which + "_" + name, what);

		String filename = which + "." + name + ".xml";

		if (what.equals(DELETE))
			return;

		SchedulerForm form = (SchedulerForm) MainWindow.getContainer().getCurrentEditor();
		form.setChangedTreeItemText(which + "_" + name);

	}

	public HashMap getChangedJob() {
		return changedForDirectory;
	}

	public void clearChangedJob() {
		changedForDirectory.clear();
	}

	private void findStyleSheet(Iterator descendants) {
		while (descendants != null && descendants.hasNext()) {
			Object o = descendants.next();
			if (o instanceof ProcessingInstruction) {
				ProcessingInstruction h = (ProcessingInstruction) o;
				try {
					styleSheet = "<?" + h.getTarget() + " " + h.getValue() + "?>";
				}
				catch (Exception e) {
					try {
						new ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), e);
					}
					catch (Exception ee) {
						// tu nichts
					}

//					System.out.println("error in SchedulerDom write: " + e.getMessage());
				}
			}
		}
	}

	public ArrayList<String> getListOfReadOnlyFiles() {
		return listOfReadOnlyFiles;
	}

	public void setListOfReadOnlyFiles(ArrayList<String> listOfReadOnlyFiles) {
		this.listOfReadOnlyFiles = listOfReadOnlyFiles;

	}

	public ArrayList<String> getListOfChangeElementNames() {
		return listOfChangeElementNames;
	}

	public void setListOfChangeElementNames(ArrayList<String> listOfChangeElementNames) {
		this.listOfChangeElementNames = listOfChangeElementNames;
		for (int i = 0; i < listOfChangeElementNames.size(); i++) {
			changedForDirectory.put(listOfChangeElementNames.get(i), MODIFY);
		}
	}

	public boolean isLifeElement() {

		return !getRoot().getName().equals("spooler");
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	/**
	 * Liest den letzten �nderungszeitpunkt (in long) der Konfigurationsdatei.
	 * Wurde ausserhalb vom Editor etwas ver�ndert?
	 * 
	 */
	public void readFileLastModified() {
		try {

			/*
			  if(!isDirectory) {    		 
				super.readFileLastModified();
			}
			 */

			if (!isDirectory) {
				super.readFileLastModified();
			}
			else {

				if (getFilename() == null) {
					this.setLastModifiedFile(0);
					return;
				}

				long lastModified = 0;
				File f = new File(getFilename());

				if (f.exists() && f.isDirectory()) {

					ArrayList<File> listOfhotFolderFiles = getHoltFolderFiles(f);
					hotFolderFiles = new HashMap<String, Long>();

					// die letzte �nderung merken
					for (int i = 0; i < listOfhotFolderFiles.size(); i++) {
						File fFile = listOfhotFolderFiles.get(i);
						hotFolderFiles.put(fFile.getName(), fFile.lastModified());
						lastModified = lastModified + fFile.lastModified();
					}

					this.setLastModifiedFile(lastModified);

				}
				else
					this.setLastModifiedFile(0);

				// System.out.println("domparser= " + _lastModifiedFile);

			}
		}
		catch (Exception e) {
			try {
				new sos.scheduler.editor.app.ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), e);
			}
			catch (Exception ee) {
				// tu nichts
			}

		}

	}

	/**
	 * Liefert alle Hot Folder dateinamen
	 * @param java.io.File entspricht das Hot Folder Verzeichnis
	 * @return Liste der Dateinamen. Ein Listeneintrag entspricht einen File Object
	 */
	public ArrayList<File> getHoltFolderFiles(File f) {
		ArrayList<File> listOfhotFolderFiles = new ArrayList<File>();
		try {

			// Alle Hot Folder Dateien nehmen
			listOfhotFolderFiles.addAll(SOSFile.getFilelist(f.getCanonicalPath(), MergeAllXMLinDirectory.MASK_JOB, java.util.regex.Pattern.CASE_INSENSITIVE));
			listOfhotFolderFiles.addAll(SOSFile.getFilelist(f.getCanonicalPath(), MergeAllXMLinDirectory.MASK_JOB_CHAIN,
					java.util.regex.Pattern.CASE_INSENSITIVE));
			listOfhotFolderFiles.addAll(SOSFile.getFilelist(f.getCanonicalPath(), MergeAllXMLinDirectory.MASK_LOCK, java.util.regex.Pattern.CASE_INSENSITIVE));
			listOfhotFolderFiles.addAll(SOSFile.getFilelist(f.getCanonicalPath(), MergeAllXMLinDirectory.MASK_ORDER, java.util.regex.Pattern.CASE_INSENSITIVE));
			listOfhotFolderFiles.addAll(SOSFile.getFilelist(f.getCanonicalPath(), MergeAllXMLinDirectory.MASK_PROCESS_CLASS,
					java.util.regex.Pattern.CASE_INSENSITIVE));
			listOfhotFolderFiles.addAll(SOSFile.getFilelist(f.getCanonicalPath(), MergeAllXMLinDirectory.MASK_SCHEDULE,
					java.util.regex.Pattern.CASE_INSENSITIVE));

		}
		catch (Exception e) {
			try {
				new sos.scheduler.editor.app.ErrorLog("error in " + sos.util.SOSClassUtil.getMethodName(), e);
			}
			catch (Exception ee) {
				// tu nichts
			}

		}
		return listOfhotFolderFiles;
	}

	/**
	 * Liefert alle Hot Folder Dateien mit der letzten �nderungen
	 * key   = File Objekt -> Hot Folder Dateiname name
	 * value = long -> letzte �nderung
	 *  @return the hotFolderFiles
	 */
	public HashMap<String, Long> getHotFolderFiles() {
		return hotFolderFiles;
	}

}
