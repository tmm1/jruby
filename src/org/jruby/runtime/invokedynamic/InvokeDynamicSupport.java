package org.jruby.runtime.invokedynamic;

import java.dyn.CallSite;
import java.dyn.JavaMethodHandle;
import java.dyn.Linkage;
import java.dyn.MethodHandle;
import java.dyn.MethodHandles;
import java.dyn.MethodType;
import java.util.HashMap;
import java.util.Map;
import org.jruby.RubyClass;
import org.jruby.RubyLocalJumpError;
import org.jruby.RubyModule;
import org.jruby.compiler.impl.SkinnyMethodAdapter;
import org.jruby.exceptions.JumpException;
import org.jruby.internal.runtime.methods.DynamicMethod;
import org.jruby.javasupport.util.RuntimeHelpers;
import org.jruby.runtime.Block;
import org.jruby.runtime.CallType;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.callsite.CacheEntry;
import static org.jruby.util.CodegenUtils.*;
import org.objectweb.asm.MethodVisitor;

public class InvokeDynamicSupport {
    public static class JRubyCallSite extends CallSite {
        private final CallType callType;

        public JRubyCallSite(Class caller, String name, MethodType type, CallType callType) {
            super(caller, name, type);
            this.callType = callType;
        }

        public CallType callType() {
            return callType;
        }
    }

    public static CallSite bootstrap(Class caller, String name, MethodType type) {
        JRubyCallSite site;

        if (name == "call") {
            site = new JRubyCallSite(caller, name, type, CallType.NORMAL);
        } else {
            site = new JRubyCallSite(caller, name, type, CallType.FUNCTIONAL);
        }
        
        MethodType fallbackType = type.insertParameterTypes(0, JRubyCallSite.class);
        MethodHandle myFallback = MethodHandles.insertArguments(
                MethodHandles.lookup().findStatic(InvokeDynamicSupport.class, "fallback",
                fallbackType),
                0,
                site);
        site.setTarget(myFallback);
        return site;
    }
    
    public static void registerBootstrap(Class cls) {
        Linkage.registerBootstrapMethod(cls, BOOTSTRAP);
    }
    
    public static void installBytecode(MethodVisitor method, String classname) {
        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(method);
        mv.ldc(c(classname));
        mv.invokestatic(p(Class.class), "forName", sig(Class.class, params(String.class)));
        mv.invokestatic(p(InvokeDynamicSupport.class), "registerBootstrap", sig(void.class, Class.class));
    }

    private static MethodHandle createGWT(MethodHandle test, MethodHandle target, MethodHandle fallback, CacheEntry entry, CallSite site) {
        MethodHandle myTest = MethodHandles.insertArguments(test, 0, entry);
        MethodHandle myTarget = MethodHandles.insertArguments(target, 0, entry);
        MethodHandle myFallback = MethodHandles.insertArguments(fallback, 0, site);
        MethodHandle guardWithTest = MethodHandles.guardWithTest(myTest, myTarget, myFallback);
        
        return MethodHandles.convertArguments(guardWithTest, site.type());
    }

    public static boolean test(CacheEntry entry, IRubyObject self) {
        return entry.typeOk(self.getMetaClass());
    }

    public static IRubyObject fallback(JRubyCallSite site, ThreadContext context, IRubyObject caller, IRubyObject self, String name) throws Throwable {
        CacheHandle handle = new CacheHandle(name, CacheHandle.ZERO);
        site.setTarget(handle);

        return handle.<IRubyObject>invoke(context, caller, self, name);
    }

    public static IRubyObject fallback(JRubyCallSite site, ThreadContext context, IRubyObject caller, IRubyObject self, String name, IRubyObject arg0) throws Throwable {
        CacheHandle handle = new CacheHandle(name, CacheHandle.ONE);
        site.setTarget(handle);

        return handle.<IRubyObject>invoke(context, caller, self, name, arg0);
    }

    public static IRubyObject fallback(JRubyCallSite site, ThreadContext context, IRubyObject caller, IRubyObject self, String name, IRubyObject arg0, IRubyObject arg1) throws Throwable {
        CacheHandle handle = new CacheHandle(name, CacheHandle.TWO);
        site.setTarget(handle);

        return handle.<IRubyObject>invoke(context, caller, self, name, arg0, arg1);
    }

    public static IRubyObject fallback(JRubyCallSite site, ThreadContext context, IRubyObject caller, IRubyObject self, String name, IRubyObject arg0, IRubyObject arg1, IRubyObject arg2) throws Throwable {
        CacheHandle handle = new CacheHandle(name, CacheHandle.THREE);
        site.setTarget(handle);

        return handle.<IRubyObject>invoke(context, caller, self, name, arg0, arg1, arg2);
    }

