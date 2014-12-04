package com.computation;

import com.computation.algo.GiftWrapping;
import com.computation.algo.GrahamScan;
import com.computation.algo.GrahamScanParallel;
import com.computation.algo.QuickHull;
import com.computation.common.Point2DCloud;
import com.computation.common.Utils;
import com.computation.experimental.OptimalThreadCountFinder;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class Main {

    public final static int POINTS = 100;
    public final static boolean DEBUG = true;

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException,
            InstantiationException, IllegalAccessException, InvocationTargetException, InterruptedException {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        OptimalThreadCountFinder.DPI_SCALING =
                Point2DCloud.DPI_SCALING = 2; /* Set display scaling */

        final Point2DCloud point2DCloud = new Point2DCloud(POINTS /* points */,
                Utils.WIDTH = 700,
                Utils.HEIGHT = 700, DEBUG);

        point2DCloud.addTopButton("GiftWrapping", new Runnable() {
            @Override
            public void run() {
                int animTime = getAnimationTime();
                new GiftWrapping(point2DCloud, getThreadCount(), animTime != 0, animTime);
            }
        });

        point2DCloud.addTopButton("GrahamScanParallel", new Runnable() {
            @Override
            public void run() {
                int animTime = getAnimationTime();
                new GrahamScanParallel(point2DCloud, getThreadCount(), animTime != 0, animTime);
            }
        });

        point2DCloud.addTopButton("QuickHull", new Runnable() {
            @Override
            public void run() {
                int animTime = getAnimationTime();
                new QuickHull(point2DCloud, getThreadCount(), animTime != 0, animTime);
            }
        });

        point2DCloud.addTopButton("GrahamScan", new Runnable() {
            @Override
            public void run() {
                int animTime = getAnimationTime();
                new GrahamScan(point2DCloud, getThreadCount(), animTime != 0, animTime);
            }
        });

        point2DCloud.show();
    }

    public static int getThreadCount() {
        try {
            return Integer.parseInt(JOptionPane.showInputDialog(null, "Number of threads (default: 4)"));
        } catch (Exception e) {
            return 4;
        }
    }

    public static int getAnimationTime() {
        try {
            return Integer.parseInt(JOptionPane.showInputDialog(null, "Animation time (default: 100)"));
        } catch (Exception e) {
            return 100;
        }
    }
}