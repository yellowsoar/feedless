package org.migor.rss.rich.scheduler

import org.migor.rss.rich.model.SourceEntry
import org.migor.rss.rich.repository.SourceEntryRepository
import org.migor.rss.rich.score.ScoreService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.function.Consumer


@Service
class ScoreSourceEntriesScheduler internal constructor() {

  private val log = LoggerFactory.getLogger(ScoreSourceEntriesScheduler::class.simpleName)

  @Autowired
  lateinit var scoreService: ScoreService

  @Autowired
  lateinit var entryRepository: SourceEntryRepository

  @Scheduled(fixedDelay = 4567)
  fun scoreSourceEntries() {
    val pageable = PageRequest.of(0, 100, Sort.by(Sort.Order.asc("createdAt")))
    entryRepository.findAllBySentimentNegative(null, pageable)
      .forEach(Consumer { entry: SourceEntry ->
        try {
          val (positive, neutral, negative) = scoreService.score(entry)
          log.info("Sentiments for ${entry.id}: ${positive}/${neutral}/${negative}")
          entryRepository.updateSentimentById(entry.id!!, positive, neutral, negative)

        } catch (ex: Exception) {
          log.error("Filed while scoring source-Entry ${entry.id}, ${ex.message}")
        }
      })
  }

}