    public static IRubyObject fallback(JRubyCallSite site, ThreadContext context, IRubyObject caller, IRubyObject self, String name, IRubyObject[] args) throws Throwable {
        CacheHandle handle = new CacheHandle(name, CacheHandle.N);
        site.setTarget(handle);

        return handle.<IRubyObject>invoke(context, caller, self, name, args);
    }

    public static IRubyObject fallback(JRubyCallSite site, ThreadContext context, IRubyObject caller, IRubyObject self, String name, Block block) throws Throwable {
        CacheHandle handle = new CacheHandle(name, CacheHandle.ZERO_BLOCK);
        site.setTarget(handle);

        return handle.<IRubyObject>invoke(context, caller, self, name, block);
    }

    public static IRubyObject fallback(JRubyCallSite site, ThreadContext context, IRubyObject caller, IRubyObject self, String name, IRubyObject arg0, Block block) throws Throwable {
        CacheHandle handle = new CacheHandle(name, CacheHandle.ONE_BLOCK);
        site.setTarget(handle);

        return handle.<IRubyObject>invoke(context, caller, self, name, arg0, block);
    }

    public static IRubyObject fallback(JRubyCallSite site, ThreadContext context, IRubyObject caller, IRubyObject self, String name, IRubyObject arg0, IRubyObject arg1, Block block) throws Throwable {
        CacheHandle handle = new CacheHandle(name, CacheHandle.TWO_BLOCK);
        site.setTarget(handle);

        return handle.<IRubyObject>invoke(context, caller, self, name, arg0, arg1, block);
    }

    public static IRubyObject fallback(JRubyCallSite site, ThreadContext context, IRubyObject caller, IRubyObject self, String name, IRubyObject arg0, IRubyObject arg1, IRubyObject arg2, Block block) throws Throwable {
        CacheHandle handle = new CacheHandle(name, CacheHandle.THREE_BLOCK);
        site.setTarget(handle);

        return handle.<IRubyObject>invoke(context, caller, self, name, arg0, arg1, arg2, block);
    }

    public static IRubyObject fallback(JRubyCallSite site, ThreadContext context, IRubyObject caller, IRubyObject self, String name, IRubyObject[] args, Block block) throws Throwable {
        CacheHandle handle = new CacheHandle(name, CacheHandle.N_BLOCK);
        site.setTarget(handle);

        return handle.<IRubyObject>invoke(context, caller, self, name, args, block);
    }

    public static RubyClass pollAndGetClass(ThreadContext context, IRubyObject self) {
        context.callThreadPoll();
        RubyClass selfType = self.getMetaClass();
        return selfType;
    }

    private static final MethodType BOOTSTRAP_TYPE = MethodType.methodType(CallSite.class, Class.class, String.class, MethodType.class);
    private static final MethodHandle BOOTSTRAP = MethodHandles.lookup().findStatic(InvokeDynamicSupport.class, "bootstrap", BOOTSTRAP_TYPE);

