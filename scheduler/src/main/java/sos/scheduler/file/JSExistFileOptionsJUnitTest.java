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


package sos.scheduler.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.Logging.Log4JHelper;

/**
 * \class 		JSExistFileOptionsJUnitTest - check wether a file exist
 *
 * \brief 
 *
 *

 *
 * see \see C:\Users\KB\Documents\xmltest\JSExistFile.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com at 20110820121039 
 * \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um f�r einen Test eine HashMap
 * mit sinnvollen Werten f�r die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		JSExistFileOptionsJUnitTest.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
public class JSExistFileOptionsJUnitTest extends  JSToolBox {
	private final String					conClassName						= "JSExistFileOptionsJUnitTest"; //$NON-NLS-1$
		@SuppressWarnings("unused") //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JSExistFileOptionsJUnitTest.class);
	@SuppressWarnings("unused")
	private static Log4JHelper	objLogger		= null;
	private JSExistsFile objE = null;

	protected JSExistsFileOptions	objOptions			= null;

	public JSExistFileOptionsJUnitTest() {
		//
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		objLogger = new Log4JHelper("./log4j.properties"); //$NON-NLS-1$
		objE = new JSExistsFile();
		objE.registerMessageListener(this);
		objOptions = objE.Options();
		objOptions.registerMessageListener(this);
		
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
	}

	@After
	public void tearDown() throws Exception {
	}


		

/**
 * \brief testcount_files : Return the size of resultset If this parameter is set true " true
 * 
 * \details
 * 
 *
 */
    @Test
    public void testcount_files() {  // SOSOptionBoolean
    	 objOptions.count_files.value(true);
    	 assertTrue ("Return the size of resultset If this parameter is set true ", objOptions.count_files.value());
    	 objOptions.count_files.value(false);
    	 assertFalse ("Return the size of resultset If this parameter is set true ", objOptions.count_files.value());
    	
    }

                

/**
 * \brief testcreate_order : Activate file-order creation With this parameter it is possible to specif
 * 
 * \details
 * 
 *
 */
    @Test
    public void testcreate_order() {  // SOSOptionBoolean
    	 objOptions.create_order.Value("true");
    	 assertTrue ("Activate file-order creation With this parameter it is possible to specif", objOptions.create_order.value());
    	 objOptions.create_order.Value("false");
    	 assertFalse ("Activate file-order creation With this parameter it is possible to specif", objOptions.create_order.value());
    	
    }

                

/**
 * \brief testcreate_orders_for_all_files : Create a file-order for every file in the result-list
 * 
 * \details
 * 
 *
 */
    @Test
    public void testcreate_orders_for_all_files() {  // SOSOptionBoolean
    	 objOptions.create_orders_for_all_files.Value("true");
    	 assertTrue ("Create a file-order for every file in the result-list", objOptions.create_orders_for_all_files.value());
    	 objOptions.create_orders_for_all_files.Value("false");
    	 assertFalse ("Create a file-order for every file in the result-list", objOptions.create_orders_for_all_files.value());
    	
    }

                

/**
 * \brief testexpected_size_of_result_set : number of expected hits in result-list
 * 
 * \details
 * 
 *
 */
    @Test
    public void testexpected_size_of_result_set() {  // SOSOptionInteger
    	 objOptions.expected_size_of_result_set.Value("12345");
    	 assertEquals ("number of expected hits in result-list", objOptions.expected_size_of_result_set.Value(),"12345");
    	 assertEquals ("number of expected hits in result-list", objOptions.expected_size_of_result_set.value(),12345);
    	 objOptions.expected_size_of_result_set.value(12345);
    	 assertEquals ("number of expected hits in result-list", objOptions.expected_size_of_result_set.Value(),"12345");
    	 assertEquals ("number of expected hits in result-list", objOptions.expected_size_of_result_set.value(),12345);
    	
    }

                

/**
 * \brief testfile : File or Folder to watch for Checked file or directory Supports
 * 
 * \details
 * 
 *
 */
    @Test
    public void testfile() {  // SOSOptionString
    	 objOptions.file.Value(".");
    	 assertEquals ("File or Folder to watch for Checked file or directory Supports", objOptions.file.Value(),".");
    	
    }

                

