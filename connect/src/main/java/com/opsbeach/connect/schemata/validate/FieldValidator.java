package com.opsbeach.connect.schemata.validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.opsbeach.connect.schemata.entity.Field;

// import static com.opsbeach.connect.schemata.validate.FieldTrigger.isClassificationLevelEmpty;
import static com.opsbeach.connect.schemata.validate.FieldTrigger.isDescriptionEmpty;

@Component
public class FieldValidator implements Function<Field, Result>, Validator<Field> {
  @Override
  public Result apply(Field field) {

    List<String> errors = new ArrayList<>();
    for (Map.Entry<Rules, FieldTrigger> ruleTrigger : fieldValidatorMap().entrySet()) {
      var result = test(ruleTrigger.getKey(), ruleTrigger.getValue(), field);
      result.ifPresent(errors::add);
    }
    return errors.size() == 0 ? new Result(field.getId(), field.getName(), Status.SUCCESS, errors) 
                              : new Result(field.getId(), field.getName(), Status.ERROR, errors);
  }

  private Map<Rules, FieldTrigger> fieldValidatorMap() {
    return Map.of(Rules.FIELD_DESCRIPTION_EMPTY, isDescriptionEmpty);
  }
}
