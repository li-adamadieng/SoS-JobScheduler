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
package sos.scheduler.editor.doc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.DataElements.JSDataElementDate;
import com.sos.JSHelper.DataElements.JSDateFormat;
import com.sos.JSHelper.io.Files.JSXMLFile;

/**
* \class SourceGenerator
*
* \brief SourceGenerator -
*
* \details
*
* \section SourceGenerator.java_intro_sec Introduction
*
* \section SourceGenerator.java_samples Some Samples
*
* \code
*   .... code goes here ...
* \endcode
*
* <p style="text-align:center">
* <br />---------------------------------------------------------------------------
* <br /> APL/Software GmbH - Berlin
* <br />##### generated by ClaviusXPress (http://www.sos-berlin.com) #########
* <br />---------------------------------------------------------------------------
* </p>
* \author Uwe Risse
* \version 29.09.2011
* \see reference
*
* Created on 29.09.2011 15:38:57
 */

public class SourceGenerator extends JSToolBox {

	private final String		conClassName					= "SourceGenerator";

	private final String		conXsltParmExtendsClassName		= "ExtendsClassName";
	private final String		conXsltParmClassNameExtension	= "ClassNameExtension";
	private final String		conXsltParmVersion				= "version";
	private final String		conXsltParmSourceType			= "sourcetype";
	private final String		conXsltParmClassName			= "ClassName";
	private final String		conXsltParmWorkerClassName		= "WorkerClassName";
	private final String		conJavaFilenameExtension		= ".java";
	private File				jobdocFile;
	private File				outputDir;
	private String				packageName;
	private String				javaClassName;
	private File				templatePath;

	private String				defaultLang						= "en";
	private boolean				standAlone						= true;

	private HashMap	<String, String>			pobjHshMap;

	private static final Logger	logger							= Logger.getLogger(SourceGenerator.class);

