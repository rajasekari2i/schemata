package com.opsbeach.connect.schemata.entity;

import java.util.List;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Node
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Organization {
    
    @Id @GeneratedValue
    private Long id;

    private String name;

    private Long clinetId;

    @Setter
    @Relationship(value = "HAS")
    private List<DomainNode> domains;
}