    private static class CacheHandle extends JavaMethodHandle {
        private CacheEntry cache = CacheEntry.NULL_CACHE;
        private final String methodName;
        public static final MethodType[] NO_BLOCK_TYPES = new MethodType[] {
            MethodType.methodType(IRubyObject.class, ThreadContext.class, IRubyObject.class, IRubyObject.class, String.class),
            MethodType.methodType(IRubyObject.class, ThreadContext.class, IRubyObject.class, IRubyObject.class, String.class, IRubyObject.class),
            MethodType.methodType(IRubyObject.class, ThreadContext.class, IRubyObject.class, IRubyObject.class, String.class, IRubyObject.class, IRubyObject.class),
            MethodType.methodType(IRubyObject.class, ThreadContext.class, IRubyObject.class, IRubyObject.class, String.class, IRubyObject.class, IRubyObject.class, IRubyObject.class),
            MethodType.methodType(IRubyObject.class, ThreadContext.class, IRubyObject.class, IRubyObject.class, String.class, IRubyObject[].class),
        };
        public static final MethodType[] BLOCK_TYPES = new MethodType[] {
            MethodType.methodType(IRubyObject.class, ThreadContext.class, IRubyObject.class, IRubyObject.class, String.class, Block.class),
            MethodType.methodType(IRubyObject.class, ThreadContext.class, IRubyObject.class, IRubyObject.class, String.class, IRubyObject.class, Block.class),
            MethodType.methodType(IRubyObject.class, ThreadContext.class, IRubyObject.class, IRubyObject.class, String.class, IRubyObject.class, IRubyObject.class, Block.class),
            MethodType.methodType(IRubyObject.class, ThreadContext.class, IRubyObject.class, IRubyObject.class, String.class, IRubyObject.class, IRubyObject.class, IRubyObject.class, Block.class),
            MethodType.methodType(IRubyObject.class, ThreadContext.class, IRubyObject.class, IRubyObject.class, String.class, IRubyObject[].class, Block.class),
        };
        public static final Map<MethodType, MethodHandle> ALL_HANDLES = new HashMap<MethodType, MethodHandle>();
        public static final MethodHandle ZERO = MethodHandles.lookup().findVirtual(CacheHandle.class, "invoke", NO_BLOCK_TYPES[0]);
        public static final MethodHandle ONE = MethodHandles.lookup().findVirtual(CacheHandle.class, "invoke", NO_BLOCK_TYPES[1]);
        public static final MethodHandle TWO = MethodHandles.lookup().findVirtual(CacheHandle.class, "invoke", NO_BLOCK_TYPES[2]);
        public static final MethodHandle THREE = MethodHandles.lookup().findVirtual(CacheHandle.class, "invoke", NO_BLOCK_TYPES[3]);
        public static final MethodHandle N = MethodHandles.lookup().findVirtual(CacheHandle.class, "invoke", NO_BLOCK_TYPES[4]);
        public static final MethodHandle ZERO_BLOCK = MethodHandles.lookup().findVirtual(CacheHandle.class, "invoke", BLOCK_TYPES[0]);
        public static final MethodHandle ONE_BLOCK = MethodHandles.lookup().findVirtual(CacheHandle.class, "invoke", BLOCK_TYPES[1]);
        public static final MethodHandle TWO_BLOCK = MethodHandles.lookup().findVirtual(CacheHandle.class, "invoke", BLOCK_TYPES[2]);
        public static final MethodHandle THREE_BLOCK = MethodHandles.lookup().findVirtual(CacheHandle.class, "invoke", BLOCK_TYPES[3]);
        public static final MethodHandle N_BLOCK = MethodHandles.lookup().findVirtual(CacheHandle.class, "invoke", BLOCK_TYPES[4]);
        static {
            ALL_HANDLES.put(NO_BLOCK_TYPES[0], ZERO);
            ALL_HANDLES.put(NO_BLOCK_TYPES[1], ONE);
            ALL_HANDLES.put(NO_BLOCK_TYPES[2], TWO);
            ALL_HANDLES.put(NO_BLOCK_TYPES[3], THREE);
            ALL_HANDLES.put(NO_BLOCK_TYPES[4], N);

            ALL_HANDLES.put(BLOCK_TYPES[0], ZERO_BLOCK);
            ALL_HANDLES.put(BLOCK_TYPES[1], ONE_BLOCK);
            ALL_HANDLES.put(BLOCK_TYPES[2], TWO_BLOCK);
            ALL_HANDLES.put(BLOCK_TYPES[3], THREE_BLOCK);
            ALL_HANDLES.put(BLOCK_TYPES[4], N_BLOCK);
        }
        
        public CacheHandle(String methodName, MethodHandle entry) {
            super(entry);
            this.methodName = methodName;
        }

        public IRubyObject invoke(ThreadContext context,
                IRubyObject caller,
                IRubyObject self,
                String unused) throws Throwable {
            RubyClass selfType = pollAndGetClass(context, self);
            CacheEntry myCache = cache;
            DynamicMethod method;
            if (CacheEntry.typeOk(myCache, selfType)) {
                method = myCache.method;
            }
            method = cacheAndGet(context, selfType);
            return method.call(context, self, selfType, methodName);
        }

        public IRubyObject invoke(ThreadContext context,
                IRubyObject caller,
                IRubyObject self,
                String unused,
                IRubyObject arg0) throws Throwable {
            RubyClass selfType = pollAndGetClass(context, self);
            CacheEntry myCache = cache;
            DynamicMethod method;
            if (CacheEntry.typeOk(myCache, selfType)) {
                method = myCache.method;
            }
            method = cacheAndGet(context, selfType);
            return method.call(context, self, selfType, methodName, arg0);
        }