/**
 * \brief testfile_spec : Regular Expression for filename filtering Regular Expression for file fi
 * 
 * \details
 * 
 *
 */
    @Test
    public void testfile_spec() {  // SOSOptionRegExp
    	objOptions.file_spec.Value("++----++");
    	assertEquals ("Regular Expression for filename filtering Regular Expression for file fi", objOptions.file_spec.Value(),"++----++");
    	
    }

                

/**
 * \brief testgracious : Specify error message tolerance Enables or disables error messages that
 * 
 * \details
 * 
 *
 */
    @Test
    public void testgracious() {  // SOSOptionGracious
    	objOptions.gracious.Value("false");
    	assertEquals ("Specify error message tolerance Enables or disables error messages that", objOptions.gracious.Value(),"false");
    	
    }

                

/**
 * \brief testmax_file_age : maximum age of a file Specifies the maximum age of a file. If a file
 * 
 * \details
 * 
 *
 */
    @Test
    public void testmax_file_age() {  // SOSOptionTime
    	 objOptions.max_file_age.Value("30");
    	 assertEquals ("maximum age of a file Specifies the maximum age of a file. If a file", objOptions.max_file_age.Value(),"30");
    	 assertEquals ("maximum age of a file Specifies the maximum age of a file. If a file", objOptions.max_file_age.getTimeAsSeconds(),30);
    	 objOptions.max_file_age.Value("1:30");
    	 assertEquals ("maximum age of a file Specifies the maximum age of a file. If a file", objOptions.max_file_age.Value(),"1:30");
    	 assertEquals ("maximum age of a file Specifies the maximum age of a file. If a file", objOptions.max_file_age.getTimeAsSeconds(),90);
    	 objOptions.max_file_age.Value("1:10:30");
    	 assertEquals ("maximum age of a file Specifies the maximum age of a file. If a file", objOptions.max_file_age.Value(),"1:10:30");
    	 assertEquals ("maximum age of a file Specifies the maximum age of a file. If a file", objOptions.max_file_age.getTimeAsSeconds(),30+10*60+60*60);
    	
    }

                

/**
 * \brief testmax_file_size : maximum size of a file Specifies the maximum size of a file in
 * 
 * \details
 * Specifies the maximum size of a file in bytes: should the size of one of the files exceed this value, then it is classified as non-existing.
 *
 */
    @Test
    public void testmax_file_size() {  // SOSOptionFileSize
    	 objOptions.max_file_size.Value("25KB");
    	 assertEquals ("maximum size of a file Specifies the maximum size of a file in", objOptions.max_file_size.Value(),"25KB");
    	 objOptions.max_file_size.Value("25MB");
    	 assertEquals ("maximum size of a file Specifies the maximum size of a file in", objOptions.max_file_size.Value(),"25MB");
    	 objOptions.max_file_size.Value("25GB");
    	 assertEquals ("maximum size of a file Specifies the maximum size of a file in", objOptions.max_file_size.Value(),"25GB");
    	
    }

                

/**
 * \brief testmin_file_age : minimum age of a file Specifies the minimum age of a files. If the fi
 * 
 * \details
 * Specifies the minimum age of a files. If the file(s) is newer then it is classified as non-existing, it will be not included in the result-list.
 *
 */
    @Test
    public void testmin_file_age() {  // SOSOptionTime
    	 objOptions.min_file_age.Value("30");
    	 assertEquals ("minimum age of a file Specifies the minimum age of a files. If the fi", objOptions.min_file_age.Value(),"30");
    	 assertEquals ("minimum age of a file Specifies the minimum age of a files. If the fi", objOptions.min_file_age.getTimeAsSeconds(),30);
    	 objOptions.min_file_age.Value("1:30");
    	 assertEquals ("minimum age of a file Specifies the minimum age of a files. If the fi", objOptions.min_file_age.Value(),"1:30");
    	 assertEquals ("minimum age of a file Specifies the minimum age of a files. If the fi", objOptions.min_file_age.getTimeAsSeconds(),90);
    	 objOptions.min_file_age.Value("1:10:30");
    	 assertEquals ("minimum age of a file Specifies the minimum age of a files. If the fi", objOptions.min_file_age.Value(),"1:10:30");
    	 assertEquals ("minimum age of a file Specifies the minimum age of a files. If the fi", objOptions.min_file_age.getTimeAsSeconds(),30+10*60+60*60);
    	
    }

                

