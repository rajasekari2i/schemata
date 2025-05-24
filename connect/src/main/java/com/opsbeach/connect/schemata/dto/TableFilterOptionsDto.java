package com.opsbeach.connect.schemata.dto;

import java.util.List;
import java.util.Set;

public record TableFilterOptionsDto(List<String> owners, List<String> domains, Set<String> subscribers) {

}
