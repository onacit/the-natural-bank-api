package com.wemakeprice.edu.whomakeprice.web.bind;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static javax.validation.Validation.byDefaultProvider;

/**
 * A utility class for Bean-Validation.
 *
 * @author Jin Kwon &lt;onacit_at_wemakeprice.com&gt;
 * @deprecated Something's wrong if you need to use this class.
 */
@Deprecated
public class BeanValidationUtils {

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An instance of (thread-safe) {@link ValidatorFactory}.
     */
    // https://stackoverflow.com/a/54750045/330457
    public static final ValidatorFactory VALIDATOR_FACTORY
            = byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory();

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns an instance of {@link Validator}.
     *
     * @return an instance of {@link Validator}
     * @see #VALIDATOR_FACTORY
     * @see ValidatorFactory#getValidator()
     */
    public static Validator getValidator() {
        return VALIDATOR_FACTORY.getValidator();
    }

    /**
     * Validates specified bean.
     *
     * @param bean   the bean to validate.
     * @param groups the group of list of groups targeted for validation.
     * @param <T>    bean type parameter
     * @return a set of {@link ConstraintViolation}; may be empty.
     * @see #getValidator()
     * @see Validator#validate(Object, Class[])
     */
    public static <T> Set<ConstraintViolation<T>> validate(final T bean, final Class<?>... groups) {
        return getValidator().validate(requireNonNull(bean, "bean is null"), groups);
    }

    /**
     * Validates specified bean.
     *
     * @param bean   the bean to validate.
     * @param groups the group of list of groups targeted for validation.
     * @param <T>    bean type parameter
     * @return given bean.
     * @throws ConstraintViolationException if specified bean is not valid.
     */
    public static <T> T requireValid(final T bean, final Class<?>... groups) throws ConstraintViolationException {
        final Set<ConstraintViolation<T>> violations = validate(requireNonNull(bean, "bean is null"), groups);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return bean;
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance.
     */
    BeanValidationUtils() {
        super();
    }
}