	public void execute() {
		logger.setLevel(Level.DEBUG);
		logger.info("Starting transformation");

		try {
			//String strXMLFileName = "c:\\temp\\job.xml";
			String strXMLFileName = jobdocFile.getCanonicalPath();

			JSXMLFile objXMLFile = new JSXMLFile(strXMLFileName);
			objXMLFile.MustExist();

			String strWorkerClassName = jobdocFile.getName();
			strWorkerClassName = strWorkerClassName.replaceAll("\\..*$", "");
			strWorkerClassName = javaClassName;

			File objXSLFile = new File(templatePath, "JSJobDoc2JSOptionSuperClass.xsl");
			pobjHshMap = new HashMap <String, String>();

			setXSLTParameter("package_name", packageName);
			setXSLTParameter("XSLTFilename", objXSLFile.getAbsolutePath());

			setXSLTParameter("default_lang", defaultLang);

			if (standAlone) {
				setXSLTParameter("standalone", "true");
			}
			else {
				setXSLTParameter("standalone", "false");
			}

			JSDataElementDate objDate = new JSDataElementDate(Now());
			objDate.setFormatPattern(JSDateFormat.dfTIMESTAMPS24);
			objDate.setParsePattern(JSDateFormat.dfTIMESTAMPS24);
			String strTimeStamp = objDate.FormattedValue();
			setXSLTParameter("timestamp", strTimeStamp);
			String strClassNameExtension = "OptionsSuperClass";

			setXSLTParameter(conXsltParmClassNameExtension, strClassNameExtension);
			setXSLTParameter(conXsltParmExtendsClassName, "JSOptionsClass");
			setXSLTParameter(conXsltParmVersion, "version");
			setXSLTParameter(conXsltParmSourceType, "options");
			setXSLTParameter(conXsltParmClassName, strWorkerClassName);
			setXSLTParameter(conXsltParmWorkerClassName, strWorkerClassName);
			setXSLTParameter("XMLDocuFilename", objXMLFile.getAbsolutePath());

			objXMLFile.setParameters(pobjHshMap);
			logger.info("Transformation JSOptionsClass");
			doTransform(objXSLFile, objXMLFile, new File(outputDir, strWorkerClassName + strClassNameExtension + conJavaFilenameExtension));

			File objXSLOptionClassFile = new File(templatePath, "JSJobDoc2JSOptionClass.xsl");
			setXSLTParameter("XSLTFilename", objXSLOptionClassFile.getAbsolutePath());

			setXSLTParameter(conXsltParmExtendsClassName, strWorkerClassName + strClassNameExtension);
			strClassNameExtension = "Options";
			setXSLTParameter(conXsltParmClassNameExtension, strClassNameExtension);

			setXSLTParameter(conXsltParmClassName, strWorkerClassName + strClassNameExtension);

			objXMLFile.setParameters(pobjHshMap);
			logger.info("Transformation Options");

			doTransform(objXSLOptionClassFile, objXMLFile, new File(outputDir, strWorkerClassName + strClassNameExtension + conJavaFilenameExtension));

			File objXSLJSAdapterClassFile = new File(templatePath, "JSJobDoc2JSAdapterClass.xsl");
			setXSLTParameter("XSLTFilename", objXSLJSAdapterClassFile.getAbsolutePath());
			setXSLTParameter(conXsltParmExtendsClassName, "JobSchedulerJob");
			strClassNameExtension = "JSAdapterClass";
			setXSLTParameter(conXsltParmClassNameExtension, strClassNameExtension);

			String strClassName = strWorkerClassName + strClassNameExtension;
			setXSLTParameter(conXsltParmClassName, strClassName);
			setXSLTParameter(conXsltParmWorkerClassName, strWorkerClassName);
			setXSLTParameter(conXsltParmSourceType, "JSJavaApiJob");

			objXMLFile.setParameters(pobjHshMap);
			doTransform(objXSLJSAdapterClassFile, objXMLFile, new File(outputDir, strClassName + conJavaFilenameExtension));

			File objXSLJSWorkerClassFile = new File(templatePath, "JSJobDoc2JSWorkerClass.xsl");
			setXSLTParameter("XSLTFilename", objXSLJSWorkerClassFile.getAbsolutePath());
			setXSLTParameter(conXsltParmExtendsClassName, "JSToolBox");
			strClassNameExtension = "";
			setXSLTParameter(conXsltParmClassNameExtension, strClassNameExtension);

			strClassName = strWorkerClassName + strClassNameExtension;
			setXSLTParameter(conXsltParmClassName, strClassName);
			setXSLTParameter(conXsltParmWorkerClassName, strWorkerClassName);

			objXMLFile.setParameters(pobjHshMap);
			doTransform(objXSLJSWorkerClassFile, objXMLFile, new File(outputDir, strClassName.trim() + conJavaFilenameExtension));

			File objXSLJSMainClassFile = new File(templatePath, "JSJobDoc2JSMainClass.xsl");
			setXSLTParameter("XSLTFilename", objXSLJSMainClassFile.getAbsolutePath());

			setXSLTParameter(conXsltParmExtendsClassName, "JSToolBox");
			strClassNameExtension = "Main";
			setXSLTParameter(conXsltParmClassNameExtension, strClassNameExtension);

			strClassName = strWorkerClassName + strClassNameExtension;
			setXSLTParameter(conXsltParmClassName, strClassName.trim());
			setXSLTParameter(conXsltParmWorkerClassName, strWorkerClassName.trim());
			setXSLTParameter(conXsltParmSourceType, "Main");

			objXMLFile.setParameters(pobjHshMap);
			doTransform(objXSLJSMainClassFile, objXMLFile, new File(outputDir, strClassName.trim() + conJavaFilenameExtension));

			File objXSLJSJUnitClassFile = new File(templatePath, "JSJobDoc2JSJUnitClass.xsl");
			setXSLTParameter("XSLTFilename", objXSLJSJUnitClassFile.getAbsolutePath());

			setXSLTParameter(conXsltParmExtendsClassName, "JSToolBox");
			strClassNameExtension = "JUnitTest";
			setXSLTParameter(conXsltParmClassNameExtension, strClassNameExtension);

			strClassName = strWorkerClassName + strClassNameExtension;
			setXSLTParameter(conXsltParmClassName, strClassName);
			setXSLTParameter(conXsltParmWorkerClassName, strWorkerClassName);
			setXSLTParameter(conXsltParmSourceType, "Junit");

			objXMLFile.setParameters(pobjHshMap);
			doTransform(objXSLJSJUnitClassFile, objXMLFile, new File(outputDir, strClassName + conJavaFilenameExtension));

			File objXSLJSJUnitOptionSuperClassFile = new File(templatePath, "JSJobDoc2JSJUnitOptionSuperClass.xsl");
			setXSLTParameter("XSLTFilename", objXSLJSJUnitOptionSuperClassFile.getAbsolutePath());

			setXSLTParameter(conXsltParmExtendsClassName, "JSToolBox");
			strClassNameExtension = "OptionsJUnitTest";
			setXSLTParameter(conXsltParmClassNameExtension, strClassNameExtension);

			strClassName = strWorkerClassName + strClassNameExtension;
			setXSLTParameter(conXsltParmClassName, strClassName);
			setXSLTParameter(conXsltParmWorkerClassName, strWorkerClassName);
			setXSLTParameter(conXsltParmSourceType, "Junit");

			objXMLFile.setParameters(pobjHshMap);
			doTransform(objXSLJSJUnitOptionSuperClassFile, objXMLFile, new File(outputDir, strClassName + conJavaFilenameExtension));

		}
		catch (Exception e) {
			e.printStackTrace(System.err);

		}
	}

