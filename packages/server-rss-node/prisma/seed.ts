import { PrismaClient } from '@prisma/client';
import { readFileSync } from 'fs';
import { EventHookType } from '../src/services/plugin/plugin.service';
import { OpmlService } from '../src/services/opml/opml.service';
import { FeedService } from '../src/services/feed/feed.service';
import { PrismaService } from '../src/modules/prisma/prisma.service';
import { RichJsonService } from '../src/services/rich-json/rich-json.service';

const prisma = new PrismaClient();

async function main() {
  console.log(`Start seeding ...`);

  await prisma.user.create({
    data: {
      id: 'system',
      name: 'system',
      email: 'sys@tem.ch',
      settings: {
        create: {},
      },
    },
  });
  await prisma.user.create({
    data: {
      id: 'john',
      name: 'john doe',
      email: 'john@doe.ch',
      settings: {
        create: {},
      },
    },
  });

  const user = await prisma.user.create({
    data: {
      name: 'Karl May',
      email: 'karl@may.ch',
      eventHooks: {
        create: [
          {
            event: 'feed.resolve',
            type: EventHookType.script,
            script_or_url: 'setResult({value: "foo"})',
          },
        ],
      },
      settings: {
        create: {},
      },
    },
  });

  const noFollowUrls = ['https://www.paypal.me', 'https://www.patreon.com'];
  await Promise.all(
    noFollowUrls.map((noFollowUrl) =>
      prisma.noFollowUrl.create({
        data: {
          url_prefix: noFollowUrl,
        },
      }),
    ),
  );

  const postProcessors = ['FOLLOW_LINKS'];
  await Promise.all(
    postProcessors.map((type) =>
      prisma.articlePostProcessor.upsert({
        where: {
          type,
        },
        create: {
          type,
        },
        update: {
          type,
        },
      }),
    ),
  );

  await prisma.user.update({
    where: {
      id: user.id,
    },
    data: {
      notebooks: {
        create: [
          {
            name: 'inbox',
            readonly: true,
            stream: {
              create: {
                articleRefs: {
                  create: {
                    article: {
                      create: {
                        title: 'from mail',
                        content_raw: '',
                        content_raw_mime: 'text/plain',
                      },
                    },
                  },
                },
              },
            },
          },
          {
            name: 'notifications',
            readonly: true,
            stream: {
              create: {
                articleRefs: {
                  create: {
                    article: {
                      create: {
                        title: '@foo follows you',
                        content_raw: 'text/plain',
                      },
                    },
                  },
                },
              },
            },
          },
          {
            name: 'archive',
            stream: {
              create: {},
            },
          },
        ],
      },
    },
  });

  const prismaService = new PrismaService();
  const opmlService = new OpmlService(
    prismaService,
    new FeedService(prismaService),
  );
  // const file = 'resources/sources-opml.xml';
  // console.log(`From file ${file}`);
  // const opml = readFileSync(file);
  // await opmlService
  //   .createBucketsFromOpml(
  //     Buffer.from(opml.toString('utf-8'), 'utf-8').toString('base64'),
  //     user,
  //   )
  //   .catch(console.error);

  const richJsonService = new RichJsonService(
    prismaService,
    new FeedService(prismaService),
  );
  const richJson = readFileSync('resources/sources-rich.json');
  await richJsonService
    .createBucketsFromRichJson(JSON.parse(richJson.toString('utf-8')), user)
    .catch(console.error);
}

main()
  .then(() => console.log(`Seeding finished.`))
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
