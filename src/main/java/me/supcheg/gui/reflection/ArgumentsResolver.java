package me.supcheg.gui.reflection;

import com.google.common.collect.Iterables;
import com.google.gson.internal.Primitives;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ArgumentsResolver {
    private static final Parameter[] EMPTY_PARAMETERS = new Parameter[0];
    private static final Function<Executable, Parameter[]> MAPPING_FUNCTION = executable -> {
        Parameter[] parameters = executable.getParameters();
        return parameters.length == 0 ? EMPTY_PARAMETERS : parameters;
    };
    private static final Map<Executable, Parameter[]> EXECUTABLE_TO_PARAMETERS_CACHE = new HashMap<>();

    private final Collection<AvailableArgument> availableParameters;
    private final Iterable<AvailableArgument> iterable;

    public ArgumentsResolver() {
        this.availableParameters = new ArrayList<>();
        this.iterable = availableParameters;
    }

    private ArgumentsResolver(@NotNull ArgumentsResolver parent) {
        this.availableParameters = new ArrayList<>();
        this.iterable = Iterables.concat(parent.iterable, availableParameters);
    }

    @NotNull
    @Contract("_, _ -> this")
    public ArgumentsResolver append(@NotNull Object first, @NotNull Object @NotNull ... parameters) {
        append(new AvailableArgument(first.getClass(), () -> first, null));
        append(parameters);
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public ArgumentsResolver append(@Nullable Object @NotNull [] parameters) {
        for (Object parameter : parameters) {
            if (parameter != null) {
                append(new AvailableArgument(parameter.getClass(), () -> parameter, null));
            }
        }
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public ArgumentsResolver append(@NotNull Object parameter, @NotNull Predicate<Parameter> predicate) {
        return append(new AvailableArgument(parameter.getClass(), () -> parameter, predicate));
    }

    @NotNull
    @Contract("_, _ -> this")
    public <T> ArgumentsResolver appendLazy(@NotNull Class<T> parameterClazz, @NotNull Supplier<T> parameterSupplier) {
        return append(new AvailableArgument(parameterClazz, parameterSupplier, null));
    }

    @NotNull
    @Contract("_, _, _ -> this")
    public <T> ArgumentsResolver appendLazy(@NotNull Class<T> parameterClazz, @NotNull Supplier<T> parameterSupplier,
                                            @NotNull Predicate<Parameter> predicate) {
        return append(new AvailableArgument(parameterClazz, parameterSupplier, predicate));
    }

    @NotNull
    @Contract("_ -> this")
    public ArgumentsResolver append(@NotNull AvailableArgument availableParameter) {
        availableParameters.add(availableParameter);
        return this;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T findWithType(@NotNull Class<T> clazz) {
        clazz = Primitives.wrap(clazz);

        for (AvailableArgument availableParameter : iterable) {
            Class<?> availableParameterType = availableParameter.getType();

            if (clazz.isAssignableFrom(availableParameterType) || availableParameterType.isAssignableFrom(clazz)) {
                return (T) availableParameter.get();
            }
        }

        return null;
    }

    @NotNull
    @Contract(" -> new")
    public ArgumentsResolver newChild() {
        return new ArgumentsResolver(this);
    }


    @SneakyThrows
    @NotNull
    @Contract("_ -> new")
    public <T> T newInstance(@NotNull Constructor<T> constructor) {
        Parameter[] parameters = EXECUTABLE_TO_PARAMETERS_CACHE.computeIfAbsent(constructor, MAPPING_FUNCTION);

        if (parameters.length == 0) {
            return constructor.newInstance();
        }

        return constructor.newInstance(buildParameters(parameters));
    }

    @SneakyThrows
    @Nullable
    public Object invoke(@NotNull Object instance, @NotNull Method method) {
        Parameter[] parameters = EXECUTABLE_TO_PARAMETERS_CACHE.computeIfAbsent(method, MAPPING_FUNCTION);

        if (parameters.length == 0) {
            return method.invoke(instance);
        }

        return method.invoke(instance, buildParameters(parameters));
    }

    @NotNull
    @Contract("_ -> new")
    private Object @NotNull [] buildParameters(@NotNull Parameter @NotNull [] parameterTypes) {
        Object[] currentParameters = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Parameter parameter = parameterTypes[i];

            for (AvailableArgument availableParameter : iterable) {
                Class<?> parameterType = Primitives.wrap(parameter.getType());
                Class<?> availableParameterType = availableParameter.getType();

                if ((parameterType.isAssignableFrom(availableParameterType) || availableParameterType.isAssignableFrom(parameterType))
                        && availableParameter.test(parameter)) {
                    currentParameters[i] = availableParameter.get();
                    break;
                }

            }
        }

        if (containNull(currentParameters)) {
            throw new IllegalStateException("No available parameters for %s, found only %s from %s"
                    .formatted(Arrays.toString(parameterTypes), Arrays.toString(currentParameters), Iterables.toString(iterable)));
        }

        return currentParameters;
    }

    private static boolean containNull(@Nullable Object @NotNull [] array) {
        for (Object o : array) {
            if (o == null) {
                return true;
            }
        }
        return false;
    }

    @AllArgsConstructor
    public static class AvailableArgument implements Supplier<Object>, Predicate<Parameter> {
        private final Class<?> clazz;
        private final Supplier<?> objectSupplier;
        private final Predicate<Parameter> parameterPredicate;

        @NotNull
        public Class<?> getType() {
            return clazz;
        }

        @Override
        @NotNull
        public Object get() {
            return objectSupplier.get();
        }

        @Override
        public boolean test(@NotNull Parameter parameter) {
            return parameterPredicate == null || parameterPredicate.test(parameter);
        }

        @Override
        public String toString() {
            return "AvailableArgument{"
                    + "clazz=" + clazz
                    + '}';
        }
    }
}
