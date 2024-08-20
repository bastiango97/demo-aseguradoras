package com.example.workflow;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CalculatePremiumDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Define the minimum and maximum values for the premium
        int minPremium = 5000;
        int maxPremium = 35000;

        // Generate a random value between the minimum and maximum values
        Random random = new Random();
        int premium = random.nextInt(maxPremium - minPremium + 1) + minPremium;

        // Log the calculated premium
        System.out.println("Calculated Premium: " + premium);

        // Set the premium as a process variable
        execution.setVariable("prima", premium);
    }
}
