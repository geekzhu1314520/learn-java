package com.watermelon.commonmistakes.java8;

import org.junit.Test;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * 使用Stream简化集合操作
 */
public class CoolStreamTest {

    private static final Map<Long, Product> cache = new ConcurrentHashMap<>();

    private static double calc(List<Integer> nums) {
        List<Point2D> point2DList = new ArrayList<>();
        for (Integer num : nums) {
            point2DList.add(new Point2D.Double((double) num % 3, (double) num / 3));
        }

        double total = 0.0;
        int count = 0;
        for (Point2D point2D : point2DList) {
            if (point2D.getY() > 1) {
                double distance = point2D.distance(0, 0);
                total += distance;
                count++;
            }
        }
        return count > 0 ? total / count : 0;
    }

    @Test
    public void stream() {
        List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        double average = calc(nums);
        double streamResult = nums.stream()
                .map(i -> new Point2D.Double((double) i % 3, (double) i / 3))
                .filter(point -> point.getY() > 1)
                .mapToDouble(point -> point.distance(0, 0))
                .average()
                .orElse(0);
        assertThat(average, is(streamResult));
    }

    @Test
    public void coolCache() {
        getProductAndCacheCool(1L);
        getProductAndCacheCool(100L);

        System.out.println(cache);
        assertThat(cache.size(), is(1));
        assertTrue(cache.containsKey(1L));
    }

    @Test
    public void nocoolCache() {
        getProductAndCache(1L);
        getProductAndCache(100L);

        System.out.println(cache);
        assertThat(cache.size(), is(1));
        assertTrue(cache.containsKey(1L));
    }


    private Product getProductAndCache(Long id) {
        Product product = cache.get(id);
        if (product == null) {
            for (Product p : Product.getData()) {
                if (p.getId().equals(id)) {
                    product = p;
                    cache.put(id, product);
                    break;
                }
            }
        }
        return product;
    }

    private Product getProductAndCacheCool(Long id) {
        return cache.computeIfAbsent(id, i ->
                Product.getData().stream()
                        .filter(p -> p.getId().equals(id))
                        .findFirst()
                        .orElse(null));
    }

    private static Pattern CLASS_PATTERN = Pattern.compile("public class");

    @Test
    public void filesExample() throws IOException {
        try (Stream<Path> pathStream = Files.walk(Paths.get("."))) {
            pathStream.filter(Files::isRegularFile)
                    .filter(FileSystems.getDefault().getPathMatcher("glob:**/*.java")::matches)
                    .flatMap(ThrowingFunction.unchecked(path ->
                            Files.readAllLines(path).stream()
                                    .filter(line -> CLASS_PATTERN.matcher(line).find())
                                    .map(line -> path.getFileName() + " >> " + line)))
                    .forEach(System.out::println);
        }
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Throwable> {
        static <T, R, E extends Throwable> Function<T, R> unchecked(ThrowingFunction<T, R, E> f) {
            return t -> {
                try {
                    return f.apply(t);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            };
        }

        R apply(T t) throws E;
    }


}
