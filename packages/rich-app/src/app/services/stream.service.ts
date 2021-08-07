import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';

@Injectable({
  providedIn: 'root',
})
export class StreamService {
  constructor(private readonly apollo: Apollo) {}

  getArticles(streamId: string, take: number = 10, skip: number = 0) {
    return this.apollo.query<any>({
      variables: {
        streamId,
        take,
        skip,
      },
      query: gql`
        query ($streamId: String!, $take: Int!, $skip: Int!) {
          articleRefs(
            take: $take
            skip: $skip
            orderBy: { createdAt: desc }
            where: { stream: { every: { id: { equals: $streamId } } } }
          ) {
            createdAt
            favored
            tags
            article {
              id
              date_published
              url
              author
              title
              content_text
              tags
            }
          }
        }
      `,
    });
  }
}
