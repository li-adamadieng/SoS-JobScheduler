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
package com.sos.jobnet.waiter;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.i18n.I18NBase;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.jobnet.classes.*;
import com.sos.jobnet.db.*;
import com.sos.jobnet.interfaces.IJobNetCollection;
import com.sos.jobnet.interfaces.IJobNetPlanEventListener;
import com.sos.jobnet.interfaces.IPlanItem;
import com.sos.jobnet.interfaces.IWaitHandler;
import com.sos.jobnet.options.JobNetOptions;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
* \class JobNetDBWaiter 
* 
* \brief JobNetDBWaiter - 
* 
* \details
*
* \section JobNetDBWaiter.java_intro_sec Introduction
*
* \section JobNetDBWaiter.java_samples Some Samples
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
* \author oh
* @version $Id: JobNetDBWaiter.java 21040 2013-09-09 16:51:14Z ss $
* \see reference
*
* Created on 22.02.2012 13:56:28
 */
/**
 * @author oh
 *
 */
@I18NResourceBundle(baseName = "com_sos_jobnet_messages", defaultLocale = "en")
public class JobNetDBWaiter extends I18NBase implements IWaitHandler {
	
	private final String		conClassName			= "JobNetDBWaiter";
	private	final String		conSVNVersion			= "$Id: JobNetDBWaiter.java 21040 2013-09-09 16:51:14Z ss $";
	private static final Logger	logger					= Logger.getLogger(JobNetDBWaiter.class);
	private List<JobNetEdgesDBItem> jobNetEdgesList		= null;
	private IJobNetCollection 	waitforNodes 			= new JobNetCollection();
	
	private static final String	JOBNETDBW_E_0010		= "JOBNETDBW_E_0010";		// %s: No item found for the current node (%s) in database table SCHEDULER_JOB_NET_PLAN.
	private static final String	JOBNETDBW_E_0011		= "JOBNETDBW_E_0011";		// %s: No predecessor with node_id=%d found in database table SCHEDULER_JOB_NET_PLAN.
	
	private final JobNetOptions options;
	private final String uuidJobnetIdentifier;
	private final String jobNet;
	private final String node;
	private final String schedulerId;
	private final JSFile hibernateConnectionConfigFile;
	private final String curNodeTitle;
	private final List<String> waitForEvents;
	private final JobNetPlanDBItem currentRecord;
	
	private final EventsDBLayer eventsDBLayer;
	private final JobNetPlanDBLayer	jobNetPlanDBLayer;
	private final JobNetHistoryDBLayer	historyDBLayer;
	private IJobNetPlanEventListener eventListener = null;
	
	public JobNetDBWaiter(JobNetOptions jobNetOptions) {
		super(JobNetConstants.strBundleBaseName);
		this.options = jobNetOptions;
		this.uuidJobnetIdentifier = options.uuid_jobnet_identifier.Value();
		this.jobNet = options.jobnet.Value();
		this.node = options.node.Value();
		this.schedulerId = options.scheduler_id.Value();
		this.hibernateConnectionConfigFile 	= options.hibernate_connection_config_file.JSFile();
 		this.curNodeTitle = uuidJobnetIdentifier + ":" + schedulerId + ":" + jobNet + ":" + node;
		this.waitForEvents = getWaitForEvents(options.wait_for_events.Value());

		String conMethodName = this.getClass().getSimpleName(); 
		logger.info(Messages.getMsg(JobNetConstants.JOBNET_D_0001, conMethodName, "uuid_jobnet_identifier", uuidJobnetIdentifier));
		logger.info(Messages.getMsg(JobNetConstants.JOBNET_D_0001, conMethodName, "jobnet", jobNet));
		logger.info(Messages.getMsg(JobNetConstants.JOBNET_D_0001, conMethodName, "node", node));
		logger.info(Messages.getMsg(JobNetConstants.JOBNET_D_0001, conMethodName, "scheduler_id", schedulerId));
		logger.info(Messages.getMsg(JobNetConstants.JOBNET_D_0001, conMethodName, "hibernate_connection_config_file", hibernateConnectionConfigFile.getAbsolutePath()));
		logger.info(Messages.getMsg(JobNetConstants.JOBNET_D_0001, conMethodName, "wait_for_events", waitForEvents));

		this.jobNetPlanDBLayer 	= new JobNetPlanDBLayer(hibernateConnectionConfigFile, schedulerId);
		this.historyDBLayer = new JobNetHistoryDBLayer(hibernateConnectionConfigFile, schedulerId);
		this.eventsDBLayer = new EventsDBLayer(hibernateConnectionConfigFile);
		this.currentRecord = readCurrentRecordFromDBAndLock();
	}
	
