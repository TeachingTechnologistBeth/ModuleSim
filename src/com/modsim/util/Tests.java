package com.modsim.util;

import java.awt.geom.AffineTransform;

import com.modsim.modules.BaseModule;
import com.modsim.modules.parts.*;
import com.modsim.modules.ports.Input;
import com.modsim.modules.ports.Output;

public class Tests {

    private static boolean result = true;

    /**
     * Runs the test suite
     * @param args
     */
    public static void main(String[] args) {
        // Instantiation and unit testing
        System.out.println("Testing instantiation of com.modsim.modules:");

        for (BaseModule.AvailableModules am : BaseModule.AvailableModules.values()) {
            System.out.println("Instantiating "+am.name()+": ");
            BaseModule m = (BaseModule) am.getSrcModule().createNew();

            System.out.print("  Test pos validity - ");
            testinstance(m.pos, Vec2.class);
            System.out.println();

            System.out.print("  Test transforms - ");
            testinstance(m.toView, AffineTransform.class);
            testinstance(m.toWorld, AffineTransform.class);
            System.out.println();

            System.out.println("  Test ports - ");
            for (Port p : m.inputs) {
                System.out.print("    " + p.text + ": ");
                testinstance(p, Input.class);
                testval(p.owner, m);
                testnull(p.getVal());
                p.setVal(new BinData(1));
                p.setVal(new BinData(0));
                testval(p.wasUpdated(), true);
                System.out.println();
            }
            for (Port p : m.outputs) {
                System.out.print("    " + p.text + ": ");
                testinstance(p, Output.class);
                testval(p.owner, m);
                testnull(p.getVal());
                p.setVal(new BinData(1));
                p.setVal(new BinData(0, 0, 0, 0));
                testval(p.wasUpdated(), true);
                System.out.println();
            }

            System.out.print("  Unit test: ");
            m.test();

            System.out.println();
        }



        if (!result) {
            System.err.println("Tests failed");
        }
        else {
            System.out.println("Tests passed");
        }
    }

    private static boolean testval(Object o, Object v) {
        if (!o.equals(v)) {
            System.err.print("BAD VALUE "+o.toString()+" ");
            result = false;
            return false;
        }
        else {
            System.out.print("OK ");
            return true;
        }
    }

    private static boolean testnull(Object o) {
        if (o == null) {
            System.err.print("NULL ");
            result = false;
            return false;
        }
        else {
            System.out.print("OK ");
            return true;
        }
    }

    private static boolean testinstance(Object o, Class<?> c) {
        if (o == null) {
            System.err.print("NULL INSTANCE ");
            result = false;
            return false;
        }
        if (!c.isInstance(o)) {
            System.err.print("BAD INSTANCE ");
            result = false;
            return false;
        }

        System.out.print("OK ");
        return true;
    }

}
