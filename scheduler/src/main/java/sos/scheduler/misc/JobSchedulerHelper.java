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
/*
 * JobSchedulerHelper.java
 * Created on 03.03.2010
 * 
 */
package sos.scheduler.misc;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import sos.connection.SOSConnection;
import sos.connection.SOSMSSQLConnection;
import sos.spooler.Job;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Spooler;
import sos.util.SOSLogger;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

/**
 * This class helps to do some tasks in Job Scheduler which are
 * inconvenient when using the Job Scheduler API
 *
 * @author Andreas Liebert 
 */
public class JobSchedulerHelper {

	private Spooler spooler;
	
	private SOSLogger logger;
	
	private SimpleDateFormat schedulerDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public JobSchedulerHelper(Spooler spo, SOSLogger log){
		this.logger = log;
		this.spooler = spo;
	}
	
	
	/**
	 * Gets a Job Chain object using an absolute or a relative path and the current job
	 * @param jobChainName absolute or relative path of the job chain
	 * @param currentJob object representing the current job (to resolve the relative path)
	 * @return Job_chain object
	 * @throws Exception if the job chain is not found
	 */
	public Job_chain getJobChain(String jobChainName, Job currentJob) throws Exception{
		Job_chain jobChain = null;
		if (spooler.job_chain_exists(jobChainName)){
			jobChain = spooler.job_chain(jobChainName);
		} else {
			String jobChainCompletePath = currentJob.folder_path()+"/"+jobChainName;
			if (!spooler.job_chain_exists(jobChainCompletePath))
				throw new Exception("Job Chain "+jobChainName+" does not exist");
			jobChain = spooler.job_chain(jobChainCompletePath);
		}
		return jobChain;
	}
	
	/**
	 * Gets a Job Chain path using an absolute or a relative path and the current job
	 * @param jobChainName absolute or relative path of the job chain
	 * @param currentJob object representing the current job (to resolve the relative path)
	 * @return Job Chain path
	 * @throws Exception if the job chain is not found
	 */
	public String getJobChainPath(String jobChainName, Job currentJob) throws Exception{
		Job_chain jobChain = null;
		if (spooler.job_chain_exists(jobChainName)){
			return jobChainName;
		} else {
			String jobChainCompletePath = currentJob.folder_path()+"/"+jobChainName;
			if (!spooler.job_chain_exists(jobChainCompletePath))
				throw new Exception("Job Chain "+jobChainName+" does not exist");
			return jobChainCompletePath;
		}		
	}
	
	/**
	 * Finds the last start time of an order
	 * @param jobChainName Name of the Job Chain
	 * @param orderID Id of the order
	 * @param connection connected database connectio object to the Job Scheduler Database
	 * @return Calendar object with last start time
	 * @throws Exception
	 */
	public Calendar getLastStartOfOrder(String jobChainName, String orderID, SOSConnection connection) throws Exception{
		ResultSet rs=null;
		GregorianCalendar cal;
		try{
			//remove leading slash
			if (jobChainName.startsWith("/")) jobChainName = jobChainName.substring(1);
			String maxQuery = "MAX(s.\"ERROR\")=0";
			if (connection instanceof SOSMSSQLConnection){
				// might be bit field
				maxQuery = "MAX(CAST(s.\"ERROR\" AS INT))=0";
			}
			connection.executeStatements("SELECT MAX(h.\"START_TIME\") st FROM SCHEDULER_ORDER_HISTORY h "+
						"WHERE  h.\"SPOOLER_ID\"='"+spooler.id()+"' AND h.\"JOB_CHAIN\"='"+jobChainName+"' "+
						"AND h.\"ORDER_ID\"='"+orderID+"' AND h.\"END_TIME\" IS NOT NULL "+
						"AND h.\"HISTORY_ID\" IN "+
						"(SELECT s.\"HISTORY_ID\" FROM SCHEDULER_ORDER_STEP_HISTORY s GROUP BY s.\"HISTORY_ID\" HAVING "+maxQuery+")");
			
			rs = connection.getResultSet();			
			if (rs==null) throw new Exception("Resultset is null");			
			if (rs.next()) {				
                Timestamp ts = rs.getTimestamp(1);                
                if (ts==null) return null;
                long milliseconds = ts.getTime() + (ts.getNanos() / 1000000);
                cal = new GregorianCalendar();
                cal.setTimeInMillis(milliseconds);
            } else return null;
		} catch(Exception e){
			throw new Exception("Error retrieving last start of order:"+e);
		} finally {
			if (rs != null) try {
                rs.close();
            } catch (Exception e) {
            }
		}
		return cal;
	}
	
	/**
	 * Return the start time of a currently active order
	 * @param order order object for the start time
	 * @return Calendar with the start time of the order
	 * @throws Exception
	 */
	public Calendar getStartOfOrder(Order order) throws Exception{
		String xml = order.xml();
		StringBuffer xmlBuf = new StringBuffer(xml);
		SOSXMLXPath xp = new SOSXMLXPath(xmlBuf);
		String startTime = xp.selectSingleNodeValue("/order/@start_time");
		if (startTime==null || startTime.length()==0) {
			throw new Exception("No start_time attribute was found for the current order");
		}
		Date dat = schedulerDateFormat.parse(startTime);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dat);
		return cal;
	}
}
