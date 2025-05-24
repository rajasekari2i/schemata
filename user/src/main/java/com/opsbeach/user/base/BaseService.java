package com.opsbeach.user.base;

import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.user.base.specification.IdSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * Base service which application service extends.
 * </p>
 */
public abstract class BaseService<M extends BaseModel, D extends BaseDto> {

    BaseRepository<M> baseRepository;
    IdSpecifications<M> idSpecifications;
    BaseMapper<M, D> baseMapper;
    Class<M> modelType;
    @Autowired
    private ResponseMessage responseMessage;

    protected BaseService(BaseRepository<M> baseRepository, BaseMapper<M, D> baseMapper,
                          IdSpecifications<M> idSpecifications) {
        this.baseRepository = baseRepository;
        this.idSpecifications = idSpecifications;
        this.baseMapper = baseMapper;
        this.modelType = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public void validateAdd(D incomingDto) {
    }

    public final D add(D incomingDto) {
        validateAdd(incomingDto);
        var incomingModel = baseMapper.dtoToDomain(incomingDto);
        var savedModel = addModel(incomingModel);
        return baseMapper.domainToDto(savedModel);
    }

    public final M addModel(M incomingModel) {
        return baseRepository.save(incomingModel);
    }

    /**
     * The extending services shall implement their logic to patch the
     * `toUpdateModel` with the `incomingModel`
     * <p>
     * This method should be abstract in which case all extending service classes
     * must provide an implementation.
     *
     * @param incomingModel - Model with updated values.
     * @param toUpdateModel - Model returned by Database which needs to be updated.
     */
    public void doPatch(M incomingModel, M toUpdateModel) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public void validatePatch(D incomingDto) {
    }

    public void patch(D incomingDto) {
        Optional<M> toUpdate = baseRepository.findOne(idSpecifications.findById(incomingDto.getId()));
        if (toUpdate.isEmpty()) {
            throw new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND));
        }
        validatePatch(incomingDto);
        var incomingModel = baseMapper.dtoToDomain(incomingDto);
        var toUpdateModel = toUpdate.get();
        patchModel(incomingModel, toUpdateModel);
    }

    public void patchModel(M incomingModel, M toUpdateModel) {
        doPatch(incomingModel, toUpdateModel);
        baseRepository.saveAndFlush(toUpdateModel);
    }

    public D findById(Long id) {
        Optional<M> entity = findModelById(id);
        if (entity.isEmpty()) {
            throw new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND,
                    responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND,
                            String.format("There is no %s with id %d", modelType.getSimpleName(), id)));
        }
        return baseMapper.domainToDto(entity.get());
    }

    public D findOne(Specification<M> specs) {
        Optional<M> entity = findOneModel(specs);
        if (entity.isEmpty()) {
            throw new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND));
        }
        return baseMapper.domainToDto(entity.get());
    }

    public Optional<M> findOneModel(Specification<M> specs) {
        return baseRepository.findOne(specs);
    }


    public Optional<M> findModelById(Long id) {
        return baseRepository.findById(id);
    }

    public D findByClientName(String clientName) {
        Specification<M> baseSpecification = idSpecifications.findByName(clientName);
        Optional<M> entity = baseRepository.findOne(baseSpecification);
        if (entity.isEmpty()) {
            throw new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND,
                    responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND,
                            String.format(". There is no %s with %s", modelType.getSimpleName(), clientName)));
        }
        return baseMapper.domainToDto(entity.get());
    }

    public List<D> findAll() {
        Specification<M> mSpecification = idSpecifications.notDeleted();
        List<M> models = findAllModels(mSpecification);
        return models.stream().map(m -> baseMapper.domainToDto(m)).collect(Collectors.toList());
    }

    public List<M> findAllModels(Specification<M> specifications) {
        return baseRepository.findAll(specifications);
    }

    public Optional<D> findOneOrReturnEmpty(Specification<M> specs) {
        Optional<M> entity = findOneModel(specs);
        if (entity.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(baseMapper.domainToDto(entity.get()));
    }
}
