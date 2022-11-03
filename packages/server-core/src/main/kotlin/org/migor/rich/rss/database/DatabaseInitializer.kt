package org.migor.rich.rss.database

import org.migor.rich.rss.database.enums.BucketVisibility
import org.migor.rich.rss.database.enums.GenericFeedStatus
import org.migor.rich.rss.database.models.BucketEntity
import org.migor.rich.rss.database.models.GenericFeedEntity
import org.migor.rich.rss.database.models.ImporterEntity
import org.migor.rich.rss.database.models.StreamEntity
import org.migor.rich.rss.database.models.UserEntity
import org.migor.rich.rss.database.repositories.BucketDAO
import org.migor.rich.rss.database.repositories.GenericFeedDAO
import org.migor.rich.rss.database.repositories.ImporterDAO
import org.migor.rich.rss.database.repositories.StreamDAO
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.NativeFeedService
import org.migor.rich.rss.service.UserService
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct

@Service
class DatabaseInitializer {

  @Autowired
  lateinit var bucketDAO: BucketDAO

  @Autowired
  lateinit var importerDAO: ImporterDAO

  @Autowired
  lateinit var streamDAO: StreamDAO

  @Autowired
  lateinit var genericFeedDAO: GenericFeedDAO

  @Autowired
  lateinit var feedDiscoveryService: FeedDiscoveryService

  @Autowired
  lateinit var nativeFeedService: NativeFeedService

  @Autowired
  lateinit var bucketService: BucketService

  @Autowired
  lateinit var userService: UserService

  @PostConstruct
  @Transactional(propagation = Propagation.REQUIRED)
  fun postConstruct() {
    val corrId = newCorrId()

    val user = userService.createUser(corrId, "system", "system@migor.org")

    createBucketForDanielDennet(user, corrId)
    createBucketForAfterOn(user, corrId)
  }

  private fun createBucketForAfterOn(user: UserEntity, corrId: String) {
    val bucket = bucketService.createBucket("",
      name = "After On Podcast",
      description = "After On Podcast",
      visibility = BucketVisibility.public,
      user = user)
    getNativeFeedForWebsite(corrId, "After On Podcast", "http://afteron.libsyn.com/rss", bucket, true)
  }

  private fun createBucketForDanielDennet(savedUser: UserEntity, corrId: String) {
    val stream = streamDAO.save(StreamEntity())

    val bucket = BucketEntity()
    bucket.stream = stream
    bucket.name = "Daniel Dennet"
    bucket.description = """Daniel Dennett received his D.Phil. in philosophy from Oxford University. He is currently
        |Austin B. Fletcher Professor of Philosophy and co-director of the Center for Cognitive Studies at Tufts
        |University. He is known for a number of philosophical concepts and coinages, including the intentional stance,
        |the Cartesian theater, and the multiple-drafts model of consciousness. Among his honors are the Erasmus Prize,
        |a Guggenheim Fellowship, and the American Humanist Association’s Humanist of the Year award. He is the author
        |of a number of books that are simultaneously scholarly and popular, including Consciousness Explained, Darwin’s
        |Dangerous Idea, and most recently Bacteria to Bach and Back.""".trimMargin()
    bucket.owner = savedUser
    val savedBucket = bucketDAO.save(bucket)

    getGenericFeedForWebsite("Daniel Dennett Blog", "https://ase.tufts.edu/cogstud/dennett/recent.html", savedBucket)
    getGenericFeedForWebsite(
      "Daniel Dennett Google Scholar",
      "https://scholar.google.com/citations?user=3FWe5OQAAAAJ&hl=en",
      savedBucket
    )
    //      getFeedForLinksInWebsite("Daniel Dennett Wikipedia", "https://en.wikipedia.org/wiki/Daniel_Dennett", savedBucket),
    getNativeFeedForWebsite(corrId, "Daniel Dennett Twitter", "https://twitter.com/danieldennett", savedBucket, false)
    getGenericFeedForWebsite("The Clergy Project", "https://clergyproject.org/stories/", savedBucket)
    getGenericFeedForWebsite("Center for Cognitive Studies", "https://ase.tufts.edu/cogstud/news.html", savedBucket)
  }

  private fun getNativeFeedForWebsite(corrId: String, title: String, websiteUrl: String, bucket: BucketEntity, harvestSite: Boolean) {
    val feed = feedDiscoveryService.discoverFeeds(corrId, websiteUrl).results.nativeFeeds.first()

    val nativeFeed = nativeFeedService.createNativeFeed(title, feed.url!!, websiteUrl, harvestSite)

    val importer = ImporterEntity()
    importer.feed = nativeFeed
    importer.bucket = bucket
    importerDAO.save(importer)
  }

  private fun getGenericFeedForWebsite(title: String, websiteUrl: String, bucket: BucketEntity) {
    val corrId = ""
    val bestRule = feedDiscoveryService.discoverFeeds(corrId, websiteUrl).results.genericFeedRules.first()
    val feedRule = feedDiscoveryService.asExtendedRule(corrId, websiteUrl, bestRule)

    val nativeFeed = nativeFeedService.createNativeFeed(title, feedRule.feedUrl, websiteUrl, true)

    val genericFeed = GenericFeedEntity()
    genericFeed.feedRule = feedRule
    genericFeed.managingFeed = nativeFeed
    genericFeed.status = GenericFeedStatus.OK

    genericFeedDAO.save(genericFeed)


    val importer = ImporterEntity()
    importer.feed = nativeFeed
    importer.bucket = bucket
    importerDAO.save(importer)
  }
}
