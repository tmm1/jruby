package org.jruby.compiler.ir.operands;

import java.util.Map;

public abstract class Operand
{
    public static final Operand[] EMPTY_ARRAY = new Operand[0];

// ---------- These methods below are used during compile-time optimizations ------- 
    public boolean isConstant() { return false; }

    // Arrays, Ranges, etc. are compound values
    // Variables, fixnums, floats, etc. are "atomic" values
    public boolean isNonAtomicValue() { return false; }

	 // getSimplifiedOperand returns the value of this operand, fully simplified
    // getSimplifiedOperand returns the operand in a form that can be materialized into bytecode, if it cannot be completely optimized away
	 //
	 // The value is used during optimizations and propagated through the IR.  But, it is thrown away after that.
	 // But, the operand form is used for constructing the compound objects represented by the operand.
	 //
	 // Example: a = [1], b = [3,4], c = [a,b], d = [2,c]
	 //   -- getValue(c) = [1,[3,4]];     getSimplifiedOperand(c) = [1, b]
	 //   -- getValue(d) = [2,[1,[3,4]]]; getSimplifiedOperand(d) = [2, c]
	 //
	 // Note that a,b,c,d are all objects, and c has a reference to objects a and b, and d has a reference to c.
	 // So, if contents of a or b is modified, the "simplified value"s of c and d also change!  This difference
	 // is captured by these two methods.
    public Operand getSimplifiedOperand(Map<Operand, Operand> valueMap) { return this; }
    public Operand getValue(Map<Operand, Operand> valueMap) { return this; }

    // if (getSubArray) is false, returns the 'index' element of the array, else returns the subarray starting at that element
    public Operand fetchCompileTimeArrayElement(int index, boolean getSubArray) { return null; }

//    public abstract Operand toArray();

// ---------- Only static definitions further below ---------

    /* Lattice TOP, BOTTOM, ANY values -- these will be used during dataflow analyses */

    public static final Operand TOP    = new LatticeTop();
    public static final Operand BOTTOM = new LatticeBottom();
    public static final Operand ANY    = new Anything();
  
    private static class LatticeBottom extends Operand
    {
        LatticeBottom() { }
       
        public String toString() { return "bottom"; }
    }
  
    private static class LatticeTop extends Operand
    {
        LatticeTop() { }
       
        public String toString() { return "top"; }
        public Operand Compute_CP_Meet(Operand op) { return op; }
    }
  
    private static class Anything extends Operand
    {
        Anything() { }
       
        public String toString() { return "anything"; }
        public Operand Compute_CP_Meet(Operand op) { return op; }
    }
}