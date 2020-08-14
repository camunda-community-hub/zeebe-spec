package io.zeebe.bpmnspec.runner.zeebe.zeeqs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ZeeqsVerifications(val zeeqsEndpoint: String = "http://localhost:9000/graphql") {

    val logger = LoggerFactory.getLogger(ZeeqsVerifications::class.java)

    val httpClient = HttpClient.newHttpClient()

    val objectMapper = ObjectMapper().registerModule(KotlinModule())

    fun getWorkflowInstanceKeys(): List<Long> {

        val responseBody = sendRequest("{ workflowInstances { nodes  { key, state } } }")
        val response = objectMapper.readValue<WorkflowInstancesResponse>(responseBody)

        return response.data.workflowInstances.nodes.map { it.key.toLong()  }
    }

    fun getWorkflowInstanceState(workflowInstanceKey: Long): String {

        val responseBody = sendRequest("{ workflowInstance(key: $workflowInstanceKey) { key, state } }")
        val response = objectMapper.readValue<WorkflowInstanceResponse>(responseBody)

        return response.data.workflowInstance.state
    }

    fun getElementInstances(workflowInstanceKey: Long): List<ElementInstanceDto> {

        val responseBody = sendRequest("{ workflowInstance(key: $workflowInstanceKey) { elementInstances { elementId, elementName, state } } }")
        val response = objectMapper.readValue<ElementInstancesResponse>(responseBody)

        return response.data.workflowInstance.elementInstances
    }

    fun getWorkflowInstanceVariables(workflowInstanceKey: Long): List<VariableDto> {

        val responseBody = sendRequest("{ workflowInstance(key: $workflowInstanceKey) { variables { name, value, scope { elementId, elementName } } } }")
        val response = objectMapper.readValue<VariablesResponse>(responseBody)

        return response.data.workflowInstance.variables
    }

    fun getIncidents(workflowInstanceKey: Long): List<IncidentDto> {

        val responseBody = sendRequest("{ workflowInstance(key: $workflowInstanceKey) { incidents { errorType, errorMessage, state, elementInstance { elementId, elementName } } } }")
        val response = objectMapper.readValue<IncidentsResponse>(responseBody)

        return response.data.workflowInstance.incidents
    }

    private fun sendRequest(query: String): String {
        val requestBody = HttpRequest.BodyPublishers.ofString("""{ "query": "$query" }""")

        val request = HttpRequest.newBuilder()
                .uri(URI.create("http://$zeeqsEndpoint"))
                .header("Content-Type", "application/json")
                .POST(requestBody)
                .build()

        logger.trace("Send query request to ZeeQS: {}", query)

        val response = httpClient
                .send(request, HttpResponse.BodyHandlers.ofString())

        val statusCode = response.statusCode()
        val responseBody = response.body()

        logger.trace("Received query response from ZeeQS: [status-code: {}, body: {}]", statusCode, responseBody)

        if (statusCode != 200) {
            throw RuntimeException("Failed to query ZeeQS. [status-code: $statusCode, body: $responseBody]")
        }

        return responseBody
    }

    data class WorkflowInstanceResponse(val data: WorkflowInstanceDataDto)
    data class WorkflowInstanceDataDto(val workflowInstance: WorkflowInstanceDto)
    data class WorkflowInstanceDto(val key: String, val state: String)

    data class WorkflowInstancesResponse(val data: WorkflowInstancesDataDto)
    data class WorkflowInstancesDataDto(val workflowInstances: WorkflowInstancesDto)
    data class WorkflowInstancesDto(val nodes: List<WorkflowInstanceDto>)

    data class ElementInstancesResponse(val data: ElementInstancesDataDto)
    data class ElementInstancesDataDto(val workflowInstance: ElementInstancesDto)
    data class ElementInstancesDto(val elementInstances: List<ElementInstanceDto>)
    data class ElementInstanceDto(val state: String, val elementId: String, val elementName: String?)

    data class VariablesResponse(val data: VariablesDataDto)
    data class VariablesDataDto(val workflowInstance: VariablesDto)
    data class VariablesDto(val variables: List<VariableDto>)
    data class VariableDto(val name: String, val value: String, val scope: VariableScopeDto)
    data class VariableScopeDto(val elementId: String, val elementName: String?)

    data class IncidentsResponse(val data: IncidentsDataDto)
    data class IncidentsDataDto(val workflowInstance: IncidentsDto)
    data class IncidentsDto(val incidents: List<IncidentDto>)
    data class IncidentDto(val errorType: String, val errorMessage: String?, val state: String, val elementInstance: IncidentElementInstanceDto)
    data class IncidentElementInstanceDto(val elementId: String, val elementName: String?)
}