	private void setXSLTParameter(final String strVarName, final String strVarValue) {
		final String conMethodName = conClassName + "::setXSLTParameter";

		String strV = strVarValue;
		String strX = String.format("%3$s: Set parameter '%1$s' to Value %2$s.", strVarName, strV, conMethodName);
		pobjHshMap.put(strVarName, strV);
	}

	private void doTransform(final File objXSLFile, final JSXMLFile objXMLFile, final File objOutFile) throws Exception {

		//File objOutFile = new File("c:\\temp","out.txt");
		//objOutFile.deleteOnExit();

		logger.debug("TargetFileName = " + objOutFile.getAbsolutePath());

		objXMLFile.Transform(objXSLFile, objOutFile);

		String strGeneratedContent = getContent(objOutFile.getAbsolutePath());
		logger.info("Size of generated content is " + strGeneratedContent.length());

	}

	// TODO fix this in JSFile
	private String getContent(final String strFileName) {

		// @SuppressWarnings("unused")
		// final String conMethodName = conClassName + "::getContent ";

		String strB = "";

		int filesize = 0;
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(strFileName);
			final byte[] buffer = new byte[4000];
			while (true) {
				final int bytesRead = fin.read(buffer);
				if (bytesRead == -1) {
					break;
				}
				filesize += bytesRead;
				strB = strB + new String(buffer);
			}

		}
		catch (final IOException e) {
			System.err.println(e);
		}
		finally {
			try {
				if (fin != null) {
					fin.close();
				}
			}
			catch (final IOException e) {
			}
		}

		final String strT = strB.substring(0, filesize);
		return strT;
	}

	public void setJobdocFile(final File jobdocFile) {
		this.jobdocFile = jobdocFile;
	}

	public void setOutputDir(final File outputDir) {
		this.outputDir = outputDir;
	}

	public void setPackageName(final String packageName) {
		this.packageName = packageName;
	}

	public void setDefaultLang(final String defaultLang) {
		this.defaultLang = defaultLang;
	}

	public void setStandAlone(final boolean standAlone) {
		this.standAlone = standAlone;
	}

	public void setJavaClassName(final String javaClassName) {
		this.javaClassName = javaClassName;
	}

	public void setTemplatePath(final File templatePath) {
		this.templatePath = templatePath;
	}

//	public static void main(final String[] args) {
//		SourceGenerator s = new SourceGenerator();
//		s.setTemplatePath(new File(
//				"C:/Dokumente und Einstellungen/Uwe Risse/Eigene Dateien/sos-berlin.com/jobscheduler.1.3.9/scheduler_139/config/JOETemplates/java/xsl"));
//		s.setDefaultLang("de");
//		s.setJobdocFile(new File("c:\\temp\\job.xml"));
//		s.setOutputDir(new File("c:\\temp\\out"));
//		s.setJavaClassName("testClass");
//		s.setPackageName("test");
//		s.setStandAlone(true);
//
//		s.execute();
//	}
}
