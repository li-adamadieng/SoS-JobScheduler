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
package sos.scheduler.process;

import java.io.File;

import sos.net.SOSMail;
import sos.net.SOSMailAttachment;
import sos.spooler.Order;
import sos.util.SOSFile;
import sos.util.SOSString;


/**
 * 
 * @author ghassan.beydoun@sos-berlin.com
 * @since 1.0 2007-07-20
 * $Id: ProcessSendMailOrderMonitor.java 15153 2011-09-14 11:59:34Z kb $ 
 */


public class ProcessSendMailOrderMonitor extends ProcessOrderMonitor {

	
    /**
     * Initialisierung vor Verarbeitung eines Auftrags
     * @see sos.spooler.Monitor_impl#spooler_process_before()
     */
    public boolean spooler_process_before() {
                
        try { // to map order configuration to this job
           
            
            if (!super.spooler_process_before()) return false;
            return true;
            
        } catch (Exception e) {
            spooler_log.warn("error occurred in spooler_process_before(): " + e.getMessage());
            return false;
        }
    }
	
	
    
	public boolean spooler_process_after( boolean rc ) throws Exception {
		
        Order order     = null;
        String orderId  = "(none)";
        
        String to = "";
        String cc = "";
        String bcc = "";
        String subject = "";
        String body = "";
        String contentType = "text/plain";
        String encoding = "Base64";
        
        String attachmentCharset = "iso-8859-1";
        String attachmentContentType = "application/octet-stream";
        String attachmentEncoding = "Base64";
        boolean cleanupAttachment = false;
        String[] attachments = {};
       
        
        String host     = spooler_log.mail().smtp();
        int port        = 25;
        String queueDir = spooler_log.mail().queue_dir();
        String from     = spooler_log.mail().from();
        
        try {
        	spooler_log.info(".. Calling ProcessSendMailOrderMonitor.spooler_task_after()");
        	
            try {
                if (spooler_job.order_queue() != null) {
                    order = spooler_task.order();
                    
                    String names = order.params().names();
                    
                    orderId = order.id();
                
                    if (order.params().value("configuration_path") != null && order.params().value("configuration_path").length() > 0) {
                        this.setConfigurationPath(order.params().value("configuration_path"));
                    } else if (spooler_task.params().value("configuration_path") != null && spooler_task.params().value("configuration_path").length() > 0) {
                        this.setConfigurationPath(spooler_task.params().value("configuration_path"));
                    }

                    if (order.params().value("configuration_file") != null && order.params().value("configuration_file").length() > 0) {
                        this.setConfigurationFilename(order.params().value("configuration_file"));
                    } else if (spooler_task.params().value("configuration_file") != null && spooler_task.params().value("configuration_file").length() > 0) {
                        this.setConfigurationFilename(spooler_task.params().value("configuration_file"));
                    }

                    // load and assign configuration
                    this.initConfiguration();
                    this.prepareConfiguration();
                }


            } catch (Exception e) {
                throw new Exception("error occurred preparing order: " + e.getMessage());
            }
            
            if (doSendMail() ){
            	
            	try {
            		if ( !SOSString.isEmpty ( order.params().value("to") ) ) {
            			to = order.params().value("to");
            		} else {
            			throw new Exception("no value was specified for mandatory parameter [to]");
            		}
            		
            		if ( !SOSString.isEmpty ( order.params().value("subject") ) ) {
            			subject = order.params().value("subject");
            		} else {
            			throw new Exception("no value was specified for mandatory parameter [subject]");
            		}
            		
            		
            		if (   !SOSString.isEmpty ( order.params().value("host") )  ) {
            			host = order.params().value("host");
            		}
            		
            		
            		if ( !SOSString.isEmpty ( order.params().value("port") ) ) {
            			try {
            				port = Integer.parseInt( order.params().value("port") );
            			} catch (Exception e) {
            				throw new Exception("illegal, non-numeric value [" + order.params().value("port") + "] for parameter [port]: " + e.getMessage());
            			}
            		}

            		
            		if (   !SOSString.isEmpty ( order.params().value("queue_directory") )  ) {
            			queueDir = order.params().value("queue_directory");
            		}
            		

            		if (   !SOSString.isEmpty ( order.params().value("from") )  ) {
            			from = order.params().value("from");
            		}
            		
            		
            		if (   !SOSString.isEmpty ( order.params().value("cc") )  ) {
            			cc = order.params().value("cc");
            		}
            		
            		
            		if (   !SOSString.isEmpty ( order.params().value("bcc") )  ) {
            			bcc = order.params().value("bcc");
            		}
            		
            		if (   !SOSString.isEmpty ( order.params().value("body") )  ) {
            			body = order.params().value("body");
            		}
            		
            		if (   !SOSString.isEmpty ( order.params().value("content_type") )  ) {
            			contentType = order.params().value("content_type");
            		}
            		

            		if (   !SOSString.isEmpty ( order.params().value("encoding") )  ) {
            			encoding = order.params().value("encoding");
            		}


            		if (   !SOSString.isEmpty ( order.params().value("attachment_charset") )  ) {
            			attachmentCharset = order.params().value("attachment_charset");
            		}
            		

            		if (   !SOSString.isEmpty ( order.params().value("attachment_content_type") )  ) {
            			attachmentContentType = order.params().value("attachment_content_type");
            		}


            		if (   !SOSString.isEmpty ( order.params().value("attachment_encoding") )  ) {
            			attachmentEncoding = order.params().value("attachment_encoding");
            		}
            		

            		if (   !SOSString.isEmpty ( order.params().value("attachment") )  ) {
            			attachments = order.params().value("attachment").split(";");
            		}
            		
            		if (   !SOSString.isEmpty ( order.params().value("cleanup_attachment") )  ) {
            			if ( order.params().value("cleanup_attachment").equals("1") || order.params().value("cleanup_attachment").equalsIgnoreCase("true") || order.params().value("cleanup_attachment").equalsIgnoreCase("yes") ) {
                            cleanupAttachment = true;
                          }
            		}
            		

            		
            	} catch (Exception e) {
            		throw new Exception("error occurred checking parameters: " + e.getMessage());
            	}
            	
            	try { // to process order
            		SOSMail sosMail = new SOSMail(host);
            		
            		sosMail.setPort(Integer.toString(port));
            		sosMail.setQueueDir(queueDir);
            		sosMail.setFrom(from);
            		sosMail.setContentType(contentType);
            		sosMail.setEncoding(encoding);
            		
            		String recipientsTo[] = to.split(",");
            		for(int i=0; i<recipientsTo.length; i++) {
            			if (i==0) sosMail.setReplyTo(recipientsTo[i].trim()); 
            			sosMail.addRecipient(recipientsTo[i].trim());
            		}
            		
            		String recipientsCC[] = cc.split(",");
            		for(int i=0; i<recipientsCC.length; i++) {
            			sosMail.addCC(recipientsCC[i].trim());
            		}
            		
            		String recipientsBCC[] = bcc.split(",");
            		for(int i=0; i<recipientsBCC.length; i++) {
            			sosMail.addBCC(recipientsBCC[i].trim());
            		}
            		
            		sosMail.setSubject(subject);
            		sosMail.setBody(body);
            		
            		sosMail.setAttachmentCharset(attachmentCharset);
            		sosMail.setAttachmentEncoding(attachmentEncoding);
            		sosMail.setAttachmentContentType(attachmentContentType);
            		
            		for(int i=0; i<attachments.length; i++) {
            			File attachmentFile = new File(attachments[i]);
            			SOSMailAttachment attachment = new SOSMailAttachment(sosMail, attachmentFile);
            			attachment.setCharset(attachmentCharset);
            			attachment.setEncoding(attachmentEncoding);
            			attachment.setContentType(attachmentContentType);
            			sosMail.addAttachment(attachment);
            		}
            		
            		sosMail.setSOSLogger(this.getLogger());
            		this.getLogger().info("sending mail: \n" + sosMail.dumpMessageAsString());
            		
            		if (!sosMail.send()){
            			this.getLogger().warn("mail server is unavailable, mail for recipient [" + to + "] is queued in local directory [" + sosMail.getQueueDir() + "]:" + sosMail.getLastError());
            		}

                    if ( cleanupAttachment ) {
                        for(int i=0; i<attachments.length; i++) {
                          File attachmentFile = new File(attachments[i]);
                          if ( attachmentFile.exists() && attachmentFile.canWrite() ) {
                            SOSFile.deleteFile( attachmentFile );
                          }
                        }
                    }
            		
            		sosMail.clearRecipients();
            		
            	} catch (Exception e) {
            		throw new Exception(e.getMessage());
            	}
            	
            }
            return rc;
            
        } catch (Exception e) {
            spooler_log.warn("error occurred processing order [" + orderId + "]: " + e.getMessage());
            return false;
        }
    }

	/**
	 * This function may be overwritten by other classes
	 * which may check with other parameters if a mail
	 * should be sent or not
	 * @return true if mail should be sent
	 */
	protected boolean doSendMail() {		
		return true;
	}
	
}
