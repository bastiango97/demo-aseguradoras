package com.example.workflow;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class AutoCompleteTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // This task does nothing and automatically completes.
        System.out.println("Llamada a API externa");
    }
}
