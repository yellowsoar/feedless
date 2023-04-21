package org.migor.rich.rss.harvest

import io.micrometer.core.annotation.Timed
import org.migor.rich.rss.transform.FetchOptions
import reactor.core.publisher.Flux

interface PuppeteerService {

  fun canPrerender(): Boolean

  @Timed
  fun prerender(
    corrId: String,
    options: FetchOptions,
  ): Flux<PuppeteerHttpResponse>
}

data class PuppeteerHttpResponse(
  val dataBase64: String?,
  val dataAscii: String?,
  val url: String,
  val errorMessage: String?,
  val isError: Boolean,
)
