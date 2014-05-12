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
package com.sos.jobnet.jobs;

//import JobSchedulerJobAdapter;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionUUID;
import com.sos.jobnet.classes.*;
import com.sos.jobnet.creator.JobNetIdCalculator;
import com.sos.jobnet.db.EventsDBItem;
import com.sos.jobnet.dispatcher.JobNetDispatcher;
import com.sos.jobnet.interfaces.IJobNetCallback;
import com.sos.jobnet.interfaces.IMessageListener;
import com.sos.jobnet.interfaces.IPlanItem;
import com.sos.jobnet.options.JobNetIdCalculatorOptions;
import com.sos.jobnet.options.JobNetOptions;
import com.sos.jobnet.options.SOSOptionRestartJobnetNode;
import com.sos.jobnet.waiter.JobNetWaiter;
import com.sos.jobnetmodel.live.JobnetOrderOptions;
import com.sos.scheduler.model.objects.JSObjOrder;
import com.sos.scheduler.model.objects.Param;
import com.sos.scheduler.model.objects.Params;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Job_chain_node;
import sos.spooler.Order;
import sos.spooler.Supervisor_client;
import sos.spooler.Variable_set;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
// import org.apache.log4j.Logger;

/**
 * \class JobNetWaitAndDispatch 
 * 
 * \brief JobNetWaitAndDispatch - 
 * 
 * \details
 *
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
 * \author KB
 * \version $Id$
 * \see reference
 *
 * Created on 28.02.2012 13:10:54
 */

/**
 * @author KB
 * 
 */