/**
 * \brief testmin_file_size : minimum size of one or multiple files Specifies the minimum size of one
 * 
 * \details
 * 
 *
 */
    @Test
    public void testmin_file_size() {  // SOSOptionFileSize
    	 objOptions.min_file_size.Value("25KB");
    	 assertEquals ("minimum size of one or multiple files Specifies the minimum size of one", objOptions.min_file_size.Value(),"25KB");
    	 objOptions.min_file_size.Value("25MB");
    	 assertEquals ("minimum size of one or multiple files Specifies the minimum size of one", objOptions.min_file_size.Value(),"25MB");
    	 objOptions.min_file_size.Value("25GB");
    	 assertEquals ("minimum size of one or multiple files Specifies the minimum size of one", objOptions.min_file_size.Value(),"25GB");
    	
    }

                

/**
 * \brief testnext_state : The first node to execute in a jobchain The name of the node of a jobchai
 * 
 * \details
 * 
 *
 */
    @Test
    public void testnext_state() {  // SOSOptionJobChainNode
    	objOptions.next_state.Value("++----++");
    	assertEquals ("The first node to execute in a jobchain The name of the node of a jobchai", objOptions.next_state.Value(),"++----++");
    	
    }

                

/**
 * \brief teston_empty_result_set : Set next node on empty result set The next Node (Step, Job) to execute i
 * 
 * \details
 * 
 *
 */
    @Test
    public void teston_empty_result_set() {  // SOSOptionJobChainNode
    	objOptions.on_empty_result_set.Value("++empty++");
    	assertEquals ("Set next node on empty result set The next Node (Step, Job) to execute i", objOptions.on_empty_result_set.Value(),"++empty++");
    	
    }

                

/**
 * \brief testorder_jobchain_name : The name of the jobchain which belongs to the order The name of the jobch
 * 
 * \details
 * 
 *
 */
    @Test
    public void testorder_jobchain_name() {  // SOSOptionString
    	 objOptions.order_jobchain_name.Value("++----++");
    	 assertEquals ("The name of the jobchain which belongs to the order The name of the jobch", objOptions.order_jobchain_name.Value(),"++----++");
    	
    }

                

/**
 * \brief testraise_error_if_result_set_is : raise error on expected size of result-set With this parameter it is poss
 * 
 * \details
 * 
 *
 */
    @Test
    public void testraise_error_if_result_set_is() {  // SOSOptionRelOp
    	objOptions.raise_error_if_result_set_is.Value("++0++");
    	assertEquals ("raise error on expected size of result-set With this parameter it is poss", objOptions.raise_error_if_result_set_is.Value(),"++0++");
    	
    }

                

/**
 * \brief testresult_list_file : Name of the result-list file If the value of this parameter specifies a v
 * 
 * \details
 * 
 *
 */
    @Test
    public void testresult_list_file() {  // SOSOptionFileName
    	objOptions.result_list_file.Value("++empty++");
    	assertEquals ("Name of the result-list file If the value of this parameter specifies a v", objOptions.result_list_file.Value(),"++empty++");
    	
    }

                

/**
 * \brief testscheduler_file_name : Name of the file to process for a file-order
 * 
 * \details
 * 
 *
 */
    @Test
    public void testscheduler_file_name() {  // SOSOptionFileName
    	objOptions.scheduler_file_name.Value("++empty++");
    	assertEquals ("Name of the file to process for a file-order", objOptions.scheduler_file_name.Value(),"++empty++");
    	
    }

                

/**
 * \brief testscheduler_file_parent : pathanme of the file to process for a file-order
 * 
 * \details
 * 
 *
 */
    @Test
    public void testscheduler_file_parent() {  // SOSOptionFileName
    	objOptions.scheduler_file_parent.Value("++empty++");
    	assertEquals ("pathanme of the file to process for a file-order", objOptions.scheduler_file_parent.Value(),"++empty++");
    	
    }

                

