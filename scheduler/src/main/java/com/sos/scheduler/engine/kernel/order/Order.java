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
package com.sos.scheduler.engine.kernel.order;

import com.sos.scheduler.engine.common.Lazy;
import com.sos.scheduler.engine.cplusplus.runtime.Sister;
import com.sos.scheduler.engine.cplusplus.runtime.SisterType;
import com.sos.scheduler.engine.cplusplus.runtime.annotation.ForCpp;
import com.sos.scheduler.engine.data.folder.AbsolutePath;
import com.sos.scheduler.engine.data.folder.FileBasedType;
import com.sos.scheduler.engine.data.folder.JobChainPath;
import com.sos.scheduler.engine.data.order.OrderId;
import com.sos.scheduler.engine.data.order.OrderKey;
import com.sos.scheduler.engine.data.order.OrderState;
import com.sos.scheduler.engine.eventbus.HasUnmodifiableDelegate;
import com.sos.scheduler.engine.kernel.cppproxy.Job_chainC;
import com.sos.scheduler.engine.kernel.cppproxy.OrderC;
import com.sos.scheduler.engine.kernel.folder.FileBased;
import com.sos.scheduler.engine.kernel.log.PrefixLog;
import com.sos.scheduler.engine.kernel.order.jobchain.JobChain;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerException;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

import javax.annotation.Nullable;

@ForCpp
public final class Order extends FileBased implements UnmodifiableOrder, HasUnmodifiableDelegate<UnmodifiableOrder>, Sister {
    private final OrderC cppProxy;
    private final Lazy<UnmodifiableOrder> unmodifiableDelegate = new Lazy<UnmodifiableOrder>() {
        @Override protected UnmodifiableOrder compute() {
            return new UnmodifiableOrderDelegate(Order.this);
        }
    };

    Order(OrderC cppProxy) {
        this.cppProxy = cppProxy;
    }

    @Override public void onCppProxyInvalidated() {}

    public void remove() {
        cppProxy.java_remove();
    }

    @Override public OrderKey getKey() {
        return new OrderKey(getJobChainPath(), getId());
    }

    @Override public OrderId getId() {
        return new OrderId(cppProxy.string_id());
    }

    @Override public OrderState getState() {
        return new OrderState(cppProxy.string_state());
    }

    public void setEndState(OrderState s) {
        cppProxy.set_end_state(s.string());
    }

//	public String getFilePath() {
//        return cppProxy.file_path();
//    }

    @Override public OrderState getEndState() {
        return new OrderState(cppProxy.string_end_state());
    }

    @Override public boolean suspended() {
        return cppProxy.suspended();
    }

    @Override public void setSuspended(boolean b) {
        cppProxy.set_suspended(b);
    }

    @Override public String getTitle() {
        return cppProxy.title();
    }

    public void setTitle(String o) {
        cppProxy.set_title(o);
    }

    @Override public FileBasedType getFileBasedType() {
        return FileBasedType.order;
    }

    @Override public AbsolutePath getPath() {
        return new AbsolutePath(cppProxy.path());
    }

    public JobChainPath getJobChainPath() {
        return JobChainPath.of(cppProxy.job_chain_path_string());
    }

    @Override public JobChain getJobChain() {
        JobChain result = getJobChainOrNull();
        if (result == null)  throw new SchedulerException("Order is not in a job chain: "+this);
        return result;
    }

    @Override @Nullable public JobChain getJobChainOrNull() {
        Job_chainC jobChainC = cppProxy.job_chain();
        return jobChainC == null? null : jobChainC.getSister();
    }

    @Override public VariableSet getParameters() {
        return cppProxy.params().getSister();
    }

    @Override public PrefixLog getLog() {
        return cppProxy.log().getSister();
    }

    @Override public UnmodifiableOrder unmodifiableDelegate() {
        return unmodifiableDelegate.get();
    }

    @Override public String toString() {
        String result = getClass().getSimpleName();
        if (cppProxy.cppReferenceIsValid())  result += " " + getId().toString();
        return result;
    }


    public static class Type implements SisterType<Order, OrderC> {
        @Override public Order sister(OrderC proxy, Sister context) {
            return new Order(proxy);
        }
    }
}