public class JobNetWaitAndDispatch extends JobSchedulerJobAdapter implements
		IJobNetCallback, IMessageListener {

	private final String conClassName = "JobNetWaitAndDispatch";
	@SuppressWarnings("unused")
	private static final String conSVNVersion = "$Id$";

	private static final Logger logger = Logger.getLogger(JobNetWaitAndDispatch.class);
	private JobNetProtocol jobnetProtocol = null;
	private StatusProtocol statusProtocol = null;
	private boolean flgRunnerIsSkipped = false;
	private boolean flgWaiterOK = true;
	private boolean flgDispatcherDone = false;
	private final static int maxLengthOfStatusText = 256;
    private boolean appenderInitialized = false;
//	private Logger jobnetProtocol = null;
	
	private class JobnetIdCalculation {
		private boolean isPresent = false;
		private String paramName;
		private String paramValue;
		private String originalValue;
	}

	public JobNetWaitAndDispatch() {
		//
	}

    @Override
	public boolean spooler_process_before() {

        flgRunnerIsSkipped = false;
		flgWaiterOK = true;
		flgDispatcherDone = false;
        JobNetOptions options = null;

        try {
			initialize();

			JobnetIdCalculation calculatedId = calculateJobNetId();
	        if(calculatedId.isPresent) {
		        spooler_log.info("JobnetId for this order was calculated: " + calculatedId.paramName + "=" + calculatedId.paramValue);
		        spooler_log.info("The original value was " + calculatedId.originalValue);
		        spooler_task.order().params().set_value(calculatedId.paramName, calculatedId.paramValue);
	        }
	        logOrderParams();
	        
			JobNetWaiter jobNetWaiter = new JobNetWaiter( spooler_task.id() );
			jobNetWaiter.setIJobNetCallback(this);
			options = jobNetWaiter.Options();
			jobNetWaiter.setOptionsUsingHashMap(getAllParametersAsProperties());
			options.scheduler_id.Value(spooler.id());
			String orderId = getOrderIdForDBNode();
			logger.info(String.format("Checking order %1s in job chain %2s ", orderId, options.jobnet.Value()));

			initializeProtocol(orderId, options.jobnet,options.uuid_jobnet_identifier);
			jobnetProtocol.log(Level.INFO,String.format("%1$s is started.",orderId));

			options.node.Value(orderId);
			
			setCheckMandatory(options);
			options.CheckMandatory();
			jobNetWaiter.doInit();
			flgWaiterOK = jobNetWaiter.run();

			if(calculatedId.isPresent) {
		        spooler_task.order().params().set_value(calculatedId.paramName, calculatedId.originalValue);
		        spooler_log.info("JobnetId for this order was reset to original value: " + calculatedId.paramName + "=" + calculatedId.originalValue);
	        }

		} catch (Exception e) {
			flgWaiterOK = false;
			raiseException(e);
		}

        boolean result = flgRunnerIsSkipped == false && flgWaiterOK;
        log(Level.INFO,"Waiter finished with: " + result);

		// run the dispatcher immediately and set the next state if runner is skipped
		// otherwise the order will be suspended if spooler_process_before ends with false 
		if (flgRunnerIsSkipped  && flgWaiterOK) {
            String msg = "Processing of the order is skipped, because %reason%.";
            if(options.restart_option.isContinueIgnore())
                msg = msg.replace("%reason%","an existing error is ignored");
            else
                msg = msg.replace("%reason%","because the runtime of the order does not allow the execution");
            logger.info(msg);
			if (jobnetProtocol != null) jobnetProtocol.info(msg);
			do_task_after();
			String nextState = getLastStateOfChain(spooler_task.order().job_chain_node());
			log(Level.INFO,"Order is set to state " + nextState);
			spooler_task.order().set_state(nextState);
		}
		return result;
	}
	
	/**
	 * A running order has an id with the pattern order.jebnetid, but in the nodes table is stored only the
	 * order name, not the jobnetid. We need the order without id to reference the node in the db.
	 * @return
	 */
	private String getOrderIdForDBNode() {
		String id = spooler_task.order().id();
		int i = id.lastIndexOf(JobNetConstants.JOBNET_ID_DELIMITER);
		return i > 0 ? id.substring(0,i) : id;
	}
	
	private void logOrderParams() {
		log(Level.INFO,"List of all order parameters after calculation:");
		log(Level.INFO,"===============================================");
        String[] arr = spooler_task.order().params().names().split(";");
        for(String key : arr) {
    		log(Level.INFO,key + "=" + spooler_task.order().params().value(key));
        }
	}
	
	@Override
	public void spooler_task_after() {

		// is already done if runner was skipped (see spooler_process_before)
		if (flgWaiterOK && !flgDispatcherDone) {
			try {
				do_task_after();
			} catch (Exception e) {
				raiseException(e);
			}
		}
		log(Level.INFO,"Order is finished.");

	}

	private String getLastStateOfChain(final Job_chain_node job_chain_node) {
		String currentState = job_chain_node.state();
		Job_chain_node job_chain_next_node = job_chain_node.next_node();
		while(job_chain_next_node != null) {
			currentState = job_chain_next_node.state();
			job_chain_next_node = job_chain_next_node.next_node();
		}
		return currentState;
	}

	private void do_task_after() {
		final String conMethodName = conClassName + "::spooler_task_after";
		try {
			initialize();

			JobnetOrderOptions jobnetOrderOptions = new JobnetOrderOptions();
			jobnetOrderOptions.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
			jobnetOrderOptions.CheckMandatory();
			
			JobNetDispatcher jobNetDispatcher = new JobNetDispatcher();
			jobNetDispatcher.setIJobNetCallback(this);
			JobNetOptions dispatcherOptions = jobNetDispatcher.Options();
			String orderId = getOrderIdForDBNode();
			jobNetDispatcher.setOptionsUsingHashMap(getAllParametersAsProperties());

			initializeProtocol(orderId, dispatcherOptions.jobnet,dispatcherOptions.uuid_jobnet_identifier);
			logger.info(String.format("Checking order %1$s in job chain %2$s ",orderId,dispatcherOptions.jobnet.Value()));

			dispatcherOptions.scheduler_id.Value(spooler.id());
			dispatcherOptions.node.Value(orderId);
			setCheckMandatory(dispatcherOptions);
			jobNetDispatcher.Options().CheckMandatory();
			jobNetDispatcher.doInit();
			jobNetDispatcher.run();
			
			// in case of successful execution of the order an event is thrown
			
			/*
            spooler_log.info("Options of the jobnet order");
			spooler_log.info("===========================");
			for(String key : jobnetOrderOptions.Settings().keySet()) {
				spooler_log.info(key + "=" + jobnetOrderOptions.getOrderRecord(key));
			}
			*/
			String scope = spooler_task.exit_code() == 0 ? "success" : "error";
			String eventId = getEventId(jobnetOrderOptions, scope);
			if(!eventId.isEmpty()) {
				log(Level.INFO,"Try to throw event " + eventId);
				jobNetDispatcher.createEvent(eventId);
			} else {
				log(Level.INFO,"No event to throw.");
			}
			
			// put an end notification in the protocol
			log(Level.INFO,String.format("End: %1$s.",orderId));
			flgDispatcherDone = true;

            /**
             * It is important to log via spooler_log, because logging vid slf4j will not place in the JobScheduler protocol here.
             */
            if (dispatcherOptions.restart_option.isContinueIgnore()) {
                spooler_log.info("A previous error of the node ignored, because 'RestartOption' was set to " + dispatcherOptions.restart_option.Value() + ".");
                spooler_log.info("Option 'RestartOption' was reset.");
                dispatcherOptions.restart_option.setValue(SOSOptionRestartJobnetNode.RestartModes.none);
                setNextState();
            }

        } catch (Exception e) {
			raiseException(e);
		}
	}
	
	/**
	 * The event will be unique by appending the id of the jobnet.
	 * 
	 * @return
	 */
	private String getEventId(final JobnetOrderOptions options, final String scope) {
		String eventName = scope.equals("success") 
				? options.event_on_success.Value()
				: options.event_on_error.Value();
		return eventName;
	}

	private void initialize() {
        if (!appenderInitialized) {
		    initializeLog4jAppenderClass();
            appenderInitialized = true;
        }
	}

	private void initializeProtocol(final String orderId, final SOSOptionString jobnet, final SOSOptionUUID uuid) {
		jobnetProtocol = new JobNetProtocol(orderId, jobnet, uuid);
		statusProtocol = new StatusProtocol(orderId, jobnet, uuid);
	}

	private void raiseException(final Exception e) {
        spooler_log.error(e.getMessage());
		throw new JobSchedulerException(e.getMessage(), e);
	}

	// TODO move to optionsClass !!!!!!!!!!!!!!!!!!!!!!!
	private void setCheckMandatory(final JobNetOptions objOptions) {
		//Das fliegt hier raus und kommt in die Options.
		objOptions.uuid_jobnet_identifier.isMandatory(true);
		objOptions.hibernate_connection_config_file.isMandatory(true);
		objOptions.scheduler_id.isMandatory(true);
		objOptions.jobnet.isMandatory(true);
		objOptions.node.isMandatory(true);

        SOSOptionRestartJobnetNode restartOption = objOptions.restart_option;
        if (restartOption.isInvalid())
            throw new JobNetException("The restart_option " +objOptions.restart_option.Value() + " is not valid - possible entries are " + restartOption.getAllOptionsAsString());
 	}

	/**
	 * The exit code given from the job can be ignored by setting the option valid_errors.
	 * If affects the flow of the jobnet only, not the flow of the jobnet node order.
	 */
	@Override
	public int getExitCode() {
		return spooler_task.exit_code();
	}

    @Override
    public void setNextState() {
        String nextState = spooler_task.order().job_chain_node().next_state();
        logger.info("The order state was set to " + nextState);
        spooler_task.order().set_state(nextState);
    }

	/**
	 * Start an order for a given jobnet node.
	 */
	@Override
	public void startNode(final IPlanItem planItem, final JobNetOptions options) {
		
		
		logger.info(String.format("Starting Plan.Node: %1s.%2s",planItem.getPlanId(),planItem.getNodeId()));
		logger.debug(String.format("  Node: %1s",planItem.getJobnetNodeItem().getNode()));
		logger.debug(String.format("  in Net: %1s",planItem.getJobnetNodeItem().getJobnet()));
		
		String uuid = planItem.getUuid();
		String jobChain = planItem.getJobnetNodeItem().getJobnet();
		String id = planItem.getJobnetNodeItem().getNode();
		JSOrderStarter starter = new JSOrderStarter(spooler.hostname(), spooler.tcp_port(), jobChain, uuid, spooler);
		starter.addMessageListener(this);
		starter.setId(id);
		// starter.setIdAndAddJobnetId(id);	// start erstmal ohne id + uuid

		JSObjOrder orderFromDB = new JSObjOrder(starter.getFactory());
		orderFromDB.getOrderFromXMLString(planItem.getOrderXml());
 		Params paramsFromDB = orderFromDB.getParams();
 		for(Object o : paramsFromDB.getParamOrCopyParamsOrInclude()) {
 			if(o instanceof Param) {
	 			Param p = (Param)o;
	 			logger.debug("Param from database order: " + p.getName() + "=" + p.getValue());
 	 		}
 		}
 		
 		Order currentOrder = spooler_task.order();
 		if(currentOrder == null)
 			throw new JobSchedulerException("The order object is not set for the current order.");
 		

 		replaceScriptRoot( paramsFromDB );

 		starter.setStartType(getStartMode(paramsFromDB));
		starter.setReplace(true);
		starter.setAt("now");
		starter.setTitle(orderFromDB.getTitle());
		starter.setState(orderFromDB.getState());
 		starter.setParams(paramsFromDB);
 		starter.run();

	}
	
	private String getStartMode(final Params paramsFromDB) {
		String result = "tcp";
		for (Object o : paramsFromDB.getParamOrCopyParamsOrInclude()) {
			if (o instanceof Param) {
				Param p = (Param)o;
				if (p.getName().equals(JobNetGlobalParams.TransferType.name())) {
					result = p.getValue().toLowerCase();
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Calculates a jobnet id for an order
	 */
	private JobnetIdCalculation calculateJobNetId() {
		JobnetIdCalculation result = new JobnetIdCalculation();
		try {
			JobNetIdCalculatorOptions calcOptions = new JobNetIdCalculatorOptions();
			calcOptions.setAllOptions(getAllParametersAsProperties());			// replaces all %params% occurences as well
			if(!calcOptions.getResultParameterName().IsEmpty()) {
				result.paramName = calcOptions.getResultParameterName().Value();
				result.originalValue = spooler_task.order().params().value(result.paramName);
				calcOptions.CheckMandatory();
				JobNetIdCalculator calculator = new JobNetIdCalculator(calcOptions,isOrderJob());
				result.paramValue = calculator.execute();
				result.isPresent = true;
			}
		} catch (Exception e) {
			String msg = "Error calculating JobNetId";
			log(Level.ERROR,msg);
			throw new JobSchedulerException(msg,e);
		}
		return result;
	}
	
	/**
	 * Puts the parameter from the current (running) order to the order to start. Placeholders like
	 * %varname% will be replaced with the real value of the parameter.
	 * Only params that no JobNetOrderParams itself (see class JobNetOrderParams) are merged.
	 * 
	 * @param fromVariables
	 * @param toParams
	 */
	private void mergeParams(final Variable_set fromVariables, final Params toParams) {
		HashMap<String,String> allParams = getAllParametersAsProperties();
		HashMap<String,String> workParamsReplaced = getSchedulerParameterAsProperties(fromVariables);
		HashMap<String,String> fromParamsReplaced = removeAdditionalParameters(workParamsReplaced, fromVariables);
 		for(String key : fromParamsReplaced.keySet()) {
			if (!hasKey(toParams,key) && JobNetGlobalParams.isItem(key)) {
				String paramValue = fromParamsReplaced.get(key);
				paramValue = replaceVars(allParams, "", paramValue);	// replace placeholder like %param%
 				addParam(toParams,key,paramValue);
 				spooler_log.info("variable merged from current order: " + key + "=" + paramValue);
 			}
 		}
	}

	private void replaceScriptRoot(final Params inParams) {
		String searchFor = "%" + JobNetConstants.SCRIPT_ROOT_GLOBAL_PARAMETER_NAME + "%";
		String replaceWith = spooler.variables().value(JobNetConstants.SCRIPT_ROOT_GLOBAL_PARAMETER_NAME);
		logger.debug("Try to replace all occurences of " + searchFor + " in the order parameters with " + replaceWith);
		int cnt = 0;
		for(int i=0; i<inParams.getParamOrCopyParamsOrInclude().size(); i++) {
			Object o = inParams.getParamOrCopyParamsOrInclude().get(i);
			if(o instanceof Param) {
				Param p = (Param)o;
				String orgValue = p.getValue();
				if(orgValue.contains(searchFor)) {
					String newValue = orgValue.replaceAll(searchFor,replaceWith);
					p.setValue(newValue);	// replace placeholder like %param%
					inParams.getParamOrCopyParamsOrInclude().set(i, p);
					logger.debug("Parameter " + p.getName() + " modified to " + newValue);
				}
			}
		}
	}
	
	private HashMap<String,String> removeAdditionalParameters(final HashMap<String,String> removeFrom, final Variable_set original) {
		HashMap<String,String> result = new HashMap<String,String>();
		String[] names = original.names().split(";");
		List<String> originalList = Arrays.asList(names);
		for(String key : removeFrom.keySet()) {
			if(originalList.contains(key))
				result.put(key, removeFrom.get(key));
		}
		return result;
	}
	
	private static boolean hasKey(final Params paramsToCheck, final String key) {
		boolean hasKey = false;
		Iterator<Object> it = paramsToCheck.getParamOrCopyParamsOrInclude().iterator();
		while(it.hasNext()) {
			Object o = it.next();
			if(o instanceof Param) {
				Param p = (Param)o;
				if(p.getName().equals(key))
					hasKey = true;
			}
		}
		return hasKey;
	}
	
	private void addParam(final Params toList, final String name, final String value) {
 		Param param = new Param();
		param.setValue(value);
		param.setName(name);
		toList.getParamOrCopyParamsOrInclude().add(param);
	}

	@Override
	public void skipRunner() {
		flgRunnerIsSkipped = true;
	}

	@Override
	public void setStateText(String stateText) {
		if(stateText.length() > maxLengthOfStatusText)
			stateText = stateText.substring(0,maxLengthOfStatusText - 4) + "...";		// maximum of state text in JobScheduler DB is 100
		spooler_task.order().set_state_text(stateText);
	}

	@Override
	public void setCC(final int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void log(final Level level, final String stateText, final Throwable error) {
		// if (jobnetProtocol != null)	jobnetProtocol.log(level,stateText, error);
		logger.log(level,stateText, error);
	}

	@Override
	public void log(final Level level, final String stateText) {
		// if (jobnetProtocol != null)	jobnetProtocol.log(level,stateText);
		logger.log(level,stateText);
	}

	@Override
	public void logStatus(final IPlanItem record, final NodeStatus oldStatus) {
        NodeStatus status = NodeStatus.valueOf(record.getStatusNode());
		String msg = "nodestatus of id " + record.getNodeId() +  " is set from " + oldStatus.name() + " to " + status.name();
		if (jobnetProtocol != null)	jobnetProtocol.log(Level.INFO,msg);
		if (statusProtocol != null)	statusProtocol.log(Level.INFO,msg);
		// logger.info(msg);
	}

	@Override
	public void logContextStatus(final String context, final NodeStatus fromStatus, final NodeStatus toStatus) {
		String msg = context + " changed from  " + fromStatus.name() +  " to " + toStatus.name();
		if (jobnetProtocol != null)	jobnetProtocol.log(Level.INFO,msg);
		if (statusProtocol != null)	statusProtocol.log(Level.INFO,msg);
		// logger.info(msg);
	}

	@Override
	public EventsDBItem createEventRecord(final String forEventClass, final String forEventId) {
        EventsDBItem record = new EventsDBItem();
        record.setSchedulerId(spooler.id());
        record.setHost(getHost(spooler));
        record.setPort(getPort(spooler));
        record.setJobChain(spooler_task.order().job_chain().name());
        record.setOrderId(spooler_task.order().id());
        record.setJobName(spooler_task.job().name());
        record.setEventClass(forEventClass.toLowerCase());
        record.setEventId(forEventId.toLowerCase());
        record.setParameters("");
        record.setExitCode(String.valueOf(spooler_task.exit_code()));
		return record;
	}
	private static String getHost(final sos.spooler.Spooler scheduler) {
		String result = null;
		try {
			Supervisor_client supervisor = scheduler.supervisor_client();
			result = supervisor.hostname();
		}
		catch (Exception e) { // there is no supervisor
			result = scheduler.hostname();
		}
		return result;
	}
	
	private static Long getPort(final sos.spooler.Spooler scheduler) {
		int result = -1;
		try {
			Supervisor_client supervisor = scheduler.supervisor_client();
			result = supervisor.tcp_port();
		}
		catch (Exception e) { // there is no supervisor
			result = scheduler.tcp_port();
		}
		return Long.valueOf(result);
	}


	@Override
	public void jobnetStarted(final JobNetWaiter jobNetwaiter) {

        String uuid = jobNetwaiter.Options().uuid_jobnet_identifier.Value();

        createEventJobnetStarted(jobNetwaiter, uuid);

		String jobChain = jobNetwaiter.Options().GraphBuilderJobChain.Value().trim();
		if(jobChain.isEmpty()) {
			logger.info("No JobChain to build the jobnet graph is configured - use Parameter GraphBuilderJobChain if necessary." );
		} else {
            startJobnetGraphJobChain(jobNetwaiter, uuid, jobChain);
		}
	}

    private void createEventJobnetStarted(final JobNetWaiter jobNetwaiter, String uuid) {
        String eventId = JobNetConstants.JOBNET_STARTED_PREFIX + uuid;
        jobNetwaiter.createEvent(eventId);
        String logText = "Jobnet started.";
        log(Level.INFO,logText);
    }

    private void startJobnetGraphJobChain(final JobNetWaiter jobNetwaiter, String uuid, String jobChain) {
        JSOrderStarter starter = new JSOrderStarter(spooler.hostname(), spooler.tcp_port(), jobChain, uuid, spooler);
        //TODO should be variabel
        starter.setStartType("tcp");
        starter.addMessageListener(this);
        starter.setId("graphbuilder." + uuid);
        starter.setReplace(true);
        starter.setAt("now");
        starter.setTitle("GraphBuilder for jobnet " + uuid);
        starter.addParam("JobNetGraphBuilderOptions.uuid_jobnet_identifier", uuid);
        starter.run();
    }


	@Override
	public void jobnetEnded(final JobNetDispatcher jobNetDispatcher) {
		String uuid = jobNetDispatcher.Options().uuid_jobnet_identifier.Value();
		String eventId = JobNetConstants.JOBNET_ENDED_PREFIX + uuid;
		// An order to create the jobnet graph will stop if this event is fired.
		jobNetDispatcher.createEvent(eventId);
		String logText = "All nodes of the jobnet are in state FINISHED or ERROR - jobnet ended.";
		log(Level.INFO,logText);
	}

	@Override
	public void onMessage(final Level level, final String message) {
		log(level,message);
	}
	
}