        public IRubyObject invoke(ThreadContext context,
                IRubyObject caller,
                IRubyObject self,
                String unused,
                IRubyObject arg0,
                IRubyObject arg1) throws Throwable {
            RubyClass selfType = pollAndGetClass(context, self);
            CacheEntry myCache = cache;
            DynamicMethod method;
            if (CacheEntry.typeOk(myCache, selfType)) {
                method = myCache.method;
            }
            method = cacheAndGet(context, selfType);
            return method.call(context, self, selfType, methodName, arg0, arg1);
        }

        public IRubyObject invoke(ThreadContext context,
                IRubyObject caller,
                IRubyObject self,
                String unused,
                IRubyObject arg0,
                IRubyObject arg1,
                IRubyObject arg2) throws Throwable {
            RubyClass selfType = pollAndGetClass(context, self);
            CacheEntry myCache = cache;
            DynamicMethod method;
            if (CacheEntry.typeOk(myCache, selfType)) {
                method = myCache.method;
            }
            method = cacheAndGet(context, selfType);
            return method.call(context, self, selfType, methodName, arg0, arg1, arg2);
        }

        public IRubyObject invoke(ThreadContext context,
                IRubyObject caller,
                IRubyObject self,
                String unused,
                IRubyObject[] args) throws Throwable {
            RubyClass selfType = pollAndGetClass(context, self);
            CacheEntry myCache = cache;
            DynamicMethod method;
            if (CacheEntry.typeOk(myCache, selfType)) {
                method = myCache.method;
            }
            method = cacheAndGet(context, selfType);
            return method.call(context, self, selfType, methodName, args);
        }

        public IRubyObject invoke(ThreadContext context,
                IRubyObject caller,
                IRubyObject self,
                String unused,
                Block block) throws Throwable {
            RubyClass selfType = pollAndGetClass(context, self);
            CacheEntry myCache = cache;
            DynamicMethod method;
            if (CacheEntry.typeOk(myCache, selfType)) {
                method = myCache.method;
            }
            method = cacheAndGet(context, selfType);
            return method.call(context, self, selfType, methodName, block);
        }

        public IRubyObject invoke(ThreadContext context,
                IRubyObject caller,
                IRubyObject self,
                String unused,
                IRubyObject arg0,
                Block block) throws Throwable {
            RubyClass selfType = pollAndGetClass(context, self);
            CacheEntry myCache = cache;
            DynamicMethod method;
            if (CacheEntry.typeOk(myCache, selfType)) {
                method = myCache.method;
            }
            method = cacheAndGet(context, selfType);
            return method.call(context, self, selfType, methodName, arg0, block);
        }

        public IRubyObject invoke(ThreadContext context,
                IRubyObject caller,
                IRubyObject self,
                String unused,
                IRubyObject arg0,
                IRubyObject arg1,
                Block block) throws Throwable {
            RubyClass selfType = pollAndGetClass(context, self);
            CacheEntry myCache = cache;
            DynamicMethod method;
            if (CacheEntry.typeOk(myCache, selfType)) {
                method = myCache.method;
            }
            method = cacheAndGet(context, selfType);
            return method.call(context, self, selfType, methodName, arg0, arg1, block);
        }

        public IRubyObject invoke(ThreadContext context,
                IRubyObject caller,
                IRubyObject self,
                String unused,
                IRubyObject arg0,
                IRubyObject arg1,
                IRubyObject arg2,
                Block block) throws Throwable {
            RubyClass selfType = pollAndGetClass(context, self);
            CacheEntry myCache = cache;
            DynamicMethod method;
            if (CacheEntry.typeOk(myCache, selfType)) {
                method = myCache.method;
            }
            method = cacheAndGet(context, selfType);
            return method.call(context, self, selfType, methodName, arg0, arg1, arg2, block);
        }

        public IRubyObject invoke(ThreadContext context,
                IRubyObject caller,
                IRubyObject self,
                String unused,
                IRubyObject[] args,
                Block block) throws Throwable {
            RubyClass selfType = pollAndGetClass(context, self);
            CacheEntry myCache = cache;
            DynamicMethod method;
            if (CacheEntry.typeOk(myCache, selfType)) {
                method = myCache.method;
            }
            method = cacheAndGet(context, selfType);
            return method.call(context, self, selfType, methodName, args, block);
        }

        private DynamicMethod cacheAndGet(ThreadContext context, RubyClass selfType) {
            CacheEntry myCache = selfType.searchWithCache(methodName);
            DynamicMethod method = myCache.method;
            if (method.isUndefined()) {
                return RuntimeHelpers.selectMethodMissing(context, selfType, method.getVisibility(), methodName, CallType.FUNCTIONAL);
            }
            cache = myCache;
            return method;
        }
    }
}