	private JobNetPlanDBItem readCurrentRecordFromDBAndLock() {
		
		final String conMethodName = conClassName + "::getCurNode";
		JobNetPlanDBItem result = null;
		JobNetPlanFilter filter = new JobNetPlanFilter();
		filter.setOrderCriteria(null);
		filter.setSchedulerId(schedulerId);
		filter.setUuid(uuidJobnetIdentifier);
		filter.setJobnet(jobNet);
		filter.setNode(node);
		jobNetPlanDBLayer.setFilter(filter);
		
		List<JobNetPlanDBItem> jobNetPlanDBItem = jobNetPlanDBLayer.getJobnetPlanListWithLock(0);
		if(jobNetPlanDBItem == null || jobNetPlanDBItem.isEmpty()) {
			String msg = Messages.getMsg(JOBNETDBW_E_0010, conMethodName, curNodeTitle);
			logger.error(msg);
			throw new JobNetException(msg);
		}
		else {
			result = jobNetPlanDBItem.get(0);
			logger.debug(Messages.getMsg(JobNetConstants.JOBNET_D_0010, conMethodName, result.getTitle()));
		}
		return result;
		
	}
	
	private List<String> getWaitForEvents(String eventsString) {
		return Arrays.asList(eventsString.split(";"));
	}
	
	/**
	 * \brief getCurNode
	 * 
	 * \details
	 */
	private JobNetPlanDBItem getCurrentRecord() {
		return currentRecord;
	}
		
