package lucee.loader.servlet

import java.io.IOException

class LuceeFilter : Filter {
    @Override
    @Throws(ServletException::class)
    fun init(filterConfig: FilterConfig?) {
    }

    @Override
    @Throws(IOException::class, ServletException::class)
    fun doFilter(request: ServletRequest, response: ServletResponse?, chain: FilterChain?) {
        try {
            val engine: CFMLEngine = CFMLEngineFactory.getInstance()
            // FUTURE add exeFilter
            engine.addServletConfig(LuceeFilterImpl(request, response, chain, "filter"))
        } catch (se: Exception) {
            se.printStackTrace()
        }
    }

    @Override
    fun destroy() {
    }

    class LuceeFilterImpl(request: ServletRequest, response: ServletResponse?, chain: FilterChain?, status: String) : ServletConfig {
        private val request: ServletRequest
        private val response: ServletResponse?
        private val chain: FilterChain?
        private val status: String

        @get:Override
        val servletName: String
            get() = "LuceeFilter"

        @get:Override
        val servletContext: ServletContext
            get() = request.getServletContext()
        val servletRequest: ServletRequest
            get() = request
        val servletResponse: ServletResponse?
            get() = response
        val filterChain: FilterChain?
            get() = chain

        @Override
        fun getInitParameter(name: String?): String? {
            return if ("status".equalsIgnoreCase(name)) status else null
        }

        @get:Override
        val initParameterNames: Enumeration<String>
            get() {
                val set: HashSet<String> = HashSet<String>()
                set.add("status")
                return EnumerationWrapper<String>(set)
            }

        init {
            this.request = request
            this.response = response
            this.chain = chain
            this.status = status
        }
    }
}