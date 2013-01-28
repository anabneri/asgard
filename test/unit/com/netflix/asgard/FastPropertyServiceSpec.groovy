package com.netflix.asgard

import spock.lang.Specification

class FastPropertyServiceSpec extends Specification {

    UserContext userContext = new UserContext(region: Region.US_EAST_1, internalAutomation: true)
    ConfigService configService = Mock(ConfigService) {
        getRegionalPlatformServiceServer(_) >> 'platformservice.us-east-1.company.net'
        getPlatformServicePort() >> '7001'
        isOnline() >> true
    }
    RestClientService restClientService = Mock(RestClientService)
    Caches caches = new Caches(new MockCachedMapBuilder([
            (EntityType.fastProperty): Mock(CachedMap),
    ]))
    TaskService taskService = new TaskService() {
        def runTask(UserContext userContext, String name, Closure work, Link link = null, Task existingTask = null) {
            work(new Task())
        }
    }
    FastPropertyService service = new FastPropertyService(configService: configService,
            restClientService: restClientService, caches: caches, taskService: taskService)

    def 'should get Fast Property'() {
        when:
        service.get(userContext, 'test invalid +!%/,[]:\\^$|*()')

        then:
        1 * restClientService.getAsXml("http://platformservice.us-east-1.company.net:7001/platformservice/REST/v1/\
props/property/test+invalid+%2B%21%25%2F%2C%5B%5D%3A%5C%5E%24%7C*%28%29")
    }

    def 'should delete Fast Property'() {
        restClientService.getAsXml(_) >>> [['key': 'test'], null]

        when:
        service.deleteFastProperty(userContext, 'test invalid +!%/,[]:\\^$|*()', 'cmccoy', 'us-west-1')

        then:
        1 * restClientService.delete("http://platformservice.us-east-1.company.net:7001/platformservice/REST/v1/\
props/property/test+invalid+%2B%21%25%2F%2C%5B%5D%3A%5C%5E%24%7C*%28%29?source=asgard&updatedBy=cmccoy&cmcTicket=")
    }
}
