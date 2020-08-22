# BPMN Spec

A tool to write tests for BPMN workflows.  

![The idea](docs/bpmn-spec.png)

**Features** :sparkles:

* business-friendly: the test spec is written in a text format, no coding is required
* vendor independent: the tests can run against any BPMN engine, available integrations
  * [Zeebe](https://github.com/zeebe-io/zeebe)

## Usage

Example spec with one test case:

```yaml
resources:
  - exclusive-gateway.bpmn

testCases:
  - name: fulfill-condition
    description: should fulfill the condition and enter the upper task
    actions:
      - action: create-instance
        args:
          bpmn_process_id: exclusive-gateway
      - action: complete-task
        args:
          job_type: a
          variables: '{"x":8}'

    verifications:
      - verification: element-instance-state
        args:
          element_name: B
          state: activated
``` 

### The Spec

A spec is written in a YAML text format. It contains the following elements:

* `resources`: a list of BPMN files that are used in test cases
* `testCases`: a list of test cases, each test case contains the following elements
  * `name`: the (short) name of the test case
  * `description`: (optional) an additional description of the test case
  * `actions`: a list of actions that are applied in order
  * `verifications`: a list of verifications that are checked in order after all actions are applied

### Actions

Actions drive the test case forward until the result is checked using the verifications. The following actions are available:

* create new instance of a workflow
  * `bpmn_process_id`: the BPMN process id of the workflow
  * `variables`: (optional) initial variables/payload to create the instance with
  * `workflow_instance_alias`: (optional) an alias that can be used in following actions and verifications to reference this instance. This can be useful if multiple instances are created.
```
      - action: create-instance
        args:
          bpmn_process_id: demo
          variables: '{"x":1}'
```
* complete tasks of a given type
  * `job_type`: the type or identifier of the job/task
  * `variables`: (optional) variables/payload to complete the tasks with
```
      - action: complete-task
        args:
          job_type: a
          variables: '{"y":2}'
```
* throw error events for tasks of a given type
  * `job_type`: the type or identifier of the job/task
  * `error_code`: the error code that is used to correlate the error to an catch event
  * `error_message`: (optional) an additional message of the error event  
```
      - action: throw-error
        args:
          job_type: b
          error_code: error-1
```
* publish a message event
  * `message_name`: the name of the message
  * `correlation_key`: the key that is used to correlate the message to a workflow instance
  * `variables`: (optional) variables/payload to publish the message with
```
      - action: publish-message
        args:
          message_name: message-1
          correlation_key: key-1
          variables: '{"z":3}'
```
* cancel/terminate a workflow instance
  * `workflow_instance`: the alias of a workflow instance that is canceled. The alias is defined in the `create-instance` action.
```
      - action: cancel-instance
        args:
          workflow_instance: wf-1
```
* await until an element of the workflow instance is in the given state 
  * `state`: the state of the element to wait for. Must be one of: `activated | completed | terminated | taken` 
  * `element_name`: (optional) the name of the element in the workflow
  * `element_id`: (optional) as an alternative to the name, the element can be identified by its id in the workflow
  * `workflow_instance`: (optional) the alias of a workflow instance. The alias is defined in the `create-instance` action. If only one instance is created then the alias is not required.
```
      - action: await-element-instance-state
        args:
          element_name: B
          state: activated
```

### Verifications

...

## Install

...

### JUnit Integration

...
