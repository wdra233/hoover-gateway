package com.eric.projects.hoover.core.mappings;

import com.eric.projects.hoover.config.MappingProperties;
import com.eric.projects.hoover.exceptions.HooverException;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.springframework.util.CollectionUtils.isEmpty;

public class MappingsValidator {

    public void validate(List<MappingProperties> mappings) {
        if (!isEmpty(mappings)) {
            mappings.forEach(this::correctMapping);
            int numberOfNames = mappings.stream()
                    .map(MappingProperties::getName)
                    .collect(toSet())
                    .size();
            if (numberOfNames < mappings.size()) {
                throw new HooverException("Duplicated route names in mappings");
            }

            int numberOfHosts = mappings.stream()
                    .map(MappingProperties::getHost)
                    .collect(toSet())
                    .size();
            if (numberOfHosts < mappings.size()) {
                throw new HooverException("Duplicated route hosts in mappings");
            }

            mappings.sort((m1, m2) -> m1.getHost().compareTo(m2.getHost()));
        }
    }

    protected void correctMapping(MappingProperties mapping) {
        validateName(mapping);
        validateDestinations(mapping);
        validateHost(mapping);
        validateTimeout(mapping);
    }

    protected void validateName(MappingProperties mapping) {
        if (isBlank(mapping.getName())) {
            throw new HooverException("Empty name for mapping " + mapping);
        }
    }


    protected void validateDestinations(MappingProperties mapping) {
        if (isEmpty(mapping.getDestinations())) {
            throw new HooverException("No destination hosts for mapping" + mapping);
        }
        List<String> correctedHosts = new ArrayList<>(mapping.getDestinations().size());
        mapping.getDestinations().forEach(destination -> {
            if (isBlank(destination)) {
                throw new HooverException("Empty destination for mapping " + mapping);
            }
            if (!destination.matches(".+://.+")) {
                destination = "http://" + destination;
            }
            destination = removeEnd(destination, "/");
            correctedHosts.add(destination);
        });
        mapping.setDestinations(correctedHosts);
    }

    protected void validateHost(MappingProperties mapping) {
        if (isBlank(mapping.getHost())) {
            throw new HooverException("No source host for mapping " + mapping);
        }
    }

    protected void validateTimeout(MappingProperties mapping) {
        int connectTimeout = mapping.getTimeout().getConnect();
        if (connectTimeout < 0) {
            throw new HooverException("Invalid connect timeout value: " + connectTimeout);
        }

        int readTimeout = mapping.getTimeout().getRead();
        if (readTimeout < 0) {
            throw new HooverException("Invalid read timeout value: " + readTimeout);
        }
    }
}
