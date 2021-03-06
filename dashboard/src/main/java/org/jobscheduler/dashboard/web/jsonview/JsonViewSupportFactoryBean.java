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
package org.jobscheduler.dashboard.web.jsonview;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

/**
 * Modified Spring 3.1's internal Return value handlers, and wires up a
 * decorator to add support for @JsonView
 * 
 * @author martypitt
 * 
 */
public class JsonViewSupportFactoryBean implements InitializingBean {

	private final Logger log = LoggerFactory
			.getLogger(JsonViewSupportFactoryBean.class);

	@Autowired
	private RequestMappingHandlerAdapter adapter;

	@Override
	public void afterPropertiesSet() throws Exception {
		List myHandlers = new ArrayList();
		for (HandlerMethodReturnValueHandler handler : adapter
				.getReturnValueHandlers()) {
			if (handler instanceof RequestResponseBodyMethodProcessor) {
				ViewInjectingReturnValueHandler decorator = new ViewInjectingReturnValueHandler(
						handler);
				myHandlers.add(decorator);
			} else
				myHandlers.add(handler);
		}
		adapter.setReturnValueHandlers(myHandlers);
	}

	private void decorateHandlers(List<HandlerMethodReturnValueHandler> handlers) {
		List customList = new ArrayList();
		for (HandlerMethodReturnValueHandler handler : handlers) {
			if (handler instanceof RequestResponseBodyMethodProcessor) {
				handler = new ViewInjectingReturnValueHandler(handler);
				log.info("JsonView decorator support wired up");
			}
			customList.add(handler);
		}
		adapter.setReturnValueHandlers(customList);
	}

}