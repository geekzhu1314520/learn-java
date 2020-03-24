package com.watermelon.commonmistakes.java8;

import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * 使用Stream简化集合操作
 */
public class CoolStreamTest {

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

}
