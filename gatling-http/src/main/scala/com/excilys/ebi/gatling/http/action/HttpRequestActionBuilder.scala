/**
 * Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.gatling.http.action

import com.excilys.ebi.gatling.core.action.builder.ActionBuilder
import com.excilys.ebi.gatling.core.action.system
import com.excilys.ebi.gatling.core.config.ProtocolConfigurationRegistry
import com.excilys.ebi.gatling.core.session.Expression
import com.excilys.ebi.gatling.http.check.HttpCheck
import com.excilys.ebi.gatling.http.check.status.HttpStatusCheckBuilder.status
import com.excilys.ebi.gatling.http.request.HttpPhase.StatusReceived
import com.excilys.ebi.gatling.http.request.builder.AbstractHttpRequestBuilder

import akka.actor.{ ActorRef, Props }
import scalaz.Scalaz.ToValidationV

object HttpRequestActionBuilder {

	/**
	 * This is the default HTTP check used to verify that the response status is 2XX
	 */
	val DEFAULT_HTTP_STATUS_CHECK = status.find.in(Session => (200 to 210).success).build

	def apply(requestName: Expression[String], requestBuilder: AbstractHttpRequestBuilder[_], checks: List[HttpCheck[_]]) = {

		val resolvedChecks = checks
			.find(_.phase == StatusReceived)
			.map(_ => checks)
			.getOrElse(HttpRequestActionBuilder.DEFAULT_HTTP_STATUS_CHECK :: checks)

		new HttpRequestActionBuilder(requestName, requestBuilder, resolvedChecks)
	}
}

/**
 * Builder for HttpRequestActionBuilder
 *
 * @constructor creates an HttpRequestActionBuilder
 * @param requestBuilder the builder for the request that will be sent
 * @param next the next action to be executed
 */
class HttpRequestActionBuilder(requestName: Expression[String], requestBuilder: AbstractHttpRequestBuilder[_], checks: List[HttpCheck[_]]) extends ActionBuilder {

	private[gatling] def build(next: ActorRef, protocolConfigurationRegistry: ProtocolConfigurationRegistry): ActorRef = system.actorOf(Props(HttpRequestAction(requestName, next, requestBuilder, checks, protocolConfigurationRegistry)))
}
