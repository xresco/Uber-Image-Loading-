# Uber-Image-Loading

###### The test APK can be found in the project 

- The structure of the project is based on MVP design pattern 
- The project heavily enforces the single responsibility principle of OOP (this can be seen in operations like task scheduling, parsing, API calling, and image downloading )
- The project provides two ways of caching mechansim, storage (file) and memory cahcing.
- The project provided a custom logger, this is to be used to have all (or part) the logs reported to the server for relase builds.


### Scheduler
Scheduler is used to run backgroun processes, it a task that accepts a request data T, and returns response data R. The schedule should make sure the task is ran `properly`. The task shouldn't be aware of the scheduling functionality (the how and when).

##### The project has two types of scheduler: 
- ThreadPoolScheduler:
Used to concurently run multiple tasks (image downloading in the current case). The thread pool size (THREAD_COUNT) can later be optimized to be different based on the devices specs.
- SingleThread: 
This scheduler ensures that the given tasks is executed at most once at a time (no concurrent executions are allowed), in our case fetching the photos should happen once at a time, since we don't need photos to be loaded why the user is already loading them. This scheduler accepts a RetryStrategy as an attribute, which can be either `no-retry` or `Fibonacci` (the current case). Since this is injected in the class and not part of it, then adding other retry strategies is an easy task in the future



### Test classes:
This is an area where things can be improved. The main reason for that is that I'm not allowed to use thrid-party libraries which I'm very used to and comfortable with. For example using okHttp would have made things much easier with the ability to create a mock server. Another example would be Mockito 

##### If those were available my testing strategy would have been: 
- Having a mock server that returns a mock response 
- Test the two cache functionalities using unit test and a mock response 
- Run instrumentation test with different caching mechansing, using a mock response provided by the mock server

