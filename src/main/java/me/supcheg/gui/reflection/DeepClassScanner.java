package me.supcheg.gui.reflection;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DeepClassScanner<C> {
    private static final Map<Class<?>, DeepClassScanner<?>> CLAZZ_TO_SCANNER = new HashMap<>();

    private final Class<C> clazz;
    private DeepClassScanner<?> enclosingClazz;
    private List<DeepClassScanner<?>> sealedClazzes;
    private DeepClassScanner<?> superClazz;

    private List<Constructor<C>> constructors;

    private List<Annotation> allAnnotations;
    private List<Annotation> currentClazzAnnotations;
    private Map<Class<? extends Annotation>, List<Annotation>> annotationType2instances;

    private List<Field> allFields;
    private List<Field> currentClazzFields;

    private List<Method> allMethods;
    private List<Method> currentClazzMethods;

    private DeepClassScanner(@NotNull Class<C> clazz) {
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <C> DeepClassScanner<C> of(@NotNull Class<C> ofClazz) {
        return (DeepClassScanner<C>) CLAZZ_TO_SCANNER.computeIfAbsent(ofClazz, DeepClassScanner::new);
    }

    @Nullable
    @Contract("null -> null; !null -> !null")
    public static <C> DeepClassScanner<C> ofNullable(@Nullable Class<C> ofClazz) {
        return ofClazz == null ? null : of(ofClazz);
    }

    @NotNull
    @Unmodifiable
    public List<Annotation> getAllAnnotations() {
        if (allAnnotations == null) {
            var superClazz = getSuper();

            if (superClazz == null) {
                allAnnotations = getCurrentAnnotations();
            } else {
                allAnnotations = new ArrayList<>(getCurrentAnnotations());
                allAnnotations.addAll(superClazz.getAllAnnotations());
                allAnnotations = List.copyOf(allAnnotations);
            }

        }

        return allAnnotations;
    }

    @NotNull
    @Unmodifiable
    public List<Annotation> getCurrentAnnotations() {
        if (currentClazzAnnotations == null) {
            currentClazzAnnotations = List.of(clazz.getAnnotations());
        }
        return currentClazzAnnotations;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Unmodifiable
    public List<Constructor<C>> getConstructors() {
        if (constructors == null) {
            constructors = List.of((Constructor<C>[]) clazz.getDeclaredConstructors());
        }
        return constructors;
    }

    public boolean isAnnotationPresent(@NotNull Class<? extends Annotation> annotationClazz) {
        return !findAnnotations(annotationClazz).isEmpty();
    }

    public boolean isAnnotationInCurrent(@NotNull Class<? extends Annotation> annotationClazz) {
        return !findAnnotationsInCurrent(annotationClazz).isEmpty();
    }

    @Nullable
    public <A extends Annotation> A findAnnotation(@NotNull Class<A> annotationClazz) {
        List<A> annotations = findAnnotations(annotationClazz);
        return annotations.isEmpty() ? null : annotations.get(0);
    }

    @Nullable
    public <A extends Annotation> A findAnnotationInCurrent(@NotNull Class<A> annotationClazz) {
        List<A> annotations = findAnnotationsInCurrent(annotationClazz);
        return annotations.isEmpty() ? null : annotations.get(0);
    }

    @Unmodifiable
    @NotNull
    public <A extends Annotation> List<A> findAnnotations(@NotNull Class<A> annotationClazz) {
        var superClazz = getSuper();

        if (superClazz == null) {
            return findAnnotationsInCurrent(annotationClazz);
        } else {
            List<A> annotations = new ArrayList<>(findAnnotationsInCurrent(annotationClazz));
            annotations.addAll(superClazz.findAnnotations(annotationClazz));

            return Collections.unmodifiableList(annotations);
        }
    }

    @UnmodifiableView
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    public <A extends Annotation> List<A> findAnnotationsInCurrent(@NotNull Class<A> annotationClazz) {
        List<Annotation> annotations;
        if (annotationType2instances == null) {
            annotationType2instances = new HashMap<>(8);
            annotations = null;
        } else {
            annotations = annotationType2instances.get(annotationClazz);
        }

        if (annotations == null) {
            annotations = List.of(clazz.getAnnotationsByType(annotationClazz));
            annotationType2instances.put(annotationClazz, annotations);
        }

        return (List) annotations;
    }

    @NotNull
    public Stream<Field> getFieldsWith(@NotNull Class<? extends Annotation> annotationClazz) {
        return getAllFields().stream().filter(method -> method.isAnnotationPresent(annotationClazz));
    }

    @NotNull
    @Unmodifiable
    public List<Field> getAllFields() {
        if (allFields == null) {
            var superClazz = getSuper();

            if (superClazz == null) {
                allFields = getCurrentFields();
            } else {
                allFields = new ArrayList<>(getCurrentFields());
                allFields.addAll(superClazz.getAllFields());
                allFields = List.copyOf(allFields);
            }

        }

        return allFields;
    }

    @NotNull
    @Unmodifiable
    public List<Field> getCurrentFields() {
        if (currentClazzFields == null) {
            var fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
            fields.removeIf(field -> field.getDeclaringClass() == Object.class);

            currentClazzFields = List.copyOf(fields);
        }
        return currentClazzFields;
    }

    @NotNull
    public Stream<Method> getMethodsWith(@NotNull Class<? extends Annotation> annotationClazz) {
        return getAllMethods().stream().filter(method -> method.isAnnotationPresent(annotationClazz));
    }

    @NotNull
    @Unmodifiable
    public List<Method> getAllMethods() {
        if (allMethods == null) {
            allMethods = List.of(clazz.getMethods());
        }
        return allMethods;
    }

    @NotNull
    @Unmodifiable
    public List<Method> getCurrentMethods() {
        if (currentClazzMethods == null) {
            currentClazzMethods = List.of(clazz.getDeclaredMethods());
        }
        return currentClazzMethods;
    }

    @NotNull
    public Class<C> getJavaClass() {
        return clazz;
    }

    @Nullable
    public DeepClassScanner<?> getEnclosing() {
        if (enclosingClazz == null) {
            this.enclosingClazz = ofNullable(clazz.getEnclosingClass());
        }
        return enclosingClazz;
    }

    @Nullable
    public DeepClassScanner<?> getSuper() {
        if (superClazz == null) {
            Class<?> superClazz = clazz.getSuperclass();
            if (superClazz != Object.class) {
                this.superClazz = of(superClazz);
            }
        }
        return superClazz;
    }

    @NotNull
    @Unmodifiable
    public List<DeepClassScanner<?>> getSealed() {
        if (sealedClazzes == null) {
            Class<?>[] sealed = clazz.getDeclaredClasses();
            if (sealed.length == 0) {
                sealedClazzes = Collections.emptyList();
            } else {
                sealedClazzes = new ArrayList<>(sealed.length);
                for (Class<?> declaredClazz : sealed) {
                    sealedClazzes.add(of(declaredClazz));
                }
                sealedClazzes = List.copyOf(sealedClazzes);
            }
        }
        return sealedClazzes;
    }

    @Nullable
    public DeepClassScanner<?> getFirstSealed() {
        return getSealed().isEmpty() ? null : getSealed().get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof DeepClassScanner<?> that)) {
            return false;
        }

        return clazz == that.clazz;
    }

    @Override
    public int hashCode() {
        return clazz.hashCode() + 1;
    }

    @Override
    public String toString() {
        return "DeepClassScanner{"
                + "clazz=" + clazz
                + '}';
    }
}
