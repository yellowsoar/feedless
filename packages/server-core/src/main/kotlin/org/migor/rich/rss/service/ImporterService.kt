package org.migor.rich.rss.service

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.enums.ArticleType
import org.migor.rich.rss.data.jpa.enums.ReleaseStatus
import org.migor.rich.rss.data.jpa.models.ArticleEntity
import org.migor.rich.rss.data.jpa.models.BucketEntity
import org.migor.rich.rss.data.jpa.models.ContentEntity
import org.migor.rich.rss.data.jpa.models.ImporterEntity
import org.migor.rich.rss.data.jpa.models.NativeFeedEntity
import org.migor.rich.rss.data.jpa.models.StreamEntity
import org.migor.rich.rss.data.jpa.repositories.ArticleDAO
import org.migor.rich.rss.data.jpa.repositories.BucketDAO
import org.migor.rich.rss.data.jpa.repositories.ContentDAO
import org.migor.rich.rss.data.jpa.repositories.ImporterDAO
import org.migor.rich.rss.data.jpa.repositories.NativeFeedDAO
import org.migor.rich.rss.generated.types.ImporterCreateInput
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile(AppProfiles.database)
class ImporterService {

  private val log = LoggerFactory.getLogger(ImporterService::class.simpleName)

  @Autowired
  lateinit var articleDAO: ArticleDAO

  @Autowired
  lateinit var importerDAO: ImporterDAO

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun importArticleToTargets(
    corrId: String,
    content: ContentEntity,
    stream: StreamEntity,
    feed: NativeFeedEntity,
    articleType: ArticleType,
    status: ReleaseStatus,
    releasedAt: Date,
  ) {
    val contentId = content.id
//    Optional.ofNullable(contentDAO.findInStream(contentId, stream.id))
//      .ifPresentOrElse({
//        log.debug("[${corrId}] already imported")
//      }, {
        log.info("[$corrId] importing content $contentId")

        // default target
        forwardToStream(corrId, content, releasedAt, stream, feed, articleType, status)

//        targets.forEach { target ->
//          when (target) {
////            ExporterTargetType.email -> forwardAsEmail(corrId, articleId, ownerId, pubDate, refType)
////            ExporterTargetType.webhook -> forwardToWebhook(corrId, article, pubDate, target)
//            else -> log.warn("[${corrId}] Unsupported importerTarget $target")
//          }
//        }
//      })

  }

//  private fun forwardAsEmail(
//    corrId: String,
//    articleId: UUID,
//    ownerId: UUID,
//    pubDate: Date,
//    refType: Stream2ArticleEntityType
//  ) {
//    TODO("Not yet implemented")
//  }

  private fun forwardToStream(
    corrId: String,
    content: ContentEntity,
    releasedAt: Date,
    stream: StreamEntity,
    feed: NativeFeedEntity,
    type: ArticleType,
    status: ReleaseStatus
  ) {
    log.debug("[$corrId] append article -> stream $stream")
    val article = ArticleEntity()
    article.content = content
    article.releasedAt = releasedAt
    article.stream = stream
    article.type = type
    article.status = status
    article.feed = feed
    articleDAO.save(article)
  }

  fun createImporter(
    corrId: String,
    nativeFeed: NativeFeedEntity,
    bucket: BucketEntity,
    data: ImporterCreateInput,
  ): ImporterEntity {

    val importer = ImporterEntity()
    importer.feed = nativeFeed
    importer.bucket = bucket
    importer.autoRelease = data.autoRelease
    importer.filter = data.filter
    val saved = importerDAO.save(importer)
    log.debug("[${corrId}] created ${saved.id}")
    return saved
  }

  fun delete(corrId: String, id: UUID) {
    log.debug("[${corrId}] create $id")
    importerDAO.deleteById(id)
  }

  fun findAllByBucketId(id: UUID): List<ImporterEntity> {
    return importerDAO.findAllByBucketId(id)
  }

  fun findById(id: UUID): Optional<ImporterEntity> {
    return importerDAO.findById(id)
  }

  fun findByBucketAndFeed(bucketId: UUID, nativeFeedId: UUID): Optional<ImporterEntity> {
    return importerDAO.findByBucketIdAndFeedId(bucketId, nativeFeedId)
  }

  fun findAllByFeedId(id: UUID): List<ImporterEntity> {
    return importerDAO.findAllByFeedId(id)
  }
}
