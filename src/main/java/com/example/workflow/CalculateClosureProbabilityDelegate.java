package com.example.workflow;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CalculateClosureProbabilityDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Array of possible priority values
        String[] priorityLevels = {"Prioridad baja", "Prioridad media", "Prioridad alta"};

        // Select a random priority
        Random random = new Random();
        String selectedPriority = priorityLevels[random.nextInt(priorityLevels.length)];

        // Log the selected priority
        System.out.println("Selected Priority: " + selectedPriority);

        // Set the selected priority as a process variable
        execution.setVariable("closureProbability", selectedPriority);
    }
}
