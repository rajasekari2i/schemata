package com.opsbeach.connect.schemata.validate;

import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.schemata.entity.Field;


public interface FieldTrigger extends Predicate<Field> {

  FieldTrigger isDescriptionEmpty = field -> StringUtils.isBlank(field.getDescription());
  // FieldTrigger isClassificationLevelEmpty = field -> ObjectUtils.isEmpty(field.getIsClassified());
}
