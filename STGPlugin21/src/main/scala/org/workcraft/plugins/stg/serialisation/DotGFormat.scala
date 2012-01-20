package org.workcraft.plugins.stg.serialisation

case class DotGData(
    inputs : Iterable[String]
    , outputs : Iterable[String]
    , internals : Iterable[String]
    , dummies : Iterable[String]
    , places : Iterable[String]
    , entries : Iterable[(String, Iterable[String])]
    , marking : Iterable[(Either[String, (String, String)], Int)]
    ) {
}