	/**
	 * \brief getJobnetEdgesList
	 * 
	 * \details
	 */
	private List<JobNetEdgesDBItem> getJobnetEdgesList() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getJobnetEdgesList";
		getCurrentRecord();
		if (jobNetEdgesList == null) {
			File configurationFile 	= new File(hibernateConnectionConfigFile.getAbsolutePath());
			JobNetEdgesDBLayer jobNetEdgesDBLayer = new JobNetEdgesDBLayer(configurationFile);
			jobNetEdgesList = jobNetEdgesDBLayer.getPredecessors(getCurrentRecord().getJobnetNodeDBItem());
		}
		return jobNetEdgesList;
	} // public List<JobNetEdgesDBItem> getJobnetEdgesList
	
	
	/**
	 * \brief updateWaiterStatus
	 * 
	 * \details
	 *
	 * @param status
	 */
	private void updateWaiterStatus(NodeStatus status) {
		final String conMethodName = conClassName + "::updateWaiterStatus";
		JobNetPlanDBItem record = getCurrentRecord();
		if (record != null) {
			int oldState = record.getStatusWaiter();
			record.setStatusWaiter(status.getIndex());
			logger.debug(Messages.getMsg(JobNetConstants.JOBNET_D_0011, conMethodName, Messages.getMsg(JobNetConstants.JOBNET_T_0010), status.getMsg()));
			update();
			if (eventListener != null)
				eventListener.waiterStatusChanged(status, NodeStatus.valueOf(oldState) );
//			after each updateWaiterStatus follows updateNodeStatus which calls the commit, 
//			so that only one sql transaction is used (see JobNetWaiter::run)
//			update(new Date());
		}
	} // private void updateWaiterStatus
	
	/**
	 * \brief updateRunnerStatus
	 * 
	 * \details
	 *
	 * @param status
	 */
	private void updateRunnerStatus(NodeStatus status) {
		final String conMethodName = conClassName + "::updateRunnerStatus";
		JobNetPlanDBItem record = getCurrentRecord();
		if (record != null) {
			int oldState = record.getStatusRunner();
			record.setStatusRunner(status.getIndex());
			logger.debug(Messages.getMsg(JobNetConstants.JOBNET_D_0011, conMethodName, Messages.getMsg(JobNetConstants.JOBNET_T_0011), status.getMsg()));
			update();
			if (eventListener != null)
				eventListener.runnerStatusChanged(status, NodeStatus.valueOf(oldState) );
//			after each updateRunnerStatus follows updateNodeStatus which calls the commit, 
//			so that only one sql transaction is used (see JobNetWaiter::run)
//			update(new Date()); 
		}
	} // private void updateRunnerStatus
	
	
	/**
	 * \brief updateNodeStatus
	 * 
	 * \details
	 *
	 * @param status
	 */
	private void updateNodeStatus(NodeStatus status) {
		final String conMethodName = conClassName + "::updateNodeStatus";
		JobNetPlanDBItem rec = getCurrentRecord();
		if (rec!=null) {
			int oldState = rec.getStatusNode();
			rec.setStatusNode(status.getIndex());
			logger.debug(Messages.getMsg(JobNetConstants.JOBNET_D_0011, conMethodName, Messages.getMsg(JobNetConstants.JOBNET_T_0013), status.getMsg()));
			update();
			nodeStatusChanged(rec, NodeStatus.valueOf(oldState) );
		}
	} // private void updateNodeStatus
	
	
	/**
	 * \brief update
	 * 
	 * \details
	 */
	private void update() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::update";
		getCurrentRecord().setModified( new Date() );
		getCurrentRecord().setModifiedBy(JobNetDBWaiter.class.getName());
		jobNetPlanDBLayer.beginTransaction();
		jobNetPlanDBLayer.saveOrUpdate(getCurrentRecord());
		jobNetPlanDBLayer.commit();
	} // private void update
	
	
	private String getI18NYesNo(boolean flgIsYes) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getI18NYesNo";
		String msg = Messages.getMsg(JobNetConstants.JOBNET_T_0002); //No
		if (flgIsYes) {
			msg = Messages.getMsg(JobNetConstants.JOBNET_T_0001); //Yes
		}
		return msg;
	}

	
	/**
	 * \brief isBootstrap
	 * 
	 * \details
	 *
	 * \return 
	 *
	 * @return boolean
	 */
	@Override
	public boolean isBootstrap() {
		final String conMethodName = conClassName + "::isBootstrap";
		boolean isBootstrap = getCurrentRecord().getBootstrap();
		logger.debug(Messages.getMsg(JobNetConstants.JOBNET_D_0021, conMethodName, getI18NYesNo(isBootstrap)));
		return isBootstrap;
	} // public boolean isBootstrap

	
	/**
	 * \brief setWaiterStatusToRunning
	 * 
	 * \details
	 */
	@Override
	public void setWaiterStatusToRunning() {
		updateWaiterStatus(NodeStatus.RUNNING);
	}

	/**
	 * \brief setNodeStatusToRunning
	 * 
	 * \details
	 */
	@Override
	public void setNodeStatusToRunning() {
		updateNodeStatus(NodeStatus.RUNNING);
	}
	


	/**
	 * \brief isWaiterSkipped
	 * 
	 * \details
	 *
	 * \return 
	 *
	 * @return boolean
	 */
	@Override
	public boolean isWaiterSkipped() {
		final String conMethodName = conClassName + "::isWaiterSkipped";
		boolean isSkipped = getCurrentRecord().getIsWaiterSkipped();
		logger.debug(Messages.getMsg(JobNetConstants.JOBNET_D_0020, conMethodName, Messages.getMsg(JobNetConstants.JOBNET_T_0010), getI18NYesNo(isSkipped)));
		return isSkipped;
	}
	

	/**
	 * \brief isRunnerSkipped
	 * 
	 * \details
	 *
	 * \return 
	 *
	 * @return boolean
	 */
	@Override
	public boolean isRunnerSkipped() {
		final String conMethodName = conClassName + "::isRunnerSkipped";
		boolean isSkipped = getCurrentRecord().getIsRunnerSkipped();
		logger.debug(Messages.getMsg(JobNetConstants.JOBNET_D_0020, conMethodName, Messages.getMsg(JobNetConstants.JOBNET_T_0011), getI18NYesNo(isSkipped)));
		return isSkipped;
	}
	
	@Override
	public boolean isRunnerRunning() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::isRunnerRunning";
        NodeStatus status = NodeStatus.valueOf(getCurrentRecord().getStatusRunner());
		return status.isRunning();
	}
	
	/**
	 * \brief isRunnerOnDemand
	 * 
	 * \details
	 *
	 * \return 
	 *
	 * @return boolean
	 */
	@Override
	public boolean isRunnerOnDemand() {
		final String conMethodName = conClassName + "::isRunnerOnDemand";
		boolean isOnDemand = getCurrentRecord().getIsRunnerOnDemand();
		logger.debug(Messages.getMsg(JobNetConstants.JOBNET_D_0022, conMethodName, Messages.getMsg(JobNetConstants.JOBNET_T_0011), getI18NYesNo(isOnDemand)));
		return isOnDemand;
	}

	/**
	 * \brief setNodeStatusToWaiting
	 * 
	 * \details
	 */
	@Override
	public void setNodeStatusToWaiting(Integer taskId) {
        getCurrentRecord().setTaskId( Long.valueOf(taskId) );
		updateNodeStatus(NodeStatus.WAITING);
	}

	/**
	 * \brief setRunnerStatusToRunning
	 * 
	 * \details
	 */
	@Override
	public void setRunnerStatusToRunning() {
		updateRunnerStatus(NodeStatus.RUNNING);
	}

	/**
	 * \brief setWaiterStatusToFinish
	 * 
	 * \details
	 */
	@Override
	public void setWaiterStatusToFinish() {
		updateWaiterStatus(NodeStatus.FINISHED);
	}

	/**
	 * \brief setWaiterStatusToError
	 * 
	 * \details
	 */
	@Override
	public void setWaiterStatusToError() {
		updateWaiterStatus(NodeStatus.ERROR);
	}

	/**
	 * \brief setNodeStatusToError
	 * 
	 * \details
	 */
	@Override
	public void setNodeStatusToError() {
		updateNodeStatus(NodeStatus.ERROR);
	}
	
	/**
	 * \brief setStartTime
	 * 
	 * \details
	 */
	@Override
	public void setStartTime() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setStartTime";
		if (getCurrentRecord() != null) {
			Date now = new Date();
			getCurrentRecord().setStartTime(now);
//			after each setStartTime follows updateNodeStatus which calls the commit, 
//			so that only one sql transaction is used (see JobNetWaiter::run)
//			update(now); 
		}
	}
	
	@Override
	public IJobNetCollection getWaitingNodes() {
		final String conMethodName = conClassName + "::getWaitingNodes";
		getJobnetEdgesList();
		
		List<JobNetPlanDBItem> jobNetPlanDBItem = null;
		JobNetPlanDBItem jobnetPlanDBItem = null;
		waitforNodes = new JobNetCollection();
		
		for (JobNetEdgesDBItem jobnetEdgesItem : jobNetEdgesList) {
			jobNetPlanDBLayer.getFilter().setNode(null);
			jobNetPlanDBLayer.getFilter().setNodeId(jobnetEdgesItem.getChildNodeId());
			jobNetPlanDBItem = jobNetPlanDBLayer.getJobnetPlanList(0);
			
			if(jobNetPlanDBItem == null || jobNetPlanDBItem.isEmpty()) {
				String msg = Messages.getMsg(JOBNETDBW_E_0011, conMethodName, jobnetEdgesItem.getChildNodeId()); 
				logger.error(msg);
				throw new JobNetException(msg);
			}
			
			jobnetPlanDBItem = jobNetPlanDBItem.get(0);
			String predecessor = jobnetPlanDBItem.getJobnetNodeItem().getNode();
			NodeStatus status = NodeStatus.valueOf(jobnetPlanDBItem.getStatusNode());
			
			logger.info(Messages.getMsg(JobNetConstants.JOBNETW_I_0010, conMethodName, predecessor, status.getMsg()));
			if (status.isFinished()) {
				continue;
			}
			waitforNodes.add(jobnetPlanDBItem);
		}
		return waitforNodes;
	}

	@Override
	public void open() {
		logger.info(String.format("%1s %2s", conClassName, conSVNVersion));
		getCurrentRecord();
		//TODO skips in Options lesen und in DB setzen
		//Ist unnoetig, wenn das dashboard die Skip-Funktionalitaet zur Verfuegung stellt
	}

	@Override
	public void close() {
		eventsDBLayer.closeSession();
		jobNetPlanDBLayer.closeSession();
	}

	@Override
	public EventsCollection getMissingEvents() {
		return eventsDBLayer.checkEvents(waitForEvents);
	}

	@Override
	public boolean isNodeStarted() {
		NodeStatus status = NodeStatus.valueOf(getCurrentRecord().getStatusNode());
		return status.isStarted();
	}

	@Override
	public boolean isNodeStarting() {
        NodeStatus status = NodeStatus.valueOf(getCurrentRecord().getStatusNode());
		return status.isStarting();
	}

	@Override
	public boolean isNodeNotProcessed() {
        NodeStatus status = NodeStatus.valueOf(getCurrentRecord().getStatusNode());
		return status.isNotProcessed();
	}


	public void nodeStatusChanged(IPlanItem node, NodeStatus oldState) {
		if(eventListener != null) {
			eventListener.nodeStatusChanged(node, oldState);
		}
	}
	
	@Override
	public boolean isReadyForProcessing() {
		return ( isNodeStarted() || isNodeInError() || isJobnetStart() );
	}
	
	@Override
	public boolean isJobnetStart() {
		return (isNodeNotProcessed() && isBootstrap());
	}

	public void setNodeStatusListener(IJobNetPlanEventListener listener) {
		this.eventListener = listener;
	}

	@Override
	public boolean isNodeInError() {
        NodeStatus status = NodeStatus.valueOf(getCurrentRecord().getStatusNode());
		return status.isError();
	}
	
	@Override
	public void createEvent(EventsDBItem record) {
		eventsDBLayer.createEvent(record);
	}

    // update the history record for the jobnetz
    @Override
    public void startHistory() {
        historyDBLayer.startHistory( getCurrentRecord().getJobNetHistoryDBItem() );
    }

    // update the history record for the jobnetz
    @Override
    public void updateHistory() {
        historyDBLayer.updateHistory( getCurrentRecord().getJobNetHistoryDBItem() );
    }

    // update the history record for the jobnetz
    @Override
    public void updateHistoryWithError(Integer exitCode, String exitMessage) {
        historyDBLayer.updateHistoryWithError(getCurrentRecord().getJobNetHistoryDBItem(),exitCode, exitMessage);
    }

    // update the history record for the jobnetz
    @Override
    public void endHistory() {
        historyDBLayer.endHistory(getCurrentRecord().getJobNetHistoryDBItem());
    }

}