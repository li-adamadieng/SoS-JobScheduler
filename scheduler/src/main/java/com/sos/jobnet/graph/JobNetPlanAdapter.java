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
package com.sos.jobnet.graph;

import com.sos.hibernate.options.HibernateOptions;
import com.sos.jobnet.db.JobNetEdgesDBLayer;
import com.sos.jobnet.db.JobNetPlanDBItem;
import com.sos.jobnet.db.JobNetPlanDBLayer;
import com.sos.jobnet.db.JobNetPlanFilter;
import com.sos.jobnetmodel.objects.JobnetAdapter;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

/*
 * Adapter class to convert the database representation of the jobnet into the internal jobnet model.
 */
public class JobNetPlanAdapter extends JobnetAdapter {
	
	private static Logger logger = Logger.getLogger(JobNetPlanAdapter.class);
	
	private final File configurationFile;
	private final JobNetPlanDBLayer jobNetPlanDBLayer;
	private final JobNetEdgesDBLayer edgesDBReader;
	private final boolean jobnetFound;

	public JobNetPlanAdapter(HibernateOptions options, String uuid) {

		this.configurationFile 	= new File(options.hibernate_connection_config_file.Value());
	    this.jobNetPlanDBLayer 	= new JobNetPlanDBLayer(configurationFile);
		this.edgesDBReader = new JobNetEdgesDBLayer(configurationFile);
	    
	    List<JobNetPlanDBItem> dbRecords = readJobnetNodesFromDB(uuid);
	    this.jobnetFound = (dbRecords.size() == 0) ? false : true;
	    logger.info(dbRecords.size() + " read from database for uuid " + uuid);
	    for(JobNetPlanDBItem record : dbRecords) {
            JobNetPlanRecordAdapter adaptedRecord = new JobNetPlanRecordAdapter(edgesDBReader,record);
            addNode(adaptedRecord);
	    }
	}
	
	private List<JobNetPlanDBItem> readJobnetNodesFromDB(String uuid) {
		JobNetPlanFilter filter = new JobNetPlanFilter();
		filter.setUuid(uuid);
		jobNetPlanDBLayer.setFilter(filter);
		return jobNetPlanDBLayer.getJobnetPlanList(0);
	}
	
	public boolean isJobnetPresentInDB() {
		return jobnetFound;
	}

}