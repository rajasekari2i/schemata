package com.opsbeach.sharedlib.exception;

import com.opsbeach.sharedlib.dto.UserDto;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * <p>
 *     Base exception handler all over the application
 * </p>
 */
@ControllerAdvice
public class BaseExceptionHandler extends ResponseEntityExceptionHandler {

    private final ExceptionResponseCreator exceptionResponseCreator;
    private final ResponseMessage responseMessage;

    @Autowired
    public BaseExceptionHandler(ExceptionResponseCreator exceptionResponseCreator, ResponseMessage responseMessage) {
        this.exceptionResponseCreator = exceptionResponseCreator;
        this.responseMessage = responseMessage;
    }

    @ExceptionHandler(value = UnAuthorizedException.class)
    protected final ResponseEntity<Object> handleException(final UnAuthorizedException unAuthorizedException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.UNAUTHORIZED, unAuthorizedException.getErrorCode(), unAuthorizedException);
    }

    @ExceptionHandler(value = UserExistException.class)
    protected final ResponseEntity<Object> handleException(final UserExistException userExistException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.CONFLICT, userExistException.getErrorCode(), userExistException);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    protected final ResponseEntity<Object> handleException(final AccessDeniedException accessDeniedException) {
        Optional<UserDto> optionalUserDetails = SecurityUtil.getOptionalUserDetails();
        if (optionalUserDetails.isPresent()) {
            return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.FORBIDDEN, ErrorCode.INSUFFICIENT_PRIVILEGES, accessDeniedException, responseMessage.getErrorMessage(ErrorCode.INSUFFICIENT_PRIVILEGES));
        } else {
            return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED_USER, accessDeniedException);
        }
    }

    @ExceptionHandler(value = CompletableFutureException.class)
    protected final ResponseEntity<Object> handleException(final CompletableFutureException completableFutureException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.PRECONDITION_FAILED, completableFutureException.getErrorCode(), completableFutureException);
    }

    /*@ExceptionHandler(value = ConstraintViolationException.class)
    protected final ResponseEntity<Object> handleException(final ConstraintViolationException   constraintViolationException) {
        var errorMessage = new StringJoiner(Constants.COMMA);
        constraintViolationException.getConstraintViolations().forEach(constraintViolation -> errorMessage.add(constraintViolation.getPropertyPath() + Constants.COLON + constraintViolation.getMessage()));
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.BAD_REQUEST, ErrorCode.ARGUMENT_NOT_VALID_EXCEPTION, constraintViolationException, responseMessage.getErrorMessage(ErrorCode.ARGUMENT_NOT_VALID_EXCEPTION, errorMessage.toString()));
    }*/

    @ExceptionHandler(value = RecordNotFoundException.class)
    protected final ResponseEntity<Object> handleException(final RecordNotFoundException recordNotFoundException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.PRECONDITION_FAILED, recordNotFoundException.getErrorCode(), recordNotFoundException);
    }

    @ExceptionHandler(value = InvalidDataException.class)
    protected final ResponseEntity<Object> handleException(final InvalidDataException invalidDataException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.PRECONDITION_FAILED, invalidDataException.getErrorCode(), invalidDataException);
    }

    @ExceptionHandler(MultipartException.class)
    protected final ResponseEntity<Object> handleException(MultipartException exception, RedirectAttributes redirectAttributes) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.PRECONDITION_FAILED, ErrorCode.INVALID_FILE, exception);
    }

    @ExceptionHandler(value = ClientEmptyException.class)
    protected final ResponseEntity<Object> handleException(final ClientEmptyException tenantEmptyException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.PRECONDITION_FAILED, tenantEmptyException.getErrorCode(), tenantEmptyException);
    }

    @ExceptionHandler(value = Exception.class)
    protected final ResponseEntity<Object> handleException(final Exception exception) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.GENERAL_ERROR, exception);
    }

    @ExceptionHandler(value = BadRequestException.class)
    protected final ResponseEntity<Object> handleException(final BadRequestException badRequestException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, badRequestException);
    }

    @ExceptionHandler(value = PreConditionException.class)
    protected final ResponseEntity<Object> handleException(final PreConditionException preConditionException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.PRECONDITION_FAILED, preConditionException.getErrorCode(), preConditionException);
    }

    @ExceptionHandler(value = EncodeException.class)
    protected final ResponseEntity<Object> handleException(final EncodeException encodeException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.NOT_ACCEPTABLE, encodeException.getErrorCode(), encodeException);
    }

    /*@Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException methodArgumentNotValidException, HttpHeaders headers, HttpStatus status, WebRequest request) {
        var errorMessage = new StringJoiner(Constants.COMMA);
        if (methodArgumentNotValidException.getBindingResult().getFieldErrors().isEmpty()) {
            if (!methodArgumentNotValidException.getBindingResult().getAllErrors().isEmpty()) {
                ObjectError error = methodArgumentNotValidException.getBindingResult().getAllErrors().get(0);
                return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.PRECONDITION_FAILED, ErrorCode.ARGUMENT_NOT_VALID_EXCEPTION, methodArgumentNotValidException, responseMessage.getErrorMessage(ErrorCode.ARGUMENT_NOT_VALID_EXCEPTION, error.getDefaultMessage()));
            }
        } else {
            for (FieldError error : methodArgumentNotValidException.getBindingResult().getFieldErrors()) {
                errorMessage.add(error.getField() + Constants.COLON + error.getDefaultMessage());
            }
        }
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.BAD_REQUEST, ErrorCode.ARGUMENT_NOT_VALID_EXCEPTION, methodArgumentNotValidException, responseMessage.getErrorMessage(ErrorCode.ARGUMENT_NOT_VALID_EXCEPTION, errorMessage.toString()));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException httpMessageNotReadableException, HttpHeaders headers, HttpStatus status, WebRequest request) {
        var enumValidationServiceException = (EnumValidationException) httpMessageNotReadableException.getMostSpecificCause();
        String errorMessage;
        if (enumValidationServiceException.getEnumValue().isEmpty()) {
            errorMessage = enumValidationServiceException.getEnumName() + " must not be empty";
        } else {
            errorMessage = enumValidationServiceException.getEnumValue() + " is an invalid " + enumValidationServiceException.getEnumName();
        }
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.BAD_REQUEST, ErrorCode.ARGUMENT_NOT_VALID_EXCEPTION, httpMessageNotReadableException, responseMessage.getErrorMessage(ErrorCode.ARGUMENT_NOT_VALID_EXCEPTION, errorMessage));
    }*/

    /*@ExceptionHandler(value = RecordMismatchException.class)
    protected final ResponseEntity<Object> handleException(final RecordMismatchException recordMismatchException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.CONFLICT, recordMismatchException.getErrorCode(), recordMismatchException);
    }*/

    /*@ExceptionHandler(value = InvalidFileException.class)
    protected final ResponseEntity<Object> handleException(final InvalidFileException invalidFileException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.PRECONDITION_FAILED, invalidFileException.getErrorCode(), invalidFileException);
    }*/

    /*@ExceptionHandler(value = IntegrityViolationException.class)
    protected final ResponseEntity<Object> handleException(final IntegrityViolationException integrityViolationException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.PRECONDITION_FAILED, integrityViolationException.getErrorCode(), integrityViolationException);
    }*/

    /*@@ExceptionHandler(value = InvalidLoginModeException.class)
    protected final ResponseEntity<Object> handleException(final InvalidLoginModeException invalidLoginModeException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.PRECONDITION_FAILED, invalidLoginModeException.getErrorCode(), invalidLoginModeException);
    }

    ExceptionHandler(value = AlreadyLoggedOutException.class)
    protected final ResponseEntity<Object> handleException(final AlreadyLoggedOutException alreadyLoggedOutException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.PERMANENT_REDIRECT, alreadyLoggedOutException.getErrorCode(), alreadyLoggedOutException);
    }

    @ExceptionHandler(value = MissingCategoryBudgetException.class)
    protected final ResponseEntity<Object> handleException(final MissingCategoryBudgetException missingCategoryBudgetException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.PRECONDITION_FAILED, missingCategoryBudgetException.getErrorCode(), missingCategoryBudgetException);
    }

    @ExceptionHandler(value = TenantExistException.class)
    protected final ResponseEntity<Object> handleException(final TenantExistException tenantExistException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.CONFLICT, tenantExistException.getErrorCode(), tenantExistException);
    }

    @ExceptionHandler(value = ConnectionException.class)
    protected final ResponseEntity<Object> handleException(final ConnectionException connectionException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.BAD_REQUEST, connectionException.getErrorCode(), connectionException);
    }*/

    /*@ExceptionHandler(value = UnprocessableEntity.class)
    protected final ResponseEntity<Object> handleException(final UnProcessableEntity unProcessableEntity) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY, unProcessableEntity.getErrorCode(), unProcessableEntity);
    }*/

    /*@ExceptionHandler(value = NotImplementedException.class)
    protected final ResponseEntity<Object> handleException(final NotImplementedException functionalityNotImplementedException) {
        return exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.NOT_IMPLEMENTED, ErrorCode.GENERAL_ERROR, functionalityNotImplementedException);
    }*/

}