/**
 * \brief testscheduler_file_path : file to process for a file-order Using Directory Monitoring with
 * 
 * \details
 * 
 *
 */
    @Test
    public void testscheduler_file_path() {  // SOSOptionFileName
    	objOptions.scheduler_file_path.Value("++empty++");
    	assertEquals ("file to process for a file-order Using Directory Monitoring with", objOptions.scheduler_file_path.Value(),"++empty++");
    	
    }

                


/**
 * \brief testscheduler_sosfileoperations_file_count : Return the size of the result set after a file operation
 * 
 * \details
 * 
 *
 */
    @Test
    public void testscheduler_sosfileoperations_file_count() {  // SOSOptionInteger
    	 objOptions.scheduler_sosfileoperations_file_count.Value("12345");
    	 assertEquals ("Return the size of the result set after a file operation", objOptions.scheduler_sosfileoperations_file_count.Value(),"12345");
    	 assertEquals ("Return the size of the result set after a file operation", objOptions.scheduler_sosfileoperations_file_count.value(),12345);
    	 objOptions.scheduler_sosfileoperations_file_count.value(12345);
    	 assertEquals ("Return the size of the result set after a file operation", objOptions.scheduler_sosfileoperations_file_count.Value(),"12345");
    	 assertEquals ("Return the size of the result set after a file operation", objOptions.scheduler_sosfileoperations_file_count.value(),12345);
    	
    }

                

/**
 * \brief testscheduler_sosfileoperations_resultset : The result of the operation as a list of items
 * 
 * \details
 * 
 *
 */
    @Test
    public void testscheduler_sosfileoperations_resultset() {  // SOSOptionstring
    	objOptions.scheduler_sosfileoperations_resultset.Value("++empty++");
    	assertEquals ("The result of the operation as a list of items", objOptions.scheduler_sosfileoperations_resultset.Value(),"++empty++");
    	
    }

                

/**
 * \brief testscheduler_sosfileoperations_resultsetsize : The amount of hits in the result set of the operation
 * 
 * \details
 * 
 *
 */
    @Test
    public void testscheduler_sosfileoperations_resultsetsize() {  // SOSOptionsInteger
    	objOptions.scheduler_sosfileoperations_resultsetsize.Value("++empty++");
    	assertEquals ("The amount of hits in the result set of the operation", objOptions.scheduler_sosfileoperations_resultsetsize.Value(),"++empty++");
    	
    }

                

/**
 * \brief testskip_first_files : number of files to remove from the top of the result-set The numbe
 * 
 * \details
 * 
 *
 */
    @Test
    public void testskip_first_files() {  // SOSOptionInteger
    	 objOptions.skip_first_files.Value("12345");
    	 assertEquals ("number of files to remove from the top of the result-set The numbe", objOptions.skip_first_files.Value(),"12345");
    	 assertEquals ("number of files to remove from the top of the result-set The numbe", objOptions.skip_first_files.value(),12345);
    	 objOptions.skip_first_files.value(12345);
    	 assertEquals ("number of files to remove from the top of the result-set The numbe", objOptions.skip_first_files.Value(),"12345");
    	 assertEquals ("number of files to remove from the top of the result-set The numbe", objOptions.skip_first_files.value(),12345);
    	
    }

                

/**
 * \brief testskip_last_files : number of files to remove from the bottom of the result-set The numbe
 * 
 * \details
 * 
 *
 */
    @Test
    public void testskip_last_files() {  // SOSOptionInteger
    	 objOptions.skip_last_files.Value("12345");
    	 assertEquals ("number of files to remove from the bottom of the result-set The numbe", objOptions.skip_last_files.Value(),"12345");
    	 assertEquals ("number of files to remove from the bottom of the result-set The numbe", objOptions.skip_last_files.value(),12345);
    	 objOptions.skip_last_files.value(12345);
    	 assertEquals ("number of files to remove from the bottom of the result-set The numbe", objOptions.skip_last_files.Value(),"12345");
    	 assertEquals ("number of files to remove from the bottom of the result-set The numbe", objOptions.skip_last_files.value(),12345);
    	
    }

                
        
} // public class JSExistFileOptionsJUnitTest
