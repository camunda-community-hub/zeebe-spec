package io.zeebe.bpmnspec.runner.zeebe.zeeqs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ZeeqsVerifications {

    val logger = LoggerFactory.getLogger(ZeeqsVerifications::class.java)

    val httpClient = HttpClient.newHttpClient()

    val objectMapper = ObjectMapper().registerModule(KotlinModule())

    val graphqlUrl = "http://localhost:9000/graphql"

    fun getWorkflowInstanceState(workflowInstanceKey: Long): String {

        val responseBody = sendRequest("{ workflowInstance(key: $workflowInstanceKey) { state } }")
        val response = objectMapper.readValue<WorkflowInstanceResponse>(responseBody)

        return response.data.workflowInstance.state
    }

    private fun sendRequest(query: String): String {
        val requestBody = HttpRequest.BodyPublishers.ofString("""{ "query": "$query" }""")

        val request = HttpRequest.newBuilder()
                .uri(URI.create(graphqlUrl))
                .header("Content-Type", "application/json")
                .POST(requestBody)
                .build()

        logger.trace("Send query request to ZeeQS: {}", query)

        val response = httpClient
                .send(request, HttpResponse.BodyHandlers.ofString())

        val statusCode = response.statusCode()
        val responseBody = response.body()

        logger.trace("Received query response from ZeeQS: [status-code: {}, body: ]", statusCode, responseBody)

        if (statusCode != 200) {
            throw RuntimeException("Failed to query ZeeQS. [status-code: $statusCode, body: $responseBody]")
        }

        return responseBody
    }

    data class WorkflowInstanceResponse(val data: WorkflowInstanceDataDto)

    data class WorkflowInstancesDataDto(val workflowInstances: WorkflowInstancesDto)

    data class WorkflowInstancesDto(val nodes: List<WorkflowInstanceDto>)

    data class WorkflowInstanceDataDto(val workflowInstance: WorkflowInstanceDto)

    data class WorkflowInstanceDto(val state: String)

}