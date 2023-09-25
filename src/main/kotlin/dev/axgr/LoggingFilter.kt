package dev.axgr

import jakarta.servlet.FilterChain
import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.StreamUtils
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

@Component
class LoggingFilter : OncePerRequestFilter() {

  companion object {
    private val log = LoggerFactory.getLogger(LoggingFilter::class.java)
  }

  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
    val builder = StringBuilder()
    val wrapper = RepeatableContentCachingRequestWrapper(request)

    builder.appendLine("Request:")
    builder.appendLine("${wrapper.method} ${wrapper.requestURI}")
    wrapper.headerNames.asIterator().forEach { header ->
      builder.appendLine("$header: ${wrapper.getHeader(header)}")
    }

    builder.appendLine()
    builder.appendLine(wrapper.contentAsString)
    log.info(builder.toString())

    chain.doFilter(wrapper, response)
  }
}

class RepeatableContentCachingRequestWrapper(request: HttpServletRequest) : ContentCachingRequestWrapper(request) {

  init {
    StreamUtils.drain(super.getInputStream())
  }

  @Throws(IOException::class)
  override fun getInputStream(): ServletInputStream {
    return ByteServletInputStream(contentAsByteArray)
  }

  private class ByteServletInputStream(content: ByteArray) : ServletInputStream() {
    private val stream: InputStream

    init {
      stream = ByteArrayInputStream(content)
    }

    override fun isFinished(): Boolean {
      return true
    }

    override fun isReady(): Boolean {
      return true
    }

    override fun setReadListener(readListener: ReadListener) {}

    @Throws(IOException::class)
    override fun read(): Int {
      return stream.read()
    }

    @Throws(IOException::class)
    override fun close() {
      stream.close()
    }
  }
}
