package lucee.runtime.instrumentation

import java.lang.instrument.Instrumentation

object ExternalAgent {
    /*
					System.out.println("start set instrumentation");
					System.out.println(Thread.currentThread().getContextClassLoader().getClass().getName());
					System.out.println(ClassLoader.getSystemClassLoader().getClass().getName());
					System.out.println(new ExternalAgent().getClass().getClassLoader().getClass().getName());
					*/  var instrumentation: Instrumentation? = null
        private set(inst) {
            if (inst != null) {
                try {
                    /*
					System.out.println("start set instrumentation");
					System.out.println(Thread.currentThread().getContextClassLoader().getClass().getName());
					System.out.println(ClassLoader.getSystemClassLoader().getClass().getName());
					System.out.println(new ExternalAgent().getClass().getClassLoader().getClass().getName());
					*/
                    field = inst
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }

    fun premain(agentArgs: String?, inst: Instrumentation?) {
        instrumentation = inst
    }

    fun agentmain(agentArgs: String?, inst: Instrumentation?) {
        instrumentation = inst
    }
}