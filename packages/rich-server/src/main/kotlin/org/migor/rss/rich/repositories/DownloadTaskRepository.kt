package org.migor.rss.rich.repositories

import org.migor.rss.rich.models.DownloadTask
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DownloadTaskRepository: CrudRepository<DownloadTask, String> {
}
