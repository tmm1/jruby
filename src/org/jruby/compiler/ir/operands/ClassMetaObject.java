package org.jruby.compiler.ir.operands;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.compiler.ir.IRClass;
import org.jruby.interpreter.InterpreterContext;
import org.jruby.parser.StaticScope;

public class ClassMetaObject extends ModuleMetaObject {
    public ClassMetaObject(IRClass scope) {
        super(scope);
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public Object retrieve(InterpreterContext interp) {
		  // SSS FIXME: why would this be null? for core classes?
        StaticScope ssc =  scope.getStaticScope();
		  return ssc == null ? null : ssc.getModule();
    }
}
