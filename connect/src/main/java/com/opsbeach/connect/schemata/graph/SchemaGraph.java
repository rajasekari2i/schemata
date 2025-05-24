package com.opsbeach.connect.schemata.graph;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.util.SupplierUtil;

import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.sharedlib.exception.SchemaNotFoundException;
import com.opsbeach.sharedlib.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SchemaGraph {

  private final DirectedWeightedMultigraph<Table, WeightedSchemaEdge> graph =
      new DirectedWeightedMultigraph<>(SupplierUtil.createSupplier(Table.class),
          SupplierUtil.createSupplier(WeightedSchemaEdge.class));

  private final List<Table> schemaList;
  Map<String, Table> schemaMap;
  private final PageRank<Table, WeightedSchemaEdge> pageRank;

  static final Map<String, Schema.Type> PRIMITIVES = new HashMap<>();

    static {
        PRIMITIVES.put("string", Schema.Type.STRING);
        PRIMITIVES.put("bytes", Schema.Type.BYTES);
        PRIMITIVES.put("int", Schema.Type.INT);
        PRIMITIVES.put("long", Schema.Type.LONG);
        PRIMITIVES.put("float", Schema.Type.FLOAT);
        PRIMITIVES.put("double", Schema.Type.DOUBLE);
        PRIMITIVES.put("boolean", Schema.Type.BOOLEAN);
        PRIMITIVES.put("null", Schema.Type.NULL);
    }

  public SchemaGraph(List<Table> schemaList) {
    this.schemaList = schemaList;
    this.schemaMap = buildGraph();
    this.buildEdge();
    pageRank = new PageRank<>(graph);
  }

  private Map<String, Table> buildGraph() {
    Map<String, Table> map = new HashMap<>();
    for (Table schema : schemaList) {
      var fullName = Objects.isNull(schema.getNameSpace()) ? schema.getName() 
                    : StringUtil.constructStringEmptySeparator(schema.getNameSpace(),".",schema.getName());
      map.put(fullName, schema);
      this.addVertex(schema);
    }
    return map;
  }

  private void buildEdge()
      throws SchemaNotFoundException {
    for (Table schema : this.schemaList) {
      log.info("processing schema - "+schema.getName());
      buildEdge(schema.getFields(), schema);
    }
  }

  private void buildEdge(List<Field> fields, Table schema) {
    for (Field field : fields) {
      log.info("processing field - "+field.getName()+" of schema - "+schema.getName());
      if (Boolean.FALSE.equals(field.getIsPrimitiveType()) && Boolean.FALSE.equals(field.getIsDeleted())) {
        var dataType = findDataType(field, schema); 
        if (Boolean.FALSE.equals(isPrimitiveType(dataType))) {
          findVertex(dataType).ifPresentOrElse(
            value -> this.addEdge(new WeightedSchemaEdge(schema, value, field)), () -> {
              throw new SchemaNotFoundException("DataType " + dataType + " Not found in the graph");
            });
        }
      }
    }
  }

  // check the DATA TYPE belongs to which COMPLEX TYPE
  private String findDataType(Field field, Table schema) {
    var dataType = field.getDataType();
    if (dataType.equalsIgnoreCase(Schema.Type.ARRAY.name())) { 
      dataType = findNestedDataType(field, schema, field.getItems());
    }
    else if (dataType.equalsIgnoreCase(Schema.Type.ENUM.name())) { dataType = Schema.Type.STRING.name(); }
    else if (dataType.equalsIgnoreCase(Schema.Type.MAP.name())) { 
      dataType = findNestedDataType(field, schema, field.getValues());
    }
    else if (dataType.equalsIgnoreCase(Schema.Type.UNION.name())) { 
      // In UNION we may have MORE THAN ONE complex dataTypes so the need to build edge to these types.
      buildEdge(field.getUnionTypes(), schema);
      dataType = Schema.Type.STRING.name(); 
    }
    else if (dataType.equalsIgnoreCase(Schema.Type.FIXED.name())) { dataType = Schema.Type.STRING.name(); }
    return dataType;
  }

  private String findNestedDataType(Field field, Table schema, String dataType) {
    // check nested field is ARRAY
    if (dataType.equalsIgnoreCase(Schema.Type.ARRAY.name())) { 
      dataType = findDataType(field.getArrayField(), schema);
    }
    // check nested field is MAP
    else if (dataType.equalsIgnoreCase(Schema.Type.MAP.name())) {
      dataType = findDataType(field.getMapField(), schema);
    }
    // check nested field is UNION
    else if (dataType.equalsIgnoreCase(Schema.Type.UNION.name())) {
      buildEdge(field.getUnionTypes(), schema);
      dataType = Schema.Type.STRING.name(); 
    }
    else if (dataType.equalsIgnoreCase(Schema.Type.FIXED.name())) { dataType = Schema.Type.STRING.name(); }
    else if (dataType.equalsIgnoreCase(Schema.Type.ENUM.name())) { dataType = Schema.Type.STRING.name(); }
    return dataType;
  }

  private boolean isPrimitiveType(String name) {
    return PRIMITIVES.containsKey(name.toLowerCase());
  }

  private void addVertex(Table schema) {
    graph.addVertex(schema);
  }

  private void addEdge(WeightedSchemaEdge edge) {
    if (edge == null) {
      throw new IllegalArgumentException("Edge can't be null");
    } 
    try {
      graph.addEdge(edge.getSource(), edge.getTarget(), edge);
    } catch (Exception e) {
      log.info("Source({}) and Destination({}) are same {}", edge.getSource().getName(), edge.getTarget().getName(), e.getMessage());
    }
  }

  public Set<WeightedSchemaEdge> incomingEdgesOf(String vertex)
      throws SchemaNotFoundException {
    return graph.incomingEdgesOf(getSchema(vertex));
  }

  public Set<Table> incomingVertexOf(String vertex) {
    Set<Table> incomingSchemaSet = new HashSet<>();
    incomingEdgesOf(vertex).forEach(e -> incomingSchemaSet.add(e.getSource()));
    return incomingSchemaSet;
  }

  public Set<WeightedSchemaEdge> outgoingEdgesOf(String vertex)
      throws SchemaNotFoundException {
    return graph.outgoingEdgesOf(getSchema(vertex));
  }

  public Set<Table> outgoingVertexOf(String vertex) {
    Set<Table> outgoingSchemaSet = new HashSet<>();
    outgoingEdgesOf(vertex).forEach(e -> outgoingSchemaSet.add(e.getTarget()));
    return outgoingSchemaSet;
  }

  public Set<Table> outgoingEntityVertexOf(String vertex) {
    return outgoingVertexOf(vertex).stream().filter(f -> "ENTITY".equalsIgnoreCase(f.getSchemaType().name()))
        .collect(Collectors.toSet());
  }

  public Set<Table> getAllEntityVertex() {
    return graph.vertexSet().stream().filter(f -> "ENTITY".equalsIgnoreCase(f.getSchemaType().name())).collect(Collectors.toSet());
  }

  public Double getVertexPageRankScore(String vertex) {
    return pageRank.getVertexScore(getSchema(vertex));
  }

  public Double getSchemataScore(String vertex) {
    var schema = getSchema(vertex);
    double score = switch (schema.getSchemaType().name().toUpperCase()) {
      case "ENTITY" -> computeEntityScore(vertex);
      case "EVENT" -> computeEventScore(vertex, schema.getEventType().name());
      default -> 0.0;
    };
    return roundUp(score);
  }

  private double computeEntityScore(String vertex) {
    double totalEdges = graph.edgeSet().size();
    double referenceEdges = referenceEdges(vertex).size();
    return totalEdges == 0 ? 0 : 1 - ((totalEdges - referenceEdges) / totalEdges);
  }

  public Set<WeightedSchemaEdge> referenceEdges(String vertex) {
    return SetUtils.union(incomingEdgesOf(vertex), outgoingEdgesOf(vertex));
  }

  private double computeEventScore(String vertex, String eventType) {
    double score = switch (eventType) {
      case "LIFECYCLE" -> outgoingEntityVertexOf(vertex).size() > 0 ? 1.0 : 0.0;
      case "ACTIVITY", "AGGREGATED" -> computeNonLifecycleScore(vertex);
      default -> 0.0;
    };
    return score;
  }

  private double computeNonLifecycleScore(String vertex) {
    Set<Table> referenceVertex =
        outgoingEntityVertexOf(vertex).stream().map(v -> outgoingEntityVertexOf(v.getNameSpace()+"."+v.getName())).flatMap(Collection::stream)
            .collect(Collectors.toSet());
    Set<Table> outgoingVertex = outgoingEntityVertexOf(vertex);
    double vertexCount = SetUtils.union(referenceVertex, outgoingVertex).size();
    double totalVertex = getAllEntityVertex().size();
    return 1 - ((totalVertex - vertexCount) / totalVertex);
  }

  public Table getSchema(String vertex)
      throws SchemaNotFoundException {
    return findVertex(vertex).orElseThrow(
        () -> new SchemaNotFoundException("Vertex " + vertex + " Not found in the graph"));
  }

  public Optional<Table> findVertex(String vertex) {
    if (StringUtils.isBlank(vertex)) {
      return Optional.empty();
    }
    if (this.schemaMap.containsKey(vertex)) {
      return Optional.of(this.schemaMap.get(vertex));
    }
    return Optional.empty();
  }

  private double roundUp(double value) {
    return new BigDecimal(value, new MathContext(3)).doubleValue();
  }
}
