package de.envite.proa.camundacloud;

import lombok.Data;

@Data
public class ProcessInstancesFilter {

    private Filter filter = new Filter();

    @Data
    static class Filter {

        private String state = "ACTIVE";
    }
}