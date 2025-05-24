package com.opsbeach.connect.schemata.validate;

import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.enums.SchemaType;


public interface SchemaTrigger extends Predicate<Table> {

  SchemaTrigger isDescriptionEmpty = schema -> StringUtils.isBlank(schema.getDescription());

  SchemaTrigger isOwnerEmpty = schema -> StringUtils.isBlank(schema.getOwner());

  SchemaTrigger isDomainEmpty = schema -> StringUtils.isBlank(schema.getDomain());

  // SchemaTrigger isInValidType = schema -> SchemaType.UNKNOWN.name().equalsIgnoreCase(schema.getType());

  // SchemaTrigger isPrimaryKeyNotExistsForEntity = schema -> {
  //   if (!schema.getType().equalsIgnoreCase(SchemaType.ENTITY.name())) {
  //     return false;
  //   }
  //   return schema.getFields().stream().filter(Field::getIsPrimaryKey).count() != 1;
  // };
}
