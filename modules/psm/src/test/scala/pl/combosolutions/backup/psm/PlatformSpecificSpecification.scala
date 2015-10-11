package pl.combosolutions.backup.psm

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

trait PlatformSpecificSpecification
    extends Specification
    with Mockito
    with ComponentsHelper {

  sequential // tests might interact with each other and break one another
}
