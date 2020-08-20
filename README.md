# BPMN Spec

A tool to write tests for BPMN workflows.  

![The idea](docs/bpmn-spec.png)

**Features** :sparkles:

* business-friendly: the test spec is written in a text format, no coding is required
* vendor independent: the tests can run against any BPMN engine (if an integration is provided)   

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

...

### Actions

...

### Verifications

...

## Install

...

### JUnit Integration

...
