package net.imglib2.algorithm.labeling.metrics;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SegmentationHelper {

    public static List<Set<String>> getLabelingSet(String[] labels){
        List< Set<String> > labelings = new ArrayList<>();

        labelings.add(new HashSet<>());

        // Add label Sets
        for(String entries: labels){
            Set<String> subLabelSet = new HashSet<>();
            for(String entry: entries.split(",")){
                subLabelSet.add(entry);
            }
            labelings.add(subLabelSet);
        }

        return labelings;
    }

    public static void paintRectangle(Img<IntType> img, long[] interval, int value){
        IntervalView<IntType> intView = Views.interval(img, Intervals.createMinMax(interval));
        Cursor<IntType> cur = intView.cursor();
        while(cur.hasNext()){
            cur.next().set(value);
        }
    }

    public static void paintRectangle(Img<IntType> img, int min_x, int min_y, int max_x, int max_y, int value){
        long[] interval = {min_x, min_y, max_x, max_y};
        IntervalView<IntType> intView = Views.interval(img, Intervals.createMinMax(interval));
        Cursor<IntType> cur = intView.cursor();
        while(cur.hasNext()){
            cur.next().set(value);
        }
    }
}
