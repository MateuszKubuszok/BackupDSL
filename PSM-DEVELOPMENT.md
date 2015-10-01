Platform Specific Model development
===

Platform specific models are resolved during runtime and so we couldn't
implement them using standard cake pattern. Instead our services are
proxies to actual implementations which might be smaller cakes.
 
`ComponentRegistry` collects all declared service components e.g.
`ElevationServiceComponent`, while the actual implementations are decided in
`*ServiceComponentImpl` implementation (e.g. `ElevationServiceComponentImpl`).
It chooses among some specific implementations (e.g.
`GKSudoElevationServiceComponent`) which do not rely on `ComponentRegistry` to
avoid cyclic dependency and stack overflow.

Tests and Platform Tests
---

`sbt test` command runs unit and functional tests (tagged as `UnitTest` and
`FunctionalTest`). All tests performed are platform independent as they do not
interact with operating system.

With `sbt platform:test` we run only tests tagged as `PlatformTest`. Those are
QA-like tests requiring special attention and advisory - they operate similarly
to integration tests in that they test current implementation as a whole, but
since they do interact with live environment and as such can deal real damage
to the whole system.

It is recommended to run them in some isolated environment like a virtual
machine to make sure that service actually works, before freezing
implementation with unit tests - since UT isolates each component testing they
can only test whether implementation match specification, not whether
specification itself describes working solution.

Development
---

To create new platform specific service one must:

 *  prepare target environment for initial implementation tests: have installed
    intended operating system, DE, package manager,
    
 *  create service and service component using existing ones as a template.
    Remember about specifying conditions when service should be available,
    
 *  add service to list within respectively `*ServiceComponentImpl`,
 
 *  run:

    * `sbt platform:testOnly pl.combosolutions.backup.psm.elevation.PlatformSpecificAdvisedTest`
      for elevation,
    * `sbt platform:testOnly pl.combosolutions.backup.psm.repositories.PlatformSpecificAdvisedTest`
      for repository
      
    platform testing. Supply tests with test data in case none of existing
    matches your config. Passing platform tests should indicate that service
    fulfils its role and can have its implementation frozen,
    
 *  write unit tests to freeze service specification and check for corner
    cases. Make sure that unit tests are tagged as `UnitTest` and DO NOT RELY ON
    A PLATFORM IN ANY WAY.
